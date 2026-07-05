package com.cncd.ch04;

import com.cncd.ch04.client.ChatClient;
import com.cncd.ch04.client.ClientKernel;
import com.cncd.ch04.server.BroadcastCommandParser;
import com.cncd.ch04.server.FileDataSource;
import com.cncd.ch04.server.MainServer;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
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
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChatRegressionRunner {
    private static final String ALICE = "AliceRegression";
    private static final String BOB = "BobRegression";
    private static final String OFFLINE_TEXT = "offline local echo regression message";
    private static final Path SCREENSHOT_DIR = Paths.get("screenshots", "regression");

    private static ChatClient alice;
    private static ChatClient bob;
    private static ChatClient bobAgain;

    public static void main(String[] args) {
        try {
            runRegression();
            System.out.println("PASS offline private local echo regression");
            System.exit(0);
        } catch(Throwable t) {
            System.out.println("FAILED offline private local echo regression: " + t.getMessage());
            t.printStackTrace(System.out);
            System.exit(1);
        }
    }

    private static void runRegression() throws Exception {
        Files.createDirectories(SCREENSHOT_DIR);
        cleanClientData(ALICE);
        cleanClientData(BOB);

        int port = freePort();
        MainServer.ds = new FileDataSource();
        MainServer.cp = new BroadcastCommandParser();
        MainServer.cp.setDataSource(MainServer.ds);
        new MainServer(port);
        sleep(900);

        onEdt(new Runnable() {
            public void run() {
                alice = createClient("Alice regression", 80, 60);
                loginAndConnect(alice, port, ALICE);
                alice.demoAddFriend(BOB);
            }
        });
        waitFor(new Condition() {
            public boolean ok() throws Exception {
                return isConnected(alice);
            }
        }, "Alice connects", 5000);

        onEdt(new Runnable() {
            public void run() {
                bob = createClient("Bob regression", 760, 60);
                loginAndConnect(bob, port, BOB);
                bob.demoAddFriend(ALICE);
            }
        });
        waitFor(new Condition() {
            public boolean ok() throws Exception {
                return isConnected(bob) && visibleUsers(alice).contains(BOB);
            }
        }, "Alice sees Bob online", 6000);

        disconnect(bob);
        waitFor(new Condition() {
            public boolean ok() throws Exception {
                return !visibleUsers(alice).contains(BOB);
            }
        }, "Alice sees Bob offline", 6000);

        onEdt(new Runnable() {
            public void run() {
                selectConversation(alice, BOB);
            }
        });
        waitFor(new Condition() {
            public boolean ok() throws Exception {
                return BOB.equals(selectedConversation(alice));
            }
        }, "Alice selects offline Bob", 3000);

        onEdt(new Runnable() {
            public void run() {
                alice.demoPrivate(BOB, OFFLINE_TEXT);
            }
        });

        waitFor(new Condition() {
            public boolean ok() throws Exception {
                return visibleHistoryText(alice).contains(OFFLINE_TEXT)
                        && visibleHistoryText(alice).contains("待发送");
            }
        }, "Alice visible offline outgoing bubble", 4000);

        assertOutgoingMessage(ALICE, alice, BOB, OFFLINE_TEXT, "待发送", 1);
        String preview = previewFor(alice, BOB);
        assertTrue(preview.contains(OFFLINE_TEXT.substring(0, 12)),
                "Alice left conversation preview should update, actual=" + preview);
        assertTrue(pendingPrivateCount(alice) == 1,
                "Alice should have one pending private message");
        assertPendingFileContains(ALICE, BOB, OFFLINE_TEXT);

        onEdt(new Runnable() {
            public void run() {
                alice.addMsg("[private] " + BOB + ": __VIDEO_CALL__|AUDIO|ignored");
                alice.addMsg("Server: regression system notice");
            }
        });
        flushEdt();
        assertTrue(!visibleHistoryText(alice).contains("__VIDEO_CALL__"),
                "Raw video protocol frame should not be visible in normal chat history");
        assertTrue(visibleHistoryText(alice).contains(OFFLINE_TEXT),
                "System/protocol messages must not clear the selected chat window");

        captureAlice("offline_private_local_echo.png");

        onEdt(new Runnable() {
            public void run() {
                bobAgain = createClient("Bob regression reconnected", 760, 60);
                loginAndConnect(bobAgain, port, BOB);
                bobAgain.demoAddFriend(ALICE);
                selectConversation(bobAgain, ALICE);
            }
        });

        waitFor(new Condition() {
            public boolean ok() throws Exception {
                return isConnected(bobAgain) && visibleUsers(alice).contains(BOB);
            }
        }, "Bob reconnects and Alice sees him online", 6000);

        waitFor(new Condition() {
            public boolean ok() throws Exception {
                return pendingPrivateCount(alice) == 0
                        && messageCount(alice, BOB, OFFLINE_TEXT) == 1
                        && messageCount(bobAgain, ALICE, OFFLINE_TEXT) == 1;
            }
        }, "Pending offline message flushes without duplicate UI echo", 7000);

        String status = statusForBody(alice, BOB, OFFLINE_TEXT);
        assertTrue(status != null && !"待发送".equals(status),
                "Alice message status should move away from pending after Bob reconnects; actual=" + status);
        assertTrue(visibleHistoryText(alice).contains(OFFLINE_TEXT),
                "Alice should still show the original outgoing message after flush");
        assertTrue(!visibleHistoryText(alice).contains("__VIDEO_CALL__"),
                "Raw protocol text should remain hidden after reconnect");
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

    private static void disconnect(final ChatClient client) throws Exception {
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

    private static void loginAndConnect(ChatClient client, int port, String nick) {
        try {
            Class loginDataClass = Class.forName("com.cncd.ch04.client.ChatClient$LoginData");
            java.lang.reflect.Constructor ctor = loginDataClass.getDeclaredConstructor(
                    String.class, String.class, String.class, String.class);
            ctor.setAccessible(true);
            Object loginData = ctor.newInstance("127.0.0.1", String.valueOf(port), nick, "test-password");
            java.lang.reflect.Method applyLogin = ChatClient.class.getDeclaredMethod("applyLogin", loginDataClass);
            applyLogin.setAccessible(true);
            applyLogin.invoke(client, loginData);
            java.lang.reflect.Method connect = ChatClient.class.getDeclaredMethod("connect");
            connect.setAccessible(true);
            connect.invoke(client);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
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

    private static int pendingPrivateCount(ChatClient client) throws Exception {
        Map map = (Map) field(client, "pendingPrivateMessages");
        return map.size();
    }

    private static String previewFor(ChatClient client, String conversationKey) throws Exception {
        Map meta = (Map) field(client, "conversationMeta");
        Object item = meta.get(conversationKey);
        return item == null ? "" : stringField(item, "preview");
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

    private static int messageCount(ChatClient client, String conversationKey, String body) throws Exception {
        List messages = messagesFor(client, conversationKey);
        int count = 0;
        for(int i=0;i<messages.size();i++) {
            Object message = messages.get(i);
            if(body.equals(stringField(message, "body"))) count++;
        }
        return count;
    }

    private static String statusForBody(ChatClient client, String conversationKey, String body) throws Exception {
        List messages = messagesFor(client, conversationKey);
        for(int i=0;i<messages.size();i++) {
            Object message = messages.get(i);
            if(body.equals(stringField(message, "body"))) {
                return stringField(message, "deliveryStatus");
            }
        }
        return null;
    }

    private static List messagesFor(ChatClient client, String conversationKey) throws Exception {
        Map messages = (Map) field(client, "conversationMessages");
        List list = (List) messages.get(conversationKey);
        assertTrue(list != null, "No conversation messages for " + conversationKey);
        return list;
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

    private static String encodeToken(String value) {
        return java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static void captureAlice(String fileName) throws Exception {
        final BufferedImage[] result = new BufferedImage[1];
        onEdt(new Runnable() {
            public void run() {
                Component content = alice.getContentPane();
                Dimension size = content.getSize();
                int margin = 28;
                int titleHeight = 30;
                BufferedImage image = new BufferedImage(size.width + margin * 2,
                        size.height + margin * 2 + titleHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = image.createGraphics();
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, image.getWidth(), image.getHeight());
                g.setColor(new Color(238, 238, 238));
                g.fillRect(margin, margin, size.width, titleHeight);
                g.setColor(new Color(185, 185, 185));
                g.drawRect(margin, margin, size.width - 1, titleHeight + size.height - 1);
                g.setColor(Color.BLACK);
                g.setFont(new Font("Dialog", Font.BOLD, 13));
                g.drawString("Alice sends to offline Bob", margin + 10, margin + 20);
                Graphics2D copy = (Graphics2D) g.create(margin, margin + titleHeight,
                        size.width, size.height);
                content.printAll(copy);
                copy.dispose();
                g.dispose();
                result[0] = image;
            }
        });
        ImageIO.write(result[0], "png", SCREENSHOT_DIR.resolve(fileName).toFile());
        System.out.println("Captured " + SCREENSHOT_DIR.resolve(fileName).toAbsolutePath());
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

    private static void cleanClientData(String user) throws Exception {
        Path dir = clientDataRoot().resolve(user);
        deleteRecursively(dir);
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
        if(SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeAndWait(runnable);
        }
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

    private static interface Condition {
        boolean ok() throws Exception;
    }
}
