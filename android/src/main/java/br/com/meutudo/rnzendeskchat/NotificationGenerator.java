package br.com.meutudo.rnzendeskchat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import java.util.Map;
import java.util.Random;

import br.com.meutudo.rnzendeskchat.receivers.NotificationOpenedReceiver;

import static br.com.meutudo.rnzendeskchat.ZendeskChatModule.CHANNEL_ID;
import static br.com.meutudo.rnzendeskchat.ZendeskChatModule.GROUP_ID;

public class NotificationGenerator {
    private Context context;

    public NotificationGenerator(Context context) {
        this.context = context;
    }

    public void sendNotification(String messageBody, String title) {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        int smallIcon = 0;
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            smallIcon = bundle.getInt("default_notification_icon", 0);
        } catch (ExceptionInInitializerError error) {
            smallIcon = R.drawable.ic_foreground_notification;
        } catch (PackageManager.NameNotFoundException e) {
            smallIcon = R.drawable.ic_foreground_notification;
        }
        if (smallIcon == 0) {
            smallIcon = R.drawable.ic_foreground_notification;
        }

        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(context);

        PendingIntent intent = getIntent(messageBody, title);
        notificationBuilder.setContentIntent(intent);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(title);
        inboxStyle.addLine(messageBody);

        notificationBuilder.setSmallIcon(smallIcon)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setGroup(GROUP_ID)
                .setStyle(inboxStyle)
                .setSound(defaultSoundUri);

        Notification summaryNotification =
                new NotificationCompat.Builder(context)
                        .setContentTitle(title)
                        .setContentText("Chat do Suporte")
                        .setSmallIcon(smallIcon)
                        .setStyle(new NotificationCompat.InboxStyle()
                                .setSummaryText("Chat do suporte"))
                        .setGroup(GROUP_ID)
                        .setGroupSummary(true)
                        .setAutoCancel(true)
                        .setContentIntent(intent)
                        .build();

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Random notification_id = new Random();
        notificationManager.notify(1001, summaryNotification);
        notificationManager.notify(notification_id.nextInt(100), notificationBuilder.build());
    }

    private PendingIntent getIntent(String message, String title) {
        Class activityClass = NotificationOpenedReceiver.class;
        Intent notificationIntent = new Intent(context, activityClass);
        notificationIntent.putExtra("message", message);
        notificationIntent.putExtra("title", title);

        return PendingIntent.getBroadcast(context, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private NotificationChannel createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "chat", importance);
            channel.setDescription("support chat");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            return channel;
        }
        return null;
    }
}
