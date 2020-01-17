#import "ZendeskChat.h"
#import "ItemFactory.m"

NSString *const onConnectionUpdateEmitter = @"onConnectionUpdate";
NSString *const onChatLogReceivedEmitter = @"onChatLogUpdate";
NSString *const onTimeoutReceivedEmitter = @"onTimeoutReceived";
NSString *const onDepartmentsReceivedEmitter = @"onDepartmentsUpdate";

bool hasConnectionListeners;
bool hasChatLogListeners;
bool hasTimeoutListeners;
NSMutableArray* entries;

@implementation ZendeskChat

- (NSArray<NSString *> *)supportedEvents {
    return @[onConnectionUpdateEmitter, onChatLogReceivedEmitter, onTimeoutReceivedEmitter, onDepartmentsReceivedEmitter];
}

RCT_EXPORT_MODULE()

RCT_REMAP_METHOD(startChat,
                  accountKey:(NSString *)accountKey
                  userInfo:(NSDictionary *)userInfo
                  userConfig:(NSDictionary *)userConfig
                 startChatWithResolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    ZDCVisitorInfo* visitorInfo = [[ZDCVisitorInfo alloc] init];
    if ([userInfo objectForKey:@"name"]) {
        visitorInfo.name = userInfo[@"name"];
    }
    if ([userInfo objectForKey:@"email"]) {
        visitorInfo.email = userInfo[@"email"];
    }
    if ([userInfo objectForKey:@"phone"]) {
        visitorInfo.phone = userInfo[@"phone"];
    }
    dispatch_sync(dispatch_get_main_queue(), ^{
        [[ZDCChatAPI instance] setVisitorInfo:visitorInfo];
        if (userConfig != nil) {
            ZDCAPIConfig* config = [[ZDCAPIConfig alloc] init];
            if ([userConfig objectForKey:@"department"]) {
                config.department = userConfig[@"department"];
            }
            if ([userConfig objectForKey:@"tags"]) {
                config.tags = userConfig[@"tags"];
            }
            [[ZDCChatAPI instance] startChatWithAccountKey:accountKey config:config];
        } else {
            [[ZDCChatAPI instance] startChatWithAccountKey:accountKey];
        }
        resolve([NSNumber numberWithBool:YES]);
    });
}

RCT_EXPORT_METHOD(endChat)
{
    dispatch_sync(dispatch_get_main_queue(), ^{
        [[ZDCChatAPI instance] endChat];
    });
}

RCT_REMAP_METHOD(getChatLog,
                 getChatLogWithResolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject
                 )
{
    dispatch_sync(dispatch_get_main_queue(), ^{
        if (entries != nil && entries.count == 0) {
            entries = [[NSMutableArray alloc] init];
        }
        NSArray* events = [[ZDCChatAPI instance] livechatLog];
        if (events != nil) {
            entries = [ItemFactory getArrayFromEntries:events];
            resolve(entries);
        }
    });
}

RCT_EXPORT_METHOD(addChatLogObserver)
{
    dispatch_sync(dispatch_get_main_queue(), ^{
        [[ZDCChatAPI instance] addObserver:self forChatLogEvents:@selector(chatEvent:)];
    });
    hasChatLogListeners = YES;
    if (entries != nil && entries.count == 0) {
        entries = [[NSMutableArray alloc] init];
    }
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
    if (events != nil) {
        entries = [[NSMutableArray alloc] init];
        for (int i = 0; i < events.count; i++) {
            NSMutableDictionary *item = [ItemFactory getDictionaryFromEntry:events[i]];
            if (item != nil) {
                [entries addObject:item];
            }
        }
        [self sendEventWithName:onChatLogReceivedEmitter body:entries];
    }
}

RCT_EXPORT_METHOD(addDepartmentsObserver)
{
    dispatch_sync(dispatch_get_main_queue(), ^{
        [[ZDCChatAPI instance] addObserver:self forDepartmentEvents:@selector(departmentEvent:)];
    });
}

