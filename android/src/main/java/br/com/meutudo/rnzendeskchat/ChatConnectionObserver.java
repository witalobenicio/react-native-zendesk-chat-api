package br.com.meutudo.rnzendeskchat;

import android.content.Context;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.zopim.android.sdk.data.observers.ConnectionObserver;
import com.zopim.android.sdk.model.Connection;
import com.zopim.android.sdk.model.items.RowItem;

import java.util.TreeMap;

public class ChatConnectionObserver extends ConnectionObserver {

    private Callback callback;
    private ReactContext context;

    public ChatConnectionObserver(ReactContext context) {
        this.callback = callback;
        this.context = context;
    }

    @Override
    protected void update(Connection connection) {
        WritableMap status = Arguments.createMap();
        status.putString("status", connection.toString());
        this.context
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(ZendeskChatModule.onConnectionUpdateEmitter, status);
    }
}
