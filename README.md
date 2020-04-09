# rn-zendesk-chat-api
Wrapper around Zendesk Chat API SDK for mobile Android and iOS

**IMPORTANT! This project doesn't support auto link feature and Pods yet.
So you may have not be able to use, or have to do some workaround to use in react native versions above 0.60.**

## Getting started

`$ npm install rn-zendesk-chat --save`

### Mostly automatic installation

`$ react-native link rn-zendesk-chat-api`
<br />

or if you prefer:
<br />

`$ yarn add global rn-zendesk-chat-api`
<br />

then you need to link the package:
<br />

`$ react-native link rn-zendesk-chat-api`

## Receiving notifications
In order to receive chat notifications you have to follow the Zendesk Chat documentation steps.
Just do the configuration stuff.
</br>
[iOS](https://developer.zendesk.com/embeddables/docs/ios-chat-sdk/push_notifications)
</br>
[Android](https://developer.zendesk.com/embeddables/docs/android-chat-sdk/push_notifications)

### Extra configuration for iOS

For notifications to work on iOS, you need to add some piece of code to your `AppDelegate.m`

First, import `ZendeskChat.h`:

`#import "ZendeskChat.h"`

And add the following code in specific methods:

```objective-c
- (void) application:(UIApplication*)app didRegisterForRemoteNotificationsWithDeviceToken:(NSData*)tokenData {
  [ZendeskChat savePushToken:tokenData];
  [RCTPushNotificationManager didRegisterForRemoteNotificationsWithDeviceToken:tokenData];
}

- (void) application:(UIApplication*)application didReceiveRemoteNotification:(NSDictionary*)userInfo {
  [ZendeskChat didReceiveRemoteNotification:userInfo];
}
```

### Registering token to receive notifications

Zendesk uses some observers to know if the app is in foreground or background, and if you don't put those specific checks to enable and disable the token, you may get some problems when sending files to the chat:
</br>So you need to add the following code to handle those problems:

```javascript
AppState.addEventListener('change', this.handleAppStateChange);
    this.appstate = AppState.addListener('appStateDidChange', (status: {}) => {
      if (status.app_state.match('inactive|background') && !this.props.pickerShowing) {
        ZendeskChat.registerFCMToken(ZENDESK_APP_KEY);
      }
      if (status.app_state.match('active') && this.props.route === 'Chat') {
        ZendeskChat.registerFCMToken(null);
      }
    });
```

Everytime you enter in your Chat screen do:

```javascript
  ZendeskChat.registerFCMToken(null);
```

And everytime your Chat screens unmounts:

```javascript
  ZendeskChat.registerFCMToken(ZENDESK_APP_KEY);
```

* When you set token value to `null` you are disabling the notifications


#### Platform specifics
Follow these steps for specific platforms

#### On Android:

You call the method to set token first (note that in this specific moment, you only set token for android):

```javascript
componentWillMount() {
    if (Platform.OS === 'android') {
      ZendeskChat.registerFCMToken(ZENDESK_APP_KEY);
    }
}
```

#### Show notifications in foreground (Android):
This is only required for Android. </br>
In order to show notifications when your app is in foreground, you need to listen for the following methods:

```javascript
  ZendeskChat.onNotificationReceived(this.onChatNotificationReceived);

onChatNotificationReceived = ({ title, text }) => {
  \\ Just show the notification if you aren't in Chat screen
  if (title && this.props.route !== 'Chat') {
      ZendeskChat.showChatNotification(text, title);
    }
};
```

If you want to do something when notification is opened:

```javascript
ZendeskChat.onNotificationOpened(this.onNotificationOpened);

onNotificationOpened = ({ title, text }) => {
  // Do your stuff
  };
```

#### On iOS:

On iOS you only will register the token when the app is ONLINE. This is because Zendesk uses some observers that may override some behaviours of some libs (e.g.: react-native-keyboard-manager), and can crash your app.
So, wait for chat to be ONLINE and then register for iOS:

```javascript
ZendeskChat.isOnline((chatStatus) => {
        if (chatStatus === 'ONLINE') {
          if (Platform.OS === 'ios') {
            ZendeskChat.registerFCMToken(ZENDESK_APP_KEY);
          }
        }
      });
```


## Usage
### Import:
```javascript
import ZendeskChatApi from 'rn-zendesk-chat-api';
```

### Start a chat session:

```javascript
const userInfo = {
  name: 'Witalo Benicio',
  email: 'contato@witalobenicio.com',
  phone: '+558899999999',
  note: 'This visitor is very nice',
};

//Currently supporting just department and tag as config values
const chatConfig = {
  department: 'My Department',
  tags: ['Tag1', 'Tag2'],
};

// This is a promise, but just to know that you called succesfully. In order to start sending messages, you need to wait until status === 'CONNECTED'
ZendeskChatApi.startChat("YOUR_ACCOUNT_KEY", userInfo, chatConfig);
```

### End a chat session:

```javascript
ZendeskChatApi.endChat();
```

### Start listening to connection updates:

```javascript
const connectionUpdate = ({ status }) => {
  if (status === ZendeskChatAPI.connectionTypes.CONNECTED) {
    // You can send messages now
  }
};

ZendeskChatApi.addConnectionObserver(connectionUpdate);
```

### Stop listening to connection updates:

```javascript
//Remember to do this
ZendeskChatApi.deleteChatConnectionObserver();
```

### Start listening to chat updates:

```javascript
const chatUpdate = (entries) => {
  //entries is an Array, so you can handle every message to show in your FlatList e.g.
  //Every entry has a type, which at the moment can be found at:
  ZendeskChatAPI.chatLogTypes.VISITOR_ATTACHMENT; //This is a file sent by the user
  ZendeskChatAPI.chatLogTypes.VISITOR_MESSAGE; //This is a message sent by the user
  ZendeskChatAPI.chatLogTypes.AGENT_ATTACHMENT; //This is a file sent by an agent
  ZendeskChatAPI.chatLogTypes.AGENT_MESSAGE; //This is a message sent by an agent
};

ZendeskChatApi.addChatLogObserver(chatUpdate);
```

### Stop listening to chat updates:

```javascript
//Remember to do this
ZendeskChatApi.deleteChatLogObserver();
```

### Start listening to timeout event:

```javascript
const onTimeoutReceived = ({ timeout }) => {
};

ZendeskChatApi.addChatTimeoutObserver(onTimeoutReceived);
```
### Stop listening to timeout event:

```javascript
//Remember to do this
ZendeskChatApi.deleteChatTimeoutObserver();
```
### Get a list of live chat messages:

```javascript
ZendeskChatApi.getChatLog()
  .then(entries => {
    // Do your stuff
  });
```
### Send a message:

```javascript
ZendeskChatApi.sendMessage("My message goes here");
```

### Send a file:

```javascript
// You need to ensure that you will only send files with supported extensions
// This is defined on your Zendesk Chat Dashboard

// Files that begin with file:// need to have this sufix removed
const realPath = (() => {
      if (path.includes('file://')) {
        return path.replace('file://', '');
      }
      return path;
    })();
    ZendeskChatApi.sendFile(realPath);
```

# Next planned steps

- Listening to file uploads (error handling)
- Handle messages errors
