#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <ZDCChatApi/ZDCChatApi.h>
#import <ZDCChat/ZDCChat.h>

@interface ZendeskChat : RCTEventEmitter <RCTBridgeModule> {
    NSMutableArray *entries;
}

@end
