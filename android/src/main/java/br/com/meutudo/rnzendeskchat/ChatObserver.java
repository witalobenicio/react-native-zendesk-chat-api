package br.com.meutudo.rnzendeskchat;

import android.content.Context;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.zopim.android.sdk.data.observers.ChatItemsObserver;
import com.zopim.android.sdk.model.items.RowItem;
import br.com.meutudo.rnzendeskchat.ZendeskChatModule;

import java.util.LinkedHashMap;
import java.util.TreeMap;

public class ChatObserver extends ChatItemsObserver {

    private ReactContext context;
    private WritableArray items;

    public ChatObserver(ReactContext context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void updateChatItems(TreeMap<String, RowItem> rowItems) {
        if (items == null) {
            items = new WritableNativeArray();
            RowItem[] rowItemsArr = (RowItem[]) rowItems.values().toArray();
            String[] keys = (String[]) rowItems.keySet().toArray();
            for (int i = 0; i < rowItems.size(); i++) {
                RowItem rowItem = rowItemsArr[i];
                String id = keys[i];
                WritableMap itemMap = ItemFactory.getReadableMapFromRowItem(id, rowItem);
                items.pushMap(itemMap);
            }
        } else {
            int sizeDiff = rowItems.size() - items.size();
            if (sizeDiff > 0) {
                RowItem rowItem = (RowItem) rowItems.lastEntry();
                String id = rowItems.lastKey();
                WritableMap itemMap = ItemFactory.getReadableMapFromRowItem(id, rowItem);
                items.pushMap(itemMap);
            }
        }
        this.context
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(ZendeskChatModule.onChatLogUpdateEmitter, items);
    }
}
