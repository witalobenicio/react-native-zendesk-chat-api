//
//  ItemFactory.h
//  ZendeskChat
//
//  Created by Witalo Benício on 07/01/20.
//  Copyright © 2020 Facebook. All rights reserved.
//
#import <Foundation/Foundation.h>
#import <ZDCChat/ZDCChat.h>

@interface ItemFactory : NSObject

+ (NSDictionary*) getDictionaryFromEntry: (ZDCChatEvent*) type;

@end
