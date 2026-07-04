const BROADCAST = '广播';
const QUICK_EMOJIS = [
  '😀', '😄', '😂', '😊', '😍', '😘', '😎', '🤔', '😭', '😡',
  '👍', '👏', '🙏', '💪', '👌', '🤝', '❤️', '🎉', '🔥', '✨',
  '🌟', '🎁', '☕', '🍰', '🌹', '🥳', '😋', '🙄', '😴', '😅'
];

const state = {
  connected: false,
  nick: localStorage.getItem('webChatNick') || 'WebUser',
  host: localStorage.getItem('webChatHost') || '127.0.0.1',
  port: localStorage.getItem('webChatPort') || '3500',
  conversations: [],
  messages: {},
  users: [],
  active: BROADCAST,
  unread: {},
  currentRail: 'chat',
};

let events;
let mediaRecorder;
let recordedChunks = [];
let recordTimer;
let recordStartedAt = 0;

const $ = (selector) => document.querySelector(selector);
const conversationList = $('#conversationList');
const messageArea = $('#messageArea');
const messageInput = $('#messageInput');
const sendButton = $('#sendButton');
const statusPill = $('#statusPill');
const emojiPanel = $('#emojiPanel');
const charCount = $('#charCount');
const dropOverlay = $('#dropOverlay');
const toastStack = $('#toastStack');

function boot() {
  $('#hostInput').value = state.host;
  $('#portInput').value = state.port;
  $('#nickInput').value = state.nick;
  buildEmojiPanel();
  bindEvents();
  connectEvents();
  loadState();
}

function bindEvents() {
  document.querySelectorAll('[data-rail]').forEach((button) => {
    button.addEventListener('click', () => {
      state.currentRail = button.dataset.rail || 'chat';
      document.querySelectorAll('[data-rail]').forEach((item) => item.classList.toggle('active', item === button));
      renderConversations();
    });
  });
  $('#connectButton').addEventListener('click', connectServer);
  $('#disconnectButton').addEventListener('click', disconnectServer);
  $('#refreshButton').addEventListener('click', async () => {
    const result = await api('/api/send', { target: BROADCAST, body: '/users' });
    showToast(result.ok ? '已刷新在线列表' : result.error || '刷新失败', result.ok ? '' : 'error');
  });
  $('#sendButton').addEventListener('click', sendMessage);
  $('#clearButton').addEventListener('click', () => {
    messageInput.value = '';
    updateSendButton();
    messageInput.focus();
  });
  $('#emojiButton').addEventListener('click', () => emojiPanel.classList.toggle('hidden'));
  $('#imageButton').addEventListener('click', () => $('#imageInput').click());
  $('#imageHeaderButton').addEventListener('click', () => $('#imageInput').click());
  $('#fileButton').addEventListener('click', () => $('#fileInput').click());
  $('#fileHeaderButton').addEventListener('click', () => $('#fileInput').click());
  $('#voiceButton').addEventListener('click', toggleRecording);
  $('#stopRecordButton').addEventListener('click', stopAndSendRecording);
  $('#cancelRecordButton').addEventListener('click', cancelRecording);
  $('#imageInput').addEventListener('change', (event) => sendPickedFile(event.target.files[0]));
  $('#fileInput').addEventListener('change', (event) => sendPickedFile(event.target.files[0]));
  $('#searchInput').addEventListener('input', renderConversations);
  $('#newChatButton').addEventListener('click', () => $('#searchInput').focus());
  messageInput.addEventListener('input', updateSendButton);
  messageInput.addEventListener('paste', handlePaste);
  messageInput.addEventListener('keydown', (event) => {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      sendMessage();
    }
  });
  document.addEventListener('keydown', handleGlobalShortcuts);
  setupDragAndDrop();
}

async function loadState() {
  const result = await fetch('/api/state').then((response) => response.json());
  applyState(result);
}

function connectEvents() {
  if (events) events.close();
  events = new EventSource('/api/events');
  events.addEventListener('state', (event) => applyState(JSON.parse(event.data)));
  events.addEventListener('users', (event) => {
    const data = JSON.parse(event.data);
    state.users = data.users || [];
    renderAll();
  });
  events.addEventListener('message', (event) => {
    try {
      rememberIncoming(JSON.parse(event.data));
    } catch (error) {
    }
    loadState();
  });
  events.addEventListener('messageStatus', () => loadState());
  events.onerror = () => setStatus('实时连接正在重试', 'error');
}

