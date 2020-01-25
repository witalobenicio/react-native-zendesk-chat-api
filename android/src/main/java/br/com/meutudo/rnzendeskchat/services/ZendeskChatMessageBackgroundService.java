package br.com.meutudo.rnzendeskchat.services;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import com.google.firebase.messaging.RemoteMessage;

import br.com.meutudo.rnzendeskchat.MessagingSerializer;

public class ZendeskChatMessageBackgroundService extends HeadlessJsTaskService {
    @Override
    protected @Nullable
    HeadlessJsTaskConfig getTaskConfig(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            RemoteMessage message = intent.getParcelableExtra("message");
            WritableMap messageMap = MessagingSerializer.parseRemoteMessage(message);
            return new HeadlessJsTaskConfig(
                    "ZendeskChatBackgroundMessage",
                    messageMap,
                    60000,
                    false
            );
        }
        return null;
    }
}
