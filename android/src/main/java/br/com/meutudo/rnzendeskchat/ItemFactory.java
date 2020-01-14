package br.com.meutudo.rnzendeskchat;

import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.zopim.android.sdk.model.ChatLog;
import com.zopim.android.sdk.model.items.AgentAttachment;
import com.zopim.android.sdk.model.items.AgentMessage;
import com.zopim.android.sdk.model.items.RowItem;
import com.zopim.android.sdk.model.items.VisitorAttachment;
import com.zopim.android.sdk.model.items.VisitorMessage;

import java.util.LinkedHashMap;

public class ItemFactory {

    public static WritableMap getReadableMapFromRowItem(String id, RowItem item) {
        switch (item.getType()) {
            case AGENT_MESSAGE: {
                return getAgentMessageMap(id, (AgentMessage) item);
            }

            case AGENT_ATTACHMENT: {
                return getAgentAttachmentMap(id, (AgentAttachment) item);
            }

            case VISITOR_ATTACHMENT: {
                return getVisitorAttachmentMap(id, (VisitorAttachment) item);
            }

            case VISITOR_MESSAGE: {
                return getVisitorMessageMap(id, (VisitorMessage) item);
            }
        }
        return getDefaultMap(item);
    }

    public static WritableMap getReadableMapFromRowItem(String id, ChatLog item) {
        switch (item.getType()) {
            case CHAT_MSG_AGENT: {
                return getAgentMessageMap(id, item);
            }

            case CHAT_MSG_VISITOR: {
                return getVisitorMessageMap(id, item);
            }

            case VISITOR_ATTACHMENT: {
                return getVisitorAttachmentMap(id, item);
            }
        }
        return getDefaultMap(id, item);
    }

    public static WritableArray getArrayFromEntries(LinkedHashMap<String, ChatLog> rowItems) {
        WritableArray items = Arguments.createArray();
        Object[] rowItemsArr = rowItems.values().toArray();
        Object[] keys = rowItems.keySet().toArray();
        try {
            for (int i = 0; i < rowItems.size(); i++) {
                ChatLog rowItem = (ChatLog) rowItemsArr[i];
                String id = (String) keys[i];
                WritableMap itemMap = ItemFactory.getReadableMapFromRowItem(id, rowItem);
                items.pushMap(itemMap);
            }
        } catch (NullPointerException e) {
            Log.d("ERROR CHAT", Log.getStackTraceString(e));
        } catch (Exception e) {
            Log.d("ERROR CHAT", Log.getStackTraceString(e));
        }
        return items;
    }

    private static WritableMap getDefaultMap(RowItem item) {
        WritableMap defaultMap = new WritableNativeMap();
        defaultMap.putString("id", item.getId());
        defaultMap.putDouble("timestamp", item.getTimestamp());
        defaultMap.putString("name", item.getDisplayName());
        defaultMap.putString("type", item.getType().toString());
        return defaultMap;
    }

    private static WritableMap getDefaultMap(String id, ChatLog item) {
        WritableMap defaultMap = new WritableNativeMap();
        defaultMap.putString("id", id);
        defaultMap.putDouble("timestamp", item.getTimestamp());
        defaultMap.putString("name", item.getDisplayName());
        return defaultMap;
    }

    private static WritableMap getAgentMessageMap(String id, AgentMessage item) {
        WritableMap agentMessage = getDefaultMap(item);
        agentMessage.putString("participantId", id);
        agentMessage.putString("message", item.getMessage());
        return agentMessage;
    }

