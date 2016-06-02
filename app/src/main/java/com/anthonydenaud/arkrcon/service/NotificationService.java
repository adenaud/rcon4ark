package com.anthonydenaud.arkrcon.service;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.anthonydenaud.arkrcon.Codes;
import com.anthonydenaud.arkrcon.R;
import com.anthonydenaud.arkrcon.view.RconActivity;
import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;


public class NotificationService {

    private final Context context;
    private final SharedPreferences preferences;

    @Inject
    public NotificationService(Context context) {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }


    public void handleChatKeyword(Activity activity, String chatbuffer) {
        if (preferences.contains("chat_notification_keyword")) {
            String keyword = preferences.getString("chat_notification_keyword", null);
            if (StringUtils.isNotEmpty(keyword) && chatbuffer.contains(keyword)) {

                long[] pattern = {100, 200, 100, 200};

                NotificationCompat.Builder builder = new NotificationCompat.Builder(activity);
                builder.setContentTitle(activity.getString(R.string.notification_title));
                builder.setContentText(chatbuffer);
                builder.setSmallIcon(android.R.drawable.stat_notify_chat);
                builder.setAutoCancel(true);
                if (preferences.getBoolean("vibrate", false)) {
                    builder.setVibrate(pattern);
                }

                Intent resultIntent = new Intent(context, RconActivity.class);
                resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                resultIntent.putExtras(activity.getIntent().getExtras());
                PendingIntent pendingIntent = PendingIntent.getActivity(activity, Codes.REQUEST_RCON_CLOSE, resultIntent, 0);
                builder.setContentIntent(pendingIntent);


                NotificationManager manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(0, builder.build());

            }
        }
    }

}
