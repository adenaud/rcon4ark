package com.anthonydenaud.arkrcon.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.anthonydenaud.arkrcon.model.Server;
import com.anthonydenaud.arkrcon.service.LogService;
import com.google.inject.Inject;

import com.anthonydenaud.arkrcon.R;
import com.anthonydenaud.arkrcon.service.ArkService;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

import roboguice.inject.InjectView;
import roboguice.util.Ln;

public class ChatLogFragment extends RconFragment implements View.OnClickListener, TextView.OnEditorActionListener {

    @Inject
    private ArkService arkService;

    @Inject
    private LogService logService;


    @InjectView(R.id.webview_log)
    private WebView webViewLog;


    @InjectView(R.id.editext_chat_send)
    private EditText editTextChatSend;

    @InjectView(R.id.btn_chat)
    private Button buttonChat;

    @InjectView(R.id.btn_broadcast)
    private Button buttonBroadcast;


    private Activity context;
    private String output = "";
    private String message;
    private SharedPreferences preferences;

    @Override
    public void onAttach(Context context) {
        this.context = (Activity) context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        arkService.addServerResponseDispatcher(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        WebSettings webSettings = webViewLog.getSettings();
        webSettings.setJavaScriptEnabled(true);

        buttonChat.setOnClickListener(this);
        buttonBroadcast.setOnClickListener(this);

        if (preferences.getBoolean("save_log", false)) {
            Server server = getActivity().getIntent().getParcelableExtra("server");
            output = logService.read(getActivity(), server);
        }


        editTextChatSend.setText(message);
        editTextChatSend.setOnEditorActionListener(this);


        String template = "";
        try {
            template = IOUtils.toString(getResources().openRawResource(R.raw.template));
        } catch (IOException e) {
            Ln.e(e);
        }
        final String finalTemplate = template;
        webViewLog.loadData(finalTemplate, "text/html", "UTF-8");
        webViewLog.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                addLogTextBefore(output);
            }
        });
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
    public void onGetLog(final String logBuffer) {
        if (StringUtils.isNotEmpty(logBuffer)) {
            writeLog(formatHtml(logBuffer));
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addLogTextAfter(logBuffer);
                }
            });
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
        Thread htmlThread = new Thread(new Runnable() {
            @Override
            public void run() {
                output = text + output;
                context.runOnUiThread(new Runnable() {
                                          @Override
                                          public void run() {
                                              webViewLog.loadUrl("javascript:addLogTextBefore(`" + text + "`)");

                                          }
                                      }
                );
            }
        }, "HtmlThreadBefore");
        htmlThread.start();
    }

    private void addLogTextAfter(final String text) {
        Thread htmlThread = new Thread(new Runnable() {
            @Override
            public void run() {
                final String htmlToAdd = formatHtml(text);
                output = output + htmlToAdd;
                context.runOnUiThread(new Runnable() {
                                          @Override
                                          public void run() {
                                              webViewLog.loadUrl("javascript:addLogTextAfter(`" + htmlToAdd + "`)");
                                          }
                                      }
                );
            }
        }, "HtmlThreadAfter");
        htmlThread.start();
    }

    private String formatHtml(String content) {
        content = content.trim();
        content += "\n";
        String html = content.replaceAll("(.*)left this ARK!", "<span class=\"joinleft\">$0</span>");
        html = html.replaceAll("(.*)joined this ARK!", "<span class=\"joinleft\">$0</span>");
        html = html.replaceAll("(.*)was killed by(.*)", "<span class=\"kill\">$0</span>");
        html = html.replaceAll("(.*)was killed!", "<span class=\"kill\">$0</span>");
        html = html.replaceAll("(.*)Tamed a ([A-z ]*) \\- (.*)", "<span class=\"tame\">$0</span>");
        html = html.replaceAll("(.*)SERVER:(.*)", "<span class=\"server\">$0</span>");
        html = html.replaceAll("\\n", "<br>\n");
        html = html.replaceAll("<br>\\n <br>\\n ", "<br>\n");

        return html;
    }

    public void scrollDown() {
        if(webViewLog != null){
            webViewLog.loadUrl("javascript:scrollDown()");
        }
    }


    private void writeLog(String log) {
        if (preferences.getBoolean("save_log", false)) {
            if (!logService.write(getActivity(), (Server) getActivity().getIntent().getParcelableExtra("server"), log)) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.error_log_write, Snackbar.LENGTH_SHORT).show();
            }
        }
    }
}
