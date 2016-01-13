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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.anthonydenaud.rconark.R;
import com.anthonydenaud.rconark.service.ArkService;
import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import roboguice.inject.InjectView;

public class CustomCommandsFragment extends RconFragment implements View.OnClickListener, TextView.OnEditorActionListener {

    @Inject
    private ArkService arkService;

    @InjectView(R.id.textview_output)
    private TextView textViewOutput;

    @InjectView(R.id.editext_command)
    private AutoCompleteTextView editTextCommand;

    @InjectView(R.id.btn_exec)
    private Button buttonExec;

    private String output;
    private String command;
    private Activity context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_custom_commands, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_dropdown_item_1line,
                context.getResources().getStringArray(R.array.commands));

        arkService.addServerResponseDispatcher(this);
        textViewOutput.setMovementMethod(new ScrollingMovementMethod());
        buttonExec.setOnClickListener(this);
        textViewOutput.setText(output);
        editTextCommand.setText(command);
        editTextCommand.setAdapter(adapter);
        editTextCommand.setOnEditorActionListener(this);
    }

    @Override
    public void onAttach(Context context) {
        this.context = (Activity) context;
        super.onAttach(context);
    }

    @Override
    public void onDestroyView() {
        output = textViewOutput.getText().toString();
        command = editTextCommand.getText().toString();
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        if (v.equals(buttonExec)) {
            sendCommand();
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        boolean handled = false;
        if (v.equals(editTextCommand) && actionId == EditorInfo.IME_ACTION_SEND) {
            sendCommand();
            handled = true;
        }
        return handled;
    }

    private void sendCommand() {
        String cmd = editTextCommand.getText().toString();
        if (StringUtils.isNotEmpty(cmd)) {
            arkService.sendRawCommand(cmd);
            editTextCommand.setText("");
        }
    }

    @Override
    public void onCustomCommandResult(final String result) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewOutput.append(result);
                final int scrollAmount = textViewOutput.getLayout().getLineTop(textViewOutput.getLineCount()) - textViewOutput.getHeight();
                if (scrollAmount > 0) {
                    textViewOutput.scrollTo(0, scrollAmount);
                } else {
                    textViewOutput.scrollTo(0, 0);
                }

            }
        });
    }
}
