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

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class NotificationService {

    private final Context context;
    private final SharedPreferences preferences;
    private String[] keywords;

    @Inject
    public NotificationService(Context context) {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        if (preferences.contains("chat_notification_keyword")) {
            String keyword = preferences.getString("chat_notification_keyword", "");
            keyword = keyword.replaceAll(", ",",");
            keywords = keyword.split(",");
        }
    }


    public void handleChatKeyword(Activity activity, String chatbuffer) {

        chatbuffer = pareLogContent(chatbuffer);

        if (StringUtils.indexOfAny(chatbuffer,keywords)>-1){

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
            activity.getIntent().putExtra("chat_notification", true);
            PendingIntent pendingIntent = PendingIntent.getActivity(activity, Codes.REQUEST_RCON_CLOSE, resultIntent, 0);
            builder.setContentIntent(pendingIntent);


            NotificationManager manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(0, builder.build());

        }
    }

    private static String pareLogContent(String logLine) {
        String result = "";
        Pattern pattern = Pattern.compile("([0-9]{4})\\.([0-9]{2})\\.([0-9]{2})_([0-9]{2}).([0-9]{2}).([0-9]{2}): (.*)");
        Matcher matcher = pattern.matcher(logLine);
        if (matcher.find()) {
            result = matcher.group(7);
        }else{
            result = logLine;
        }
        return result;
    }

}