async function connectServer() {
  const host = $('#hostInput').value.trim() || '127.0.0.1';
  const port = $('#portInput').value.trim() || '3500';
  const nick = $('#nickInput').value.trim() || 'WebUser';
  setStatus('正在连接...', '');
  localStorage.setItem('webChatHost', host);
  localStorage.setItem('webChatPort', port);
  localStorage.setItem('webChatNick', nick);
  const result = await api('/api/connect', { host, port, nick });
  if (!result.ok) {
    setStatus(result.error || '连接失败', 'error');
    showToast(result.error || '连接失败', 'error');
    return;
  }
  applyState(result.data);
  showToast('连接成功');
}

async function disconnectServer() {
  const result = await api('/api/disconnect', {});
  if (result.ok) {
    applyState(result.data);
    showToast('已断开连接', 'warn');
  }
}

async function sendMessage() {
  const body = messageInput.value.trim();
  if (!body || sendButton.disabled) return;
  messageInput.value = '';
  updateSendButton();
  const result = await api('/api/send', { target: state.active, body });
  if (!result.ok) {
    setStatus(result.error || '发送失败', 'error');
    showToast(result.error || '发送失败', 'error');
    messageInput.value = body;
    updateSendButton();
  } else {
    setStatus('已发送', 'online');
  }
}

async function sendPickedFile(file) {
  $('#imageInput').value = '';
  $('#fileInput').value = '';
  if (!file) return;
  if (!canSendAttachment()) return;
  if (file.size > 5 * 1024 * 1024) {
    setStatus('文件超过 5MB 限制', 'error');
    showToast('文件超过 5MB 限制', 'error');
    return;
  }
  const dataBase64 = await fileToBase64(file);
  const result = await api('/api/file', {
    target: state.active,
    fileName: file.name,
    mime: file.type || 'application/octet-stream',
    dataBase64,
  });
  if (!result.ok) {
    setStatus(result.error || '文件发送失败', 'error');
    showToast(result.error || '文件发送失败', 'error');
  } else {
    showToast(`${file.name} 已发送`);
  }
}

async function toggleRecording() {
  if (mediaRecorder && mediaRecorder.state === 'recording') {
    stopAndSendRecording();
    return;
  }
  if (!canSendAttachment()) return;
  if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia || !window.MediaRecorder) {
    setStatus('当前浏览器不支持录音', 'error');
    showToast('当前浏览器不支持录音', 'error');
    return;
  }
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    recordedChunks = [];
    mediaRecorder = new MediaRecorder(stream);
    mediaRecorder.ondataavailable = (event) => {
      if (event.data && event.data.size > 0) recordedChunks.push(event.data);
    };
    mediaRecorder.onstop = () => stream.getTracks().forEach((track) => track.stop());
    mediaRecorder.start();
    recordStartedAt = Date.now();
    $('#recordPopover').classList.remove('hidden');
    recordTimer = setInterval(updateRecordSeconds, 250);
    updateRecordSeconds();
  } catch (error) {
    setStatus('无法使用麦克风：' + error.message, 'error');
    showToast('无法使用麦克风：' + error.message, 'error');
  }
}

function stopAndSendRecording() {
  if (!mediaRecorder || mediaRecorder.state !== 'recording') return;
  mediaRecorder.onstop = async () => {
    clearInterval(recordTimer);
    $('#recordPopover').classList.add('hidden');
    const blob = new Blob(recordedChunks, { type: mediaRecorder.mimeType || 'audio/webm' });
    if (blob.size < 100) {
      setStatus('录音太短，未发送', 'error');
      showToast('录音太短，未发送', 'error');
      return;
    }
    const stamp = new Date().toISOString().replace(/[-:T.]/g, '').slice(0, 14);
    const file = new File([blob], `voice_${stamp}.webm`, { type: blob.type || 'audio/webm' });
    await sendPickedFile(file);
  };
  mediaRecorder.stop();
}

function cancelRecording() {
  if (mediaRecorder && mediaRecorder.state === 'recording') {
    mediaRecorder.onstop = () => {};
    mediaRecorder.stop();
  }
  clearInterval(recordTimer);
  recordedChunks = [];
  $('#recordPopover').classList.add('hidden');
}

function updateRecordSeconds() {
  const seconds = Math.max(0, Math.round((Date.now() - recordStartedAt) / 1000));
  $('#recordSeconds').textContent = `${seconds}s`;
}

