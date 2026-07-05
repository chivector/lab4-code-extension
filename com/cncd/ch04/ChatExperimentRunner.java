package com.cncd.ch04;

import com.cncd.ch04.client.ChatClient;
import com.cncd.ch04.server.BroadcastCommandParser;
import com.cncd.ch04.server.FileDataSource;
import com.cncd.ch04.server.MainServer;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ChatExperimentRunner {
    private static ChatClient alice;
    private static ChatClient bob;

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 4567;
        Path screenshots = Paths.get("screenshots");
        Files.createDirectories(screenshots);

        MainServer.ds = new FileDataSource();
        MainServer.cp = new BroadcastCommandParser();
        MainServer.cp.setDataSource(MainServer.ds);
        new MainServer(port);
        Thread.sleep(800);

        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                alice = createClient("Alice client", 40, 80);
                alice.demoConnect("127.0.0.1", port, "Alice");
                alice.demoAddFriend("Bob");
            }
        });
        Thread.sleep(1200);
        capture("01_friend_waiting.png");

        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                bob = createClient("Bob client", 660, 80);
                bob.demoConnect("127.0.0.1", port, "Bob");
            }
        });
        Thread.sleep(1200);
        capture("02_online_list_friend_notice.png");

        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                alice.demoBroadcast("大家好，这是实验四中文群聊测试。");
                bob.demoBroadcast("收到，Bob 端中文显示正常。");
            }
        });
        Thread.sleep(1200);
        capture("03_group_chat_utf8.png");

        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                alice.demoPrivate("Bob", "这是一条只发给 Bob 的私聊消息。");
                bob.demoPrivate("Alice", "私聊收到，链路正常。");
            }
        });
        Thread.sleep(1200);
        capture("04_private_chat.png");

        Path sampleFile = Paths.get("实验四发送测试.txt");
        Files.write(sampleFile,
                ("实验四聊天工具文件传输测试\r\n发送方: Alice\r\n接收方: Bob\r\n")
                        .getBytes(StandardCharsets.UTF_8));
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                alice.demoSendFile("Bob", sampleFile.toFile());
            }
        });
        Thread.sleep(1200);
        capture("05_file_transfer.png");

        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                alice.demoCommand("/users");
                alice.demoCommand("/stats");
                alice.demoCommand("/whoami");
            }
        });
        Thread.sleep(1200);
        capture("06_command_extensions.png");

        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                alice.demoCommand("/help");
                alice.demoCommand("/log");
                alice.demoBroadcast("这是一条广告测试消息，服务器应当过滤。");
            }
        });
        Thread.sleep(1200);
        capture("07_help_log_filter.png");

        System.out.println("Screenshots saved to " + screenshots.toAbsolutePath());
        Thread.sleep(500);
        System.exit(0);
    }

    private static ChatClient createClient(String title, int x, int y) {
        ChatClient client = new ChatClient();
        client.setTitle(title);
        client.setSize(920, 640);
        client.setMinimumSize(new Dimension(920, 600));
        client.setLocation(x, y);
        client.setDefaultCloseOperation(ChatClient.DISPOSE_ON_CLOSE);
        client.setVisible(true);
        return client;
    }

    private static void capture(String fileName) throws Exception {
        BufferedImage image = renderClients();
        ImageIO.write(image, "png", new File("screenshots", fileName));
        System.out.println("Captured " + fileName);
    }

    private static BufferedImage renderClients() throws Exception {
        final BufferedImage[] result = new BufferedImage[1];
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                Component left = alice.getContentPane();
                Component right = bob == null ? null : bob.getContentPane();
                Dimension leftSize = left.getSize();
                Dimension rightSize = right == null ? new Dimension(0, 0) : right.getSize();
                int margin = 24;
                int gap = right == null ? 0 : 28;
                int titleHeight = 28;
                int width = margin * 2 + leftSize.width + gap + rightSize.width;
                int height = margin * 2 + titleHeight
                        + Math.max(leftSize.height, rightSize.height);
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = image.createGraphics();
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, width, height);
                paintClient(g, left, margin, margin, "Alice client");
                if(right != null) {
                    paintClient(g, right, margin + leftSize.width + gap, margin, "Bob client");
                }
                g.dispose();
                result[0] = image;
            }
        });
        return result[0];
    }

    private static void paintClient(Graphics2D g, Component content, int x, int y, String title) {
        Dimension size = content.getSize();
        int titleHeight = 28;
        g.setColor(new Color(238, 238, 238));
        g.fillRect(x, y, size.width, titleHeight);
        g.setColor(new Color(185, 185, 185));
        g.drawRect(x, y, size.width - 1, titleHeight + size.height - 1);
        g.setColor(Color.BLACK);
        g.setFont(new Font("Dialog", Font.BOLD, 13));
        g.drawString(title, x + 10, y + 19);
        Graphics2D copy = (Graphics2D) g.create(x, y + titleHeight, size.width, size.height);
        content.printAll(copy);
        copy.dispose();
    }
}
