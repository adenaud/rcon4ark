package com.anthonydenaud.rconark.fargment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.inject.Inject;

import com.anthonydenaud.rconark.R;
import com.anthonydenaud.rconark.service.ArkService;

import roboguice.inject.InjectView;

/**
 * Created by Anthony on 22/10/2015.
 */
public class ChatFragment extends RconFragment implements View.OnClickListener, TextView.OnEditorActionListener {

    @Inject
    private ArkService arkService;


    @InjectView(R.id.textview_chat)
    private TextView textViewChat;

    @InjectView(R.id.editext_chat_send)
    private EditText editTextChatSend;

    @InjectView(R.id.btn_chat)
    private Button buttonChat;

    @InjectView(R.id.btn_broadcast)
    private Button buttonBroadcast;


    private Activity context;
    private String chat;
    private String message;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        arkService.addServerResponseDispatcher(this);

        textViewChat.setMovementMethod(new ScrollingMovementMethod());

        buttonChat.setOnClickListener(this);
        buttonBroadcast.setOnClickListener(this);

        textViewChat.setText(chat);
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
    public void onGetChat(final String chatBuffer) {

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewChat.append(chatBuffer);
                final int scrollAmount = textViewChat.getLayout().getLineTop(textViewChat.getLineCount()) - textViewChat.getHeight();
                if (scrollAmount > 0) {
                    textViewChat.scrollTo(0, scrollAmount);
                } else {
                    textViewChat.scrollTo(0, 0);
                }

            }
        });

    }

    @Override
    public void onDestroyView() {
        chat = textViewChat.getText().toString();
        message = editTextChatSend.getText().toString();
        super.onDestroyView();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        boolean handled = false;
        if(v.equals(editTextChatSend) && actionId == EditorInfo.IME_ACTION_SEND){
            arkService.serverChat(editTextChatSend.getText().toString());
            editTextChatSend.setText("");
            handled = true;
        }
        return handled;
    }
}
