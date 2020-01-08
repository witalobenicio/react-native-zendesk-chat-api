import { NativeModules, NativeEventEmitter } from 'react-native';
import { emitters, chatLogTypes, connectionTypes } from './consts';

const { ZendeskChat } = NativeModules;

const ZendeskChatEmitter = new NativeEventEmitter(ZendeskChat);
let chatLogSubscription;
let connectionSubscription;
let timeoutSubscription;

const startChat = async (accountKey, userInfo) => {
  return ZendeskChat.startChat(accountKey, userInfo);
};

const addChatLogObserver = (callback) => {
  ZendeskChat.addChatLogObserver();
  chatLogSubscription = ZendeskChatEmitter.addListener(emitters.CHAT_LOG, (items) => {
    callback(items);
  })
};

const deleteChatLogObserver = () => {
  if (chatLogSubscription) {
    chatLogSubscription.remove();
  }
  ZendeskChat.deleteChatLogObserver();
};

const addChatConnectionObserver = (callback) => {
  ZendeskChat.addChatConnectionObserver();
  connectionSubscription = ZendeskChatEmitter.addListener(emitters.CONNECTION, (connection) => {
    callback(connection);
  })
};

const deleteChatConnectionObserver = () => {
  if (connectionSubscription) {
    connectionSubscription.remove();
  }
  ZendeskChat.deleteChatConnectionObserver();
};

const addChatTimeoutObserver = (callback) => {
  ZendeskChat.addChatTimeoutObserver();
  timeoutSubscription = ZendeskChatEmitter.addListener(emitters.TIMEOUT, (timeout) => {
    callback(timeout);
  })
};

const deleteChatTimeoutObserver = () => {
  if (timeoutSubscription) {
    timeoutSubscription.remove();
  }
};

const sendMessage = (message) => {
  ZendeskChat.sendMessage(message);
};

const sendFile = (path) => {
  ZendeskChat.sendFile(path);
};

export default {
  startChat,
  addChatLogObserver,
  deleteChatLogObserver,
  addChatConnectionObserver,
  deleteChatConnectionObserver,
  addChatTimeoutObserver,
  deleteChatTimeoutObserver,
  sendMessage,
  sendFile,
  chatLogTypes,
  connectionTypes,
};
