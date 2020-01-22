package br.com.meutudo.rnzendeskchat;

import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.react.bridge.Callback;
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
import com.zopim.android.sdk.model.Department;
import com.zopim.android.sdk.model.VisitorInfo;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.zopim.android.sdk.model.Department.Status.ONLINE;

public class ZendeskChatModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private ChatObserver chatObserver;
    private ChatDepartmentsObserver departmentsObserver;
    private ChatTimeoutObserver chatTimeoutObserver;
    private ChatConnectionObserver chatConnectionObserver;
    private ChatApi chatApi = null;
    public final static String onConnectionUpdateEmitter = "onConnectionUpdate";
    public final static String onDepartmentsUpdateEmitter = "onDepartmentsUpdate";
    public final static String onChatLogUpdateEmitter = "onChatLogUpdate";
    public final static String onTimeoutReceivedEmitter = "onTimeoutReceived";
    public final static String onAgentLeaveEmitter = "onAgentLeave";

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
    public void isChatAvailable(Callback callback) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            callback.invoke(true);
            return;
        }
        callback.invoke(false);
    }

    @ReactMethod
    public void isOnline(Callback callback) {
        Map<String, Department> departments = ZopimChatApi.getDataSource().getDepartments();
        for (Map.Entry<String, Department> department : departments.entrySet()) {
            Department departmentValue = department.getValue();
            if (departmentValue.getStatus() == ONLINE) {
                callback.invoke("ONLINE");
                return;
            }
        }
        callback.invoke("OFFLINE");
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
    public void addDepartmentsObserver() {
        departmentsObserver = new ChatDepartmentsObserver(this.reactContext);
        ZopimChatApi.getDataSource().addDepartmentsObserver(departmentsObserver);
    }

    @ReactMethod
    public void deleteDepartmentsObserver() {
        if (departmentsObserver != null) {
            ZopimChatApi.getDataSource().deleteDepartmentsObserver(departmentsObserver);
        }
    }

    @ReactMethod
    public void addChatConnectionObserver() {
        chatConnectionObserver = new ChatConnectionObserver(reactContext);
        ZopimChatApi.getDataSource().addConnectionObserver(chatConnectionObserver);
        chatConnectionObserver.update(ZopimChatApi.getDataSource().getConnection());
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
            Uri uri = null;
            if (path.contains("content")) {
                uri = Uri.parse(path);
            }
            File file = new File(path);
            if (uri != null) {
                file = new File(uri.getPath());
            }
            if (file.exists()) {
                chatApi.send(new File(path));
            } else {
                Log.d("FILE", "FILE DOESNT EXISTS");
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
