package com.anthonydenaud.arkrcon.fargment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import roboguice.inject.InjectView;

public class ChatLogFragment extends RconFragment implements View.OnClickListener, TextView.OnEditorActionListener {

    @Inject
    private ArkService arkService;

    @Inject
    private LogService logService;

    @InjectView(R.id.textview_chat)
    private TextView textViewOuput;

    @InjectView(R.id.editext_chat_send)
    private EditText editTextChatSend;

    @InjectView(R.id.btn_chat)
    private Button buttonChat;

    @InjectView(R.id.btn_broadcast)
    private Button buttonBroadcast;


    private Activity context;
    private String output;
    private String message;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        arkService.addServerResponseDispatcher(this);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
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

        textViewOuput.setMovementMethod(new ScrollingMovementMethod());

        buttonChat.setOnClickListener(this);
        buttonBroadcast.setOnClickListener(this);

        textViewOuput.setText(output);
        editTextChatSend.setText(message);
        editTextChatSend.setOnEditorActionListener(this);
    }

    @Override
    public void onAttach(Context context) {
        this.context = (Activity) context;
        super.onAttach(context);
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
        output = output + logBuffer;
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewOuput.setText(output);
                final int scrollAmount = textViewOuput.getLayout().getLineTop(textViewOuput.getLineCount()) - textViewOuput.getHeight();
                if (scrollAmount > 0) {
                    textViewOuput.scrollTo(0, scrollAmount);
                } else {
                    textViewOuput.scrollTo(0, 0);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        message = editTextChatSend.getText().toString();
        super.onDestroyView();
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

    @Override
    public void onDestroy() {
        arkService.removeServerResponseDispatcher(this);
        super.onDestroy();
    }

    public String getLog() {
        return output;
    }
}
