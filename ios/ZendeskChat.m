#import "ZendeskChat.h"
#import "ItemFactory.m"

@implementation ZendeskChat

{
    bool hasConnectionListeners;
    bool hasChatLogListeners;
    bool hasTimeoutListeners;
    NSString* onConnectionUpdateEmitter;
    NSString* onChatLogReceivedEmitter;
    NSString* onTimeoutReceivedEmitter;
}


- (NSArray<NSString *> *)supportedEvents {
    onConnectionUpdateEmitter = @"onConnectionUpdate";
    onChatLogReceivedEmitter = @"onChatLogUpdate";
    onTimeoutReceivedEmitter = @"onTimeoutReceived";
    return @[onConnectionUpdateEmitter, onChatLogReceivedEmitter, onTimeoutReceivedEmitter];
}

RCT_EXPORT_MODULE()

RCT_REMAP_METHOD(startChat,
                 accountKey:(NSString *)accountKey
                 userInfo:(NSDictionary *)userInfo
                 startChatWithResolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    ZDCVisitorInfo* visitorInfo = [[ZDCVisitorInfo alloc] init];
    visitorInfo.name = userInfo[@"name"];
    visitorInfo.email = userInfo[@"email"];
    visitorInfo.phone = userInfo[@"phone"];
    dispatch_sync(dispatch_get_main_queue(), ^{
        [[ZDCChatAPI instance] setVisitorInfo:visitorInfo];
        [[ZDCChatAPI instance] startChatWithAccountKey:accountKey];
        resolve([NSNumber numberWithBool:YES]);
    });
}


RCT_EXPORT_METHOD(addChatLogObserver)
{
    dispatch_sync(dispatch_get_main_queue(), ^{
        [[ZDCChatAPI instance] addObserver:self forChatLogEvents:@selector(chatEvent:)];
    });
    hasChatLogListeners = YES;
    entries = [[NSMutableArray alloc] init];
}

RCT_EXPORT_METHOD(deleteChatLogObserver)
{
    hasChatLogListeners = NO;
    dispatch_sync(dispatch_get_main_queue(), ^{
        [[ZDCChatAPI instance] removeObserverForChatLogEvents:self];
    });
}

-(void)chatEvent:(NSNotification *) notification
{
    NSArray *events = [[ZDCChatAPI instance] livechatLog];
    ZDCChatEvent *event = [events lastObject];
    long sizeDiff = events.count - entries.count;
    if (entries.count == 0) {
        for (int i = 0; i < events.count; i++) {
            NSMutableDictionary *item = [ItemFactory getDictionaryFromEntry:events[i]];
            [entries addObject:item];
        }
    }
    if (sizeDiff > 0) {
        NSMutableDictionary *item = [ItemFactory getDictionaryFromEntry:event];
        [entries addObject:item];
    }
    //TODO: Emit Chat EVENT
    [self sendEventWithName:onChatLogReceivedEmitter body:@{@"entries": entries}];
//    logCallback(@[entries]);
}

RCT_EXPORT_METHOD(addChatConnectionObserver)
{
    hasConnectionListeners = YES;
    NSLog(@"addChatConnection Mensagem");
    dispatch_sync(dispatch_get_main_queue(), ^{
        [[ZDCChatAPI instance] addObserver:self forConnectionEvents:@selector(connectionEvent:)];
    });
}

RCT_EXPORT_METHOD(deleteChatConnectionObserver)
{
    hasConnectionListeners = NO;
    dispatch_sync(dispatch_get_main_queue(), ^{
        [[ZDCChatAPI instance] removeObserverForConnectionEvents:self];
    });
}

-(void)connectionEvent:(NSNotification *) notification
{
    ZDCConnectionStatus status = [[ZDCChatAPI instance] connectionStatus];
    NSString* statusText = @"";
    switch (status) {
        case ZDCConnectionStatusConnecting:
            statusText = @"CONNECTING";
            break;
        case ZDCConnectionStatusClosed:
            statusText = @"CLOSED";
            break;
        case ZDCConnectionStatusConnected:
            statusText = @"CONNECTED";
            break;
        case ZDCConnectionStatusDisconnected:
            statusText = @"DISCONNECTED";
            break;
        case ZDCConnectionStatusNoConnection:
            statusText = @"NO_CONNECTION";
            break;
        case ZDCConnectionStatusUninitialized:
            statusText = @"UNITIALIZED";
            break;
        default:
            statusText = @"UNKNOWN";
            break;
    }
    NSLog(@"STATUS CONNECTION: %@", statusText);
    [self sendEventWithName:onConnectionUpdateEmitter body:@{@"status": statusText}];
}

RCT_EXPORT_METHOD(addChatTimeoutObserver)
{
    hasTimeoutListeners = YES;
    dispatch_sync(dispatch_get_main_queue(), ^{
        [[ZDCChatAPI instance] addObserver:self forTimeoutEvents:@selector(timeoutEvent:)];
    });
}

RCT_EXPORT_METHOD(deleteChatTimeoutObserver)
{
    hasTimeoutListeners = NO;
    dispatch_sync(dispatch_get_main_queue(), ^{
        [[ZDCChatAPI instance] removeObserverForTimeoutEvents:self];
    });
}

-(void)timeoutEvent:(ZDCChatEvent*) notification
{
    [self sendEventWithName:onTimeoutReceivedEmitter body:@{@"timeout": @true}];
}

RCT_EXPORT_METHOD(sendMessage:(NSString*)message)
{
    
        NSLog(@"Mensagem: %@", message);
        [[ZDCChatAPI instance] sendChatMessage:message];
}

RCT_EXPORT_METHOD(sendFile:(NSString*)path)
{
    NSFileManager *fm = [NSFileManager defaultManager];
    if ([fm fileExistsAtPath:path]) {
        dispatch_sync(dispatch_get_main_queue(), ^{
            [[ZDCChatAPI instance] uploadFileWithPath:path name:@"file"];
        });
    }
}

@end

