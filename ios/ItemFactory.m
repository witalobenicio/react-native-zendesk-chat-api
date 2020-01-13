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
        [entries addObject:item];
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
    [defaultDict setValue:item.eventId forKey:@"id"];
    [defaultDict setValue:item.timestamp forKey:@"timestamp"];
    [defaultDict setValue:item.displayName forKey:@"name"];
    return defaultDict;
}

+ (NSMutableDictionary *) getAgentMessageDict: (ZDCChatEvent*) item {
    NSMutableDictionary * agentMessageDict = [self getDefaultDict:item];
    [agentMessageDict setValue:@"AGENT_MESSAGE" forKey:@"type"];
    [agentMessageDict setValue:item.message forKey:@"message"];
    return agentMessageDict;
}

+ (NSMutableDictionary *) getVisitorMessageDict: (ZDCChatEvent*) item {
    NSMutableDictionary * visitorMessageDict = [self getDefaultDict:item];
    [visitorMessageDict setValue:@"VISITOR_MESSAGE" forKey:@"type"];
    [visitorMessageDict setValue:item.message forKey:@"message"];
    return visitorMessageDict;
}

+ (NSMutableDictionary *) getAgentAttachmentDict: (ZDCChatEvent*) item {
    NSMutableDictionary * agentAttachmentDict = [self getDefaultDict:item];
    [agentAttachmentDict setValue:@"AGENT_ATTACHMENT" forKey:@"type"];
    if (item.attachment != nil) {
        NSString* name = item.attachment.fileName;
        NSString* extension = [[name componentsSeparatedByString:@"."] objectAtIndex:1];
        [agentAttachmentDict setValue:item.attachment.fileName forKey:@"attachmentName"];
        [agentAttachmentDict setValue:item.attachment.fileSize forKey:@"attachmentSize"];
        [agentAttachmentDict setValue:extension forKey:@"attachmentExtension"];
        [agentAttachmentDict setValue:item.attachment.url forKey:@"absolutePath"];
        [agentAttachmentDict setValue:item.attachment.url forKey:@"path"];
    }
    return agentAttachmentDict;
}

+ (NSMutableDictionary *) getVisitorAttachmentDict: (ZDCChatEvent*) item {
    NSMutableDictionary * visitorAttachmentDict = [self getDefaultDict:item];
    [visitorAttachmentDict setValue:@"VISITOR_ATTACHMENT" forKey:@"type"];
    if (item.attachment != nil) {
        [visitorAttachmentDict setValue:item.attachment.fileName forKey:@"attachmentName"];
        [visitorAttachmentDict setValue:item.attachment.fileSize forKey:@"attachmentSize"];
        [visitorAttachmentDict setValue:item.fileUpload.fileExtension forKey:@"attachmentExtension"];
        [visitorAttachmentDict setValue:[NSNumber numberWithFloat:item.fileUpload.progress] forKey:@"uploadProgress"];
        [visitorAttachmentDict setValue:item.fileUpload.uploadURL forKey:@"uploadUrl"];
        [visitorAttachmentDict setValue:item.attachment.url forKey:@"absolutePath"];
        [visitorAttachmentDict setValue:item.attachment.url forKey:@"path"];
    }
    return visitorAttachmentDict;
}

@end