RCT_EXPORT_METHOD(deleteDepartmentsObserver)
{
    dispatch_sync(dispatch_get_main_queue(), ^{
        [[ZDCChatAPI instance] removeObserverForChatLogEvents:self];
    });
}

-(void)departmentEvent:(NSNotification *) notification
{
    NSArray *departments = [[ZDCChatAPI instance] departments];
    NSString * statusOnline = @"OFFLINE";
    BOOL online = [[ZDCChatAPI instance] isAccountOnline];
    if (online) {
        statusOnline = @"ONLINE";
    }
    if (departments != nil && departments.count > 0) {
        [self sendEventWithName:onDepartmentsReceivedEmitter body:@{@"status": statusOnline, @"departments": departments }];
    }
}

RCT_EXPORT_METHOD(addChatConnectionObserver)
{
    hasConnectionListeners = YES;
    dispatch_sync(dispatch_get_main_queue(), ^{
        [[ZDCChatAPI instance] addObserver:self forConnectionEvents:@selector(connectionEvent:)];
        dispatch_time_t delay = dispatch_time(DISPATCH_TIME_NOW, NSEC_PER_SEC * 0.5);
        dispatch_after(delay, dispatch_get_main_queue(), ^(void){
            [self connectionEvent:[NSNotification notificationWithName:@"name" object:nil]];
        });
    });
}

RCT_EXPORT_METHOD(getChatConnection:(RCTResponseSenderBlock)callback)
{
    dispatch_sync(dispatch_get_main_queue(), ^{
        ZDCConnectionStatus status = [[ZDCChatAPI instance] connectionStatus];
        NSString *statusText = [self getConnectionName:status];
        if (statusText != nil) {
            callback(@[statusText]);
        }
    });
}

RCT_EXPORT_METHOD(isOnline:(RCTResponseSenderBlock)callback)
{
    dispatch_sync(dispatch_get_main_queue(), ^{
        BOOL isOnline = [[ZDCChatAPI instance] isAccountOnline];
        if (isOnline) {
            callback(@[@"ONLINE"]);
        } else {
            callback(@[@"OFFLINE"]);
        }
    });
}

RCT_EXPORT_METHOD(deleteChatConnectionObserver)
{
    hasConnectionListeners = NO;
    dispatch_sync(dispatch_get_main_queue(), ^{
        [[ZDCChatAPI instance] removeObserverForConnectionEvents:self];
    });
}

-(NSString *)getConnectionName:(ZDCConnectionStatus) status {
    if (status) {
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
        return statusText;
    }
    return nil;
}

-(void)connectionEvent:(NSNotification *) notification
{
    ZDCConnectionStatus status = [[ZDCChatAPI instance] connectionStatus];
    NSString *statusText = [self getConnectionName:status];
    NSLog(@"Notification: %@", notification.name);
    NSLog(@"Notification with status: %@ %@", statusText, onConnectionUpdateEmitter);
    if (statusText != nil) {
        [self sendEventWithName:onConnectionUpdateEmitter body:@{@"status": statusText}];
    }
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
    NSLog(@"File exists: %d", [fm fileExistsAtPath:path]);
    if ([fm fileExistsAtPath:path]) {
        dispatch_sync(dispatch_get_main_queue(), ^{
            NSString *fileName = [[path lastPathComponent] stringByDeletingPathExtension];
            NSString *fileExtension = [path pathExtension];
            if ([path containsString:@"jpg"] ||
                [path containsString:@"png"] ||
                [path containsString:@"jpeg"] ||
                [path containsString:@"gif"]
                ) {
                UIImage *image = [UIImage imageWithContentsOfFile:path];
                [self addUploadEventObserver];
                [[ZDCChatAPI instance] uploadImage:image name:[NSString stringWithFormat:@"%@.%@", fileName, fileExtension]];
            } else {
                NSData *data = [[NSFileManager defaultManager] contentsAtPath:path];
                [[ZDCChatAPI instance] uploadFileWithData:data name:fileName];
            }
        });
    }
}

@end