function canSendAttachment() {
  if (!state.connected) {
    setStatus('请先连接服务器', 'error');
    showToast('请先连接服务器', 'error');
    return false;
  }
  if (!state.active || state.active === BROADCAST) {
    setStatus('请先选择一个在线私聊对象', 'error');
    showToast('请先选择一个在线私聊对象', 'error');
    return false;
  }
  if (!state.users.includes(state.active)) {
    setStatus('对方离线，文件/语音暂不能发送', 'error');
    showToast('对方离线，文件/语音暂不能发送', 'error');
    return false;
  }
  return true;
}

function setupDragAndDrop() {
  let dragDepth = 0;
  const showDrop = () => dropOverlay.classList.add('visible');
  const hideDrop = () => {
    dragDepth = 0;
    dropOverlay.classList.remove('visible');
  };
  window.addEventListener('dragenter', (event) => {
    if (!hasDraggedFiles(event)) return;
    event.preventDefault();
    dragDepth += 1;
    showDrop();
  });
  window.addEventListener('dragover', (event) => {
    if (!hasDraggedFiles(event)) return;
    event.preventDefault();
  });
  window.addEventListener('dragleave', (event) => {
    if (!hasDraggedFiles(event)) return;
    dragDepth = Math.max(0, dragDepth - 1);
    if (dragDepth === 0) dropOverlay.classList.remove('visible');
  });
  window.addEventListener('drop', (event) => {
    if (!hasDraggedFiles(event)) return;
    event.preventDefault();
    const file = event.dataTransfer.files && event.dataTransfer.files[0];
    hideDrop();
    sendPickedFile(file);
  });
  window.addEventListener('blur', hideDrop);
}

function hasDraggedFiles(event) {
  return event.dataTransfer && [...event.dataTransfer.types].includes('Files');
}

function handlePaste(event) {
  const items = event.clipboardData && [...event.clipboardData.items];
  if (!items) return;
  const fileItem = items.find((item) => item.kind === 'file');
  if (!fileItem) return;
  const file = fileItem.getAsFile();
  if (!file) return;
  event.preventDefault();
  sendPickedFile(file);
}

function handleGlobalShortcuts(event) {
  if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
    event.preventDefault();
    $('#searchInput').focus();
    $('#searchInput').select();
  }
  if (event.key === 'Escape') {
    emojiPanel.classList.add('hidden');
    dropOverlay.classList.remove('visible');
  }
}

function rememberIncoming(message) {
  if (!message || !message.conversation || message.conversation === state.active) return;
  if (message.direction !== 'incoming') return;
  state.unread[message.conversation] = Math.min(99, (state.unread[message.conversation] || 0) + 1);
  showToast(`${message.sender || '新消息'}：${messagePreview(message)}`);
}

function applyState(next) {
  if (!next) return;
  state.connected = Boolean(next.connected);
  state.nick = next.nick || state.nick;
  state.host = next.host || state.host;
  state.port = String(next.port || state.port);
  state.users = next.users || [];
  state.conversations = next.conversations || [];
  state.messages = next.messages || {};
  if (!state.conversations.some((item) => item.id === state.active)) state.active = BROADCAST;
  state.unread[state.active] = 0;
  renderAll();
}

function renderAll() {
  $('#railAvatar').textContent = avatarText(state.nick);
  $('#connectionCard').classList.toggle('compact', state.connected);
  $('#connectButton').textContent = state.connected ? '重新连接' : '连接';
  $('#hostInput').value = state.host;
  $('#portInput').value = state.port;
  $('#nickInput').value = state.nick;
  $('#connectionSummary').textContent = state.connected
    ? `${state.host}:${state.port} · ${state.nick}`
    : '尚未连接';
  $('#connectionDot').className = `connection-dot ${state.connected ? 'online' : ''}`;
  setStatus(state.connected ? `已连接 · ${state.nick}` : '未连接', state.connected ? 'online' : '');
  renderConversations();
  renderMessages();
  updateHeader();
  updateSendButton();
}

