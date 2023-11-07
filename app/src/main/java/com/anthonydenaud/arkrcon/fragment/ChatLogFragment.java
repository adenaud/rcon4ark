package com.anthonydenaud.arkrcon.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.anthonydenaud.arkrcon.model.Server;
import com.anthonydenaud.arkrcon.service.LogService;
import com.anthonydenaud.arkrcon.service.NotificationService;

import com.anthonydenaud.arkrcon.R;
import com.anthonydenaud.arkrcon.service.ArkService;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;

import timber.log.Timber;
import toothpick.Scope;
import toothpick.Toothpick;

public class ChatLogFragment extends RconFragment implements View.OnClickListener, TextView.OnEditorActionListener {


    @Inject
    ArkService arkService;

    @Inject
    LogService logService;

    @Inject
    NotificationService notificationService;

    WebView webViewLog;

    EditText editTextChatSend;

    Button buttonChat;

    Button buttonBroadcast;

    private Context context;
    private String message;
    private SharedPreferences preferences;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getActivity();
        this.logService = new LogService();
        this.preferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);

        Scope s = Toothpick.openScopes(getActivity().getApplication(), this);
        Toothpick.inject(this, s);

    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        arkService.addServerResponseDispatcher(this);

        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        webViewLog = view.findViewById(R.id.webview_log);
        editTextChatSend = view.findViewById(R.id.editext_chat_send);
        buttonChat = view.findViewById(R.id.btn_chat);
        buttonBroadcast = view.findViewById(R.id.btn_broadcast);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        buttonChat.setOnClickListener(this);
        buttonBroadcast.setOnClickListener(this);

        editTextChatSend.setText(message);
        editTextChatSend.setOnEditorActionListener(this);

        Server server = getActivity().getIntent().getParcelableExtra("server");
        String log = "";
        if (preferences.getBoolean("save_log", true)) {
            log = logService.readLatest(getActivity(), server);
        }
        initWebView(log);

    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView(final String content) {
        String template = "";
        try {
            template = IOUtils.toString(getResources().openRawResource(R.raw.template));
        } catch (IOException e) {
            Timber.e(e);
        }
        final String finalTemplate = template;
        String encodedHtml = Base64.encodeToString(finalTemplate.getBytes(), Base64.NO_PADDING);
        webViewLog.loadData(encodedHtml, "text/html", "base64");
        webViewLog.setWebChromeClient(new WebChromeClient());
        webViewLog.getSettings().setJavaScriptEnabled(true);
        webViewLog.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                addLogTextBefore(content);
            }
        });
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.menu_logs, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_log_history) {
            final Server server = getActivity().getIntent().getParcelableExtra("server");
            final List<String> files = logService.listArchives(getContext(), server);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.select_file));

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_list_item_1, files);
            builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String filename = files.get(which);
                    initWebView(logService.readArchive(getContext(), server, filename));
                    dialog.dismiss();
                }
            });
            builder.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        if (getActivity() != null && getActivity().getIntent().hasExtra("chat_notification")) {
            notificationService.notificationClicked();
        }
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        message = editTextChatSend.getText().toString();

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        arkService.removeServerResponseDispatcher(this);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v.equals(buttonChat)) {
            arkService.serverChat(editTextChatSend.getText().toString());
            editTextChatSend.setText("");
        }
        if (v.equals(buttonBroadcast)) {
            arkService.broadcast(editTextChatSend.getText().toString());
            editTextChatSend.setText("");
        }
    }

    @Override
    public void onGetChat(final String chatBuffer) {
        if (StringUtils.isNotEmpty(chatBuffer)) {
            notificationService.handleChatKeyword(getActivity(), chatBuffer);
            writeLog(formatHtml(chatBuffer));
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> addLogTextAfter(chatBuffer));
            }
        }
    }

    @Override
    public void onGetLog(final String logBuffer) {
        if (StringUtils.isNotEmpty(logBuffer)) {
            notificationService.handleChatKeyword(getActivity(), logBuffer);
            writeLog(formatHtml(logBuffer));
            if (getActivity() != null) {
                getActivity().runOnUiThread((Runnable) () -> addLogTextAfter(logBuffer));
            }
        }
    }


    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        boolean handled = false;
        if (v.equals(editTextChatSend) && actionId == EditorInfo.IME_ACTION_SEND) {
            arkService.serverChat(editTextChatSend.getText().toString());
            editTextChatSend.setText("");
            handled = true;
        }
        return handled;
    }

    private void addLogTextBefore(final String text) {
        Thread htmlThread = new Thread(() -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                            String log = text.replaceAll("\n", "");
                            webViewLog.loadUrl("javascript:addLogTextBefore('" + log + "');");
                            forceScroll();
                        }
                );
            }
        }, "HtmlThreadBefore");
        htmlThread.start();
    }

    private void addLogTextAfter(final String text) {
        Thread htmlThread = new Thread(() -> {

            final String htmlToAdd = formatHtml(text);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                            String log = htmlToAdd.replaceAll("\n", "");
                            webViewLog.loadUrl("javascript:addLogTextAfter('" + log + "');");
                            scrollDown();
                        }
                );
            }
        }, "HtmlThreadAfter");
        htmlThread.start();
    }

    private String formatHtml(String content) {
        content = content.trim();
        content = content.replaceAll("'", "\\\\'");
        content += "\n";
        String html = content.replaceAll("(.*)left this ARK!", "<span class=\"joinleft\">$0</span>");
        html = html.replaceAll("(.*)joined this ARK!", "<span class=\"joinleft\">$0</span>");
        html = html.replaceAll("(.*)was killed by(.*)", "<span class=\"kill\">$0</span>");
        html = html.replaceAll("(.*)was killed!", "<span class=\"kill\">$0</span>");
        html = html.replaceAll("(.*)Tamed an? ([A-z ]*) - (.*)", "<span class=\"tame\">$0</span>");
        html = html.replaceAll("(.*)SERVER:(.*)", "<span class=\"server\">$0</span>");
        html = html.replaceAll("(.*)AdminCmd:(.*)", "<span class=\"server\">$0</span>");
        html = html.replaceAll("\\n", "<br>\n");
        html = html.replaceAll("<br>\\n <br>\\n ", "<br>\n");

        return html;
    }

    public void scrollDown() {
        if (preferences.getBoolean("chat_auto_scroll", true) && webViewLog != null) {
            webViewLog.loadUrl("javascript:scrollDown()");
        }
    }

    public void scrollDown(boolean scroll) {
        if (scroll && webViewLog != null) {
            webViewLog.loadUrl("javascript:scrollDown()");
        }
    }

    private void forceScroll() {
        if (webViewLog != null) {
            webViewLog.loadUrl("javascript:scrollDown()");
        }
    }

    private void writeLog(String log) {
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (preferences.getBoolean("save_log", true)) {
            if (!logService.write(getActivity(), (Server) getActivity().getIntent().getParcelableExtra("server"), log)) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.error_log_write, Snackbar.LENGTH_SHORT).show();
            }
        }
    }
}
