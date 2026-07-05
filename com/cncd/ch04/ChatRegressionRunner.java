package com.cncd.ch04;

import com.cncd.ch04.client.ChatClient;
import com.cncd.ch04.client.ClientKernel;
import com.cncd.ch04.server.BroadcastCommandParser;
import com.cncd.ch04.server.FileDataSource;
import com.cncd.ch04.server.MainServer;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChatRegressionRunner {
    private static final Path SCREENSHOT_DIR = Paths.get("screenshots", "regression");
    private static int failureCount = 0;

    public static void main(String[] args) {
        try {
            Files.createDirectories(SCREENSHOT_DIR);
            runScenario("A 在线私聊", new Scenario() {
                public void run() throws Exception {
                    scenarioOnlinePrivate();
                }
            });
            runScenario("B 广播消息", new Scenario() {
                public void run() throws Exception {
                    scenarioBroadcast();
                }
            });
            runScenario("C 离线私聊", new Scenario() {
                public void run() throws Exception {
                    scenarioOfflinePrivate();
                }
            });
            runScenario("D 好友申请", new Scenario() {
                public void run() throws Exception {
                    scenarioFriendRequest();
                }
            });
            runScenario("E 群聊", new Scenario() {
                public void run() throws Exception {
                    scenarioGroupChat();
                }
            });
            runScenario("F 文件和附件入口", new Scenario() {
                public void run() throws Exception {
                    scenarioFileAndAttachmentGuards();
                }
            });
            runScenario("G 原始协议过滤", new Scenario() {
                public void run() throws Exception {
                    scenarioProtocolFilter();
                }
            });

            if(failureCount > 0) {
                System.out.println("FAILED: " + failureCount + " regression scenario(s)");
                System.exit(1);
            }
            System.out.println("PASS: all Swing chat regression scenarios");
            System.exit(0);
        } catch(Throwable t) {
            System.out.println("FAILED: regression runner crashed: " + t.getMessage());
            t.printStackTrace(System.out);
            System.exit(1);
        }
    }

    private static void runScenario(String name, Scenario scenario) throws Exception {
        try {
            scenario.run();
            flushEdt();
            System.out.println("PASS: " + name);
        } catch(Throwable t) {
            failureCount++;
            System.out.println("FAILED: " + name + " - " + t.getMessage());
            t.printStackTrace(System.out);
        } finally {
            disposeAllWindows();
            sleep(300);
        }
    }

    private static void scenarioOnlinePrivate() throws Exception {
        final Context ctx = startContext("onlinePrivate", "AliceRegression1", "BobRegression1");
        openPair(ctx, true);
        final String text = "online private regression message";

        onEdt(new Runnable() {
            public void run() {
                selectConversation(ctx.alice, ctx.bobName);
                selectConversation(ctx.bob, ctx.aliceName);
                ctx.alice.demoPrivate(ctx.bobName, text);
            }
        });

        waitFor(new Condition() {
            public boolean ok() throws Exception {
                return messageCount(ctx.alice, ctx.bobName, text) == 1
                        && messageCount(ctx.bob, ctx.aliceName, text) == 1
                        && visibleHistoryText(ctx.alice).contains(text)
                        && visibleHistoryText(ctx.bob).contains(text);
            }
        }, "online private message visible on both clients", 6000);

        assertConversationKey(ctx.alice, ctx.bobName, text, ctx.bobName);
        assertConversationKey(ctx.bob, ctx.aliceName, text, ctx.aliceName);
        assertPreviewContains(ctx.alice, ctx.bobName, text, "Alice private preview");
        assertPreviewContains(ctx.bob, ctx.aliceName, text, "Bob private preview");
        assertTrue(unreadFor(ctx.bob, ctx.aliceName) == 0,
                "Bob selected Alice, unread count should be 0");
        captureClients("online_private.png", "A online private", ctx.alice, ctx.bob);
    }

    private static void scenarioBroadcast() throws Exception {
        final Context ctx = startContext("broadcast", "AliceRegression2", "BobRegression2");
        openPair(ctx, true);
        final String text = "broadcast regression message";

        onEdt(new Runnable() {
            public void run() {
                selectConversation(ctx.alice, "广播");
                selectConversation(ctx.bob, "广播");
                ctx.alice.demoBroadcast(text);
            }
        });

        waitFor(new Condition() {
            public boolean ok() throws Exception {
                return messageCount(ctx.alice, "广播", text) == 1
                        && messageCount(ctx.bob, "广播", text) == 1
                        && visibleHistoryText(ctx.bob).contains(text);
            }
        }, "broadcast delivered to Bob", 6000);

        assertPreviewContains(ctx.alice, "广播", text, "Alice broadcast preview");
        assertPreviewContains(ctx.bob, "广播", text, "Bob broadcast preview");
        assertTrue(messageCountOptional(ctx.bob, ctx.aliceName, text) == 0,
                "Broadcast must not be stored in Bob private conversation");
        onEdt(new Runnable() {
            public void run() {
                selectConversation(ctx.bob, ctx.aliceName);
            }
        });
        flushEdt();
        assertTrue(!visibleHistoryText(ctx.bob).contains(text),
                "Broadcast must not be visible in private chat after switching conversations");
        captureClients("broadcast.png", "B broadcast", ctx.alice, ctx.bob);
    }

    private static void scenarioOfflinePrivate() throws Exception {
        final Context ctx = startContext("offlinePrivate", "AliceRegression3", "BobRegression3");
        openPair(ctx, true);
        final String text = "offline local echo regression message";

        disconnect(ctx.bob);
        waitFor(new Condition() {
            public boolean ok() throws Exception {
                return !visibleUsers(ctx.alice).contains(ctx.bobName);
            }
        }, "Alice sees Bob offline", 6000);

        onEdt(new Runnable() {
            public void run() {
                selectConversation(ctx.alice, ctx.bobName);
                ctx.alice.demoPrivate(ctx.bobName, text);
            }
        });

        waitFor(new Condition() {
            public boolean ok() throws Exception {
                return visibleHistoryText(ctx.alice).contains(text)
                        && visibleHistoryText(ctx.alice).contains("待发送");
            }
        }, "Alice visible offline outgoing bubble", 4000);

        assertOutgoingMessage(ctx.aliceName, ctx.alice, ctx.bobName, text, "待发送", 1);
        assertPreviewContains(ctx.alice, ctx.bobName, text, "Alice offline preview");
        assertTrue(pendingPrivateCount(ctx.alice) == 1,
                "Alice should have one pending private message");
        assertPendingFileContains(ctx.aliceName, ctx.bobName, text);

        onEdt(new Runnable() {
            public void run() {
                ctx.alice.addMsg("[private] " + ctx.bobName + ": __VIDEO_CALL__|AUDIO|ignored");
                ctx.alice.addMsg("Server: regression system notice");
            }
        });
        flushEdt();
        assertTrue(!visibleHistoryText(ctx.alice).contains("__VIDEO_CALL__"),
                "Raw protocol frame should not be visible");
        assertTrue(visibleHistoryText(ctx.alice).contains(text),
                "System/protocol messages must not clear current chat");
        captureClients("offline_private_local_echo.png", "C offline private", ctx.alice);

        onEdt(new Runnable() {
            public void run() {
                ctx.bobAgain = createClient("Bob reconnected", 760, 60);
                loginAndConnect(ctx.bobAgain, ctx.port, ctx.bobName);
                ctx.bobAgain.demoAddFriend(ctx.aliceName);
                selectConversation(ctx.bobAgain, ctx.aliceName);
            }
        });

        waitFor(new Condition() {
            public boolean ok() throws Exception {
                return isConnected(ctx.bobAgain) && visibleUsers(ctx.alice).contains(ctx.bobName);
            }
        }, "Bob reconnects", 6000);
        waitFor(new Condition() {
            public boolean ok() throws Exception {
                return pendingPrivateCount(ctx.alice) == 0
                        && messageCount(ctx.alice, ctx.bobName, text) == 1
                        && messageCount(ctx.bobAgain, ctx.aliceName, text) == 1;
            }
        }, "pending message flushes without duplicate echo", 7000);

        String status = statusForBody(ctx.alice, ctx.bobName, text);
        assertTrue(status != null && !"待发送".equals(status),
                "Alice status should move away from pending after Bob reconnects; actual=" + status);
    }

    private static void scenarioFriendRequest() throws Exception {
        final Context ctx = startContext("friendRequest", "AliceRegression4", "BobRegression4");
        openPair(ctx, false);

        onEdt(new Runnable() {
            public void run() {
                invoke(ctx.alice, "requestFriend",
                        new Class[] {String.class}, new Object[] {ctx.bobName});
            }
        });

        waitFor(new Condition() {
            public boolean ok() throws Exception {
                return visibleHistoryText(ctx.bob).contains("请求添加你为好友")
                        && incomingFriendRequests(ctx.bob).contains(ctx.aliceName);
            }
        }, "Bob sees inline friend request card", 6000);
        assertTrue(!hasVisibleOptionPane(), "Friend request should not show blocking JOptionPane");
        captureClients("friend_request.png", "D friend request", ctx.alice, ctx.bob);

        onEdt(new Runnable() {
            public void run() {
                AbstractButton accept = findButton(ctx.bob.getContentPane(), "同意");
                assertTrue(accept != null, "Friend request card should have an accept button");
                accept.doClick();
            }
        });

        waitFor(new Condition() {
            public boolean ok() throws Exception {
                return friends(ctx.alice).contains(ctx.bobName)
                        && friends(ctx.bob).contains(ctx.aliceName)
                        && !incomingFriendRequests(ctx.bob).contains(ctx.aliceName);
            }
        }, "friend relationship accepted on both sides", 6000);
    }

    private static void scenarioGroupChat() throws Exception {
        final Context ctx = startContext("groupChat", "AliceRegression5", "BobRegression5");
        openPair(ctx, true);
        final String groupName = "StudyGroupRegression5";
        final String groupLabel = groupLabel(groupName);
        final String text = "group regression message";

        onEdt(new Runnable() {
            public void run() {
                Set<String> members = new LinkedHashSet<String>();
                members.add(ctx.aliceName);
                members.add(ctx.bobName);
                invoke(ctx.alice, "addOrUpdateGroup",
                        new Class[] {String.class, Set.class, boolean.class},
                        new Object[] {groupName, members, Boolean.TRUE});
                invoke(ctx.alice, "sendGroupInvite",
                        new Class[] {String.class, Set.class},
                        new Object[] {groupName, members});
                invoke(ctx.alice, "rebuildConversationList",
                        new Class[] {String.class}, new Object[] {groupLabel});
            }
        });

        waitFor(new Condition() {
            public boolean ok() throws Exception {
                return chatGroups(ctx.alice).containsKey(groupName)
                        && chatGroups(ctx.bob).containsKey(groupName);
            }
        }, "group appears on both clients", 6000);

        onEdt(new Runnable() {
            public void run() {
                invoke(ctx.alice, "rebuildConversationList",
                        new Class[] {String.class}, new Object[] {groupLabel});
                invoke(ctx.bob, "rebuildConversationList",
                        new Class[] {String.class}, new Object[] {groupLabel});
                selectConversation(ctx.alice, groupLabel);
                selectConversation(ctx.bob, groupLabel);
                ctx.alice.demoCommand(text);
            }
        });

        waitFor(new Condition() {
            public boolean ok() throws Exception {
                return messageCount(ctx.alice, groupLabel, text) == 1
                        && messageCount(ctx.bob, groupLabel, text) == 1;
            }
        }, "group message delivered", 6000);

        assertTrue(messageCountOptional(ctx.bob, ctx.aliceName, text) == 0,
                "Group message must not pollute private chat");
        assertTrue(messageCountOptional(ctx.bob, "广播", text) == 0,
                "Group message must not pollute broadcast chat");
        assertPreviewContains(ctx.alice, groupLabel, text, "Alice group preview");
        assertPreviewContains(ctx.bob, groupLabel, text, "Bob group preview");
        assertTrue(labelText(ctx.alice, "conversationTitleLabel").equals(groupName),
                "Group title should show group name");
        assertTrue(labelText(ctx.alice, "conversationSubtitleLabel").contains("2 人"),
                "Group subtitle should show member count");
        captureClients("group_chat.png", "E group chat", ctx.alice, ctx.bob);
    }

    private static void scenarioFileAndAttachmentGuards() throws Exception {
        final Context ctx = startContext("fileTransfer", "AliceRegression6", "BobRegression6");
        openPair(ctx, true);
        final Path file = Files.createTempFile("regression-small-file", ".txt");
        Files.write(file, "small file regression payload".getBytes(StandardCharsets.UTF_8));

        onEdt(new Runnable() {
            public void run() {
                selectConversation(ctx.alice, ctx.bobName);
                selectConversation(ctx.bob, ctx.aliceName);
                ctx.alice.demoSendFile(ctx.bobName, file.toFile());
            }
        });

        waitFor(new Condition() {
            public boolean ok() throws Exception {
                return fileMessageCount(ctx.alice, ctx.bobName, file.getFileName().toString()) == 1
                        && fileMessageCount(ctx.bob, ctx.aliceName, file.getFileName().toString()) == 1;
            }
        }, "file card appears on sender and receiver", 7000);

        String aliceHistory = visibleHistoryText(ctx.alice);
        String bobHistory = visibleHistoryText(ctx.bob);
        assertFileNameVisible(aliceHistory, file.getFileName().toString(), "Sender file name");
        assertFileNameVisible(bobHistory, file.getFileName().toString(), "Receiver file name");
        assertTrue(aliceHistory.contains("文件") && bobHistory.contains("文件"),
                "File cards should show file type");
        assertTrue(bobHistory.contains("点击下载") || bobHistory.contains("点击预览"),
                "Receiver file card should expose download/save affordance");

        onEdt(new Runnable() {
            public void run() {
                selectConversation(ctx.alice, "广播");
            }
        });
        flushEdt();
        assertAttachmentDisabledWithReason(ctx.alice, "broadcast attachment guard");

        final String groupName = "FileGuardGroup6";
        final String groupLabel = groupLabel(groupName);
        onEdt(new Runnable() {
            public void run() {
                Set<String> members = new LinkedHashSet<String>();
                members.add(ctx.aliceName);
                members.add(ctx.bobName);
                invoke(ctx.alice, "addOrUpdateGroup",
                        new Class[] {String.class, Set.class, boolean.class},
                        new Object[] {groupName, members, Boolean.TRUE});
                invoke(ctx.alice, "rebuildConversationList",
                        new Class[] {String.class}, new Object[] {groupLabel});
                selectConversation(ctx.alice, groupLabel);
            }
        });
        flushEdt();
        assertAttachmentDisabledWithReason(ctx.alice, "group attachment guard");

        disconnect(ctx.bob);
        waitFor(new Condition() {
            public boolean ok() throws Exception {
                return !visibleUsers(ctx.alice).contains(ctx.bobName);
            }
        }, "Bob offline for attachment guard", 6000);
        onEdt(new Runnable() {
            public void run() {
                selectConversation(ctx.alice, ctx.bobName);
            }
        });
        flushEdt();
        assertAttachmentDisabledWithReason(ctx.alice, "offline attachment guard");
        captureClients("file_transfer.png", "F file transfer", ctx.alice);
    }

    private static void scenarioProtocolFilter() throws Exception {
        final Context ctx = startContext("protocolFilter", "AliceRegression7", "BobRegression7");
        openPair(ctx, true);
        final String visibleText = "protocol filter ordinary text";

        onEdt(new Runnable() {
            public void run() {
                selectConversation(ctx.alice, ctx.bobName);
                ctx.alice.demoPrivate(ctx.bobName, visibleText);
            }
        });
        waitFor(new Condition() {
            public boolean ok() throws Exception {
                return visibleHistoryText(ctx.alice).contains(visibleText);
            }
        }, "baseline private text visible", 5000);

        onEdt(new Runnable() {
            public void run() {
                ctx.alice.addMsg("[private] " + ctx.bobName + ": __VIDEO_CALL__|AUDIO|raw-frame");
                ctx.alice.addMsg("[private] " + ctx.bobName + ": __READ__|raw-read");
                ctx.alice.addMsg("Server: protocol filter system notice");
            }
        });
        flushEdt();

        String history = visibleHistoryText(ctx.alice);
        assertTrue(history.contains(visibleText), "Protocol/system messages must not clear visible chat");
        assertTrue(!history.contains("__VIDEO_CALL__"), "Raw video protocol must be hidden");
        assertTrue(!history.contains("__READ__"), "Raw read receipt protocol must be hidden");
        captureClients("protocol_filter.png", "G protocol filter", ctx.alice);
    }

    private static Context startContext(String id, String aliceName, String bobName) throws Exception {
        cleanClientData(aliceName);
        cleanClientData(bobName);
        int port = freePort();
        MainServer.ds = new FileDataSource();
        MainServer.cp = new BroadcastCommandParser();
        MainServer.cp.setDataSource(MainServer.ds);
        new MainServer(port);
        sleep(700);
        Context ctx = new Context();
        ctx.id = id;
        ctx.port = port;
        ctx.aliceName = aliceName;
        ctx.bobName = bobName;
        return ctx;
    }

    private static void openPair(final Context ctx, final boolean makeFriends) throws Exception {
        onEdt(new Runnable() {
            public void run() {
                ctx.alice = createClient("Alice " + ctx.id, 60, 60);
                loginAndConnect(ctx.alice, ctx.port, ctx.aliceName);
                if(makeFriends) ctx.alice.demoAddFriend(ctx.bobName);
            }
        });
        waitFor(new Condition() {
            public boolean ok() throws Exception {
                return isConnected(ctx.alice);
            }
        }, ctx.id + " Alice connects", 5000);

        onEdt(new Runnable() {
            public void run() {
                ctx.bob = createClient("Bob " + ctx.id, 720, 60);
                loginAndConnect(ctx.bob, ctx.port, ctx.bobName);
                if(makeFriends) ctx.bob.demoAddFriend(ctx.aliceName);
            }
        });
        waitFor(new Condition() {
            public boolean ok() throws Exception {
                return isConnected(ctx.bob)
                        && visibleUsers(ctx.alice).contains(ctx.bobName)
                        && visibleUsers(ctx.bob).contains(ctx.aliceName);
            }
        }, ctx.id + " pair online", 7000);
    }

    private static ChatClient createClient(String title, int x, int y) {
        ChatClient client = new ChatClient();
        client.setTitle(title);
        client.setSize(1120, 760);
        client.setMinimumSize(new Dimension(920, 600));
        client.setLocation(x, y);
        client.setDefaultCloseOperation(ChatClient.DISPOSE_ON_CLOSE);
        client.setVisible(true);
        return client;
    }

    private static void loginAndConnect(ChatClient client, int port, String nick) {
        try {
            Class loginDataClass = Class.forName("com.cncd.ch04.client.ChatClient$LoginData");
            java.lang.reflect.Constructor ctor = loginDataClass.getDeclaredConstructor(
                    String.class, String.class, String.class, String.class);
            ctor.setAccessible(true);
            Object loginData = ctor.newInstance("127.0.0.1", String.valueOf(port), nick, "test-password");
            Method applyLogin = ChatClient.class.getDeclaredMethod("applyLogin", loginDataClass);
            applyLogin.setAccessible(true);
            applyLogin.invoke(client, loginData);
            Method connect = ChatClient.class.getDeclaredMethod("connect");
            connect.setAccessible(true);
            connect.invoke(client);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void disconnect(final ChatClient client) throws Exception {
        if(client == null) return;
        onEdt(new Runnable() {
            public void run() {
                try {
                    ClientKernel kernel = (ClientKernel) field(client, "ck");
                    if(kernel != null) kernel.dropMe();
                    client.dispose();
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        sleep(500);
    }

    private static void selectConversation(ChatClient client, String name) {
        try {
            JList list = (JList) field(client, "onlineList");
            list.setSelectedValue(name, true);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String selectedConversation(ChatClient client) throws Exception {
        JList list = (JList) field(client, "onlineList");
        Object value = list.getSelectedValue();
        return value == null ? null : value.toString();
    }

    private static boolean isConnected(ChatClient client) throws Exception {
        ClientKernel kernel = (ClientKernel) field(client, "ck");
        return kernel != null && kernel.isConnected();
    }

    private static Set visibleUsers(ChatClient client) throws Exception {
        return (Set) field(client, "visibleUsers");
    }

    private static Set friends(ChatClient client) throws Exception {
        return (Set) field(client, "friends");
    }

    private static Set incomingFriendRequests(ChatClient client) throws Exception {
        return (Set) field(client, "incomingFriendRequests");
    }

    private static Map chatGroups(ChatClient client) throws Exception {
        return (Map) field(client, "chatGroups");
    }

    private static int pendingPrivateCount(ChatClient client) throws Exception {
        Map map = (Map) field(client, "pendingPrivateMessages");
        return map.size();
    }

    private static String previewFor(ChatClient client, String conversationKey) throws Exception {
        Map meta = (Map) field(client, "conversationMeta");
        Object item = meta.get(conversationKey);
        return item == null ? "" : stringField(item, "preview");
    }

    private static int unreadFor(ChatClient client, String conversationKey) throws Exception {
        Map meta = (Map) field(client, "conversationMeta");
        Object item = meta.get(conversationKey);
        if(item == null) return 0;
        Object value = field(item, "unread");
        return value == null ? 0 : ((Integer) value).intValue();
    }

    private static void assertPreviewContains(ChatClient client, String conversationKey,
            String body, String label) throws Exception {
        String preview = previewFor(client, conversationKey);
        String needle = body.length() > 12 ? body.substring(0, 12) : body;
        boolean containsMessage = preview.contains(needle)
                || (body.length() > 0 && preview.contains(body.substring(0, 1)));
        assertTrue(containsMessage, label + " should update with message content; actual=" + preview);
    }

    private static void assertOutgoingMessage(String owner, ChatClient client,
            String conversationKey, String body, String status, int expectedCount) throws Exception {
        int count = 0;
        String foundStatus = null;
        List messages = messagesFor(client, conversationKey);
        for(int i=0;i<messages.size();i++) {
            Object message = messages.get(i);
            if(body.equals(stringField(message, "body"))) {
                count++;
                foundStatus = stringField(message, "deliveryStatus");
                assertTrue(conversationKey.equals(stringField(message, "conversationKey")),
                        owner + " message conversationKey should be " + conversationKey);
            }
        }
        assertTrue(count == expectedCount,
                owner + " expected " + expectedCount + " message(s), actual=" + count);
        assertTrue(status == null || status.equals(foundStatus),
                owner + " expected status=" + status + ", actual=" + foundStatus);
    }

    private static void assertConversationKey(ChatClient client, String conversationKey,
            String body, String expectedKey) throws Exception {
        List messages = messagesFor(client, conversationKey);
        for(int i=0;i<messages.size();i++) {
            Object message = messages.get(i);
            if(body.equals(stringField(message, "body"))) {
                assertTrue(expectedKey.equals(stringField(message, "conversationKey")),
                        "conversationKey should be " + expectedKey);
                return;
            }
        }
        throw new AssertionError("No message body found: " + body);
    }

    private static int messageCount(ChatClient client, String conversationKey, String body) throws Exception {
        List messages = messagesFor(client, conversationKey);
        int count = 0;
        for(int i=0;i<messages.size();i++) {
            Object message = messages.get(i);
            if(body.equals(stringField(message, "body"))) count++;
        }
        return count;
    }

    private static int messageCountOptional(ChatClient client, String conversationKey, String body) throws Exception {
        List messages = messagesForOptional(client, conversationKey);
        int count = 0;
        for(int i=0;i<messages.size();i++) {
            Object message = messages.get(i);
            if(body.equals(stringField(message, "body"))) count++;
        }
        return count;
    }

    private static int fileMessageCount(ChatClient client, String conversationKey, String fileName) throws Exception {
        List messages = messagesFor(client, conversationKey);
        int count = 0;
        for(int i=0;i<messages.size();i++) {
            Object message = messages.get(i);
            Object fileMessage = field(message, "fileMessage");
            if(Boolean.TRUE.equals(fileMessage) && fileName.equals(stringField(message, "fileName"))) count++;
        }
        return count;
    }

    private static String statusForBody(ChatClient client, String conversationKey, String body) throws Exception {
        List messages = messagesFor(client, conversationKey);
        for(int i=0;i<messages.size();i++) {
            Object message = messages.get(i);
            if(body.equals(stringField(message, "body"))) return stringField(message, "deliveryStatus");
        }
        return null;
    }

    private static List messagesFor(ChatClient client, String conversationKey) throws Exception {
        List list = messagesForOptional(client, conversationKey);
        assertTrue(list != null && list.size() >= 0, "No conversation messages for " + conversationKey);
        return list;
    }

    private static List messagesForOptional(ChatClient client, String conversationKey) throws Exception {
        Map messages = (Map) field(client, "conversationMessages");
        List list = (List) messages.get(conversationKey);
        return list == null ? new ArrayList() : list;
    }

    private static String visibleHistoryText(ChatClient client) throws Exception {
        final StringBuilder text = new StringBuilder();
        final JComponent history = (JComponent) field(client, "historyWindow");
        onEdt(new Runnable() {
            public void run() {
                collectText(history, text);
            }
        });
        return text.toString();
    }

    private static void collectText(Component component, StringBuilder text) {
        if(component instanceof JLabel) {
            text.append(' ').append(((JLabel) component).getText());
        } else if(component instanceof JTextComponent) {
            text.append(' ').append(((JTextComponent) component).getText());
        } else if(component instanceof AbstractButton) {
            text.append(' ').append(((AbstractButton) component).getText());
        }
        if(component instanceof Container) {
            Component[] children = ((Container) component).getComponents();
            for(int i=0;i<children.length;i++) collectText(children[i], text);
        }
    }

    private static void assertPendingFileContains(String user, String target, String body) throws Exception {
        Path file = clientDataRoot().resolve(user).resolve("pending-private.txt");
        assertTrue(Files.exists(file), "pending-private.txt should exist for " + user);
        String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        assertTrue(content.contains(target) || content.contains(encodeToken(target)),
                "pending-private.txt should mention target " + target);
        assertTrue(content.contains(body) || content.contains(encodeToken(body)),
                "pending-private.txt should persist message body");
    }

    private static void assertAttachmentDisabledWithReason(ChatClient client, String label) throws Exception {
        JButton fileButton = (JButton) field(client, "buttonFile");
        JButton imageButton = (JButton) field(client, "buttonImage");
        assertTrue(!fileButton.isEnabled(), label + ": file button should be disabled");
        assertTrue(!imageButton.isEnabled(), label + ": image button should be disabled");
        String tooltip = fileButton.getToolTipText();
        assertTrue(tooltip != null && tooltip.length() > 0,
                label + ": disabled attachment tool should expose a reason");
    }

    private static void assertFileNameVisible(String history, String fileName, String label) {
        String head = fileName.length() <= 8 ? fileName : fileName.substring(0, 8);
        String suffix = "";
        int dot = fileName.lastIndexOf('.');
        if(dot >= 0) suffix = fileName.substring(dot);
        boolean visible = history.contains(fileName)
                || (history.contains(head) && (suffix.length() == 0 || history.contains(suffix)));
        assertTrue(visible, label + " should be visible or professionally clipped; history=" + history);
    }

    private static String labelText(ChatClient client, String fieldName) throws Exception {
        JLabel label = (JLabel) field(client, fieldName);
        return label == null ? "" : label.getText();
    }

    private static AbstractButton findButton(Component root, String text) {
        if(root instanceof AbstractButton && text.equals(((AbstractButton) root).getText())) {
            return (AbstractButton) root;
        }
        if(root instanceof Container) {
            Component[] children = ((Container) root).getComponents();
            for(int i=0;i<children.length;i++) {
                AbstractButton found = findButton(children[i], text);
                if(found != null) return found;
            }
        }
        return null;
    }

    private static boolean hasVisibleOptionPane() {
        Window[] windows = Window.getWindows();
        for(int i=0;i<windows.length;i++) {
            if(!windows[i].isVisible() || !(windows[i] instanceof JDialog)) continue;
            String text = componentTreeText(windows[i]);
            if(text.indexOf("OptionPane") >= 0 || text.indexOf("JOptionPane") >= 0) return true;
        }
        return false;
    }

    private static String componentTreeText(Component component) {
        StringBuilder builder = new StringBuilder();
        collectText(component, builder);
        builder.append(' ').append(component.getClass().getName());
        if(component instanceof Container) {
            Component[] children = ((Container) component).getComponents();
            for(int i=0;i<children.length;i++) builder.append(' ').append(componentTreeText(children[i]));
        }
        return builder.toString();
    }

    private static void captureClients(String fileName, String title, ChatClient first) throws Exception {
        captureClients(fileName, title, first, null);
    }

    private static void captureClients(String fileName, final String title,
            final ChatClient first, final ChatClient second) throws Exception {
        final BufferedImage[] result = new BufferedImage[1];
        onEdt(new Runnable() {
            public void run() {
                Component left = first.getContentPane();
                Component right = second == null ? null : second.getContentPane();
                Dimension leftSize = left.getSize();
                Dimension rightSize = right == null ? new Dimension(0, 0) : right.getSize();
                int margin = 28;
                int gap = right == null ? 0 : 28;
                int titleHeight = 30;
                int width = margin * 2 + leftSize.width + gap + rightSize.width;
                int height = margin * 2 + titleHeight + Math.max(leftSize.height, rightSize.height);
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = image.createGraphics();
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, width, height);
                paintClient(g, left, margin, margin, title);
                if(right != null) {
                    paintClient(g, right, margin + leftSize.width + gap, margin, "peer");
                }
                g.dispose();
                result[0] = image;
            }
        });
        ImageIO.write(result[0], "png", SCREENSHOT_DIR.resolve(fileName).toFile());
        System.out.println("Captured " + SCREENSHOT_DIR.resolve(fileName).toAbsolutePath());
    }

    private static void paintClient(Graphics2D g, Component content, int x, int y, String title) {
        Dimension size = content.getSize();
        int titleHeight = 30;
        g.setColor(new Color(238, 238, 238));
        g.fillRect(x, y, size.width, titleHeight);
        g.setColor(new Color(185, 185, 185));
        g.drawRect(x, y, size.width - 1, titleHeight + size.height - 1);
        g.setColor(Color.BLACK);
        g.setFont(new Font("Dialog", Font.BOLD, 13));
        g.drawString(title, x + 10, y + 20);
        Graphics2D copy = (Graphics2D) g.create(x, y + titleHeight, size.width, size.height);
        content.printAll(copy);
        copy.dispose();
    }

    private static Object invoke(Object target, String methodName, Class[] types, Object[] args) {
        try {
            Method method = target.getClass().getDeclaredMethod(methodName, types);
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object field(Object target, String name) throws Exception {
        Field f = findField(target.getClass(), name);
        f.setAccessible(true);
        return f.get(target);
    }

    private static String stringField(Object target, String name) throws Exception {
        Object value = field(target, name);
        return value == null ? null : value.toString();
    }

    private static Field findField(Class type, String name) throws Exception {
        Class current = type;
        while(current != null) {
            try {
                return current.getDeclaredField(name);
            } catch(NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static String encodeToken(String value) {
        return java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String groupLabel(String groupName) {
        return "群聊 · " + groupName;
    }

    private static void cleanClientData(String user) throws Exception {
        deleteRecursively(clientDataRoot().resolve(user));
    }

    private static Path clientDataRoot() {
        return Paths.get(System.getProperty("user.home"), ".mihalychat", "client");
    }

    private static void deleteRecursively(Path path) throws Exception {
        if(path == null || !Files.exists(path)) return;
        Files.walkFileTree(path, new java.nio.file.SimpleFileVisitor<Path>() {
            public java.nio.file.FileVisitResult visitFile(Path file,
                    java.nio.file.attribute.BasicFileAttributes attrs) throws java.io.IOException {
                Files.delete(file);
                return java.nio.file.FileVisitResult.CONTINUE;
            }

            public java.nio.file.FileVisitResult postVisitDirectory(Path dir, java.io.IOException exc)
                    throws java.io.IOException {
                Files.delete(dir);
                return java.nio.file.FileVisitResult.CONTINUE;
            }
        });
    }

    private static int freePort() throws Exception {
        ServerSocket socket = new ServerSocket(0);
        int port = socket.getLocalPort();
        socket.close();
        return port;
    }

    private static void waitFor(Condition condition, String label, long timeoutMs) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;
        Throwable last = null;
        while(System.currentTimeMillis() < deadline) {
            try {
                flushEdt();
                if(condition.ok()) return;
            } catch(Throwable t) {
                last = t;
            }
            sleep(100);
        }
        if(last != null) throw new AssertionError(label + " timed out; last error=" + last.getMessage(), last);
        throw new AssertionError(label + " timed out");
    }

    private static void flushEdt() throws Exception {
        onEdt(new Runnable() {
            public void run() {
            }
        });
    }

    private static void onEdt(Runnable runnable) throws Exception {
        if(SwingUtilities.isEventDispatchThread()) runnable.run();
        else SwingUtilities.invokeAndWait(runnable);
    }

    private static void disposeAllWindows() throws Exception {
        onEdt(new Runnable() {
            public void run() {
                Window[] windows = Window.getWindows();
                for(int i=0;i<windows.length;i++) {
                    windows[i].dispose();
                }
            }
        });
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if(!condition) throw new AssertionError(message);
    }

    private static interface Scenario {
        void run() throws Exception;
    }

    private static interface Condition {
        boolean ok() throws Exception;
    }

    private static class Context {
        String id;
        int port;
        String aliceName;
        String bobName;
        ChatClient alice;
        ChatClient bob;
        ChatClient bobAgain;
    }
}
