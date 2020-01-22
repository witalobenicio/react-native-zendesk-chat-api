package br.com.meutudo.rnzendeskchat;

import android.content.Context;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.zopim.android.sdk.api.ZopimChatApi;
import com.zopim.android.sdk.data.observers.ChatItemsObserver;
import com.zopim.android.sdk.data.observers.ChatLogObserver;
import com.zopim.android.sdk.model.ChatLog;
import com.zopim.android.sdk.model.Connection;
import com.zopim.android.sdk.model.items.RowItem;
import br.com.meutudo.rnzendeskchat.ZendeskChatModule;

import java.util.LinkedHashMap;
import java.util.TreeMap;

public class ChatObserver extends ChatLogObserver {

    private ReactContext context;
    private WritableArray items;

    public ChatObserver(ReactContext context) {
        super();
        this.context = context;
    }

//    @Override
//    protected void updateChatItems(TreeMap<String, RowItem> rowItems) {
////        if (items == null) {
//            items = new WritableNativeArray();
//            Object[] rowItemsArr = rowItems.values().toArray();
//            Object[] keys = rowItems.keySet().toArray();
//            for (int i = 0; i < rowItems.size(); i++) {
//                RowItem rowItem = (RowItem) rowItemsArr[i];
//                String id = (String) keys[i];
//                WritableMap itemMap = ItemFactory.getReadableMapFromRowItem(id, rowItem);
//                items.pushMap(itemMap);
//            }
////        }
////        else {
////            int sizeDiff = rowItems.size() - items.size();
////            if (sizeDiff > 0) {
////                RowItem rowItem = rowItems.lastEntry().getValue();
////                String id = rowItems.lastKey();
////                WritableMap itemMap = ItemFactory.getReadableMapFromRowItem(id, rowItem);
////                items.pushMap(itemMap);
////            }
////        }
//        this.context
//                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
//                .emit(ZendeskChatModule.onChatLogUpdateEmitter, items);
//    }

    @Override
    protected void update(LinkedHashMap<String, ChatLog> rowItems) {
        items = new WritableNativeArray();
        Object[] rowItemsArr = rowItems.values().toArray();
        Object[] keys = rowItems.keySet().toArray();
        for (int i = 0; i < rowItems.size(); i++) {
            ChatLog rowItem = (ChatLog) rowItemsArr[i];
            if (rowItem.getType() == ChatLog.Type.MEMBER_LEAVE) {
                if (ZopimChatApi.getDataSource().getConnection().getStatus() == Connection.Status.CONNECTED) {
                    this.context
                            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(ZendeskChatModule.onAgentLeaveEmitter, "AGENT_LEAVE");
                }
            }
            String id = (String) keys[i];
            WritableMap itemMap = ItemFactory.getReadableMapFromRowItem(id, rowItem);
            if (itemMap != null) {
                items.pushMap(itemMap);
            }
        }
        this.context
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(ZendeskChatModule.onChatLogUpdateEmitter, items);
    }
}
