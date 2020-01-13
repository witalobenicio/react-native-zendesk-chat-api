# react-native-zendesk-chat
Wrapper around Zendesk Chat API SDK for mobile Android and iOS

## Getting started

`$ npm install react-native-zendesk-chat --save`

### Mostly automatic installation

`$ react-native link react-native-zendesk-chat-api`
<br />

or if you prefer:
<br />

`$ yarn add global react-native-zendesk-chat-api`
<br />

then you need to link the package:
<br />

`$ react-native link react-native-zendesk-chat-api`

## Usage
###Import:
```javascript
import ZendeskChatApi from 'react-native-zendesk-chat-api';
```

###Start a chat session:

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

###End a chat session:

```javascript
ZendeskChatApi.endChat();
```

###Start listening to connection updates:

```javascript
const connectionUpdate = ({ status }) => {
  if (status === ZendeskChatAPI.connectionTypes.CONNECTED) {
    // You can send messages now
  }
};

ZendeskChatApi.addConnectionObserver(connectionUpdate);
```

###Stop listening to connection updates:

```javascript
//Remember to do this
ZendeskChatApi.deleteChatConnectionObserver();
```

###Start listening to chat updates:

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

###Stop listening to chat updates:

```javascript
//Remember to do this
ZendeskChatApi.deleteChatLogObserver();
```

###Start listening to timeout event:

```javascript
const onTimeoutReceived = ({ timeout }) => {
};

ZendeskChatApi.addChatTimeoutObserver(onTimeoutReceived);
```
###Stop listening to timeout event:

```javascript
//Remember to do this
ZendeskChatApi.deleteChatTimeoutObserver();
```
###Get a list of live chat messages:

```javascript
ZendeskChatApi.getChatLog()
  .then(entries => {
    // Do your stuff
  });
```
###Send a message:

```javascript
ZendeskChatApi.sendMessage("My message goes here");
```

###Send a file:

```javascript
// You need to ensure that you will only send files with supported extensions
// This is defined on your Zendesk Chat Dashboard
ZendeskChatApi.sendFile("path/to/myFile");
```

#Next planned steps

- Listening to file uploads (error handling)
- Handle messages errors
- Set tags to the chat
- Set a department when starting a chat
