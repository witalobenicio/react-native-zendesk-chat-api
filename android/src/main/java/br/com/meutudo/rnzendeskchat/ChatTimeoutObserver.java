package br.com.meutudo.rnzendeskchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.zopim.android.sdk.api.ChatSession;

public class ChatTimeoutObserver extends BroadcastReceiver {

    private ReactContext context;

    public ChatTimeoutObserver(ReactContext context) {
        this.context = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && ChatSession.ACTION_CHAT_SESSION_TIMEOUT.equals(intent.getAction())) {
            WritableMap timeout = Arguments.createMap();
            timeout.putBoolean("timeout", true);
            this.context
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(ZendeskChatModule.onChatLogUpdateEmitter, timeout);
        }
    }
}
