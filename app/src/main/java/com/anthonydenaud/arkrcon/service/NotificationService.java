package com.anthonydenaud.arkrcon.service;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;

import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import roboguice.util.Ln;

public class NotificationService {

    private final Context context;
    private final SharedPreferences preferences;

    private int notificationId;

    private Vibrator vibrator;

    @Inject
    public NotificationService(Context context) {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void handleChatKeyword(String chatbuffer) {
        if (preferences.contains("chat_notification_keyword")) {
            String keyword = preferences.getString("chat_notification_keyword", null);
            if (StringUtils.isNotEmpty(keyword) && chatbuffer.contains(keyword)) {

                long[] tmp = {0, 100, 200, 100, 200};

                vibrator.vibrate(tmp, -1);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                builder.setContentTitle("Chat alert");
                builder.setContentText(chatbuffer);

                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(getNotificationId(), builder.build());

            }
        }
    }

    public synchronized int getNotificationId() {
        return ++notificationId;
    }
}
