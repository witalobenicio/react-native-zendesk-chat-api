export const emitters = {
  CONNECTION: 'onConnectionUpdate',
  CHAT_LOG: 'onChatLogUpdate',
  TIMEOUT: 'onTimeoutReceived',
  DEPARTMENTS: 'onDepartmentsUpdate',
};

export const connectionTypes = {
  CONNECTING: 'CONNECTING',
  CLOSED: 'CLOSED',
  CONNECTED: 'CONNECTED',
  DISCONNECTED: 'DISCONNECTED',
  NO_CONNECTION: 'NO_CONNECTION',
  UNITIALIZED: 'UNITIALIZED',
  UNKNOWN: 'UNKNOWN',
};

const chatTypes = {
  AGENT_MESSAGE: 'AGENT_MESSAGE',
  AGENT_ATTACHMENT: 'AGENT_ATTACHMENT',
  VISITOR_MESSAGE: 'VISITOR_MESSAGE',
  VISITOR_ATTACHMENT: 'VISITOR_ATTACHMENT',
};

const chatTypesAsArray = () => {
  return [
    chatTypes.AGENT_ATTACHMENT,
    chatTypes.AGENT_MESSAGE,
    chatTypes.VISITOR_ATTACHMENT,
    chatTypes.VISITOR_MESSAGE,
  ]
};

export const chatLogTypes = {
  ...chatTypes,
  asArray: chatTypesAsArray,
};
