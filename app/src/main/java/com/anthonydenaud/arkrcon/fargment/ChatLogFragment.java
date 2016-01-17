package com.anthonydenaud.arkrcon.fargment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.anthonydenaud.arkrcon.model.Server;
import com.anthonydenaud.arkrcon.service.LogService;
import com.google.inject.Inject;

import com.anthonydenaud.arkrcon.R;
import com.anthonydenaud.arkrcon.service.ArkService;

import org.apache.commons.lang3.StringUtils;

import roboguice.inject.InjectView;

public class ChatLogFragment extends RconFragment implements View.OnClickListener, TextView.OnEditorActionListener {

    @Inject
    private ArkService arkService;

    @Inject
    private LogService logService;

    @InjectView(R.id.textview_chat)
    private TextView textViewOutput;

    @InjectView(R.id.editext_chat_send)
    private EditText editTextChatSend;

    @InjectView(R.id.btn_chat)
    private Button buttonChat;

    @InjectView(R.id.btn_broadcast)
    private Button buttonBroadcast;


    private Activity context;
    private String output;
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
        output = "";
        if (preferences.getBoolean("save_log", false)) {
            Server server = getActivity().getIntent().getParcelableExtra("server");
            output = logService.read(getActivity(), server);
        }
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textViewOutput.setMovementMethod(new ScrollingMovementMethod());

        buttonChat.setOnClickListener(this);
        buttonBroadcast.setOnClickListener(this);

        addLogText(output);
        editTextChatSend.setText(message);
        editTextChatSend.setOnEditorActionListener(this);
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
            output = output + logBuffer;
            writeLog(logBuffer);
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addLogText(logBuffer);
                    scrollDown();
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

    private void addLogText(final String text) {
        Thread htmlThread = new Thread(new Runnable() {
            @Override
            public void run() {
                final String html = formatHtml(text);
                context.runOnUiThread(new Runnable() {
                                          @Override
                                          public void run() {
                                              textViewOutput.append(Html.fromHtml(html));
                                          }
                                      }
                );
            }
        }, "HtmlThread");
        htmlThread.start();
    }

    private String formatHtml(String content) {
        String html = content.replaceAll("(.*)left this ARK!", "<font color=\"#0000CD\">$0!</font>");
        html = html.replaceAll("(.*)joined this ARK!", "<font color=\"#0000CD\">$0</font>");
        html = html.replaceAll("(.*)was killed by(.*)", "<font color=\"#DC143C\">$0</font>");
        html = html.replaceAll("(.*)was killed!", "<font color=\"#DC143C\">$0</font>");
        html = html.replaceAll("(.*)Tamed a ([A-z]+) \\- (.*)", "<font color=\"#008000\">$0</font>");
        html = html.replaceAll("\\n", "<br>");
        return html;
    }

    public void scrollDown() {
        final int scrollAmount = textViewOutput.getLayout().getLineTop(textViewOutput.getLineCount()) - textViewOutput.getHeight();
        if (scrollAmount > 0) {
            textViewOutput.scrollTo(0, scrollAmount);
        } else {
            textViewOutput.scrollTo(0, 0);
        }
    }

    private void writeLog(String log) {
        if(preferences.getBoolean("save_log",false)){
            if(!logService.write(getActivity(), (Server) getActivity().getIntent().getParcelableExtra("server"), log)){
                Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.error_log_write, Snackbar.LENGTH_SHORT).show();
            }
        }
    }
}
