package com.anthonydenaud.rconark.fargment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anthonydenaud.rconark.R;
import com.anthonydenaud.rconark.service.ArkService;
import com.google.inject.Inject;

import roboguice.inject.InjectView;

public class GameLogFragment extends RconFragment {

    @Inject
    private ArkService arkService;

    @InjectView(R.id.textview_log)
    private TextView textViewLog;
    private Activity context;
    private String log;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gamelog, container, false);
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
}
