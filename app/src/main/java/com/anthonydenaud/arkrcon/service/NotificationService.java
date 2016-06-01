package com.anthonydenaud.arkrcon.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.anthonydenaud.arkrcon.model.Server;
import com.anthonydenaud.arkrcon.view.RconActivity;
import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import roboguice.util.Ln;

public class NotificationService {

    private final Context context;
    private final SharedPreferences preferences;

    private int notificationId = 0;

    private Vibrator vibrator;

    @Inject
    public NotificationService(Context context) {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }


    /*
    *  http://stackoverflow.com/questions/16885706/click-on-notification-to-go-current-activity
     */

    public void handleChatKeyword(Context activity, String chatbuffer) {
        if (preferences.contains("chat_notification_keyword")) {
            String keyword = preferences.getString("chat_notification_keyword", null);
            if (StringUtils.isNotEmpty(keyword) && chatbuffer.contains(keyword)) {

                long[] tmp = {0, 100, 200, 100, 200};

                vibrator.vibrate(tmp, -1);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(activity);
                builder.setContentTitle("Chat alert");
                builder.setContentText(chatbuffer);
                builder.setSmallIcon(android.R.drawable.stat_notify_chat);

                Intent resultIntent = new Intent(context, RconActivity.class);
                resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);


                PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0,resultIntent,0);

                builder.setContentIntent(pendingIntent);

                NotificationManager manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(getNotificationId(), builder.build());

            }
        }
    }

    public synchronized int getNotificationId() {
        return ++notificationId;
    }
}
