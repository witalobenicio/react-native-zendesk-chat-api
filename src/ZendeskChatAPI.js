// @flow

import { NativeModules, NativeEventEmitter, Platform } from 'react-native';
import { emitters, chatLogTypes, connectionTypes } from './consts';

const { ZendeskChat } = NativeModules;

const ZendeskChatEmitter = new NativeEventEmitter(ZendeskChat);
let chatLogSubscription;
let departmentsSubscription;
let connectionSubscription;
let timeoutSubscription;
let agentLeaveSubscription;

type UserInfo = {
  name: string,
  email: string,
  phone: string,
  note: string,
};
type UserConfig = {
  department: string,
  tags: Array<string>,
};

const isChatAvailable = (callback: (boolean) => void) => {
  if (Platform.OS === 'android') {
    ZendeskChat.isChatAvailable(callback);
  } else {
    callback(true);
  }
};

const isOnline = (callback: (boolean) => void) => {
  ZendeskChat.isOnline(callback);
};

const startChat = async (accountKey: string, userInfo: UserInfo, userConfig: UserConfig) => {
  return ZendeskChat.startChat(accountKey, userInfo, userConfig);
};
const endChat = () => {
  ZendeskChat.endChat();
};

const getChatLog = async () => {
  return ZendeskChat.getChatLog();
};

type ChatLog = {
  id: string,
  participantId?: string,
  type: string,
  name: string,
  timestamp: number,
  message?: string,
  attachmentPath?: string,
  attachmentSize?: number,
  attachmentExtension?: number,
  uploadUrl?: number,
  error?: string,
  absolutePath?: string,
  path?: string,
};

const addAgentLeaveObserver = (callback: (Array<ChatLog>) => void) => {
  agentLeaveSubscription = ZendeskChatEmitter.addListener(emitters.AGENT_LEAVE, (leave: string) => {
    callback(leave);
  })
};

const deleteAgentLeaveObserver = (callback: (Array<ChatLog>) => void) => {
  if (agentLeaveSubscription) {
    agentLeaveSubscription.remove();
  }
};

const addChatLogObserver = (callback: (Array<ChatLog>) => void) => {
  ZendeskChat.addChatLogObserver();
  chatLogSubscription = ZendeskChatEmitter.addListener(emitters.CHAT_LOG, (items: Array<ChatLog>) => {
    callback(items);
  })
};

const deleteChatLogObserver = () => {
  if (chatLogSubscription) {
    chatLogSubscription.remove();
  }
  ZendeskChat.deleteChatLogObserver();
};

const addDepartmentsObserver = (callback: ({ isOnline: boolean }) => void) => {
  ZendeskChat.addDepartmentsObserver();
  departmentsSubscription = ZendeskChatEmitter
    .addListener(
      emitters.DEPARTMENTS,
      (response: { status: string, departments: Array<string> }) => {
        callback(response);
      })
};

const deleteDepartmentsObserver = () => {
  if (departmentsSubscription) {
    departmentsSubscription.remove();
  }
  ZendeskChat.deleteDepartmentsObserver();
};

const addChatConnectionObserver = (callback: ({ status: string}) => void) => {
  ZendeskChat.addChatConnectionObserver();
  connectionSubscription = ZendeskChatEmitter.addListener(emitters.CONNECTION, (connection: { status: string }) => {
    callback(connection);
  })
};

const deleteChatConnectionObserver = () => {
  if (connectionSubscription) {
    connectionSubscription.remove();
  }
  ZendeskChat.deleteChatConnectionObserver();
};

const addChatTimeoutObserver = (callback: (boolean) => void) => {
  ZendeskChat.addChatTimeoutObserver();
  timeoutSubscription = ZendeskChatEmitter.addListener(emitters.TIMEOUT, (timeout: boolean) => {
    callback(timeout);
  })
};

const deleteChatTimeoutObserver = () => {
  if (timeoutSubscription) {
    timeoutSubscription.remove();
  }
};

const sendMessage = (message: string) => {
  ZendeskChat.sendMessage(message);
};

const sendFile = (path: string) => {
  ZendeskChat.sendFile(path);
};

export default {
  isChatAvailable,
  isOnline,
  startChat,
  endChat,
  getChatLog,
  addAgentLeaveObserver,
  deleteAgentLeaveObserver,
  addChatLogObserver,
  deleteChatLogObserver,
  addDepartmentsObserver,
  deleteDepartmentsObserver,
  addChatConnectionObserver,
  deleteChatConnectionObserver,
  addChatTimeoutObserver,
  deleteChatTimeoutObserver,
  sendMessage,
  sendFile,
  chatLogTypes,
  connectionTypes,
};
