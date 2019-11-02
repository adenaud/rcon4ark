package com.anthonydenaud.arkrcon.service;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.anthonydenaud.arkrcon.Codes;
import com.anthonydenaud.arkrcon.R;
import com.anthonydenaud.arkrcon.view.RconActivity;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton
public class NotificationService {

    private static final String NOTIFICATION_DELETED = "NOTIFICATION_DELETED";

    private static final String CHANNEL_ID = "rcon4ark_chat_channel";
    private static final String CHANNEL_NAME = "Chat alerts";

    private SharedPreferences preferences;
    private NotificationManager notificationManager;

    private String[] keywords;
    private boolean active;
    private int nbNotifications = 1;
    private String currentText;

    @Inject
    NotificationService(Application application, NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(application);
        createNotificationChannel();
        reloadKeywords();
    }

    private void createNotificationChannel(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);

            if (preferences.getBoolean("vibrate", false)) {
                long[] vibratePattern = {100, 200, 100, 200};
                notificationChannel.setVibrationPattern(vibratePattern);
            }
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public void reloadKeywords(){
        if (preferences.contains("chat_notification_keyword")) {
            String keyword = preferences.getString("chat_notification_keyword", "");
            keyword = keyword.replaceAll(", ", ",");
            keywords = keyword.split(",");
        }
    }

    public void handleChatKeyword(Activity activity, String chatbuffer) {

        chatbuffer = parseLogContent(chatbuffer);

        if (StringUtils.indexOfAny(chatbuffer, keywords) > -1 && preferences.getBoolean("notifications_enabled", false)) {
            showNotification(activity, chatbuffer);
        }
    }

    private void showNotification(final Activity activity, String contentText) {

       Application application = activity.getApplication();

        BroadcastReceiver deleteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                notificationManager.cancel(0);
                activity.unregisterReceiver(this);
                setActive(false);
            }
        };

        Intent resultIntent = new Intent(application, RconActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        resultIntent.putExtras(activity.getIntent().getExtras());
        activity.getIntent().putExtra("chat_notification", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, Codes.REQUEST_RCON_CLOSE, resultIntent, 0);

        Intent deleteIntent = new Intent(NOTIFICATION_DELETED);
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(activity, 0, deleteIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(application, CHANNEL_ID);

        // Notification is not present, we create it.
        if (!active) {
            builder.setContentTitle(activity.getString(R.string.notification_title));
            builder.setContentText(contentText);
            activity.registerReceiver(deleteReceiver, new IntentFilter(NOTIFICATION_DELETED));
            currentText = contentText;
            active = true;
            nbNotifications = 1;
        }else{
            //Notification is present, we update it.
            currentText = String.format("%s\n%s", currentText, contentText);
            builder.setContentTitle( String.valueOf(++nbNotifications) + " " + activity.getString(R.string.notification_title));
            builder.setContentText(contentText);
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(currentText));
        }
        builder.setSmallIcon(android.R.drawable.stat_notify_chat);
        builder.setContentIntent(pendingIntent);
        builder.setDeleteIntent(deletePendingIntent);

        notificationManager.notify(0, builder.build());
    }

    public void notificationClicked(){
        notificationManager.cancel(0);
        setActive(false);
    }

    private static String parseLogContent(String logLine) {
        String result;
        Pattern pattern = Pattern.compile("([0-9]{4})\\.([0-9]{2})\\.([0-9]{2})_([0-9]{2}).([0-9]{2}).([0-9]{2}): (.*)");
        Matcher matcher = pattern.matcher(logLine);
        if (matcher.find()) {
            result = matcher.group(7);
        } else {
            result = logLine;
        }
        return result;
    }

    private void setActive(boolean active) {
        this.active = active;
    }
}