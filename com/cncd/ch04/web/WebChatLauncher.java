package com.cncd.ch04.web;

import com.cncd.ch04.client.ClientKernel;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

public class WebChatLauncher {
    private static final String BROADCAST_CHAT = "广播";
    private static final String USERS_PREFIX = "__USERS__|";
    private static final String FILE_PREFIX = "__FILE__|";
    private static final String SERVER_MOMENT_ITEM_PREFIX = "__SERVER_MOMENT__|";
    private static final String PRIVATE_MESSAGE_PREFIX = "__CHAT_MSG__|";
    private static final String READ_RECEIPT_PREFIX = "__READ__|";
    private static final String VIDEO_CALL_PREFIX = "__VIDEO_CALL__|";
    private static final long MAX_FILE_BYTES = 5L * 1024L * 1024L;
    private static final Path WEB_ROOT = Paths.get("web").toAbsolutePath().normalize();

    public static void main(String[] args) throws Exception {
        int bridgePort = 8088;
        boolean noOpen = false;
        for(int i=0;i<args.length;i++) {
            if("--no-open".equalsIgnoreCase(args[i])) {
                noOpen = true;
            } else {
                bridgePort = parseInt(args[i], bridgePort);
            }
        }
        WebChatService chatService = new WebChatService();
        HttpServer server = null;
        int startPort = bridgePort;
        while(server == null) {
            try {
                server = HttpServer.create(new InetSocketAddress("127.0.0.1", bridgePort), 0);
            } catch(BindException e) {
                String existingUrl = "http://127.0.0.1:" + bridgePort + "/";
                if(isExistingBridge(bridgePort)) {
                    System.out.println("Web Chat client is already running at " + existingUrl);
                    if(!noOpen) openBrowser(existingUrl);
                    return;
                }
                bridgePort++;
                if(bridgePort > startPort + 10) throw e;
            }
        }
        server.setExecutor(Executors.newCachedThreadPool());
        server.createContext("/api/connect", new JsonActionHandler(chatService) {
            protected ApiResult handle(Map<String, String> json) {
                return service.connect(
                        value(json, "host", "127.0.0.1"),
                        parseInt(value(json, "port", "3500"), 3500),
                        value(json, "nick", "WebUser"));
            }
        });
        server.createContext("/api/disconnect", new JsonActionHandler(chatService) {
            protected ApiResult handle(Map<String, String> json) {
                return service.disconnect();
            }
        });
        server.createContext("/api/send", new JsonActionHandler(chatService) {
            protected ApiResult handle(Map<String, String> json) {
                return service.sendText(value(json, "target", BROADCAST_CHAT),
                        value(json, "body", ""));
            }
        });
        server.createContext("/api/file", new JsonActionHandler(chatService) {
            protected ApiResult handle(Map<String, String> json) {
                return service.sendFile(value(json, "target", ""),
                        value(json, "fileName", "file.bin"),
                        value(json, "mime", "application/octet-stream"),
                        value(json, "dataBase64", ""));
            }
        });
        server.createContext("/api/video", new JsonActionHandler(chatService) {
            protected ApiResult handle(Map<String, String> json) {
                return service.sendVideoSignal(value(json, "target", ""),
                        value(json, "action", ""),
                        value(json, "payload", ""));
            }
        });
        server.createContext("/api/state", new StateHandler(chatService));
        server.createContext("/api/events", new EventsHandler(chatService));
        server.createContext("/", new StaticHandler());
        server.start();

        String url = "http://127.0.0.1:" + bridgePort + "/";
        System.out.println("Web Chat client is running at " + url);
        System.out.println("Keep this Java process alive while using the browser UI.");
        if(!noOpen) openBrowser(url);
    }

    private static void openBrowser(String url) {
        try {
            if(Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch(Exception e) {
            System.out.println("Open the URL manually: " + url);
        }
    }

    private static boolean isExistingBridge(int port) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL("http://127.0.0.1:" + port + "/api/state");
            connection = (HttpURLConnection)url.openConnection();
            connection.setConnectTimeout(500);
            connection.setReadTimeout(500);
            connection.setRequestMethod("GET");
            return connection.getResponseCode() == 200;
        } catch(Exception e) {
            return false;
        } finally {
            if(connection != null) connection.disconnect();
        }
    }

    private abstract static class JsonActionHandler implements HttpHandler {
        protected final WebChatService service;