    private static WritableMap getAgentMessageMap(String id, ChatLog item) {
        WritableMap agentMessage = getDefaultMap(id, item);
        try {
            agentMessage.putString("participantId", id);
            agentMessage.putString("participantId", id);
            if (item.getAttachment() != null) {
                String path = item.getAttachment().getName();
                agentMessage.putString("type", "AGENT_ATTACHMENT");
                agentMessage.putString("attachmentName", item.getAttachment().getName());
                agentMessage.putDouble("attachmentSize", item.getAttachment().getSize());
                agentMessage.putString("attachmentExtension", path.substring(path.lastIndexOf(".")));
                agentMessage.putString("absolutePath", item.getAttachment().getUrl().toString());
                agentMessage.putString("path", item.getAttachment().getUrl().toString());
            } else {
                agentMessage.putString("message", item.getMessage());
                agentMessage.putString("type", "AGENT_MESSAGE");
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.d("ERROR CHAT", Log.getStackTraceString(e));
        } catch (Exception e) {
            Log.d("ERROR CHAT", Log.getStackTraceString(e));
        }
        return agentMessage;
    }

    private static WritableMap getVisitorMessageMap(String id, VisitorMessage item) {
        WritableMap visitorMessage = getDefaultMap(item);
        visitorMessage.putString("participantId", id);
        visitorMessage.putString("message", item.getMessage());
        return visitorMessage;
    }

    private static WritableMap getVisitorMessageMap(String id, ChatLog item) {
        WritableMap visitorMessage = getDefaultMap(id, item);
        visitorMessage.putString("participantId", id);
        visitorMessage.putString("message", item.getMessage());
        visitorMessage.putString("type", "VISITOR_MESSAGE");
        if (item.getAttachment() != null) {
            try {
                String path = item.getFile().getPath();
                visitorMessage.putString("participantId", id);
                visitorMessage.putString("attachmentName", item.getFile().getName());
                visitorMessage.putDouble("attachmentSize", item.getFile().length());
                visitorMessage.putString("attachmentExtension", path.substring(path.lastIndexOf(".")));
                visitorMessage.putString("absolutePath", item.getFile().getAbsolutePath());
                visitorMessage.putString("uploadUrl", item.getUploadUrl().toString());
                visitorMessage.putString("error", item.getError().getValue());
                visitorMessage.putString("path", path);
                visitorMessage.putString("type", "VISITOR_ATTACHMENT");
            } catch (NullPointerException e) {
                e.printStackTrace();
                Log.d("ERROR CHAT", Log.getStackTraceString(e));
            } catch (Exception e) {
                Log.d("ERROR CHAT", Log.getStackTraceString(e));
            }
        }
        return visitorMessage;
    }

    private static WritableMap getAgentAttachmentMap(String id, AgentAttachment item) {
        WritableMap agentFile = getDefaultMap(item);
        try {
            String path = item.getAttachmentFile().getPath();
            agentFile.putString("participantId", id);
            agentFile.putString("attachmentName", item.getAttachmentName());
            agentFile.putDouble("attachmentSize", item.getAttachmentFile().length());
            agentFile.putString("attachmentExtension", path.substring(path.lastIndexOf(".")));
            agentFile.putString("absolutePath", item.getAttachmentFile().getAbsolutePath());
            agentFile.putDouble("attachmentSize", item.getAttachmentSize());
            agentFile.putString("path", path);
        } catch (NullPointerException e) {
            Log.d("ERROR CHAT", Log.getStackTraceString(e));
        } catch (Exception e) {
            Log.d("ERROR CHAT", Log.getStackTraceString(e));
        }
        return agentFile;
    }

    private static WritableMap getVisitorAttachmentMap(String id, VisitorAttachment item) {
        WritableMap visitorFile = getDefaultMap(item);
        try {
            String path = item.getFile().getPath();
            visitorFile.putString("participantId", id);
            visitorFile.putString("attachmentName", item.getFile().getName());
            visitorFile.putDouble("attachmentSize", item.getFile().length());
            visitorFile.putString("attachmentExtension", path.substring(path.lastIndexOf(".")));
            visitorFile.putString("absolutePath", item.getFile().getAbsolutePath());
            visitorFile.putDouble("uploadProgress", item.getUploadProgress());
            visitorFile.putString("uploadUrl", item.getUploadUrl().toString());
            visitorFile.putString("error", item.getError());
            visitorFile.putString("path", path);
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.d("ERROR CHAT", Log.getStackTraceString(e));
        } catch (Exception e) {
            Log.d("ERROR CHAT", Log.getStackTraceString(e));
        }
        return visitorFile;
    }

    private static WritableMap getVisitorAttachmentMap(String id, ChatLog item) {
        WritableMap visitorFile = getDefaultMap(id, item);
        try {
            visitorFile.putString("participantId", id);
            if (item.getFile() != null) {
                // User is sending the file
                String path = item.getFile().getPath();
                visitorFile.putString("attachmentName", item.getFileName());
                visitorFile.putDouble("attachmentSize", item.getFile().length());
                visitorFile.putString("attachmentExtension", path.substring(path.lastIndexOf(".")));
                visitorFile.putString("absolutePath", item.getFile().getAbsolutePath());
                if (item.getUploadUrl() != null) {
                    visitorFile.putString("uploadUrl", item.getUploadUrl().toString());
                }
                visitorFile.putDouble("progress", item.getProgress());
                visitorFile.putString("path", path);
            } else {
                // User sent the file
                String path = item.getAttachment().getUrl().toString();
                visitorFile.putString("attachmentName", item.getAttachment().getName());
                visitorFile.putDouble("attachmentSize", item.getAttachment().getSize());
                visitorFile.putString("attachmentExtension", path.substring(path.lastIndexOf(".")));
                visitorFile.putString("absolutePath", item.getAttachment().getUrl().toString());
                visitorFile.putString("path", path);
            }
            visitorFile.putString("type", "VISITOR_ATTACHMENT");
            if (item.getError() != null) {
                visitorFile.putString("error", item.getError().getValue());
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.d("ERROR CHAT", Log.getStackTraceString(e));
        } catch (Exception e) {
            Log.d("ERROR CHAT", Log.getStackTraceString(e));
        }
        return visitorFile;
    }
}
