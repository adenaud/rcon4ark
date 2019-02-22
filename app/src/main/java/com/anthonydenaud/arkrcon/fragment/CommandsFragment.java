package com.anthonydenaud.arkrcon.fragment;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.anthonydenaud.arkrcon.R;
import com.anthonydenaud.arkrcon.service.ArkService;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CommandsFragment extends RconFragment implements View.OnClickListener, TimePickerDialog.OnTimeSetListener, TextView.OnEditorActionListener {

    @BindView(R.id.button_changeTime)
    Button buttonChangeTime;

    @BindView(R.id.button_saveWorld)
    Button buttonSaveWorld;

    @BindView(R.id.button_killDinos)
    Button buttonKillDinos;


    @BindView(R.id.button_unban_player)
    Button buttonUnBan;

    @BindView(R.id.button_whitelist_remove)
    Button buttonWhiteListRemove;

    @BindView(R.id.textview_output)
    TextView textViewOutput;

    @BindView(R.id.editext_command)
    AutoCompleteTextView editTextCommand;

    @BindView(R.id.btn_exec)
    Button buttonExec;


    /* View expanding */
    @BindView(R.id.layout_quick_commands)
    LinearLayout layoutQuickCmds;
    @BindView(R.id.layout_custom_commands)
    LinearLayout layoutCustomCmds;
    @BindView(R.id.btn_expand_quick_cmd)
    ImageButton btnExpandQuickCmds;
    @BindView(R.id.btn_expend_custom_cmd)
    ImageButton btnExpandCustomCmds;

    private ArkService arkService;
    private Activity context;

    private String output;
    private String command;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            output = savedInstanceState.getString("output");
            command = savedInstanceState.getString("command");
        }
        this.context = getActivity();
        this.arkService = new ArkService(this.context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rcon_commands, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        arkService.addServerResponseDispatcher(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_dropdown_item_1line,
                context.getResources().getStringArray(R.array.commands));

        textViewOutput.setMovementMethod(new ScrollingMovementMethod());
        textViewOutput.setText(output);
        editTextCommand.setText(command);
        editTextCommand.setAdapter(adapter);
        editTextCommand.setOnEditorActionListener(this);

        buttonChangeTime.setOnClickListener(this);
        buttonSaveWorld.setOnClickListener(this);
        buttonKillDinos.setOnClickListener(this);
        buttonUnBan.setOnClickListener(this);
        buttonWhiteListRemove.setOnClickListener(this);

        btnExpandQuickCmds.setOnClickListener(this);
        btnExpandCustomCmds.setOnClickListener(this);
        buttonExec.setOnClickListener(this);
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
    public void onDestroy() {
        arkService.removeServerResponseDispatcher(this);
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        output = textViewOutput.getText().toString();
        command = editTextCommand.getText().toString();
        outState.putString("output",output);
        outState.putString("command",command);
    }

    @Override
    public void onClick(View v) {

        /* View expanding */

        if(v.equals(btnExpandQuickCmds)){
            if(layoutQuickCmds.getVisibility() == View.VISIBLE){
                layoutQuickCmds.setVisibility(View.GONE);
                btnExpandQuickCmds.setBackgroundResource(R.drawable.ic_expand_more);
            }
            else{
                layoutQuickCmds.setVisibility(View.VISIBLE);
                btnExpandQuickCmds.setBackgroundResource(R.drawable.ic_expand_less);
            }
        }
        if(v.equals(btnExpandCustomCmds)){
            if(layoutCustomCmds.getVisibility() == View.VISIBLE){
                layoutCustomCmds.setVisibility(View.GONE);
                btnExpandCustomCmds.setBackgroundResource(R.drawable.ic_expand_more);
            }
            else{
                layoutCustomCmds.setVisibility(View.VISIBLE);
                btnExpandCustomCmds.setBackgroundResource(R.drawable.ic_expand_less);
            }
        }

        /* RCON */
        if (v.equals(buttonChangeTime)) {
            TimePickerDialog timePicker = new TimePickerDialog(context, this, 7, 30, true);
            timePicker.show();
        }
        if (v.equals(buttonSaveWorld)) {
            arkService.saveWorld();
        }
        if(v.equals(buttonUnBan)){
            final EditText editText = new EditText(context);
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
            builder.setTitle(R.string.unban);
            builder.setMessage(R.string.player_name);
            builder.setView(editText);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    arkService.unBan(editText.getText().toString());
                }
            });
            builder.setNegativeButton(R.string.cancel,null);
            builder.show();
        }
        if(v.equals(buttonWhiteListRemove)){
            final EditText editText = new EditText(context);
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
            builder.setTitle(R.string.whitelist_remove);
            builder.setMessage(R.string.player_steamid);
            builder.setView(editText);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    arkService.disallowPlayerToJoinNoCheck(editText.getText().toString());
                }
            });
            builder.setNegativeButton(R.string.cancel,null);
            builder.show();
        }
        if (v.equals(buttonKillDinos)) {
            new AlertDialog.Builder(context)
                    .setMessage(R.string.killdinos_confirm)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            arkService.destroyWildDinos();
                        }
                    })
                    .setNegativeButton(R.string.cancel,null).show();
        }
        if (v.equals(buttonExec)) {
            sendCommand();
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        arkService.setTimeofDay(hourOfDay, minute);
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
