package br.com.meutudo.rnzendeskchat;

import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.zopim.android.sdk.api.ChatApi;
import com.zopim.android.sdk.api.ChatSession;
import com.zopim.android.sdk.api.ZopimChatApi;
import com.zopim.android.sdk.model.ChatLog;
import com.zopim.android.sdk.model.VisitorInfo;

import java.io.File;
import java.util.LinkedHashMap;

public class ZendeskChatModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private ChatObserver chatObserver;
    private ChatTimeoutObserver chatTimeoutObserver;
    private ChatConnectionObserver chatConnectionObserver;
    private ChatApi chatApi = null;
    public final static String onConnectionUpdateEmitter = "onConnectionUpdate";
    public final static String onChatLogUpdateEmitter = "onChatLogUpdate";
    public final static String onTimeoutReceivedEmitter = "onTimeoutReceived";

    public ZendeskChatModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "ZendeskChat";
    }


    @ReactMethod
    public void startChat(String accountKey, ReadableMap userInfo, ReadableMap userConfig, Promise promise) {
        ZopimChatApi.init(accountKey);
        AppCompatActivity currentActivity = (AppCompatActivity) getCurrentActivity();
        this.setUserInfo(userInfo);
        ZopimChatApi.SessionConfig config = new ZopimChatApi.SessionConfig();
        if (userConfig != null) {
            if (userConfig.hasKey("department")) {
                config.department(userConfig.getString("department"));
            }
            if (userConfig.hasKey("tags")) {
                ReadableArray tags = userConfig.getArray("tags");
                if (tags.toArrayList().size() > 0) {
                    config.tags(tags.toArrayList().toArray(new String[tags.toArrayList().size()]));
                }
            }
        }
        this.chatApi = config.build(currentActivity);
        promise.resolve(true);
    }

    @ReactMethod
    public void endChat() {
        if (this.chatApi != null) {
            this.chatApi.endChat();
        }
    }

    @ReactMethod
    public void getChatLog(Promise promise) {
        LinkedHashMap<String, ChatLog> entries = ZopimChatApi.getDataSource().getChatLog();
        promise.resolve(ItemFactory.getArrayFromEntries(entries));
    }

    @ReactMethod
    public void addChatLogObserver() {
        chatObserver = new ChatObserver(this.reactContext);
        ZopimChatApi.getDataSource().addChatLogObserver(chatObserver);
    }

    @ReactMethod
    public void deleteChatLogObserver() {
        if (chatObserver != null) {
            ZopimChatApi.getDataSource().deleteChatLogObserver(chatObserver);
        }
    }

    @ReactMethod
    public void addChatConnectionObserver() {
        chatConnectionObserver = new ChatConnectionObserver(reactContext);
        ZopimChatApi.getDataSource().addConnectionObserver(chatConnectionObserver);
    }

    @ReactMethod
    public void deleteChatConnectionObserver() {
        if (chatConnectionObserver != null) {
            ZopimChatApi.getDataSource().deleteConnectionObserver(chatConnectionObserver);
        }
    }

    @ReactMethod
    public void addChatTimeoutObserver() {
        chatTimeoutObserver = new ChatTimeoutObserver(reactContext);
        LocalBroadcastManager.getInstance(getReactApplicationContext())
                .registerReceiver(chatTimeoutObserver, new IntentFilter(ChatSession.ACTION_CHAT_SESSION_TIMEOUT));
    }

    @ReactMethod
    public void deleteChatTimeoutObserver() {
        if (chatTimeoutObserver != null) {
            LocalBroadcastManager
                    .getInstance(getReactApplicationContext())
                    .unregisterReceiver(chatTimeoutObserver);
        }
    }

    @ReactMethod
    public void sendMessage(String message) {
        if (this.chatApi != null) {
            chatApi.send(message);
        }
    }

    @ReactMethod
    public void sendFile(String path) {
        if (this.chatApi != null && !TextUtils.isEmpty(path)) {
            File file = new File(path);
            if (file.exists()) {
                chatApi.send(new File(path));
            }
        }
    }

    private void setUserInfo(ReadableMap userInfo) {
        VisitorInfo.Builder visitorInfoBuilder = new VisitorInfo.Builder();
        VisitorInfo visitorInfo = visitorInfoBuilder.build();
        if (userInfo.hasKey("name")) {
            String userName = userInfo.getString("name");
            visitorInfo.setName(userName);
        }
        if (userInfo.hasKey("email")) {
            String userEmail = userInfo.getString("email");
            visitorInfo.setEmail(userEmail);
        }
        if (userInfo.hasKey("phone")) {
            String userPhone = userInfo.getString("phone");
            visitorInfo.setPhoneNumber(userPhone);
        }
        if (userInfo.hasKey("note")) {
            String userNote = userInfo.getString("note");
            visitorInfo.setNote(userNote);
        }
        ZopimChatApi.setVisitorInfo(visitorInfo);
    }
}