        JsonActionHandler(WebChatService service) {
            this.service = service;
        }

        public void handle(HttpExchange exchange) throws IOException {
            if(!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"ok\":false,\"error\":\"Method not allowed\"}");
                return;
            }
            ApiResult result;
            try {
                result = handle(parseJsonObject(readBody(exchange)));
            } catch(Exception e) {
                result = ApiResult.error(e.getMessage());
            }
            sendJson(exchange, result.ok ? 200 : 400, result.toJson());
        }

        protected abstract ApiResult handle(Map<String, String> json);
    }

    private static class StateHandler implements HttpHandler {
        private final WebChatService service;

        StateHandler(WebChatService service) {
            this.service = service;
        }

        public void handle(HttpExchange exchange) throws IOException {
            sendJson(exchange, 200, service.stateJson());
        }
    }

    private static class EventsHandler implements HttpHandler {
        private final WebChatService service;

        EventsHandler(WebChatService service) {
            this.service = service;
        }

        public void handle(HttpExchange exchange) throws IOException {
            Headers headers = exchange.getResponseHeaders();
            headers.set("Content-Type", "text/event-stream; charset=utf-8");
            headers.set("Cache-Control", "no-cache");
            headers.set("Connection", "keep-alive");
            exchange.sendResponseHeaders(200, 0);
            SseClient client = new SseClient(exchange.getResponseBody());
            service.addClient(client);
            try {
                client.send("state", service.stateJson());
                while(client.isOpen()) {
                    sleep(25000);
                    client.comment("ping");
                }
            } finally {
                service.removeClient(client);
                exchange.close();
            }
        }
    }

    private static class StaticHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String rawPath = exchange.getRequestURI().getPath();
            String relative = rawPath == null || rawPath.equals("/") ? "index.html" : rawPath.substring(1);
            Path file = WEB_ROOT.resolve(relative).normalize();
            if(!file.startsWith(WEB_ROOT) || Files.isDirectory(file) || !Files.exists(file)) {
                sendText(exchange, 404, "Not found", "text/plain; charset=utf-8");
                return;
            }
            byte[] bytes = Files.readAllBytes(file);
            Headers headers = exchange.getResponseHeaders();
            headers.set("Content-Type", mimeType(file));
            exchange.sendResponseHeaders(200, bytes.length);
            try(OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        }
    }

    private static class WebChatService implements ClientKernel.MessageListener {
        private final CopyOnWriteArrayList<SseClient> clients = new CopyOnWriteArrayList<SseClient>();
        private final Map<String, List<WebMessage>> conversations =
                new ConcurrentHashMap<String, List<WebMessage>>();
        private final Map<String, WebMessage> trackedOutgoing =
                new ConcurrentHashMap<String, WebMessage>();
        private final Map<String, PendingMessage> pendingPrivate =
                new LinkedHashMap<String, PendingMessage>();
        private final LinkedList<String> localBroadcastEchoes = new LinkedList<String>();
        private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
        private volatile Set<String> users = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
        private volatile ClientKernel kernel;
        private volatile String host = "127.0.0.1";
        private volatile int port = 3500;
        private volatile String nick = "WebUser";
        private volatile boolean connected = false;

        WebChatService() {
            ensureConversation(BROADCAST_CHAT);
        }

        synchronized ApiResult connect(String host, int port, String nick) {
            disconnect();
            this.host = empty(host) ? "127.0.0.1" : host.trim();
            this.port = port <= 0 ? 3500 : port;
            this.nick = empty(nick) ? "WebUser" : nick.trim();
            addSystemMessage(BROADCAST_CHAT, "正在连接 " + this.host + ":" + this.port + " ...");
            ClientKernel next = new ClientKernel(this.host, this.port);
            if(!next.isConnected()) {
                connected = false;
                String detail = next.getLastErrorMessage();
                addSystemMessage(BROADCAST_CHAT, "连接失败：" +
                        (empty(detail) ? "请确认服务端已启动，防火墙已放行端口。" : detail));
                broadcast("state", stateJson());
                return ApiResult.error("连接失败：" + detail);
            }
            kernel = next;
            kernel.addMessageListener(this);
            kernel.setNick(this.nick);
            connected = true;
            addSystemMessage(BROADCAST_CHAT, "已连接 " + this.host + ":" + this.port);
            kernel.sendMessage("/users");
            broadcast("state", stateJson());
            return ApiResult.ok(stateJson());
        }

        synchronized ApiResult disconnect() {
            if(kernel != null) {
                kernel.removeMessageListener(this);
                kernel.dropMe();
            }
            kernel = null;
            connected = false;
            users.clear();
            broadcast("state", stateJson());
            return ApiResult.ok("{\"connected\":false}");
        }

        synchronized ApiResult sendText(String target, String body) {
            if(!isReady()) return ApiResult.error("请先连接服务器");
            body = body == null ? "" : body.trim();
            if(body.length() == 0) return ApiResult.error("消息不能为空");
            target = empty(target) ? BROADCAST_CHAT : target.trim();
            if(BROADCAST_CHAT.equals(target)) {
                if(body.startsWith("/")) {
                    kernel.sendMessage(body);
                    return ApiResult.ok("{\"command\":true}");
                }
                rememberLocalBroadcastEcho(body);
                kernel.sendMessage(body);
                WebMessage message = WebMessage.text(newId(), BROADCAST_CHAT, nick, body,
                        "outgoing", now(), "");
                addMessage(message);
                return ApiResult.ok(message.toJson());
            }
            String id = newId();
            boolean online = users.contains(target);
            WebMessage message = WebMessage.text(id, target, "我", body,
                    "outgoing", now(), online ? "未读" : "待发送");
            trackedOutgoing.put(id, message);
            addMessage(message);
            if(online) {
                sendTrackedPrivate(target, id, body);
            } else {
                pendingPrivate.put(id, new PendingMessage(id, target, body));
            }
            return ApiResult.ok(message.toJson());
        }

        synchronized ApiResult sendFile(String target, String fileName, String mime, String dataBase64) {
            if(!isReady()) return ApiResult.error("请先连接服务器");
            if(empty(target) || BROADCAST_CHAT.equals(target)) return ApiResult.error("文件需要选择一个私聊对象");
            if(!users.contains(target)) return ApiResult.error("对方当前离线，文件暂不支持离线发送");
            if(empty(dataBase64)) return ApiResult.error("文件内容为空");
            dataBase64 = stripDataUrl(dataBase64);
            byte[] bytes;
            try {
                bytes = Base64.getDecoder().decode(dataBase64);
            } catch(Exception e) {
                return ApiResult.error("文件编码无效");
            }
            if(bytes.length > MAX_FILE_BYTES) {
                return ApiResult.error("文件超过 5MB 限制");
            }
            String safeName = safeFileName(fileName);
            kernel.sendMessage("/file " + target + " " + safeName + " " + dataBase64);
            WebMessage message = WebMessage.file(newId(), target, "我", safeName,
                    "outgoing", now(), "已发送", mime, bytes.length, dataBase64);
            addMessage(message);
            return ApiResult.ok(message.toJson());
        }

        synchronized ApiResult sendVideoSignal(String target, String action, String payload) {
            if(!isReady()) return ApiResult.error("请先连接服务器");
            target = target == null ? "" : target.trim();
            action = action == null ? "" : action.trim();
            payload = payload == null ? "" : payload;
            if(empty(target) || BROADCAST_CHAT.equals(target)) return ApiResult.error("视频通话需要选择在线私聊对象");
            if(!users.contains(target)) return ApiResult.error("对方当前离线，无法视频通话");
            if(empty(action)) return ApiResult.error("视频通话指令为空");
            kernel.sendMessage("/msg " + target + " " + VIDEO_CALL_PREFIX
                    + action + (payload.length() == 0 ? "" : "|" + payload));
            return ApiResult.ok("{\"sent\":true}");
        }

        public void onKernelMessage(String raw) {
            if(raw == null) return;
            if(raw.startsWith(USERS_PREFIX)) {
                updateUsers(raw.substring(USERS_PREFIX.length()));
            } else if(raw.startsWith(FILE_PREFIX)) {
                String[] parts = raw.split("\\|", 4);
                if(parts.length == 4) receiveFile(parts[1], parts[2], parts[3]);
            } else if(raw.startsWith(SERVER_MOMENT_ITEM_PREFIX)) {
                addSystemMessage(BROADCAST_CHAT, "已同步一条朋友圈动态。");
            } else {
                parseDisplayMessage(raw);
            }
        }

        private void updateUsers(String csv) {
            Set<String> next = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
            String[] parts = csv.split(",");
            for(int i=0;i<parts.length;i++) {
                String user = parts[i].trim();
                if(user.length() > 0) {
                    next.add(user);
                    if(!user.equalsIgnoreCase(nick)) ensureConversation(user);
                }
            }
            users = next;
            flushPendingPrivateMessages();
            broadcast("users", usersJson());
            broadcast("state", stateJson());
        }

        private void receiveFile(String sender, String fileName, String base64) {
            ensureConversation(sender);
            String cleanName = safeFileName(fileName);
            int size = 0;
            try {
                size = Base64.getDecoder().decode(base64).length;
            } catch(Exception e) {
            }
            WebMessage message = WebMessage.file(newId(), sender, sender, cleanName,
                    "incoming", now(), "点击下载", mimeFromName(cleanName), size, base64);
            addMessage(message);
        }

        private void parseDisplayMessage(String raw) {
            String text = stripHtml(raw).replace('\u00A0', ' ').trim();
            if(text.length() == 0 || text.startsWith("[private sent to ")) return;

            if(text.startsWith("Unable to find user ")) {
                String target = text.substring("Unable to find user ".length()).trim();
                markLatestPrivateAsQueued(target);
                addSystemMessage(BROADCAST_CHAT, target + " 当前离线，消息已转为待发送。");
                return;
            }

            if(text.startsWith("[private] ")) {
                String payload = text.substring("[private] ".length()).trim();
                int colon = payload.indexOf(':');
                if(colon > 0) {
                    String sender = payload.substring(0, colon).trim();
                    String body = payload.substring(colon + 1).trim();
                    if(handleProtocolMessage(sender, body)) return;
                    WebMessage message = WebMessage.text(newId(), sender, sender, body,
                            sender.equalsIgnoreCase(nick) ? "outgoing" : "incoming", now(), "");
                    addMessage(message);
                    return;
                }
            }

            if(isSystemMessage(text)) {
                addSystemMessage(BROADCAST_CHAT, text);
                return;
            }

            int colon = text.indexOf(':');
            if(colon > 0) {
                String sender = text.substring(0, colon).trim();
                String body = text.substring(colon + 1).trim();
                if(sender.equalsIgnoreCase(nick) && consumeLocalBroadcastEcho(body)) return;
                WebMessage message = WebMessage.text(newId(), BROADCAST_CHAT, sender, body,
                        sender.equalsIgnoreCase(nick) ? "outgoing" : "incoming", now(), "");
                addMessage(message);
                return;
            }

            addSystemMessage(BROADCAST_CHAT, text);
        }

        private boolean handleProtocolMessage(String sender, String body) {
            if(body.startsWith(PRIVATE_MESSAGE_PREFIX)) {
                String payload = body.substring(PRIVATE_MESSAGE_PREFIX.length());
                String[] parts = payload.split("\\|", 2);
                if(parts.length == 2) {
                    String messageId = parts[0];
                    String message = decodeToken(parts[1]);
                    WebMessage webMessage = WebMessage.text(messageId, sender, sender, message,
                            "incoming", now(), "");
                    addMessage(webMessage);
                    if(isReady()) {
                        kernel.sendMessage("/msg " + sender + " " + READ_RECEIPT_PREFIX + messageId);
                    }
                }
                return true;
            }
            if(body.startsWith(READ_RECEIPT_PREFIX)) {
                String messageId = body.substring(READ_RECEIPT_PREFIX.length()).trim();
                WebMessage message = trackedOutgoing.get(messageId);
                if(message != null) {
                    message.status = "已读";
                    broadcast("messageStatus", message.toJson());
                    broadcast("state", stateJson());
                }
                return true;
            }
            if(body.startsWith(VIDEO_CALL_PREFIX)) {
                String payload = body.substring(VIDEO_CALL_PREFIX.length()).trim();
                String action = payload;
                String data = "";
                int split = payload.indexOf('|');
                if(split >= 0) {
                    action = payload.substring(0, split);
                    data = payload.substring(split + 1);
                }
                broadcast("video", videoEventJson(sender, action, data));
                return true;
            }
            return false;
        }

        private void flushPendingPrivateMessages() {
            Iterator<PendingMessage> it = new ArrayList<PendingMessage>(pendingPrivate.values()).iterator();
            while(it.hasNext()) {
                PendingMessage pending = it.next();
                if(users.contains(pending.target)) {
                    sendTrackedPrivate(pending.target, pending.id, pending.body);
                    WebMessage message = trackedOutgoing.get(pending.id);
                    if(message != null) message.status = "未读";
                    pendingPrivate.remove(pending.id);
                }
            }
        }

        private void sendTrackedPrivate(String target, String id, String body) {
            kernel.sendMessage("/msg " + target + " " + PRIVATE_MESSAGE_PREFIX
                    + id + "|" + encodeToken(body));
        }

        private String videoEventJson(String sender, String action, String payload) {
            StringBuilder out = new StringBuilder();
            out.append('{');
            appendField(out, "sender", sender);
            out.append(',');
            appendField(out, "action", action);
            out.append(',');
            appendField(out, "payload", payload);
            out.append('}');
            return out.toString();
        }

        private void markLatestPrivateAsQueued(String target) {
            List<WebMessage> list = conversations.get(target);
            if(list == null) return;
            synchronized(list) {
                for(int i=list.size() - 1;i>=0;i--) {
                    WebMessage message = list.get(i);
                    if("outgoing".equals(message.direction) && "text".equals(message.kind)) {
                        message.status = "待发送";
                        pendingPrivate.put(message.id, new PendingMessage(message.id, target, message.body));
                        broadcast("messageStatus", message.toJson());
                        return;
                    }
                }
            }
        }

        private void addSystemMessage(String conversation, String body) {
            addMessage(WebMessage.system(newId(), conversation, body, now()));
        }

        private void addMessage(WebMessage message) {
            ensureConversation(message.conversation);
            List<WebMessage> list = conversations.get(message.conversation);
            synchronized(list) {
                list.add(message);
                while(list.size() > 300) list.remove(0);
            }
            broadcast("message", message.toJson());
            broadcast("state", stateJson());
        }

        private void ensureConversation(String key) {
            if(empty(key)) key = BROADCAST_CHAT;
            if(!conversations.containsKey(key)) {
                conversations.put(key, Collections.synchronizedList(new ArrayList<WebMessage>()));
            }
        }

        private boolean isReady() {
            return connected && kernel != null && kernel.isConnected();
        }

        private String now() {
            return LocalDateTime.now().format(timeFormat);
        }

        private String newId() {
            return UUID.randomUUID().toString().replace("-", "");
        }

        void addClient(SseClient client) {
            clients.add(client);
        }

        void removeClient(SseClient client) {
            clients.remove(client);
        }

        void broadcast(String type, String json) {
            Iterator<SseClient> it = clients.iterator();
            while(it.hasNext()) {
                SseClient client = it.next();
                if(!client.send(type, json)) clients.remove(client);
            }
        }

        String stateJson() {
            StringBuilder out = new StringBuilder();
            out.append('{');
            appendField(out, "connected", isReady());
            out.append(',');
            appendField(out, "host", host);
            out.append(',');
            appendField(out, "port", port);
            out.append(',');
            appendField(out, "nick", nick);
            out.append(",\"users\":").append(usersArrayJson());
            out.append(",\"conversations\":").append(conversationsJson());
            out.append(",\"messages\":").append(messagesJson());
            out.append('}');
            return out.toString();
        }

        private String usersJson() {
            return "{\"users\":" + usersArrayJson() + "}";
        }

        private String usersArrayJson() {
            List<String> sorted = new ArrayList<String>(users);
            Collections.sort(sorted, String.CASE_INSENSITIVE_ORDER);
            StringBuilder out = new StringBuilder("[");
            for(int i=0;i<sorted.size();i++) {
                if(i > 0) out.append(',');
                appendString(out, sorted.get(i));
            }
            out.append(']');
            return out.toString();
        }

        private String conversationsJson() {
            List<String> keys = new ArrayList<String>(conversations.keySet());
            if(!keys.contains(BROADCAST_CHAT)) keys.add(BROADCAST_CHAT);
            Collections.sort(keys, new Comparator<String>() {
                public int compare(String a, String b) {
                    if(BROADCAST_CHAT.equals(a)) return -1;
                    if(BROADCAST_CHAT.equals(b)) return 1;
                    long ta = latestSequence(a);
                    long tb = latestSequence(b);
                    if(ta != tb) return ta > tb ? -1 : 1;
                    return a.compareToIgnoreCase(b);
                }
            });
            StringBuilder out = new StringBuilder("[");
            for(int i=0;i<keys.size();i++) {
                String key = keys.get(i);
                if(i > 0) out.append(',');
                WebMessage latest = latestMessage(key);
                out.append('{');
                appendField(out, "id", key);
                out.append(',');
                appendField(out, "title", key);
                out.append(',');
                appendField(out, "online", BROADCAST_CHAT.equals(key) || users.contains(key));
                out.append(',');
                appendField(out, "time", latest == null ? "" : latest.time);
                out.append(',');
                appendField(out, "preview", latest == null ? previewFor(key) : latest.preview());
                out.append('}');
            }
            out.append(']');
            return out.toString();
        }

        private String messagesJson() {
            StringBuilder out = new StringBuilder("{");
            List<String> keys = new ArrayList<String>(conversations.keySet());
            Collections.sort(keys, String.CASE_INSENSITIVE_ORDER);
            for(int i=0;i<keys.size();i++) {
                if(i > 0) out.append(',');
                String key = keys.get(i);
                appendString(out, key);
                out.append(':');
                List<WebMessage> list = conversations.get(key);
                out.append('[');
                synchronized(list) {
                    for(int j=0;j<list.size();j++) {
                        if(j > 0) out.append(',');
                        out.append(list.get(j).toJson());
                    }
                }
                out.append(']');
            }
            out.append('}');
            return out.toString();
        }

        private WebMessage latestMessage(String key) {
            List<WebMessage> list = conversations.get(key);
            if(list == null || list.size() == 0) return null;
            synchronized(list) {
                return list.size() == 0 ? null : list.get(list.size() - 1);
            }
        }

        private long latestSequence(String key) {
            WebMessage latest = latestMessage(key);
            return latest == null ? 0L : latest.sequence;
        }

        private String previewFor(String key) {
            if(BROADCAST_CHAT.equals(key)) return "全服务器广播 · " + users.size() + " 人在线";
            return users.contains(key) ? "在线" : "离线";
        }

        private boolean isSystemMessage(String text) {
            return text.startsWith("Server:")
                    || text.startsWith("connected!")
                    || text.startsWith("Current Connected Users:")
                    || text.startsWith("Available commands:")
                    || text.startsWith("Unable to find")
                    || text.startsWith("Unknown command")
                    || text.startsWith("Invalid Command")
                    || text.startsWith("usage:")
                    || text.startsWith("Your ")
                    || text.startsWith("Server has been running")
                    || text.startsWith("Connected Port:")
                    || text.startsWith("message blocked")
                    || text.startsWith("repeated message blocked");
        }

        private void rememberLocalBroadcastEcho(String body) {
            localBroadcastEchoes.addLast(normalize(body));
            while(localBroadcastEchoes.size() > 12) localBroadcastEchoes.removeFirst();
        }

        private boolean consumeLocalBroadcastEcho(String body) {
            String normalized = normalize(body);
            Iterator<String> it = localBroadcastEchoes.iterator();
            while(it.hasNext()) {
                if(normalized.equals(it.next())) {
                    it.remove();
                    return true;
                }
            }
            return false;
        }

        private String normalize(String body) {
            return body == null ? "" : body.trim();
        }
    }

    private static class PendingMessage {
        String id;
        String target;
        String body;

        PendingMessage(String id, String target, String body) {
            this.id = id;
            this.target = target;
            this.body = body;
        }
    }

    private static class WebMessage {
        private static long nextSequence = 0;
        String id;
        String conversation;
        String sender;
        String body;
        String direction;
        String kind;
        String time;
        String status;
        String fileName;
        String mime;
        long fileSize;
        String dataBase64;
        long sequence;

        static WebMessage text(String id, String conversation, String sender, String body,
                String direction, String time, String status) {
            WebMessage message = base(id, conversation, sender, direction, "text", time, status);
            message.body = body;
            return message;
        }

        static WebMessage system(String id, String conversation, String body, String time) {
            WebMessage message = base(id, conversation, "系统", "system", "system", time, "");
            message.body = body;
            return message;
        }

        static WebMessage file(String id, String conversation, String sender, String fileName,
                String direction, String time, String status, String mime, long fileSize, String dataBase64) {
            WebMessage message = base(id, conversation, sender, direction, "file", time, status);
            message.fileName = fileName;
            message.mime = empty(mime) ? mimeFromName(fileName) : mime;
            message.fileSize = fileSize;
            message.dataBase64 = dataBase64;
            return message;
        }

        private static synchronized WebMessage base(String id, String conversation, String sender,
                String direction, String kind, String time, String status) {
            WebMessage message = new WebMessage();
            message.id = id;
            message.conversation = empty(conversation) ? BROADCAST_CHAT : conversation;
            message.sender = sender;
            message.direction = direction;
            message.kind = kind;
            message.time = time;
            message.status = status;
            message.sequence = ++nextSequence;
            return message;
        }

        String preview() {
            if("system".equals(kind)) return body;
            if("file".equals(kind)) return ("outgoing".equals(direction) ? "我: " : sender + ": ")
                    + "[" + fileLabel(fileName) + "] " + fileName;
            return ("outgoing".equals(direction) ? "我: " : sender + ": ") + body;
        }

        String toJson() {
            StringBuilder out = new StringBuilder();
            out.append('{');
            appendField(out, "id", id);
            out.append(',');
            appendField(out, "conversation", conversation);
            out.append(',');
            appendField(out, "sender", sender);
            out.append(',');
            appendField(out, "body", body);
            out.append(',');
            appendField(out, "direction", direction);
            out.append(',');
            appendField(out, "kind", kind);
            out.append(',');
            appendField(out, "time", time);
            out.append(',');
            appendField(out, "status", status);
            out.append(',');
            appendField(out, "fileName", fileName);
            out.append(',');
            appendField(out, "mime", mime);
            out.append(',');
            appendField(out, "fileSize", fileSize);
            out.append(',');
            appendField(out, "dataBase64", dataBase64);
            out.append('}');
            return out.toString();
        }
    }

    private static class SseClient {
        private final OutputStream out;
        private volatile boolean open = true;

        SseClient(OutputStream out) {
            this.out = out;
        }

        synchronized boolean send(String type, String json) {
            if(!open) return false;
            try {
                String payload = "event: " + type + "\n"
                        + "data: " + (json == null ? "{}" : json.replace("\n", "\\n")) + "\n\n";
                out.write(payload.getBytes(StandardCharsets.UTF_8));
                out.flush();
                return true;
            } catch(IOException e) {
                open = false;
                return false;
            }
        }

        synchronized void comment(String text) {
            if(!open) return;
            try {
                out.write((":" + text + "\n\n").getBytes(StandardCharsets.UTF_8));
                out.flush();
            } catch(IOException e) {
                open = false;
            }
        }

        boolean isOpen() {
            return open;
        }
    }

    private static class ApiResult {
        boolean ok;
        String payload;
        String error;

        static ApiResult ok(String payload) {
            ApiResult result = new ApiResult();
            result.ok = true;
            result.payload = payload == null ? "{}" : payload;
            return result;
        }

        static ApiResult error(String error) {
            ApiResult result = new ApiResult();
            result.ok = false;
            result.error = empty(error) ? "操作失败" : error;
            return result;
        }

        String toJson() {
            if(ok) return "{\"ok\":true,\"data\":" + payload + "}";
            StringBuilder out = new StringBuilder("{\"ok\":false,\"error\":");
            appendString(out, error);
            out.append('}');
            return out.toString();
        }
    }

    private static Map<String, String> parseJsonObject(String json) {
        Map<String, String> map = new HashMap<String, String>();
        if(json == null) return map;
        int i = 0;
        while(i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
        if(i < json.length() && json.charAt(i) == '{') i++;
        while(i < json.length()) {
            while(i < json.length() && (Character.isWhitespace(json.charAt(i))
                    || json.charAt(i) == ',')) i++;
            if(i >= json.length() || json.charAt(i) == '}') break;
            ParseResult key = readJsonValue(json, i);
            i = key.next;
            while(i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
            if(i < json.length() && json.charAt(i) == ':') i++;
            while(i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
            ParseResult value = readJsonValue(json, i);
            i = value.next;
            map.put(key.value, value.value);
        }
        return map;
    }

    private static ParseResult readJsonValue(String json, int start) {
        if(start >= json.length()) return new ParseResult("", start);
        if(json.charAt(start) == '"') {
            StringBuilder out = new StringBuilder();
            int i = start + 1;
            while(i < json.length()) {
                char ch = json.charAt(i++);
                if(ch == '"') break;
                if(ch == '\\' && i < json.length()) {
                    char esc = json.charAt(i++);
                    if(esc == 'n') out.append('\n');
                    else if(esc == 'r') out.append('\r');
                    else if(esc == 't') out.append('\t');
                    else if(esc == 'b') out.append('\b');
                    else if(esc == 'f') out.append('\f');
                    else if(esc == 'u' && i + 4 <= json.length()) {
                        out.append((char)Integer.parseInt(json.substring(i, i + 4), 16));
                        i += 4;
                    } else {
                        out.append(esc);
                    }
                } else {
                    out.append(ch);
                }
            }
            return new ParseResult(out.toString(), i);
        }
        int i = start;
        while(i < json.length() && ",}".indexOf(json.charAt(i)) < 0) i++;
        return new ParseResult(json.substring(start, i).trim(), i);
    }

    private static class ParseResult {
        String value;
        int next;

        ParseResult(String value, int next) {
            this.value = value;
            this.next = next;
        }
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        InputStream in = exchange.getRequestBody();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while((read = in.read(buffer)) >= 0) {
            out.write(buffer, 0, read);
        }
        return out.toString(StandardCharsets.UTF_8.name());
    }

    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        sendText(exchange, status, json, "application/json; charset=utf-8");
    }

    private static void sendText(HttpExchange exchange, int status, String text, String contentType) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);
        try(OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static String value(Map<String, String> map, String key, String fallback) {
        String value = map.get(key);
        return value == null ? fallback : value;
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch(Exception e) {
            return fallback;
        }
    }

    private static boolean empty(String value) {
        return value == null || value.trim().length() == 0;
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String stripHtml(String text) {
        if(text == null) return "";
        return text.replaceAll("(?is)<br\\s*/?>", "\n")
                .replaceAll("(?is)<[^>]+>", "")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&");
    }

    private static String encodeToken(String value) {
        return Base64.getEncoder().encodeToString((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
    }

    private static String decodeToken(String value) {
        try {
            return new String(Base64.getDecoder().decode(value == null ? "" : value), StandardCharsets.UTF_8);
        } catch(Exception e) {
            return "";
        }
    }

    private static String stripDataUrl(String dataBase64) {
        int comma = dataBase64.indexOf(',');
        return comma >= 0 ? dataBase64.substring(comma + 1) : dataBase64;
    }

    private static String safeFileName(String fileName) {
        if(empty(fileName)) return "file.bin";
        return fileName.trim().replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
    }

    private static String fileLabel(String name) {
        String mime = mimeFromName(name);
        if(mime.startsWith("image/")) return "图片";
        if(mime.startsWith("audio/")) return "语音";
        if(mime.startsWith("video/")) return "视频";
        return "文件";
    }

    private static String mimeFromName(String name) {
        String lower = name == null ? "" : name.toLowerCase(Locale.ROOT);
        if(lower.endsWith(".png")) return "image/png";
        if(lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if(lower.endsWith(".gif")) return "image/gif";
        if(lower.endsWith(".webp")) return "image/webp";
        if(lower.endsWith(".wav")) return "audio/wav";
        if(lower.endsWith(".mp3")) return "audio/mpeg";
        if(lower.endsWith(".webm")) return "audio/webm";
        if(lower.endsWith(".ogg")) return "audio/ogg";
        if(lower.endsWith(".mp4")) return "video/mp4";
        return "application/octet-stream";
    }

    private static String mimeType(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        if(name.endsWith(".html")) return "text/html; charset=utf-8";
        if(name.endsWith(".css")) return "text/css; charset=utf-8";
        if(name.endsWith(".js")) return "application/javascript; charset=utf-8";
        if(name.endsWith(".png")) return "image/png";
        if(name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if(name.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }

    private static void appendField(StringBuilder out, String key, String value) {
        appendString(out, key);
        out.append(':');
        appendString(out, value == null ? "" : value);
    }

    private static void appendField(StringBuilder out, String key, boolean value) {
        appendString(out, key);
        out.append(':').append(value);
    }

    private static void appendField(StringBuilder out, String key, int value) {
        appendString(out, key);
        out.append(':').append(value);
    }

    private static void appendField(StringBuilder out, String key, long value) {
        appendString(out, key);
        out.append(':').append(value);
    }

    private static void appendString(StringBuilder out, String value) {
        out.append('"');
        if(value != null) {
            for(int i=0;i<value.length();i++) {
                char ch = value.charAt(i);
                if(ch == '"' || ch == '\\') out.append('\\').append(ch);
                else if(ch == '\n') out.append("\\n");
                else if(ch == '\r') out.append("\\r");
                else if(ch == '\t') out.append("\\t");
                else if(ch < 32) out.append(String.format("\\u%04x", (int)ch));
                else out.append(ch);
            }
        }
        out.append('"');
    }
}
