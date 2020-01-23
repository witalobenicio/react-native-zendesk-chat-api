//
//  ItemFactory.m
//  ZendeskChat
//
//  Created by Witalo Benício on 07/01/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

#import "ItemFactory.h"

@implementation ItemFactory : NSObject

+ (NSMutableArray *) getArrayFromEntries: (NSArray *) events {
    NSMutableArray* entries = [[NSMutableArray alloc] init];
    for (int i = 0; i < events.count; i++) {
        NSMutableDictionary *item = [ItemFactory getDictionaryFromEntry:events[i]];
        if (item != nil) {
            [entries addObject:item];
        }
    }
    return entries;
}

+ (NSMutableDictionary *)getDictionaryFromEntry:(ZDCChatEvent*) entry {
    switch (entry.type) {
        case ZDCChatEventTypeAgentMessage:
            return [self getAgentMessageDict:entry];
            break;
        case ZDCChatEventTypeAgentUpload:
            return [self getAgentAttachmentDict:entry];
            break;
        case ZDCChatEventTypeVisitorMessage:
            return [self getVisitorMessageDict:entry];
            break;
        case ZDCChatEventTypeVisitorUpload:
            return [self getVisitorAttachmentDict:entry];
            break;
    }
    return [self getDefaultDict:entry];
}

+ (NSMutableDictionary *) getDefaultDict: (ZDCChatEvent*) item {
    NSMutableDictionary * defaultDict = [[NSMutableDictionary alloc] init];
    if (item != nil) {
        if (item.eventId) {
            [defaultDict setValue:item.eventId forKey:@"id"];
        }
        if (item.timestamp) {
            [defaultDict setValue:item.timestamp forKey:@"timestamp"];
        }
        if (item.displayName) {
            [defaultDict setValue:item.displayName forKey:@"name"];
        }
        if (item.verified) {
            [defaultDict setValue:@YES forKey:@"verified"];
        } else {
            [defaultDict setValue:@NO forKey:@"verified"];
        }
        return defaultDict;
    }
    return nil;
}

+ (NSMutableDictionary *) getAgentMessageDict: (ZDCChatEvent*) item {
    NSMutableDictionary * agentMessageDict = [self getDefaultDict:item];
    if (agentMessageDict != nil) {
        if (item.message) {
            [agentMessageDict setValue:item.message forKey:@"message"];
        }
        [agentMessageDict setValue:@"AGENT_MESSAGE" forKey:@"type"];
    }
    return agentMessageDict;
}

+ (NSMutableDictionary *) getVisitorMessageDict: (ZDCChatEvent*) item {
    NSMutableDictionary * visitorMessageDict = [self getDefaultDict:item];
    if (visitorMessageDict != nil) {
        [visitorMessageDict setValue:@"VISITOR_MESSAGE" forKey:@"type"];
        [visitorMessageDict setValue:item.message forKey:@"message"];
    }
    return visitorMessageDict;
}

+ (NSMutableDictionary *) getAgentAttachmentDict: (ZDCChatEvent*) item {
    NSMutableDictionary * agentAttachmentDict = [self getDefaultDict:item];
    if (agentAttachmentDict != nil) {
        [agentAttachmentDict setValue:@"AGENT_ATTACHMENT" forKey:@"type"];
        if (item.attachment != nil) {
            NSString* name = item.attachment.fileName;
            NSString* extension = [name pathExtension];
            [agentAttachmentDict setValue:item.attachment.fileName forKey:@"attachmentName"];
            [agentAttachmentDict setValue:item.attachment.fileSize forKey:@"attachmentSize"];
            [agentAttachmentDict setValue:extension forKey:@"attachmentExtension"];
            [agentAttachmentDict setValue:item.attachment.url forKey:@"absolutePath"];
            [agentAttachmentDict setValue:item.attachment.url forKey:@"path"];
        }
    }
    return agentAttachmentDict;
}

+ (NSMutableDictionary *) getVisitorAttachmentDict: (ZDCChatEvent*) item {
    NSMutableDictionary * visitorAttachmentDict = [self getDefaultDict:item];
    if (visitorAttachmentDict != nil) {
        [visitorAttachmentDict setValue:@"VISITOR_ATTACHMENT" forKey:@"type"];
        if (item.attachment != nil) {
            NSString *extension = [item.attachment.url pathExtension];
            [visitorAttachmentDict setValue:item.attachment.fileName forKey:@"attachmentName"];
            [visitorAttachmentDict setValue:item.attachment.fileSize forKey:@"attachmentSize"];
            [visitorAttachmentDict setValue:extension forKey:@"attachmentExtension"];
            [visitorAttachmentDict setValue:item.attachment.url forKey:@"absolutePath"];
            [visitorAttachmentDict setValue:item.attachment.url forKey:@"path"];
        }
        if (item.fileUpload != nil) {
            [visitorAttachmentDict setValue:item.fileUpload.uploadURL forKey:@"uploadUrl"];
            [visitorAttachmentDict setValue:item.fileUpload.fileExtension forKey:@"attachmentExtension"];
            [visitorAttachmentDict setValue:item.fileUpload.fileSize forKey:@"attachmentSize"];
            [visitorAttachmentDict setValue:item.fileUpload.fileName forKey:@"attachmentName"];
            [visitorAttachmentDict setValue:[NSNumber numberWithFloat:item.fileUpload.progress] forKey:@"uploadProgress"];
            if (item.attachment) {
                [visitorAttachmentDict setValue:item.attachment.url forKey:@"absolutePath"];
                [visitorAttachmentDict setValue:item.attachment.url forKey:@"path"];
            }
        }
    }
    return visitorAttachmentDict;
}

@end
