package com.cncd.ch04.client;

import javax.swing.*;
import javax.swing.border.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ChatClient extends JFrame implements KeyListener, ActionListener, FocusListener {
    public static final String appName = "CNCD Chat";
    public static final String serverText = "127.0.0.1";
    public static final String portText = "3500";
    public static final String nickText = "YourName";
    public static final String USERS_PREFIX = "__USERS__|";
    public static final String FILE_PREFIX = "__FILE__|";
    public static final String SERVER_MOMENT_ITEM_PREFIX = "__SERVER_MOMENT__|";
    private static final String FRIEND_REQUEST_PREFIX = "__FRIEND_REQ__|";
    private static final String FRIEND_ACCEPT_PREFIX = "__FRIEND_ACCEPT__|";
    private static final String FRIEND_REJECT_PREFIX = "__FRIEND_REJECT__|";
    private static final String GROUP_INVITE_PREFIX = "__GROUP_INVITE__|";
    private static final String GROUP_MESSAGE_PREFIX = "__GROUP_MSG__|";
    private static final String MOMENT_SYNC_REQUEST_PREFIX = "__MOMENT_REQ__|";
    private static final String MOMENT_SYNC_ITEM_PREFIX = "__MOMENT_ITEM__|";
    private static final String MOMENT_DELETE_PREFIX = "__MOMENT_DELETE__|";

    private static final long MAX_FILE_BYTES = 5L * 1024L * 1024L;
    private static final String ACCOUNT_FILE = "accounts.properties";
    private static final String LOGIN_PREF_FILE = "login.properties";
    private static final String MOMENTS_FILE = "moments.txt";
    private static final String MOMENT_RECORD_VERSION = "M2";
    private static final int MOMENT_TEXT_LIMIT = 300;
    private static final String BROADCAST_CHAT = "广播";
    private static final String GROUP_LABEL_PREFIX = "群聊 · ";
    private static final Color APP_BACKGROUND = new Color(242, 245, 249);
    private static final Color SIDEBAR_BACKGROUND = new Color(235, 240, 247);
    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color SURFACE = Color.WHITE;
    private static final Color SURFACE_SOFT = new Color(248, 250, 252);
    private static final Color CHAT_BACKGROUND = new Color(246, 248, 251);
    private static final Color PRIMARY = new Color(37, 99, 235);
    private static final Color PRIMARY_DARK = new Color(29, 78, 216);
    private static final Color PRIMARY_LIGHT = new Color(219, 234, 254);
    private static final Color PRIMARY_SOFT = new Color(239, 246, 255);
    private static final Color BORDER = new Color(215, 224, 236);
    private static final Color BORDER_LIGHT = new Color(229, 235, 245);
    private static final Color TEXT = new Color(30, 41, 59);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color SOFT_MUTED = new Color(148, 163, 184);
    private static final Color SUCCESS = new Color(22, 163, 74);
    private static final Color WARNING = new Color(217, 119, 6);
    private static final Color DANGER = new Color(220, 38, 38);
    private static final Color INCOMING_BUBBLE = Color.WHITE;
    private static final Color OUTGOING_BUBBLE = new Color(218, 235, 255);
    private static final int RADIUS_SM = 10;
    private static final int RADIUS_MD = 14;
    private static final int RADIUS_LG = 18;
    private static final int RADIUS_XL = 24;
    private static final int SPACE_XS = 4;
    private static final int SPACE_SM = 8;
    private static final int SPACE_MD = 12;
    private static final int SPACE_LG = 16;
    private static final int SPACE_XL = 20;
    private static final int SIDEBAR_WIDTH = 304;
    private static final int BUTTON_HEIGHT = 36;
    private static final int INPUT_HEIGHT = 38;
    private static final Font UI_FONT = new Font("Microsoft YaHei UI", Font.PLAIN, 13);
    private static final Font UI_FONT_BOLD = new Font("Microsoft YaHei UI", Font.BOLD, 13);
    private static final Font UI_FONT_SMALL = new Font("Microsoft YaHei UI", Font.PLAIN, 12);
    private static final Font TITLE_FONT = new Font("Microsoft YaHei UI", Font.BOLD, 17);
    private static final Font PAGE_TITLE_FONT = new Font("Microsoft YaHei UI", Font.BOLD, 22);
    private static final Font EMOJI_FONT = new Font("Segoe UI Emoji", Font.PLAIN, 18);
    private static final String[] QUICK_EMOJIS = {
            "\uD83D\uDE00", "\uD83D\uDE04", "\uD83D\uDE02", "\uD83D\uDE05",
            "\uD83D\uDE0A", "\uD83D\uDE0D", "\uD83D\uDE18", "\uD83D\uDE0E",
            "\uD83E\uDD14", "\uD83D\uDE34", "\uD83D\uDE22", "\uD83D\uDE2D",
            "\uD83D\uDE21", "\uD83D\uDC4D", "\uD83D\uDC4F", "\uD83D\uDE4F",
            "\uD83D\uDCAA", "\uD83D\uDC4C", "\uD83E\uDD1D", "\u2764\uFE0F",
            "\uD83D\uDC94", "\uD83C\uDF89", "\uD83D\uDD25", "\u2728",
            "\uD83C\uDF1F", "\uD83C\uDF81", "\u2615", "\uD83C\uDF70",
            "\uD83C\uDF39", "\uD83E\uDD73", "\uD83D\uDE0B", "\uD83D\uDE44"
    };

    JPanel northPanel, southPanel, eastPanel;
    JTextField txtHost, txtPort, txtNick;
    JTextArea msgWindow;
    JButton buttonConnect, buttonSend, buttonRefresh, buttonAddFriend, buttonFile, buttonEmoji, buttonLog;
    JButton buttonCreateGroup;
    JButton buttonProfile, buttonMoments;
    JLabel statusLabel, conversationTitleLabel, conversationSubtitleLabel;
    JLabel sidebarNameLabel, sidebarSignatureLabel;
    JPanel attachmentPanel;
    JLabel attachmentNameLabel, attachmentMetaLabel;
    JButton buttonRemoveAttachment;
    AvatarView conversationAvatar, sidebarAvatar;
    JTextField conversationSearchField;
    JPanel conversationListPanel;
    JList<String> onlineList;
    DefaultListModel<String> userModel;
    JScrollPane sc;
    ClientKernel ck;
    ClientHistory historyWindow;
    JPopupMenu emojiPopup;
    private String currentUser = "";
    private Properties currentProfile = new Properties();
    private Path currentUserDir;
    private String selectedChatTarget = null;
    private String selectedGroupName = null;
    private int hoveredConversationIndex = -1;
    private File pendingFile = null;
    private String lastMsg = "";
    private Set<String> friends = new HashSet<String>();
    private Map<String, Set<String>> chatGroups = new LinkedHashMap<String, Set<String>>();
    private Set<String> sentFriendRequests = new HashSet<String>();
    private Set<String> incomingFriendRequests = new HashSet<String>();
    private Set<String> visibleUsers = new HashSet<String>();
    private MomentsDialog activeMomentsDialog;
    private Path logFile;
    private DateTimeFormatter displayTime = DateTimeFormatter.ofPattern("HH:mm:ss");
    private DateTimeFormatter logTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private enum MessageKind {
        INCOMING,
        OUTGOING,
        SYSTEM
    }

    private static class ChatMessage {
        MessageKind kind;
        String sender;
        String body;
        String time;
        boolean fileMessage = false;
        String fileName;
        long fileSize;
        byte[] fileData;
        File savedFile;

        ChatMessage(MessageKind kind, String sender, String body, String time) {
            this.kind = kind;
            this.sender = sender;
            this.body = body;
            this.time = time;
        }

        static ChatMessage file(MessageKind kind, String sender, String fileName,
                long fileSize, byte[] fileData, String time) {
            ChatMessage message = new ChatMessage(kind, sender, fileName, time);
            message.fileMessage = true;
            message.fileName = fileName;
            message.fileSize = fileSize;
            message.fileData = fileData;
            return message;
        }
    }

    private static class LoginData {
        String host;
        String port;
        String username;
        String password;

        LoginData(String host, String port, String username, String password) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
        }
    }

    private static class LoginPreference {
        String host;
        String port;
        String username;

        LoginPreference(String host, String port, String username) {
            this.host = host;
            this.port = port;
            this.username = username;
        }
    }

    private static class Moment {
        String id;
        String time;
        String author;
        String text;
        Set<String> likes = new LinkedHashSet<String>();
        java.util.List<MomentComment> comments = new ArrayList<MomentComment>();

        Moment(String id, String time, String author, String text) {
            this.id = id;
            this.time = time;
            this.author = author;
            this.text = text;
        }
    }

    private static class MomentComment {
        String time;
        String author;
        String text;

        MomentComment(String time, String author, String text) {
            this.time = time;
            this.author = author;
            this.text = text;
        }
    }

    /** Creates a new instance of Class */
    public ChatClient() {
        uiInit();
        txtHost.setText("127.0.0.1");
        txtPort.setText("3500");
    }

    public void uiInit() {
        setLayout(new BorderLayout());
        setBackground(APP_BACKGROUND);
        getContentPane().setBackground(APP_BACKGROUND);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(APP_BACKGROUND);
        setContentPane(rootPanel);

        rootPanel.add(createConnectionPanel(), BorderLayout.NORTH);
        rootPanel.add(createMainPanel(), BorderLayout.CENTER);
    }

    private JPanel createConnectionPanel() {
        northPanel = new JPanel(new BorderLayout());
        northPanel.setBackground(SURFACE);
        northPanel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER_LIGHT),
                pad(12, 18, 12, 18)));

        JLabel appTitle = new JLabel("CNCD Chat");
        appTitle.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 19));
        appTitle.setForeground(TEXT);
        JLabel appSubTitle = new JLabel("现代桌面聊天实验");
        appSubTitle.setFont(UI_FONT_SMALL);
        appSubTitle.setForeground(MUTED);
        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.add(appTitle);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(appSubTitle);
        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.X_AXIS));
        brand.add(new AvatarView("C", false));
        brand.add(Box.createHorizontalStrut(SPACE_MD));
        brand.add(titleBlock);

        txtHost = createTextField(ChatClient.serverText, 10);
        txtPort = createTextField(ChatClient.portText, 5);
        txtNick = createTextField(ChatClient.nickText, 10);
        buttonConnect = createButton("重连", false);
        buttonConnect.setPreferredSize(new Dimension(68, BUTTON_HEIGHT));
        statusLabel = new JLabel("未连接");
        statusLabel.setFont(UI_FONT_SMALL);
        statusLabel.setForeground(MUTED);

        buttonConnect.addActionListener(this);
        buttonConnect.addKeyListener(this);

        JPanel statusBlock = new JPanel();
        statusBlock.setOpaque(false);
        statusBlock.setLayout(new BoxLayout(statusBlock, BoxLayout.X_AXIS));
        BubblePanel statusPill = createPillPanel(SURFACE_SOFT, BORDER_LIGHT);
        statusPill.setLayout(new BorderLayout());
        statusPill.add(statusLabel, BorderLayout.CENTER);
        statusBlock.add(statusPill);
        statusBlock.add(Box.createHorizontalStrut(SPACE_SM));
        statusBlock.add(buttonConnect);

        northPanel.add(brand, BorderLayout.WEST);
        northPanel.add(statusBlock, BorderLayout.EAST);
        return northPanel;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(APP_BACKGROUND);
        mainPanel.add(createChatPanel(), BorderLayout.CENTER);
        mainPanel.add(createConversationPanel(), BorderLayout.WEST);
        return mainPanel;
    }

    private JPanel createConversationPanel() {
        eastPanel = new JPanel(new BorderLayout(0, SPACE_MD));
        eastPanel.setBackground(SIDEBAR_BACKGROUND);
        eastPanel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 0, 1, BORDER_LIGHT),
                pad(SPACE_LG, SPACE_MD, SPACE_MD, SPACE_MD)));
        eastPanel.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));

        JPanel topPanel = new JPanel(new BorderLayout(0, 12));
        topPanel.setOpaque(false);
        topPanel.add(createSidebarAccountPanel(), BorderLayout.NORTH);
        topPanel.add(createConversationSearchPanel(), BorderLayout.SOUTH);
        eastPanel.add(topPanel, BorderLayout.NORTH);

        userModel = new DefaultListModel<String>();
        userModel.addElement(BROADCAST_CHAT);
        onlineList = new JList<String>(userModel);
        onlineList.setFont(UI_FONT_BOLD);
        onlineList.setFixedCellHeight(64);
        onlineList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        onlineList.setSelectionBackground(PRIMARY_LIGHT);
        onlineList.setSelectionForeground(TEXT);
        onlineList.setBackground(SIDEBAR_BACKGROUND);
        onlineList.setBorder(pad(SPACE_XS, 0, SPACE_XS, 0));
        onlineList.setCellRenderer(new ConversationRenderer());
        onlineList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                if(!e.getValueIsAdjusting()) updateSelectedConversation();
            }
        });
        onlineList.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                int index = onlineList.locationToIndex(e.getPoint());
                if(index != hoveredConversationIndex) {
                    hoveredConversationIndex = index;
                    onlineList.repaint();
                }
            }
        });
        onlineList.addMouseListener(new MouseAdapter() {
            public void mouseExited(MouseEvent e) {
                hoveredConversationIndex = -1;
                onlineList.repaint();
            }
        });
        onlineList.setSelectedIndex(0);

        JScrollPane userScroll = new JScrollPane(onlineList);
        userScroll.setBorder(null);
        userScroll.getViewport().setBackground(SIDEBAR_BACKGROUND);
        conversationListPanel = new JPanel(new CardLayout());
        conversationListPanel.setOpaque(false);
        conversationListPanel.add(userScroll, "list");
        conversationListPanel.add(createEmptyConversationPanel(), "empty");
        eastPanel.add(conversationListPanel, BorderLayout.CENTER);

        JPanel tools = new JPanel(new GridLayout(0, 2, SPACE_SM, SPACE_SM));
        tools.setOpaque(false);
        buttonRefresh = createToolButton("刷新");
        buttonAddFriend = createToolButton("好友");
        buttonCreateGroup = createToolButton("建群");
        buttonProfile = createToolButton("资料");
        buttonMoments = createToolButton("朋友圈");
        buttonLog = createToolButton("记录");
        buttonRefresh.addActionListener(this);
        buttonAddFriend.addActionListener(this);
        buttonCreateGroup.addActionListener(this);
        buttonProfile.addActionListener(this);
        buttonMoments.addActionListener(this);
        buttonLog.addActionListener(this);
        tools.add(buttonRefresh);
        tools.add(buttonAddFriend);
        tools.add(buttonCreateGroup);
        tools.add(buttonProfile);
        tools.add(buttonMoments);
        tools.add(buttonLog);
        eastPanel.add(tools, BorderLayout.SOUTH);
        return eastPanel;
    }

    private JPanel createConversationSearchPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = createSectionTitle("会话");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        conversationSearchField = new PromptTextField("", 14, "搜索好友、群聊、广播");
        styleTextField(conversationSearchField);
        conversationSearchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, INPUT_HEIGHT));
        conversationSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterConversations(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterConversations(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterConversations(); }
        });

        panel.add(title);
        panel.add(Box.createVerticalStrut(SPACE_SM));
        panel.add(conversationSearchField);
        return panel;
    }

    private JPanel createEmptyConversationPanel() {
        return createEmptyState("没有匹配的会话", "换个关键词试试");
    }

    private JPanel createSidebarAccountPanel() {
        JPanel accountPanel = createCardPanel(SPACE_MD);
        accountPanel.setLayout(new BorderLayout(SPACE_MD, 0));

        sidebarAvatar = new AvatarView("?", false);
        sidebarNameLabel = new JLabel("未登录");
        sidebarNameLabel.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 16));
        sidebarNameLabel.setForeground(TEXT);
        sidebarSignatureLabel = new JLabel("请先登录账号");
        sidebarSignatureLabel.setFont(UI_FONT_SMALL);
        sidebarSignatureLabel.setForeground(MUTED);
        JLabel onlineBadge = new JLabel("本地账号");
        onlineBadge.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
        onlineBadge.setForeground(PRIMARY_DARK);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(sidebarNameLabel);
        textPanel.add(Box.createVerticalStrut(SPACE_XS));
        textPanel.add(sidebarSignatureLabel);
        textPanel.add(Box.createVerticalStrut(SPACE_XS));
        textPanel.add(onlineBadge);

        accountPanel.add(sidebarAvatar, BorderLayout.WEST);
        accountPanel.add(textPanel, BorderLayout.CENTER);
        return accountPanel;
    }

    private void refreshSidebarProfile() {
        if(sidebarAvatar != null) sidebarAvatar.setAvatar(currentUser == null || currentUser.length() == 0 ? "?" : currentUser, false);
        if(sidebarNameLabel != null) sidebarNameLabel.setText(currentProfile.getProperty("displayName", currentUser));
        if(sidebarSignatureLabel != null) {
            String signature = currentProfile.getProperty("signature", "");
            if(signature.length() > 18) signature = signature.substring(0, 18) + "...";
            sidebarSignatureLabel.setText(signature.length() == 0 ? "还没有个性签名" : signature);
        }
    }

    private JPanel createChatPanel() {
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBackground(CHAT_BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SURFACE);
        header.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER_LIGHT),
                pad(SPACE_LG, SPACE_XL, SPACE_LG, SPACE_XL)));
        JPanel identity = new JPanel();
        identity.setOpaque(false);
        identity.setLayout(new BoxLayout(identity, BoxLayout.X_AXIS));
        conversationAvatar = new AvatarView(BROADCAST_CHAT, false);
        identity.add(conversationAvatar);
        identity.add(Box.createHorizontalStrut(10));
        JPanel textBlock = new JPanel();
        textBlock.setOpaque(false);
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));
        conversationTitleLabel = new JLabel(BROADCAST_CHAT);
        conversationTitleLabel.setFont(TITLE_FONT);
        conversationTitleLabel.setForeground(TEXT);
        conversationSubtitleLabel = new JLabel("消息将发送给所有在线用户");
        conversationSubtitleLabel.setFont(UI_FONT_SMALL);
        conversationSubtitleLabel.setForeground(MUTED);
        textBlock.add(conversationTitleLabel);
        textBlock.add(Box.createVerticalStrut(SPACE_XS));
        textBlock.add(conversationSubtitleLabel);
        identity.add(textBlock);
        header.add(identity, BorderLayout.WEST);

        BubblePanel statePill = createPillPanel(PRIMARY_SOFT, BORDER_LIGHT);
        statePill.setLayout(new BorderLayout());
        JLabel stateText = createHintLabel("消息同步中");
        stateText.setForeground(PRIMARY_DARK);
        statePill.add(stateText, BorderLayout.CENTER);
        header.add(statePill, BorderLayout.EAST);

        chatPanel.add(header, BorderLayout.NORTH);
        chatPanel.add(createHistoryPanel(), BorderLayout.CENTER);
        chatPanel.add(createInputPanel(), BorderLayout.SOUTH);
        return chatPanel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CHAT_BACKGROUND);
        panel.setBorder(pad(SPACE_LG, SPACE_XL, SPACE_LG, SPACE_XL));
        historyWindow = new ClientHistory();
        sc = createModernScrollPane(historyWindow, CHAT_BACKGROUND);
        sc.setAutoscrolls(true);
        panel.add(sc, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createInputPanel() {
        southPanel = new JPanel(new BorderLayout(SPACE_SM, SPACE_SM));
        southPanel.setBackground(SURFACE);
        southPanel.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDER_LIGHT),
                pad(SPACE_MD, SPACE_XL, SPACE_MD, SPACE_XL)));

        attachmentPanel = createAttachmentPanel();
        msgWindow = new PromptTextArea("输入消息，Enter 发送，Shift+Enter 换行");
        styleTextArea(msgWindow);
        msgWindow.setRows(3);
        msgWindow.setToolTipText("Enter 发送，Shift+Enter 换行，↑ 取回上一条消息");
        msgWindow.addKeyListener(this);
        msgWindow.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateSendButtonState(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateSendButtonState(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateSendButtonState(); }
        });
        installMessageShortcuts();
        buttonSend = createButton("发送", true);
        buttonSend.setPreferredSize(new Dimension(76, 42));
        buttonSend.setToolTipText("发送消息 Enter");
        buttonEmoji = createButton("\uD83D\uDE0A", false);
        buttonEmoji.setFont(EMOJI_FONT);
        buttonEmoji.setToolTipText("插入表情");
        buttonEmoji.setPreferredSize(new Dimension(42, 42));
        buttonFile = createButton("+", false);
        buttonFile.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 18));
        buttonFile.setToolTipText("选择本地文件");
        buttonFile.setPreferredSize(new Dimension(42, 42));
        buttonSend.addActionListener(this);
        buttonEmoji.addActionListener(this);
        buttonFile.addActionListener(this);
        buttonFile.setEnabled(true);
        buttonSend.setEnabled(false);

        JPanel inputRow = new JPanel(new BorderLayout(SPACE_SM, 0));
        inputRow.setOpaque(false);
        JPanel inputTools = new JPanel(new GridLayout(1, 2, SPACE_SM, 0));
        inputTools.setOpaque(false);
        inputTools.add(buttonEmoji);
        inputTools.add(buttonFile);
        inputRow.add(inputTools, BorderLayout.WEST);
        JScrollPane messageScroll = new JScrollPane(msgWindow);
        messageScroll.setBorder(new RoundedBorder(BORDER, RADIUS_LG));
        messageScroll.getViewport().setBackground(Color.WHITE);
        inputRow.add(messageScroll, BorderLayout.CENTER);
        inputRow.add(buttonSend, BorderLayout.EAST);

        JLabel hint = createHintLabel("Enter 发送 · Shift+Enter 换行 · ↑ 取回上一条");

        southPanel.add(attachmentPanel, BorderLayout.NORTH);
        southPanel.add(inputRow, BorderLayout.CENTER);
        southPanel.add(hint, BorderLayout.SOUTH);
        return southPanel;
    }

    private JPanel createAttachmentPanel() {
        JPanel panel = new BubblePanel(PRIMARY_SOFT, new Color(147, 197, 253), RADIUS_MD);
        panel.setLayout(new BorderLayout(SPACE_MD, 0));
        panel.setBorder(pad(SPACE_SM, SPACE_MD, SPACE_SM, SPACE_MD));
        panel.setVisible(false);

        JLabel icon = new JLabel("文件");
        icon.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 12));
        icon.setForeground(PRIMARY_DARK);
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        icon.setPreferredSize(new Dimension(48, 30));
        icon.setBorder(new RoundedBorder(new Color(147, 197, 253), RADIUS_SM));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        attachmentNameLabel = new JLabel("");
        attachmentNameLabel.setFont(UI_FONT_BOLD);
        attachmentNameLabel.setForeground(TEXT);
        attachmentMetaLabel = new JLabel("");
        attachmentMetaLabel.setFont(UI_FONT_SMALL);
        attachmentMetaLabel.setForeground(MUTED);
        textPanel.add(attachmentNameLabel);
        textPanel.add(Box.createVerticalStrut(SPACE_XS));
        textPanel.add(attachmentMetaLabel);

        buttonRemoveAttachment = createButton("取消", false);
        buttonRemoveAttachment.setPreferredSize(new Dimension(68, 32));
        buttonRemoveAttachment.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearAttachment();
            }
        });

        panel.add(icon, BorderLayout.WEST);
        panel.add(textPanel, BorderLayout.CENTER);
        panel.add(buttonRemoveAttachment, BorderLayout.EAST);
        return panel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UI_FONT_BOLD);
        label.setForeground(TEXT);
        return label;
    }

    private JLabel createSectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UI_FONT_BOLD);
        label.setForeground(MUTED);
        return label;
    }

    private JLabel createHintLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UI_FONT_SMALL);
        label.setForeground(MUTED);
        return label;
    }

    private BubblePanel createCardPanel(int padding) {
        BubblePanel panel = new BubblePanel(SURFACE, BORDER_LIGHT, RADIUS_LG);
        panel.setBorder(pad(padding));
        return panel;
    }

    private BubblePanel createPillPanel(Color fill, Color stroke) {
        BubblePanel panel = new BubblePanel(fill, stroke, RADIUS_XL);
        panel.setBorder(new EmptyBorder(5, 10, 5, 10));
        return panel;
    }

    private Border pad(int padding) {
        return new EmptyBorder(padding, padding, padding, padding);
    }

    private Border pad(int top, int left, int bottom, int right) {
        return new EmptyBorder(top, left, bottom, right);
    }

    private JScrollPane createModernScrollPane(Component component, Color background) {
        JScrollPane scroll = new JScrollPane(component);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(background);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getHorizontalScrollBar().setUnitIncrement(18);
        return scroll;
    }

    private JPanel createEmptyState(String title, String detail) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        JPanel card = createCardPanel(SPACE_XL);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UI_FONT_BOLD);
        titleLabel.setForeground(TEXT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel detailLabel = new JLabel(detail);
        detailLabel.setFont(UI_FONT_SMALL);
        detailLabel.setForeground(MUTED);
        detailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(SPACE_SM));
        card.add(detailLabel);
        panel.add(card);
        return panel;
    }

    private JTextField createTextField(String text, int columns) {
        JTextField field = new JTextField(text, columns);
        styleTextField(field);
        return field;
    }

    private void styleTextField(JTextField field) {
        field.setFont(UI_FONT);
        field.setForeground(TEXT);
        field.setBackground(Color.WHITE);
        field.setCaretColor(PRIMARY_DARK);
        field.setBorder(new CompoundBorder(new RoundedBorder(BORDER, RADIUS_MD), pad(8, 11, 8, 11)));
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, INPUT_HEIGHT));
    }

    private void styleTextArea(JTextArea area) {
        area.setFont(UI_FONT);
        area.setForeground(TEXT);
        area.setBackground(Color.WHITE);
        area.setCaretColor(PRIMARY_DARK);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(pad(10, 12, 10, 12));
    }

    private void installMessageShortcuts() {
        InputMap inputMap = msgWindow.getInputMap();
        ActionMap actionMap = msgWindow.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "sendMessage");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK), "insert-break");
        actionMap.put("sendMessage", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                send();
            }
        });
    }

    private JButton createButton(String text, boolean primary) {
        return new StyledButton(text, primary);
    }

    private JButton createToolButton(String text) {
        JButton button = createButton(text, false);
        button.setPreferredSize(new Dimension(0, BUTTON_HEIGHT));
        button.setFont(UI_FONT_BOLD);
        return button;
    }

    private void showEmojiPicker() {
        if(emojiPopup == null) emojiPopup = createEmojiPopup();
        Dimension size = emojiPopup.getPreferredSize();
        emojiPopup.show(buttonEmoji, 0, -size.height - SPACE_SM);
    }

    private JPopupMenu createEmojiPopup() {
        final JPopupMenu popup = new JPopupMenu();
        popup.setBorder(new CompoundBorder(
                new RoundedBorder(BORDER, RADIUS_MD),
                pad(SPACE_SM, SPACE_SM, SPACE_SM, SPACE_SM)));
        JPanel grid = new JPanel(new GridLayout(0, 8, SPACE_XS, SPACE_XS));
        grid.setBackground(Color.WHITE);
        for(int i=0;i<QUICK_EMOJIS.length;i++) {
            final String emoji = QUICK_EMOJIS[i];
            JButton item = new JButton(emoji);
            item.setFont(EMOJI_FONT);
            item.setFocusPainted(false);
            item.setContentAreaFilled(false);
            item.setBorder(new RoundedBorder(BORDER_LIGHT, RADIUS_SM));
            item.setPreferredSize(new Dimension(34, 34));
            item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    insertEmoji(emoji);
                    popup.setVisible(false);
                }
            });
            grid.add(item);
        }
        popup.add(grid);
        return popup;
    }

    private void insertEmoji(String emoji) {
        if(msgWindow == null || emoji == null) return;
        msgWindow.requestFocusInWindow();
        msgWindow.replaceSelection(emoji);
        updateSendButtonState();
    }

    public static void main(String args[]) {
        ChatClient client = new ChatClient();
        client.setTitle(client.appName);
        client.setSize(1100, 720);
        client.setMinimumSize(new Dimension(920, 600));
        client.setLocationRelativeTo(null);
        client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        LoginData login = client.showLoginDialog(args);
        if(login == null) {
            System.exit(0);
        }
        client.applyLogin(login);
        client.setVisible(true);
        client.connect();
        client.msgWindow.requestFocus();
    }

    private LoginData showLoginDialog(String[] args) {
        LoginPreference preference = loadLoginPreference();
        String host = args.length >= 1 ? args[0] : preference.host;
        String port = args.length >= 2 ? args[1] : preference.port;
        String username = args.length >= 3 ? args[2] : preference.username;
        LoginDialog dialog = new LoginDialog(this, host, port, username);
        dialog.setVisible(true);
        LoginData login = dialog.getLoginData();
        if(login != null) saveLoginPreference(login);
        return login;
    }

    private void addLoginRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent input) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        JLabel textLabel = new JLabel(label);
        textLabel.setFont(UI_FONT_BOLD);
        textLabel.setForeground(MUTED);
        panel.add(textLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(input, gbc);
    }

    private void applyLogin(LoginData login) {
        currentUser = login.username;
        currentUserDir = userDirectory(login.username);
        txtHost.setText(login.host);
        txtPort.setText(login.port);
        txtNick.setText(login.username);
        txtNick.setEditable(false);
        setTitle(appName + " - " + login.username);
        loadUserData();
        refreshSidebarProfile();
        rebuildConversationList(BROADCAST_CHAT);
        initLogFile(login.username);
        loadHistoryPreview();
    }

    private static Path clientDataRoot() {
        return Paths.get(System.getProperty("user.home"), ".mihalychat", "client");
    }

    private static Path accountFile() {
        return clientDataRoot().resolve(ACCOUNT_FILE);
    }

    private static Path loginPreferenceFile() {
        return clientDataRoot().resolve(LOGIN_PREF_FILE);
    }

    private static Path userDirectory(String username) {
        return clientDataRoot().resolve(safeFileName(username));
    }

    private static LoginPreference loadLoginPreference() {
        Properties props = new Properties();
        try {
            Path file = loginPreferenceFile();
            if(Files.exists(file)) {
                try(InputStream in = Files.newInputStream(file)) {
                    props.load(in);
                }
            }
        } catch(Exception e) {
        }
        return new LoginPreference(
                props.getProperty("host", ChatClient.serverText),
                props.getProperty("port", "4567"),
                props.getProperty("username", ""));
    }

    private static void saveLoginPreference(LoginData login) {
        try {
            Files.createDirectories(clientDataRoot());
            Properties props = new Properties();
            props.setProperty("host", login.host == null ? ChatClient.serverText : login.host);
            props.setProperty("port", login.port == null ? "4567" : login.port);
            props.setProperty("username", login.username == null ? "" : login.username);
            try(OutputStream out = Files.newOutputStream(loginPreferenceFile())) {
                props.store(out, "Chat login preference without password");
            }
        } catch(Exception e) {
        }
    }

    private static boolean registerLocalAccount(String username, String password) throws Exception {
        Files.createDirectories(clientDataRoot());
        Properties accounts = loadAccountProperties();
        String key = accountKey(username);
        if(accounts.getProperty(key + ".hash") != null) return false;
        accounts.setProperty(key + ".name", username);
        accounts.setProperty(key + ".hash", hashPassword(username, password));
        saveAccountProperties(accounts);
        Path userDir = userDirectory(username);
        Files.createDirectories(userDir);
        Path profileFile = userDir.resolve("profile.properties");
        if(!Files.exists(profileFile)) {
            Properties profile = new Properties();
            profile.setProperty("displayName", username);
            profile.setProperty("signature", "这个人还没有写个性签名。");
            try(OutputStream out = Files.newOutputStream(profileFile)) {
                profile.store(out, "Chat profile");
            }
        }
        Files.createDirectories(userDir.resolve("logs"));
        return true;
    }

    private static boolean verifyLocalAccount(String username, String password) throws Exception {
        Properties accounts = loadAccountProperties();
        String stored = accounts.getProperty(accountKey(username) + ".hash");
        return stored != null && stored.equals(hashPassword(username, password));
    }

    private static Properties loadAccountProperties() throws Exception {
        Properties props = new Properties();
        Path file = accountFile();
        if(Files.exists(file)) {
            try(InputStream in = Files.newInputStream(file)) {
                props.load(in);
            }
        }
        return props;
    }

    private static void saveAccountProperties(Properties props) throws Exception {
        Files.createDirectories(clientDataRoot());
        try(OutputStream out = Files.newOutputStream(accountFile())) {
            props.store(out, "Chat accounts");
        }
    }

    private static String accountKey(String username) {
        return username.toLowerCase(Locale.ROOT);
    }

    private static String safeFileName(String value) {
        return value.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private static String hashPassword(String username, String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = digest.digest((accountKey(username) + ":" + password).getBytes(StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();
        for(int i=0;i<bytes.length;i++) {
            builder.append(String.format("%02x", bytes[i] & 0xff));
        }
        return builder.toString();
    }

    public void addMsg(String str) {
        String time = LocalDateTime.now().format(displayTime);
        appendLog(str);
        ChatMessage message = parseDisplayMessage(str, time);
        if(message != null) historyWindow.addMessage(message);
    }

    private ChatMessage parseDisplayMessage(String str, String time) {
        String text = stripHtml(str).replace('\u00A0', ' ').trim();
        if(text.length() == 0) return null;

        if(text.startsWith("[private sent to ")) return null;

        if(text.startsWith("[private to ")) {
            int end = text.indexOf(']');
            String body = end >= 0 ? text.substring(end + 1).trim() : text;
            return new ChatMessage(MessageKind.OUTGOING, ownNick(), body, time);
        }

        if(text.startsWith("[private] ")) {
            String payload = text.substring("[private] ".length()).trim();
            int colon = payload.indexOf(':');
            if(colon > 0) {
                String sender = payload.substring(0, colon).trim();
                String body = payload.substring(colon + 1).trim();
                if(handleProtocolMessage(sender, body, time)) return null;
                return new ChatMessage(messageKindFor(sender), sender, body, time);
            }
        }

        if(text.startsWith("[file to ")) {
            int end = text.indexOf(']');
            String body = end >= 0 ? "发送文件 " + text.substring(end + 1).trim() : text;
            return new ChatMessage(MessageKind.OUTGOING, ownNick(), body, time);
        }

        if(text.startsWith("[file from ")) {
            int end = text.indexOf(']');
            String sender = text.substring("[file from ".length(), end > 0 ? end : text.length()).trim();
            String body = end >= 0 ? "收到文件 " + text.substring(end + 1).trim() : text;
            return new ChatMessage(messageKindFor(sender), sender, body, time);
        }

        if(isSystemMessage(text)) {
            return new ChatMessage(MessageKind.SYSTEM, "系统", text, time);
        }

        int colon = text.indexOf(':');
        if(colon > 0) {
            String sender = text.substring(0, colon).trim();
            String body = text.substring(colon + 1).trim();
            if(sender.length() > 0 && sender.indexOf(' ') < 0 && !sender.equalsIgnoreCase("Server")) {
                return new ChatMessage(messageKindFor(sender), sender, body, time);
            }
        }

        return new ChatMessage(MessageKind.SYSTEM, "系统", text, time);
    }

    private boolean isSystemMessage(String text) {
        return text.startsWith("Server:")
                || text.startsWith("connected!")
                || text.startsWith("connect ")
                || text.startsWith("Current Connected Users:")
                || text.startsWith("Available commands:")
                || text.startsWith("Local log:")
                || text.startsWith("Unable to find")
                || text.startsWith("Unknown command")
                || text.startsWith("Invalid Command")
                || text.startsWith("usage:")
                || text.startsWith("Nick ")
                || text.startsWith("User ")
                || text.startsWith("Your ")
                || text.startsWith("Server has been running")
                || text.startsWith("Connected Port:")
                || text.startsWith("File ")
                || text.startsWith("The username")
                || text.startsWith("message blocked")
                || text.startsWith("repeated message blocked");
    }

    private MessageKind messageKindFor(String sender) {
        return sender.equalsIgnoreCase(ownNick()) ? MessageKind.OUTGOING : MessageKind.INCOMING;
    }

    private boolean handleProtocolMessage(String sender, String body, String time) {
        if(handleMomentProtocolMessage(sender, body)) return true;
        if(handleGroupProtocolMessage(sender, body, time)) return true;
        return handleFriendProtocolMessage(sender, body);
    }

    private boolean handleMomentProtocolMessage(String sender, String body) {
        if(body.startsWith(MOMENT_SYNC_REQUEST_PREFIX)) {
            if(friends.contains(sender)) sendOwnMomentsTo(sender);
            return true;
        }
        if(body.startsWith(MOMENT_SYNC_ITEM_PREFIX)) {
            String encoded = body.substring(MOMENT_SYNC_ITEM_PREFIX.length());
            Moment moment = parseModernMoment(decodeToken(encoded));
            if(moment != null && shouldAcceptMoment(sender, moment)) {
                upsertMoment(moment);
                refreshActiveMoments();
            }
            return true;
        }
        if(body.startsWith(MOMENT_DELETE_PREFIX)) {
            String id = decodeToken(body.substring(MOMENT_DELETE_PREFIX.length()));
            if(id.length() > 0) {
                removeMomentById(id);
                refreshActiveMoments();
            }
            return true;
        }
        return false;
    }

    private boolean handleGroupProtocolMessage(String sender, String body, String time) {
        if(body.startsWith(GROUP_INVITE_PREFIX)) {
            String payload = body.substring(GROUP_INVITE_PREFIX.length());
            String[] parts = payload.split("\\|", 2);
            if(parts.length == 2) {
                String groupName = decodeToken(parts[0]);
                Set<String> members = csvToMembers(decodeToken(parts[1]));
                members.add(sender);
                members.add(ownNick());
                addOrUpdateGroup(groupName, members, true);
                rebuildConversationList(groupLabel(groupName));
                addMsg("<font color=\"#3366cc\">" + escapeHtml(sender)
                        + " 已将你拉入群聊：" + escapeHtml(groupName) + "</font>");
            }
            return true;
        }
        if(body.startsWith(GROUP_MESSAGE_PREFIX)) {
            String payload = body.substring(GROUP_MESSAGE_PREFIX.length());
            String[] parts = payload.split("\\|", 2);
            if(parts.length == 2) {
                String groupName = decodeToken(parts[0]);
                String message = decodeToken(parts[1]);
                Set<String> members = chatGroups.get(groupName);
                if(members == null) {
                    members = new LinkedHashSet<String>();
                    members.add(sender);
                    members.add(ownNick());
                    addOrUpdateGroup(groupName, members, true);
                    rebuildConversationList(groupLabel(groupName));
                } else if(!members.contains(sender)) {
                    members.add(sender);
                    saveGroups();
                    if(onlineList != null) onlineList.repaint();
                }
                historyWindow.addMessage(new ChatMessage(MessageKind.INCOMING,
                        sender + " / " + groupName, message, time));
                appendLog("[group " + groupName + " from " + sender + "] " + message);
            }
            return true;
        }
        return false;
    }

    private boolean handleFriendProtocolMessage(String sender, String body) {
        if(body.startsWith(FRIEND_REQUEST_PREFIX)) {
            handleIncomingFriendRequest(sender);
            return true;
        }
        if(body.startsWith(FRIEND_ACCEPT_PREFIX)) {
            sentFriendRequests.remove(sender);
            if(!friends.contains(sender)) {
                addFriend(sender);
            } else {
                addMsg("<font color=\"#3366cc\">" + escapeHtml(sender) + " 已经是你的好友。</font>");
            }
            addMsg("<font color=\"#008000\">" + escapeHtml(sender) + " 已同意你的好友申请。</font>");
            if(onlineList != null) onlineList.repaint();
            return true;
        }
        if(body.startsWith(FRIEND_REJECT_PREFIX)) {
            sentFriendRequests.remove(sender);
            addMsg("<font color=\"#666666\">" + escapeHtml(sender) + " 拒绝了你的好友申请。</font>");
            if(onlineList != null) onlineList.repaint();
            return true;
        }
        return false;
    }

    private void handleIncomingFriendRequest(final String sender) {
        if(sender == null || sender.length() == 0 || sender.equalsIgnoreCase(ownNick())) return;
        if(friends.contains(sender)) {
            sendFriendResponse(sender, true);
            return;
        }
        if(incomingFriendRequests.contains(sender)) return;
        incomingFriendRequests.add(sender);
        addMsg("<font color=\"#3366cc\">收到 " + escapeHtml(sender) + " 的好友申请。</font>");

        int choice = JOptionPane.showConfirmDialog(this,
                sender + " 请求添加你为好友，是否同意？",
                "好友申请",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if(choice == JOptionPane.YES_OPTION) {
            addFriend(sender);
            sendFriendResponse(sender, true);
            addMsg("<font color=\"#008000\">已同意 " + escapeHtml(sender) + " 的好友申请。</font>");
        } else {
            incomingFriendRequests.remove(sender);
            sendFriendResponse(sender, false);
            addMsg("<font color=\"#666666\">已拒绝 " + escapeHtml(sender) + " 的好友申请。</font>");
        }
    }

    private void sendFriendResponse(String target, boolean accepted) {
        if(ck != null && ck.isConnected()) {
            ck.sendMessage("/msg " + target + " "
                    + (accepted ? FRIEND_ACCEPT_PREFIX : FRIEND_REJECT_PREFIX) + ownNick());
        }
    }

    private String ownNick() {
        if(txtNick == null) return "Me";
        String nick = txtNick.getText();
        if(nick == null || nick.trim().length() == 0) return "Me";
        return nick.trim();
    }

    private void connect() {
        try {
            if(ck!=null) ck.dropMe();
            ck = new ClientKernel(txtHost.getText(), Integer.parseInt(txtPort.getText()));
            if(ck.isConnected()) {
                ck.addClient(this);
                ck.setNick(txtNick.getText());
                initLogFile(txtNick.getText());
                setConnectionStatus("已连接 " + txtHost.getText() + ":" + txtPort.getText()
                        + "，本地端口 " + ck.getLocalPort(), SUCCESS);
                setTitle(appName + " - " + txtNick.getText());
                addMsg("<font color=\"#008000\">connected! Local Port:" + ck.getLocalPort() + "</font>");
                refreshUsers();
                uploadOwnMomentsToServer();
            } else {
                setConnectionStatus("连接失败，请检查服务端和端口", DANGER);
                addMsg("<font color=\"#ff0000\">连接失败，请确认服务端已启动、IP 和端口正确。</font>");
            }
        } catch(Exception e) {
            setConnectionStatus("连接错误：" + e.getMessage(), DANGER);
            addMsg("<font color=\"#ff0000\">连接错误：" + escapeHtml(e.getMessage()) + "</font>");
            e.printStackTrace();
        }
    }

    private void setConnectionStatus(String text, Color color) {
        if(statusLabel != null) {
            statusLabel.setText(text);
            statusLabel.setForeground(color);
        }
    }

    private boolean isBroadcastConversation(String value) {
        return BROADCAST_CHAT.equals(value);
    }

    private boolean isGroupConversation(String value) {
        return value != null && value.startsWith(GROUP_LABEL_PREFIX);
    }

    private String groupLabel(String groupName) {
        return GROUP_LABEL_PREFIX + groupName;
    }

    private String groupNameFromLabel(String label) {
        if(!isGroupConversation(label)) return null;
        return label.substring(GROUP_LABEL_PREFIX.length());
    }

    private void updateSelectedConversation() {
        String selected = onlineList == null ? null : onlineList.getSelectedValue();
        if(selected == null) {
            selectedChatTarget = null;
            selectedGroupName = null;
            if(conversationTitleLabel != null) conversationTitleLabel.setText("选择会话");
            if(conversationSubtitleLabel != null) conversationSubtitleLabel.setText("从左侧选择好友、群聊或广播");
            if(conversationAvatar != null) conversationAvatar.setAvatar("?", false);
            if(buttonFile != null) buttonFile.setEnabled(true);
        } else if(isBroadcastConversation(selected)) {
            selectedChatTarget = null;
            selectedGroupName = null;
            if(conversationTitleLabel != null) conversationTitleLabel.setText(BROADCAST_CHAT);
            if(conversationSubtitleLabel != null) conversationSubtitleLabel.setText("消息将广播给所有在线用户");
            if(conversationAvatar != null) conversationAvatar.setAvatar(BROADCAST_CHAT, false);
            if(buttonFile != null) buttonFile.setEnabled(true);
        } else if(isGroupConversation(selected)) {
            selectedChatTarget = null;
            selectedGroupName = groupNameFromLabel(selected);
            Set<String> members = chatGroups.get(selectedGroupName);
            int memberCount = members == null ? 1 : members.size();
            int onlineCount = countOnlineMembers(members);
            if(conversationTitleLabel != null) conversationTitleLabel.setText(selectedGroupName);
            if(conversationSubtitleLabel != null) {
                conversationSubtitleLabel.setText("群聊 · " + memberCount + " 人，"
                        + onlineCount + " 人在线 · 只发送给群成员");
            }
            if(conversationAvatar != null) conversationAvatar.setAvatar(selectedGroupName, false);
            if(buttonFile != null) buttonFile.setEnabled(true);
        } else {
            selectedChatTarget = selected;
            selectedGroupName = null;
            if(conversationTitleLabel != null) conversationTitleLabel.setText(selected);
            boolean online = visibleUsers.contains(selected);
            if(conversationSubtitleLabel != null) {
                conversationSubtitleLabel.setText(online ? "当前为私聊，只发送给 " + selected : "好友离线，暂不能发送消息");
            }
            if(conversationAvatar != null) conversationAvatar.setAvatar(selected, false);
            if(buttonFile != null) buttonFile.setEnabled(true);
        }
        updateAttachmentPreview();
        updateSendButtonState();
        if(msgWindow != null) msgWindow.requestFocusInWindow();
    }

    private String getSelectedPrivateTarget() {
        String selected = onlineList == null ? null : onlineList.getSelectedValue();
        if(isBroadcastConversation(selected) || isGroupConversation(selected)) return null;
        return selected;
    }

    private int countOnlineMembers(Set<String> members) {
        if(members == null) return 0;
        int count = 0;
        Iterator<String> it = members.iterator();
        while(it.hasNext()) {
            String member = it.next();
            if(visibleUsers.contains(member)) count++;
        }
        return count;
    }

    private void send() {
        String toSend = msgWindow.getText();
        boolean hasText = toSend != null && toSend.trim().length() > 0;
        boolean hasFile = pendingFile != null;
        if(!hasText && !hasFile) return;
        if(ck == null || !ck.isConnected()) {
            addMsg("<font color=\"#ff0000\">请先连接服务器。</font>");
            return;
        }
        if(onlineList != null && onlineList.getSelectedValue() == null) {
            addMsg("<font color=\"#ff0000\">请先选择一个会话。</font>");
            return;
        }
        if(hasFile) {
            String target = getSelectedPrivateTarget();
            if(target == null || target.length() == 0) {
                addMsg("<font color=\"#ff0000\">请选择一个联系人后再发送附件。</font>");
                return;
            }
            if(!visibleUsers.contains(target)) {
                addMsg("<font color=\"#ff0000\">对方当前离线，暂不能发送附件。</font>");
                return;
            }
            if(hasText && toSend.startsWith("/")) {
                addMsg("<font color=\"#ff0000\">附件待发送时不能同时输入命令，请先取消附件或单独发送命令。</font>");
                return;
            }
            if(hasText) sendPrivate(target, toSend);
            if(sendFileTo(target, pendingFile)) {
                clearAttachment();
                lastMsg = hasText ? "" + toSend : "";
                msgWindow.setText("");
            }
            return;
        }
        if(toSend.startsWith("/friend ")) {
            requestFriend(toSend.substring("/friend ".length()).trim());
            lastMsg = "" + toSend;
            msgWindow.setText("");
            return;
        }
        if(toSend.equalsIgnoreCase("/log")) {
            showLogPath();
            lastMsg = "" + toSend;
            msgWindow.setText("");
            return;
        }
        if(toSend.equalsIgnoreCase("/clear")) {
            historyWindow.clear();
            lastMsg = "" + toSend;
            msgWindow.setText("");
            return;
        }
        if(toSend.startsWith("/sendfile ")) {
            sendFileCommand(toSend);
            lastMsg = "" + toSend;
            msgWindow.setText("");
            return;
        }
        if(toSend.startsWith("/")) {
            ck.sendMessage(toSend);
        } else if(selectedGroupName != null) {
            if(!sendGroupMessage(selectedGroupName, toSend)) return;
        } else if(selectedChatTarget != null) {
            if(!visibleUsers.contains(selectedChatTarget)) {
                addMsg("<font color=\"#ff0000\">对方当前离线，暂不能发送消息。</font>");
                return;
            }
            sendPrivate(selectedChatTarget, toSend);
        } else {
            ck.sendMessage(toSend);
        }
        lastMsg = "" + toSend;
        msgWindow.setText("");
    }

    private void refreshUsers() {
        if(ck != null && ck.isConnected()) ck.sendMessage("/users");
    }

    private void addSelectedFriend() {
        String selected = getSelectedPrivateTarget();
        if(selected == null) {
            addMsg("<font color=\"#ff0000\">请选择一个联系人。</font>");
        } else {
            requestFriend(selected);
        }
    }

    private void requestFriend(String nick) {
        if(nick == null || nick.trim().length() == 0) return;
        nick = nick.trim();
        if(nick.equalsIgnoreCase(ownNick())) {
            addMsg("<font color=\"#ff0000\">不能添加自己为好友。</font>");
            return;
        }
        if(friends.contains(nick)) {
            addMsg("<font color=\"#3366cc\">" + escapeHtml(nick) + " 已经是你的好友。</font>");
            return;
        }
        if(sentFriendRequests.contains(nick)) {
            addMsg("<font color=\"#3366cc\">已向 " + escapeHtml(nick) + " 发送过好友申请，等待对方同意。</font>");
            return;
        }
        if(ck == null || !ck.isConnected()) {
            addMsg("<font color=\"#ff0000\">请先连接服务器。</font>");
            return;
        }
        sentFriendRequests.add(nick);
        ck.sendMessage("/msg " + nick + " " + FRIEND_REQUEST_PREFIX + ownNick());
        addMsg("<font color=\"#3366cc\">已向 " + escapeHtml(nick) + " 发送好友申请，等待对方同意。</font>");
        if(onlineList != null) onlineList.repaint();
    }

    private void addFriend(String nick) {
        if(nick == null || nick.length() == 0) return;
        friends.add(nick);
        sentFriendRequests.remove(nick);
        incomingFriendRequests.remove(nick);
        saveFriends();
        addMsg("<font color=\"#3366cc\">已添加好友：" + escapeHtml(nick) + "</font>");
        if(visibleUsers.contains(nick)) {
            addMsg("<font color=\"#cc6600\">好友在线：" + escapeHtml(nick) + "</font>");
        }
        if(onlineList != null) onlineList.repaint();
    }

    private void loadUserData() {
        friends.clear();
        chatGroups.clear();
        sentFriendRequests.clear();
        incomingFriendRequests.clear();
        currentProfile = new Properties();
        try {
            if(currentUserDir == null) return;
            Files.createDirectories(currentUserDir);
            Path profileFile = currentUserDir.resolve("profile.properties");
            if(Files.exists(profileFile)) {
                try(InputStream in = Files.newInputStream(profileFile)) {
                    currentProfile.load(in);
                }
            }
            if(currentProfile.getProperty("displayName") == null) currentProfile.setProperty("displayName", currentUser);
            if(currentProfile.getProperty("signature") == null) currentProfile.setProperty("signature", "这个人还没有写个性签名。");

            Path friendsFile = currentUserDir.resolve("friends.txt");
            if(Files.exists(friendsFile)) {
                java.util.List<String> lines = Files.readAllLines(friendsFile, StandardCharsets.UTF_8);
                for(int i=0;i<lines.size();i++) {
                    String friend = lines.get(i).trim();
                    if(friend.length() > 0) friends.add(friend);
                }
            }

            Path groupsFile = currentUserDir.resolve("groups.txt");
            if(Files.exists(groupsFile)) {
                java.util.List<String> groupLines = Files.readAllLines(groupsFile, StandardCharsets.UTF_8);
                for(int i=0;i<groupLines.size();i++) {
                    String line = groupLines.get(i);
                    String[] parts = line.split("\\t", 2);
                    if(parts.length == 2) {
                        String groupName = decodeToken(parts[0]);
                        Set<String> members = csvToMembers(decodeToken(parts[1]));
                        if(groupName.length() > 0) addOrUpdateGroup(groupName, members, false);
                    }
                }
            }
        } catch(Exception e) {
            JOptionPane.showMessageDialog(this, "加载本地用户数据失败：" + e.getMessage());
        }
    }

    private void saveFriends() {
        try {
            if(currentUserDir == null) return;
            Files.createDirectories(currentUserDir);
            java.util.List<String> lines = new ArrayList<String>(friends);
            Collections.sort(lines);
            Files.write(currentUserDir.resolve("friends.txt"), lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch(Exception e) {
            addMsg("<font color=\"#ff0000\">保存好友列表失败: " + escapeHtml(e.getMessage()) + "</font>");
        }
    }

    private void saveGroups() {
        try {
            if(currentUserDir == null) return;
            Files.createDirectories(currentUserDir);
            java.util.List<String> lines = new ArrayList<String>();
            Iterator<Map.Entry<String, Set<String>>> it = chatGroups.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry<String, Set<String>> entry = it.next();
                lines.add(encodeToken(entry.getKey()) + "\t" + encodeToken(membersToCsv(entry.getValue())));
            }
            Files.write(currentUserDir.resolve("groups.txt"), lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch(Exception e) {
            addMsg("<font color=\"#ff0000\">保存群聊列表失败: " + escapeHtml(e.getMessage()) + "</font>");
        }
    }

    private void showCreateGroupDialog() {
        java.util.List<String> candidates = new ArrayList<String>();
        Iterator<String> friendIt = friends.iterator();
        while(friendIt.hasNext()) {
            String friend = friendIt.next();
            if(visibleUsers.contains(friend)) candidates.add(friend);
        }
        if(candidates.size() < 2) {
            addMsg("<font color=\"#ff0000\">至少需要 2 个在线好友才能创建群聊。</font>");
            return;
        }
        Collections.sort(candidates, String.CASE_INSENSITIVE_ORDER);
        final JList<String> friendList = new JList<String>(new Vector<String>(candidates));
        friendList.setFont(UI_FONT);
        friendList.setVisibleRowCount(Math.min(8, Math.max(4, candidates.size())));
        friendList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JTextField groupNameField = createTextField("", 18);
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel rows = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        addLoginRow(rows, gbc, 0, "群名称", groupNameField);
        panel.add(rows, BorderLayout.NORTH);
        panel.add(new JLabel("选择要拉入群聊的在线好友（至少 2 个）："), BorderLayout.CENTER);
        panel.add(new JScrollPane(friendList), BorderLayout.SOUTH);

        int option = JOptionPane.showConfirmDialog(this, panel, "创建群聊",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if(option != JOptionPane.OK_OPTION) return;

        String groupName = groupNameField.getText().trim();
        java.util.List<String> selectedFriends = friendList.getSelectedValuesList();
        if(groupName.length() == 0) {
            JOptionPane.showMessageDialog(this, "群名称不能为空。");
            return;
        }
        if(groupName.equals(BROADCAST_CHAT) || groupName.startsWith(GROUP_LABEL_PREFIX)
                || groupName.indexOf('|') >= 0 || groupName.indexOf('\t') >= 0) {
            JOptionPane.showMessageDialog(this, "群名称不能使用系统保留名称或特殊字符。");
            return;
        }
        if(selectedFriends.size() < 2) {
            JOptionPane.showMessageDialog(this, "请至少选择 2 个好友。");
            return;
        }
        Set<String> members = new LinkedHashSet<String>();
        members.add(ownNick());
        members.addAll(selectedFriends);
        addOrUpdateGroup(groupName, members, true);
        sendGroupInvite(groupName, members);
        rebuildConversationList(groupLabel(groupName));
        addMsg("<font color=\"#3366cc\">已创建群聊：" + escapeHtml(groupName)
                + "，成员 " + members.size() + " 人。</font>");
    }

    private void addOrUpdateGroup(String groupName, Set<String> members, boolean save) {
        if(groupName == null || groupName.trim().length() == 0) return;
        Set<String> merged = chatGroups.get(groupName);
        if(merged == null) {
            merged = new LinkedHashSet<String>();
            chatGroups.put(groupName, merged);
        }
        if(members != null) merged.addAll(members);
        merged.add(ownNick());
        if(save) saveGroups();
    }

    private void sendGroupInvite(String groupName, Set<String> members) {
        if(ck == null || !ck.isConnected()) {
            addMsg("<font color=\"#ff0000\">当前未连接服务器，群聊已保存，本次无法发送邀请。</font>");
            return;
        }
        String payload = GROUP_INVITE_PREFIX + encodeToken(groupName) + "|"
                + encodeToken(membersToCsv(members));
        Iterator<String> it = members.iterator();
        while(it.hasNext()) {
            String member = it.next();
            if(member.equalsIgnoreCase(ownNick())) continue;
            ck.sendMessage("/msg " + member + " " + payload);
        }
    }

    private void saveProfile() {
        try {
            if(currentUserDir == null) return;
            Files.createDirectories(currentUserDir);
            try(OutputStream out = Files.newOutputStream(currentUserDir.resolve("profile.properties"))) {
                currentProfile.store(out, "Chat profile");
            }
        } catch(Exception e) {
            JOptionPane.showMessageDialog(this, "保存个人信息失败：" + e.getMessage());
        }
    }

    private void loadHistoryPreview() {
        try {
            if(logFile == null || !Files.exists(logFile)) return;
            java.util.List<String> lines = Files.readAllLines(logFile, StandardCharsets.UTF_8);
            int start = Math.max(0, lines.size() - 30);
            historyWindow.addMessage(new ChatMessage(MessageKind.SYSTEM, "系统", "已加载最近聊天记录", ""));
            for(int i=start;i<lines.size();i++) {
                historyWindow.addMessage(new ChatMessage(MessageKind.SYSTEM, "历史", lines.get(i), ""));
            }
        } catch(Exception e) {
            historyWindow.addMessage(new ChatMessage(MessageKind.SYSTEM, "系统", "加载聊天记录失败：" + e.getMessage(), ""));
        }
    }

    private void initLogFile(String nick) {
        try {
            Path dir = currentUserDir != null
                    ? currentUserDir.resolve("logs")
                    : Paths.get(System.getProperty("user.home"), ".mihalychat", "logs");
            Files.createDirectories(dir);
            logFile = currentUserDir != null ? dir.resolve("chat.log") : dir.resolve(nick + "_chat.log");
        } catch(Exception e) {
            logFile = null;
        }
    }

    private void showLogPath() {
        if(logFile == null) initLogFile(txtNick.getText());
        if(logFile == null) {
            addMsg("<font color=\"#ff0000\">聊天记录文件暂不可用。</font>");
        } else {
            addMsg("<font color=\"#3366cc\">Local log: " + escapeHtml(logFile.toString()) + "</font>");
        }
    }

    private void showProfileDialog() {
        JTextField displayNameField = new JTextField(currentProfile.getProperty("displayName", currentUser), 18);
        JTextArea signatureArea = new JTextArea(currentProfile.getProperty("signature", ""), 4, 18);
        signatureArea.setLineWrap(true);
        signatureArea.setWrapStyleWord(true);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel rows = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        addLoginRow(rows, gbc, 0, "账号", new JLabel(currentUser));
        addLoginRow(rows, gbc, 1, "显示名", displayNameField);
        addLoginRow(rows, gbc, 2, "签名", new JScrollPane(signatureArea));
        panel.add(rows, BorderLayout.CENTER);

        int option = JOptionPane.showConfirmDialog(this, panel, "个人信息",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if(option == JOptionPane.OK_OPTION) {
            currentProfile.setProperty("displayName", displayNameField.getText().trim());
            currentProfile.setProperty("signature", signatureArea.getText().trim());
            saveProfile();
            refreshSidebarProfile();
            addMsg("<font color=\"#3366cc\">个人信息已保存。</font>");
        }
    }

    private void showMomentsDialog() {
        if(currentUserDir == null || currentUser == null || currentUser.length() == 0) {
            JOptionPane.showMessageDialog(this, "请先登录后再打开朋友圈。");
            return;
        }
        MomentsDialog dialog = new MomentsDialog(this);
        dialog.setVisible(true);
    }

    private Path momentsFile() {
        return currentUserDir.resolve(MOMENTS_FILE);
    }

    private java.util.List<Moment> loadMoments() {
        java.util.List<Moment> moments = new ArrayList<Moment>();
        try {
            if(currentUserDir == null) return moments;
            Path file = momentsFile();
            if(!Files.exists(file)) return moments;
            // M2 is the new tab-separated format; legacy "time + tab + Base64 text" records still load.
            java.util.List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for(int i=0;i<lines.size();i++) {
                String line = lines.get(i);
                if(line == null || line.trim().length() == 0) continue;
                Moment moment = line.startsWith(MOMENT_RECORD_VERSION + "\t")
                        ? parseModernMoment(line)
                        : parseLegacyMoment(line, i);
                if(moment != null) moments.add(moment);
            }
        } catch(Exception e) {
            addMsg("<font color=\"#ff0000\">读取朋友圈失败：" + escapeHtml(e.getMessage()) + "</font>");
        }
        sortMoments(moments);
        return moments;
    }

    private Moment parseModernMoment(String line) {
        try {
            String[] parts = line.split("\\t", -1);
            if(parts.length < 7) return null;
            Moment moment = new Moment(parts[1], parts[2], parts[3], decodeToken(parts[4]));
            moment.likes.addAll(csvToMembers(decodeToken(parts[5])));
            moment.comments.addAll(decodeMomentComments(parts[6]));
            return moment;
        } catch(Exception e) {
            return null;
        }
    }

    private boolean shouldAcceptMoment(String sender, Moment moment) {
        if(moment.id == null || moment.id.length() == 0) return false;
        if(moment.author == null || moment.author.length() == 0) return false;
        return friends.contains(sender) || friends.contains(moment.author)
                || moment.author.equalsIgnoreCase(currentUser);
    }

    private void upsertMoment(Moment incoming) {
        java.util.List<Moment> moments = loadMoments();
        boolean replaced = false;
        for(int i=0;i<moments.size();i++) {
            if(moments.get(i).id.equals(incoming.id)) {
                moments.set(i, incoming);
                replaced = true;
                break;
            }
        }
        if(!replaced) moments.add(incoming);
        saveMoments(moments);
    }

    private void removeMomentById(String id) {
        java.util.List<Moment> moments = loadMoments();
        Iterator<Moment> it = moments.iterator();
        while(it.hasNext()) {
            if(it.next().id.equals(id)) it.remove();
        }
        saveMoments(moments);
    }

    private Moment parseLegacyMoment(String line, int index) {
        try {
            int tab = line.indexOf('\t');
            if(tab <= 0) return null;
            String time = line.substring(0, tab);
            String text = decodeToken(line.substring(tab + 1));
            String id = "legacy_" + index + "_" + Math.abs(line.hashCode());
            return new Moment(id, time, currentUser, text);
        } catch(Exception e) {
            return null;
        }
    }

    private void saveMoments(java.util.List<Moment> moments) {
        try {
            if(currentUserDir == null) return;
            Files.createDirectories(currentUserDir);
            sortMoments(moments);
            java.util.List<String> lines = new ArrayList<String>();
            for(int i=0;i<moments.size();i++) {
                lines.add(serializeMoment(moments.get(i)));
            }
            // One record per line: M2, id, time, author, textBase64, likesBase64, commentsBase64.
            Files.write(momentsFile(), lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch(Exception e) {
            JOptionPane.showMessageDialog(this, "保存朋友圈失败：" + e.getMessage());
        }
    }

    private String serializeMoment(Moment moment) {
        return MOMENT_RECORD_VERSION + "\t"
                + nullSafe(moment.id) + "\t"
                + nullSafe(moment.time) + "\t"
                + nullSafe(moment.author) + "\t"
                + encodeToken(moment.text) + "\t"
                + encodeToken(membersToCsv(moment.likes)) + "\t"
                + encodeMomentComments(moment.comments);
    }

    private Moment addMoment(String text) {
        java.util.List<Moment> moments = loadMoments();
        Moment moment = new Moment(UUID.randomUUID().toString(),
                LocalDateTime.now().format(logTime), currentUser, text);
        moments.add(moment);
        saveMoments(moments);
        uploadMomentToServer(moment);
        broadcastMoment(moment);
        return moment;
    }

    private void deleteMoment(String id) {
        java.util.List<Moment> moments = loadMoments();
        boolean deleted = false;
        Iterator<Moment> it = moments.iterator();
        while(it.hasNext()) {
            Moment moment = it.next();
            if(moment.id.equals(id) && moment.author.equalsIgnoreCase(currentUser)) {
                it.remove();
                deleted = true;
                break;
            }
        }
        saveMoments(moments);
        if(deleted) {
            deleteMomentFromServer(id);
            broadcastMomentDelete(id);
        }
    }

    private Moment toggleMomentLike(String id) {
        java.util.List<Moment> moments = loadMoments();
        Moment changed = null;
        for(int i=0;i<moments.size();i++) {
            Moment moment = moments.get(i);
            if(moment.id.equals(id)) {
                if(moment.likes.contains(currentUser)) moment.likes.remove(currentUser);
                else moment.likes.add(currentUser);
                changed = moment;
                break;
            }
        }
        saveMoments(moments);
        if(changed != null) {
            uploadMomentToServer(changed);
            broadcastMoment(changed);
        }
        return changed;
    }

    private Moment addMomentComment(String id, String comment) {
        java.util.List<Moment> moments = loadMoments();
        Moment changed = null;
        for(int i=0;i<moments.size();i++) {
            Moment moment = moments.get(i);
            if(moment.id.equals(id)) {
                moment.comments.add(new MomentComment(LocalDateTime.now().format(logTime), currentUser, comment));
                changed = moment;
                break;
            }
        }
        saveMoments(moments);
        if(changed != null) {
            uploadMomentToServer(changed);
            broadcastMoment(changed);
        }
        return changed;
    }

    private void requestFriendMoments() {
        if(ck == null || !ck.isConnected()) return;
        Iterator<String> it = friends.iterator();
        while(it.hasNext()) {
            String friend = it.next();
            if(visibleUsers.contains(friend)) {
                ck.sendMessage("/msg " + friend + " " + MOMENT_SYNC_REQUEST_PREFIX + ownNick());
            }
        }
    }

    private void requestServerMoments() {
        if(ck != null && ck.isConnected()) ck.sendMessage("/moment_list");
    }

    private void uploadOwnMomentsToServer() {
        if(ck == null || !ck.isConnected()) return;
        java.util.List<Moment> moments = loadMoments();
        for(int i=0;i<moments.size();i++) {
            Moment moment = moments.get(i);
            if(moment.author.equalsIgnoreCase(currentUser)) uploadMomentToServer(moment);
        }
    }

    private void uploadMomentToServer(Moment moment) {
        if(ck != null && ck.isConnected() && moment != null) {
            ck.sendMessage("/moment_put " + encodeToken(serializeMoment(moment)));
        }
    }

    private void deleteMomentFromServer(String id) {
        if(ck != null && ck.isConnected()) ck.sendMessage("/moment_delete " + encodeToken(id));
    }

    public void receiveServerMoment(String encodedMoment) {
        Moment moment = parseModernMoment(decodeToken(encodedMoment));
        if(moment != null && shouldAcceptMoment("server", moment)) {
            upsertMoment(moment);
            refreshActiveMoments();
        }
    }

    private void sendOwnMomentsTo(String target) {
        if(ck == null || !ck.isConnected()) return;
        java.util.List<Moment> moments = loadMoments();
        for(int i=0;i<moments.size();i++) {
            Moment moment = moments.get(i);
            if(moment.author.equalsIgnoreCase(currentUser)) sendMomentTo(target, moment);
        }
    }

    private void broadcastMoment(Moment moment) {
        if(ck == null || !ck.isConnected() || moment == null) return;
        Iterator<String> it = friends.iterator();
        while(it.hasNext()) {
            String friend = it.next();
            if(visibleUsers.contains(friend)) sendMomentTo(friend, moment);
        }
    }

    private void sendMomentTo(String target, Moment moment) {
        ck.sendMessage("/msg " + target + " " + MOMENT_SYNC_ITEM_PREFIX
                + encodeToken(serializeMoment(moment)));
    }

    private void broadcastMomentDelete(String id) {
        if(ck == null || !ck.isConnected()) return;
        Iterator<String> it = friends.iterator();
        while(it.hasNext()) {
            String friend = it.next();
            if(visibleUsers.contains(friend)) {
                ck.sendMessage("/msg " + friend + " " + MOMENT_DELETE_PREFIX + encodeToken(id));
            }
        }
    }

    private void refreshActiveMoments() {
        if(activeMomentsDialog != null) activeMomentsDialog.refreshFeed();
    }

    private void sortMoments(java.util.List<Moment> moments) {
        Collections.sort(moments, new Comparator<Moment>() {
            public int compare(Moment a, Moment b) {
                return nullSafe(b.time).compareTo(nullSafe(a.time));
            }
        });
    }

    private String encodeMomentComments(java.util.List<MomentComment> comments) {
        StringBuilder builder = new StringBuilder();
        for(int i=0;i<comments.size();i++) {
            MomentComment comment = comments.get(i);
            if(i > 0) builder.append('\n');
            builder.append(nullSafe(comment.time)).append('\u001f')
                    .append(nullSafe(comment.author)).append('\u001f')
                    .append(encodeToken(comment.text));
        }
        return encodeToken(builder.toString());
    }

    private java.util.List<MomentComment> decodeMomentComments(String encoded) {
        java.util.List<MomentComment> comments = new ArrayList<MomentComment>();
        String raw = decodeToken(encoded);
        if(raw.length() == 0) return comments;
        String[] rows = raw.split("\\n");
        for(int i=0;i<rows.length;i++) {
            String[] parts = rows[i].split("\u001f", 3);
            if(parts.length == 3) {
                comments.add(new MomentComment(parts[0], parts[1], decodeToken(parts[2])));
            }
        }
        return comments;
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private void appendLog(String str) {
        try {
            if(logFile == null) return;
            String line = "[" + LocalDateTime.now().format(logTime) + "] "
                    + stripHtml(str) + System.lineSeparator();
            Files.write(logFile, line.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch(Exception e) {
        }
    }

    private void chooseAndSendFile() {
        if(ck == null || !ck.isConnected()) {
            addMsg("<font color=\"#ff0000\">请先连接服务器后再发送文件。</font>");
            return;
        }
        String selected = onlineList == null ? null : onlineList.getSelectedValue();
        if(selected == null) {
            addMsg("<font color=\"#ff0000\">请先从左侧选择一个在线好友。</font>");
            return;
        }
        if(isBroadcastConversation(selected)) {
            addMsg("<font color=\"#ff0000\">广播暂不支持发送文件，请选择一个在线好友私聊。</font>");
            return;
        }
        if(isGroupConversation(selected)) {
            addMsg("<font color=\"#ff0000\">群聊暂不支持文件群发，请选择一个在线好友私聊。</font>");
            return;
        }
        String target = getSelectedPrivateTarget();
        if(target == null || target.length() == 0) {
            addMsg("<font color=\"#ff0000\">请选择一个在线好友后再发送文件。</font>");
            return;
        }
        if(!visibleUsers.contains(target)) {
            addMsg("<font color=\"#ff0000\">对方当前离线，暂不能发送文件。</font>");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("选择本地文件");
        chooser.setCurrentDirectory(new File(System.getProperty("user.home"), "Desktop"));
        if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if(validateAttachment(file)) {
                pendingFile = file;
                updateAttachmentPreview();
                msgWindow.requestFocusInWindow();
            }
        }
    }

    private boolean validateAttachment(File file) {
        if(file == null) return false;
        if(!file.exists() || !file.isFile()) {
            addMsg("<font color=\"#ff0000\">没有找到文件：" + escapeHtml(file.getPath()) + "</font>");
            return false;
        }
        if(file.length() > MAX_FILE_BYTES) {
            addMsg("<font color=\"#ff0000\">文件超过 " + displayFileSize(MAX_FILE_BYTES) + " 限制。</font>");
            return false;
        }
        return true;
    }

    private void updateAttachmentPreview() {
        if(attachmentPanel == null) return;
        if(pendingFile == null) {
            attachmentPanel.setVisible(false);
        } else {
            String target = getSelectedPrivateTarget();
            attachmentNameLabel.setText(pendingFile.getName());
            attachmentMetaLabel.setText(displayFileSize(pendingFile.length()) + " · "
                    + (target == null ? "请选择联系人后发送" : "发送给 " + target));
            attachmentPanel.setVisible(true);
        }
        if(southPanel != null) {
            southPanel.revalidate();
            southPanel.repaint();
        }
        updateSendButtonState();
    }

    private void updateSendButtonState() {
        if(buttonSend == null || msgWindow == null) return;
        boolean hasText = msgWindow.getText() != null && msgWindow.getText().trim().length() > 0;
        boolean hasFile = pendingFile != null;
        boolean hasConversation = onlineList != null && onlineList.getSelectedValue() != null;
        buttonSend.setEnabled((hasText || hasFile) && hasConversation);
    }

    private void clearAttachment() {
        pendingFile = null;
        updateAttachmentPreview();
    }

    private String displayFileSize(long bytes) {
        if(bytes < 1024) return bytes + " B";
        if(bytes < 1024 * 1024) return String.format(Locale.US, "%.1f KB", bytes / 1024.0);
        return String.format(Locale.US, "%.1f MB", bytes / 1024.0 / 1024.0);
    }

    private void sendFileCommand(String command) {
        String[] parts = command.split("\\s+", 3);
        if(parts.length < 3) {
            addMsg("<font color=\"#ff0000\">格式：/sendfile &lt;好友名&gt; &lt;文件路径&gt;</font>");
            return;
        }
        sendFileTo(parts[1], new File(parts[2].replace("\"", "")));
    }

    private void sendPrivate(String target, String message) {
        ck.sendMessage("/msg " + target + " " + message);
        addMsg("<font color=\"#666666\">[private to " + escapeHtml(target) + "] "
                + escapeHtml(message) + "</font>");
    }

    private boolean sendGroupMessage(String groupName, String message) {
        Set<String> members = chatGroups.get(groupName);
        if(members == null || members.size() <= 1) {
            addMsg("<font color=\"#ff0000\">群聊成员为空，请重新建群。</font>");
            return false;
        }
        String payload = GROUP_MESSAGE_PREFIX + encodeToken(groupName) + "|"
                + encodeToken(message);
        int sentCount = 0;
        java.util.List<String> offline = new ArrayList<String>();
        Iterator<String> it = members.iterator();
        while(it.hasNext()) {
            String member = it.next();
            if(member.equalsIgnoreCase(ownNick())) continue;
            if(!visibleUsers.contains(member)) {
                offline.add(member);
                continue;
            }
            ck.sendMessage("/msg " + member + " " + payload);
            sentCount++;
        }
        if(sentCount == 0) {
            addMsg("<font color=\"#ff0000\">群成员当前都不在线，消息未发送。</font>");
            return false;
        }
        historyWindow.addMessage(new ChatMessage(MessageKind.OUTGOING,
                ownNick() + " / " + groupName, message, LocalDateTime.now().format(displayTime)));
        appendLog("[group " + groupName + " to " + sentCount + " members] " + message);
        if(offline.size() > 0) {
            addMsg("<font color=\"#666666\">群聊 " + escapeHtml(groupName)
                    + " 中 " + offline.size() + " 名成员离线，已跳过。</font>");
        }
        return true;
    }

    private boolean sendFileTo(String target, File file) {
        try {
            if(ck == null || !ck.isConnected()) {
                addMsg("<font color=\"#ff0000\">请先连接服务器。</font>");
                return false;
            }
            if(!file.exists() || !file.isFile()) {
                addMsg("<font color=\"#ff0000\">没有找到文件：" + escapeHtml(file.getPath()) + "</font>");
                return false;
            }
            byte[] data = Files.readAllBytes(file.toPath());
            if(data.length > MAX_FILE_BYTES) {
                addMsg("<font color=\"#ff0000\">文件超过 " + displayFileSize(MAX_FILE_BYTES) + " 限制。</font>");
                return false;
            }
            String encoded = Base64.getEncoder().encodeToString(data);
            ck.sendMessage("/file " + target + " " + file.getName() + " " + encoded);
            appendLog("[file to " + target + "] " + file.getName() + " (" + data.length + " bytes)");
            historyWindow.addMessage(ChatMessage.file(MessageKind.OUTGOING, ownNick(),
                    file.getName(), data.length, data, LocalDateTime.now().format(displayTime)));
            return true;
        } catch(Exception e) {
            addMsg("<font color=\"#ff0000\">文件发送失败：" + escapeHtml(e.getMessage()) + "</font>");
            return false;
        }
    }

    private void rebuildConversationList(String preferredSelection) {
        if(userModel == null) return;
        String ownNick = txtNick == null ? "" : txtNick.getText().trim();
        String filter = conversationFilterText();
        userModel.clear();
        if(matchesConversation(BROADCAST_CHAT, filter)) userModel.addElement(BROADCAST_CHAT);

        Iterator<String> groupIt = chatGroups.keySet().iterator();
        while(groupIt.hasNext()) {
            String label = groupLabel(groupIt.next());
            if(matchesConversation(label, filter)) userModel.addElement(label);
        }

        java.util.List<String> onlineUsers = new ArrayList<String>(visibleUsers);
        Collections.sort(onlineUsers, String.CASE_INSENSITIVE_ORDER);
        for(int i=0;i<onlineUsers.size();i++) {
            String user = onlineUsers.get(i);
            if(user.length() > 0 && !user.equalsIgnoreCase(ownNick)
                    && matchesConversation(user, filter) && !modelContains(user)) {
                userModel.addElement(user);
            }
        }

        java.util.List<String> savedFriends = new ArrayList<String>(friends);
        Collections.sort(savedFriends, String.CASE_INSENSITIVE_ORDER);
        for(int i=0;i<savedFriends.size();i++) {
            String friend = savedFriends.get(i);
            if(friend.length() > 0 && !friend.equalsIgnoreCase(ownNick)
                    && matchesConversation(friend, filter) && !modelContains(friend)) {
                userModel.addElement(friend);
            }
        }

        showConversationListState(userModel.size() == 0);
        if(userModel.size() == 0) {
            onlineList.clearSelection();
        } else if(preferredSelection != null && modelContains(preferredSelection)) {
            onlineList.setSelectedValue(preferredSelection, true);
        } else {
            onlineList.setSelectedIndex(0);
        }
        updateSelectedConversation();
    }

    private void filterConversations() {
        String preferred = onlineList == null ? null : onlineList.getSelectedValue();
        rebuildConversationList(preferred);
    }

    private String conversationFilterText() {
        return conversationSearchField == null ? "" : conversationSearchField.getText().trim().toLowerCase(Locale.ROOT);
    }

    private boolean matchesConversation(String value, String filter) {
        if(filter == null || filter.length() == 0) return true;
        return value != null && value.toLowerCase(Locale.ROOT).contains(filter);
    }

    private void showConversationListState(boolean empty) {
        if(conversationListPanel == null) return;
        CardLayout layout = (CardLayout)conversationListPanel.getLayout();
        layout.show(conversationListPanel, empty ? "empty" : "list");
    }

    private boolean modelContains(String value) {
        if(userModel == null || value == null) return false;
        for(int i=0;i<userModel.size();i++) {
            if(value.equals(userModel.getElementAt(i))) return true;
        }
        return false;
    }

    public void updateUsers(String csv) {
        String previousSelection = onlineList == null ? null : onlineList.getSelectedValue();
        String ownNick = txtNick == null ? "" : txtNick.getText().trim();
        String[] parts = csv.split(",");
        Set<String> nextUsers = new HashSet<String>();

        for(int i=0;i<parts.length;i++) {
            String user = parts[i].trim();
            if(user.length() > 0) {
                nextUsers.add(user);
            }
        }

        Iterator<String> friendIt = friends.iterator();
        while(friendIt.hasNext()) {
            String friend = friendIt.next();
            if(nextUsers.contains(friend) && !visibleUsers.contains(friend)) {
                addMsg("<font color=\"#cc6600\">好友上线：" + escapeHtml(friend) + "</font>");
            }
        }
        visibleUsers = nextUsers;
        rebuildConversationList(previousSelection);
    }

    public void receiveFile(String sender, String filename, String base64) {
        try {
            byte[] data = Base64.getDecoder().decode(base64);
            appendLog("[file from " + sender + "] " + filename + " (" + data.length + " bytes)");
            historyWindow.addMessage(ChatMessage.file(MessageKind.INCOMING, sender,
                    filename, data.length, data, LocalDateTime.now().format(displayTime)));
        } catch(Exception e) {
            addMsg("<font color=\"#ff0000\">接收文件失败：" + escapeHtml(e.getMessage()) + "</font>");
        }
    }

    public void demoConnect(String host, int port, String nick) {
        txtHost.setText(host);
        txtPort.setText("" + port);
        txtNick.setText(nick);
        connect();
    }

    public void demoAddFriend(String nick) {
        addFriend(nick);
    }

    public void demoBroadcast(String message) {
        selectedChatTarget = null;
        if(onlineList != null) onlineList.setSelectedIndex(0);
        msgWindow.setText(message);
        send();
    }

    public void demoPrivate(String target, String message) {
        selectedChatTarget = target;
        sendPrivate(target, message);
    }

    public void demoCommand(String command) {
        msgWindow.setText(command);
        send();
    }

    public void demoSendFile(String target, File file) {
        sendFileTo(target, file);
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
        if(e.getSource() == msgWindow && e.getKeyCode() == KeyEvent.VK_UP) msgWindow.setText(lastMsg);
    }

    public void keyTyped(KeyEvent e) {
        if(e.getKeyChar() == KeyEvent.VK_ENTER) {
            if(e.getSource() == msgWindow) return;
            if(e.getSource() == txtNick) { connect(); msgWindow.requestFocus(); }
            if(e.getSource() == txtHost) txtPort.requestFocus();
            if(e.getSource() == txtPort) txtNick.requestFocus();
        }
    }

    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==buttonConnect) connect();
        if(e.getSource()==buttonSend) send();
        if(e.getSource()==buttonRefresh) refreshUsers();
        if(e.getSource()==buttonAddFriend) addSelectedFriend();
        if(e.getSource()==buttonCreateGroup) showCreateGroupDialog();
        if(e.getSource()==buttonEmoji) showEmojiPicker();
        if(e.getSource()==buttonFile) chooseAndSendFile();
        if(e.getSource()==buttonProfile) showProfileDialog();
        if(e.getSource()==buttonMoments) showMomentsDialog();
        if(e.getSource()==buttonLog) showLogPath();
    }

    public void focusGained(FocusEvent e) {
        if(e.getSource()==txtHost && txtHost.getText().equals(ChatClient.serverText)) txtHost.setText("");
        if(e.getSource()==txtPort && txtPort.getText().equals(ChatClient.portText)) txtPort.setText("");
        if(e.getSource()==txtNick && txtNick.getText().equals(ChatClient.nickText)) txtNick.setText("");
    }

    public void focusLost(FocusEvent e) {
        if(e.getSource()==txtPort && txtPort.getText().equals("")) txtPort.setText(ChatClient.portText);
        if(e.getSource()==txtHost && txtHost.getText().equals("")) txtHost.setText(ChatClient.serverText);
        if(e.getSource()==txtNick && txtNick.getText().equals("")) txtNick.setText(ChatClient.nickText);
    }

    private static String escapeHtml(String str) {
        if(str == null) return "";
        return str.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;");
    }

    private static String encodeToken(String str) {
        return Base64.getEncoder().encodeToString((str == null ? "" : str).getBytes(StandardCharsets.UTF_8));
    }

    private static String decodeToken(String str) {
        try {
            return new String(Base64.getDecoder().decode(str == null ? "" : str), StandardCharsets.UTF_8);
        } catch(Exception e) {
            return "";
        }
    }

    private static Set<String> csvToMembers(String csv) {
        Set<String> members = new LinkedHashSet<String>();
        if(csv == null) return members;
        String[] parts = csv.split(",");
        for(int i=0;i<parts.length;i++) {
            String member = parts[i].trim();
            if(member.length() > 0) members.add(member);
        }
        return members;
    }

    private static String membersToCsv(Set<String> members) {
        if(members == null || members.size() == 0) return "";
        java.util.List<String> values = new ArrayList<String>(members);
        Collections.sort(values, String.CASE_INSENSITIVE_ORDER);
        StringBuilder builder = new StringBuilder();
        for(int i=0;i<values.size();i++) {
            if(i > 0) builder.append(",");
            builder.append(values.get(i));
        }
        return builder.toString();
    }

    private static String stripHtml(String str) {
        if(str == null) return "";
        return str.replaceAll("<[^>]+>", "")
                  .replace("&lt;", "<")
                  .replace("&gt;", ">")
                  .replace("&quot;", "\"")
                  .replace("&amp;", "&");
    }

    private static String htmlText(String str) {
        return escapeHtml(str == null ? "" : str)
                .replace("\r\n", "<br>")
                .replace("\n", "<br>");
    }

    // Local-only moments feed. It upgrades the old text-area timeline into card-based Swing UI.
    class MomentsDialog extends JDialog {
        private JPanel feedPanel;
        private JPanel composerPanel;
        private JTextArea editor;
        private JLabel countLabel;
        private JLabel tipLabel;
        private JButton publishButton;
        private JTextField searchField;

        MomentsDialog(JFrame owner) {
            super(owner, "朋友圈", true);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setSize(680, 720);
            setMinimumSize(new Dimension(620, 640));
            setLocationRelativeTo(owner);
            activeMomentsDialog = this;
            addWindowListener(new WindowAdapter() {
                public void windowClosed(WindowEvent e) {
                    if(activeMomentsDialog == MomentsDialog.this) activeMomentsDialog = null;
                }
            });
            setContentPane(createMomentsContent());
            refreshFeed();
            requestServerMoments();
            requestFriendMoments();
        }

        private JPanel createMomentsContent() {
            JPanel root = new JPanel(new BorderLayout());
            root.setBackground(CHAT_BACKGROUND);
            root.add(createMomentsHeader(), BorderLayout.NORTH);
            root.add(createMomentsCenter(), BorderLayout.CENTER);
            return root;
        }

        private JPanel createMomentsHeader() {
            JPanel header = new CoverPanel();
            header.setLayout(new BorderLayout(SPACE_MD, SPACE_MD));
            header.setBorder(new CompoundBorder(
                    new MatteBorder(0, 0, 1, 0, BORDER_LIGHT),
                    pad(SPACE_LG, SPACE_XL, SPACE_LG, SPACE_XL)));
            header.setPreferredSize(new Dimension(0, 150));

            JPanel titleRow = new JPanel(new BorderLayout());
            titleRow.setOpaque(false);
            JLabel title = new JLabel("朋友圈");
            title.setFont(PAGE_TITLE_FONT);
            title.setForeground(Color.WHITE);
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, SPACE_SM, 0));
            actions.setOpaque(false);
            JButton refreshButton = smallButton("刷新");
            JButton composeButton = createButton("发布动态", true);
            composeButton.setPreferredSize(new Dimension(96, BUTTON_HEIGHT));
            refreshButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    refreshFeed();
                    showTip("已刷新");
                }
            });
            composeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    composerPanel.setVisible(!composerPanel.isVisible());
                    editor.requestFocusInWindow();
                    MomentsDialog.this.revalidate();
                }
            });
            actions.add(refreshButton);
            actions.add(composeButton);
            titleRow.add(title, BorderLayout.WEST);
            titleRow.add(actions, BorderLayout.EAST);

            JPanel profile = new JPanel(new BorderLayout(SPACE_MD, 0));
            profile.setOpaque(false);
            profile.add(new AvatarView(currentUser, false), BorderLayout.WEST);
            JPanel textBlock = new JPanel();
            textBlock.setOpaque(false);
            textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));
            JLabel name = new JLabel(currentProfile.getProperty("displayName", currentUser));
            name.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 16));
            name.setForeground(Color.WHITE);
            JLabel signature = new JLabel(currentProfile.getProperty("signature", "这个人还没有写个性签名。"));
            signature.setFont(UI_FONT_SMALL);
            signature.setForeground(new Color(226, 232, 240));
            textBlock.add(name);
            textBlock.add(Box.createVerticalStrut(SPACE_XS));
            textBlock.add(signature);
            profile.add(textBlock, BorderLayout.CENTER);

            header.add(titleRow, BorderLayout.NORTH);
            header.add(profile, BorderLayout.CENTER);
            return header;
        }

        private JPanel createMomentsCenter() {
            JPanel center = new JPanel(new BorderLayout(0, SPACE_MD));
            center.setBackground(CHAT_BACKGROUND);
            center.setBorder(pad(SPACE_LG));

            JPanel top = new JPanel();
            top.setOpaque(false);
            top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
            top.add(createSearchPanel());
            top.add(Box.createVerticalStrut(SPACE_MD));
            composerPanel = createComposerPanel();
            composerPanel.setVisible(false);
            top.add(composerPanel);
            center.add(top, BorderLayout.NORTH);

            feedPanel = new JPanel();
            feedPanel.setBackground(CHAT_BACKGROUND);
            feedPanel.setLayout(new BoxLayout(feedPanel, BoxLayout.Y_AXIS));
            JScrollPane scroll = createModernScrollPane(feedPanel, CHAT_BACKGROUND);
            center.add(scroll, BorderLayout.CENTER);
            return center;
        }

        private JPanel createSearchPanel() {
            JPanel panel = new JPanel(new BorderLayout(8, 0));
            panel.setOpaque(false);
            searchField = new PromptTextField("", 20, "搜索动态或评论");
            styleTextField(searchField);
            searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { refreshFeed(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { refreshFeed(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { refreshFeed(); }
            });
            panel.add(searchField, BorderLayout.CENTER);
            return panel;
        }

        private JPanel createComposerPanel() {
            BubblePanel panel = createCardPanel(SPACE_MD);
            panel.setLayout(new BorderLayout(SPACE_MD, SPACE_MD));

            JLabel title = new JLabel("发布新动态");
            title.setFont(UI_FONT_BOLD);
            title.setForeground(TEXT);
            panel.add(title, BorderLayout.NORTH);

            editor = new JTextArea(4, 30);
            editor.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
            editor.setForeground(TEXT);
            editor.setLineWrap(true);
            editor.setWrapStyleWord(true);
            editor.setBorder(pad(SPACE_SM));
            editor.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { updateComposerState(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { updateComposerState(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { updateComposerState(); }
            });
            JScrollPane editorScroll = new JScrollPane(editor);
            editorScroll.setBorder(new RoundedBorder(BORDER, RADIUS_MD));
            panel.add(editorScroll, BorderLayout.CENTER);

            JPanel footer = new JPanel(new BorderLayout());
            footer.setOpaque(false);
            countLabel = new JLabel("0/" + MOMENT_TEXT_LIMIT);
            countLabel.setFont(UI_FONT_SMALL);
            countLabel.setForeground(MUTED);
            tipLabel = new JLabel(" ");
            tipLabel.setFont(UI_FONT_SMALL);
            tipLabel.setForeground(SUCCESS);
            JPanel left = new JPanel();
            left.setOpaque(false);
            left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
            left.add(countLabel);
            left.add(tipLabel);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            buttons.setOpaque(false);
            JButton clearButton = smallButton("清空");
            publishButton = createButton("发布", true);
            publishButton.setPreferredSize(new Dimension(72, 32));
            publishButton.setEnabled(false);
            clearButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    editor.setText("");
                    showTip(" ");
                }
            });
            publishButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    publishMoment();
                }
            });
            buttons.add(clearButton);
            buttons.add(publishButton);

            footer.add(left, BorderLayout.WEST);
            footer.add(buttons, BorderLayout.EAST);
            panel.add(footer, BorderLayout.SOUTH);
            return panel;
        }

        private void updateComposerState() {
            int remain = MOMENT_TEXT_LIMIT - editor.getText().length();
            countLabel.setText(editor.getText().length() + "/" + MOMENT_TEXT_LIMIT
                    + (remain < 0 ? "，已超出 " + Math.abs(remain) + " 字" : ""));
            countLabel.setForeground(remain >= 0 ? MUTED : DANGER);
            publishButton.setEnabled(editor.getText().trim().length() > 0 && remain >= 0);
        }

        private void publishMoment() {
            String text = editor.getText().trim();
            if(text.length() == 0 || text.length() > MOMENT_TEXT_LIMIT) return;
            addMoment(text);
            editor.setText("");
            refreshFeed();
            showTip("动态发布成功");
            addMsg("<font color=\"#3366cc\">朋友圈动态已发布。</font>");
        }

        private void refreshFeed() {
            if(feedPanel == null) return;
            feedPanel.removeAll();
            java.util.List<Moment> moments = filterMoments(loadMoments());
            if(moments.size() == 0) {
                feedPanel.add(createEmptyMomentsPanel());
            } else {
                for(int i=0;i<moments.size();i++) {
                    feedPanel.add(createMomentCard(moments.get(i)));
                    feedPanel.add(Box.createVerticalStrut(12));
                }
            }
            feedPanel.revalidate();
            feedPanel.repaint();
        }

        private java.util.List<Moment> filterMoments(java.util.List<Moment> moments) {
            String keyword = searchField == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
            if(keyword.length() == 0) return moments;
            java.util.List<Moment> filtered = new ArrayList<Moment>();
            for(int i=0;i<moments.size();i++) {
                Moment moment = moments.get(i);
                if(nullSafe(moment.text).toLowerCase(Locale.ROOT).contains(keyword)) {
                    filtered.add(moment);
                    continue;
                }
                for(int j=0;j<moment.comments.size();j++) {
                    if(nullSafe(moment.comments.get(j).text).toLowerCase(Locale.ROOT).contains(keyword)) {
                        filtered.add(moment);
                        break;
                    }
                }
            }
            return filtered;
        }

        private Component createEmptyMomentsPanel() {
            return createEmptyState("还没有动态", "发布第一条，或者刷新好友朋友圈");
        }

        private Component createMomentCard(final Moment moment) {
            BubblePanel card = createCardPanel(SPACE_LG);
            card.setLayout(new BorderLayout(0, SPACE_MD));
            card.setAlignmentX(Component.LEFT_ALIGNMENT);

            card.add(createMomentCardHeader(moment), BorderLayout.NORTH);

            JPanel body = new JPanel();
            body.setOpaque(false);
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
            JLabel text = new JLabel("<html><div style=\"width:520px;font-size:14px;line-height:1.5;\">"
                    + htmlText(moment.text) + "</div></html>");
            text.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
            text.setForeground(TEXT);
            body.add(text);
            body.add(Box.createVerticalStrut(SPACE_MD));
            body.add(createMomentActions(moment));
            if(moment.likes.size() > 0 || moment.comments.size() > 0) {
                body.add(Box.createVerticalStrut(SPACE_SM));
                body.add(createMomentSocialPanel(moment));
            }
            card.add(body, BorderLayout.CENTER);
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
            return card;
        }

        private Component createMomentCardHeader(Moment moment) {
            JPanel header = new JPanel(new BorderLayout(SPACE_MD, 0));
            header.setOpaque(false);
            header.add(new AvatarView(moment.author, false), BorderLayout.WEST);
            JPanel textBlock = new JPanel();
            textBlock.setOpaque(false);
            textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));
            JLabel author = new JLabel(moment.author);
            author.setFont(UI_FONT_BOLD);
            author.setForeground(TEXT);
            JLabel time = new JLabel(moment.time);
            time.setFont(UI_FONT_SMALL);
            time.setForeground(MUTED);
            textBlock.add(author);
            textBlock.add(Box.createVerticalStrut(SPACE_XS));
            textBlock.add(time);
            header.add(textBlock, BorderLayout.CENTER);
            return header;
        }

        private Component createMomentActions(final Moment moment) {
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, SPACE_SM, 0));
            buttons.setOpaque(false);
            boolean liked = moment.likes.contains(currentUser);
            JButton likeButton = smallButton(liked ? "取消点赞" : "点赞");
            JButton commentButton = smallButton("评论");
            likeButton.setPreferredSize(new Dimension(liked ? 86 : 58, 30));
            likeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    toggleMomentLike(moment.id);
                    refreshFeed();
                }
            });
            commentButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    commentMoment(moment);
                }
            });
            buttons.add(likeButton);
            buttons.add(commentButton);
            if(moment.author.equalsIgnoreCase(currentUser)) {
                JButton deleteButton = smallButton("删除");
                deleteButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        int choice = JOptionPane.showConfirmDialog(MomentsDialog.this,
                                "确定删除这条动态吗？",
                                "删除动态",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE);
                        if(choice == JOptionPane.YES_OPTION) {
                            deleteMoment(moment.id);
                            refreshFeed();
                            showTip("动态已删除");
                        }
                    }
                });
                buttons.add(deleteButton);
            }
            row.add(buttons, BorderLayout.WEST);
            return row;
        }

        private Component createMomentSocialPanel(Moment moment) {
            BubblePanel panel = new BubblePanel(SURFACE_SOFT, BORDER_LIGHT, RADIUS_MD);
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(pad(SPACE_SM, SPACE_MD, SPACE_SM, SPACE_MD));
            if(moment.likes.size() > 0) {
                JLabel likes = new JLabel("点赞 " + moment.likes.size() + " · " + membersToCsv(moment.likes));
                likes.setFont(UI_FONT_SMALL);
                likes.setForeground(PRIMARY_DARK);
                panel.add(likes);
            }
            for(int i=0;i<moment.comments.size();i++) {
                MomentComment comment = moment.comments.get(i);
                JLabel line = new JLabel("<html><div style=\"width:500px;\"><b>"
                        + escapeHtml(comment.author) + "</b>："
                        + htmlText(comment.text)
                        + " <span style=\"color:#64748b;font-size:10px;\">"
                        + escapeHtml(comment.time) + "</span></div></html>");
                line.setFont(UI_FONT_SMALL);
                line.setForeground(TEXT);
                if(i > 0 || moment.likes.size() > 0) panel.add(Box.createVerticalStrut(5));
                panel.add(line);
            }
            return panel;
        }

        private void commentMoment(Moment moment) {
            JTextArea area = new JTextArea(4, 24);
            area.setFont(UI_FONT);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setBorder(pad(SPACE_SM));
            JScrollPane scroll = new JScrollPane(area);
            scroll.setBorder(new RoundedBorder(BORDER, RADIUS_MD));
            int option = JOptionPane.showConfirmDialog(this, scroll, "评论",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if(option != JOptionPane.OK_OPTION) return;
            String text = area.getText().trim();
            if(text.length() == 0) return;
            addMomentComment(moment.id, text);
            refreshFeed();
            showTip("评论已发布");
        }

        private JButton smallButton(String text) {
            JButton button = createButton(text, false);
            button.setFont(UI_FONT_BOLD);
            button.setPreferredSize(new Dimension(58, 30));
            return button;
        }

        private void showTip(String text) {
            if(tipLabel != null) {
                tipLabel.setText(text == null ? " " : text);
                tipLabel.setForeground(SUCCESS);
            }
        }
    }

    // Custom modal login window; successful result continues through applyLogin(login) and connect().
    class LoginDialog extends JDialog {
        private JTabbedPane modeTabs;
        private JTextField loginUserField;
        private JTextField registerUserField;
        private PromptPasswordField loginPasswordField;
        private PromptPasswordField registerPasswordField;
        private JTextField hostField;
        private JTextField portField;
        private JLabel statusLabel;
        private JPanel advancedPanel;
        private JButton primaryButton;
        private JButton advancedButton;
        private LoginData loginData;

        LoginDialog(JFrame owner, String host, String port, String username) {
            super(owner, "登录 / 注册", true);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setResizable(false);
            setSize(460, 570);
            setLocationRelativeTo(owner);

            loginUserField = new PromptTextField(username, 18, "请输入用户名");
            registerUserField = new PromptTextField("", 18, "创建一个用户名");
            loginPasswordField = new PromptPasswordField(18, "请输入密码");
            registerPasswordField = new PromptPasswordField(18, "至少 4 位密码");
            hostField = createTextField(host == null || host.length() == 0 ? ChatClient.serverText : host, 16);
            portField = createTextField(port == null || port.length() == 0 ? "4567" : port, 6);
            styleTextField(loginUserField);
            styleTextField(registerUserField);
            styleTextField(loginPasswordField);
            styleTextField(registerPasswordField);

            setContentPane(createLoginContent());
            getRootPane().setDefaultButton(primaryButton);
        }

        LoginData getLoginData() {
            return loginData;
        }

        private JPanel createLoginContent() {
            JPanel root = new JPanel(new BorderLayout());
            root.setBackground(APP_BACKGROUND);
            root.setBorder(pad(SPACE_XL));

            BubblePanel card = new BubblePanel(SURFACE, BORDER_LIGHT, RADIUS_XL);
            card.setLayout(new BorderLayout(0, SPACE_LG));
            card.setBorder(pad(24));
            card.add(createLoginHeader(), BorderLayout.NORTH);
            card.add(createLoginCenter(), BorderLayout.CENTER);
            card.add(createLoginFooter(), BorderLayout.SOUTH);
            root.add(card, BorderLayout.CENTER);
            return root;
        }

        private JPanel createLoginHeader() {
            JPanel header = new JPanel(new BorderLayout(SPACE_MD, 0));
            header.setOpaque(false);
            header.add(new AvatarView("C", false), BorderLayout.WEST);

            JPanel textBlock = new JPanel();
            textBlock.setOpaque(false);
            textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));
            JLabel title = new JLabel("CNCD Chat");
            title.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 25));
            title.setForeground(TEXT);
            JLabel subtitle = new JLabel("登录后继续你的好友、群聊和朋友圈");
            subtitle.setFont(UI_FONT_SMALL);
            subtitle.setForeground(MUTED);
            textBlock.add(title);
            textBlock.add(Box.createVerticalStrut(SPACE_XS));
            textBlock.add(subtitle);
            header.add(textBlock, BorderLayout.CENTER);
            return header;
        }

        private JPanel createLoginCenter() {
            JPanel center = new JPanel();
            center.setOpaque(false);
            center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

            modeTabs = new JTabbedPane();
            modeTabs.setFont(UI_FONT_BOLD);
            modeTabs.setBackground(SURFACE);
            modeTabs.setForeground(TEXT);
            modeTabs.addTab("登录", createAccountForm(loginUserField, loginPasswordField));
            modeTabs.addTab("注册", createAccountForm(registerUserField, registerPasswordField));
            modeTabs.addChangeListener(new javax.swing.event.ChangeListener() {
                public void stateChanged(javax.swing.event.ChangeEvent e) {
                    primaryButton.setText(modeTabs.getSelectedIndex() == 0 ? "登录" : "注册并登录");
                    clearStatus();
                }
            });

            advancedButton = createButton("高级设置", false);
            advancedButton.setPreferredSize(new Dimension(96, BUTTON_HEIGHT));
            advancedButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    advancedPanel.setVisible(!advancedPanel.isVisible());
                    advancedButton.setText(advancedPanel.isVisible() ? "收起设置" : "高级设置");
                    LoginDialog.this.revalidate();
                }
            });

            advancedPanel = createAdvancedPanel();
            advancedPanel.setVisible(false);

            statusLabel = new JLabel(" ");
            statusLabel.setFont(UI_FONT_SMALL);
            statusLabel.setForeground(DANGER);

            center.add(modeTabs);
            center.add(Box.createVerticalStrut(SPACE_MD));
            center.add(advancedButton);
            center.add(Box.createVerticalStrut(SPACE_SM));
            center.add(advancedPanel);
            center.add(Box.createVerticalStrut(SPACE_SM));
            center.add(statusLabel);
            return center;
        }

        private JPanel createAccountForm(JTextField userField, JPasswordField passwordField) {
            JPanel form = new JPanel();
            form.setOpaque(false);
            form.setBorder(pad(SPACE_MD, 0, SPACE_SM, 0));
            form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
            form.add(createStackedField("用户名", userField));
            form.add(Box.createVerticalStrut(SPACE_MD));
            form.add(createPasswordRow(passwordField));
            return form;
        }

        private JPanel createStackedField(String label, JComponent input) {
            JPanel panel = new JPanel(new BorderLayout(0, SPACE_SM));
            panel.setOpaque(false);
            JLabel textLabel = new JLabel(label);
            textLabel.setFont(UI_FONT_BOLD);
            textLabel.setForeground(MUTED);
            panel.add(textLabel, BorderLayout.NORTH);
            panel.add(input, BorderLayout.CENTER);
            return panel;
        }

        private JPanel createPasswordRow(final JPasswordField passwordField) {
            JPanel row = new JPanel(new BorderLayout(SPACE_SM, SPACE_SM));
            row.setOpaque(false);
            JLabel label = new JLabel("密码");
            label.setFont(UI_FONT_BOLD);
            label.setForeground(MUTED);
            row.add(label, BorderLayout.NORTH);

            final JButton toggle = createButton("显示", false);
            toggle.setPreferredSize(new Dimension(64, BUTTON_HEIGHT));
            final char echoChar = passwordField.getEchoChar();
            toggle.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    boolean hidden = passwordField.getEchoChar() != 0;
                    passwordField.setEchoChar(hidden ? (char)0 : echoChar);
                    toggle.setText(hidden ? "隐藏" : "显示");
                }
            });

            JPanel inputRow = new JPanel(new BorderLayout(SPACE_SM, 0));
            inputRow.setOpaque(false);
            inputRow.add(passwordField, BorderLayout.CENTER);
            inputRow.add(toggle, BorderLayout.EAST);
            row.add(inputRow, BorderLayout.CENTER);
            return row;
        }

        private JPanel createAdvancedPanel() {
            JPanel panel = new JPanel(new BorderLayout(SPACE_MD, 0));
            panel.setOpaque(false);
            panel.setBorder(new CompoundBorder(
                    new RoundedBorder(BORDER_LIGHT, RADIUS_LG),
                    pad(SPACE_SM)));
            portField.setPreferredSize(new Dimension(82, 34));
            JPanel hostBlock = createStackedField("服务器", hostField);
            JPanel portBlock = createStackedField("端口", portField);
            portBlock.setPreferredSize(new Dimension(96, 58));
            panel.add(hostBlock, BorderLayout.CENTER);
            panel.add(portBlock, BorderLayout.EAST);
            return panel;
        }

        private JPanel createLoginFooter() {
            JPanel footer = new JPanel(new BorderLayout());
            footer.setOpaque(false);
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            buttons.setOpaque(false);
            JButton cancelButton = createButton("取消", false);
            primaryButton = createButton("登录", true);
            primaryButton.setPreferredSize(new Dimension(112, 40));
            cancelButton.setPreferredSize(new Dimension(78, 40));
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    loginData = null;
                    dispose();
                }
            });
            primaryButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    submit();
                }
            });
            buttons.add(cancelButton);
            buttons.add(primaryButton);
            footer.add(buttons, BorderLayout.EAST);
            return footer;
        }

        private void submit() {
            boolean registerMode = modeTabs.getSelectedIndex() == 1;
            JTextField userField = registerMode ? registerUserField : loginUserField;
            JPasswordField passwordField = registerMode ? registerPasswordField : loginPasswordField;
            String host = hostField.getText().trim();
            String port = portField.getText().trim();
            String username = userField.getText().trim();
            String password = new String(passwordField.getPassword());

            String error = validateLoginInput(host, port, username, password);
            if(error != null) {
                showError(error);
                return;
            }

            setBusy(true);
            try {
                if(registerMode) {
                    if(!registerLocalAccount(username, password)) {
                        showError("用户名已存在。");
                        return;
                    }
                } else if(!verifyLocalAccount(username, password)) {
                    showError("用户名或密码错误。");
                    return;
                }
                loginData = new LoginData(host, port, username, password);
                dispose();
            } catch(Exception e) {
                showError("账号操作失败：" + e.getMessage());
            } finally {
                if(loginData == null) setBusy(false);
            }
        }

        private String validateLoginInput(String host, String port, String username, String password) {
            if(host.length() == 0) return "服务器地址不能为空。";
            if(port.length() == 0) return "端口不能为空。";
            try {
                int value = Integer.parseInt(port);
                if(value <= 0 || value > 65535) return "端口范围应为 1 - 65535。";
            } catch(Exception e) {
                return "端口必须是数字。";
            }
            if(username.length() == 0) return "用户名不能为空。";
            if(password.length() == 0) return "密码不能为空。";
            if(username.length() < 3) return "用户名至少 3 位。";
            if(password.length() < 4) return "密码至少 4 位。";
            return null;
        }

        private void setBusy(boolean busy) {
            primaryButton.setEnabled(!busy);
            modeTabs.setEnabled(!busy);
        }

        private void clearStatus() {
            statusLabel.setForeground(DANGER);
            statusLabel.setText(" ");
        }

        private void showError(String text) {
            statusLabel.setForeground(DANGER);
            statusLabel.setText(text);
        }
    }

    class PromptTextField extends JTextField {
        private String prompt;

        PromptTextField(String text, int columns, String prompt) {
            super(text, columns);
            this.prompt = prompt;
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if(prompt == null || prompt.length() == 0 || getText().length() > 0) return;
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(getFont());
            g2.setColor(SOFT_MUTED);
            Insets insets = getInsets();
            FontMetrics fm = g2.getFontMetrics();
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(prompt, insets.left + 1, y);
            g2.dispose();
        }
    }

    class PromptTextArea extends JTextArea {
        private String prompt;

        PromptTextArea(String prompt) {
            super();
            this.prompt = prompt;
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if(prompt == null || prompt.length() == 0 || getText().length() > 0) return;
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(getFont());
            g2.setColor(SOFT_MUTED);
            Insets insets = getInsets();
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(prompt, insets.left + 1, insets.top + fm.getAscent());
            g2.dispose();
        }
    }

    class PromptPasswordField extends JPasswordField {
        private String prompt;

        PromptPasswordField(int columns, String prompt) {
            super(columns);
            this.prompt = prompt;
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if(prompt == null || prompt.length() == 0 || getPassword().length > 0) return;
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(getFont());
            g2.setColor(SOFT_MUTED);
            Insets insets = getInsets();
            FontMetrics fm = g2.getFontMetrics();
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(prompt, insets.left + 1, y);
            g2.dispose();
        }
    }

    class StyledButton extends JButton {
        private boolean primary;
        private boolean hover = false;

        StyledButton(String text, boolean primary) {
            super(text);
            this.primary = primary;
            setFont(UI_FONT_BOLD);
            setFocusPainted(false);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorder(pad(7, 14, 7, 14));
            setPreferredSize(new Dimension(primary ? 86 : 92, BUTTON_HEIGHT));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill;
            Color stroke;
            if(!isEnabled()) {
                fill = new Color(232, 237, 245);
                stroke = BORDER_LIGHT;
                setForeground(SOFT_MUTED);
            } else if(primary) {
                fill = hover ? PRIMARY_DARK : PRIMARY;
                stroke = PRIMARY_DARK;
                setForeground(Color.WHITE);
            } else {
                fill = hover ? PRIMARY_SOFT : SURFACE;
                stroke = hover ? new Color(147, 197, 253) : BORDER_LIGHT;
                setForeground(TEXT);
            }
            g2.setColor(fill);
            g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, RADIUS_LG, RADIUS_LG);
            g2.setColor(stroke);
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, RADIUS_LG, RADIUS_LG);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    class RoundedBorder extends AbstractBorder {
        private Color color;
        private int radius;

        RoundedBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }
    }

    class BubblePanel extends JPanel {
        private Color fill;
        private Color stroke;
        private int radius;

        BubblePanel(Color fill, Color stroke, int radius) {
            this.fill = fill;
            this.stroke = stroke;
            this.radius = radius;
            setOpaque(false);
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.setColor(stroke);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    class CoverPanel extends JPanel {
        CoverPanel() {
            setOpaque(false);
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint paint = new GradientPaint(0, 0, PRIMARY_DARK,
                    getWidth(), getHeight(), new Color(13, 148, 136));
            g2.setPaint(paint);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(255, 255, 255, 32));
            g2.fillRoundRect(getWidth() - 140, -30, 180, 180, 90, 90);
            g2.setColor(new Color(255, 255, 255, 22));
            g2.fillRoundRect(-40, getHeight() - 75, 150, 150, 80, 80);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    class ConversationRenderer extends JPanel implements ListCellRenderer<String> {
        private AvatarView avatar = new AvatarView("?", false);
        private JLabel nameLabel = new JLabel();
        private JLabel detailLabel = new JLabel();
        private JLabel stateDot = new JLabel("●");
        private JPanel textPanel = new JPanel();
        private boolean selectedRow = false;
        private boolean hoverRow = false;

        ConversationRenderer() {
            setLayout(new BorderLayout(SPACE_MD, 0));
            setBorder(pad(7, 8, 7, 8));
            textPanel.setOpaque(false);
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            nameLabel.setFont(UI_FONT_BOLD);
            detailLabel.setFont(UI_FONT_SMALL);
            stateDot.setFont(new Font("Dialog", Font.BOLD, 12));
            textPanel.add(nameLabel);
            textPanel.add(Box.createVerticalStrut(2));
            textPanel.add(detailLabel);
            add(avatar, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);
            add(stateDot, BorderLayout.EAST);
        }

        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
                boolean isSelected, boolean cellHasFocus) {
            String name = value == null ? "" : value.toString();
            avatar.setAvatar(name, false);
            if(BROADCAST_CHAT.equals(name)) {
                nameLabel.setText(BROADCAST_CHAT);
                detailLabel.setText("全服务器广播 · " + visibleUsers.size() + " 人在线");
                stateDot.setForeground(PRIMARY);
            } else if(isGroupConversation(name)) {
                String groupName = groupNameFromLabel(name);
                Set<String> members = chatGroups.get(groupName);
                int memberCount = members == null ? 1 : members.size();
                nameLabel.setText(groupName);
                detailLabel.setText("群聊 · " + memberCount + " 人 · " + countOnlineMembers(members) + " 人在线");
                stateDot.setForeground(new Color(13, 148, 136));
            } else {
                nameLabel.setText(name);
                if(friends.contains(name)) {
                    if(visibleUsers.contains(name)) {
                        detailLabel.setText("好友在线");
                        stateDot.setForeground(SUCCESS);
                    } else {
                        detailLabel.setText("好友离线");
                        stateDot.setForeground(SOFT_MUTED);
                    }
                } else if(sentFriendRequests.contains(name)) {
                    detailLabel.setText("等待验证");
                    stateDot.setForeground(SOFT_MUTED);
                } else {
                    detailLabel.setText("在线");
                    stateDot.setForeground(SUCCESS);
                }
            }
            selectedRow = isSelected;
            hoverRow = index == hoveredConversationIndex;
            if(isSelected) {
                nameLabel.setForeground(TEXT);
                detailLabel.setForeground(PRIMARY_DARK);
            } else if(index == hoveredConversationIndex) {
                nameLabel.setForeground(TEXT);
                detailLabel.setForeground(MUTED);
            } else {
                nameLabel.setForeground(TEXT);
                detailLabel.setForeground(MUTED);
            }
            setOpaque(false);
            return this;
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if(selectedRow || hoverRow) {
                g2.setColor(selectedRow ? PRIMARY_LIGHT : SURFACE_SOFT);
                g2.fillRoundRect(2, 3, getWidth() - 4, getHeight() - 6, RADIUS_LG, RADIUS_LG);
                if(selectedRow) {
                    g2.setColor(new Color(147, 197, 253));
                    g2.fillRoundRect(3, 13, 3, getHeight() - 26, 3, 3);
                }
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    class ClientHistory extends JPanel {
        private boolean showingEmptyState = false;

        public ClientHistory() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(CHAT_BACKGROUND);
            setBorder(pad(SPACE_SM));
            showEmptyState();
        }

        public void addMessage(ChatMessage message) {
            if(showingEmptyState) {
                removeAll();
                showingEmptyState = false;
            }
            Component row = message.kind == MessageKind.SYSTEM
                    ? createSystemRow(message)
                    : createChatRow(message);
            add(row);
            add(Box.createVerticalStrut(6));
            revalidate();
            repaint();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JScrollPane pane = (JScrollPane)SwingUtilities.getAncestorOfClass(JScrollPane.class, ClientHistory.this);
                    if(pane != null) {
                        JScrollBar bar = pane.getVerticalScrollBar();
                        bar.setValue(bar.getMaximum());
                    }
                }
            });
        }

        private Component createSystemRow(ChatMessage message) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            JLabel label = new JLabel(message.time + "  " + message.body);
            label.setFont(UI_FONT_SMALL);
            label.setForeground(MUTED);
            label.setOpaque(false);
            BubblePanel pill = new BubblePanel(new Color(235, 240, 247), BORDER_LIGHT, RADIUS_XL);
            pill.setLayout(new BorderLayout());
            pill.setBorder(pad(5, 12, 5, 12));
            pill.add(label, BorderLayout.CENTER);
            row.add(pill);
            return row;
        }

        private Component createChatRow(ChatMessage message) {
            boolean outgoing = message.kind == MessageKind.OUTGOING;
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setBorder(pad(3, 0, 5, 0));

            JPanel pack = new JPanel();
            pack.setOpaque(false);
            pack.setLayout(new BoxLayout(pack, BoxLayout.X_AXIS));

            Component avatar = new AvatarView(message.sender, outgoing);
            Component bubble = createBubbleBlock(message, outgoing);
            if(outgoing) {
                pack.add(bubble);
                pack.add(Box.createHorizontalStrut(8));
                pack.add(avatar);
                row.add(pack, BorderLayout.EAST);
            } else {
                pack.add(avatar);
                pack.add(Box.createHorizontalStrut(8));
                pack.add(bubble);
                row.add(pack, BorderLayout.WEST);
            }
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
            return row;
        }

        private Component createBubbleBlock(ChatMessage message, boolean outgoing) {
            JPanel block = new JPanel();
            block.setOpaque(false);
            block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));

            JLabel name = new JLabel((outgoing ? "我 / " : "") + message.sender + "  " + message.time);
            name.setFont(UI_FONT_SMALL);
            name.setForeground(MUTED);
            name.setAlignmentX(outgoing ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);

            if(message.fileMessage) {
                return createFileBubbleBlock(message, outgoing, name);
            }

            JLabel text = new JLabel("<html><div style=\"width:" + messageWidth(message.body)
                    + "px;\">" + htmlText(message.body) + "</div></html>");
            text.setFont(UI_FONT);
            text.setForeground(TEXT);

            JPanel bubble = new BubblePanel(outgoing ? OUTGOING_BUBBLE : INCOMING_BUBBLE,
                    outgoing ? new Color(147, 197, 253) : BORDER_LIGHT,
                    RADIUS_LG);
            bubble.setLayout(new BorderLayout());
            bubble.setBorder(pad(10, 12, 10, 12));
            bubble.add(text, BorderLayout.CENTER);
            Dimension bubbleSize = bubble.getPreferredSize();
            bubble.setMaximumSize(new Dimension(bubbleSize.width, bubbleSize.height));
            bubble.setAlignmentX(outgoing ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);

            block.add(name);
            block.add(Box.createVerticalStrut(SPACE_XS));
            block.add(bubble);
            block.setMaximumSize(new Dimension(470, block.getPreferredSize().height));
            return block;
        }

        private int messageWidth(String body) {
            if(body == null || body.length() == 0) return 42;
            int maxLine = 0;
            int currentLine = 0;
            for(int i=0;i<body.length();i++) {
                char ch = body.charAt(i);
                if(ch == '\n' || ch == '\r') {
                    maxLine = Math.max(maxLine, currentLine);
                    currentLine = 0;
                } else {
                    currentLine += ch < 128 ? 7 : 14;
                }
            }
            maxLine = Math.max(maxLine, currentLine);
            return Math.max(42, Math.min(280, maxLine));
        }

        private Component createFileBubbleBlock(final ChatMessage message, boolean outgoing, JLabel name) {
            if(isImageFile(message.fileName) && message.fileData != null) {
                ImageIcon imageIcon = createThumbnailIcon(message.fileData, 240, 170);
                if(imageIcon != null) {
                    return createImageBubbleBlock(message, outgoing, name, imageIcon);
                }
            }

            JPanel block = new JPanel();
            block.setOpaque(false);
            block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));

            final BubblePanel card = new BubblePanel(outgoing ? OUTGOING_BUBBLE : INCOMING_BUBBLE,
                    outgoing ? new Color(147, 197, 253) : BORDER_LIGHT,
                    RADIUS_LG);
            card.setLayout(new BorderLayout(SPACE_MD, 0));
            card.setBorder(pad(11, 12, 11, 12));
            card.setMaximumSize(new Dimension(292, 82));
            card.setPreferredSize(new Dimension(292, 82));

            JLabel icon = new JLabel(mediaBadge(message.fileName));
            icon.setFont(new Font("Dialog", Font.BOLD, 11));
            icon.setForeground(outgoing ? PRIMARY_DARK : new Color(15, 118, 110));
            icon.setHorizontalAlignment(SwingConstants.CENTER);
            icon.setPreferredSize(new Dimension(50, 46));
            icon.setBorder(new RoundedBorder(outgoing ? new Color(147, 197, 253) : BORDER, RADIUS_MD));

            JPanel textPanel = new JPanel();
            textPanel.setOpaque(false);
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            JLabel fileName = new JLabel(clipFileName(message.fileName));
            fileName.setFont(UI_FONT_BOLD);
            fileName.setForeground(TEXT);
            final JLabel fileMeta = new JLabel(fileStatusText(message, outgoing));
            fileMeta.setFont(UI_FONT_SMALL);
            fileMeta.setForeground(outgoing ? PRIMARY_DARK : MUTED);
            textPanel.add(fileName);
            textPanel.add(Box.createVerticalStrut(SPACE_XS));
            textPanel.add(fileMeta);

            card.add(icon, BorderLayout.WEST);
            card.add(textPanel, BorderLayout.CENTER);
            if(!outgoing && message.fileData != null) {
                card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                card.setToolTipText("点击下载文件");
                card.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        saveReceivedFile(message, fileMeta);
                    }
                });
            }

            card.setAlignmentX(outgoing ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
            block.add(name);
            block.add(Box.createVerticalStrut(SPACE_XS));
            block.add(card);
            block.setMaximumSize(new Dimension(470, 104));
            return block;
        }

        private Component createImageBubbleBlock(final ChatMessage message, boolean outgoing,
                JLabel name, ImageIcon imageIcon) {
            JPanel block = new JPanel();
            block.setOpaque(false);
            block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));

            final BubblePanel card = new BubblePanel(outgoing ? OUTGOING_BUBBLE : INCOMING_BUBBLE,
                    outgoing ? new Color(147, 197, 253) : BORDER_LIGHT,
                    RADIUS_LG);
            card.setLayout(new BorderLayout());
            card.setBorder(pad(SPACE_SM));

            JLabel imageLabel = new JLabel(imageIcon);
            imageLabel.setBorder(new RoundedBorder(outgoing ? new Color(147, 197, 253) : BORDER, RADIUS_MD));
            card.add(imageLabel, BorderLayout.CENTER);

            JLabel meta = new JLabel(displayFileSize(message.fileSize)
                    + (outgoing ? " · 已发送" : " · 点击下载"));
            meta.setFont(UI_FONT_SMALL);
            meta.setForeground(outgoing ? PRIMARY_DARK : MUTED);
            meta.setBorder(pad(6, 2, 0, 2));
            card.add(meta, BorderLayout.SOUTH);

            Dimension preferred = new Dimension(
                    Math.max(160, imageIcon.getIconWidth() + 18),
                    imageIcon.getIconHeight() + 42);
            card.setPreferredSize(preferred);
            card.setMaximumSize(preferred);

            if(!outgoing) {
                card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                card.setToolTipText("点击下载图片");
                card.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        saveReceivedFile(message, null);
                    }
                });
            }

            card.setAlignmentX(outgoing ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
            block.add(name);
            block.add(Box.createVerticalStrut(SPACE_XS));
            block.add(card);
            block.setMaximumSize(new Dimension(430, preferred.height + 24));
            return block;
        }

        private ImageIcon createThumbnailIcon(byte[] data, int maxWidth, int maxHeight) {
            try {
                Image image = ImageIO.read(new ByteArrayInputStream(data));
                if(image == null) return null;
                int width = image.getWidth(null);
                int height = image.getHeight(null);
                if(width <= 0 || height <= 0) return null;
                double scale = Math.min(maxWidth / (double)width, maxHeight / (double)height);
                scale = Math.min(1.0, scale);
                int targetWidth = Math.max(1, (int)Math.round(width * scale));
                int targetHeight = Math.max(1, (int)Math.round(height * scale));
                Image scaled = image.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            } catch(Exception e) {
                return null;
            }
        }

        private boolean isImageFile(String name) {
            String lower = extensionSource(name);
            return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                    || lower.endsWith(".gif") || lower.endsWith(".bmp") || lower.endsWith(".webp");
        }

        private boolean isAudioFile(String name) {
            String lower = extensionSource(name);
            return lower.endsWith(".mp3") || lower.endsWith(".wav") || lower.endsWith(".aac")
                    || lower.endsWith(".flac") || lower.endsWith(".m4a") || lower.endsWith(".ogg");
        }

        private boolean isVideoFile(String name) {
            String lower = extensionSource(name);
            return lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".mov")
                    || lower.endsWith(".mkv") || lower.endsWith(".wmv") || lower.endsWith(".webm");
        }

        private String mediaBadge(String name) {
            if(isImageFile(name)) return "图片";
            if(isAudioFile(name)) return "音频";
            if(isVideoFile(name)) return "视频";
            return "文件";
        }

        private String extensionSource(String name) {
            return name == null ? "" : name.toLowerCase(Locale.ROOT);
        }

        private String fileStatusText(ChatMessage message, boolean outgoing) {
            String type = "文件";
            if(isAudioFile(message.fileName)) type = "音频";
            if(isVideoFile(message.fileName)) type = "视频";
            if(isImageFile(message.fileName)) type = "图片";
            if(outgoing) return type + " · " + displayFileSize(message.fileSize) + " · 已发送";
            if(message.savedFile != null) return type + " · " + displayFileSize(message.fileSize) + " · 已保存";
            return type + " · " + displayFileSize(message.fileSize) + " · 点击下载";
        }

        private String clipFileName(String name) {
            if(name == null) return "";
            if(name.length() <= 24) return name;
            return name.substring(0, 10) + "..." + name.substring(name.length() - 10);
        }

        private void saveReceivedFile(ChatMessage message, JLabel statusLabel) {
            if(message.savedFile != null) {
                JOptionPane.showMessageDialog(ChatClient.this,
                        "文件已保存到：\n" + message.savedFile.getAbsolutePath(),
                        "文件已保存",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("保存文件");
            chooser.setSelectedFile(new File(message.fileName));
            chooser.setCurrentDirectory(new File(System.getProperty("user.home"), "Desktop"));
            if(chooser.showSaveDialog(ChatClient.this) != JFileChooser.APPROVE_OPTION) return;

            File selected = chooser.getSelectedFile();
            if(selected.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(ChatClient.this,
                        "文件已存在，是否覆盖？\n" + selected.getAbsolutePath(),
                        "确认覆盖",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if(overwrite != JOptionPane.YES_OPTION) return;
            }

            try {
                Files.write(selected.toPath(), message.fileData);
                message.savedFile = selected;
                if(statusLabel != null) {
                    statusLabel.setText(fileStatusText(message, false));
                    statusLabel.setForeground(SUCCESS);
                }
                appendLog("[file saved] " + selected.getAbsolutePath());
            } catch(Exception e) {
                JOptionPane.showMessageDialog(ChatClient.this,
                        "保存失败：" + e.getMessage(),
                        "保存文件",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        public void clear() {
            removeAll();
            showEmptyState();
            revalidate();
            repaint();
        }

        private void showEmptyState() {
            removeAll();
            JPanel state = createEmptyState("选择一个会话开始聊天", "消息、文件和群聊记录会显示在这里");
            state.setAlignmentX(Component.LEFT_ALIGNMENT);
            state.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
            add(Box.createVerticalGlue());
            add(state);
            add(Box.createVerticalGlue());
            showingEmptyState = true;
        }
    }

    class AvatarView extends JComponent {
        private String name;
        private boolean outgoing;

        AvatarView(String name, boolean outgoing) {
            setAvatar(name, outgoing);
            setPreferredSize(new Dimension(36, 36));
            setMinimumSize(new Dimension(36, 36));
            setMaximumSize(new Dimension(36, 36));
        }

        public void setAvatar(String name, boolean outgoing) {
            this.name = name == null || name.length() == 0 ? "?" : name;
            this.outgoing = outgoing;
            repaint();
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color start = outgoing ? PRIMARY : new Color(15, 118, 110);
            Color end = outgoing ? new Color(96, 165, 250) : new Color(45, 212, 191);
            g2.setPaint(new GradientPaint(0, 0, start, getWidth(), getHeight(), end));
            g2.fillOval(1, 1, 34, 34);
            g2.setColor(new Color(255, 255, 255, 90));
            g2.drawOval(2, 2, 32, 32);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 13));
            String text = avatarText(name);
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(text)) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2 - 1;
            g2.drawString(text, x, y);
            g2.dispose();
        }

        private String avatarText(String source) {
            String trimmed = source.trim();
            if(trimmed.length() == 0) return "?";
            return trimmed.substring(0, 1).toUpperCase();
        }
    }
}
