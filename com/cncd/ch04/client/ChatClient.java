package com.cncd.ch04.client;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
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
    private static final String VIDEO_CALL_PREFIX = "__VIDEO_CALL__|";
    private static final String PRIVATE_MESSAGE_PREFIX = "__CHAT_MSG__|";
    private static final String READ_RECEIPT_PREFIX = "__READ__|";
    private static final String VIDEO_INVITE = "INVITE";
    private static final String VIDEO_ACCEPT = "ACCEPT";
    private static final String VIDEO_REJECT = "REJECT";
    private static final String VIDEO_HANGUP = "HANGUP";

    private static final long MAX_FILE_BYTES = 5L * 1024L * 1024L;
    private static final String ACCOUNT_FILE = "accounts.properties";
    private static final String LOGIN_PREF_FILE = "login.properties";
    private static final String MOMENTS_FILE = "moments.txt";
    private static final String PENDING_MESSAGES_FILE = "pending-private.txt";
    private static final String MOMENT_RECORD_VERSION = "M2";
    private static final int MOMENT_TEXT_LIMIT = 300;
    private static final String BROADCAST_CHAT = "广播";
    private static final String GROUP_LABEL_PREFIX = "群聊 · ";
    private static final Color APP_BACKGROUND = new Color(226, 229, 236);
    private static final Color RAIL_BACKGROUND = new Color(226, 229, 238);
    private static final Color SIDEBAR_BACKGROUND = new Color(243, 244, 247);
    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color SURFACE = Color.WHITE;
    private static final Color SURFACE_SOFT = new Color(248, 249, 251);
    private static final Color CHAT_BACKGROUND = new Color(246, 247, 249);
    private static final Color PRIMARY = new Color(7, 193, 96);
    private static final Color PRIMARY_DARK = new Color(5, 166, 83);
    private static final Color PRIMARY_SOFT = new Color(240, 251, 244);
    private static final Color SIDEBAR_SELECTED = new Color(224, 228, 234);
    private static final Color BORDER = new Color(216, 220, 228);
    private static final Color BORDER_LIGHT = new Color(232, 235, 241);
    private static final Color TEXT = new Color(31, 35, 42);
    private static final Color MUTED = new Color(112, 118, 128);
    private static final Color SOFT_MUTED = new Color(166, 172, 182);
    private static final Color SUCCESS = new Color(7, 193, 96);
    private static final Color WARNING = new Color(224, 150, 35);
    private static final Color DANGER = new Color(219, 68, 55);
    private static final Color INCOMING_BUBBLE = Color.WHITE;
    private static final Color OUTGOING_BUBBLE = new Color(149, 236, 105);
    private static final int RADIUS_SM = 4;
    private static final int RADIUS_MD = 6;
    private static final int RADIUS_LG = 8;
    private static final int RADIUS_XL = 10;
    private static final int SPACE_XS = 4;
    private static final int SPACE_SM = 8;
    private static final int SPACE_MD = 12;
    private static final int SPACE_LG = 16;
    private static final int SPACE_XL = 20;
    private static final int RAIL_WIDTH = 56;
    private static final int SIDEBAR_WIDTH = 300;
    private static final int BUTTON_HEIGHT = 34;
    private static final int INPUT_HEIGHT = 38;
    private static final Font UI_FONT = new Font("Microsoft YaHei UI", Font.PLAIN, 13);
    private static final Font UI_FONT_BOLD = new Font("Microsoft YaHei UI", Font.BOLD, 13);
    private static final Font UI_FONT_SMALL = new Font("Microsoft YaHei UI", Font.PLAIN, 12);
    private static final Font TITLE_FONT = new Font("Microsoft YaHei UI", Font.BOLD, 17);
    private static final Font PAGE_TITLE_FONT = new Font("Microsoft YaHei UI", Font.BOLD, 22);
    private static final Font CHAT_TEXT_FONT = new Font("Dialog", Font.PLAIN, 15);
    private static final Font CHAT_TEXT_SMALL_FONT = new Font("Dialog", Font.PLAIN, 12);
    private static final Font EMOJI_FONT = new Font("Dialog", Font.PLAIN, 26);
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
    private static final String[] IMAGE_EXTENSIONS = {
            ".png", ".jpg", ".jpeg", ".gif", ".bmp", ".webp"
    };
    private static final String[] AUDIO_EXTENSIONS = {
            ".mp3", ".wav", ".aac", ".flac", ".m4a", ".ogg", ".wma", ".amr", ".opus"
    };
    private static final String[] VIDEO_EXTENSIONS = {
            ".mp4", ".avi", ".mov", ".mkv", ".wmv", ".webm"
    };
    private static final Color[] AVATAR_COLORS = {
            new Color(7, 193, 96), new Color(87, 107, 149), new Color(238, 153, 71),
            new Color(93, 156, 236), new Color(155, 89, 182), new Color(80, 180, 170)
    };

    JPanel northPanel, southPanel, eastPanel;
    JTextField txtHost, txtPort, txtNick;
    JTextArea msgWindow;
    JButton buttonConnect, buttonSend, buttonRefresh, buttonAddFriend;
    JButton buttonFile, buttonImage, buttonVoice, buttonRecord, buttonEmoji, buttonLog;
    JButton buttonHeaderMore, buttonVideo;
    JButton buttonCreateGroup;
    JButton buttonProfile, buttonMoments;
    JLabel statusLabel, conversationTitleLabel, conversationSubtitleLabel;
    JLabel sidebarNameLabel, sidebarSignatureLabel;
    JPanel attachmentPanel;
    JLabel attachmentIconLabel, attachmentNameLabel, attachmentMetaLabel;
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
    private Map<String, ConversationMeta> conversationMeta = new HashMap<String, ConversationMeta>();
    private Set<String> sentFriendRequests = new HashSet<String>();
    private Set<String> incomingFriendRequests = new HashSet<String>();
    private Set<String> visibleUsers = new HashSet<String>();
    private Map<String, VideoCallWindow> videoCallWindows = new HashMap<String, VideoCallWindow>();
    private Set<String> pendingVideoCalls = new HashSet<String>();
    private long conversationSequence = 0;
    private Map<String, PendingPrivateMessage> pendingPrivateMessages = new LinkedHashMap<String, PendingPrivateMessage>();
    private Map<String, ChatMessage> trackedOutgoingMessages = new LinkedHashMap<String, ChatMessage>();
    private Map<String, JLabel> trackedStatusLabels = new HashMap<String, JLabel>();
    private Map<String, java.util.List<String>> pendingReadReceipts = new HashMap<String, java.util.List<String>>();
    private LinkedList<String> localBroadcastEchoes = new LinkedList<String>();
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
        String conversationKey;
        String messageId;
        String deliveryStatus;

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

    private static class ConversationMeta {
        String preview = "";
        String time = "";
        int unread = 0;
        long sequence = 0;
    }

    private static class PendingPrivateMessage {
        String id;
        String target;
        String body;
        String time;
        ChatMessage message;

        PendingPrivateMessage(String id, String target, String body, String time) {
            this.id = id;
            this.target = target;
            this.body = body;
            this.time = time;
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
                new MatteBorder(0, 0, 1, 0, BORDER),
                pad(8, 18, 8, 18)));

        JLabel appTitle = new JLabel("CNCD Chat");
        appTitle.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 16));
        appTitle.setForeground(TEXT);
        JLabel appSubTitle = new JLabel("桌面聊天实验");
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
        AvatarView brandAvatar = new AvatarView("C", true);
        brandAvatar.setPreferredSize(new Dimension(30, 30));
        brandAvatar.setMinimumSize(new Dimension(30, 30));
        brandAvatar.setMaximumSize(new Dimension(30, 30));
        brand.add(brandAvatar);
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
        BubblePanel statusPill = createPillPanel(PRIMARY_SOFT, BORDER_LIGHT);
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
        JPanel leftShell = new JPanel(new BorderLayout());
        leftShell.setBackground(SIDEBAR_BACKGROUND);
        leftShell.add(createNavigationRail(), BorderLayout.WEST);
        leftShell.add(createConversationPanel(), BorderLayout.CENTER);
        mainPanel.add(createChatPanel(), BorderLayout.CENTER);
        mainPanel.add(leftShell, BorderLayout.WEST);
        return mainPanel;
    }

    private JPanel createNavigationRail() {
        JPanel rail = new JPanel();
        rail.setPreferredSize(new Dimension(RAIL_WIDTH, 0));
        rail.setBackground(RAIL_BACKGROUND);
        rail.setBorder(new MatteBorder(0, 0, 0, 1, BORDER));
        rail.setLayout(new BoxLayout(rail, BoxLayout.Y_AXIS));

        AvatarView logo = new AvatarView("C", true);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setPreferredSize(new Dimension(36, 36));
        logo.setMinimumSize(new Dimension(36, 36));
        logo.setMaximumSize(new Dimension(36, 36));

        rail.add(Box.createVerticalStrut(SPACE_LG));
        rail.add(logo);
        rail.add(Box.createVerticalStrut(SPACE_XL));

        buttonRefresh = createRailButton("刷", "刷新在线列表");
        buttonAddFriend = createRailButton("友", "添加选中好友");
        buttonCreateGroup = createRailButton("群", "创建群聊");
        buttonProfile = createRailButton("我", "个人资料");
        buttonMoments = createRailButton("圈", "朋友圈");
        buttonLog = createRailButton("记", "聊天记录");

        buttonRefresh.addActionListener(this);
        buttonAddFriend.addActionListener(this);
        buttonCreateGroup.addActionListener(this);
        buttonProfile.addActionListener(this);
        buttonMoments.addActionListener(this);
        buttonLog.addActionListener(this);

        rail.add(buttonRefresh);
        rail.add(Box.createVerticalStrut(SPACE_SM));
        rail.add(buttonAddFriend);
        rail.add(Box.createVerticalStrut(SPACE_SM));
        rail.add(buttonCreateGroup);
        rail.add(Box.createVerticalGlue());
        rail.add(buttonProfile);
        rail.add(Box.createVerticalStrut(SPACE_SM));
        rail.add(buttonMoments);
        rail.add(Box.createVerticalStrut(SPACE_SM));
        rail.add(buttonLog);
        rail.add(Box.createVerticalStrut(SPACE_LG));
        return rail;
    }

    private JPanel createConversationPanel() {
        eastPanel = new JPanel(new BorderLayout(0, SPACE_MD));
        eastPanel.setBackground(SIDEBAR_BACKGROUND);
        eastPanel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 0, 1, BORDER),
                pad(SPACE_MD, SPACE_SM, SPACE_SM, SPACE_SM)));
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
        onlineList.setFixedCellHeight(78);
        onlineList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        onlineList.setSelectionBackground(SIDEBAR_SELECTED);
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

        JScrollPane userScroll = createModernScrollPane(onlineList, SIDEBAR_BACKGROUND);
        conversationListPanel = new JPanel(new CardLayout());
        conversationListPanel.setOpaque(false);
        conversationListPanel.add(userScroll, "list");
        conversationListPanel.add(createEmptyConversationPanel(), "empty");
        eastPanel.add(conversationListPanel, BorderLayout.CENTER);
        return eastPanel;
    }

    private JPanel createConversationSearchPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = createSectionTitle("会话");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        conversationSearchField = new PromptTextField("", 14, "搜索");
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
        JPanel accountPanel = new JPanel(new BorderLayout(SPACE_MD, 0));
        accountPanel.setOpaque(false);
        accountPanel.setBorder(pad(SPACE_SM, SPACE_XS, SPACE_MD, SPACE_XS));

        sidebarAvatar = new AvatarView("?", false);
        sidebarNameLabel = new JLabel("未登录");
        sidebarNameLabel.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 16));
        sidebarNameLabel.setForeground(TEXT);
        sidebarSignatureLabel = new JLabel("请先登录账号");
        sidebarSignatureLabel.setFont(UI_FONT_SMALL);
        sidebarSignatureLabel.setForeground(MUTED);
        JLabel onlineBadge = new JLabel("在线");
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
                new MatteBorder(0, 0, 1, 0, BORDER),
                pad(SPACE_MD, SPACE_MD, SPACE_MD, SPACE_MD)));
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
        buttonVideo = createButton("视频", false);
        buttonVideo.setFont(UI_FONT_BOLD);
        buttonVideo.setPreferredSize(new Dimension(58, BUTTON_HEIGHT));
        buttonVideo.setToolTipText("选择在线好友后发起视频通话");
        buttonVideo.addActionListener(this);
        buttonHeaderMore = createButton("⋯", false);
        buttonHeaderMore.setFont(new Font("Dialog", Font.BOLD, 18));
        buttonHeaderMore.setPreferredSize(new Dimension(40, BUTTON_HEIGHT));
        buttonHeaderMore.setToolTipText("更多聊天操作");
        buttonHeaderMore.addActionListener(this);
        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, SPACE_SM, 0));
        headerActions.setOpaque(false);
        headerActions.add(buttonVideo);
        headerActions.add(buttonHeaderMore);
        header.add(headerActions, BorderLayout.EAST);

        chatPanel.add(header, BorderLayout.NORTH);
        chatPanel.add(createHistoryPanel(), BorderLayout.CENTER);
        chatPanel.add(createInputPanel(), BorderLayout.SOUTH);
        return chatPanel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CHAT_BACKGROUND);
        panel.setBorder(pad(SPACE_LG, SPACE_MD, SPACE_LG, SPACE_MD));
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
                pad(SPACE_MD, SPACE_MD, SPACE_MD, SPACE_MD)));

        attachmentPanel = createAttachmentPanel();
        msgWindow = new PromptTextArea("输入消息");
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
        buttonSend.setPreferredSize(new Dimension(76, 36));
        buttonSend.setToolTipText("发送消息 Enter");
        buttonEmoji = createButton("笑", false);
        buttonEmoji.setFont(UI_FONT_BOLD);
        buttonEmoji.setToolTipText("插入表情");
        buttonEmoji.setBorder(pad(4, 0, 4, 0));
        buttonEmoji.setPreferredSize(new Dimension(40, 34));
        buttonImage = createButton("图", false);
        buttonImage.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 14));
        buttonImage.setToolTipText("发送图片");
        buttonImage.setBorder(pad(4, 0, 4, 0));
        buttonImage.setPreferredSize(new Dimension(40, 34));
        buttonVoice = createButton("音", false);
        buttonVoice.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 14));
        buttonVoice.setToolTipText("发送语音文件");
        buttonVoice.setBorder(pad(4, 0, 4, 0));
        buttonVoice.setPreferredSize(new Dimension(40, 34));
        buttonRecord = createButton("录", false);
        buttonRecord.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 14));
        buttonRecord.setToolTipText("录制一段语音并发送");
        buttonRecord.setBorder(pad(4, 0, 4, 0));
        buttonRecord.setPreferredSize(new Dimension(40, 34));
        buttonFile = createButton("文", false);
        buttonFile.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 14));
        buttonFile.setToolTipText("选择本地文件");
        buttonFile.setBorder(pad(4, 0, 4, 0));
        buttonFile.setPreferredSize(new Dimension(40, 34));
        buttonSend.addActionListener(this);
        buttonEmoji.addActionListener(this);
        buttonImage.addActionListener(this);
        buttonVoice.addActionListener(this);
        buttonRecord.addActionListener(this);
        buttonFile.addActionListener(this);
        buttonFile.setEnabled(true);
        buttonSend.setEnabled(false);

        JPanel composer = new JPanel(new BorderLayout(0, SPACE_SM));
        composer.setOpaque(false);

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        JPanel inputTools = new JPanel(new FlowLayout(FlowLayout.LEFT, SPACE_XS, 0));
        inputTools.setOpaque(false);
        inputTools.add(buttonEmoji);
        inputTools.add(buttonImage);
        inputTools.add(buttonVoice);
        inputTools.add(buttonRecord);
        inputTools.add(buttonFile);
        toolbar.add(inputTools, BorderLayout.WEST);

        JScrollPane messageScroll = new JScrollPane(msgWindow);
        messageScroll.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_LIGHT));
        messageScroll.getViewport().setBackground(Color.WHITE);
        messageScroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        messageScroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        messageScroll.setPreferredSize(new Dimension(0, 96));

        JPanel actionRow = new JPanel(new BorderLayout());
        actionRow.setOpaque(false);
        JLabel hint = createHintLabel("Enter 发送 · Shift+Enter 换行");
        actionRow.add(hint, BorderLayout.WEST);
        actionRow.add(buttonSend, BorderLayout.EAST);

        composer.add(toolbar, BorderLayout.NORTH);
        composer.add(messageScroll, BorderLayout.CENTER);
        composer.add(actionRow, BorderLayout.SOUTH);

        southPanel.add(attachmentPanel, BorderLayout.NORTH);
        southPanel.add(composer, BorderLayout.CENTER);
        return southPanel;
    }

    private JPanel createAttachmentPanel() {
        JPanel panel = new BubblePanel(PRIMARY_SOFT, new Color(194, 235, 210), RADIUS_MD);
        panel.setLayout(new BorderLayout(SPACE_MD, 0));
        panel.setBorder(pad(SPACE_SM, SPACE_MD, SPACE_SM, SPACE_MD));
        panel.setVisible(false);

        attachmentIconLabel = new JLabel("文件");
        attachmentIconLabel.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 12));
        attachmentIconLabel.setForeground(PRIMARY_DARK);
        attachmentIconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        attachmentIconLabel.setPreferredSize(new Dimension(48, 30));
        attachmentIconLabel.setBorder(new RoundedBorder(new Color(194, 235, 210), RADIUS_SM));

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

        panel.add(attachmentIconLabel, BorderLayout.WEST);
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
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getHorizontalScrollBar().setUnitIncrement(18);
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scroll.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));
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
        field.setBorder(new CompoundBorder(new RoundedBorder(BORDER_LIGHT, RADIUS_MD), pad(8, 11, 8, 11)));
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, INPUT_HEIGHT));
    }

    private void styleTextArea(JTextArea area) {
        area.setFont(CHAT_TEXT_FONT);
        area.setForeground(TEXT);
        area.setBackground(Color.WHITE);
        area.setCaretColor(PRIMARY_DARK);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(pad(12, 14, 12, 14));
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

    private JButton createRailButton(String text, String tooltip) {
        JButton button = new RailButton(text);
        button.setToolTipText(tooltip);
        return button;
    }

    private JButton createToolButton(String text) {
        JButton button = createButton(text, false);
        button.setPreferredSize(new Dimension(0, 32));
        button.setFont(UI_FONT_BOLD);
        return button;
    }

    private void showConversationMoreMenu() {
        if(buttonHeaderMore == null) return;
        JPopupMenu menu = new JPopupMenu();
        menu.setBorder(new CompoundBorder(new RoundedBorder(BORDER, RADIUS_MD), pad(SPACE_XS)));
        menu.add(createConversationMenuItem("发起视频通话", new Runnable() {
            public void run() {
                startVideoCall();
            }
        }));
        menu.addSeparator();
        menu.add(createConversationMenuItem("刷新在线列表", new Runnable() {
            public void run() {
                refreshUsers();
            }
        }));
        menu.add(createConversationMenuItem("打开聊天记录", new Runnable() {
            public void run() {
                showLogPath();
            }
        }));
        menu.addSeparator();
        menu.add(createConversationMenuItem("清空当前窗口", new Runnable() {
            public void run() {
                if(historyWindow != null) historyWindow.clear();
            }
        }));
        menu.show(buttonHeaderMore, 0, buttonHeaderMore.getHeight() + SPACE_XS);
    }

    private JMenuItem createConversationMenuItem(String text, final Runnable action) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(UI_FONT);
        item.setForeground(TEXT);
        item.setBackground(SURFACE);
        item.setBorder(pad(7, 10, 7, 18));
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
        return item;
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
        JPanel grid = new JPanel(new GridLayout(0, 6, SPACE_SM, SPACE_SM));
        grid.setBackground(Color.WHITE);
        for(int i=0;i<QUICK_EMOJIS.length;i++) {
            final String emoji = QUICK_EMOJIS[i];
            JButton item = new JButton(emoji);
            item.setFont(EMOJI_FONT);
            item.setFocusPainted(false);
            item.setContentAreaFilled(false);
            item.setBorder(new RoundedBorder(BORDER_LIGHT, RADIUS_SM));
            item.setPreferredSize(new Dimension(48, 44));
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
        if(args.length > 0 && "--web".equalsIgnoreCase(args[0])) {
            try {
                String[] webArgs = new String[Math.max(0, args.length - 1)];
                System.arraycopy(args, 1, webArgs, 0, webArgs.length);
                com.cncd.ch04.web.WebChatLauncher.main(webArgs);
                return;
            } catch(Exception e) {
                System.out.println("Unable to start Web client, falling back to Swing: " + e.getMessage());
            }
        }
        if(args.length > 0 && "--swing".equalsIgnoreCase(args[0])) {
            String[] swingArgs = new String[Math.max(0, args.length - 1)];
            System.arraycopy(args, 1, swingArgs, 0, swingArgs.length);
            args = swingArgs;
        }
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
        loadPendingPrivateMessages();
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
                props.getProperty("port", ChatClient.portText),
                props.getProperty("username", ""));
    }

    private static void saveLoginPreference(LoginData login) {
        try {
            Files.createDirectories(clientDataRoot());
            Properties props = new Properties();
            props.setProperty("host", login.host == null ? ChatClient.serverText : login.host);
            props.setProperty("port", login.port == null ? ChatClient.portText : login.port);
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

        if(text.startsWith("Unable to find user ")) {
            String target = text.substring("Unable to find user ".length()).trim();
            if(markLatestOutgoingAsQueued(target)) {
                return new ChatMessage(MessageKind.SYSTEM, "系统",
                        target + " 当前离线，消息已转为待发送。", time);
            }
        }

        if(text.startsWith("[private to ")) {
            int end = text.indexOf(']');
            String body = end >= 0 ? text.substring(end + 1).trim() : text;
            String target = end > 0 ? text.substring("[private to ".length(), end).trim() : null;
            return withConversation(new ChatMessage(MessageKind.OUTGOING, ownNick(), body, time), target);
        }

        if(text.startsWith("[private] ")) {
            String payload = text.substring("[private] ".length()).trim();
            int colon = payload.indexOf(':');
            if(colon > 0) {
                String sender = payload.substring(0, colon).trim();
                String body = payload.substring(colon + 1).trim();
                if(handleProtocolMessage(sender, body, time)) return null;
                return withConversation(new ChatMessage(messageKindFor(sender), sender, body, time), sender);
            }
        }

        if(text.startsWith("[file to ")) {
            int end = text.indexOf(']');
            String body = end >= 0 ? "发送文件 " + text.substring(end + 1).trim() : text;
            String target = end > 0 ? text.substring("[file to ".length(), end).trim() : null;
            return withConversation(new ChatMessage(MessageKind.OUTGOING, ownNick(), body, time), target);
        }

        if(text.startsWith("[file from ")) {
            int end = text.indexOf(']');
            String sender = text.substring("[file from ".length(), end > 0 ? end : text.length()).trim();
            String body = end >= 0 ? "收到文件 " + text.substring(end + 1).trim() : text;
            return withConversation(new ChatMessage(messageKindFor(sender), sender, body, time), sender);
        }

        if(isSystemMessage(text)) {
            return new ChatMessage(MessageKind.SYSTEM, "系统", text, time);
        }

        int colon = text.indexOf(':');
        if(colon > 0) {
            String sender = text.substring(0, colon).trim();
            String body = text.substring(colon + 1).trim();
            if(sender.length() > 0 && sender.indexOf(' ') < 0 && !sender.equalsIgnoreCase("Server")) {
                if(sender.equalsIgnoreCase(ownNick()) && consumeLocalBroadcastEcho(body)) return null;
                return withConversation(new ChatMessage(messageKindFor(sender), sender, body, time), BROADCAST_CHAT);
            }
        }

        return new ChatMessage(MessageKind.SYSTEM, "系统", text, time);
    }

    private ChatMessage withConversation(ChatMessage message, String conversationKey) {
        if(message != null) message.conversationKey = conversationKey;
        return message;
    }

    private void rememberLocalBroadcastEcho(String body) {
        localBroadcastEchoes.addLast(normalizeMessageBody(body));
        while(localBroadcastEchoes.size() > 12) localBroadcastEchoes.removeFirst();
    }

    private boolean consumeLocalBroadcastEcho(String body) {
        String normalized = normalizeMessageBody(body);
        Iterator<String> it = localBroadcastEchoes.iterator();
        while(it.hasNext()) {
            if(normalized.equals(it.next())) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    private String normalizeMessageBody(String body) {
        return body == null ? "" : body.trim();
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
        if(handlePrivateTextProtocolMessage(sender, body, time)) return true;
        if(handleReadReceiptMessage(sender, body)) return true;
        if(handleVideoCallProtocolMessage(sender, body)) return true;
        if(handleMomentProtocolMessage(sender, body)) return true;
        if(handleGroupProtocolMessage(sender, body, time)) return true;
        return handleFriendProtocolMessage(sender, body);
    }

    private boolean handlePrivateTextProtocolMessage(String sender, String body, String time) {
        if(!body.startsWith(PRIVATE_MESSAGE_PREFIX)) return false;
        String payload = body.substring(PRIVATE_MESSAGE_PREFIX.length());
        String[] parts = payload.split("\\|", 2);
        if(parts.length != 2) return true;
        String messageId = parts[0];
        String message = decodeToken(parts[1]);
        ChatMessage chatMessage = withConversation(new ChatMessage(MessageKind.INCOMING,
                sender, message, time), sender);
        chatMessage.messageId = messageId;
        if(historyWindow != null) historyWindow.addMessage(chatMessage);
        rememberReadReceipt(sender, messageId);
        return true;
    }

    private boolean handleReadReceiptMessage(String sender, String body) {
        if(!body.startsWith(READ_RECEIPT_PREFIX)) return false;
        String messageId = body.substring(READ_RECEIPT_PREFIX.length()).trim();
        if(messageId.length() > 0) updateOutgoingStatus(messageId, "已读");
        return true;
    }

    private boolean handleVideoCallProtocolMessage(String sender, String body) {
        if(!body.startsWith(VIDEO_CALL_PREFIX)) return false;
        String action = body.substring(VIDEO_CALL_PREFIX.length()).trim();
        if(VIDEO_INVITE.equals(action)) {
            handleIncomingVideoInvite(sender);
        } else if(VIDEO_ACCEPT.equals(action)) {
            handleVideoAccepted(sender);
        } else if(VIDEO_REJECT.equals(action)) {
            handleVideoRejected(sender);
        } else if(VIDEO_HANGUP.equals(action)) {
            handleVideoHangup(sender);
        }
        return true;
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
                historyWindow.addMessage(withConversation(new ChatMessage(MessageKind.INCOMING,
                        sender + " / " + groupName, message, time), groupLabel(groupName)));
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
            String host = txtHost.getText().trim();
            int port = Integer.parseInt(txtPort.getText().trim());
            setConnectionStatus("正在连接 " + host + ":" + port + " ...", WARNING);
            ck = new ClientKernel(host, port);
            if(ck.isConnected()) {
                ck.addClient(this);
                ck.setNick(txtNick.getText());
                initLogFile(txtNick.getText());
                setConnectionStatus("已连接 " + host + ":" + port
                        + "，本地端口 " + ck.getLocalPort(), SUCCESS);
                setTitle(appName + " - " + txtNick.getText());
                addMsg("<font color=\"#008000\">connected! Local Port:" + ck.getLocalPort() + "</font>");
                refreshUsers();
                uploadOwnMomentsToServer();
            } else {
                String detail = ck.getLastErrorMessage();
                setConnectionStatus("连接失败，请检查服务端、防火墙和端口", DANGER);
                addMsg("<font color=\"#ff0000\">连接失败："
                        + escapeHtml(detail.length() == 0 ? "请确认服务端已启动、IP 和端口正确。" : detail)
                        + "</font>");
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
                conversationSubtitleLabel.setText(online ? "当前为私聊，只发送给 " + selected : "好友离线，文字消息将待发送");
            }
            if(conversationAvatar != null) conversationAvatar.setAvatar(selected, false);
            if(buttonFile != null) buttonFile.setEnabled(true);
        }
        markConversationRead(selected);
        sendPendingReadReceipts(selected);
        updateVideoButtonState();
        updateAttachmentPreview();
        updateSendButtonState();
        if(msgWindow != null) msgWindow.requestFocusInWindow();
    }

    private void updateVideoButtonState() {
        if(buttonVideo == null) return;
        boolean canCall = ck != null && ck.isConnected()
                && selectedChatTarget != null
                && visibleUsers.contains(selectedChatTarget);
        buttonVideo.setEnabled(canCall);
        buttonVideo.setToolTipText(canCall
                ? "和 " + selectedChatTarget + " 视频通话"
                : "请选择在线好友后发起视频通话");
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

    private void updateConversationPreview(ChatMessage message) {
        if(message == null || message.kind == MessageKind.SYSTEM) return;
        String key = message.conversationKey;
        if(key == null || key.length() == 0) key = BROADCAST_CHAT;
        ConversationMeta meta = conversationMeta.get(key);
        if(meta == null) {
            meta = new ConversationMeta();
            conversationMeta.put(key, meta);
        }
        meta.preview = previewText(message);
        meta.time = compactTime(message.time);
        meta.sequence = ++conversationSequence;
        if(message.kind == MessageKind.INCOMING && !isSelectedConversation(key)) {
            meta.unread = Math.min(99, meta.unread + 1);
        } else if(isSelectedConversation(key)) {
            meta.unread = 0;
        }
        if(onlineList != null) {
            String selected = onlineList.getSelectedValue();
            if(userModel != null && modelContains(key)) {
                rebuildConversationList(selected);
            } else {
                onlineList.repaint();
            }
        }
    }

    private void markConversationRead(String key) {
        if(key == null || key.length() == 0) return;
        ConversationMeta meta = conversationMeta.get(key);
        if(meta != null && meta.unread != 0) {
            meta.unread = 0;
            if(onlineList != null) onlineList.repaint();
        }
    }

    private void rememberReadReceipt(String sender, String messageId) {
        if(sender == null || sender.length() == 0 || messageId == null || messageId.length() == 0) return;
        java.util.List<String> ids = pendingReadReceipts.get(sender);
        if(ids == null) {
            ids = new ArrayList<String>();
            pendingReadReceipts.put(sender, ids);
        }
        if(!ids.contains(messageId)) ids.add(messageId);
        if(isSelectedConversation(sender)) sendPendingReadReceipts(sender);
    }

    private void sendPendingReadReceipts(String target) {
        if(target == null || target.length() == 0) return;
        if(ck == null || !ck.isConnected() || !visibleUsers.contains(target)) return;
        java.util.List<String> ids = pendingReadReceipts.remove(target);
        if(ids == null) return;
        for(int i=0;i<ids.size();i++) {
            ck.sendMessage("/msg " + target + " " + READ_RECEIPT_PREFIX + ids.get(i));
        }
    }

    private void updateOutgoingStatus(String messageId, String status) {
        if(messageId == null || messageId.length() == 0) return;
        ChatMessage message = trackedOutgoingMessages.get(messageId);
        if(message != null) message.deliveryStatus = status;
        JLabel label = trackedStatusLabels.get(messageId);
        if(label != null) {
            label.setText(senderLineText(message));
            label.repaint();
        }
    }

    private String senderLineText(ChatMessage message) {
        if(message == null) return "";
        boolean outgoing = message.kind == MessageKind.OUTGOING;
        String text = (outgoing ? "我 / " : "") + message.sender + "  " + message.time;
        if(outgoing && message.deliveryStatus != null && message.deliveryStatus.length() > 0) {
            text += " · " + message.deliveryStatus;
        }
        return text;
    }

    private void registerStatusLabel(ChatMessage message, JLabel label) {
        if(message == null || label == null) return;
        if(message.messageId != null && message.messageId.length() > 0
                && message.kind == MessageKind.OUTGOING) {
            trackedStatusLabels.put(message.messageId, label);
        }
    }

    private boolean markLatestOutgoingAsQueued(String target) {
        if(target == null || target.length() == 0) return false;
        ChatMessage latest = null;
        Iterator<ChatMessage> it = trackedOutgoingMessages.values().iterator();
        while(it.hasNext()) {
            ChatMessage message = it.next();
            if(target.equals(message.conversationKey) && "未读".equals(message.deliveryStatus)) {
                latest = message;
            }
        }
        if(latest == null || latest.messageId == null || pendingPrivateMessages.containsKey(latest.messageId)) {
            return false;
        }
        PendingPrivateMessage pending = new PendingPrivateMessage(latest.messageId,
                target, latest.body, latest.time);
        pending.message = latest;
        pendingPrivateMessages.put(latest.messageId, pending);
        updateOutgoingStatus(latest.messageId, "待发送");
        savePendingPrivateMessages();
        return true;
    }

    private boolean isSelectedConversation(String key) {
        return key != null && onlineList != null && key.equals(onlineList.getSelectedValue());
    }

    private String previewText(ChatMessage message) {
        String speaker = message.kind == MessageKind.OUTGOING ? "我" : shortSenderName(message.sender);
        String content;
        if(message.fileMessage) {
            content = "[" + attachmentTypeLabel(message.fileName) + "] " + message.fileName;
        } else {
            content = message.body == null ? "" : message.body;
        }
        return clipPreview(speaker + ": " + content, 20);
    }

    private String shortSenderName(String sender) {
        if(sender == null || sender.length() == 0) return "对方";
        int slash = sender.indexOf('/');
        if(slash > 0) return sender.substring(0, slash).trim();
        return sender;
    }

    private String clipPreview(String text, int maxLength) {
        if(text == null) return "";
        String normalized = text.replace('\n', ' ').replace('\r', ' ').trim();
        if(normalized.length() <= maxLength) return normalized;
        return normalized.substring(0, maxLength - 1) + "...";
    }

    private String compactTime(String time) {
        if(time == null) return "";
        return time.length() >= 5 ? time.substring(0, 5) : time;
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
                addMsg("<font color=\"#ff0000\">对方当前离线，附件暂不能离线发送，请先发送文字消息或等待对方上线。</font>");
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
            sendPrivate(selectedChatTarget, toSend);
        } else {
            sendBroadcastMessage(toSend);
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

    private void startVideoCall() {
        String target = requireOnlinePrivateTarget("视频通话");
        if(target == null) return;
        VideoCallWindow existing = videoWindowFor(target);
        if(existing != null && existing.isDisplayable()) {
            existing.toFront();
            existing.requestFocus();
            return;
        }
        pendingVideoCalls.add(target);
        sendVideoSignal(target, VIDEO_INVITE);
        VideoCallWindow window = openVideoCallWindow(target, true);
        window.showWaiting("正在等待 " + target + " 接听");
        showVideoSystemMessage("已向 " + target + " 发起视频通话。");
    }

    private void handleIncomingVideoInvite(String sender) {
        if(sender == null || sender.length() == 0 || sender.equalsIgnoreCase(ownNick())) return;
        VideoCallWindow existing = videoWindowFor(sender);
        if(existing != null && existing.isDisplayable()) {
            sendVideoSignal(sender, VIDEO_REJECT);
            showVideoSystemMessage(sender + " 发来视频通话邀请，已因当前通话占用而拒绝。");
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                sender + " 邀请你视频通话，是否接听？",
                "视频通话",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if(choice == JOptionPane.YES_OPTION) {
            sendVideoSignal(sender, VIDEO_ACCEPT);
            VideoCallWindow window = openVideoCallWindow(sender, false);
            window.showConnected();
            showVideoSystemMessage("已接听 " + sender + " 的视频通话。");
        } else {
            sendVideoSignal(sender, VIDEO_REJECT);
            showVideoSystemMessage("已拒绝 " + sender + " 的视频通话邀请。");
        }
    }

    private void handleVideoAccepted(String sender) {
        pendingVideoCalls.remove(sender);
        VideoCallWindow window = openVideoCallWindow(sender, true);
        window.showConnected();
        showVideoSystemMessage(sender + " 已接听视频通话。");
    }

    private void handleVideoRejected(String sender) {
        pendingVideoCalls.remove(sender);
        VideoCallWindow window = videoWindowFor(sender);
        if(window != null) window.finishFromRemote("对方已拒绝通话");
        showVideoSystemMessage(sender + " 已拒绝视频通话。");
    }

    private void handleVideoHangup(String sender) {
        pendingVideoCalls.remove(sender);
        VideoCallWindow window = videoWindowFor(sender);
        if(window != null) window.finishFromRemote("对方已挂断");
        showVideoSystemMessage(sender + " 已结束视频通话。");
    }

    private void sendVideoSignal(String target, String action) {
        if(ck != null && ck.isConnected() && target != null && target.length() > 0) {
            ck.sendMessage("/msg " + target + " " + VIDEO_CALL_PREFIX + action);
        }
    }

    private VideoCallWindow openVideoCallWindow(String peer, boolean outgoing) {
        VideoCallWindow existing = videoWindowFor(peer);
        if(existing != null && existing.isDisplayable()) return existing;
        VideoCallWindow window = new VideoCallWindow(this, peer, outgoing);
        videoCallWindows.put(peer, window);
        window.setLocationRelativeTo(this);
        window.setVisible(true);
        return window;
    }

    private VideoCallWindow videoWindowFor(String peer) {
        if(peer == null) return null;
        VideoCallWindow direct = videoCallWindows.get(peer);
        if(direct != null) return direct;
        Iterator<Map.Entry<String, VideoCallWindow>> it = videoCallWindows.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<String, VideoCallWindow> entry = it.next();
            if(peer.equalsIgnoreCase(entry.getKey())) return entry.getValue();
        }
        return null;
    }

    private void removeVideoWindow(String peer, VideoCallWindow window) {
        Iterator<Map.Entry<String, VideoCallWindow>> windowIt = videoCallWindows.entrySet().iterator();
        while(windowIt.hasNext()) {
            Map.Entry<String, VideoCallWindow> entry = windowIt.next();
            if(entry.getValue() == window || peer.equalsIgnoreCase(entry.getKey())) {
                windowIt.remove();
                break;
            }
        }
        Iterator<String> pendingIt = pendingVideoCalls.iterator();
        while(pendingIt.hasNext()) {
            String pending = pendingIt.next();
            if(peer.equalsIgnoreCase(pending)) {
                pendingIt.remove();
                break;
            }
        }
    }

    private void showVideoSystemMessage(String text) {
        addMsg("<font color=\"#3366cc\">" + escapeHtml(text) + "</font>");
    }

    private void loadUserData() {
        friends.clear();
        chatGroups.clear();
        sentFriendRequests.clear();
        incomingFriendRequests.clear();
        pendingPrivateMessages.clear();
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

    private void loadPendingPrivateMessages() {
        if(currentUserDir == null) return;
        Path file = currentUserDir.resolve(PENDING_MESSAGES_FILE);
        if(!Files.exists(file)) return;
        try {
            java.util.List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for(int i=0;i<lines.size();i++) {
                String line = lines.get(i);
                String[] parts = line.split("\\t", 4);
                if(parts.length != 4) continue;
                String id = parts[0];
                String target = decodeToken(parts[1]);
                String time = decodeToken(parts[2]);
                String body = decodeToken(parts[3]);
                if(id.length() == 0 || target.length() == 0) continue;
                PendingPrivateMessage pending = new PendingPrivateMessage(id, target, body, time);
                ChatMessage message = createOutgoingTrackedMessage(target, "发给 " + target,
                        body, time.length() == 0 ? LocalDateTime.now().format(displayTime) : time,
                        id, "待发送");
                pending.message = message;
                pendingPrivateMessages.put(id, pending);
                showOutgoingTextMessage(message);
            }
        } catch(Exception e) {
            addMsg("<font color=\"#ff0000\">加载待发送消息失败：" + escapeHtml(e.getMessage()) + "</font>");
        }
    }

    private void savePendingPrivateMessages() {
        if(currentUserDir == null) return;
        try {
            Files.createDirectories(currentUserDir);
            java.util.List<String> lines = new ArrayList<String>();
            Iterator<PendingPrivateMessage> it = pendingPrivateMessages.values().iterator();
            while(it.hasNext()) {
                PendingPrivateMessage pending = it.next();
                lines.add(pending.id + "\t" + encodeToken(pending.target) + "\t"
                        + encodeToken(pending.time) + "\t" + encodeToken(pending.body));
            }
            Files.write(currentUserDir.resolve(PENDING_MESSAGES_FILE), lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch(Exception e) {
            addMsg("<font color=\"#ff0000\">保存待发送消息失败：" + escapeHtml(e.getMessage()) + "</font>");
        }
    }

    private void flushPendingPrivateMessages(Set<String> onlineUsers) {
        if(ck == null || !ck.isConnected() || pendingPrivateMessages.size() == 0) return;
        java.util.List<String> sent = new ArrayList<String>();
        Iterator<PendingPrivateMessage> it = pendingPrivateMessages.values().iterator();
        while(it.hasNext()) {
            PendingPrivateMessage pending = it.next();
            if(!onlineUsers.contains(pending.target)) continue;
            sendTrackedPrivateMessage(pending.target, pending.id, pending.body);
            sent.add(pending.id);
            appendLog("[private delivered from queue to " + pending.target + "] " + pending.body);
        }
        for(int i=0;i<sent.size();i++) {
            pendingPrivateMessages.remove(sent.get(i));
        }
        if(sent.size() > 0) savePendingPrivateMessages();
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
        chooseAttachment("选择本地文件", null, null);
    }

    private void chooseAndSendImage() {
        chooseAttachment("选择图片",
                new FileNameExtensionFilter("图片文件 (*.png, *.jpg, *.jpeg, *.gif, *.bmp, *.webp)",
                        "png", "jpg", "jpeg", "gif", "bmp", "webp"),
                "图片");
    }

    private void chooseAndSendVoice() {
        chooseAttachment("选择语音文件",
                new FileNameExtensionFilter("语音文件 (*.mp3, *.wav, *.aac, *.flac, *.m4a, *.ogg, *.wma, *.amr, *.opus)",
                        "mp3", "wav", "aac", "flac", "m4a", "ogg", "wma", "amr", "opus"),
                "语音");
    }

    private void chooseAttachment(String title, FileNameExtensionFilter filter, String requiredType) {
        String target = requireOnlinePrivateTarget("发送文件");
        if(target == null) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);
        chooser.setCurrentDirectory(new File(System.getProperty("user.home"), "Desktop"));
        if(filter != null) {
            chooser.setFileFilter(filter);
            chooser.setAcceptAllFileFilterUsed(false);
        }
        if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if(validateAttachment(file) && validateAttachmentType(file, requiredType)) {
                pendingFile = file;
                updateAttachmentPreview();
                msgWindow.requestFocusInWindow();
            }
        }
    }

    private void recordAndSendVoice() {
        String target = requireOnlinePrivateTarget("录制语音");
        if(target == null) return;
        VoiceRecorderDialog dialog = new VoiceRecorderDialog(this, target);
        dialog.setVisible(true);
        File voiceFile = dialog.getVoiceFileToSend();
        if(voiceFile != null && validateAttachment(voiceFile)) {
            sendFileTo(target, voiceFile);
        }
    }

    private String requireOnlinePrivateTarget(String action) {
        if(ck == null || !ck.isConnected()) {
            addMsg("<font color=\"#ff0000\">请先连接服务器后再" + action + "。</font>");
            return null;
        }
        String selected = onlineList == null ? null : onlineList.getSelectedValue();
        if(selected == null) {
            addMsg("<font color=\"#ff0000\">请先从左侧选择一个在线好友。</font>");
            return null;
        }
        if(isBroadcastConversation(selected)) {
            addMsg("<font color=\"#ff0000\">广播暂不支持" + action + "，请选择一个在线好友私聊。</font>");
            return null;
        }
        if(isGroupConversation(selected)) {
            addMsg("<font color=\"#ff0000\">群聊暂不支持" + action + "，请选择一个在线好友私聊。</font>");
            return null;
        }
        String target = getSelectedPrivateTarget();
        if(target == null || target.length() == 0) {
            addMsg("<font color=\"#ff0000\">请选择一个在线好友后再" + action + "。</font>");
            return null;
        }
        if(!visibleUsers.contains(target)) {
            addMsg("<font color=\"#ff0000\">对方当前离线，暂不能" + action + "。</font>");
            return null;
        }
        return target;
    }

    private boolean validateAttachmentType(File file, String requiredType) {
        if(requiredType == null) return true;
        if("图片".equals(requiredType) && !hasExtension(file.getName(), IMAGE_EXTENSIONS)) {
            addMsg("<font color=\"#ff0000\">请选择图片文件：png、jpg、jpeg、gif、bmp 或 webp。</font>");
            return false;
        }
        if("语音".equals(requiredType) && !hasExtension(file.getName(), AUDIO_EXTENSIONS)) {
            addMsg("<font color=\"#ff0000\">请选择语音文件：mp3、wav、aac、flac、m4a、ogg、wma、amr 或 opus。</font>");
            return false;
        }
        return true;
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
            String type = attachmentTypeLabel(pendingFile.getName());
            if(attachmentIconLabel != null) attachmentIconLabel.setText(type);
            attachmentNameLabel.setText(pendingFile.getName());
            attachmentMetaLabel.setText(type + " · " + displayFileSize(pendingFile.length()) + " · "
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

    private String attachmentTypeLabel(String name) {
        if(hasExtension(name, IMAGE_EXTENSIONS)) return "图片";
        if(hasExtension(name, AUDIO_EXTENSIONS)) return "语音";
        if(hasExtension(name, VIDEO_EXTENSIONS)) return "视频";
        return "文件";
    }

    private static boolean hasExtension(String name, String[] extensions) {
        String lower = name == null ? "" : name.toLowerCase(Locale.ROOT);
        for(int i=0;i<extensions.length;i++) {
            if(lower.endsWith(extensions[i])) return true;
        }
        return false;
    }

    private AudioFormat voiceFormat() {
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                16000.0f, 16, 1, 2, 16000.0f, false);
    }

    private File createVoiceMessageFile() throws IOException {
        Path dir = currentUserDir != null
                ? currentUserDir.resolve("voice")
                : Paths.get(System.getProperty("java.io.tmpdir"), "cncd-chat-voice");
        Files.createDirectories(dir);
        String name = "voice_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                + "_" + UUID.randomUUID().toString().substring(0, 8) + ".wav";
        return dir.resolve(name).toFile();
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
        String messageId = newMessageId();
        String time = LocalDateTime.now().format(displayTime);
        boolean online = visibleUsers.contains(target);
        ChatMessage chatMessage = createOutgoingTrackedMessage(target, "发给 " + target,
                message, time, messageId, online ? "未读" : "待发送");
        showOutgoingTextMessage(chatMessage);
        if(online) {
            sendTrackedPrivateMessage(target, messageId, message);
        } else {
            PendingPrivateMessage pending = new PendingPrivateMessage(messageId, target, message, time);
            pending.message = chatMessage;
            pendingPrivateMessages.put(messageId, pending);
            savePendingPrivateMessages();
        }
        appendLog("[private to " + target + "] " + message);
    }

    private ChatMessage createOutgoingTrackedMessage(String conversationKey, String sender,
            String message, String time, String messageId, String status) {
        ChatMessage chatMessage = withConversation(new ChatMessage(MessageKind.OUTGOING,
                sender, message, time), conversationKey);
        chatMessage.messageId = messageId;
        chatMessage.deliveryStatus = status;
        trackedOutgoingMessages.put(messageId, chatMessage);
        return chatMessage;
    }

    private String newMessageId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private void sendTrackedPrivateMessage(String target, String messageId, String message) {
        ck.sendMessage("/msg " + target + " " + PRIVATE_MESSAGE_PREFIX
                + messageId + "|" + encodeToken(message));
        updateOutgoingStatus(messageId, "未读");
    }

    private void showOutgoingTextMessage(final ChatMessage chatMessage) {
        Runnable show = new Runnable() {
            public void run() {
                if(historyWindow != null) {
                    historyWindow.addMessage(chatMessage);
                }
            }
        };
        if(SwingUtilities.isEventDispatchThread()) show.run();
        else SwingUtilities.invokeLater(show);
    }

    private void sendBroadcastMessage(String message) {
        rememberLocalBroadcastEcho(message);
        ck.sendMessage(message);
        ChatMessage chatMessage = withConversation(new ChatMessage(MessageKind.OUTGOING,
                ownNick(), message, LocalDateTime.now().format(displayTime)), BROADCAST_CHAT);
        if(historyWindow != null) historyWindow.addMessage(chatMessage);
        appendLog("[broadcast] " + message);
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
        historyWindow.addMessage(withConversation(new ChatMessage(MessageKind.OUTGOING,
                ownNick() + " / " + groupName, message, LocalDateTime.now().format(displayTime)), groupLabel(groupName)));
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
            showOutgoingFileMessage(target, file.getName(), data);
            return true;
        } catch(Exception e) {
            addMsg("<font color=\"#ff0000\">文件发送失败：" + escapeHtml(e.getMessage()) + "</font>");
            return false;
        }
    }

    private void showOutgoingFileMessage(final String target, final String fileName, final byte[] data) {
        final String sender = ownNick() + " → " + target;
        final String time = LocalDateTime.now().format(displayTime);
        Runnable show = new Runnable() {
            public void run() {
                if(historyWindow != null) {
                    ChatMessage message = ChatMessage.file(MessageKind.OUTGOING, sender,
                            fileName, data.length, data, time);
                    message.conversationKey = target;
                    historyWindow.addMessage(message);
                }
            }
        };
        if(SwingUtilities.isEventDispatchThread()) show.run();
        else SwingUtilities.invokeLater(show);
    }

    private void rebuildConversationList(String preferredSelection) {
        if(userModel == null) return;
        String ownNick = txtNick == null ? "" : txtNick.getText().trim();
        String filter = conversationFilterText();
        userModel.clear();
        final java.util.List<String> candidates = new ArrayList<String>();
        addConversationCandidate(candidates, BROADCAST_CHAT, filter, ownNick);

        Iterator<String> groupIt = chatGroups.keySet().iterator();
        while(groupIt.hasNext()) {
            String label = groupLabel(groupIt.next());
            addConversationCandidate(candidates, label, filter, ownNick);
        }

        java.util.List<String> onlineUsers = new ArrayList<String>(visibleUsers);
        Collections.sort(onlineUsers, String.CASE_INSENSITIVE_ORDER);
        for(int i=0;i<onlineUsers.size();i++) {
            String user = onlineUsers.get(i);
            addConversationCandidate(candidates, user, filter, ownNick);
        }

        java.util.List<String> savedFriends = new ArrayList<String>(friends);
        Collections.sort(savedFriends, String.CASE_INSENSITIVE_ORDER);
        for(int i=0;i<savedFriends.size();i++) {
            String friend = savedFriends.get(i);
            addConversationCandidate(candidates, friend, filter, ownNick);
        }

        Collections.sort(candidates, new Comparator<String>() {
            public int compare(String a, String b) {
                return compareConversationOrder(a, b);
            }
        });
        for(int i=0;i<candidates.size();i++) {
            userModel.addElement(candidates.get(i));
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

    private void addConversationCandidate(java.util.List<String> candidates,
            String value, String filter, String ownNick) {
        if(value == null || value.length() == 0) return;
        if(!isBroadcastConversation(value) && !isGroupConversation(value)
                && value.equalsIgnoreCase(ownNick)) return;
        if(!matchesConversation(value, filter)) return;
        if(candidates.contains(value)) return;
        candidates.add(value);
    }

    private int compareConversationOrder(String a, String b) {
        long aSeq = conversationSequenceOf(a);
        long bSeq = conversationSequenceOf(b);
        if(aSeq != bSeq) return aSeq > bSeq ? -1 : 1;
        int aCategory = conversationCategory(a);
        int bCategory = conversationCategory(b);
        if(aCategory != bCategory) return aCategory - bCategory;
        return displayConversationNameForSort(a).compareToIgnoreCase(displayConversationNameForSort(b));
    }

    private long conversationSequenceOf(String key) {
        ConversationMeta meta = conversationMeta.get(key);
        return meta == null ? 0 : meta.sequence;
    }

    private int conversationCategory(String value) {
        if(isBroadcastConversation(value)) return 0;
        if(isGroupConversation(value)) return 1;
        if(visibleUsers.contains(value)) return 2;
        if(friends.contains(value)) return 3;
        return 4;
    }

    private String displayConversationNameForSort(String value) {
        if(isGroupConversation(value)) return groupNameFromLabel(value);
        return value == null ? "" : value;
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
        flushPendingPrivateMessages(nextUsers);
        flushPendingReadReceipts(nextUsers);
        rebuildConversationList(previousSelection);
        updateVideoButtonState();
    }

    private void flushPendingReadReceipts(Set<String> onlineUsers) {
        Iterator<String> it = new ArrayList<String>(pendingReadReceipts.keySet()).iterator();
        while(it.hasNext()) {
            String user = it.next();
            if(onlineUsers.contains(user)) sendPendingReadReceipts(user);
        }
    }

    public void receiveFile(String sender, String filename, String base64) {
        try {
            byte[] data = Base64.getDecoder().decode(base64);
            appendLog("[file from " + sender + "] " + filename + " (" + data.length + " bytes)");
            ChatMessage message = ChatMessage.file(MessageKind.INCOMING, sender,
                    filename, data.length, data, LocalDateTime.now().format(displayTime));
            message.conversationKey = sender;
            historyWindow.addMessage(message);
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
        if(e.getSource()==buttonImage) chooseAndSendImage();
        if(e.getSource()==buttonVoice) chooseAndSendVoice();
        if(e.getSource()==buttonRecord) recordAndSendVoice();
        if(e.getSource()==buttonFile) chooseAndSendFile();
        if(e.getSource()==buttonVideo) startVideoCall();
        if(e.getSource()==buttonProfile) showProfileDialog();
        if(e.getSource()==buttonMoments) showMomentsDialog();
        if(e.getSource()==buttonLog) showLogPath();
        if(e.getSource()==buttonHeaderMore) showConversationMoreMenu();
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
            title.setForeground(TEXT);
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
            name.setForeground(TEXT);
            JLabel signature = new JLabel(currentProfile.getProperty("signature", "这个人还没有写个性签名。"));
            signature.setFont(UI_FONT_SMALL);
            signature.setForeground(MUTED);
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

    class VoiceRecorderDialog extends JDialog {
        private final int maxRecordSeconds = 60;
        private String target;
        private JLabel statusLabel;
        private JLabel timerLabel;
        private JButton startButton;
        private JButton stopButton;
        private JButton sendButton;
        private TargetDataLine line;
        private Thread recordThread;
        private File voiceFile;
        private volatile boolean recording = false;
        private volatile boolean saving = false;
        private boolean sendRequested = false;
        private boolean closeWhenSaved = false;
        private boolean discardWhenSaved = false;
        private long startedAt = 0;
        private javax.swing.Timer elapsedTimer;

        VoiceRecorderDialog(JFrame owner, String target) {
            super(owner, "录制语音", true);
            this.target = target;
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            setResizable(false);
            setSize(420, 260);
            setLocationRelativeTo(owner);
            setContentPane(createRecorderContent());
            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    cancelAndClose();
                }
            });
        }

        File getVoiceFileToSend() {
            return sendRequested ? voiceFile : null;
        }

        private JPanel createRecorderContent() {
            JPanel root = new JPanel(new BorderLayout(0, SPACE_LG));
            root.setBackground(SURFACE);
            root.setBorder(pad(SPACE_XL));

            JPanel header = new JPanel();
            header.setOpaque(false);
            header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
            JLabel title = new JLabel("语音消息");
            title.setFont(PAGE_TITLE_FONT);
            title.setForeground(TEXT);
            JLabel subtitle = new JLabel("发送给 " + target + "，最长 " + maxRecordSeconds + " 秒");
            subtitle.setFont(UI_FONT_SMALL);
            subtitle.setForeground(MUTED);
            header.add(title);
            header.add(Box.createVerticalStrut(SPACE_XS));
            header.add(subtitle);

            JPanel center = new JPanel(new GridBagLayout());
            center.setOpaque(false);
            JPanel meter = new BubblePanel(PRIMARY_SOFT, BORDER_LIGHT, RADIUS_LG);
            meter.setLayout(new BoxLayout(meter, BoxLayout.Y_AXIS));
            meter.setBorder(pad(SPACE_LG, SPACE_XL, SPACE_LG, SPACE_XL));
            timerLabel = new JLabel("00:00");
            timerLabel.setFont(new Font("Dialog", Font.BOLD, 32));
            timerLabel.setForeground(PRIMARY_DARK);
            timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            statusLabel = new JLabel("点击开始录音");
            statusLabel.setFont(UI_FONT_SMALL);
            statusLabel.setForeground(MUTED);
            statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            meter.add(timerLabel);
            meter.add(Box.createVerticalStrut(SPACE_SM));
            meter.add(statusLabel);
            center.add(meter);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, SPACE_SM, 0));
            actions.setOpaque(false);
            startButton = createButton("开始录音", true);
            stopButton = createButton("停止", false);
            sendButton = createButton("发送", true);
            JButton cancelButton = createButton("取消", false);
            startButton.setPreferredSize(new Dimension(96, BUTTON_HEIGHT));
            stopButton.setPreferredSize(new Dimension(72, BUTTON_HEIGHT));
            sendButton.setPreferredSize(new Dimension(72, BUTTON_HEIGHT));
            cancelButton.setPreferredSize(new Dimension(72, BUTTON_HEIGHT));
            stopButton.setEnabled(false);
            sendButton.setEnabled(false);
            startButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    startRecording();
                }
            });
            stopButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    stopRecording();
                }
            });
            sendButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(saving || voiceFile == null || !voiceFile.exists()) return;
                    sendRequested = true;
                    dispose();
                }
            });
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancelAndClose();
                }
            });
            actions.add(startButton);
            actions.add(stopButton);
            actions.add(sendButton);
            actions.add(cancelButton);

            root.add(header, BorderLayout.NORTH);
            root.add(center, BorderLayout.CENTER);
            root.add(actions, BorderLayout.SOUTH);
            return root;
        }

        private void startRecording() {
            if(saving) return;
            try {
                cleanupVoiceFile();
                closeWhenSaved = false;
                discardWhenSaved = false;
                AudioFormat format = voiceFormat();
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                if(!AudioSystem.isLineSupported(info)) {
                    setError("当前系统不支持麦克风录音格式。");
                    return;
                }
                line = (TargetDataLine)AudioSystem.getLine(info);
                line.open(format);
                line.start();
                voiceFile = createVoiceMessageFile();
                final File outputFile = voiceFile;
                final AudioInputStream stream = new AudioInputStream(line);
                recording = true;
                saving = false;
                sendRequested = false;
                recordThread = new Thread(new Runnable() {
                    public void run() {
                        try {
                            AudioSystem.write(stream, AudioFileFormat.Type.WAVE, outputFile);
                        } catch(IOException ioe) {
                            if(recording || saving) setErrorLater("录音保存失败：" + ioe.getMessage());
                        } finally {
                            try {
                                stream.close();
                            } catch(IOException ioe) {
                            }
                        }
                    }
                }, "voice-recorder");
                recordThread.setDaemon(true);
                recordThread.start();
                startedAt = System.currentTimeMillis();
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                sendButton.setEnabled(false);
                statusLabel.setText("正在录音...");
                statusLabel.setForeground(PRIMARY_DARK);
                startTimer();
            } catch(Exception e) {
                setError("无法开始录音：" + e.getMessage());
                recording = false;
                saving = false;
                if(elapsedTimer != null) elapsedTimer.stop();
                try {
                    if(line != null) {
                        line.stop();
                        line.close();
                    }
                } catch(Exception closeError) {
                }
                line = null;
            }
        }

        private void stopRecording() {
            if(saving) return;
            if(!recording && line == null) return;
            recording = false;
            saving = true;
            if(elapsedTimer != null) elapsedTimer.stop();
            try {
                if(line != null) {
                    line.stop();
                    line.close();
                }
            } catch(Exception e) {}
            line = null;
            stopButton.setEnabled(false);
            startButton.setEnabled(false);
            sendButton.setEnabled(false);
            statusLabel.setText("正在保存录音...");
            waitForRecorderThread();
        }

        private void waitForRecorderThread() {
            final Thread thread = recordThread;
            Thread joinThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        if(thread != null) thread.join();
                    } catch(InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            saving = false;
                            recordThread = null;
                            if(discardWhenSaved) cleanupVoiceFile();
                            if(closeWhenSaved) {
                                closeWhenSaved = false;
                                discardWhenSaved = false;
                                dispose();
                                return;
                            }
                            closeWhenSaved = false;
                            discardWhenSaved = false;
                            startButton.setEnabled(true);
                            if(voiceFile != null && voiceFile.exists() && voiceFile.length() > 44) {
                                sendButton.setEnabled(true);
                                statusLabel.setText("录音完成：" + displayFileSize(voiceFile.length()));
                                statusLabel.setForeground(SUCCESS);
                            } else {
                                sendButton.setEnabled(false);
                                statusLabel.setText("录音太短，请重新录制。");
                                statusLabel.setForeground(DANGER);
                                cleanupVoiceFile();
                            }
                        }
                    });
                }
            }, "voice-recorder-join");
            joinThread.setDaemon(true);
            joinThread.start();
        }

        private void startTimer() {
            if(elapsedTimer != null) elapsedTimer.stop();
            elapsedTimer = new javax.swing.Timer(500, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    long elapsed = (System.currentTimeMillis() - startedAt) / 1000;
                    timerLabel.setText(formatElapsed(elapsed));
                    if(elapsed >= maxRecordSeconds) {
                        statusLabel.setText("已达到最长录音时间。");
                        stopRecording();
                    }
                }
            });
            elapsedTimer.start();
        }

        private String formatElapsed(long seconds) {
            return String.format(Locale.US, "%02d:%02d", seconds / 60, seconds % 60);
        }

        private void cancelAndClose() {
            sendRequested = false;
            closeWhenSaved = true;
            discardWhenSaved = true;
            if(recording || line != null) {
                startButton.setEnabled(false);
                stopButton.setEnabled(false);
                sendButton.setEnabled(false);
                statusLabel.setText("正在取消录音...");
                stopRecording();
                return;
            }
            if(saving) {
                startButton.setEnabled(false);
                stopButton.setEnabled(false);
                sendButton.setEnabled(false);
                statusLabel.setText("正在取消录音...");
                return;
            }
            cleanupVoiceFile();
            closeWhenSaved = false;
            discardWhenSaved = false;
            dispose();
        }

        private void cleanupVoiceFile() {
            if(voiceFile != null && !sendRequested && voiceFile.exists()) {
                try {
                    voiceFile.delete();
                } catch(Exception e) {}
            }
            voiceFile = null;
        }

        private void setError(String text) {
            statusLabel.setText(text);
            statusLabel.setForeground(DANGER);
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            sendButton.setEnabled(false);
            saving = false;
        }

        private void setErrorLater(final String text) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setError(text);
                }
            });
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
            portField = createTextField(port == null || port.length() == 0 ? ChatClient.portText : port, 6);
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

    class VideoCallWindow extends JDialog {
        private String peer;
        private boolean outgoing;
        private boolean connected = false;
        private boolean finished = false;
        private boolean cameraOn = true;
        private boolean micOn = true;
        private long connectedAt = 0L;
        private JLabel statusLabel;
        private JLabel timerLabel;
        private VideoSurface remoteSurface;
        private VideoSurface localSurface;
        private JButton cameraButton;
        private JButton micButton;
        private JButton hangupButton;
        private javax.swing.Timer frameTimer;
        private javax.swing.Timer callTimer;

        VideoCallWindow(Frame owner, String peer, boolean outgoing) {
            super(owner, "视频通话 - " + peer, false);
            this.peer = peer;
            this.outgoing = outgoing;
            setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            setMinimumSize(new Dimension(520, 620));
            setSize(520, 620);
            setContentPane(createVideoContent());
            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    hangupLocal();
                }
            });
            startFrameTimer();
        }

        private JPanel createVideoContent() {
            JPanel root = new JPanel(new BorderLayout());
            root.setBackground(new Color(20, 20, 20));

            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            header.setBorder(pad(SPACE_MD, SPACE_LG, SPACE_SM, SPACE_LG));
            JLabel title = new JLabel(peer);
            title.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 20));
            title.setForeground(Color.WHITE);
            statusLabel = new JLabel(outgoing ? "正在呼叫..." : "正在接入...");
            statusLabel.setFont(UI_FONT_SMALL);
            statusLabel.setForeground(new Color(210, 210, 210));
            JPanel titleBlock = new JPanel();
            titleBlock.setOpaque(false);
            titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
            titleBlock.add(title);
            titleBlock.add(Box.createVerticalStrut(SPACE_XS));
            titleBlock.add(statusLabel);
            timerLabel = new JLabel("00:00");
            timerLabel.setFont(new Font("Dialog", Font.BOLD, 18));
            timerLabel.setForeground(new Color(210, 210, 210));
            header.add(titleBlock, BorderLayout.WEST);
            header.add(timerLabel, BorderLayout.EAST);

            JLayeredPane stage = new JLayeredPane() {
                public void doLayout() {
                    if(remoteSurface != null) remoteSurface.setBounds(0, 0, getWidth(), getHeight());
                    if(localSurface != null) {
                        int width = Math.min(168, Math.max(126, getWidth() / 3));
                        int height = Math.max(96, width * 3 / 4);
                        localSurface.setBounds(getWidth() - width - 16, 16, width, height);
                    }
                }
            };
            stage.setBackground(new Color(26, 26, 26));
            stage.setOpaque(true);
            remoteSurface = new VideoSurface(peer, false);
            localSurface = new VideoSurface(ownNick(), true);
            localSurface.setBorder(new RoundedBorder(new Color(255, 255, 255, 120), RADIUS_LG));
            stage.add(remoteSurface, Integer.valueOf(0));
            stage.add(localSurface, Integer.valueOf(1));

            JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, SPACE_MD, SPACE_MD));
            controls.setBackground(new Color(20, 20, 20));
            cameraButton = createCallButton("关摄像头", new Color(62, 62, 62));
            micButton = createCallButton("静音", new Color(62, 62, 62));
            hangupButton = createCallButton("挂断", new Color(224, 68, 68));
            cameraButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    toggleCamera();
                }
            });
            micButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    toggleMic();
                }
            });
            hangupButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    hangupLocal();
                }
            });
            controls.add(cameraButton);
            controls.add(micButton);
            controls.add(hangupButton);

            root.add(header, BorderLayout.NORTH);
            root.add(stage, BorderLayout.CENTER);
            root.add(controls, BorderLayout.SOUTH);
            return root;
        }

        private JButton createCallButton(final String text, final Color fill) {
            JButton button = new JButton(text) {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(isEnabled() ? fill : new Color(80, 80, 80));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS_XL, RADIUS_XL);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            button.setFont(UI_FONT_BOLD);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setOpaque(false);
            button.setContentAreaFilled(false);
            button.setBorder(pad(9, 18, 9, 18));
            button.setPreferredSize(new Dimension(104, 42));
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return button;
        }

        void showWaiting(String text) {
            connected = false;
            if(statusLabel != null) statusLabel.setText(text);
            if(remoteSurface != null) remoteSurface.setWaiting(true);
        }

        void showConnected() {
            if(finished) return;
            connected = true;
            connectedAt = System.currentTimeMillis();
            if(statusLabel != null) statusLabel.setText("正在通话");
            if(remoteSurface != null) remoteSurface.setWaiting(false);
            startCallTimer();
        }

        void finishFromRemote(String text) {
            if(finished) return;
            finished = true;
            connected = false;
            if(statusLabel != null) statusLabel.setText(text);
            disableControls();
            stopCallTimer();
            javax.swing.Timer timer = new javax.swing.Timer(1200, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    disposeWindow();
                }
            });
            timer.setRepeats(false);
            timer.start();
        }

        private void hangupLocal() {
            if(finished) return;
            finished = true;
            sendVideoSignal(peer, VIDEO_HANGUP);
            showVideoSystemMessage("已结束与 " + peer + " 的视频通话。");
            disposeWindow();
        }

        private void toggleCamera() {
            cameraOn = !cameraOn;
            localSurface.setCameraOn(cameraOn);
            cameraButton.setText(cameraOn ? "关摄像头" : "开摄像头");
            if(statusLabel != null && connected) statusLabel.setText(cameraOn ? "正在通话" : "摄像头已关闭");
        }

        private void toggleMic() {
            micOn = !micOn;
            micButton.setText(micOn ? "静音" : "取消静音");
            if(statusLabel != null && connected) statusLabel.setText(micOn ? "正在通话" : "麦克风已静音");
        }

        private void startFrameTimer() {
            frameTimer = new javax.swing.Timer(80, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    remoteSurface.tick();
                    localSurface.tick();
                }
            });
            frameTimer.start();
        }

        private void startCallTimer() {
            stopCallTimer();
            callTimer = new javax.swing.Timer(1000, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updateCallTime();
                }
            });
            callTimer.start();
            updateCallTime();
        }

        private void updateCallTime() {
            if(timerLabel == null || connectedAt <= 0) return;
            long seconds = Math.max(0, (System.currentTimeMillis() - connectedAt) / 1000);
            timerLabel.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));
        }

        private void stopCallTimer() {
            if(callTimer != null) {
                callTimer.stop();
                callTimer = null;
            }
        }

        private void disableControls() {
            if(cameraButton != null) cameraButton.setEnabled(false);
            if(micButton != null) micButton.setEnabled(false);
            if(hangupButton != null) hangupButton.setEnabled(false);
        }

        private void disposeWindow() {
            if(frameTimer != null) frameTimer.stop();
            stopCallTimer();
            removeVideoWindow(peer, this);
            dispose();
        }
    }

    class VideoSurface extends JPanel {
        private String name;
        private boolean local;
        private boolean cameraOn = true;
        private boolean waiting = false;
        private int frame = 0;

        VideoSurface(String name, boolean local) {
            this.name = name == null ? "" : name;
            this.local = local;
            setOpaque(true);
            setBackground(Color.BLACK);
        }

        void setCameraOn(boolean cameraOn) {
            this.cameraOn = cameraOn;
            repaint();
        }

        void setWaiting(boolean waiting) {
            this.waiting = waiting;
            repaint();
        }

        void tick() {
            frame++;
            repaint();
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if(cameraOn) paintVideo(g2);
            else paintCameraOff(g2);
            paintOverlay(g2);
            g2.dispose();
        }

        private void paintVideo(Graphics2D g2) {
            Color base = videoColor(name);
            Color dark = base.darker().darker();
            g2.setPaint(new GradientPaint(0, 0, dark, getWidth(), getHeight(), base));
            g2.fillRect(0, 0, getWidth(), getHeight());

            for(int i=0;i<8;i++) {
                int alpha = local ? 38 : 54;
                g2.setColor(new Color(255, 255, 255, alpha));
                int size = local ? 34 + i * 12 : 70 + i * 28;
                int x = (int)((Math.sin((frame + i * 13) * 0.06) + 1) * (getWidth() - size) / 2.0);
                int y = (int)((Math.cos((frame + i * 17) * 0.045) + 1) * (getHeight() - size) / 2.0);
                g2.fillOval(x, y, size, size);
            }

            int faceSize = Math.min(getWidth(), getHeight()) / (local ? 4 : 5);
            faceSize = Math.max(local ? 28 : 64, faceSize);
            int faceX = (getWidth() - faceSize) / 2;
            int faceY = (getHeight() - faceSize) / 2 - (local ? 2 : 12);
            g2.setColor(new Color(255, 255, 255, 210));
            g2.fillRoundRect(faceX, faceY, faceSize, faceSize, RADIUS_XL, RADIUS_XL);
            g2.setColor(base.darker());
            g2.setFont(new Font("Microsoft YaHei UI", Font.BOLD, Math.max(18, faceSize / 3)));
            String avatar = avatarText(name);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(avatar, faceX + (faceSize - fm.stringWidth(avatar)) / 2,
                    faceY + (faceSize + fm.getAscent() - fm.getDescent()) / 2);

            if(waiting) {
                g2.setColor(new Color(0, 0, 0, 90));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Microsoft YaHei UI", Font.BOLD, local ? 12 : 18));
                String text = "等待接听...";
                FontMetrics textMetrics = g2.getFontMetrics();
                g2.drawString(text, (getWidth() - textMetrics.stringWidth(text)) / 2,
                        getHeight() / 2 + faceSize / 2 + 36);
            }
        }

        private void paintCameraOff(Graphics2D g2) {
            g2.setColor(new Color(31, 31, 31));
            g2.fillRect(0, 0, getWidth(), getHeight());
            int size = Math.min(getWidth(), getHeight()) / (local ? 3 : 4);
            size = Math.max(local ? 36 : 86, size);
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2 - 12;
            g2.setColor(new Color(72, 72, 72));
            g2.fillRoundRect(x, y, size, size, RADIUS_XL, RADIUS_XL);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Microsoft YaHei UI", Font.BOLD, Math.max(18, size / 3)));
            String avatar = avatarText(name);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(avatar, x + (size - fm.stringWidth(avatar)) / 2,
                    y + (size + fm.getAscent() - fm.getDescent()) / 2);
            g2.setFont(UI_FONT_SMALL);
            String text = local ? "摄像头已关" : "对方摄像头暂不可用";
            FontMetrics textMetrics = g2.getFontMetrics();
            g2.drawString(text, (getWidth() - textMetrics.stringWidth(text)) / 2,
                    y + size + 28);
        }

        private void paintOverlay(Graphics2D g2) {
            g2.setColor(new Color(0, 0, 0, local ? 95 : 75));
            int labelWidth = Math.min(getWidth() - 20, Math.max(72, g2.getFontMetrics(UI_FONT_SMALL).stringWidth(name) + 24));
            g2.fillRoundRect(10, getHeight() - 34, labelWidth, 24, RADIUS_LG, RADIUS_LG);
            g2.setColor(Color.WHITE);
            g2.setFont(UI_FONT_SMALL);
            g2.drawString(name, 22, getHeight() - 17);

            if(!local && cameraOn && !waiting) {
                int barsX = getWidth() - 48;
                int barsY = getHeight() - 28;
                for(int i=0;i<4;i++) {
                    int barHeight = 5 + (int)((Math.sin((frame + i * 7) * 0.18) + 1) * 7);
                    g2.fillRoundRect(barsX + i * 8, barsY - barHeight, 5, barHeight, 4, 4);
                }
            }
        }

        private Color videoColor(String source) {
            int hash = source == null ? 0 : source.hashCode();
            Color base = AVATAR_COLORS[(hash & 0x7fffffff) % AVATAR_COLORS.length];
            return local ? base.brighter() : base;
        }

        private String avatarText(String source) {
            String trimmed = source == null ? "" : source.trim();
            if(trimmed.length() == 0) return "?";
            return trimmed.substring(0, 1).toUpperCase();
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
                stroke = fill;
                setForeground(Color.WHITE);
            } else {
                fill = hover ? SURFACE_SOFT : SURFACE;
                stroke = hover ? BORDER : BORDER_LIGHT;
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

    class RailButton extends JButton {
        private boolean hover = false;

        RailButton(String text) {
            super(text);
            setFont(new Font("Microsoft YaHei UI", Font.BOLD, 14));
            setForeground(new Color(74, 80, 92));
            setFocusPainted(false);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorder(pad(0));
            setPreferredSize(new Dimension(40, 40));
            setMinimumSize(new Dimension(40, 40));
            setMaximumSize(new Dimension(40, 40));
            setAlignmentX(Component.CENTER_ALIGNMENT);
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
            if(hover) {
                g2.setColor(new Color(255, 255, 255, 185));
                g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, RADIUS_LG, RADIUS_LG);
                setForeground(PRIMARY_DARK);
            } else {
                setForeground(new Color(74, 80, 92));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        protected void configureScrollBarColors() {
            thumbColor = new Color(198, 203, 213);
            trackColor = new Color(0, 0, 0, 0);
        }

        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }

        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        }

        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if(!scrollbar.isEnabled() || thumbBounds.width <= 0 || thumbBounds.height <= 0) return;
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isThumbRollover() ? new Color(158, 165, 177) : thumbColor);
            g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2,
                    Math.max(4, thumbBounds.width - 4),
                    Math.max(4, thumbBounds.height - 4),
                    8, 8);
            g2.dispose();
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
            g2.setColor(SURFACE);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(PRIMARY);
            g2.fillRect(0, 0, 5, getHeight());
            g2.setColor(BORDER_LIGHT);
            g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    class BadgeLabel extends JComponent {
        private int count = 0;

        BadgeLabel() {
            setFont(new Font("Microsoft YaHei UI", Font.BOLD, 10));
            setMinimumSize(new Dimension(0, 18));
        }

        void setCount(int count) {
            this.count = Math.max(0, count);
            revalidate();
            repaint();
        }

        public Dimension getPreferredSize() {
            if(count <= 0) return new Dimension(0, 18);
            String text = count > 99 ? "99+" : String.valueOf(count);
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(Math.max(18, fm.stringWidth(text) + 10), 18);
        }

        protected void paintComponent(Graphics g) {
            if(count <= 0) return;
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            String text = count > 99 ? "99+" : String.valueOf(count);
            int width = getWidth();
            int height = getHeight();
            g2.setColor(new Color(250, 81, 81));
            g2.fillRoundRect(0, 1, width - 1, height - 3, height - 3, height - 3);
            g2.setColor(Color.WHITE);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int x = (width - fm.stringWidth(text)) / 2;
            int y = (height + fm.getAscent() - fm.getDescent()) / 2 - 1;
            g2.drawString(text, x, y);
            g2.dispose();
        }
    }

    class ConversationRenderer extends JPanel implements ListCellRenderer<String> {
        private AvatarView avatar = new AvatarView("?", false);
        private JLabel nameLabel = new JLabel();
        private JLabel previewLabel = new JLabel();
        private JLabel timeLabel = new JLabel();
        private JLabel stateDot = new JLabel("●");
        private BadgeLabel unreadBadge = new BadgeLabel();
        private JPanel textPanel = new JPanel();
        private JPanel titleRow = new JPanel(new BorderLayout(SPACE_SM, 0));
        private JPanel previewRow = new JPanel(new BorderLayout(SPACE_SM, 0));
        private boolean selectedRow = false;
        private boolean hoverRow = false;

        ConversationRenderer() {
            setLayout(new BorderLayout(SPACE_MD, 0));
            setBorder(pad(7, 10, 7, 10));
            textPanel.setOpaque(false);
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            nameLabel.setFont(UI_FONT_BOLD);
            previewLabel.setFont(CHAT_TEXT_SMALL_FONT);
            timeLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
            stateDot.setFont(new Font("Dialog", Font.BOLD, 9));
            stateDot.setBorder(pad(0, 0, 0, 5));

            JPanel namePack = new JPanel();
            namePack.setOpaque(false);
            namePack.setLayout(new BoxLayout(namePack, BoxLayout.X_AXIS));
            namePack.add(stateDot);
            namePack.add(nameLabel);

            titleRow.setOpaque(false);
            titleRow.add(namePack, BorderLayout.CENTER);
            titleRow.add(timeLabel, BorderLayout.EAST);
            previewRow.setOpaque(false);
            previewRow.add(previewLabel, BorderLayout.CENTER);
            previewRow.add(unreadBadge, BorderLayout.EAST);

            textPanel.add(titleRow);
            textPanel.add(Box.createVerticalStrut(5));
            textPanel.add(previewRow);
            add(avatar, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);
        }

        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
                boolean isSelected, boolean cellHasFocus) {
            String name = value == null ? "" : value.toString();
            avatar.setAvatar(name, false);
            nameLabel.setText(displayConversationName(name));
            ConversationMeta meta = conversationMeta.get(name);
            previewLabel.setText(meta != null && meta.preview.length() > 0
                    ? meta.preview
                    : fallbackConversationDetail(name));
            timeLabel.setText(meta == null ? "" : meta.time);
            unreadBadge.setCount(meta == null ? 0 : meta.unread);
            stateDot.setForeground(conversationStateColor(name));
            selectedRow = isSelected;
            hoverRow = index == hoveredConversationIndex;
            if(isSelected) {
                nameLabel.setForeground(TEXT);
                previewLabel.setForeground(new Color(72, 72, 72));
                timeLabel.setForeground(MUTED);
            } else if(index == hoveredConversationIndex) {
                nameLabel.setForeground(TEXT);
                previewLabel.setForeground(MUTED);
                timeLabel.setForeground(SOFT_MUTED);
            } else {
                nameLabel.setForeground(TEXT);
                previewLabel.setForeground(MUTED);
                timeLabel.setForeground(SOFT_MUTED);
            }
            setOpaque(false);
            return this;
        }

        private String displayConversationName(String name) {
            if(BROADCAST_CHAT.equals(name)) return BROADCAST_CHAT;
            if(isGroupConversation(name)) return groupNameFromLabel(name);
            return name;
        }

        private String fallbackConversationDetail(String name) {
            if(BROADCAST_CHAT.equals(name)) {
                return "全服务器广播 · " + visibleUsers.size() + " 人在线";
            }
            if(isGroupConversation(name)) {
                String groupName = groupNameFromLabel(name);
                Set<String> members = chatGroups.get(groupName);
                int memberCount = members == null ? 1 : members.size();
                return "群聊 · " + memberCount + " 人 · " + countOnlineMembers(members) + " 人在线";
            }
            if(friends.contains(name)) return visibleUsers.contains(name) ? "好友在线" : "好友离线";
            if(sentFriendRequests.contains(name)) return "等待验证";
            return visibleUsers.contains(name) ? "在线" : "离线";
        }

        private Color conversationStateColor(String name) {
            if(BROADCAST_CHAT.equals(name)) return PRIMARY;
            if(isGroupConversation(name)) return PRIMARY_DARK;
            if(visibleUsers.contains(name)) return SUCCESS;
            return SOFT_MUTED;
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if(selectedRow || hoverRow) {
                g2.setColor(selectedRow ? SIDEBAR_SELECTED : SURFACE_SOFT);
                g2.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, RADIUS_LG, RADIUS_LG);
                if(selectedRow) {
                    g2.setColor(PRIMARY);
                    g2.fillRoundRect(3, 13, 3, getHeight() - 26, 3, 3);
                }
            } else {
                g2.setColor(BORDER_LIGHT);
                g2.drawLine(58, getHeight() - 1, getWidth() - 12, getHeight() - 1);
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
            updateConversationPreview(message);
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
            label.setFont(CHAT_TEXT_SMALL_FONT);
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

            JLabel name = new JLabel(senderLineText(message));
            name.setFont(CHAT_TEXT_SMALL_FONT);
            name.setForeground(MUTED);
            name.setAlignmentX(outgoing ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
            registerStatusLabel(message, name);

            if(message.fileMessage) {
                return createFileBubbleBlock(message, outgoing, name);
            }

            JLabel text = new JLabel("<html><div style=\"width:" + messageWidth(message.body)
                    + "px;\">" + htmlText(message.body) + "</div></html>");
            text.setFont(CHAT_TEXT_FONT);
            text.setForeground(TEXT);

            JPanel bubble = new BubblePanel(outgoing ? OUTGOING_BUBBLE : INCOMING_BUBBLE,
                    outgoing ? new Color(125, 211, 88) : BORDER_LIGHT,
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
            int maxBubble = availableBubbleWidth(160, 320);
            return Math.max(42, Math.min(maxBubble, maxLine));
        }

        private int availableBubbleWidth(int min, int max) {
            int maxBubble = max;
            if(sc != null && sc.getViewport() != null) {
                int available = sc.getViewport().getWidth() - 110;
                if(available > 0) maxBubble = Math.max(min, Math.min(max, available));
            }
            return maxBubble;
        }

        private Component createFileBubbleBlock(final ChatMessage message, boolean outgoing, JLabel name) {
            if(isImageFile(message.fileName) && message.fileData != null) {
                ImageIcon imageIcon = createThumbnailIcon(message.fileData, availableBubbleWidth(160, 240), 170);
                if(imageIcon != null) {
                    return createImageBubbleBlock(message, outgoing, name, imageIcon);
                }
            }

            JPanel block = new JPanel();
            block.setOpaque(false);
            block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));

            final BubblePanel card = new BubblePanel(outgoing ? OUTGOING_BUBBLE : INCOMING_BUBBLE,
                    outgoing ? new Color(125, 211, 88) : BORDER_LIGHT,
                    RADIUS_LG);
            card.setLayout(new BorderLayout(SPACE_MD, 0));
            card.setBorder(pad(11, 12, 11, 12));
            int cardWidth = availableBubbleWidth(200, 292);
            card.setMaximumSize(new Dimension(cardWidth, 82));
            card.setPreferredSize(new Dimension(cardWidth, 82));

            JLabel icon = new JLabel(mediaBadge(message.fileName));
            icon.setFont(new Font("Dialog", Font.BOLD, 11));
            icon.setForeground(outgoing ? new Color(44, 112, 44) : MUTED);
            icon.setHorizontalAlignment(SwingConstants.CENTER);
            icon.setPreferredSize(new Dimension(50, 46));
            icon.setBorder(new RoundedBorder(outgoing ? new Color(125, 211, 88) : BORDER, RADIUS_MD));

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
                    outgoing ? new Color(125, 211, 88) : BORDER_LIGHT,
                    RADIUS_LG);
            card.setLayout(new BorderLayout());
            card.setBorder(pad(SPACE_SM));

            JLabel imageLabel = new JLabel(imageIcon);
            imageLabel.setBorder(new RoundedBorder(outgoing ? new Color(125, 211, 88) : BORDER, RADIUS_MD));
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
            return hasExtension(name, IMAGE_EXTENSIONS);
        }

        private boolean isAudioFile(String name) {
            return hasExtension(name, AUDIO_EXTENSIONS);
        }

        private boolean isVideoFile(String name) {
            return hasExtension(name, VIDEO_EXTENSIONS);
        }

        private String mediaBadge(String name) {
            if(isImageFile(name)) return "图片";
            if(isAudioFile(name)) return "语音";
            if(isVideoFile(name)) return "视频";
            return "文件";
        }

        private String fileStatusText(ChatMessage message, boolean outgoing) {
            String type = "文件";
            if(isAudioFile(message.fileName)) type = "语音";
            if(isVideoFile(message.fileName)) type = "视频";
            if(isImageFile(message.fileName)) type = "图片";
            if(outgoing) return type + " · " + displayFileSize(message.fileSize) + " · 已发送";
            if(message.savedFile != null) return type + " · " + displayFileSize(message.fileSize) + " · 已保存";
            return type + " · " + displayFileSize(message.fileSize) + " · 点击下载";
        }

        private String clipFileName(String name) {
            if(name == null) return "";
            if(name.length() <= 18) return name;
            return name.substring(0, 8) + "..." + name.substring(name.length() - 7);
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
            Color fill = outgoing ? PRIMARY : avatarColor(name);
            int size = Math.min(getWidth(), getHeight()) - 2;
            if(size <= 0) {
                g2.dispose();
                return;
            }
            int x0 = (getWidth() - size) / 2;
            int y0 = (getHeight() - size) / 2;
            int radius = Math.max(RADIUS_MD, size / 4);
            g2.setColor(fill);
            g2.fillRoundRect(x0, y0, size, size, radius, radius);
            g2.setColor(new Color(255, 255, 255, 70));
            g2.drawRoundRect(x0 + 1, y0 + 1, size - 2, size - 2, radius, radius);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Microsoft YaHei UI", Font.BOLD, Math.max(12, size / 3)));
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

        private Color avatarColor(String source) {
            if(BROADCAST_CHAT.equals(source)) return PRIMARY;
            int index = (source == null ? 0 : source.hashCode() & 0x7fffffff) % AVATAR_COLORS.length;
            return AVATAR_COLORS[index];
        }
    }
}