function renderConversations() {
  const keyword = $('#searchInput').value.trim().toLowerCase();
  const conversations = normalizedConversations().filter((item) => {
    if (state.currentRail === 'contacts' && item.id === BROADCAST) return false;
    if (state.currentRail === 'files' && !hasFileMessages(item.id)) return false;
    return !keyword || item.title.toLowerCase().includes(keyword) || (item.preview || '').toLowerCase().includes(keyword);
  });
  conversationList.innerHTML = '';
  if (!conversations.length) {
    const empty = document.createElement('div');
    empty.className = 'conversation-empty';
    empty.textContent = conversationEmptyText();
    conversationList.appendChild(empty);
    return;
  }
  conversations.forEach((item) => {
    const row = document.createElement('button');
    row.className = `conversation-item ${item.id === state.active ? 'active' : ''}`;
    row.addEventListener('click', () => {
      state.active = item.id;
      state.unread[item.id] = 0;
      renderAll();
      messageInput.focus();
    });
    const unread = Math.min(99, state.unread[item.id] || 0);
    row.innerHTML = `
      <div class="avatar ${item.id === BROADCAST ? 'broadcast' : ''} ${item.online ? 'online' : ''}">${escapeHtml(avatarText(item.title))}</div>
      <div class="conversation-main">
        <div class="conversation-title">
          <strong>${escapeHtml(item.title)}</strong>
          <span class="conversation-time">${escapeHtml(compactTime(item.time))}</span>
        </div>
        <div class="conversation-preview">${escapeHtml(item.preview || '')}</div>
      </div>
      <div class="conversation-side">
        <span class="unread-badge ${unread ? '' : 'hidden'}">${unread > 99 ? '99+' : unread}</span>
      </div>
    `;
    conversationList.appendChild(row);
  });
}

function normalizedConversations() {
  const byId = new Map();
  (state.conversations || []).forEach((item) => byId.set(item.id, item));
  byId.set(BROADCAST, byId.get(BROADCAST) || {
    id: BROADCAST,
    title: BROADCAST,
    online: true,
    preview: `全服务器广播 · ${state.users.length} 人在线`,
  });
  state.users.forEach((user) => {
    if (user.toLowerCase() === state.nick.toLowerCase()) return;
    if (!byId.has(user)) {
      byId.set(user, { id: user, title: user, online: true, preview: '在线' });
    }
  });
  return [...byId.values()];
}

function hasFileMessages(conversationId) {
  const messages = state.messages[conversationId] || [];
  return messages.some((message) => message.kind === 'file');
}

function conversationEmptyText() {
  if (state.currentRail === 'contacts') return '还没有可显示的联系人';
  if (state.currentRail === 'files') return '当前没有文件或图片消息';
  return '没有匹配的会话';
}

function renderMessages() {
  const messages = state.messages[state.active] || [];
  messageArea.innerHTML = '';
  if (!messages.length) {
    const empty = document.createElement('div');
    empty.className = 'empty-state';
    empty.textContent = state.connected ? '还没有消息' : '连接服务器后开始聊天';
    messageArea.appendChild(empty);
    return;
  }
  messages.forEach((message) => messageArea.appendChild(renderMessage(message)));
  messageArea.scrollTop = messageArea.scrollHeight;
}

function renderMessage(message) {
  const row = document.createElement('div');
  row.className = `message-row ${message.direction || ''}`;
  if (message.kind === 'system') {
    row.className = 'message-row system';
    row.innerHTML = `<div class="bubble">${escapeHtml(message.body || '')}</div>`;
    return row;
  }

  const avatar = `<div class="avatar">${escapeHtml(avatarText(message.sender || ''))}</div>`;
  const content = `
    <div class="message-stack">
      <div class="message-meta">${escapeHtml(message.sender || '')} · ${escapeHtml(message.time || '')}</div>
      <div class="bubble">${renderBubbleContent(message)}</div>
      ${message.status ? `<div class="message-status ${message.status === '已读' ? 'read' : ''}">${escapeHtml(message.status)}</div>` : ''}
    </div>
  `;
  row.innerHTML = message.direction === 'outgoing' ? content + avatar : avatar + content;
  return row;
}

function renderBubbleContent(message) {
  if (message.kind !== 'file') return escapeHtml(message.body || '');
  const url = `data:${message.mime || 'application/octet-stream'};base64,${message.dataBase64 || ''}`;
  if ((message.mime || '').startsWith('image/')) {
    return `<a href="${url}" download="${escapeAttr(message.fileName)}"><img class="image-message" src="${url}" alt="${escapeAttr(message.fileName)}"></a>`;
  }
  if ((message.mime || '').startsWith('audio/')) {
    return `
      <audio class="audio-message" controls src="${url}"></audio>
      <div class="file-meta">${escapeHtml(message.fileName || '语音消息')} · ${formatBytes(message.fileSize || 0)}</div>
    `;
  }
  return `
    <a class="file-card" href="${url}" download="${escapeAttr(message.fileName)}">
      <span class="file-icon">${fileLabel(message.fileName)}</span>
      <span>
        <span class="file-name">${escapeHtml(message.fileName || '文件')}</span>
        <span class="file-meta">${formatBytes(message.fileSize || 0)} · 点击下载</span>
      </span>
    </a>
  `;
}

