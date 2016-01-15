package com.anthonydenaud.arkrcon.fargment;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

import com.google.inject.Inject;

import com.anthonydenaud.arkrcon.R;
import com.anthonydenaud.arkrcon.service.ArkService;

import roboguice.inject.InjectView;

public class CommandsFragment extends RconFragment implements View.OnClickListener, TimePickerDialog.OnTimeSetListener {

    @Inject
    private ArkService arkService;

    @InjectView(R.id.button_changeTime)
    private Button buttonChangeTime;

    @InjectView(R.id.button_saveWorld)
    private Button buttonSaveWorld;

    @InjectView(R.id.button_killDinos)
    private Button buttonKillDinos;


    @InjectView(R.id.button_unban_player)
    private Button buttonUnBan;

    @InjectView(R.id.button_whitelist_remove)
    private Button buttonWhiteListRemove;


    private Activity context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rcon_commands, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        arkService.addServerResponseDispatcher(this);

        buttonChangeTime.setOnClickListener(this);
        buttonSaveWorld.setOnClickListener(this);
        buttonKillDinos.setOnClickListener(this);
        buttonUnBan.setOnClickListener(this);
        buttonWhiteListRemove.setOnClickListener(this);
    }

    @Override
    public void onAttach(Context context) {
        this.context = (Activity) context;
        super.onAttach(context);
    }

    @Override
    public void onClick(View v) {
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
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        arkService.setTimeofDay(hourOfDay, minute);
    }
}