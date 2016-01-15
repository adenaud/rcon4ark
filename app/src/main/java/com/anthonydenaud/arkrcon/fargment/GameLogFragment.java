package com.anthonydenaud.arkrcon.fargment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anthonydenaud.arkrcon.R;
import com.anthonydenaud.arkrcon.model.Server;
import com.anthonydenaud.arkrcon.service.ArkService;
import com.anthonydenaud.arkrcon.service.LogService;
import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import roboguice.inject.InjectView;

public class GameLogFragment extends RconFragment {

    @Inject
    private ArkService arkService;

    @Inject
    private LogService logService;

    @InjectView(R.id.textview_log)
    private TextView textViewLog;
    private Activity context;
    private String log;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_gamelog, container, false);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if(preferences.getBoolean("save_log",false)){
            Server server = getActivity().getIntent().getParcelableExtra("server");
            log = logService.read(getActivity(),server);
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        arkService.addServerResponseDispatcher(this);
        textViewLog.setMovementMethod(new ScrollingMovementMethod());
        textViewLog.setText(log);
    }

    @Override
    public void onAttach(Context context) {
        this.context = (Activity) context;
        super.onAttach(context);
    }

    @Override
    public void onGetLog(final String logBuffer) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewLog.append(logBuffer);
                final int scrollAmount = textViewLog.getLayout().getLineTop(textViewLog.getLineCount()) - textViewLog.getHeight();
                if (scrollAmount > 0) {
                    textViewLog.scrollTo(0, scrollAmount);
                } else {
                    textViewLog.scrollTo(0, 0);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        log = textViewLog.getText().toString();
        super.onDestroyView();
    }

    public String getLog() {
        if(textViewLog != null){
            return textViewLog.getText().toString();
        }
        return "";
    }
}
