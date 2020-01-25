package br.com.meutudo.rnzendeskchat.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import br.com.meutudo.rnzendeskchat.ZendeskChatModule;

public class NotificationOpenedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ZendeskChatModule.startOrResumeApp(context);
        broadcastMessageOpen(context, intent);
    }

    private void broadcastMessageOpen(Context context, Intent intent) {
        Intent messagingEvent = new Intent(ZendeskChatModule.MESSAGE_OPEN);
        if (intent != null && intent.getExtras() != null) {
            messagingEvent.putExtras(intent.getExtras());
        }
        LocalBroadcastManager
                .getInstance(context)
                .sendBroadcast(messagingEvent);
    }
}
