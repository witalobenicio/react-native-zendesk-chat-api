package br.com.meutudo.rnzendeskchat.services;

import android.content.ComponentName;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.facebook.react.HeadlessJsTaskService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import br.com.meutudo.rnzendeskchat.Utils;
import br.com.meutudo.rnzendeskchat.ZendeskChatModule;

public class ZendeskChatApiMessageService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Intent messagingEvent = new Intent(ZendeskChatModule.MESSAGE_EVENT);
        messagingEvent.putExtra("message", remoteMessage);
        // Broadcast it so it is only available to the RN Application
        LocalBroadcastManager
                .getInstance(this.getApplicationContext())
                .sendBroadcast(messagingEvent);
        if (!Utils.isAppInForeground(this.getApplicationContext())) {
            try {
                Intent headlessIntent = new Intent(
                        this.getApplicationContext(),
                        ZendeskChatMessageBackgroundService.class
                );
                headlessIntent.putExtra("message", remoteMessage);
                ComponentName name = this.getApplicationContext().startService(headlessIntent);
                if (name != null) {
                    HeadlessJsTaskService.acquireWakeLockNow(this.getApplicationContext());
                }
            } catch (IllegalStateException ex) {
                Log.e(
                        "ERROR FCM",
                        "Background messages will only work if the message priority is set to 'high'",
                        ex
                );
            }
        }
    }
}