function updateHeader() {
  const active = normalizedConversations().find((item) => item.id === state.active);
  $('#chatTitle').textContent = active ? active.title : BROADCAST;
  $('#chatAvatar').textContent = avatarText(active ? active.title : BROADCAST);
  $('#chatAvatar').classList.toggle('offline', Boolean(active && !active.online && active.id !== BROADCAST));
  if (!state.connected) {
    $('#chatSubtitle').textContent = '连接后开始聊天';
  } else if (state.active === BROADCAST) {
    $('#chatSubtitle').textContent = `广播消息将发送给所有在线用户 · ${state.users.length} 人在线`;
  } else if (state.users.includes(state.active)) {
    $('#chatSubtitle').textContent = `在线 · 私聊消息只发送给 ${state.active}`;
  } else {
    $('#chatSubtitle').textContent = '离线 · 文字消息会待发送，文件暂不可发送';
  }
}

function updateSendButton() {
  const length = messageInput.value.length;
  charCount.textContent = `${length}/500`;
  charCount.classList.toggle('warning', length > 450);
  sendButton.disabled = !state.connected || messageInput.value.trim().length === 0;
  if (!state.connected) {
    $('#hintText').textContent = '请先连接服务器';
  } else if (state.active === BROADCAST) {
    $('#hintText').textContent = '当前为广播，会发送给所有在线用户';
  } else if (state.users.includes(state.active)) {
    $('#hintText').textContent = '支持拖拽、粘贴图片或录音发送';
  } else {
    $('#hintText').textContent = '对方离线：文字待发送，文件暂不可发送';
  }
}

function setStatus(text, mode) {
  statusPill.textContent = text;
  statusPill.className = `status-pill ${mode || ''}`;
}

function showToast(text, mode = '') {
  if (!text) return;
  const toast = document.createElement('div');
  toast.className = `toast ${mode || ''}`;
  toast.textContent = text;
  toastStack.appendChild(toast);
  window.setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(-8px)';
    toast.style.transition = 'opacity .18s ease, transform .18s ease';
    window.setTimeout(() => toast.remove(), 220);
  }, 2600);
}

function messagePreview(message) {
  if (!message) return '新消息';
  if (message.kind === 'file') return `[${fileLabel(message.fileName)}] ${message.fileName || '文件'}`;
  return String(message.body || '新消息').replace(/\s+/g, ' ').slice(0, 36);
}

function buildEmojiPanel() {
  emojiPanel.innerHTML = '';
  QUICK_EMOJIS.forEach((emoji) => {
    const button = document.createElement('button');
    button.type = 'button';
    button.textContent = emoji;
    button.addEventListener('click', () => {
      insertAtCursor(messageInput, emoji);
      emojiPanel.classList.add('hidden');
      updateSendButton();
    });
    emojiPanel.appendChild(button);
  });
}

async function api(path, body) {
  try {
    const response = await fetch(path, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });
    return response.json();
  } catch (error) {
    return { ok: false, error: error.message };
  }
}

function fileToBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(String(reader.result).split(',')[1] || '');
    reader.onerror = () => reject(reader.error);
    reader.readAsDataURL(file);
  });
}

function insertAtCursor(input, text) {
  const start = input.selectionStart || 0;
  const end = input.selectionEnd || 0;
  input.value = input.value.slice(0, start) + text + input.value.slice(end);
  input.selectionStart = input.selectionEnd = start + text.length;
  input.focus();
}

function avatarText(name) {
  return String(name || '?').trim().slice(0, 1).toUpperCase() || '?';
}

function compactTime(time) {
  return String(time || '').slice(0, 5);
}

function fileLabel(name) {
  const lower = String(name || '').toLowerCase();
  if (/\.(png|jpg|jpeg|gif|webp|bmp)$/.test(lower)) return '图';
  if (/\.(wav|mp3|webm|ogg|m4a|aac|flac)$/.test(lower)) return '音';
  if (/\.(mp4|mov|avi|mkv|webm)$/.test(lower)) return '视';
  return '文';
}

function formatBytes(bytes) {
  const size = Number(bytes) || 0;
  if (size < 1024) return `${size} B`;
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
  return `${(size / 1024 / 1024).toFixed(1)} MB`;
}

function escapeHtml(value) {
  return String(value ?? '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');
}

function escapeAttr(value) {
  return escapeHtml(value).replaceAll('`', '&#96;');
}

boot();
