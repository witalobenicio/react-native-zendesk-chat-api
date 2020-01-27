package br.com.meutudo.rnzendeskchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.RemoteMessage;
import com.zopim.android.sdk.api.ChatApi;
import com.zopim.android.sdk.api.ChatSession;
import com.zopim.android.sdk.api.ZopimChatApi;
import com.zopim.android.sdk.model.ChatLog;
import com.zopim.android.sdk.model.Department;
import com.zopim.android.sdk.model.PushData;
import com.zopim.android.sdk.model.VisitorInfo;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import br.com.meutudo.rnzendeskchat.observers.ChatConnectionObserver;
import br.com.meutudo.rnzendeskchat.observers.ChatDepartmentsObserver;
import br.com.meutudo.rnzendeskchat.observers.ChatObserver;
import br.com.meutudo.rnzendeskchat.observers.ChatTimeoutObserver;

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
    public final static String onMessageReceivedEmitter = "onMessageReceived";
    public final static String onMessageOpenedEmitter = "onMessageOpened";

    public final static String MESSAGE_EVENT = "message_event_broadcast";
    public final static String MESSAGE_OPEN = "message_open_event_broadcast";
    public final static String GROUP_ID = "br.com.meutudo.rnzendeskchat.CHAT_GROUP";
    public final static String CHANNEL_ID = "br.com.meutudo.rnzendeskchat.CHANNEL_CHAT";

    private RemoteMessage lastRemoteMessage = null;

    public ZendeskChatModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(reactContext);

        // Subscribe to message events
        localBroadcastManager.registerReceiver(
                new MessageReceiver(),
                new IntentFilter(MESSAGE_EVENT)
        );
        localBroadcastManager.registerReceiver(
                new MessageOpenedReceiver(),
                new IntentFilter(MESSAGE_OPEN)
        );
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
                if (tags != null && tags.toArrayList().size() > 0) {
                    config.tags(tags.toArrayList().toArray(new String[tags.toArrayList().size()]));
                }
            }
        }
        if (currentActivity != null) {
            this.chatApi = config.build(currentActivity);
            promise.resolve(true);
        } else {
            promise.reject(new Throwable("No Activity found"));
        }
    }

    @ReactMethod
    public void getNotificationData(Callback callback) {
        WritableMap writableMap = Arguments.createMap();
        if (lastRemoteMessage != null) {
            writableMap.putMap("message", MessagingSerializer.parseRemoteMessage(lastRemoteMessage));
            callback.invoke(writableMap);
            lastRemoteMessage = null;
            return;
        }
        writableMap.putMap("message", null);
        callback.invoke(writableMap);
    }

    @ReactMethod
    public void registerFCMToken(String accountKey) {
        ZopimChatApi.init(accountKey);
//        try {
//            FirebaseInstanceId.getInstance().getInstanceId()
//                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
//                        @Override
//                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
//                            if (!task.isSuccessful()) {
//                                Log.w("FCM", "getInstanceId failed", task.getException());
//                                return;
//                            }
//
//                            // Get new Instance ID token
//                            final String token = task.getResult().getToken();
//                            // Log and toast
//                            Log.d("FCM", "REGISTERED TOKEN");
//                            ZopimChatApi.setPushToken(token);
//                        }
//                    });
//        } catch (Exception e) {
//
//        }
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
        chatConnectionObserver.updateConnection(ZopimChatApi.getDataSource().getConnection());
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
                Cursor cursor = this.reactContext.getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    String pathCursor = cursor.getString(8);
                    file = new File(pathCursor);
                }
            }
            if (path.contains("jpg") || path.contains("png")) {

            }
            if (file.exists()) {
                chatApi.send(file);
            } else {
                Log.d("FILE", "FILE DOESNT EXISTS");
            }
        }
    }

    @ReactMethod
    public void showChatNotification(String message, String title) {
        NotificationGenerator generator = new NotificationGenerator(getReactApplicationContext());
        generator.sendNotification(message, title);
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

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getReactApplicationContext().hasActiveCatalystInstance()) {
                RemoteMessage message = intent.getParcelableExtra("message");
                if (message != null) {
                    PushData pushData = PushData.getChatNotification(message.getData());
                    if (!Utils.isAppInForeground(reactContext)) {
                        lastRemoteMessage = message;
                        if (pushData.getType() == PushData.Type.MESSAGE) {
                            NotificationGenerator generator = new NotificationGenerator(getReactApplicationContext());
                            generator.sendNotification(pushData.getMessage(), pushData.getAuthor());
                        }
                    } else {
                        emitOnNotificationReceived(pushData);
                    }
                }
            }
        }
    }

    private class MessageOpenedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getReactApplicationContext().hasActiveCatalystInstance()) {
                if (intent != null && intent.hasExtra("message")) {
                    String text = intent.getStringExtra("message");
                    String title = intent.getStringExtra("title");
                    emitOnNotificationOpened(text, title);
                }
            }
        }
    }

    private void emitOnNotificationReceived(PushData pushData) {
        if (pushData != null && pushData.getType() == PushData.Type.MESSAGE) {
            WritableMap writableMap = Arguments.createMap();
            writableMap.putString("title", pushData.getAuthor());
            writableMap.putString("text", pushData.getMessage());
            reactContext
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(ZendeskChatModule.onMessageReceivedEmitter, writableMap);
        }
    }

    private void emitOnNotificationOpened(String text, String title) {
        WritableMap writableMap = Arguments.createMap();
        writableMap.putString("title", title);
        writableMap.putString("text", text);
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(ZendeskChatModule.onMessageOpenedEmitter, writableMap);
    }

    public static boolean startOrResumeApp(Context inContext) {
        Intent launchIntent = inContext.getPackageManager().getLaunchIntentForPackage(inContext.getPackageName());
        // Make sure we have a launcher intent.
        if (launchIntent != null) {
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            inContext.startActivity(launchIntent);
            return true;
        }
        return false;
    }
}
