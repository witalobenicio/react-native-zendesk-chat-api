package br.com.meutudo.rnzendeskchat;

import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.zopim.android.sdk.model.items.AgentAttachment;
import com.zopim.android.sdk.model.items.AgentMessage;
import com.zopim.android.sdk.model.items.RowItem;
import com.zopim.android.sdk.model.items.VisitorAttachment;
import com.zopim.android.sdk.model.items.VisitorMessage;

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

    private static WritableMap getDefaultMap(RowItem item) {
        WritableMap defaultMap = new WritableNativeMap();
        defaultMap.putString("id", item.getId());
        defaultMap.putDouble("timestamp", item.getTimestamp());
        defaultMap.putString("name", item.getDisplayName());
        defaultMap.putString("type", item.getType().toString());
        return defaultMap;
    }

    private static WritableMap getAgentMessageMap(String id, AgentMessage item) {
        WritableMap agentMessage = getDefaultMap(item);
        agentMessage.putString("participantId", id);
        agentMessage.putString("message", item.getMessage());
        return agentMessage;
    }

    private static WritableMap getVisitorMessageMap(String id, VisitorMessage item) {
        WritableMap visitorMessage = getDefaultMap(item);
        visitorMessage.putString("participantId", id);
        visitorMessage.putString("message", item.getMessage());
        return visitorMessage;
    }

    private static WritableMap getAgentAttachmentMap(String id, AgentAttachment item) {
        WritableMap agentFile = getDefaultMap(item);
        agentFile.putString("participantId", id);
        agentFile.putString("attachmentName", item.getAttachmentName());
        agentFile.putString("absolutePath", item.getAttachmentFile().getAbsolutePath());
        agentFile.putDouble("attachmentSize", item.getAttachmentSize());
        agentFile.putString("path", item.getAttachmentFile().getPath());
        return agentFile;
    }

    private static WritableMap getVisitorAttachmentMap(String id, VisitorAttachment item) {
        WritableMap visitorFile = getDefaultMap(item);
        visitorFile.putString("participantId", id);
        visitorFile.putString("attachmentName", item.getFile().getName());
        visitorFile.putString("absolutePath", item.getFile().getAbsolutePath());
        visitorFile.putDouble("uploadProgress", item.getUploadProgress());
        visitorFile.putString("uploadUrl", item.getUploadUrl().toString());
        visitorFile.putString("error", item.getError());
        visitorFile.putString("path", item.getFile().getPath());
        return visitorFile;
    }
}
