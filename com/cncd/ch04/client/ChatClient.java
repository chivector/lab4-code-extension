package com.cncd.ch04.client;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import com.github.sarxos.webcam.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ChatClient extends JFrame implements KeyListener, ActionListener, FocusListener {
    public static final String appName = "CNCD Chat";
    public static final String serverText = "192.168.85.86";
    public static final String portText = "3500";
    public static final String nickText = "YourName";
    public static final String USERS_PREFIX = "__USERS__|";
    public static final String FILE_PREFIX = "__FILE__|";
    public static final String SERVER_MOMENT_ITEM_PREFIX = "__SERVER_MOMENT__|";
    private static final String FRIEND_REQUEST_PREFIX = "__FRIEND_REQ__|";
    private static final String FRIEND_ACCEPT_PREFIX = "__FRIEND_ACCEPT__|";
    private static final String FRIEND_REJECT_PREFIX = "__FRIEND_REJECT__|";
    private static final String FRIEND_DELETE_PREFIX = "__FRIEND_DELETE__|";
    private static final String GROUP_INVITE_PREFIX = "__GROUP_INVITE__|";
    private static final String GROUP_MESSAGE_PREFIX = "__GROUP_MSG__|";
    private static final String GROUP_KICK_PREFIX = "__GROUP_KICK__|";
    private static final String GROUP_LEAVE_PREFIX = "__GROUP_LEAVE__|";
    private static final String GROUP_ROLE_PREFIX = "__GROUP_ROLE__|";
    private static final String MESSAGE_RECALL_PREFIX = "__RECALL__|";
    private static final String GROUP_FILE_PREFIX = "__GROUP_FILE__";
    private static final String BROADCAST_FILE_PREFIX = "__BROADCAST_FILE__";
    private static final String PRIVATE_FILE_PREFIX = "__PRIVATE_FILE__";
    private static final String PENDING_FILE_PREFIX = "__PENDING_FILE__|";
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
    private static final String VIDEO_AUDIO = "AUDIO";
    private static final String VIDEO_FRAME = "FRAME";

    private static final long MAX_FILE_BYTES = 5L * 1024L * 1024L;
    private static final int MESSAGE_TEXT_LIMIT = 800;
    private static final int CALL_AUDIO_CHUNK_BYTES = 1024;
    private static final int CALL_AUDIO_QUEUE_LIMIT = 45;
    private static final int CALL_VIDEO_WIDTH = 320;
    private static final int CALL_VIDEO_HEIGHT = 180;
    private static final int CALL_VIDEO_INTERVAL_MS = 50;
    private static final int HEALTH_CHECK_INTERVAL_MS = 2500;
    private static final int USER_REFRESH_INTERVAL_MS = 30000;
    private static final String ACCOUNT_FILE = "accounts.properties";
    private static final String LOGIN_PREF_FILE = "login.properties";
    private static final String MOMENTS_FILE = "moments.txt";
    private static final String MOMENT_NOTIFICATIONS_FILE = "moment-notifications.txt";
    private static final String FRIEND_REQUESTS_FILE = "friend-requests.txt";
    private static final String PENDING_MESSAGES_FILE = "pending-private.txt";
    private static final String PENDING_DELIVERIES_FILE = "pending-deliveries.txt";
    private static final String MOMENT_RECORD_VERSION = "M2";
    private static final String MOMENT_VISIBILITY_PUBLIC = "public";
    private static final String MOMENT_VISIBILITY_FRIENDS = "friends";
    private static final String MOMENT_VISIBILITY_PRIVATE = "private";
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
    private static final int RAIL_WIDTH = 76;
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
    JTextField messageSearchField;
    JLabel messageSearchCountLabel;
    JButton buttonSearchPrev, buttonSearchNext, buttonSearchClear;
    JLabel statusLabel, connectionDotLabel, conversationTitleLabel, conversationSubtitleLabel;
    JLabel conversationStateLabel, composerHintLabel, composerCountLabel;
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
    private Map<String, String> friendRemarks = new HashMap<String, String>();
    private Map<String, Set<String>> chatGroups = new LinkedHashMap<String, Set<String>>();
    private Map<String, String> groupOwners = new HashMap<String, String>();
    private Map<String, Set<String>> groupAdmins = new HashMap<String, Set<String>>();
    private Map<String, ConversationMeta> conversationMeta = new HashMap<String, ConversationMeta>();
    private Map<String, java.util.List<ChatMessage>> conversationMessages =
            new LinkedHashMap<String, java.util.List<ChatMessage>>();
    private Set<String> sentFriendRequests = new HashSet<String>();
    private Set<String> incomingFriendRequests = new HashSet<String>();
    private Set<String> visibleUsers = new HashSet<String>();
    private Map<String, VideoCallWindow> videoCallWindows = new HashMap<String, VideoCallWindow>();
    private Set<String> pendingVideoCalls = new HashSet<String>();
    private long conversationSequence = 0;
    private Map<String, PendingPrivateMessage> pendingPrivateMessages = new LinkedHashMap<String, PendingPrivateMessage>();
    private Map<String, PendingDelivery> pendingDeliveries = new LinkedHashMap<String, PendingDelivery>();
    private Map<String, ChatMessage> trackedOutgoingMessages = new LinkedHashMap<String, ChatMessage>();
    private Map<String, JLabel> trackedStatusLabels = new HashMap<String, JLabel>();
    private Map<String, java.util.List<String>> pendingReadReceipts = new HashMap<String, java.util.List<String>>();
    private java.util.List<String> messageSearchMatches = new ArrayList<String>();
    private LinkedList<String> localBroadcastEchoes = new LinkedList<String>();
    private String renderedConversationKey = null;
    private String messageSearchKeyword = "";
    private int messageSearchIndex = -1;
    private VoicePlayback activeVoicePlayback;
    private MomentsDialog activeMomentsDialog;
    private java.util.List<MomentNotification> momentNotifications = new ArrayList<MomentNotification>();
    private Path logFile;
    private DateTimeFormatter displayTime = DateTimeFormatter.ofPattern("HH:mm:ss");
    private DateTimeFormatter logTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private javax.swing.Timer healthTimer;
    private boolean connectionWasHealthy = false;
    private boolean connectionLossReported = false;
    private long lastUserRefreshAt = 0;

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
        String localMessageId = UUID.randomUUID().toString();
        String deliveryStatus;
        long sentAtMillis = 0L;
        boolean recalled = false;

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

    private static class PendingDelivery {
        String id;
        String target;
        String body;
        String note;
        String time;

        PendingDelivery(String id, String target, String body, String note, String time) {
            this.id = id;
            this.target = target;
            this.body = body;
            this.note = note;
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
        String visibility = MOMENT_VISIBILITY_FRIENDS;
        String imageName = "";
        byte[] imageData;
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
        String replyTo;

        MomentComment(String time, String author, String text) {
            this(time, author, text, "");
        }

        MomentComment(String time, String author, String text, String replyTo) {
            this.time = time;
            this.author = author;
            this.text = text;
            this.replyTo = replyTo == null ? "" : replyTo;
        }
    }

    private static class MomentNotification {
        String id;
        String time;
        String actor;
        String action;
        String detail;
        boolean read;

        MomentNotification(String id, String time, String actor, String action, String detail, boolean read) {
            this.id = id;
            this.time = time;
            this.actor = actor;
            this.action = action;
            this.detail = detail;
            this.read = read;
        }
    }

    /** Creates a new instance of Class */
    public ChatClient() {
        uiInit();
        txtHost.setText("127.0.0.1");
        txtPort.setText("3500");
        startHealthMonitor();
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
        appTitle.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 17));
        appTitle.setForeground(TEXT);
        JLabel appSubTitle = new JLabel("现代桌面即时通讯");
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
        buttonConnect = createButton("连接", false);
        buttonConnect.setPreferredSize(new Dimension(78, BUTTON_HEIGHT));
        statusLabel = new JLabel("未连接");
        statusLabel.setFont(UI_FONT_SMALL);
        statusLabel.setForeground(MUTED);
        connectionDotLabel = new JLabel("●");
        connectionDotLabel.setFont(new Font("Dialog", Font.BOLD, 12));
        connectionDotLabel.setForeground(SOFT_MUTED);

        buttonConnect.addActionListener(this);
        buttonConnect.addKeyListener(this);

        JPanel statusBlock = new JPanel();
        statusBlock.setOpaque(false);
        statusBlock.setLayout(new BoxLayout(statusBlock, BoxLayout.X_AXIS));
        BubblePanel statusPill = createPillPanel(new Color(248, 250, 252), BORDER_LIGHT);
        statusPill.setLayout(new BoxLayout(statusPill, BoxLayout.X_AXIS));
        statusPill.add(connectionDotLabel);
        statusPill.add(Box.createHorizontalStrut(SPACE_XS));
        statusPill.add(statusLabel);
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

        buttonRefresh = createRailButton("刷新", "刷新在线列表");
        buttonAddFriend = createRailButton("好友", "添加选中联系人为好友");
        buttonCreateGroup = createRailButton("群聊", "创建群聊");
        buttonProfile = createRailButton("资料", "编辑个人资料");
        buttonMoments = createRailButton("动态", "朋友圈 / 动态");
        buttonLog = createRailButton("记录", "打开聊天记录");

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
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int index = onlineList.locationToIndex(e.getPoint());
                    if(index >= 0) {
                        Rectangle bounds = onlineList.getCellBounds(index, index);
                        String value = onlineList.getModel().getElementAt(index);
                        if(bounds != null && e.getX() - bounds.x <= 62 && isUserConversation(value)) {
                            showUserHomeDialog(value);
                        }
                    }
                }
            }

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
        sidebarAvatar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sidebarAvatar.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) showProfileDialog();
            }
        });
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
        conversationAvatar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        conversationAvatar.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    String target = getSelectedPrivateTarget();
                    if(target != null && target.length() > 0) showUserHomeDialog(target);
                }
            }
        });
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
        conversationStateLabel = new JLabel("广播");
        conversationStateLabel.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 12));
        conversationStateLabel.setForeground(PRIMARY_DARK);
        conversationStateLabel.setHorizontalAlignment(SwingConstants.CENTER);
        conversationStateLabel.setBorder(new CompoundBorder(
                new RoundedBorder(new Color(194, 235, 210), RADIUS_XL),
                pad(4, 10, 4, 10)));
        JPanel titleLine = new JPanel();
        titleLine.setOpaque(false);
        titleLine.setLayout(new BoxLayout(titleLine, BoxLayout.X_AXIS));
        titleLine.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleLine.add(conversationTitleLabel);
        titleLine.add(Box.createHorizontalStrut(SPACE_SM));
        titleLine.add(conversationStateLabel);
        textBlock.add(titleLine);
        textBlock.add(Box.createVerticalStrut(SPACE_XS));
        textBlock.add(conversationSubtitleLabel);
        identity.add(textBlock);
        header.add(identity, BorderLayout.CENTER);
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

        JPanel chatBody = new JPanel(new BorderLayout());
        chatBody.setBackground(CHAT_BACKGROUND);
        chatBody.add(createMessageSearchPanel(), BorderLayout.NORTH);
        chatBody.add(createHistoryPanel(), BorderLayout.CENTER);

        chatPanel.add(header, BorderLayout.NORTH);
        chatPanel.add(chatBody, BorderLayout.CENTER);
        chatPanel.add(createInputPanel(), BorderLayout.SOUTH);
        return chatPanel;
    }

    private JPanel createMessageSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(SPACE_SM, 0));
        panel.setBackground(SURFACE_SOFT);
        panel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER_LIGHT),
                pad(SPACE_SM, SPACE_MD, SPACE_SM, SPACE_MD)));

        messageSearchField = new PromptTextField("", 18, "搜索当前会话消息");
        styleTextField(messageSearchField);
        messageSearchField.setToolTipText("搜索当前会话中的消息内容、发送者或文件名");
        messageSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyMessageSearchField(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyMessageSearchField(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyMessageSearchField(); }
        });
        messageSearchField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { goToMessageSearchMatch(1); }
        });

        messageSearchCountLabel = createHintLabel("0/0");
        messageSearchCountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageSearchCountLabel.setPreferredSize(new Dimension(48, INPUT_HEIGHT));

        buttonSearchPrev = createButton("上", false);
        buttonSearchNext = createButton("下", false);
        buttonSearchClear = createButton("清", false);
        buttonSearchPrev.setPreferredSize(new Dimension(42, BUTTON_HEIGHT));
        buttonSearchNext.setPreferredSize(new Dimension(42, BUTTON_HEIGHT));
        buttonSearchClear.setPreferredSize(new Dimension(42, BUTTON_HEIGHT));
        buttonSearchPrev.setToolTipText("上一条匹配消息");
        buttonSearchNext.setToolTipText("下一条匹配消息");
        buttonSearchClear.setToolTipText("清空当前会话搜索");
        buttonSearchPrev.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { goToMessageSearchMatch(-1); }
        });
        buttonSearchNext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { goToMessageSearchMatch(1); }
        });
        buttonSearchClear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { clearMessageSearch(); }
        });

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, SPACE_XS, 0));
        controls.setOpaque(false);
        controls.add(messageSearchCountLabel);
        controls.add(buttonSearchPrev);
        controls.add(buttonSearchNext);
        controls.add(buttonSearchClear);

        panel.add(messageSearchField, BorderLayout.CENTER);
        panel.add(controls, BorderLayout.EAST);
        updateMessageSearchControls();
        return panel;
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
        composerHintLabel = createHintLabel("选择会话后开始聊天");
        actionRow.add(composerHintLabel, BorderLayout.WEST);
        composerCountLabel = createHintLabel("0/" + MESSAGE_TEXT_LIMIT);
        composerCountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        JPanel sendPack = new JPanel(new FlowLayout(FlowLayout.RIGHT, SPACE_SM, 0));
        sendPack.setOpaque(false);
        sendPack.add(composerCountLabel);
        sendPack.add(buttonSend);
        actionRow.add(sendPack, BorderLayout.EAST);

        composer.add(toolbar, BorderLayout.NORTH);
        composer.add(messageScroll, BorderLayout.CENTER);
        composer.add(actionRow, BorderLayout.SOUTH);

        southPanel.add(attachmentPanel, BorderLayout.NORTH);
        southPanel.add(composer, BorderLayout.CENTER);
        updateVideoButtonState();
        updateSendButtonState();
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

    private JPanel createStackedBlock(String label, JComponent input) {
        JPanel panel = new JPanel(new BorderLayout(0, SPACE_SM));
        panel.setOpaque(false);
        JLabel textLabel = createSectionTitle(label);
        panel.add(textLabel, BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);
        return panel;
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
        JPanel card = new BubblePanel(SURFACE, BORDER_LIGHT, RADIUS_XL);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(pad(SPACE_XL, SPACE_XL + 8, SPACE_XL, SPACE_XL + 8));
        card.setPreferredSize(new Dimension(260, 154));
        JLabel mark = new JLabel("CNCD");
        mark.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 13));
        mark.setForeground(PRIMARY_DARK);
        mark.setHorizontalAlignment(SwingConstants.CENTER);
        mark.setAlignmentX(Component.CENTER_ALIGNMENT);
        mark.setBorder(new CompoundBorder(new RoundedBorder(new Color(194, 235, 210), RADIUS_XL),
                pad(5, 12, 5, 12)));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 17));
        titleLabel.setForeground(TEXT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel detailLabel = new JLabel(detail);
        detailLabel.setFont(UI_FONT_SMALL);
        detailLabel.setForeground(MUTED);
        detailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(mark);
        card.add(Box.createVerticalStrut(SPACE_MD));
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
        final String target = getSelectedPrivateTarget();
        if(target != null && target.length() > 0) {
            menu.add(createConversationMenuItem("查看主页", new Runnable() {
                public void run() { showUserHomeDialog(target); }
            }));
            if(friends.contains(target)) {
                menu.add(createConversationMenuItem("设置备注", new Runnable() {
                    public void run() { setFriendRemark(target); }
                }));
                menu.add(createConversationMenuItem("删除好友", new Runnable() {
                    public void run() { deleteFriend(target); }
                }));
            } else {
                menu.add(createConversationMenuItem("添加好友", new Runnable() {
                    public void run() { requestFriend(target); }
                }));
            }
            menu.addSeparator();
        }
        if(selectedGroupName != null) {
            menu.add(createConversationMenuItem("群聊资料", new Runnable() {
                public void run() { showGroupInfoDialog(selectedGroupName); }
            }));
            menu.add(createConversationMenuItem("查看群成员", new Runnable() {
                public void run() { showGroupMembersDialog(selectedGroupName); }
            }));
            menu.add(createConversationMenuItem("拉人进群", new Runnable() {
                public void run() { inviteMembersToGroup(selectedGroupName); }
            }));
            menu.add(createConversationMenuItem("踢人出群", new Runnable() {
                public void run() { kickMemberFromGroup(selectedGroupName); }
            }));
            menu.add(createConversationMenuItem("退出群聊", new Runnable() {
                public void run() { leaveGroup(selectedGroupName); }
            }));
            menu.addSeparator();
        }
        menu.add(createConversationMenuItem("发起视频通话", new Runnable() {
            public void run() { startVideoCall(); }
        }));
        menu.addSeparator();
        menu.add(createConversationMenuItem("刷新在线列表", new Runnable() {
            public void run() { refreshUsers(); }
        }));
        menu.add(createConversationMenuItem("打开聊天记录", new Runnable() {
            public void run() { showLogPath(); }
        }));
        menu.addSeparator();
        menu.add(createConversationMenuItem("清空当前窗口", new Runnable() {
            public void run() { clearCurrentConversationWindow(); }
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
        loadPendingDeliveries();
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
        if(text.startsWith("Current Connected Users:")) return null;

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
                if(!friends.contains(sender)) {
                    showInlineNotice("收到非好友 " + sender + " 的私聊，已拦截。", DANGER);
                    appendLog("[blocked private from " + sender + "] " + body);
                    return null;
                }
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
                if(handleBroadcastProtocolMessage(sender, body, time)) return null;
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
        if(handleRecallProtocolMessage(sender, body)) return true;
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
        if(!friends.contains(sender)) {
            showInlineNotice("收到非好友 " + sender + " 的私聊，已拦截。", DANGER);
            appendLog("[blocked private from " + sender + "] " + message);
            return true;
        }
        ChatMessage chatMessage = withConversation(new ChatMessage(MessageKind.INCOMING,
                sender, message, time), sender);
        chatMessage.messageId = messageId;
        if(historyWindow != null) historyWindow.addMessage(chatMessage);
        appendLog("[private] " + sender + ": " + message);
        rememberReadReceipt(sender, messageId);
        return true;
    }

    private boolean handleRecallProtocolMessage(String sender, String body) {
        if(!body.startsWith(MESSAGE_RECALL_PREFIX)) return false;
        String payload = body.substring(MESSAGE_RECALL_PREFIX.length());
        String[] parts = payload.split("\\|", 2);
        String messageId = parts.length > 0 ? decodeToken(parts[0]) : "";
        String key = parts.length > 1 ? decodeToken(parts[1]) : sender;
        if(key == null || key.length() == 0 || "private".equals(key)) key = sender;
        recallMessageById(key, messageId, sender + " 撤回了一条消息");
        return true;
    }

    private boolean handleBroadcastProtocolMessage(String sender, String body, String time) {
        if(body.startsWith(MESSAGE_RECALL_PREFIX)) return handleRecallProtocolMessage(sender, body);
        return false;
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
        if(action.startsWith(VIDEO_AUDIO + "|")) {
            handleVideoAudioFrame(sender, action.substring((VIDEO_AUDIO + "|").length()));
        } else if(action.startsWith(VIDEO_FRAME + "|")) {
            handleVideoFrame(sender, action.substring((VIDEO_FRAME + "|").length()));
        } else if(VIDEO_INVITE.equals(action)) {
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
            if(moment != null) {
                if(shouldAcceptMoment(sender, moment)) {
                    upsertMoment(moment);
                } else {
                    removeMomentById(moment.id);
                }
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
            String[] parts = payload.split("\\|", -1);
            if(parts.length >= 2) {
                String groupName = decodeToken(parts[0]);
                Set<String> members = csvToMembers(decodeToken(parts[1]));
                members.add(sender);
                members.add(ownNick());
                addOrUpdateGroup(groupName, members, true);
                String owner = parts.length >= 3 ? decodeToken(parts[2]) : sender;
                if(owner.length() == 0 || !members.contains(owner)) owner = sender;
                groupOwners.put(groupName, owner);
                Set<String> admins = parts.length >= 4 ? csvToMembers(decodeToken(parts[3])) : new LinkedHashSet<String>();
                admins.retainAll(members);
                admins.remove(owner);
                groupAdmins.put(groupName, admins);
                ensureGroupRoleDefaults(groupName);
                saveGroups();
                rebuildConversationList(groupLabel(groupName));
                addMsg("<font color=\"#3366cc\">" + escapeHtml(sender)
                        + " 已将你拉入群聊：" + escapeHtml(groupName) + "</font>");
            }
            return true;
        }
        if(body.startsWith(GROUP_KICK_PREFIX)) {
            String groupName = decodeToken(body.substring(GROUP_KICK_PREFIX.length()));
            if(groupName.length() > 0) {
                chatGroups.remove(groupName);
                groupOwners.remove(groupName);
                groupAdmins.remove(groupName);
                saveGroups();
                rebuildConversationList(BROADCAST_CHAT);
                showInlineNotice("你已被移出群聊：" + groupName, WARNING);
            }
            return true;
        }
        if(body.startsWith(GROUP_LEAVE_PREFIX)) {
            String payload = body.substring(GROUP_LEAVE_PREFIX.length());
            String[] parts = payload.split("\\|", 2);
            if(parts.length == 2) {
                String groupName = decodeToken(parts[0]);
                String member = decodeToken(parts[1]);
                Set<String> members = chatGroups.get(groupName);
                if(members != null) {
                    members.remove(member);
                    saveGroups();
                    rebuildConversationList(selectedConversationKey());
                }
            }
            return true;
        }
        if(body.startsWith(GROUP_ROLE_PREFIX)) {
            String payload = body.substring(GROUP_ROLE_PREFIX.length());
            String[] parts = payload.split("\\|", -1);
            if(parts.length >= 3) {
                String groupName = decodeToken(parts[0]);
                String owner = decodeToken(parts[1]);
                Set<String> admins = csvToMembers(decodeToken(parts[2]));
                if(chatGroups.containsKey(groupName)) {
                    groupOwners.put(groupName, owner);
                    admins.retainAll(chatGroups.get(groupName));
                    admins.remove(owner);
                    groupAdmins.put(groupName, admins);
                    saveGroups();
                    rebuildConversationList(selectedConversationKey());
                }
            }
            return true;
        }
        if(body.startsWith(GROUP_MESSAGE_PREFIX)) {
            String payload = body.substring(GROUP_MESSAGE_PREFIX.length());
            String[] parts = payload.split("\\|", 3);
            if(parts.length >= 2) {
                String groupName = decodeToken(parts[0]);
                String messageId = parts.length == 3 ? decodeToken(parts[1]) : "";
                String message = decodeToken(parts.length == 3 ? parts[2] : parts[1]);
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
                ChatMessage groupMessage = withConversation(new ChatMessage(MessageKind.INCOMING,
                        sender + " / " + groupName, message, time), groupLabel(groupName));
                groupMessage.messageId = messageId;
                historyWindow.addMessage(groupMessage);
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
            saveFriendRequestState();
            updateFriendRequestBadge();
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
            saveFriendRequestState();
            updateFriendRequestBadge();
            addMsg("<font color=\"#666666\">" + escapeHtml(sender) + " 拒绝了你的好友申请。</font>");
            if(onlineList != null) onlineList.repaint();
            return true;
        }
        if(body.startsWith(FRIEND_DELETE_PREFIX)) {
            removeFriendLocally(sender, false);
            showInlineNotice(sender + " 已将你删除，双方现在是非好友。", WARNING);
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
        saveFriendRequestState();
        updateFriendRequestBadge();
        showInlineNotice("收到 " + sender + " 的好友申请，请在好友申请通知中心处理。", PRIMARY_DARK);
    }

    private void sendFriendResponse(String target, boolean accepted) {
        if(ck != null && ck.isConnected()) {
            ck.sendMessage("/msg " + target + " "
                    + (accepted ? FRIEND_ACCEPT_PREFIX : FRIEND_REJECT_PREFIX) + ownNick());
        }
    }

    private void sendFriendAcceptedGreeting(String target) {
        if(target == null || target.length() == 0) return;
        sendPrivate(target, "我们已成为好友，开始聊天吧");
    }

    private void showFriendRequestCard(final String sender) {
        if(historyWindow == null) {
            addMsg("<font color=\"#3366cc\">收到 " + escapeHtml(sender) + " 的好友申请。</font>");
            return;
        }
        final BubblePanel card = new BubblePanel(SURFACE, BORDER_LIGHT, RADIUS_LG);
        card.setLayout(new BorderLayout(SPACE_MD, 0));
        card.setBorder(pad(SPACE_MD));
        card.setMaximumSize(new Dimension(520, 92));

        AvatarView avatar = new AvatarView(sender, false);
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(sender + " 请求添加你为好友");
        title.setFont(UI_FONT_BOLD);
        title.setForeground(TEXT);
        JLabel detail = new JLabel("同意后可在私聊中发送文字、文件、语音和视频通话。");
        detail.setFont(UI_FONT_SMALL);
        detail.setForeground(MUTED);
        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(SPACE_XS));
        textPanel.add(detail);

        final JButton reject = createButton("拒绝", false);
        final JButton accept = createButton("同意", true);
        reject.setPreferredSize(new Dimension(68, 34));
        accept.setPreferredSize(new Dimension(68, 34));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, SPACE_SM, 0));
        actions.setOpaque(false);
        actions.add(reject);
        actions.add(accept);

        ActionListener finish = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean accepted = e.getSource() == accept;
                accept.setEnabled(false);
                reject.setEnabled(false);
                incomingFriendRequests.remove(sender);
                if(accepted) {
                    addFriend(sender);
                    sendFriendResponse(sender, true);
                    sendFriendAcceptedGreeting(sender);
                    detail.setText("已同意，" + sender + " 已加入好友列表。");
                    detail.setForeground(SUCCESS);
                } else {
                    sendFriendResponse(sender, false);
                    detail.setText("已拒绝该好友申请。");
                    detail.setForeground(MUTED);
                }
                card.revalidate();
                card.repaint();
            }
        };
        reject.addActionListener(finish);
        accept.addActionListener(finish);

        card.add(avatar, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        card.add(actions, BorderLayout.EAST);
        historyWindow.addInlineComponent(card);
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
            setConnectionStatus("正在连接 " + host + ":" + port, WARNING);
            ck = new ClientKernel(host, port);
            if(ck.isConnected()) {
                ck.addClient(this);
                ck.setNick(txtNick.getText());
                initLogFile(txtNick.getText());
                setConnectionStatus("已连接 · " + host + ":" + port, SUCCESS);
                if(statusLabel != null) {
                    statusLabel.setToolTipText("已连接 " + host + ":" + port
                            + "，本地端口 " + ck.getLocalPort());
                }
                setTitle(appName + " - " + txtNick.getText());
                connectionWasHealthy = true;
                connectionLossReported = false;
                lastUserRefreshAt = 0;
                addMsg("<font color=\"#008000\">已连接到聊天服务器。</font>");
                appendLog("[connection] connected to " + host + ":" + port
                        + ", local port " + ck.getLocalPort());
                refreshUsers();
                uploadOwnMomentsToServer();
            } else {
                String detail = ck.getLastErrorMessage();
                connectionWasHealthy = false;
                setConnectionStatus("连接失败，请检查服务端、防火墙和端口", DANGER);
                showInlineNotice("连接失败：服务端未启动、IP/端口错误或防火墙未放行。", DANGER);
                appendLog("[connection failed] "
                        + (detail.length() == 0 ? "server not reachable" : detail));
            }
        } catch(Exception e) {
            setConnectionStatus("连接错误：" + e.getMessage(), DANGER);
            showInlineNotice("连接错误：" + e.getMessage(), DANGER);
            appendLog("[connection error] " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void startHealthMonitor() {
        if(healthTimer != null) healthTimer.stop();
        healthTimer = new javax.swing.Timer(HEALTH_CHECK_INTERVAL_MS, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runHealthCheck();
            }
        });
        healthTimer.setInitialDelay(HEALTH_CHECK_INTERVAL_MS);
        healthTimer.start();
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                shutdownClientConnection();
            }
            public void windowClosed(WindowEvent e) {
                shutdownClientConnection();
            }
        });
    }

    private void shutdownClientConnection() {
        if(healthTimer != null) {
            healthTimer.stop();
            healthTimer = null;
        }
        if(ck != null) {
            ck.dropMe();
            ck = null;
        }
    }

    private void runHealthCheck() {
        boolean connected = ck != null && ck.isConnected();
        if(connected) {
            connectionWasHealthy = true;
            connectionLossReported = false;
            long now = System.currentTimeMillis();
            if(now - lastUserRefreshAt >= USER_REFRESH_INTERVAL_MS) {
                lastUserRefreshAt = now;
                refreshUsers();
            }
            return;
        }
        if(connectionWasHealthy) {
            handleConnectionLost();
        } else {
            updateVideoButtonState();
            updateSendButtonState();
        }
    }

    private void handleConnectionLost() {
        connectionWasHealthy = false;
        String selected = selectedConversationKey();
        visibleUsers.clear();
        setConnectionStatus("连接已断开，点击重连", DANGER);
        if(!connectionLossReported) {
            connectionLossReported = true;
            addMsg("<font color=\"#cc6600\">连接已断开。请确认服务端仍在运行，然后点击“重试/重连”。未发送文字消息会保留在待发送队列。</font>");
            showInlineNotice("连接已断开，请检查服务端并点击重连。", DANGER);
            appendLog("[connection lost] client detected disconnected socket");
        }
        if(onlineList != null) rebuildConversationList(selected);
        updateAttachmentPreview();
    }

    private void setConnectionStatus(String text, Color color) {
        if(statusLabel != null) {
            statusLabel.setText(text);
            statusLabel.setForeground(color);
            statusLabel.setToolTipText(text);
        }
        if(connectionDotLabel != null) {
            connectionDotLabel.setForeground(color == null ? SOFT_MUTED : color);
        }
        if(buttonConnect != null) {
            if(SUCCESS.equals(color)) buttonConnect.setText("重连");
            else if(WARNING.equals(color)) buttonConnect.setText("连接中");
            else if(DANGER.equals(color)) buttonConnect.setText("重试");
            else buttonConnect.setText("连接");
        }
        updateVideoButtonState();
        updateSendButtonState();
    }

    private void setConversationState(String text, Color foreground, Color border) {
        if(conversationStateLabel == null) return;
        conversationStateLabel.setText(text);
        conversationStateLabel.setForeground(foreground);
        conversationStateLabel.setVisible(!"广播".equals(text) && !"群聊".equals(text));
        conversationStateLabel.setBorder(new CompoundBorder(
                new RoundedBorder(border, RADIUS_XL),
                pad(4, 10, 4, 10)));
        conversationStateLabel.setToolTipText(text);
    }

    private boolean isBroadcastConversation(String value) {
        return BROADCAST_CHAT.equals(value);
    }

    private boolean isGroupConversation(String value) {
        return value != null && value.startsWith(GROUP_LABEL_PREFIX);
    }

    private boolean isUserConversation(String value) {
        return value != null && value.length() > 0
                && !isBroadcastConversation(value)
                && !isGroupConversation(value);
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
        boolean conversationChanged = !sameConversation(renderedConversationKey, selected);
        if(selected == null) {
            selectedChatTarget = null;
            selectedGroupName = null;
            if(conversationTitleLabel != null) conversationTitleLabel.setText("选择会话");
            if(conversationSubtitleLabel != null) conversationSubtitleLabel.setText("从左侧选择好友、群聊或广播");
            if(conversationAvatar != null) conversationAvatar.setAvatar("?", false);
            setConversationState("未选择", MUTED, BORDER_LIGHT);
            if(buttonFile != null) buttonFile.setEnabled(true);
        } else if(isBroadcastConversation(selected)) {
            selectedChatTarget = null;
            selectedGroupName = null;
            if(conversationTitleLabel != null) conversationTitleLabel.setText(BROADCAST_CHAT);
            if(conversationSubtitleLabel != null) conversationSubtitleLabel.setText("消息将广播给所有在线用户");
            if(conversationAvatar != null) conversationAvatar.setAvatar(BROADCAST_CHAT, false);
            setConversationState("广播", PRIMARY_DARK, new Color(194, 235, 210));
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
            setConversationState("群聊", PRIMARY_DARK, new Color(194, 235, 210));
            if(buttonFile != null) buttonFile.setEnabled(true);
        } else {
            selectedChatTarget = selected;
            selectedGroupName = null;
            if(conversationTitleLabel != null) conversationTitleLabel.setText(displayNameWithRemark(selected));
            boolean online = visibleUsers.contains(selected);
            if(conversationSubtitleLabel != null) {
                if(!friends.contains(selected)) {
                    conversationSubtitleLabel.setText(online ? "非好友，不能发送消息" : "非好友离线，不能发送消息");
                } else {
                    conversationSubtitleLabel.setText(online ? "当前为私聊，只发送给 " + selected : "好友离线，消息将待发送");
                }
            }
            if(conversationAvatar != null) conversationAvatar.setAvatar(selected, false);
            setConversationState(online ? "在线" : "离线",
                    online ? SUCCESS : SOFT_MUTED,
                    online ? new Color(194, 235, 210) : BORDER_LIGHT);
            if(buttonFile != null) buttonFile.setEnabled(true);
        }
        markConversationRead(selected);
        sendPendingReadReceipts(selected);
        if(conversationChanged) renderConversationMessages(selected);
        updateVideoButtonState();
        updateAttachmentPreview();
        updateSendButtonState();
        if(msgWindow != null) msgWindow.requestFocusInWindow();
    }

    private void updateVideoButtonState() {
        boolean canCall = ck != null && ck.isConnected()
                && selectedChatTarget != null
                && friends.contains(selectedChatTarget)
                && visibleUsers.contains(selectedChatTarget);
        if(buttonVideo != null) {
            buttonVideo.setEnabled(canCall);
            buttonVideo.setVisible(canCall);
            buttonVideo.setText(canCall ? "视频" : "");
            buttonVideo.setPreferredSize(canCall
                    ? new Dimension(58, BUTTON_HEIGHT)
                    : new Dimension(0, BUTTON_HEIGHT));
            buttonVideo.setToolTipText(canCall
                    ? "和 " + selectedChatTarget + " 视频通话"
                    : "请选择在线好友后发起视频通话");
            Container parent = buttonVideo.getParent();
            if(parent != null) {
                parent.revalidate();
                parent.repaint();
            }
        }
        updateAttachmentToolState(canCall);
    }

    private void updateAttachmentToolState(boolean canSendLiveAttachment) {
        boolean canAttach = ck != null && ck.isConnected() && attachmentAllowedInCurrentConversation();
        String selected = selectedConversationKey();
        String targetName = selected == null ? "当前会话" : displayConversationName(selected);
        String tooltip = canAttach ? "发送到 " + targetName : "请选择可发送的会话";
        if(buttonImage != null) {
            buttonImage.setEnabled(canAttach);
            buttonImage.setToolTipText(canAttach ? "发送图片到 " + targetName : tooltip);
        }
        if(buttonVoice != null) {
            buttonVoice.setEnabled(canAttach);
            buttonVoice.setToolTipText(canAttach ? "发送语音文件到 " + targetName : tooltip);
        }
        if(buttonRecord != null) {
            buttonRecord.setEnabled(canAttach);
            buttonRecord.setToolTipText(canAttach ? "录制语音到 " + targetName : tooltip);
        }
        if(buttonFile != null) {
            buttonFile.setEnabled(canAttach);
            buttonFile.setToolTipText(canAttach ? "发送文件到 " + targetName : tooltip);
        }
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
        String key = conversationKeyForMessage(message);
        message.conversationKey = key;
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
            if(userModel != null) {
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
        if(outgoing && message.deliveryStatus != null && message.deliveryStatus.length() > 0
                && !isFailureDeliveryStatus(message.deliveryStatus)) {
            text += " · " + message.deliveryStatus;
        }
        return text;
    }

    private boolean isFailureDeliveryStatus(String status) {
        if(status == null) return false;
        return status.indexOf("未发送") >= 0
                || status.indexOf("发送失败") >= 0
                || status.indexOf("非好友") >= 0
                || status.indexOf("失败") >= 0;
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
        return sameConversation(normalizeConversationKey(key), selectedConversationKey());
    }

    private boolean sameConversation(String a, String b) {
        if(a == null) return b == null;
        return a.equals(b);
    }

    private String selectedConversationKey() {
        return onlineList == null ? null : onlineList.getSelectedValue();
    }

    private String normalizeConversationKey(String key) {
        return key == null || key.length() == 0 ? BROADCAST_CHAT : key;
    }

    private String conversationKeyForMessage(ChatMessage message) {
        if(message == null) return BROADCAST_CHAT;
        String key = message.conversationKey;
        if((key == null || key.length() == 0) && message.kind == MessageKind.SYSTEM) {
            key = BROADCAST_CHAT;
        }
        return normalizeConversationKey(key);
    }

    private String storeConversationMessage(ChatMessage message) {
        if(message.localMessageId == null || message.localMessageId.length() == 0) {
            message.localMessageId = UUID.randomUUID().toString();
        }
        String key = conversationKeyForMessage(message);
        message.conversationKey = key;
        java.util.List<ChatMessage> messages = conversationMessages.get(key);
        if(messages == null) {
            messages = new ArrayList<ChatMessage>();
            conversationMessages.put(key, messages);
        }
        if(message.messageId != null && message.messageId.length() > 0) {
            for(int i=0;i<messages.size();i++) {
                ChatMessage existing = messages.get(i);
                if(message.messageId.equals(existing.messageId)
                        && existing.kind == message.kind) {
                    return key;
                }
            }
        }
        messages.add(message);
        while(messages.size() > 500) messages.remove(0);
        return key;
    }

    private void renderConversationMessages(String key) {
        String previousRenderedKey = renderedConversationKey;
        renderedConversationKey = key;
        if(!sameConversation(previousRenderedKey, key)) messageSearchIndex = -1;
        java.util.List<ChatMessage> messages = key == null
                ? null
                : conversationMessages.get(key);
        updateMessageSearchResults(messages);
        if(historyWindow == null) return;
        trackedStatusLabels.clear();
        historyWindow.showMessages(messages, emptyConversationTitle(key), emptyConversationText(key));
    }

    private void applyMessageSearchField() {
        String next = messageSearchField == null ? "" : messageSearchField.getText().trim();
        if(!next.equals(messageSearchKeyword)) {
            messageSearchKeyword = next;
            messageSearchIndex = -1;
        }
        renderConversationMessages(selectedConversationKey());
    }

    private boolean isMessageSearchActive() {
        return messageSearchKeyword != null && messageSearchKeyword.trim().length() > 0;
    }

    private void updateMessageSearchResults(java.util.List<ChatMessage> messages) {
        String currentId = currentMessageSearchMatchId();
        messageSearchMatches.clear();
        if(isMessageSearchActive() && messages != null) {
            for(int i=0;i<messages.size();i++) {
                ChatMessage message = messages.get(i);
                if(messageMatchesSearch(message, messageSearchKeyword)) {
                    if(message.localMessageId == null || message.localMessageId.length() == 0) {
                        message.localMessageId = UUID.randomUUID().toString();
                    }
                    messageSearchMatches.add(message.localMessageId);
                }
            }
        }
        if(messageSearchMatches.size() == 0) {
            messageSearchIndex = -1;
        } else if(currentId != null && messageSearchMatches.contains(currentId)) {
            messageSearchIndex = messageSearchMatches.indexOf(currentId);
        } else if(messageSearchIndex < 0 || messageSearchIndex >= messageSearchMatches.size()) {
            messageSearchIndex = 0;
        }
        updateMessageSearchControls();
    }

    private boolean messageMatchesSearch(ChatMessage message, String keyword) {
        if(message == null || keyword == null || keyword.trim().length() == 0) return false;
        String needle = keyword.trim().toLowerCase(Locale.ROOT);
        return containsSearchText(message.body, needle)
                || containsSearchText(message.sender, needle)
                || containsSearchText(message.fileName, needle);
    }

    private boolean containsSearchText(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).indexOf(needle) >= 0;
    }

    private boolean isMessageSearchMatch(ChatMessage message) {
        return message != null && isMessageSearchActive()
                && message.localMessageId != null
                && messageSearchMatches.contains(message.localMessageId);
    }

    private boolean isCurrentMessageSearchMatch(ChatMessage message) {
        String currentId = currentMessageSearchMatchId();
        return message != null && currentId != null && currentId.equals(message.localMessageId);
    }

    private String currentMessageSearchMatchId() {
        if(messageSearchIndex < 0 || messageSearchIndex >= messageSearchMatches.size()) return null;
        return messageSearchMatches.get(messageSearchIndex);
    }

    private void goToMessageSearchMatch(int delta) {
        if(!isMessageSearchActive() || messageSearchMatches.size() == 0) return;
        int size = messageSearchMatches.size();
        if(messageSearchIndex < 0) messageSearchIndex = 0;
        else messageSearchIndex = (messageSearchIndex + delta + size) % size;
        updateMessageSearchControls();
        renderConversationMessages(selectedConversationKey());
    }

    private void clearMessageSearch() {
        if(messageSearchField != null && messageSearchField.getText().length() > 0) {
            messageSearchField.setText("");
            messageSearchField.requestFocusInWindow();
            return;
        }
        messageSearchKeyword = "";
        messageSearchIndex = -1;
        messageSearchMatches.clear();
        updateMessageSearchControls();
        renderConversationMessages(selectedConversationKey());
    }

    private void updateMessageSearchControls() {
        boolean active = isMessageSearchActive();
        int count = messageSearchMatches.size();
        if(messageSearchCountLabel != null) {
            messageSearchCountLabel.setText(active && count > 0 ? (messageSearchIndex + 1) + "/" + count : "0/0");
            messageSearchCountLabel.setForeground(active && count == 0 ? WARNING : MUTED);
        }
        boolean canNavigate = active && count > 0;
        if(buttonSearchPrev != null) buttonSearchPrev.setEnabled(canNavigate);
        if(buttonSearchNext != null) buttonSearchNext.setEnabled(canNavigate);
        if(buttonSearchClear != null) buttonSearchClear.setEnabled(active);
    }

    private String emptyConversationTitle(String key) {
        if(key == null) return "选择一个会话";
        if(isBroadcastConversation(key)) return "还没有广播消息";
        if(isGroupConversation(key)) return groupNameFromLabel(key) + " 还没有消息";
        return "还没有和 " + key + " 聊天";
    }

    private String emptyConversationText(String key) {
        if(key == null) return "从左侧选择好友、群聊或广播后开始聊天";
        if(isBroadcastConversation(key)) return "发送一条广播消息，在线用户都能看到";
        if(isGroupConversation(key)) return "发送一条群聊消息，离线成员上线后也会收到";
        return visibleUsers.contains(key) ? "发送一条消息开始私聊" : "对方离线，文字消息会先显示为待发送";
    }

    private boolean canRecallMessage(ChatMessage message) {
        return message != null && message.kind == MessageKind.OUTGOING && !message.fileMessage
                && !message.recalled && message.messageId != null && message.messageId.length() > 0
                && message.sentAtMillis > 0 && System.currentTimeMillis() - message.sentAtMillis <= 2L * 60L * 1000L;
    }

    private void recallOwnMessage(ChatMessage message) {
        if(!canRecallMessage(message)) {
            showInlineNotice("只能撤回自己发送且 2 分钟内的文字消息。", WARNING);
            return;
        }
        String key = message.conversationKey;
        message.recalled = true;
        message.body = "你撤回了一条消息";
        message.deliveryStatus = null;
        sendRecallProtocol(key, message.messageId);
        renderConversationMessages(selectedConversationKey());
        updateConversationPreview(message);
    }

    private void sendRecallProtocol(String key, String messageId) {
        String payload = MESSAGE_RECALL_PREFIX + encodeToken(messageId) + "|" + encodeToken(key == null ? "" : key);
        if(selectedChatTarget != null || (key != null && isUserConversation(key))) {
            String target = selectedChatTarget != null ? selectedChatTarget : key;
            if(ck != null && ck.isConnected() && target != null) ck.sendMessage("/msg " + target + " " + payload);
            else if(target != null) queuePendingDelivery(target, payload, "消息撤回");
        } else if(key != null && isGroupConversation(key)) {
            String groupName = groupNameFromLabel(key);
            Set<String> members = chatGroups.get(groupName);
            if(members != null) {
                Iterator<String> it = members.iterator();
                while(it.hasNext()) {
                    String member = it.next();
                    if(member.equalsIgnoreCase(ownNick())) continue;
                    if(ck != null && ck.isConnected() && visibleUsers.contains(member)) ck.sendMessage("/msg " + member + " " + payload);
                    else queuePendingDelivery(member, payload, "群消息撤回");
                }
            }
        } else if(key != null && isBroadcastConversation(key)) {
            if(ck != null && ck.isConnected()) ck.sendMessage(payload);
        }
    }

    private void recallMessageById(String key, String messageId, String text) {
        if(messageId == null || messageId.length() == 0) return;
        String normalized = normalizeConversationKey(key);
        java.util.List<ChatMessage> messages = conversationMessages.get(normalized);
        if(messages == null && isUserConversation(normalized)) messages = conversationMessages.get(key);
        if(messages == null) return;
        for(int i=0;i<messages.size();i++) {
            ChatMessage m = messages.get(i);
            if(messageId.equals(m.messageId)) {
                m.recalled = true;
                m.body = text == null ? "对方撤回了一条消息" : text;
                m.deliveryStatus = null;
                if(isSelectedConversation(normalized)) renderConversationMessages(normalized);
                updateConversationPreview(m);
                break;
            }
        }
    }

    private void clearCurrentConversationWindow() {
        String selected = selectedConversationKey();
        if(selected != null) conversationMessages.remove(selected);
        renderedConversationKey = null;
        renderConversationMessages(selected);
    }

    private String previewText(ChatMessage message) {
        String speaker = message.kind == MessageKind.OUTGOING ? "我" : shortSenderName(message.sender);
        String content;
        if(message.recalled) {
            content = "[已撤回]";
        } else if(message.fileMessage) {
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
        if(toSend != null && toSend.length() > MESSAGE_TEXT_LIMIT) {
            showInlineNotice("消息过长，请删减到 " + MESSAGE_TEXT_LIMIT + " 字以内。", DANGER);
            return;
        }
        if(ck == null || !ck.isConnected()) {
            showInlineNotice("请先连接服务器。", DANGER);
            return;
        }
        if(onlineList != null && onlineList.getSelectedValue() == null) {
            showInlineNotice("请先选择一个会话。", DANGER);
            return;
        }
        if(hasFile) {
            if(!attachmentAllowedInCurrentConversation()) {
                showInlineNotice(selectedChatTarget != null ? "非好友不能发送附件，请先添加好友。" : "当前会话不能发送附件。", DANGER);
                return;
            }
            if(hasText && toSend.startsWith("/")) {
                showInlineNotice("附件待发送时不能同时输入命令。", DANGER);
                return;
            }
            if(hasText) {
                if(selectedGroupName != null) sendGroupMessage(selectedGroupName, toSend);
                else if(selectedChatTarget != null) sendPrivate(selectedChatTarget, toSend);
                else sendBroadcastMessage(toSend);
            }
            if(sendFileInCurrentConversation(pendingFile)) {
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
            clearCurrentConversationWindow();
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
            if(!friends.contains(selectedChatTarget)) {
                showFailedOutgoingMessage(selectedChatTarget, ownNick(), toSend, "非好友，消息未发送");
                showInlineNotice("非好友不能发送消息，请先添加好友。", DANGER);
                lastMsg = "" + toSend;
                msgWindow.setText("");
                return;
            }
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


    private void showFriendRequestCenter() {
        final JDialog dialog = new JDialog(this, "好友申请通知中心", true);
        dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        dialog.setSize(520, 560);
        dialog.setMinimumSize(new Dimension(460, 420));
        dialog.setLocationRelativeTo(this);
        JPanel root = new JPanel(new BorderLayout(SPACE_MD, SPACE_MD));
        root.setBackground(CHAT_BACKGROUND);
        root.setBorder(pad(SPACE_LG));
        JLabel title = new JLabel("好友申请通知中心");
        title.setFont(PAGE_TITLE_FONT);
        title.setForeground(TEXT);
        root.add(title, BorderLayout.NORTH);
        JPanel list = new JPanel();
        list.setBackground(CHAT_BACKGROUND);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        java.util.List<String> incoming = new ArrayList<String>(incomingFriendRequests);
        java.util.List<String> outgoing = new ArrayList<String>(sentFriendRequests);
        Collections.sort(incoming, String.CASE_INSENSITIVE_ORDER);
        Collections.sort(outgoing, String.CASE_INSENSITIVE_ORDER);
        if(incoming.size() == 0 && outgoing.size() == 0) list.add(createEmptyState("暂无好友申请", "新的好友请求会显示在这里"));
        else {
            if(incoming.size() > 0) list.add(friendRequestSectionTitle("收到的申请"));
            for(int i=0;i<incoming.size();i++) { list.add(createFriendRequestRow(incoming.get(i), true, dialog)); list.add(Box.createVerticalStrut(SPACE_SM)); }
            if(outgoing.size() > 0) list.add(friendRequestSectionTitle("已发送的申请"));
            for(int i=0;i<outgoing.size();i++) { list.add(createFriendRequestRow(outgoing.get(i), false, dialog)); list.add(Box.createVerticalStrut(SPACE_SM)); }
        }
        root.add(createModernScrollPane(list, CHAT_BACKGROUND), BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout(SPACE_SM, 0));
        bottom.setOpaque(false);
        final JTextField input = createTextField("", 16);
        JButton add = createButton("发送申请", true);
        add.setPreferredSize(new Dimension(96, BUTTON_HEIGHT));
        add.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { String nick = input.getText().trim(); if(nick.length() > 0) requestFriend(nick); dialog.dispose(); showFriendRequestCenter(); }});
        bottom.add(input, BorderLayout.CENTER);
        bottom.add(add, BorderLayout.EAST);
        root.add(bottom, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private Component friendRequestSectionTitle(String text) { JLabel label = createSectionTitle(text); label.setBorder(pad(SPACE_SM, 0, SPACE_SM, 0)); return label; }

    private Component createFriendRequestRow(final String user, final boolean incoming, final JDialog owner) {
        BubblePanel row = createCardPanel(SPACE_MD);
        row.setLayout(new BorderLayout(SPACE_MD, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));
        row.add(new AvatarView(user, false), BorderLayout.WEST);
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JLabel name = new JLabel(user);
        name.setFont(UI_FONT_BOLD);
        name.setForeground(TEXT);
        JLabel detail = new JLabel(incoming ? "请求添加你为好友" : "等待对方验证");
        detail.setFont(UI_FONT_SMALL);
        detail.setForeground(MUTED);
        info.add(name); info.add(Box.createVerticalStrut(SPACE_XS)); info.add(detail);
        row.add(info, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, SPACE_SM, 0));
        actions.setOpaque(false);
        if(incoming) {
            JButton reject = createButton("拒绝", false);
            JButton accept = createButton("同意", true);
            reject.setPreferredSize(new Dimension(64, 32));
            accept.setPreferredSize(new Dimension(64, 32));
            accept.addActionListener(new ActionListener(){ public void actionPerformed(ActionEvent e){ respondToFriendRequest(user, true); owner.dispose(); showFriendRequestCenter(); }});
            reject.addActionListener(new ActionListener(){ public void actionPerformed(ActionEvent e){ respondToFriendRequest(user, false); owner.dispose(); showFriendRequestCenter(); }});
            actions.add(reject); actions.add(accept);
        } else actions.add(createHintLabel("已发送"));
        row.add(actions, BorderLayout.EAST);
        return row;
    }

    private void respondToFriendRequest(String sender, boolean accepted) {
        incomingFriendRequests.remove(sender);
        if(accepted) addFriend(sender);
        sendFriendResponse(sender, accepted);
        if(accepted) sendFriendAcceptedGreeting(sender);
        saveFriendRequestState();
        updateFriendRequestBadge();
        showInlineNotice(accepted ? "已同意 " + sender + " 的好友申请。" : "已拒绝 " + sender + " 的好友申请。", accepted ? PRIMARY_DARK : MUTED);
    }

    private void loadFriendRequestState() {
        if(currentUserDir == null) return;
        Path file = currentUserDir.resolve(FRIEND_REQUESTS_FILE);
        if(!Files.exists(file)) return;
        try {
            java.util.List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for(int i=0;i<lines.size();i++) {
                String[] parts = lines.get(i).split("\\t", 2);
                if(parts.length != 2) continue;
                String nick = decodeToken(parts[1]);
                if(nick.length() == 0 || friends.contains(nick)) continue;
                if("IN".equals(parts[0])) incomingFriendRequests.add(nick); else if("OUT".equals(parts[0])) sentFriendRequests.add(nick);
            }
        } catch(Exception e) {}
    }

    private void saveFriendRequestState() {
        if(currentUserDir == null) return;
        try {
            Files.createDirectories(currentUserDir);
            java.util.List<String> lines = new ArrayList<String>();
            java.util.List<String> incoming = new ArrayList<String>(incomingFriendRequests);
            java.util.List<String> outgoing = new ArrayList<String>(sentFriendRequests);
            Collections.sort(incoming, String.CASE_INSENSITIVE_ORDER);
            Collections.sort(outgoing, String.CASE_INSENSITIVE_ORDER);
            for(int i=0;i<incoming.size();i++) lines.add("IN\t" + encodeToken(incoming.get(i)));
            for(int i=0;i<outgoing.size();i++) lines.add("OUT\t" + encodeToken(outgoing.get(i)));
            Files.write(currentUserDir.resolve(FRIEND_REQUESTS_FILE), lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch(Exception e) {}
    }

    private void updateFriendRequestBadge() { if(buttonAddFriend instanceof RailButton) ((RailButton)buttonAddFriend).setBadgeCount(incomingFriendRequests.size()); }

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
        saveFriendRequestState();
        updateFriendRequestBadge();
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
        saveFriendRequestState();
        updateFriendRequestBadge();
        addMsg("<font color=\"#3366cc\">已添加好友：" + escapeHtml(nick) + "</font>");
        if(visibleUsers.contains(nick)) {
            addMsg("<font color=\"#cc6600\">好友在线：" + escapeHtml(nick) + "</font>");
        }
        if(onlineList != null) onlineList.repaint();
    }

    private void deleteFriend(final String nick) {
        if(nick == null || nick.length() == 0) return;
        if(!friends.contains(nick)) {
            showInlineNotice(nick + " 还不是你的好友。", MUTED);
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "确定删除好友 “" + nick + "” 吗？删除后双方都将变为非好友。",
                "删除好友", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if(choice != JOptionPane.YES_OPTION) return;
        removeFriendLocally(nick, true);
        if(ck != null && ck.isConnected()) {
            ck.sendMessage("/msg " + nick + " " + FRIEND_DELETE_PREFIX + ownNick());
        } else {
            queuePendingDelivery(nick, FRIEND_DELETE_PREFIX + ownNick(), "删除好友同步");
        }
    }

    private void removeFriendLocally(String nick, boolean showNotice) {
        if(nick == null || nick.length() == 0) return;
        friends.remove(nick);
        friendRemarks.remove(nick);
        saveFriendRemarks();
        sentFriendRequests.remove(nick);
        incomingFriendRequests.remove(nick);
        saveFriends();
        saveFriendRequestState();
        updateFriendRequestBadge();
        refreshActiveMoments();
        rebuildConversationList(selectedConversationKey());
        updateSelectedConversation();
        if(showNotice) showInlineNotice("已删除好友：" + nick, WARNING);
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
        VideoCallWindow window = videoWindowFor(sender);
        if(window == null) {
            window = openVideoCallWindow(sender, true);
        }
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

    private void handleVideoAudioFrame(String sender, String encoded) {
        VideoCallWindow window = videoWindowFor(sender);
        if(window != null) window.receiveAudioFrame(encoded);
    }

    private void handleVideoFrame(String sender, String encoded) {
        VideoCallWindow window = videoWindowFor(sender);
        if(window != null) window.receiveVideoFrame(encoded);
    }

    private void sendVideoSignal(String target, String action) {
        if(ck != null && ck.isConnected() && target != null && target.length() > 0) {
            ck.sendMessage("/msg " + target + " " + VIDEO_CALL_PREFIX + action);
        }
    }

    private void sendVideoMedia(String target, String type, String payload) {
        if(ck != null && ck.isConnected() && target != null && target.length() > 0
                && type != null && payload != null) {
            ck.sendMessage("/msg " + target + " " + VIDEO_CALL_PREFIX + type + "|" + payload);
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
        friendRemarks.clear();
        chatGroups.clear();
        groupOwners.clear();
        groupAdmins.clear();
        sentFriendRequests.clear();
        incomingFriendRequests.clear();
        pendingPrivateMessages.clear();
        pendingDeliveries.clear();
        conversationMessages.clear();
        conversationMeta.clear();
        trackedOutgoingMessages.clear();
        trackedStatusLabels.clear();
        pendingReadReceipts.clear();
        localBroadcastEchoes.clear();
        renderedConversationKey = null;
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
            loadFriendRemarks();
            loadFriendRequestState();
            updateFriendRequestBadge();

            Path groupsFile = currentUserDir.resolve("groups.txt");
            if(Files.exists(groupsFile)) {
                java.util.List<String> groupLines = Files.readAllLines(groupsFile, StandardCharsets.UTF_8);
                for(int i=0;i<groupLines.size();i++) {
                    String line = groupLines.get(i);
                    String[] parts = line.split("\t", -1);
                    if(parts.length >= 2) {
                        String groupName = decodeToken(parts[0]);
                        Set<String> members = csvToMembers(decodeToken(parts[1]));
                        if(groupName.length() > 0) {
                            addOrUpdateGroup(groupName, members, false);
                            String owner = parts.length >= 3 ? decodeToken(parts[2]) : "";
                            if(owner.length() == 0 || !members.contains(owner)) owner = ownNick();
                            groupOwners.put(groupName, owner);
                            Set<String> admins = parts.length >= 4 ? csvToMembers(decodeToken(parts[3])) : new LinkedHashSet<String>();
                            admins.retainAll(members);
                            admins.remove(owner);
                            groupAdmins.put(groupName, admins);
                            ensureGroupRoleDefaults(groupName);
                        }
                    }
                }
            }
            loadMomentNotifications();
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
            if(sendTrackedPrivateMessage(pending.target, pending.id, pending.body)) {
                sent.add(pending.id);
                appendLog("[private delivered from queue to " + pending.target + "] " + pending.body);
            }
        }
        for(int i=0;i<sent.size();i++) {
            pendingPrivateMessages.remove(sent.get(i));
        }
        if(sent.size() > 0) savePendingPrivateMessages();
    }

    private void queuePendingDelivery(String target, String body, String note) {
        if(target == null || target.length() == 0 || body == null || body.length() == 0) return;
        String id = UUID.randomUUID().toString();
        PendingDelivery pending = new PendingDelivery(id, target, body,
                note == null ? "" : note, LocalDateTime.now().format(displayTime));
        pendingDeliveries.put(id, pending);
        savePendingDeliveries();
    }

    private void loadPendingDeliveries() {
        if(currentUserDir == null) return;
        Path file = currentUserDir.resolve(PENDING_DELIVERIES_FILE);
        if(!Files.exists(file)) return;
        try {
            java.util.List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for(int i=0;i<lines.size();i++) {
                String[] parts = lines.get(i).split("\\t", 5);
                if(parts.length != 5) continue;
                String id = parts[0];
                String target = decodeToken(parts[1]);
                String time = decodeToken(parts[2]);
                String note = decodeToken(parts[3]);
                String body = decodeToken(parts[4]);
                if(id.length() == 0 || target.length() == 0 || body.length() == 0) continue;
                pendingDeliveries.put(id, new PendingDelivery(id, target, body, note, time));
            }
        } catch(Exception e) {
            addMsg("<font color=\"#ff0000\">加载离线投递队列失败：" + escapeHtml(e.getMessage()) + "</font>");
        }
    }

    private void savePendingDeliveries() {
        if(currentUserDir == null) return;
        try {
            Files.createDirectories(currentUserDir);
            java.util.List<String> lines = new ArrayList<String>();
            Iterator<PendingDelivery> it = pendingDeliveries.values().iterator();
            while(it.hasNext()) {
                PendingDelivery pending = it.next();
                lines.add(pending.id + "\t" + encodeToken(pending.target) + "\t"
                        + encodeToken(pending.time) + "\t" + encodeToken(pending.note)
                        + "\t" + encodeToken(pending.body));
            }
            Files.write(currentUserDir.resolve(PENDING_DELIVERIES_FILE), lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch(Exception e) {
            addMsg("<font color=\"#ff0000\">保存离线投递队列失败：" + escapeHtml(e.getMessage()) + "</font>");
        }
    }

    private void flushPendingDeliveries(Set<String> onlineUsers) {
        if(ck == null || !ck.isConnected() || pendingDeliveries.size() == 0) return;
        java.util.List<String> sent = new ArrayList<String>();
        Iterator<PendingDelivery> it = pendingDeliveries.values().iterator();
        while(it.hasNext()) {
            PendingDelivery pending = it.next();
            if(!onlineUsers.contains(pending.target)) continue;
            if(pending.body.startsWith(PENDING_FILE_PREFIX)) {
                String payload = pending.body.substring(PENDING_FILE_PREFIX.length());
                String[] fileParts = payload.split("\\|", 2);
                if(fileParts.length == 2) {
                    ck.sendMessage("/file " + pending.target + " " + decodeToken(fileParts[0]) + " " + decodeToken(fileParts[1]));
                }
            } else {
                ck.sendMessage("/msg " + pending.target + " " + pending.body);
            }
            sent.add(pending.id);
            appendLog("[offline delivery to " + pending.target + "] " + pending.note);
        }
        for(int i=0;i<sent.size();i++) {
            pendingDeliveries.remove(sent.get(i));
        }
        if(sent.size() > 0) savePendingDeliveries();
    }

    private Path friendRemarksFile() {
        return currentUserDir.resolve("friend-remarks.properties");
    }

    private void loadFriendRemarks() {
        friendRemarks.clear();
        if(currentUserDir == null) return;
        Path file = friendRemarksFile();
        if(!Files.exists(file)) return;
        try(InputStream in = Files.newInputStream(file)) {
            Properties props = new Properties();
            props.load(in);
            Iterator<String> it = props.stringPropertyNames().iterator();
            while(it.hasNext()) {
                String key = it.next();
                String value = props.getProperty(key, "").trim();
                if(value.length() > 0) friendRemarks.put(key, value);
            }
        } catch(Exception e) {}
    }

    private void saveFriendRemarks() {
        if(currentUserDir == null) return;
        try {
            Files.createDirectories(currentUserDir);
            Properties props = new Properties();
            Iterator<Map.Entry<String,String>> it = friendRemarks.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry<String,String> entry = it.next();
                if(entry.getKey() != null && entry.getValue() != null && entry.getValue().trim().length() > 0) {
                    props.setProperty(entry.getKey(), entry.getValue().trim());
                }
            }
            try(OutputStream out = Files.newOutputStream(friendRemarksFile())) {
                props.store(out, "Friend remarks");
            }
        } catch(Exception e) {
            showInlineNotice("保存好友备注失败：" + e.getMessage(), DANGER);
        }
    }

    private void setFriendRemark(final String nick) {
        if(nick == null || nick.length() == 0) return;
        if(!friends.contains(nick)) {
            showInlineNotice("只有好友才能设置备注。", WARNING);
            return;
        }
        JTextField field = createTextField(friendRemarks.get(nick) == null ? "" : friendRemarks.get(nick), 18);
        JPanel panel = new JPanel(new BorderLayout(SPACE_SM, SPACE_SM));
        panel.add(new JLabel("给好友 “" + nick + "” 设置备注，留空则恢复原昵称："), BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        int option = JOptionPane.showConfirmDialog(this, panel, "设置好友备注", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if(option != JOptionPane.OK_OPTION) return;
        String value = field.getText().trim();
        if(value.length() == 0) friendRemarks.remove(nick);
        else friendRemarks.put(nick, value);
        saveFriendRemarks();
        rebuildConversationList(selectedConversationKey());
        updateSelectedConversation();
        if(onlineList != null) onlineList.repaint();
        showInlineNotice(value.length() == 0 ? "已恢复好友原昵称。" : "好友备注已保存。", PRIMARY_DARK);
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
                ensureGroupRoleDefaults(entry.getKey());
                lines.add(encodeToken(entry.getKey()) + "	"
                        + encodeToken(membersToCsv(entry.getValue())) + "	"
                        + encodeToken(nullSafe(groupOwners.get(entry.getKey()))) + "	"
                        + encodeToken(membersToCsv(groupAdmins.get(entry.getKey()))));
            }
            Files.write(currentUserDir.resolve("groups.txt"), lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch(Exception e) {
            addMsg("<font color=\"#ff0000\">保存群聊列表失败: " + escapeHtml(e.getMessage()) + "</font>");
        }
    }

    private void ensureGroupRoleDefaults(String groupName) {
        if(groupName == null || groupName.length() == 0) return;
        Set<String> members = chatGroups.get(groupName);
        if(members == null) return;
        String owner = groupOwners.get(groupName);
        if(owner == null || owner.length() == 0 || !members.contains(owner)) {
            owner = members.contains(ownNick()) ? ownNick() : (members.size() > 0 ? members.iterator().next() : ownNick());
            groupOwners.put(groupName, owner);
        }
        Set<String> admins = groupAdmins.get(groupName);
        if(admins == null) { admins = new LinkedHashSet<String>(); groupAdmins.put(groupName, admins); }
        admins.retainAll(members);
        admins.remove(owner);
    }

    private boolean isGroupOwner(String groupName, String user) {
        ensureGroupRoleDefaults(groupName);
        String owner = groupOwners.get(groupName);
        return user != null && owner != null && owner.equalsIgnoreCase(user);
    }

    private boolean isGroupAdmin(String groupName, String user) {
        ensureGroupRoleDefaults(groupName);
        Set<String> admins = groupAdmins.get(groupName);
        if(user == null || admins == null) return false;
        Iterator<String> it = admins.iterator();
        while(it.hasNext()) if(user.equalsIgnoreCase(it.next())) return true;
        return false;
    }

    private boolean canManageGroupMembers(String groupName) {
        return isGroupOwner(groupName, ownNick()) || isGroupAdmin(groupName, ownNick());
    }

    private String groupRoleText(String groupName, String user) {
        if(isGroupOwner(groupName, user)) return "群主";
        if(isGroupAdmin(groupName, user)) return "管理员";
        return "成员";
    }

    private void broadcastGroupRoles(String groupName) {
        Set<String> members = chatGroups.get(groupName);
        if(members == null) return;
        ensureGroupRoleDefaults(groupName);
        String payload = GROUP_ROLE_PREFIX + encodeToken(groupName) + "|" + encodeToken(nullSafe(groupOwners.get(groupName)))
                + "|" + encodeToken(membersToCsv(groupAdmins.get(groupName)));
        Iterator<String> it = members.iterator();
        while(it.hasNext()) {
            String member = it.next();
            if(member.equalsIgnoreCase(ownNick())) continue;
            if(ck != null && ck.isConnected() && visibleUsers.contains(member)) ck.sendMessage("/msg " + member + " " + payload);
            else queuePendingDelivery(member, payload, "群权限同步：" + groupName);
        }
    }

    private void transferGroupOwner(String groupName) {
        if(!isGroupOwner(groupName, ownNick())) { showInlineNotice("只有群主可以转让群主。", WARNING); return; }
        Set<String> members = chatGroups.get(groupName);
        if(members == null) return;
        java.util.List<String> candidates = new ArrayList<String>(members);
        candidates.remove(ownNick());
        candidates.remove(groupOwners.get(groupName));
        if(!isGroupOwner(groupName, ownNick())) {
            Set<String> admins = groupAdmins.get(groupName);
            if(admins != null) candidates.removeAll(admins);
        }
        Collections.sort(candidates, String.CASE_INSENSITIVE_ORDER);
        if(candidates.size() == 0) { showInlineNotice("暂无可转让的成员。", MUTED); return; }
        String nextOwner = (String)JOptionPane.showInputDialog(this, "选择新的群主", "转让群主", JOptionPane.PLAIN_MESSAGE, null, candidates.toArray(), candidates.get(0));
        if(nextOwner == null || nextOwner.length() == 0) return;
        groupOwners.put(groupName, nextOwner);
        Set<String> admins = groupAdmins.get(groupName);
        if(admins == null) admins = new LinkedHashSet<String>();
        admins.remove(nextOwner);
        groupAdmins.put(groupName, admins);
        saveGroups();
        broadcastGroupRoles(groupName);
        rebuildConversationList(groupLabel(groupName));
        showInlineNotice("已将群主转让给 " + nextOwner + "。", PRIMARY_DARK);
    }

    private void manageGroupAdmins(String groupName) {
        if(!isGroupOwner(groupName, ownNick())) { showInlineNotice("只有群主可以设置管理员。", WARNING); return; }
        Set<String> members = chatGroups.get(groupName);
        if(members == null) return;
        java.util.List<String> candidates = new ArrayList<String>(members);
        candidates.remove(ownNick());
        candidates.remove(groupOwners.get(groupName));
        Collections.sort(candidates, String.CASE_INSENSITIVE_ORDER);
        if(candidates.size() == 0) { showInlineNotice("暂无可设置的管理员。", MUTED); return; }
        final JList<String> list = new JList<String>(new Vector<String>(candidates));
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        Set<String> admins = groupAdmins.get(groupName);
        if(admins != null) {
            java.util.List<Integer> selected = new ArrayList<Integer>();
            for(int i=0;i<candidates.size();i++) if(admins.contains(candidates.get(i))) selected.add(Integer.valueOf(i));
            int[] indices = new int[selected.size()];
            for(int i=0;i<selected.size();i++) indices[i] = selected.get(i).intValue();
            list.setSelectedIndices(indices);
        }
        int option = JOptionPane.showConfirmDialog(this, new JScrollPane(list), "设置管理员", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if(option != JOptionPane.OK_OPTION) return;
        groupAdmins.put(groupName, new LinkedHashSet<String>(list.getSelectedValuesList()));
        saveGroups();
        broadcastGroupRoles(groupName);
        showInlineNotice("群管理员已更新。", PRIMARY_DARK);
    }

    private void showCreateGroupDialog() {
        java.util.List<String> candidates = new ArrayList<String>();
        Iterator<String> friendIt = friends.iterator();
        while(friendIt.hasNext()) {
            String friend = friendIt.next();
            candidates.add(friend);
        }
        if(candidates.size() < 2) {
            showInlineNotice("至少需要 2 个好友才能创建群聊。", WARNING);
            return;
        }
        Collections.sort(candidates, String.CASE_INSENSITIVE_ORDER);

        final JDialog dialog = new JDialog(this, "创建群聊", true);
        dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        dialog.setSize(430, 520);
        dialog.setMinimumSize(new Dimension(390, 460));
        dialog.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(APP_BACKGROUND);
        root.setBorder(pad(SPACE_XL));
        BubblePanel card = new BubblePanel(SURFACE, BORDER_LIGHT, RADIUS_XL);
        card.setLayout(new BorderLayout(0, SPACE_MD));
        card.setBorder(pad(SPACE_LG));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("创建群聊");
        title.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 22));
        title.setForeground(TEXT);
        JLabel detail = new JLabel("选择至少 2 位好友，离线成员上线后会收到群聊邀请。");
        detail.setFont(UI_FONT_SMALL);
        detail.setForeground(MUTED);
        header.add(title);
        header.add(Box.createVerticalStrut(SPACE_XS));
        header.add(detail);

        final JTextField groupNameField = createTextField("", 18);
        JPanel nameBlock = createStackedBlock("群名称", groupNameField);

        final JList<String> friendList = new JList<String>(new Vector<String>(candidates));
        friendList.setFont(UI_FONT);
        friendList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        friendList.setVisibleRowCount(Math.min(9, Math.max(5, candidates.size())));
        final JLabel selectionLabel = createHintLabel("已选择 0 人");
        JScrollPane friendScroll = createModernScrollPane(friendList, Color.WHITE);
        friendScroll.setPreferredSize(new Dimension(0, 210));
        JPanel memberBlock = new JPanel(new BorderLayout(0, SPACE_SM));
        memberBlock.setOpaque(false);
        JLabel memberTitle = createSectionTitle("好友");
        memberBlock.add(memberTitle, BorderLayout.NORTH);
        memberBlock.add(friendScroll, BorderLayout.CENTER);
        memberBlock.add(selectionLabel, BorderLayout.SOUTH);

        final JLabel status = createHintLabel(" ");
        status.setForeground(DANGER);
        final JButton cancel = createButton("取消", false);
        final JButton create = createButton("创建", true);
        cancel.setPreferredSize(new Dimension(76, 38));
        create.setPreferredSize(new Dimension(88, 38));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, SPACE_SM, 0));
        buttons.setOpaque(false);
        buttons.add(cancel);
        buttons.add(create);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(nameBlock);
        center.add(Box.createVerticalStrut(SPACE_MD));
        center.add(memberBlock);
        center.add(Box.createVerticalStrut(SPACE_SM));
        center.add(status);

        card.add(header, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);
        root.add(card, BorderLayout.CENTER);
        dialog.setContentPane(root);

        friendList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                int count = friendList.getSelectedValuesList().size();
                selectionLabel.setText("已选择 " + count + " 人");
                selectionLabel.setForeground(count >= 2 ? PRIMARY_DARK : MUTED);
            }
        });
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        create.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String groupName = groupNameField.getText().trim();
                java.util.List<String> selectedFriends = friendList.getSelectedValuesList();
                if(groupName.length() == 0) {
                    status.setText("请输入群名称。");
                    return;
                }
                if(groupName.equals(BROADCAST_CHAT) || groupName.startsWith(GROUP_LABEL_PREFIX)
                        || groupName.indexOf('|') >= 0 || groupName.indexOf('\t') >= 0) {
                    status.setText("群名称不能使用系统保留名称或特殊字符。");
                    return;
                }
                if(selectedFriends.size() < 2) {
                    status.setText("请至少选择 2 个好友。");
                    return;
                }
                Set<String> members = new LinkedHashSet<String>();
                members.add(ownNick());
                members.addAll(selectedFriends);
                groupOwners.put(groupName, ownNick());
                groupAdmins.put(groupName, new LinkedHashSet<String>());
                addOrUpdateGroup(groupName, members, true);
                sendGroupInvite(groupName, members);
                rebuildConversationList(groupLabel(groupName));
                addMsg("<font color=\"#3366cc\">已创建群聊：" + escapeHtml(groupName)
                        + "，成员 " + members.size() + " 人。</font>");
                dialog.dispose();
            }
        });
        dialog.getRootPane().setDefaultButton(create);
        dialog.getRootPane().registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.setVisible(true);
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
        ensureGroupRoleDefaults(groupName);
        if(save) saveGroups();
    }

    private void sendGroupInvite(String groupName, Set<String> members) {
        ensureGroupRoleDefaults(groupName);
        String payload = GROUP_INVITE_PREFIX + encodeToken(groupName) + "|"
                + encodeToken(membersToCsv(members)) + "|"
                + encodeToken(nullSafe(groupOwners.get(groupName))) + "|"
                + encodeToken(membersToCsv(groupAdmins.get(groupName)));
        Iterator<String> it = members.iterator();
        while(it.hasNext()) {
            String member = it.next();
            if(member.equalsIgnoreCase(ownNick())) continue;
            if(ck != null && ck.isConnected() && visibleUsers.contains(member)) {
                ck.sendMessage("/msg " + member + " " + payload);
            } else {
                queuePendingDelivery(member, payload, "群聊邀请：" + groupName);
            }
        }
        if(ck == null || !ck.isConnected()) {
            showInlineNotice("群聊已创建，邀请会在连接服务器后自动发送。", WARNING);
        }
    }

    private void showGroupInfoDialog(final String groupName) {
        if(groupName == null || groupName.length() == 0) return;
        final JDialog dialog = new JDialog(this, "群聊资料", true);
        dialog.setSize(440, 560);
        dialog.setLocationRelativeTo(this);
        JPanel root = new JPanel(new BorderLayout(SPACE_MD, SPACE_MD));
        root.setBackground(CHAT_BACKGROUND);
        root.setBorder(pad(SPACE_LG));
        JLabel title = new JLabel(groupName);
        title.setFont(PAGE_TITLE_FONT);
        title.setForeground(TEXT);
        root.add(title, BorderLayout.NORTH);
        JTextArea membersText = new JTextArea(membersToDisplayText(chatGroups.get(groupName)));
        membersText.setEditable(false);
        membersText.setFont(UI_FONT);
        membersText.setBackground(SURFACE);
        membersText.setBorder(pad(SPACE_MD));
        root.add(new JScrollPane(membersText), BorderLayout.CENTER);
        JPanel buttons = new JPanel(new GridLayout(3, 2, SPACE_SM, SPACE_SM));
        buttons.setOpaque(false);
        JButton invite = createButton("拉人进群", false);
        JButton kick = createButton("踢人出群", false);
        JButton admin = createButton("设置管理员", false);
        JButton transfer = createButton("转让群主", false);
        JButton file = createButton("发送群文件", false);
        JButton leave = createButton("退出群聊", false);
        boolean manage = canManageGroupMembers(groupName);
        invite.setEnabled(manage);
        kick.setEnabled(manage);
        admin.setEnabled(isGroupOwner(groupName, ownNick()));
        transfer.setEnabled(isGroupOwner(groupName, ownNick()));
        invite.addActionListener(new ActionListener(){ public void actionPerformed(ActionEvent e){ inviteMembersToGroup(groupName); membersText.setText(membersToDisplayText(chatGroups.get(groupName))); }});
        kick.addActionListener(new ActionListener(){ public void actionPerformed(ActionEvent e){ kickMemberFromGroup(groupName); membersText.setText(membersToDisplayText(chatGroups.get(groupName))); }});
        admin.addActionListener(new ActionListener(){ public void actionPerformed(ActionEvent e){ manageGroupAdmins(groupName); membersText.setText(membersToDisplayText(chatGroups.get(groupName))); }});
        transfer.addActionListener(new ActionListener(){ public void actionPerformed(ActionEvent e){ transferGroupOwner(groupName); membersText.setText(membersToDisplayText(chatGroups.get(groupName))); }});
        file.addActionListener(new ActionListener(){ public void actionPerformed(ActionEvent e){ dialog.dispose(); selectConversation(groupLabel(groupName)); chooseAndSendFile(); }});
        leave.addActionListener(new ActionListener(){ public void actionPerformed(ActionEvent e){ dialog.dispose(); leaveGroup(groupName); }});
        buttons.add(invite); buttons.add(kick); buttons.add(admin); buttons.add(transfer); buttons.add(file); buttons.add(leave);
        root.add(buttons, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private void showGroupMembersDialog(String groupName) {
        JOptionPane.showMessageDialog(this, membersToDisplayText(chatGroups.get(groupName)), "群成员", JOptionPane.INFORMATION_MESSAGE);
    }

    private String membersToDisplayText(Set<String> members) {
        if(members == null || members.size() == 0) return "暂无成员";
        StringBuilder sb = new StringBuilder();
        java.util.List<String> list = new ArrayList<String>(members);
        Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
        String groupName = null;
        Iterator<Map.Entry<String, Set<String>>> groupIt = chatGroups.entrySet().iterator();
        while(groupIt.hasNext()) {
            Map.Entry<String, Set<String>> e = groupIt.next();
            if(e.getValue() == members) { groupName = e.getKey(); break; }
        }
        for(int i=0;i<list.size();i++) {
            String member = list.get(i);
            String role = groupName == null ? "成员" : groupRoleText(groupName, member);
            sb.append(member).append("（").append(role).append("）")
                    .append(visibleUsers.contains(member) ? "（在线）" : "").append('\n');
        }
        return sb.toString();
    }

    private void inviteMembersToGroup(String groupName) {
        Set<String> members = chatGroups.get(groupName);
        if(members == null) return;
        if(!canManageGroupMembers(groupName)) { showInlineNotice("只有群主或管理员可以拉人进群。", WARNING); return; }
        java.util.List<String> candidates = new ArrayList<String>();
        Set<String> all = new LinkedHashSet<String>();
        all.addAll(friends);
        all.addAll(visibleUsers);
        Iterator<String> it = all.iterator();
        while(it.hasNext()) {
            String user = it.next();
            if(user.equalsIgnoreCase(ownNick()) || members.contains(user)) continue;
            candidates.add(user);
        }
        Collections.sort(candidates, String.CASE_INSENSITIVE_ORDER);
        if(candidates.size() == 0) { showInlineNotice("没有可拉入的新成员。", MUTED); return; }
        JList<String> list = new JList<String>(new Vector<String>(candidates));
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setVisibleRowCount(Math.min(8, candidates.size()));
        int option = JOptionPane.showConfirmDialog(this, new JScrollPane(list), "选择拉入群聊的成员", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if(option != JOptionPane.OK_OPTION) return;
        java.util.List<String> selected = list.getSelectedValuesList();
        if(selected.size() == 0) return;
        members.addAll(selected);
        saveGroups();
        sendGroupInvite(groupName, members);
        rebuildConversationList(groupLabel(groupName));
        showInlineNotice("已拉入 " + selected.size() + " 人。", PRIMARY_DARK);
    }

    private void kickMemberFromGroup(String groupName) {
        Set<String> members = chatGroups.get(groupName);
        if(members == null || members.size() <= 1) return;
        if(!canManageGroupMembers(groupName)) { showInlineNotice("只有群主或管理员可以踢人出群。", WARNING); return; }
        java.util.List<String> candidates = new ArrayList<String>(members);
        candidates.remove(ownNick());
        candidates.remove(groupOwners.get(groupName));
        if(!isGroupOwner(groupName, ownNick())) {
            Set<String> admins = groupAdmins.get(groupName);
            if(admins != null) candidates.removeAll(admins);
        }
        Collections.sort(candidates, String.CASE_INSENSITIVE_ORDER);
        if(candidates.size() == 0) { showInlineNotice("没有可踢出的成员。", MUTED); return; }
        String kicked = (String)JOptionPane.showInputDialog(this, "选择要踢出的成员", "踢人出群",
                JOptionPane.PLAIN_MESSAGE, null, candidates.toArray(), candidates.get(0));
        if(kicked == null || kicked.length() == 0) return;
        members.remove(kicked);
        Set<String> adminsAfterKick = groupAdmins.get(groupName);
        if(adminsAfterKick != null) adminsAfterKick.remove(kicked);
        saveGroups();
        broadcastGroupRoles(groupName);
        if(ck != null && ck.isConnected()) ck.sendMessage("/msg " + kicked + " " + GROUP_KICK_PREFIX + encodeToken(groupName));
        else queuePendingDelivery(kicked, GROUP_KICK_PREFIX + encodeToken(groupName), "踢出群聊：" + groupName);
        sendGroupInvite(groupName, members);
        rebuildConversationList(groupLabel(groupName));
        showInlineNotice("已将 " + kicked + " 移出群聊。", WARNING);
    }

    private void leaveGroup(String groupName) {
        if(groupName == null || !chatGroups.containsKey(groupName)) return;
        int choice = JOptionPane.showConfirmDialog(this, "确定退出群聊 “" + groupName + "” 吗？", "退出群聊", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if(choice != JOptionPane.YES_OPTION) return;
        Set<String> members = chatGroups.get(groupName);
        chatGroups.remove(groupName);
        groupOwners.remove(groupName);
        groupAdmins.remove(groupName);
        saveGroups();
        if(members != null) {
            String payload = GROUP_LEAVE_PREFIX + encodeToken(groupName) + "|" + encodeToken(ownNick());
            Iterator<String> it = members.iterator();
            while(it.hasNext()) {
                String member = it.next();
                if(member.equalsIgnoreCase(ownNick())) continue;
                if(ck != null && ck.isConnected() && visibleUsers.contains(member)) ck.sendMessage("/msg " + member + " " + payload);
                else queuePendingDelivery(member, payload, "退出群聊：" + groupName);
            }
        }
        rebuildConversationList(BROADCAST_CHAT);
        showInlineNotice("已退出群聊：" + groupName, WARNING);
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
            java.util.List<String> readable = new ArrayList<String>();
            for(int i=0;i<lines.size();i++) {
                String display = readableHistoryLine(lines.get(i));
                if(display != null && display.length() > 0) readable.add(display);
            }
            if(readable.size() == 0) return;
            int start = Math.max(0, readable.size() - 30);
            historyWindow.addMessage(new ChatMessage(MessageKind.SYSTEM, "系统", "已加载最近聊天记录", ""));
            for(int i=start;i<readable.size();i++) {
                historyWindow.addMessage(new ChatMessage(MessageKind.SYSTEM, "历史", readable.get(i), ""));
            }
        } catch(Exception e) {
            historyWindow.addMessage(new ChatMessage(MessageKind.SYSTEM, "系统", "加载聊天记录失败：" + e.getMessage(), ""));
        }
    }

    private String readableHistoryLine(String line) {
        String text = stripLogTimestamp(stripHtml(line)).trim();
        if(text.length() == 0 || shouldSkipLogLine(text)) return null;
        if(text.startsWith("[connection")) return null;
        if(text.startsWith("[offline delivery to ")) return null;
        if(text.startsWith("Unable to find user ")) return null;

        if(text.startsWith("[private to ")) {
            int end = text.indexOf(']');
            if(end > 0) {
                String target = text.substring("[private to ".length(), end).trim();
                return clipHistoryText("我发给 " + target + "：" + text.substring(end + 1).trim());
            }
        }
        if(text.startsWith("[private delivered from queue to ")) {
            int end = text.indexOf(']');
            if(end > 0) {
                String target = text.substring("[private delivered from queue to ".length(), end).trim();
                return clipHistoryText("已补发给 " + target + "：" + text.substring(end + 1).trim());
            }
        }
        if(text.startsWith("[private] ")) {
            String payload = text.substring("[private] ".length()).trim();
            int colon = payload.indexOf(':');
            if(colon > 0) {
                return clipHistoryText(payload.substring(0, colon).trim()
                        + "：" + payload.substring(colon + 1).trim());
            }
        }
        if(text.startsWith("[broadcast] ")) {
            return clipHistoryText("广播：" + text.substring("[broadcast] ".length()).trim());
        }
        if(text.startsWith("[group ")) {
            int end = text.indexOf(']');
            if(end > 0) return clipHistoryText("群聊 " + text.substring("[group ".length(), end).trim()
                    + "：" + text.substring(end + 1).trim());
        }
        if(text.startsWith("[file to ")) {
            int end = text.indexOf(']');
            if(end > 0) return clipHistoryText("发送文件给 "
                    + text.substring("[file to ".length(), end).trim()
                    + "：" + text.substring(end + 1).trim());
        }
        if(text.startsWith("[file from ")) {
            int end = text.indexOf(']');
            if(end > 0) return clipHistoryText("收到文件 "
                    + text.substring("[file from ".length(), end).trim()
                    + "：" + text.substring(end + 1).trim());
        }
        if(isSystemMessage(text)) return null;
        return clipHistoryText(text);
    }

    private String stripLogTimestamp(String line) {
        if(line == null) return "";
        if(line.length() > 22 && line.charAt(0) == '[' && line.charAt(20) == ']') {
            return line.substring(22);
        }
        return line;
    }

    private String clipHistoryText(String text) {
        if(text == null) return "";
        String normalized = text.replace('\n', ' ').replace('\r', ' ').trim();
        if(normalized.length() <= 120) return normalized;
        return normalized.substring(0, 119) + "...";
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
        final JTextField displayNameField = new JTextField(currentProfile.getProperty("displayName", currentUser), 18);
        final JTextArea signatureArea = new JTextArea(currentProfile.getProperty("signature", ""), 4, 18);
        signatureArea.setLineWrap(true);
        signatureArea.setWrapStyleWord(true);

        final AvatarView previewAvatar = new AvatarView(currentUser, false);
        previewAvatar.setAvatarSize(72);
        JButton chooseAvatar = createButton("选择头像", false);
        JButton resetAvatar = createButton("恢复字母头像", false);
        chooseAvatar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                chooseLocalAvatar(previewAvatar);
            }
        });
        resetAvatar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentProfile.remove("avatarPath");
                currentProfile.setProperty("avatarMode", "letter");
                previewAvatar.repaint();
            }
        });

        JPanel avatarRow = new JPanel(new BorderLayout(SPACE_MD, 0));
        avatarRow.setOpaque(false);
        JPanel avatarButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, SPACE_SM, 0));
        avatarButtons.setOpaque(false);
        avatarButtons.add(chooseAvatar);
        avatarButtons.add(resetAvatar);
        avatarRow.add(previewAvatar, BorderLayout.WEST);
        avatarRow.add(avatarButtons, BorderLayout.CENTER);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel rows = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        addLoginRow(rows, gbc, 0, "账号", new JLabel(currentUser));
        addLoginRow(rows, gbc, 1, "头像", avatarRow);
        addLoginRow(rows, gbc, 2, "显示名", displayNameField);
        addLoginRow(rows, gbc, 3, "签名", new JScrollPane(signatureArea));
        panel.add(rows, BorderLayout.CENTER);

        int option = JOptionPane.showConfirmDialog(this, panel, "个人信息",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if(option == JOptionPane.OK_OPTION) {
            currentProfile.setProperty("displayName", displayNameField.getText().trim());
            currentProfile.setProperty("signature", signatureArea.getText().trim());
            saveProfile();
            refreshSidebarProfile();
            if(conversationAvatar != null) conversationAvatar.repaint();
            if(onlineList != null) onlineList.repaint();
            showInlineNotice("个人信息已保存。", PRIMARY_DARK);
        }
    }

    private void chooseLocalAvatar(AvatarView previewAvatar) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("选择头像图片");
        chooser.setCurrentDirectory(new File(System.getProperty("user.home"), "Pictures"));
        chooser.setFileFilter(new FileNameExtensionFilter("图片文件", "png", "jpg", "jpeg", "gif", "bmp", "webp"));
        if(chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File selected = chooser.getSelectedFile();
        if(selected == null || !selected.isFile()) return;
        try {
            BufferedImage image = ImageIO.read(selected);
            if(image == null) {
                JOptionPane.showMessageDialog(this, "请选择有效的图片文件。", "头像", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Path avatarDir = currentUserDir.resolve("avatar");
            Files.createDirectories(avatarDir);
            String ext = fileExtension(selected.getName());
            if(ext.length() == 0) ext = ".png";
            Path target = avatarDir.resolve("avatar" + ext.toLowerCase(Locale.ROOT));
            Files.copy(selected.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            currentProfile.setProperty("avatarPath", target.toString());
            currentProfile.setProperty("avatarMode", "image");
            if(previewAvatar != null) previewAvatar.repaint();
            refreshSidebarProfile();
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(this, "头像保存失败：" + ex.getMessage(), "头像", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String fileExtension(String name) {
        if(name == null) return "";
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot) : "";
    }

    private void showUserHomeDialog(final String username) {
        if(username == null || username.length() == 0) return;
        final boolean isSelf = username.equalsIgnoreCase(currentUser);
        final boolean isFriend = friends.contains(username);
        if(isFriend && !isSelf) {
            requestServerMoments();
            if(ck != null && ck.isConnected() && visibleUsers.contains(username)) {
                ck.sendMessage("/msg " + username + " " + MOMENT_SYNC_REQUEST_PREFIX + ownNick());
            }
        }
        Properties profile = loadProfileForUser(username);
        String displayName = profile.getProperty("displayName", username);
        if(isFriend && !isSelf && friendRemarks.get(username) != null && friendRemarks.get(username).trim().length() > 0) {
            displayName = friendRemarks.get(username).trim();
        }
        String signature = profile.getProperty("signature", "这个人还没有写个性签名。");

        final JDialog dialog = new JDialog(this, username + " 的主页", true);
        dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        dialog.setSize(390, 640);
        dialog.setMinimumSize(new Dimension(350, 520));
        dialog.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CHAT_BACKGROUND);
        root.setBorder(pad(SPACE_LG));
        BubblePanel card = createCardPanel(SPACE_LG);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JPanel header = new JPanel(new BorderLayout(SPACE_MD, 0));
        header.setOpaque(false);
        AvatarView avatar = new AvatarView(username, false);
        avatar.setAvatarSize(72);
        header.add(avatar, BorderLayout.WEST);
        JPanel headerText = new JPanel();
        headerText.setOpaque(false);
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        JLabel name = new JLabel(displayName + (isSelf ? "（我）" : ""));
        name.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 18));
        name.setForeground(TEXT);
        JLabel account = new JLabel("账号：" + username);
        account.setFont(UI_FONT_SMALL);
        account.setForeground(MUTED);
        JLabel relation = new JLabel(isSelf ? "自己的主页" : (isFriend ? "好友" : "非好友"));
        relation.setFont(UI_FONT_SMALL);
        relation.setForeground(isFriend || isSelf ? PRIMARY_DARK : SOFT_MUTED);
        headerText.add(name);
        headerText.add(Box.createVerticalStrut(SPACE_XS));
        headerText.add(account);
        headerText.add(Box.createVerticalStrut(SPACE_XS));
        headerText.add(relation);
        header.add(headerText, BorderLayout.CENTER);
        card.add(header);
        card.add(sectionDivider());
        card.add(profileInfoRow("个性签名", signature.length() == 0 ? "这个人还没有写个性签名。" : signature));
        if(isFriend && !isSelf) {
            card.add(profileInfoRow("好友备注", friendRemarks.get(username) == null || friendRemarks.get(username).trim().length() == 0 ? "未设置" : friendRemarks.get(username).trim()));
        }
        if(isFriend && !isSelf) {
            card.add(profileInfoRow("共同好友", commonFriendsCount(username) + " 个"));
            card.add(profileInfoRow("共同群聊", commonGroupsCount(username) + " 个"));
        }
        card.add(sectionDivider());
        JLabel momentsTitle = new JLabel("朋友圈");
        momentsTitle.setFont(UI_FONT_BOLD);
        momentsTitle.setForeground(MUTED);
        momentsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(momentsTitle);
        card.add(Box.createVerticalStrut(SPACE_SM));
        JPanel momentsPreview = createUserMomentsPreview(username);
        momentsPreview.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(momentsPreview);
        card.add(Box.createVerticalGlue());
        card.add(sectionDivider());
        JPanel actions = new JPanel(new GridLayout(1, isSelf ? 1 : (isFriend ? 3 : 2), SPACE_SM, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        if(isSelf) {
            JButton edit = createButton("编辑资料", false);
            edit.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { dialog.dispose(); showProfileDialog(); }
            });
            actions.add(edit);
        } else {
            JButton msg = createButton("发消息", true);
            msg.setEnabled(isFriend);
            msg.setToolTipText(isFriend ? "进入聊天" : "非好友不能发送消息");
            msg.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { dialog.dispose(); selectConversation(username); }
            });
            if(isFriend) {
                JButton remarkButton = createButton("设置备注", false);
                remarkButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        dialog.dispose();
                        setFriendRemark(username);
                    }
                });
                actions.add(remarkButton);
            }
            JButton relationButton = createButton(isFriend ? "删除好友" : "添加好友", false);
            relationButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dialog.dispose();
                    if(isFriend) deleteFriend(username);
                    else requestFriend(username);
                }
            });
            actions.add(msg);
            actions.add(relationButton);
        }
        card.add(actions);
        root.add(card, BorderLayout.CENTER);
        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private JPanel profileInfoRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(SPACE_MD, 0));
        row.setOpaque(false);
        row.setBorder(pad(SPACE_XS, 0, SPACE_XS, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel left = new JLabel(label);
        left.setFont(UI_FONT_BOLD);
        left.setForeground(MUTED);
        left.setPreferredSize(new Dimension(84, 24));
        JLabel right = new JLabel("<html><div style=\"width:210px;\">" + htmlText(value) + "</div></html>");
        right.setFont(UI_FONT);
        right.setForeground(TEXT);
        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.CENTER);
        return row;
    }

    private Component sectionDivider() {
        JSeparator separator = new JSeparator();
        separator.setForeground(BORDER_LIGHT);
        separator.setBackground(BORDER_LIGHT);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);
        return separator;
    }

    private JPanel createUserMomentsPreview(final String username) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(pad(SPACE_SM, 0, SPACE_SM, 0));
        final java.util.List<Moment> moments = visibleMomentsForAuthor(username);
        if(moments.size() == 0) {
            JLabel empty = new JLabel("暂无可查看的朋友圈动态");
            empty.setFont(UI_FONT);
            empty.setForeground(SOFT_MUTED);
            panel.add(empty, BorderLayout.CENTER);
            JLabel tip = new JLabel("双击查看全部朋友圈");
            tip.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
            tip.setForeground(MUTED);
            panel.add(tip, BorderLayout.SOUTH);
            panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            panel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if(e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) showUserMomentsDialog(username);
                }
            });
            return panel;
        }
        JPanel preview = new JPanel(new GridLayout(1, Math.min(3, moments.size()), SPACE_SM, 0));
        preview.setOpaque(false);
        int count = Math.min(3, moments.size());
        for(int i=0;i<count;i++) {
            Moment moment = moments.get(i);
            BubblePanel item = new BubblePanel(SURFACE_SOFT, BORDER_LIGHT, RADIUS_MD);
            item.setLayout(new BorderLayout());
            item.setBorder(pad(SPACE_SM));
            JLabel text = new JLabel("<html><div style=\"width:76px;height:48px;\">" + htmlText(clipPreview(moment.text, 18)) + "</div></html>");
            text.setFont(UI_FONT_SMALL);
            text.setForeground(TEXT);
            item.add(text, BorderLayout.CENTER);
            preview.add(item);
        }
        panel.add(preview, BorderLayout.CENTER);
        JLabel tip = new JLabel("双击查看全部朋友圈");
        tip.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
        tip.setForeground(MUTED);
        panel.add(tip, BorderLayout.SOUTH);
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) showUserMomentsDialog(username);
            }
        });
        return panel;
    }

    private void showUserMomentsDialog(final String username) {
        final JDialog dialog = new JDialog(this, username + " 的朋友圈", true);
        dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        dialog.setSize(560, 640);
        dialog.setMinimumSize(new Dimension(500, 520));
        dialog.setLocationRelativeTo(this);
        final JPanel feed = new JPanel();
        feed.setBackground(CHAT_BACKGROUND);
        feed.setLayout(new BoxLayout(feed, BoxLayout.Y_AXIS));
        Runnable refresh = new Runnable() {
            public void run() {
                feed.removeAll();
                java.util.List<Moment> moments = visibleMomentsForAuthor(username);
                if(moments.size() == 0) {
                    feed.add(createEmptyState("暂无可查看的朋友圈动态", "如果是非好友，只会显示对方设置为“所有人可见”的动态"));
                } else {
                    for(int i=0;i<moments.size();i++) {
                        feed.add(createUserMomentCard(moments.get(i), feed, username));
                        feed.add(Box.createVerticalStrut(SPACE_MD));
                    }
                }
                feed.revalidate();
                feed.repaint();
            }
        };
        JPanel root = new JPanel(new BorderLayout(SPACE_MD, SPACE_MD));
        root.setBackground(CHAT_BACKGROUND);
        root.setBorder(pad(SPACE_LG));
        JLabel title = new JLabel(username + " 的朋友圈");
        title.setFont(PAGE_TITLE_FONT);
        title.setForeground(TEXT);
        root.add(title, BorderLayout.NORTH);
        root.add(createModernScrollPane(feed, CHAT_BACKGROUND), BorderLayout.CENTER);
        dialog.setContentPane(root);
        refresh.run();
        dialog.setVisible(true);
    }

    private Component createUserMomentCard(final Moment moment, final JPanel feed, final String username) {
        BubblePanel card = createCardPanel(SPACE_MD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel time = new JLabel(moment.time + " · " + visibilityLabel(moment.visibility));
        time.setFont(UI_FONT_SMALL);
        time.setForeground(MUTED);
        time.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel text = new JLabel("<html><div style=\"width:450px;text-align:left;font-size:14px;line-height:1.5;\">" + htmlText(moment.text) + "</div></html>");
        text.setFont(UI_FONT);
        text.setForeground(TEXT);
        text.setHorizontalAlignment(SwingConstants.LEFT);
        text.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.setMaximumSize(new Dimension(Integer.MAX_VALUE, text.getPreferredSize().height));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, SPACE_SM, 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        boolean liked = moment.likes.contains(currentUser);
        JButton like = createButton(liked ? "取消点赞" : "点赞", false);
        JButton comment = createButton("评论", false);
        like.setPreferredSize(new Dimension(liked ? 86 : 58, 30));
        comment.setPreferredSize(new Dimension(58, 30));
        like.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { toggleMomentLike(moment.id); refreshUserMomentFeed(feed, username); }
        });
        comment.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { commentMomentFromDialog(moment, feed, username); }
        });
        buttons.add(like);
        buttons.add(comment);
        if(moment.author.equalsIgnoreCase(currentUser)) {
            JButton permission = createButton("权限", false);
            permission.setPreferredSize(new Dimension(58, 30));
            permission.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JComboBox<String> box = new JComboBox<String>(new String[]{"仅好友可见", "所有人可见", "仅自己可见"});
                    box.setSelectedItem(visibilityLabel(moment.visibility));
                    int option = JOptionPane.showConfirmDialog(ChatClient.this, box, "修改这条朋友圈权限", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if(option == JOptionPane.OK_OPTION) {
                        updateMomentVisibility(moment.id, visibilityFromLabel((String)box.getSelectedItem()));
                        refreshUserMomentFeed(feed, username);
                    }
                }
            });
            JButton delete = createButton("删除", false);
            delete.setPreferredSize(new Dimension(58, 30));
            delete.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int choice = JOptionPane.showConfirmDialog(ChatClient.this, "确定删除这条动态吗？", "删除动态", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if(choice == JOptionPane.YES_OPTION) { deleteMoment(moment.id); refreshUserMomentFeed(feed, username); }
                }
            });
            buttons.add(permission);
            buttons.add(delete);
        }
        card.add(time);
        card.add(Box.createVerticalStrut(SPACE_SM));
        card.add(text);
        if(moment.imageData != null) {
            card.add(Box.createVerticalStrut(SPACE_SM));
            Component image = createMomentImagePreview(moment, 360, 220);
            if(image instanceof JComponent) ((JComponent) image).setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(image);
        }
        card.add(Box.createVerticalStrut(SPACE_SM));
        buttons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        card.add(buttons);
        if(moment.likes.size() > 0 || moment.comments.size() > 0) {
            card.add(Box.createVerticalStrut(SPACE_XS));
            Component social = createMomentSocialSummary(moment, feed, username);
            if(social instanceof JComponent) ((JComponent) social).setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(social);
        }
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        return card;
    }

    private void refreshUserMomentFeed(JPanel feed, String username) {
        if(feed == null) return;
        feed.removeAll();
        java.util.List<Moment> moments = visibleMomentsForAuthor(username);
        for(int i=0;i<moments.size();i++) {
            feed.add(createUserMomentCard(moments.get(i), feed, username));
            feed.add(Box.createVerticalStrut(SPACE_MD));
        }
        if(moments.size() == 0) feed.add(createEmptyState("暂无可查看的朋友圈动态", "如果是非好友，只会显示对方设置为“所有人可见”的动态"));
        feed.revalidate();
        feed.repaint();
    }

    private Component createMomentSocialSummary(final Moment moment, final JPanel feed, final String username) {
        BubblePanel panel = new BubblePanel(SURFACE_SOFT, BORDER_LIGHT, RADIUS_MD);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(pad(SPACE_SM));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        if(moment.likes.size() > 0) {
            JLabel likes = new JLabel("点赞 " + moment.likes.size() + " · " + membersToCsv(moment.likes));
            likes.setFont(UI_FONT_SMALL);
            likes.setForeground(PRIMARY_DARK);
            likes.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(likes);
        }
        for(int i=0;i<moment.comments.size();i++) {
            final MomentComment c = moment.comments.get(i);
            JLabel line = new JLabel("<html><div style=\"width:430px;\"><b>" + escapeHtml(c.author) + "</b>"
                    + (c.replyTo == null || c.replyTo.length() == 0 ? "" : " 回复 <b>" + escapeHtml(c.replyTo) + "</b>")
                    + "：" + htmlText(c.text) + "</div></html>");
            line.setFont(UI_FONT_SMALL);
            line.setForeground(TEXT);
            line.setHorizontalAlignment(SwingConstants.LEFT);
            line.setAlignmentX(Component.LEFT_ALIGNMENT);
            if(moment.author.equalsIgnoreCase(currentUser)) {
                line.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                line.setToolTipText("点击回复该评论");
                line.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        replyMomentCommentFromDialog(moment, c, feed, username);
                    }
                });
            }
            if(i > 0 || moment.likes.size() > 0) panel.add(Box.createVerticalStrut(4));
            panel.add(line);
        }
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
        return panel;
    }

    private void replyMomentCommentFromDialog(Moment moment, MomentComment target, JPanel feed, String username) {
        JTextArea area = new JTextArea(4, 24);
        area.setFont(UI_FONT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(pad(SPACE_SM));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(new RoundedBorder(BORDER, RADIUS_MD));
        int option = JOptionPane.showConfirmDialog(this, scroll, "回复 " + target.author, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if(option != JOptionPane.OK_OPTION) return;
        String text = area.getText().trim();
        if(text.length() == 0) return;
        addMomentComment(moment.id, text, target.author);
        refreshUserMomentFeed(feed, username);
    }

    private void commentMomentFromDialog(Moment moment, JPanel feed, String username) {
        JTextArea area = new JTextArea(4, 24);
        area.setFont(UI_FONT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(pad(SPACE_SM));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(new RoundedBorder(BORDER, RADIUS_MD));
        int option = JOptionPane.showConfirmDialog(this, scroll, "评论", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if(option != JOptionPane.OK_OPTION) return;
        String text = area.getText().trim();
        if(text.length() == 0) return;
        addMomentComment(moment.id, text);
        refreshUserMomentFeed(feed, username);
    }

    private Properties loadProfileForUser(String username) {
        Properties profile = new Properties();
        profile.setProperty("displayName", username == null ? "" : username);
        profile.setProperty("signature", "这个人还没有写个性签名。");
        if(username == null || username.length() == 0) return profile;
        if(username.equalsIgnoreCase(currentUser) && currentProfile != null) {
            profile.putAll(currentProfile);
            return profile;
        }
        try {
            Path profileFile = userDirectory(username).resolve("profile.properties");
            if(Files.exists(profileFile)) {
                try(InputStream in = Files.newInputStream(profileFile)) { profile.load(in); }
            }
        } catch(Exception e) {}
        if(profile.getProperty("displayName") == null || profile.getProperty("displayName").trim().length() == 0) profile.setProperty("displayName", username);
        if(profile.getProperty("signature") == null) profile.setProperty("signature", "这个人还没有写个性签名。");
        return profile;
    }

    private Set<String> loadFriendsForUser(String username) {
        Set<String> result = new HashSet<String>();
        if(username == null || username.length() == 0) return result;
        if(username.equalsIgnoreCase(currentUser)) { result.addAll(friends); return result; }
        try {
            Path file = userDirectory(username).resolve("friends.txt");
            if(Files.exists(file)) {
                java.util.List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                for(int i=0;i<lines.size();i++) { String f = lines.get(i).trim(); if(f.length() > 0) result.add(f); }
            }
        } catch(Exception e) {}
        return result;
    }

    private int commonFriendsCount(String username) {
        Set<String> otherFriends = loadFriendsForUser(username);
        int count = 0;
        Iterator<String> it = friends.iterator();
        while(it.hasNext()) if(otherFriends.contains(it.next())) count++;
        return count;
    }

    private int commonGroupsCount(String username) {
        int count = 0;
        Iterator<Map.Entry<String, Set<String>>> it = chatGroups.entrySet().iterator();
        while(it.hasNext()) {
            Set<String> members = it.next().getValue();
            if(members != null && members.contains(username)) count++;
        }
        return count;
    }

    private java.util.List<Moment> visibleMomentsForAuthor(String author) {
        java.util.List<Moment> result = new ArrayList<Moment>();
        java.util.List<Moment> moments = loadMoments();
        for(int i=0;i<moments.size();i++) {
            Moment moment = moments.get(i);
            if(moment.author != null && moment.author.equalsIgnoreCase(author) && canViewMoment(moment)) result.add(moment);
        }
        sortMoments(result);
        return result;
    }

    private boolean canViewMomentAuthor(String author) {
        return author != null && (author.equalsIgnoreCase(currentUser) || friends.contains(author));
    }

    private boolean canViewMoment(Moment moment) {
        if(moment == null || moment.author == null) return false;
        if(moment.author.equalsIgnoreCase(currentUser)) return true;
        String visibility = normalizeMomentVisibility(moment.visibility);
        if(MOMENT_VISIBILITY_PRIVATE.equals(visibility)) return false;
        if(MOMENT_VISIBILITY_PUBLIC.equals(visibility)) return true;
        return friends.contains(moment.author);
    }

    private String normalizeMomentVisibility(String visibility) {
        if(MOMENT_VISIBILITY_PUBLIC.equals(visibility)) return MOMENT_VISIBILITY_PUBLIC;
        if(MOMENT_VISIBILITY_PRIVATE.equals(visibility)) return MOMENT_VISIBILITY_PRIVATE;
        return MOMENT_VISIBILITY_FRIENDS;
    }

    private String visibilityLabel(String visibility) {
        visibility = normalizeMomentVisibility(visibility);
        if(MOMENT_VISIBILITY_PUBLIC.equals(visibility)) return "所有人可见";
        if(MOMENT_VISIBILITY_PRIVATE.equals(visibility)) return "仅自己可见";
        return "仅好友可见";
    }

    private String visibilityFromLabel(String label) {
        if("所有人可见".equals(label)) return MOMENT_VISIBILITY_PUBLIC;
        if("仅自己可见".equals(label)) return MOMENT_VISIBILITY_PRIVATE;
        return MOMENT_VISIBILITY_FRIENDS;
    }


    private ImageIcon createScaledImageIcon(byte[] data, int maxWidth, int maxHeight) {
        try {
            if(data == null || data.length == 0) return null;
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
            if(image == null) return null;
            int width = image.getWidth();
            int height = image.getHeight();
            double scale = Math.min(maxWidth / (double)Math.max(1, width), maxHeight / (double)Math.max(1, height));
            scale = Math.min(1.0, scale);
            int targetWidth = Math.max(1, (int)Math.round(width * scale));
            int targetHeight = Math.max(1, (int)Math.round(height * scale));
            return new ImageIcon(image.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH));
        } catch(Exception e) { return null; }
    }

    private Component createMomentImagePreview(final Moment moment, int maxWidth, int maxHeight) {
        if(moment == null || moment.imageData == null) return Box.createVerticalStrut(0);
        ImageIcon icon = createScaledImageIcon(moment.imageData, maxWidth, maxHeight);
        if(icon == null) return Box.createVerticalStrut(0);
        BubblePanel panel = new BubblePanel(SURFACE_SOFT, BORDER_LIGHT, RADIUS_MD);
        panel.setLayout(new BorderLayout());
        panel.setBorder(pad(SPACE_SM));
        JLabel image = new JLabel(icon);
        image.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(image, BorderLayout.CENTER);
        JLabel tip = createHintLabel((moment.imageName == null || moment.imageName.length() == 0 ? "朋友圈图片" : moment.imageName) + " · 双击查看大图");
        tip.setBorder(pad(6, 0, 0, 0));
        panel.add(tip, BorderLayout.SOUTH);
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { if(e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) previewMomentImage(moment); }});
        return panel;
    }

    private void previewMomentImage(final Moment moment) {
        if(moment == null || moment.imageData == null) return;
        try {
            final BufferedImage image = ImageIO.read(new ByteArrayInputStream(moment.imageData));
            if(image == null) return;
            final int originalWidth = image.getWidth();
            final int originalHeight = image.getHeight();
            final double[] scale = new double[]{Math.min(1.0, Math.min(820.0 / Math.max(1, originalWidth), 580.0 / Math.max(1, originalHeight)))};
            final JDialog dialog = new JDialog(this, "朋友圈图片预览", true);
            dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            JPanel root = new JPanel(new BorderLayout(SPACE_MD, SPACE_MD));
            root.setBackground(CHAT_BACKGROUND);
            root.setBorder(pad(SPACE_LG));
            final JLabel imageLabel = new JLabel();
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            root.add(createModernScrollPane(imageLabel, CHAT_BACKGROUND), BorderLayout.CENTER);
            final JLabel info = createHintLabel(nullSafe(moment.imageName) + " · " + displayFileSize(moment.imageData.length) + " · " + originalWidth + "×" + originalHeight);
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, SPACE_SM, 0));
            actions.setOpaque(false);
            JButton zoomOut = createButton("缩小", false);
            JButton zoomIn = createButton("放大", false);
            JButton fit = createButton("适应", false);
            final Runnable update = new Runnable() { public void run() {
                int width = Math.max(1, (int)Math.round(originalWidth * scale[0]));
                int height = Math.max(1, (int)Math.round(originalHeight * scale[0]));
                imageLabel.setIcon(new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH)));
                info.setText(nullSafe(moment.imageName) + " · " + (int)Math.round(scale[0] * 100) + "%");
            }};
            zoomOut.addActionListener(new ActionListener(){ public void actionPerformed(ActionEvent e){ scale[0] = Math.max(0.1, scale[0] / 1.25); update.run(); }});
            zoomIn.addActionListener(new ActionListener(){ public void actionPerformed(ActionEvent e){ scale[0] = Math.min(4.0, scale[0] * 1.25); update.run(); }});
            fit.addActionListener(new ActionListener(){ public void actionPerformed(ActionEvent e){ scale[0] = Math.min(1.0, Math.min(820.0 / Math.max(1, originalWidth), 580.0 / Math.max(1, originalHeight))); update.run(); }});
            actions.add(zoomOut); actions.add(zoomIn); actions.add(fit);
            JPanel bottom = new JPanel(new BorderLayout());
            bottom.setOpaque(false);
            bottom.add(info, BorderLayout.WEST);
            bottom.add(actions, BorderLayout.EAST);
            root.add(bottom, BorderLayout.SOUTH);
            dialog.setContentPane(root);
            dialog.setSize(920, 700);
            dialog.setLocationRelativeTo(this);
            update.run();
            dialog.setVisible(true);
        } catch(Exception e) { showInlineNotice("无法打开朋友圈图片：" + e.getMessage(), DANGER); }
    }

    private void selectConversation(String key) {
        if(key == null || key.length() == 0 || onlineList == null) return;
        if(!modelContains(key)) rebuildConversationList(key);
        if(modelContains(key)) onlineList.setSelectedValue(key, true);
    }

    private void showMomentsDialog() {
        if(currentUserDir == null || currentUser == null || currentUser.length() == 0) {
            JOptionPane.showMessageDialog(this, "请先登录后再打开朋友圈。");
            return;
        }
        MomentsDialog dialog = new MomentsDialog(this);
        dialog.setVisible(true);
        updateMomentBadge();
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
            if(parts.length >= 8) moment.visibility = normalizeMomentVisibility(decodeToken(parts[7]));
            else moment.visibility = MOMENT_VISIBILITY_FRIENDS;
            if(parts.length >= 10) {
                moment.imageName = decodeToken(parts[8]);
                String imageBase64 = decodeToken(parts[9]);
                if(imageBase64.length() > 0) {
                    try { moment.imageData = Base64.getDecoder().decode(imageBase64); } catch(Exception ignore) { moment.imageData = null; }
                }
            }
            return moment;
        } catch(Exception e) {
            return null;
        }
    }

    private boolean shouldAcceptMoment(String sender, Moment moment) {
        if(moment.id == null || moment.id.length() == 0) return false;
        if(moment.author == null || moment.author.length() == 0) return false;
        return canViewMoment(moment) || friends.contains(sender);
    }

    private void upsertMoment(Moment incoming) {
        java.util.List<Moment> moments = loadMoments();
        Moment old = null;
        boolean replaced = false;
        for(int i=0;i<moments.size();i++) {
            if(moments.get(i).id.equals(incoming.id)) {
                old = moments.get(i);
                moments.set(i, incoming);
                replaced = true;
                break;
            }
        }
        if(!replaced) moments.add(incoming);
        detectMomentNotifications(old, incoming);
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
            Moment moment = new Moment(id, time, currentUser, text);
            moment.visibility = MOMENT_VISIBILITY_FRIENDS;
            return moment;
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
        String imageBase64 = moment.imageData == null ? "" : Base64.getEncoder().encodeToString(moment.imageData);
        return MOMENT_RECORD_VERSION + "	"
                + nullSafe(moment.id) + "	"
                + nullSafe(moment.time) + "	"
                + nullSafe(moment.author) + "	"
                + encodeToken(moment.text) + "	"
                + encodeToken(membersToCsv(moment.likes)) + "	"
                + encodeMomentComments(moment.comments) + "	"
                + encodeToken(normalizeMomentVisibility(moment.visibility)) + "	"
                + encodeToken(moment.imageName) + "	"
                + encodeToken(imageBase64);
    }

    private Moment addMoment(String text) {
        return addMoment(text, MOMENT_VISIBILITY_FRIENDS, null);
    }

    private Moment addMoment(String text, String visibility) {
        return addMoment(text, visibility, null);
    }

    private Moment addMoment(String text, String visibility, File imageFile) {
        java.util.List<Moment> moments = loadMoments();
        Moment moment = new Moment(UUID.randomUUID().toString(),
                LocalDateTime.now().format(logTime), currentUser, text == null ? "" : text);
        moment.visibility = normalizeMomentVisibility(visibility);
        if(imageFile != null) {
            try {
                if(!validateAttachmentType(imageFile, "图片")) return null;
                if(!validateAttachment(imageFile)) return null;
                moment.imageName = imageFile.getName();
                moment.imageData = Files.readAllBytes(imageFile.toPath());
            } catch(Exception e) {
                showInlineNotice("朋友圈图片读取失败：" + e.getMessage(), DANGER);
                return null;
            }
        }
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
        return addMomentComment(id, comment, "");
    }

    private Moment addMomentComment(String id, String comment, String replyTo) {
        java.util.List<Moment> moments = loadMoments();
        Moment changed = null;
        for(int i=0;i<moments.size();i++) {
            Moment moment = moments.get(i);
            if(moment.id.equals(id)) {
                moment.comments.add(new MomentComment(LocalDateTime.now().format(logTime), currentUser, comment, replyTo));
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

    private void updateMomentVisibility(String id, String visibility) {
        java.util.List<Moment> moments = loadMoments();
        Moment changed = null;
        for(int i=0;i<moments.size();i++) {
            Moment moment = moments.get(i);
            if(moment.id.equals(id) && moment.author.equalsIgnoreCase(currentUser)) {
                moment.visibility = normalizeMomentVisibility(visibility);
                changed = moment;
                break;
            }
        }
        saveMoments(moments);
        if(changed != null) {
            uploadMomentToServer(changed);
            broadcastMoment(changed);
            refreshActiveMoments();
        }
    }

    private void updateAllOwnMomentsVisibility(String visibility) {
        java.util.List<Moment> moments = loadMoments();
        boolean changedAny = false;
        for(int i=0;i<moments.size();i++) {
            Moment moment = moments.get(i);
            if(moment.author.equalsIgnoreCase(currentUser)) {
                moment.visibility = normalizeMomentVisibility(visibility);
                uploadMomentToServer(moment);
                broadcastMoment(moment);
                changedAny = true;
            }
        }
        if(changedAny) saveMoments(moments);
        refreshActiveMoments();
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
        if(moment != null) {
            if(shouldAcceptMoment("server", moment)) upsertMoment(moment);
            else removeMomentById(moment.id);
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
        Iterator<String> it = visibleUsers.iterator();
        while(it.hasNext()) {
            String user = it.next();
            if(user.equalsIgnoreCase(ownNick())) continue;
            sendMomentTo(user, moment);
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

    private void detectMomentNotifications(Moment oldMoment, Moment incoming) {
        if(incoming == null || incoming.author == null || !incoming.author.equalsIgnoreCase(currentUser)) return;
        Set<String> oldLikes = oldMoment == null ? new LinkedHashSet<String>() : oldMoment.likes;
        Iterator<String> likeIt = incoming.likes.iterator();
        while(likeIt.hasNext()) {
            String actor = likeIt.next();
            if(actor.equalsIgnoreCase(currentUser)) continue;
            if(!oldLikes.contains(actor)) addMomentNotification(actor, "点赞", clipPreview(incoming.text, 36));
        }
        Set<String> oldComments = new HashSet<String>();
        if(oldMoment != null) {
            for(int i=0;i<oldMoment.comments.size();i++) oldComments.add(commentFingerprint(oldMoment.comments.get(i)));
        }
        for(int i=0;i<incoming.comments.size();i++) {
            MomentComment c = incoming.comments.get(i);
            if(c.author.equalsIgnoreCase(currentUser)) continue;
            if(!oldComments.contains(commentFingerprint(c))) addMomentNotification(c.author, "评论", clipPreview(c.text, 48));
        }
    }

    private String commentFingerprint(MomentComment c) {
        if(c == null) return "";
        return nullSafe(c.time) + "|" + nullSafe(c.author) + "|" + nullSafe(c.text) + "|" + nullSafe(c.replyTo);
    }

    private void addMomentNotification(String actor, String action, String detail) {
        MomentNotification n = new MomentNotification(UUID.randomUUID().toString(),
                LocalDateTime.now().format(logTime), actor, action, detail, false);
        momentNotifications.add(0, n);
        while(momentNotifications.size() > 200) momentNotifications.remove(momentNotifications.size() - 1);
        saveMomentNotifications();
        updateMomentBadge();
    }

    private Path momentNotificationsFile() {
        return currentUserDir.resolve(MOMENT_NOTIFICATIONS_FILE);
    }

    private void loadMomentNotifications() {
        momentNotifications.clear();
        if(currentUserDir == null) return;
        Path file = momentNotificationsFile();
        if(!Files.exists(file)) { updateMomentBadge(); return; }
        try {
            java.util.List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for(int i=0;i<lines.size();i++) {
                String[] parts = lines.get(i).split("\\t", 6);
                if(parts.length == 6) {
                    momentNotifications.add(new MomentNotification(parts[0], decodeToken(parts[1]), decodeToken(parts[2]),
                            decodeToken(parts[3]), decodeToken(parts[4]), "1".equals(parts[5])));
                }
            }
        } catch(Exception e) {}
        updateMomentBadge();
    }

    private void saveMomentNotifications() {
        if(currentUserDir == null) return;
        try {
            Files.createDirectories(currentUserDir);
            java.util.List<String> lines = new ArrayList<String>();
            for(int i=0;i<momentNotifications.size();i++) {
                MomentNotification n = momentNotifications.get(i);
                lines.add(n.id + "\t" + encodeToken(n.time) + "\t" + encodeToken(n.actor) + "\t"
                        + encodeToken(n.action) + "\t" + encodeToken(n.detail) + "\t" + (n.read ? "1" : "0"));
            }
            Files.write(momentNotificationsFile(), lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch(Exception e) {}
    }

    private int unreadMomentNotificationCount() {
        int count = 0;
        for(int i=0;i<momentNotifications.size();i++) if(!momentNotifications.get(i).read) count++;
        return count;
    }

    private void updateMomentBadge() {
        int unreadCount = unreadMomentNotificationCount();
        if(buttonMoments instanceof RailButton) ((RailButton)buttonMoments).setBadgeCount(unreadCount);
        if(activeMomentsDialog != null) activeMomentsDialog.updateMessageButtonBadge();
    }

    private void showMomentNotificationsDialog(Component parent) {
        final JDialog dialog = new JDialog(this, "朋友圈消息", true);
        dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        dialog.setSize(460, 520);
        dialog.setLocationRelativeTo(parent == null ? this : parent);
        JPanel root = new JPanel(new BorderLayout(SPACE_MD, SPACE_MD));
        root.setBackground(CHAT_BACKGROUND);
        root.setBorder(pad(SPACE_LG));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("朋友圈消息");
        title.setFont(PAGE_TITLE_FONT);
        title.setForeground(TEXT);
        JButton readAll = createButton("全部已读", false);
        readAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for(int i=0;i<momentNotifications.size();i++) momentNotifications.get(i).read = true;
                saveMomentNotifications();
                updateMomentBadge();
                dialog.dispose();
            }
        });
        header.add(title, BorderLayout.WEST);
        header.add(readAll, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);
        JPanel list = new JPanel();
        list.setBackground(CHAT_BACKGROUND);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        if(momentNotifications.size() == 0) {
            list.add(createEmptyState("暂无互动消息", "别人点赞或评论你的朋友圈后会显示在这里"));
        } else {
            for(int i=0;i<momentNotifications.size();i++) list.add(createMomentNotificationRow(momentNotifications.get(i)));
        }
        root.add(createModernScrollPane(list, CHAT_BACKGROUND), BorderLayout.CENTER);
        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private Component createMomentNotificationRow(MomentNotification n) {
        BubblePanel row = createCardPanel(SPACE_MD);
        row.setLayout(new BorderLayout(SPACE_MD, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(new AvatarView(n.actor, false), BorderLayout.WEST);
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(n.actor + " " + n.action + "了你的朋友圈" + (n.read ? "" : "  ●"));
        title.setFont(UI_FONT_BOLD);
        title.setForeground(n.read ? TEXT : PRIMARY_DARK);
        JLabel detail = new JLabel("<html><div style=\"width:310px;\">" + htmlText(n.detail) + "</div></html>");
        detail.setFont(UI_FONT_SMALL);
        detail.setForeground(MUTED);
        JLabel time = new JLabel(n.time);
        time.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
        time.setForeground(SOFT_MUTED);
        text.add(title);
        text.add(Box.createVerticalStrut(SPACE_XS));
        text.add(detail);
        text.add(Box.createVerticalStrut(SPACE_XS));
        text.add(time);
        row.add(text, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        return row;
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
                    .append(encodeToken(comment.text)).append('\u001f')
                    .append(encodeToken(comment.replyTo));
        }
        return encodeToken(builder.toString());
    }

    private java.util.List<MomentComment> decodeMomentComments(String encoded) {
        java.util.List<MomentComment> comments = new ArrayList<MomentComment>();
        String raw = decodeToken(encoded);
        if(raw.length() == 0) return comments;
        String[] rows = raw.split("\\n");
        for(int i=0;i<rows.length;i++) {
            String[] parts = rows[i].split("\u001f", 4);
            if(parts.length >= 3) {
                String replyTo = parts.length >= 4 ? decodeToken(parts[3]) : "";
                comments.add(new MomentComment(parts[0], parts[1], decodeToken(parts[2]), replyTo));
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
            String plain = stripHtml(str);
            if(shouldSkipLogLine(plain)) return;
            String line = "[" + LocalDateTime.now().format(logTime) + "] "
                    + plain + System.lineSeparator();
            Files.write(logFile, line.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch(Exception e) {
        }
    }

    private boolean shouldSkipLogLine(String text) {
        if(text == null) return true;
        String plain = stripLogTimestamp(text).trim();
        if(plain.length() == 0) return true;
        return plain.contains(USERS_PREFIX)
                || plain.contains(FILE_PREFIX)
                || plain.contains(SERVER_MOMENT_ITEM_PREFIX)
                || plain.contains(FRIEND_REQUEST_PREFIX)
                || plain.contains(FRIEND_ACCEPT_PREFIX)
                || plain.contains(FRIEND_REJECT_PREFIX)
                || plain.contains(FRIEND_DELETE_PREFIX)
                || plain.contains(GROUP_INVITE_PREFIX)
                || plain.contains(GROUP_MESSAGE_PREFIX)
                || plain.contains(GROUP_KICK_PREFIX)
                || plain.contains(GROUP_LEAVE_PREFIX)
                || plain.contains(MESSAGE_RECALL_PREFIX)
                || plain.contains(MOMENT_SYNC_REQUEST_PREFIX)
                || plain.contains(MOMENT_SYNC_ITEM_PREFIX)
                || plain.contains(MOMENT_DELETE_PREFIX)
                || plain.contains(VIDEO_CALL_PREFIX)
                || plain.contains(PRIVATE_MESSAGE_PREFIX)
                || plain.contains(READ_RECEIPT_PREFIX);
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
        if(ck == null || !ck.isConnected()) {
            showInlineNotice("请先连接服务器后再发送附件。", DANGER);
            return;
        }
        String selected = selectedConversationKey();
        if(selected == null) {
            showInlineNotice("请先选择会话。", DANGER);
            return;
        }
        if(selectedChatTarget != null && !friends.contains(selectedChatTarget)) {
            showInlineNotice("非好友不能发送附件，请先添加好友。", DANGER);
            return;
        }
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
        if(ck == null || !ck.isConnected()) {
            showInlineNotice("请先连接服务器后再录制语音。", DANGER);
            return;
        }
        String selected = selectedConversationKey();
        if(selected == null) {
            showInlineNotice("请先选择会话。", DANGER);
            return;
        }
        if(selectedChatTarget != null && !friends.contains(selectedChatTarget)) {
            showInlineNotice("非好友不能发送语音。", DANGER);
            return;
        }
        VoiceRecorderDialog dialog = new VoiceRecorderDialog(this, displayConversationName(selected));
        dialog.setVisible(true);
        File voiceFile = dialog.getVoiceFileToSend();
        if(voiceFile != null && validateAttachment(voiceFile)) {
            sendFileInCurrentConversation(voiceFile);
        }
    }

    private String requireOnlinePrivateTarget(String action) {
        if(ck == null || !ck.isConnected()) {
            showInlineNotice("请先连接服务器后再" + action + "。", DANGER);
            return null;
        }
        String selected = onlineList == null ? null : onlineList.getSelectedValue();
        if(selected == null) {
            showInlineNotice("请先从左侧选择一个在线好友。", DANGER);
            return null;
        }
        if(isBroadcastConversation(selected)) {
            showInlineNotice("广播暂不支持" + action + "，请选择在线好友。", WARNING);
            return null;
        }
        if(isGroupConversation(selected)) {
            showInlineNotice("群聊暂不支持" + action + "，请选择在线好友。", WARNING);
            return null;
        }
        String target = getSelectedPrivateTarget();
        if(target == null || target.length() == 0) {
            showInlineNotice("请选择一个在线好友后再" + action + "。", DANGER);
            return null;
        }
        if(!visibleUsers.contains(target)) {
            showInlineNotice("对方当前离线，暂不能" + action + "。", WARNING);
            return null;
        }
        return target;
    }

    private boolean validateAttachmentType(File file, String requiredType) {
        if(requiredType == null) return true;
        if("图片".equals(requiredType) && !hasExtension(file.getName(), IMAGE_EXTENSIONS)) {
            showInlineNotice("请选择图片文件：png、jpg、jpeg、gif、bmp 或 webp。", DANGER);
            return false;
        }
        if("语音".equals(requiredType) && !hasExtension(file.getName(), AUDIO_EXTENSIONS)) {
            showInlineNotice("请选择语音文件：mp3、wav、aac、flac、m4a、ogg、wma、amr 或 opus。", DANGER);
            return false;
        }
        return true;
    }

    private boolean validateAttachment(File file) {
        if(file == null) return false;
        if(!file.exists() || !file.isFile()) {
            showInlineNotice("没有找到文件：" + file.getName(), DANGER);
            return false;
        }
        if(file.length() > MAX_FILE_BYTES) {
            showInlineNotice("文件超过 " + displayFileSize(MAX_FILE_BYTES) + " 限制。", DANGER);
            return false;
        }
        return true;
    }

    private void updateAttachmentPreview() {
        if(attachmentPanel == null) return;
        if(pendingFile == null) {
            attachmentPanel.setVisible(false);
        } else {
            String selected = selectedConversationKey();
            String type = attachmentTypeLabel(pendingFile.getName());
            if(attachmentIconLabel != null) attachmentIconLabel.setText(type);
            attachmentNameLabel.setText(pendingFile.getName());
            attachmentMetaLabel.setText(type + " · " + displayFileSize(pendingFile.length()) + " · "
                    + (selected == null ? "请选择会话后发送" : "发送到 " + displayConversationName(selected)));
            attachmentPanel.setVisible(true);
        }
        if(southPanel != null) {
            southPanel.revalidate();
            southPanel.repaint();
        }
        updateSendButtonState();
    }

    private boolean attachmentAllowedInCurrentConversation() {
        String selected = selectedConversationKey();
        if(selected == null) return false;
        if(selectedChatTarget != null) return friends.contains(selectedChatTarget);
        return isBroadcastConversation(selected) || selectedGroupName != null;
    }

    private void updateSendButtonState() {
        if(buttonSend == null || msgWindow == null) return;
        String text = msgWindow.getText() == null ? "" : msgWindow.getText();
        int textLength = text.length();
        boolean hasText = text.trim().length() > 0;
        boolean hasFile = pendingFile != null;
        boolean hasConversation = onlineList != null && onlineList.getSelectedValue() != null;
        boolean connected = ck != null && ck.isConnected();
        boolean textWithinLimit = textLength <= MESSAGE_TEXT_LIMIT;
        boolean attachmentTargetReady = !hasFile || attachmentAllowedInCurrentConversation();
        boolean ready = connected && hasConversation && (hasText || hasFile)
                && attachmentTargetReady && textWithinLimit;
        buttonSend.setEnabled(ready);
        updateComposerCount(textLength, textWithinLimit);
        if(buttonSend != null) {
            if(ready) buttonSend.setToolTipText("发送到当前会话");
            else if(!connected) buttonSend.setToolTipText("请先连接服务器");
            else if(!hasConversation) buttonSend.setToolTipText("请先选择会话");
            else if(hasFile && !attachmentTargetReady) buttonSend.setToolTipText("当前会话不能发送附件");
            else if(!textWithinLimit) buttonSend.setToolTipText("消息过长，请删减后发送");
            else buttonSend.setToolTipText("输入内容后发送");
        }
        updateComposerHint(connected, hasConversation, hasText, hasFile,
                attachmentTargetReady, textWithinLimit);
    }

    private void updateComposerHint(boolean connected, boolean hasConversation,
            boolean hasText, boolean hasFile, boolean attachmentTargetReady,
            boolean textWithinLimit) {
        if(composerHintLabel == null) return;
        if(!connected) {
            composerHintLabel.setText("未连接服务器");
            composerHintLabel.setForeground(DANGER);
        } else if(!hasConversation) {
            composerHintLabel.setText("选择一个会话");
            composerHintLabel.setForeground(MUTED);
        } else if(hasFile && !attachmentTargetReady) {
            composerHintLabel.setText("当前会话不能发送附件");
            composerHintLabel.setForeground(WARNING);
        } else if(!textWithinLimit) {
            composerHintLabel.setText("消息过长，请删减到 " + MESSAGE_TEXT_LIMIT + " 字以内");
            composerHintLabel.setForeground(DANGER);
        } else if(hasText || hasFile) {
            composerHintLabel.setText("准备发送到当前会话");
            composerHintLabel.setForeground(PRIMARY_DARK);
        } else if(selectedGroupName != null) {
            composerHintLabel.setText("群聊消息");
            composerHintLabel.setForeground(MUTED);
        } else if(selectedChatTarget != null) {
            composerHintLabel.setText(visibleUsers.contains(selectedChatTarget) ? "私聊消息" : "离线文字消息");
            composerHintLabel.setForeground(MUTED);
        } else {
            composerHintLabel.setText("广播消息");
            composerHintLabel.setForeground(MUTED);
        }
    }

    private void updateComposerCount(int textLength, boolean textWithinLimit) {
        if(composerCountLabel == null) return;
        composerCountLabel.setText(textLength + "/" + MESSAGE_TEXT_LIMIT);
        composerCountLabel.setForeground(textWithinLimit ? MUTED : DANGER);
    }

    private void showInlineNotice(String text, Color color) {
        if(composerHintLabel != null) {
            composerHintLabel.setText(text);
            composerHintLabel.setForeground(color == null ? MUTED : color);
        }
        Toolkit.getDefaultToolkit().beep();
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
        if(!friends.contains(target)) {
            showFailedOutgoingMessage(target, ownNick(), message, "非好友，消息未发送");
            showInlineNotice("非好友不能发送消息，请先添加好友。", DANGER);
            return;
        }
        String messageId = newMessageId();
        String time = LocalDateTime.now().format(displayTime);
        boolean online = visibleUsers.contains(target);
        ChatMessage chatMessage = createOutgoingTrackedMessage(target, "发给 " + target,
                message, time, messageId, online ? "未读" : "待发送");
        showOutgoingTextMessage(chatMessage);
        if(!online || !sendTrackedPrivateMessage(target, messageId, message)) {
            queuePendingPrivateMessage(messageId, target, message, time, chatMessage);
        }
        appendLog("[private to " + target + "] " + message);
    }

    private void queuePendingPrivateMessage(String messageId, String target,
            String message, String time, ChatMessage chatMessage) {
        PendingPrivateMessage pending = new PendingPrivateMessage(messageId, target, message, time);
        pending.message = chatMessage;
        pendingPrivateMessages.put(messageId, pending);
        updateOutgoingStatus(messageId, "待发送");
        savePendingPrivateMessages();
    }

    private ChatMessage createOutgoingTrackedMessage(String conversationKey, String sender,
            String message, String time, String messageId, String status) {
        ChatMessage chatMessage = withConversation(new ChatMessage(MessageKind.OUTGOING,
                sender, message, time), conversationKey);
        chatMessage.messageId = messageId;
        chatMessage.deliveryStatus = status;
        chatMessage.sentAtMillis = System.currentTimeMillis();
        trackedOutgoingMessages.put(messageId, chatMessage);
        return chatMessage;
    }

    private String newMessageId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private boolean sendTrackedPrivateMessage(String target, String messageId, String message) {
        boolean queued = ck != null && ck.sendMessage("/msg " + target + " " + PRIVATE_MESSAGE_PREFIX
                + messageId + "|" + encodeToken(message));
        if(!queued) {
            updateOutgoingStatus(messageId, "待发送");
            return false;
        }
        updateOutgoingStatus(messageId, "未读");
        return true;
    }

    private void showFailedOutgoingMessage(final String conversationKey, final String sender, final String message, final String reason) {
        final ChatMessage failed = withConversation(new ChatMessage(MessageKind.OUTGOING, sender, message, LocalDateTime.now().format(displayTime)), conversationKey);
        failed.deliveryStatus = reason == null ? "发送失败" : reason;
        Runnable show = new Runnable() {
            public void run() { if(historyWindow != null) historyWindow.addMessage(failed); }
        };
        if(SwingUtilities.isEventDispatchThread()) show.run();
        else SwingUtilities.invokeLater(show);
    }

    private void showOutgoingTextMessage(final ChatMessage chatMessage) {
        if(chatMessage != null && chatMessage.sentAtMillis == 0L) chatMessage.sentAtMillis = System.currentTimeMillis();
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
        chatMessage.messageId = newMessageId();
        chatMessage.sentAtMillis = System.currentTimeMillis();
        if(historyWindow != null) historyWindow.addMessage(chatMessage);
        appendLog("[broadcast] " + message);
    }

    private boolean sendGroupMessage(String groupName, String message) {
        Set<String> members = chatGroups.get(groupName);
        if(members == null || members.size() <= 1) {
            addMsg("<font color=\"#ff0000\">群聊成员为空，请重新建群。</font>");
            return false;
        }
        String messageId = newMessageId();
        String payload = GROUP_MESSAGE_PREFIX + encodeToken(groupName) + "|"
                + encodeToken(messageId) + "|" + encodeToken(message);
        int sentCount = 0;
        int queuedCount = 0;
        java.util.List<String> offline = new ArrayList<String>();
        Iterator<String> it = members.iterator();
        while(it.hasNext()) {
            String member = it.next();
            if(member.equalsIgnoreCase(ownNick())) continue;
            if(!visibleUsers.contains(member)) {
                offline.add(member);
                queuePendingDelivery(member, payload, "群聊 " + groupName + " 消息");
                queuedCount++;
                continue;
            }
            ck.sendMessage("/msg " + member + " " + payload);
            sentCount++;
        }
        ChatMessage chatMessage = withConversation(new ChatMessage(MessageKind.OUTGOING,
                ownNick() + " / " + groupName, message, LocalDateTime.now().format(displayTime)), groupLabel(groupName));
        chatMessage.messageId = messageId;
        chatMessage.sentAtMillis = System.currentTimeMillis();
        if(queuedCount > 0) {
            chatMessage.deliveryStatus = sentCount > 0 ? "部分待发送" : "待发送";
        } else {
            chatMessage.deliveryStatus = "已发送";
        }
        historyWindow.addMessage(chatMessage);
        appendLog("[group " + groupName + " to " + sentCount
                + " online, queued " + queuedCount + "] " + message);
        if(offline.size() > 0) {
            showInlineNotice("群聊中 " + offline.size() + " 名成员离线，消息已加入待发送。", WARNING);
        }
        return true;
    }

    private boolean sendFileInCurrentConversation(File file) {
        if(selectedGroupName != null) return sendFileToGroup(selectedGroupName, file);
        if(selectedChatTarget != null) return sendFileTo(selectedChatTarget, file);
        return sendFileToBroadcast(file);
    }

    private boolean sendFileTo(String target, File file) {
        if(!friends.contains(target)) {
            showFailedOutgoingMessage(target, ownNick(), "[文件] " + (file == null ? "" : file.getName()), "非好友，文件未发送");
            showInlineNotice("非好友不能发送文件，请先添加好友。", DANGER);
            return false;
        }
        try {
            byte[] data = readFileDataForSending(file);
            if(data == null) return false;
            String wireName = encodePrivateFileName(file.getName());
            String encoded = Base64.getEncoder().encodeToString(data);
            if(ck != null && ck.isConnected() && visibleUsers.contains(target)) {
                ck.sendMessage("/file " + target + " " + wireName + " " + encoded);
            } else {
                queuePendingFileDelivery(target, wireName, encoded, "私聊文件：" + file.getName());
                showInlineNotice("好友离线，文件已加入待发送队列。", WARNING);
            }
            appendLog("[file to " + target + "] " + file.getName() + " (" + data.length + " bytes)");
            showOutgoingFileMessage(target, ownNick() + " → " + target, file.getName(), data);
            return true;
        } catch(Exception e) {
            addMsg("<font color=\"#ff0000\">文件发送失败：" + escapeHtml(e.getMessage()) + "</font>");
            return false;
        }
    }

    private boolean sendFileToGroup(String groupName, File file) {
        Set<String> members = chatGroups.get(groupName);
        if(members == null || members.size() <= 1) {
            showInlineNotice("群聊成员为空，请重新建群。", DANGER);
            return false;
        }
        try {
            byte[] data = readFileDataForSending(file);
            if(data == null) return false;
            String encoded = Base64.getEncoder().encodeToString(data);
            String wireName = encodeGroupFileName(groupName, file.getName());
            int count = 0;
            Iterator<String> it = members.iterator();
            while(it.hasNext()) {
                String member = it.next();
                if(member.equalsIgnoreCase(ownNick())) continue;
                if(ck != null && ck.isConnected() && visibleUsers.contains(member)) {
                    ck.sendMessage("/file " + member + " " + wireName + " " + encoded);
                } else {
                    queuePendingFileDelivery(member, wireName, encoded, "群聊文件：" + groupName + "/" + file.getName());
                }
                count++;
            }
            showOutgoingFileMessage(groupLabel(groupName), ownNick() + " / " + groupName, file.getName(), data);
            appendLog("[group file " + groupName + "] " + file.getName() + " to " + count + " members");
            return true;
        } catch(Exception e) {
            showInlineNotice("群文件发送失败：" + e.getMessage(), DANGER);
            return false;
        }
    }

    private boolean sendFileToBroadcast(File file) {
        try {
            byte[] data = readFileDataForSending(file);
            if(data == null) return false;
            String encoded = Base64.getEncoder().encodeToString(data);
            String wireName = encodeBroadcastFileName(file.getName());
            int count = 0;
            Iterator<String> it = visibleUsers.iterator();
            while(it.hasNext()) {
                String user = it.next();
                if(user.equalsIgnoreCase(ownNick())) continue;
                ck.sendMessage("/file " + user + " " + wireName + " " + encoded);
                count++;
            }
            showOutgoingFileMessage(BROADCAST_CHAT, ownNick(), file.getName(), data);
            appendLog("[broadcast file] " + file.getName() + " to " + count + " users");
            return true;
        } catch(Exception e) {
            showInlineNotice("广播文件发送失败：" + e.getMessage(), DANGER);
            return false;
        }
    }

    private byte[] readFileDataForSending(File file) throws IOException {
        if(file == null || !file.exists() || !file.isFile()) {
            showInlineNotice("没有找到文件。", DANGER);
            return null;
        }
        byte[] data = Files.readAllBytes(file.toPath());
        if(data.length > MAX_FILE_BYTES) {
            showInlineNotice("文件超过 " + displayFileSize(MAX_FILE_BYTES) + " 限制。", DANGER);
            return null;
        }
        return data;
    }

    private void queuePendingFileDelivery(String target, String wireName, String encoded, String note) {
        queuePendingDelivery(target, PENDING_FILE_PREFIX + encodeToken(wireName) + "|" + encodeToken(encoded), note);
    }

    private String encodeGroupFileName(String groupName, String original) {
        return GROUP_FILE_PREFIX + encodeToken(groupName).replace("=", "_") + "__" + encodeToken(original).replace("=", "_");
    }

    private String encodeBroadcastFileName(String original) {
        return BROADCAST_FILE_PREFIX + encodeToken(original).replace("=", "_");
    }

    private String encodePrivateFileName(String original) {
        return PRIVATE_FILE_PREFIX + encodeToken(original).replace("=", "_");
    }

    private String restoreTokenPadding(String token) {
        String raw = token == null ? "" : token.replace('_', '=');
        int mod = raw.length() % 4;
        if(mod != 0) raw += "====".substring(mod);
        return raw;
    }

    private void showOutgoingFileMessage(final String target, final String sender, final String fileName, final byte[] data) {
        final String time = LocalDateTime.now().format(displayTime);
        Runnable show = new Runnable() {
            public void run() {
                if(historyWindow != null) {
                    ChatMessage message = ChatMessage.file(MessageKind.OUTGOING, sender,
                            fileName, data.length, data, time);
                    message.conversationKey = target;
                    message.sentAtMillis = System.currentTimeMillis();
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

        java.util.List<String> rememberedConversations = new ArrayList<String>();
        rememberedConversations.addAll(conversationMeta.keySet());
        Iterator<String> messageKeys = conversationMessages.keySet().iterator();
        while(messageKeys.hasNext()) {
            String key = messageKeys.next();
            if(!rememberedConversations.contains(key)) rememberedConversations.add(key);
        }
        Iterator<PendingPrivateMessage> pendingIt = pendingPrivateMessages.values().iterator();
        while(pendingIt.hasNext()) {
            String target = pendingIt.next().target;
            if(!rememberedConversations.contains(target)) rememberedConversations.add(target);
        }
        Collections.sort(rememberedConversations, String.CASE_INSENSITIVE_ORDER);
        for(int i=0;i<rememberedConversations.size();i++) {
            addConversationCandidate(candidates, rememberedConversations.get(i), filter, ownNick);
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

    private String displayNameWithRemark(String username) {
        if(username == null) return "";
        if(isBroadcastConversation(username) || isGroupConversation(username)) return username;
        String remark = friendRemarks.get(username);
        if(remark != null && remark.trim().length() > 0) return remark.trim();
        return username;
    }

    private String remarkSuffix(String username) {
        String remark = friendRemarks.get(username);
        if(remark != null && remark.trim().length() > 0 && !remark.trim().equals(username)) return "（" + username + "）";
        return "";
    }

    private String displayConversationName(String value) {
        if(value == null) return "";
        if(isGroupConversation(value)) return groupNameFromLabel(value);
        if(isBroadcastConversation(value)) return BROADCAST_CHAT;
        return displayNameWithRemark(value);
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

        Iterator<String> userIt = nextUsers.iterator();
        while(userIt.hasNext()) {
            String user = userIt.next();
            if(user.length() > 0 && !user.equalsIgnoreCase(ownNick()) && !visibleUsers.contains(user)) {
                ChatMessage notice = withConversation(new ChatMessage(MessageKind.SYSTEM, "系统",
                        user + " 上线了", LocalDateTime.now().format(displayTime)), BROADCAST_CHAT);
                if(historyWindow != null) historyWindow.addMessage(notice);
            }
        }
        visibleUsers = nextUsers;
        flushPendingPrivateMessages(nextUsers);
        flushPendingDeliveries(nextUsers);
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
            String conversationKey = sender;
            String displayName = filename;
            String displaySender = sender;
            if(filename != null && filename.startsWith(GROUP_FILE_PREFIX)) {
                int sep = filename.indexOf("__", GROUP_FILE_PREFIX.length());
                if(sep > 0) {
                    String groupName = decodeToken(restoreTokenPadding(filename.substring(GROUP_FILE_PREFIX.length(), sep)));
                    displayName = decodeToken(restoreTokenPadding(filename.substring(sep + 2)));
                    conversationKey = groupLabel(groupName);
                    displaySender = sender + " / " + groupName;
                    Set<String> members = chatGroups.get(groupName);
                    if(members == null || !members.contains(ownNick())) return;
                }
            } else if(filename != null && filename.startsWith(BROADCAST_FILE_PREFIX)) {
                displayName = decodeToken(restoreTokenPadding(filename.substring(BROADCAST_FILE_PREFIX.length())));
                conversationKey = BROADCAST_CHAT;
            } else if(filename != null && filename.startsWith(PRIVATE_FILE_PREFIX)) {
                displayName = decodeToken(restoreTokenPadding(filename.substring(PRIVATE_FILE_PREFIX.length())));
                conversationKey = sender;
                displaySender = sender;
                if(!friends.contains(sender)) {
                    showInlineNotice("收到非好友 " + sender + " 的文件，已拦截。", DANGER);
                    appendLog("[blocked file from " + sender + "] " + displayName);
                    return;
                }
            } else if(!friends.contains(sender)) {
                showInlineNotice("收到非好友 " + sender + " 的文件，已拦截。", DANGER);
                appendLog("[blocked file from " + sender + "] " + filename);
                return;
            }
            byte[] data = Base64.getDecoder().decode(base64);
            appendLog("[file from " + sender + "] " + displayName + " (" + data.length + " bytes)");
            ChatMessage message = ChatMessage.file(MessageKind.INCOMING, displaySender,
                    displayName, data.length, data, LocalDateTime.now().format(displayTime));
            message.conversationKey = conversationKey;
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
        if(e.getSource()==buttonAddFriend) showFriendRequestCenter();
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
        private JComboBox<String> visibilityCombo;
        private File selectedMomentImage;
        private JLabel momentImageLabel;
        private JLabel publishStatusLabel;
        private JTextField searchField;
        private BadgeButton momentMessageButton;

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
            momentMessageButton = smallBadgeButton("消息");
            JButton messageButton = momentMessageButton;
            JButton permissionButton = smallButton("权限");
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
            messageButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showMomentNotificationsDialog(MomentsDialog.this);
                    refreshFeed();
                }
            });
            permissionButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    changeAllOwnMomentsVisibility();
                }
            });
            actions.add(refreshButton);
            actions.add(messageButton);
            actions.add(permissionButton);
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
            publishStatusLabel = createHintLabel(" ");
            publishStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            top.add(Box.createVerticalStrut(SPACE_XS));
            top.add(publishStatusLabel);
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
            JPanel topRow = new JPanel(new BorderLayout(SPACE_MD, 0));
            topRow.setOpaque(false);
            topRow.add(title, BorderLayout.WEST);
            visibilityCombo = new JComboBox<String>(new String[]{"仅好友可见", "所有人可见", "仅自己可见"});
            visibilityCombo.setFont(UI_FONT_SMALL);
            visibilityCombo.setPreferredSize(new Dimension(120, 30));
            topRow.add(visibilityCombo, BorderLayout.EAST);
            panel.add(topRow, BorderLayout.NORTH);

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
            momentImageLabel = new JLabel("未选择图片");
            momentImageLabel.setFont(UI_FONT_SMALL);
            momentImageLabel.setForeground(MUTED);
            left.add(momentImageLabel);
            left.add(tipLabel);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            buttons.setOpaque(false);
            JButton imageButton = smallButton("图片");
            JButton removeImageButton = smallButton("去图");
            JButton clearButton = smallButton("清空");
            publishButton = createButton("发布", true);
            publishButton.setPreferredSize(new Dimension(72, 32));
            publishButton.setEnabled(false);
            imageButton.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { chooseMomentImage(); }});
            removeImageButton.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { selectedMomentImage = null; updateMomentImageLabel(); updateComposerState(); }});
            clearButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    editor.setText("");
                    selectedMomentImage = null;
                    updateMomentImageLabel();
                    showTip(" ");
                }
            });
            publishButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    publishMoment();
                }
            });
            buttons.add(imageButton);
            buttons.add(removeImageButton);
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
            boolean hasText = editor.getText().trim().length() > 0;
            publishButton.setEnabled((hasText || selectedMomentImage != null) && remain >= 0);
        }

        private void chooseMomentImage() {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("选择朋友圈图片");
            chooser.setCurrentDirectory(new File(System.getProperty("user.home"), "Desktop"));
            chooser.setFileFilter(new FileNameExtensionFilter("图片文件 (*.png, *.jpg, *.jpeg, *.gif, *.bmp, *.webp)", "png", "jpg", "jpeg", "gif", "bmp", "webp"));
            chooser.setAcceptAllFileFilterUsed(false);
            if(chooser.showOpenDialog(MomentsDialog.this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if(validateAttachment(file) && validateAttachmentType(file, "图片")) {
                    selectedMomentImage = file;
                    updateMomentImageLabel();
                    updateComposerState();
                }
            }
        }

        private void updateMomentImageLabel() {
            if(momentImageLabel == null) return;
            if(selectedMomentImage == null) { momentImageLabel.setText("未选择图片"); momentImageLabel.setForeground(MUTED); }
            else { momentImageLabel.setText("已选择图片：" + selectedMomentImage.getName() + " · " + displayFileSize(selectedMomentImage.length())); momentImageLabel.setForeground(PRIMARY_DARK); }
        }

        private void publishMoment() {
            String text = editor.getText().trim();
            if(text.length() > MOMENT_TEXT_LIMIT) return;
            if(text.length() == 0 && selectedMomentImage == null) return;
            String visibility = visibilityFromLabel((String)visibilityCombo.getSelectedItem());
            Moment created = addMoment(text, visibility, selectedMomentImage);
            if(created == null) return;
            editor.setText("");
            selectedMomentImage = null;
            updateMomentImageLabel();
            refreshFeed();
            if(composerPanel != null) composerPanel.setVisible(false);
            if(publishStatusLabel != null) {
                publishStatusLabel.setText("动态发布成功 · " + visibilityLabel(visibility));
                publishStatusLabel.setForeground(SUCCESS);
            }
            MomentsDialog.this.revalidate();
            MomentsDialog.this.repaint();
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
            java.util.List<Moment> filtered = new ArrayList<Moment>();
            for(int i=0;i<moments.size();i++) {
                Moment moment = moments.get(i);
                if(!canViewMoment(moment)) continue;
                if(keyword.length() == 0) {
                    filtered.add(moment);
                    continue;
                }
                if(nullSafe(moment.text).toLowerCase(Locale.ROOT).contains(keyword)
                        || nullSafe(moment.imageName).toLowerCase(Locale.ROOT).contains(keyword)) {
                    filtered.add(moment);
                    continue;
                }
                for(int j=0;j<moment.comments.size();j++) {
                    MomentComment comment = moment.comments.get(j);
                    if(nullSafe(comment.text).toLowerCase(Locale.ROOT).contains(keyword)
                            || nullSafe(comment.replyTo).toLowerCase(Locale.ROOT).contains(keyword)) {
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
            body.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel text = new JLabel("<html><div style=\"width:520px;text-align:left;font-size:14px;line-height:1.5;\">"
                    + htmlText(moment.text) + "</div></html>");
            text.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
            text.setForeground(TEXT);
            text.setHorizontalAlignment(SwingConstants.LEFT);
            text.setAlignmentX(Component.LEFT_ALIGNMENT);
            text.setMaximumSize(new Dimension(Integer.MAX_VALUE, text.getPreferredSize().height));
            body.add(text);
            if(moment.imageData != null) {
                body.add(Box.createVerticalStrut(SPACE_SM));
                Component image = createMomentImagePreview(moment, 520, 260);
                if(image instanceof JComponent) ((JComponent) image).setAlignmentX(Component.LEFT_ALIGNMENT);
                body.add(image);
            }
            body.add(Box.createVerticalStrut(SPACE_MD));
            Component actions = createMomentActions(moment);
            if(actions instanceof JComponent) ((JComponent) actions).setAlignmentX(Component.LEFT_ALIGNMENT);
            body.add(actions);
            if(moment.likes.size() > 0 || moment.comments.size() > 0) {
                body.add(Box.createVerticalStrut(SPACE_SM));
                Component social = createMomentSocialPanel(moment);
                if(social instanceof JComponent) ((JComponent) social).setAlignmentX(Component.LEFT_ALIGNMENT);
                body.add(social);
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
            JLabel time = new JLabel(moment.time + " · " + visibilityLabel(moment.visibility));
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
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, SPACE_SM, 0));
            buttons.setOpaque(false);
            buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
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
                JButton permissionButton = smallButton("权限");
                permissionButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        changeSingleMomentVisibility(moment);
                    }
                });
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
                buttons.add(permissionButton);
                buttons.add(deleteButton);
            }
            row.add(buttons, BorderLayout.WEST);
            return row;
        }

        private Component createMomentSocialPanel(final Moment moment) {
            BubblePanel panel = new BubblePanel(SURFACE_SOFT, BORDER_LIGHT, RADIUS_MD);
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(pad(SPACE_SM, SPACE_MD, SPACE_SM, SPACE_MD));
            panel.setAlignmentX(Component.LEFT_ALIGNMENT);
            if(moment.likes.size() > 0) {
                JLabel likes = new JLabel("点赞 " + moment.likes.size() + " · " + membersToCsv(moment.likes));
                likes.setFont(UI_FONT_SMALL);
                likes.setForeground(PRIMARY_DARK);
                likes.setAlignmentX(Component.LEFT_ALIGNMENT);
                panel.add(likes);
            }
            for(int i=0;i<moment.comments.size();i++) {
                final MomentComment comment = moment.comments.get(i);
                JLabel line = new JLabel("<html><div style=\"width:500px;\"><b>"
                        + escapeHtml(comment.author) + "</b>"
                        + (comment.replyTo == null || comment.replyTo.length() == 0 ? "" : " 回复 <b>" + escapeHtml(comment.replyTo) + "</b>")
                        + "：" + htmlText(comment.text)
                        + " <span style=\"color:#64748b;font-size:10px;\">"
                        + escapeHtml(comment.time) + "</span></div></html>");
                line.setFont(UI_FONT_SMALL);
                line.setForeground(TEXT);
                line.setHorizontalAlignment(SwingConstants.LEFT);
                line.setAlignmentX(Component.LEFT_ALIGNMENT);
                if(moment.author.equalsIgnoreCase(currentUser)) {
                    line.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    line.setToolTipText("点击回复该评论");
                    line.addMouseListener(new MouseAdapter() {
                        public void mouseClicked(MouseEvent e) {
                            replyMomentComment(moment, comment, new Runnable() {
                                public void run() { refreshFeed(); }
                            });
                        }
                    });
                }
                if(i > 0 || moment.likes.size() > 0) panel.add(Box.createVerticalStrut(5));
                panel.add(line);
            }
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
            return panel;
        }

        private void changeSingleMomentVisibility(final Moment moment) {
            JComboBox<String> box = new JComboBox<String>(new String[]{"仅好友可见", "所有人可见", "仅自己可见"});
            box.setSelectedItem(visibilityLabel(moment.visibility));
            int option = JOptionPane.showConfirmDialog(MomentsDialog.this, box, "修改这条朋友圈权限", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if(option != JOptionPane.OK_OPTION) return;
            updateMomentVisibility(moment.id, visibilityFromLabel((String)box.getSelectedItem()));
            refreshFeed();
            showTip("权限已修改");
        }

        private void changeAllOwnMomentsVisibility() {
            JComboBox<String> box = new JComboBox<String>(new String[]{"仅好友可见", "所有人可见", "仅自己可见"});
            int option = JOptionPane.showConfirmDialog(MomentsDialog.this, box, "修改自己所有朋友圈权限", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if(option != JOptionPane.OK_OPTION) return;
            updateAllOwnMomentsVisibility(visibilityFromLabel((String)box.getSelectedItem()));
            refreshFeed();
            showTip("已修改自己所有朋友圈权限");
        }

        private void replyMomentComment(Moment moment, MomentComment targetComment, final Runnable after) {
            JTextArea area = new JTextArea(4, 24);
            area.setFont(UI_FONT);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setBorder(pad(SPACE_SM));
            JScrollPane scroll = new JScrollPane(area);
            scroll.setBorder(new RoundedBorder(BORDER, RADIUS_MD));
            int option = JOptionPane.showConfirmDialog(this, scroll, "回复 " + targetComment.author,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if(option != JOptionPane.OK_OPTION) return;
            String text = area.getText().trim();
            if(text.length() == 0) return;
            addMomentComment(moment.id, text, targetComment.author);
            if(after != null) after.run();
            showTip("回复已发布");
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

        private void updateMessageButtonBadge() {
            if(momentMessageButton != null) {
                momentMessageButton.setBadgeCount(unreadMomentNotificationCount());
            }
        }

        private BadgeButton smallBadgeButton(String text) {
            BadgeButton button = new BadgeButton(text, false);
            button.setFont(UI_FONT_BOLD);
            button.setPreferredSize(new Dimension(58, 30));
            button.setBadgeCount(unreadMomentNotificationCount());
            return button;
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
            getRootPane().registerKeyboardAction(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    loginData = null;
                    dispose();
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
            loginUserField.setNextFocusableComponent(loginPasswordField);
            loginPasswordField.setNextFocusableComponent(primaryButton);
            registerUserField.setNextFocusableComponent(registerPasswordField);
            registerPasswordField.setNextFocusableComponent(primaryButton);
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

            statusLabel = new JLabel("默认连接本机服务器，跨电脑时再打开高级设置。");
            statusLabel.setFont(UI_FONT_SMALL);
            statusLabel.setForeground(MUTED);

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
            statusLabel.setForeground(MUTED);
            statusLabel.setText("默认连接本机服务器，跨电脑时再打开高级设置。");
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
        private boolean micOn = true;
        private volatile boolean audioRunning = false;
        private volatile boolean playbackRunning = false;
        private volatile boolean videoRunning = false;
        private long connectedAt = 0L;
        private JLabel statusLabel;
        private JLabel timerLabel;
        private VideoSurface remoteSurface;
        private VideoSurface localSurface;
        private JButton micButton;
        private JButton hangupButton;
        private TargetDataLine callMicLine;
        private SourceDataLine callSpeakerLine;
        private Thread audioSendThread;
        private Thread audioPlaybackThread;
        private Thread videoSendThread;
        private LinkedList<AudioChunk> audioQueue = new LinkedList<AudioChunk>();
        private String playbackFormatKey = "";
        private javax.swing.Timer frameTimer;
        private javax.swing.Timer callTimer;

        class AudioChunk {
            String formatKey;
            byte[] data;

            AudioChunk(String formatKey, byte[] data) {
                this.formatKey = formatKey;
                this.data = data;
            }
        }

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
            micButton = createCallButton("静音", new Color(62, 62, 62));
            hangupButton = createCallButton("挂断", new Color(224, 68, 68));
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
            if(statusLabel != null) statusLabel.setText("正在通话 · 摄像头和麦克风已连接");
            if(remoteSurface != null) remoteSurface.setWaiting(false);
            startCallTimer();
            startAudioMedia();
            startVideoSender();
        }

        void finishFromRemote(String text) {
            if(finished) return;
            finished = true;
            connected = false;
            if(statusLabel != null) statusLabel.setText(text);
            stopCallMedia();
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

        private void toggleMic() {
            micOn = !micOn;
            micButton.setText(micOn ? "静音" : "取消静音");
            if(statusLabel != null && connected) statusLabel.setText(micOn ? "正在通话 · 麦克风已开启" : "正在通话 · 麦克风已静音");
        }

        private AudioFormat[] callAudioFormats() {
            return new AudioFormat[] {
                    new AudioFormat(16000.0f, 16, 1, true, false),
                    new AudioFormat(8000.0f, 16, 1, true, false),
                    new AudioFormat(44100.0f, 16, 1, true, false),
                    new AudioFormat(48000.0f, 16, 1, true, false)
            };
        }

        private AudioFormat callAudioFormat() {
            return callAudioFormats()[0];
        }

        private AudioFormat chooseSupportedAudioFormat(Class<?> lineClass) {
            AudioFormat[] formats = callAudioFormats();
            for(int i=0;i<formats.length;i++) {
                DataLine.Info info = new DataLine.Info(lineClass, formats[i]);
                if(AudioSystem.isLineSupported(info)) return formats[i];
            }
            return null;
        }

        private String audioFormatKey(AudioFormat format) {
            return String.valueOf(Math.round(format.getSampleRate()));
        }

        private AudioFormat audioFormatForKey(String key) {
            try {
                int rate = Integer.parseInt(key);
                if(rate >= 8000 && rate <= 48000) {
                    return new AudioFormat((float)rate, 16, 1, true, false);
                }
            } catch(Exception e) {
            }
            return callAudioFormat();
        }

        private void startAudioMedia() {
            startAudioSender();
            startAudioPlayback();
        }

        private void startAudioSender() {
            if(audioRunning) return;
            audioRunning = true;
            audioSendThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        AudioFormat format = chooseSupportedAudioFormat(TargetDataLine.class);
                        if(format == null) {
                            setCallStatusLater("当前系统没有可用的麦克风输入格式");
                            audioRunning = false;
                            return;
                        }
                        String formatKey = audioFormatKey(format);
                        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                        callMicLine = (TargetDataLine)AudioSystem.getLine(info);
                        callMicLine.open(format);
                        callMicLine.start();
                        int chunkBytes = Math.max(CALL_AUDIO_CHUNK_BYTES,
                                (int)(format.getFrameRate() * format.getFrameSize() / 8));
                        int frameSize = Math.max(1, format.getFrameSize());
                        chunkBytes = Math.max(frameSize, chunkBytes - (chunkBytes % frameSize));
                        byte[] buffer = new byte[chunkBytes];
                        while(audioRunning && !finished) {
                            int read = callMicLine.read(buffer, 0, buffer.length);
                            if(read > 0 && micOn && connected) {
                                byte[] chunk = Arrays.copyOf(buffer, read);
                                sendVideoMedia(peer, VIDEO_AUDIO, formatKey + "|"
                                        + Base64.getEncoder().encodeToString(chunk));
                            }
                        }
                    } catch(Exception e) {
                        if(audioRunning && !finished) setCallStatusLater("麦克风不可用：" + e.getMessage());
                    } finally {
                        closeMicLine();
                        audioRunning = false;
                    }
                }
            }, "call-audio-send-" + peer);
            audioSendThread.setDaemon(true);
            audioSendThread.start();
        }

        private void startAudioPlayback() {
            if(playbackRunning) return;
            playbackRunning = true;
            audioPlaybackThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        while(playbackRunning && !finished) {
                            AudioChunk chunk = nextAudioChunk();
                            if(chunk != null && chunk.data != null && chunk.data.length > 0) {
                                try {
                                    ensureSpeakerLine(chunk.formatKey);
                                    if(callSpeakerLine != null) {
                                        callSpeakerLine.write(chunk.data, 0, chunk.data.length);
                                    }
                                } catch(Exception e) {
                                    closeSpeakerLine(false);
                                    if(playbackRunning && !finished) {
                                        setCallStatusLater("扬声器无法播放对方音频：" + e.getMessage());
                                        try {
                                            Thread.sleep(120);
                                        } catch(InterruptedException interrupted) {
                                            Thread.currentThread().interrupt();
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    } finally {
                        closeSpeakerLine(false);
                        playbackRunning = false;
                    }
                }
            }, "call-audio-playback-" + peer);
            audioPlaybackThread.setDaemon(true);
            audioPlaybackThread.start();
        }

        private AudioChunk nextAudioChunk() {
            synchronized(audioQueue) {
                while(playbackRunning && audioQueue.isEmpty() && !finished) {
                    try {
                        audioQueue.wait(250);
                    } catch(InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }
                return audioQueue.isEmpty() ? null : audioQueue.removeFirst();
            }
        }

        void receiveAudioFrame(String encoded) {
            if(encoded == null || encoded.length() == 0 || finished) return;
            try {
                String formatKey = audioFormatKey(callAudioFormat());
                String payload = encoded;
                int split = encoded.indexOf('|');
                if(split > 0) {
                    formatKey = encoded.substring(0, split);
                    payload = encoded.substring(split + 1);
                }
                byte[] chunk = Base64.getDecoder().decode(payload);
                synchronized(audioQueue) {
                    while(audioQueue.size() > CALL_AUDIO_QUEUE_LIMIT) audioQueue.removeFirst();
                    audioQueue.addLast(new AudioChunk(formatKey, chunk));
                    audioQueue.notifyAll();
                }
            } catch(Exception e) {
            }
        }

        private void ensureSpeakerLine(String formatKey) throws LineUnavailableException {
            if(formatKey == null || formatKey.length() == 0) formatKey = audioFormatKey(callAudioFormat());
            if(callSpeakerLine != null && callSpeakerLine.isOpen() && formatKey.equals(playbackFormatKey)) return;
            closeSpeakerLine(false);
            AudioFormat format = audioFormatForKey(formatKey);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            if(!AudioSystem.isLineSupported(info)) {
                throw new LineUnavailableException("不支持 " + formatKey + "Hz PCM");
            }
            callSpeakerLine = (SourceDataLine)AudioSystem.getLine(info);
            callSpeakerLine.open(format);
            callSpeakerLine.start();
            playbackFormatKey = formatKey;
        }

        private void closeMicLine() {
            TargetDataLine line = callMicLine;
            callMicLine = null;
            try {
                if(line != null) {
                    line.stop();
                    line.close();
                }
            } catch(Exception e) {
            }
        }

        private void closeSpeakerLine(boolean drain) {
            SourceDataLine line = callSpeakerLine;
            callSpeakerLine = null;
            playbackFormatKey = "";
            try {
                if(line != null) {
                    if(drain) line.drain();
                    else line.flush();
                    line.stop();
                    line.close();
                }
            } catch(Exception e) {
            }
        }

        private void stopAudioMedia() {
            audioRunning = false;
            playbackRunning = false;
            closeMicLine();
            closeSpeakerLine(false);
            if(audioSendThread != null) {
                audioSendThread.interrupt();
                audioSendThread = null;
            }
            if(audioPlaybackThread != null) {
                audioPlaybackThread.interrupt();
                audioPlaybackThread = null;
            }
            synchronized(audioQueue) {
                audioQueue.clear();
                audioQueue.notifyAll();
            }
        }

        private void startVideoSender() {
            if(videoRunning) return;
            videoRunning = true;
            if(statusLabel != null) statusLabel.setText("正在通话 · 摄像头已开启");
            videoSendThread = new Thread(new Runnable() {
                public void run() {
                    Webcam webcam = null;
                    try {
                        System.out.println("[VIDEO] Starting webcam capture...");
                        webcam = Webcam.getDefault();
                        if(webcam == null) {
                            System.out.println("[VIDEO] No webcam detected");
                            setCallStatusLater("未检测到摄像头");
                            return;
                        }
                        System.out.println("[VIDEO] Webcam found: " + webcam.getName());
                        Dimension[] resolutions = webcam.getViewSizes();
                        System.out.println("[VIDEO] Available resolutions: " + Arrays.toString(resolutions));
                        Dimension targetSize = resolutions[resolutions.length - 1];
                        for(Dimension d : resolutions) {
                            if(d.width <= CALL_VIDEO_WIDTH && d.height <= CALL_VIDEO_HEIGHT) {
                                if(d.width * d.height > targetSize.width * targetSize.height) {
                                    targetSize = d;
                                }
                            }
                        }
                        System.out.println("[VIDEO] Selected resolution: " + targetSize);
                        webcam.setViewSize(targetSize);
                        System.out.println("[VIDEO] Opening webcam with size: " + targetSize);
                        webcam.open();
                        System.out.println("[VIDEO] Webcam opened successfully");
                        int frameCount = 0;
                        while(videoRunning && !finished && connected) {
                            BufferedImage image = webcam.getImage();
                            if(image != null) {
                                BufferedImage scaled = scaleImage(image, CALL_VIDEO_WIDTH, CALL_VIDEO_HEIGHT);
                                if(localSurface != null) localSurface.setFrame(scaled);
                                String encoded = encodeImage(scaled);
                                if(encoded.length() > 0) sendVideoMedia(peer, VIDEO_FRAME, encoded);
                                frameCount++;
                                if(frameCount % 10 == 0) {
                                    System.out.println("[VIDEO] Sent " + frameCount + " frames");
                                }
                            } else {
                                System.out.println("[VIDEO] Image is null");
                            }
                            try {
                                Thread.sleep(CALL_VIDEO_INTERVAL_MS);
                            } catch(InterruptedException e) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                        System.out.println("[VIDEO] Loop exited - videoRunning=" + videoRunning + ", finished=" + finished + ", connected=" + connected);
                    } catch(Exception e) {
                        System.out.println("[VIDEO] Error: " + e.getMessage());
                        e.printStackTrace();
                        setCallStatusLater("摄像头启动失败：" + e.getMessage());
                    } finally {
                        System.out.println("[VIDEO] Cleaning up");
                        if(webcam != null && webcam.isOpen()) {
                            try { webcam.close(); } catch(Exception e) {}
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                videoRunning = false;
                                videoSendThread = null;
                                if(localSurface != null) localSurface.clearFrame();
                            }
                        });
                    }
                }
            }, "call-video-send-" + peer);
            videoSendThread.setDaemon(true);
            videoSendThread.start();
        }

        private void stopVideoSender() {
            videoRunning = false;
            if(videoSendThread != null) videoSendThread.interrupt();
            videoSendThread = null;
            if(localSurface != null) localSurface.clearFrame();
        }

        void receiveVideoFrame(String encoded) {
            if(encoded == null || encoded.length() == 0 || finished) return;
            try {
                byte[] bytes = Base64.getDecoder().decode(encoded);
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
                if(image != null && remoteSurface != null) {
                    remoteSurface.setFrame(image);
                    if(statusLabel != null && connected) statusLabel.setText("正在通话 · 正在接收对方画面");
                }
            } catch(Exception e) {
            }
        }

        private BufferedImage scaleImage(BufferedImage source, int maxWidth, int maxHeight) {
            double scale = Math.min(maxWidth / (double)source.getWidth(),
                    maxHeight / (double)source.getHeight());
            int width = Math.max(1, (int)Math.round(source.getWidth() * scale));
            int height = Math.max(1, (int)Math.round(source.getHeight() * scale));
            BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = scaled.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(source, 0, 0, width, height, null);
            g2.dispose();
            return scaled;
        }

        private String encodeImage(BufferedImage image) {
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ImageIO.write(image, "jpg", out);
                return Base64.getEncoder().encodeToString(out.toByteArray());
            } catch(Exception e) {
                return "";
            }
        }

        private void setCallStatusLater(final String text) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if(statusLabel != null && !finished) statusLabel.setText(text);
                }
            });
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
            if(micButton != null) micButton.setEnabled(false);
            if(hangupButton != null) hangupButton.setEnabled(false);
        }

        private void stopCallMedia() {
            stopVideoSender();
            stopAudioMedia();
            if(remoteSurface != null) remoteSurface.clearFrame();
        }

        private void disposeWindow() {
            stopCallMedia();
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
        private BufferedImage frameImage;
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

        void setFrame(final BufferedImage image) {
            if(SwingUtilities.isEventDispatchThread()) {
                frameImage = image;
                repaint();
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        frameImage = image;
                        repaint();
                    }
                });
            }
        }

        void clearFrame() {
            if(SwingUtilities.isEventDispatchThread()) {
                frameImage = null;
                repaint();
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        frameImage = null;
                        repaint();
                    }
                });
            }
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
            BufferedImage image = frameImage;
            if(image != null) paintFrame(g2, image);
            else if(cameraOn) paintVideo(g2);
            else paintCameraOff(g2);
            paintOverlay(g2);
            g2.dispose();
        }

        private void paintFrame(Graphics2D g2, BufferedImage image) {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, getWidth(), getHeight());
            double scale = Math.min(getWidth() / (double)image.getWidth(),
                    getHeight() / (double)image.getHeight());
            int width = Math.max(1, (int)Math.round(image.getWidth() * scale));
            int height = Math.max(1, (int)Math.round(image.getHeight() * scale));
            int x = (getWidth() - width) / 2;
            int y = (getHeight() - height) / 2;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(image, x, y, width, height, null);
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

            if(!local && frameImage != null && !waiting) {
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

    class BadgeButton extends StyledButton {
        private int badgeCount = 0;

        BadgeButton(String text, boolean primary) {
            super(text, primary);
        }

        void setBadgeCount(int count) {
            this.badgeCount = Math.max(0, count);
            repaint();
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if(badgeCount <= 0) return;
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            String badgeText = badgeCount > 99 ? "99+" : String.valueOf(badgeCount);
            g2.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 10));
            FontMetrics fm = g2.getFontMetrics();
            int h = 18;
            int w = badgeText.length() == 1 ? h : Math.max(h, fm.stringWidth(badgeText) + 10);
            int x = getWidth() - w - 2;
            int y = -1;
            if(x < 0) x = 0;
            g2.setColor(new Color(250, 81, 81));
            g2.fillRoundRect(x, y, w, h, h, h);
            g2.setColor(Color.WHITE);
            g2.drawString(badgeText, x + (w - fm.stringWidth(badgeText)) / 2, y + (h + fm.getAscent() - fm.getDescent()) / 2 - 1);
            g2.dispose();
        }
    }

    class RailButton extends JButton {
        private boolean hover = false;
        private int badgeCount = 0;

        void setBadgeCount(int count) {
            this.badgeCount = Math.max(0, count);
            repaint();
        }

        RailButton(String text) {
            super(text);
            setFont(new Font("Microsoft YaHei UI", Font.BOLD, 12));
            setForeground(new Color(74, 80, 92));
            setFocusPainted(false);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorder(pad(0));
            setPreferredSize(new Dimension(58, 42));
            setMinimumSize(new Dimension(58, 42));
            setMaximumSize(new Dimension(58, 42));
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
            if(badgeCount > 0) {
                String badgeText = badgeCount > 99 ? "99+" : String.valueOf(badgeCount);
                g2.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 10));
                FontMetrics fm = g2.getFontMetrics();
                int h = 18;
                int w = badgeText.length() == 1 ? h : Math.max(h, fm.stringWidth(badgeText) + 10);
                int x = getWidth() - w - 4;
                int y = 3;
                g2.setColor(new Color(250, 81, 81));
                g2.fillRoundRect(x, y, w, h, h, h);
                g2.setColor(Color.WHITE);
                g2.drawString(badgeText, x + (w - fm.stringWidth(badgeText)) / 2, y + (h + fm.getAscent() - fm.getDescent()) / 2 - 1);
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

    private void toggleVoicePlayback(ChatMessage message, JButton button,
            JLabel statusLabel, VoiceWaveform waveform, String idleText) {
        if(message == null || message.fileData == null || message.fileData.length == 0) return;

        VoicePlayback previous = null;
        VoicePlayback next = null;
        synchronized(this) {
            if(activeVoicePlayback != null && activeVoicePlayback.isFor(message)) {
                previous = activeVoicePlayback;
                activeVoicePlayback = null;
            } else {
                previous = activeVoicePlayback;
                next = new VoicePlayback(message, button, statusLabel, waveform, idleText);
                activeVoicePlayback = next;
            }
        }
        if(previous != null) previous.stopPlayback();
        if(next != null) next.start();
    }

    private synchronized boolean clearActiveVoicePlayback(VoicePlayback playback) {
        if(activeVoicePlayback == playback) {
            activeVoicePlayback = null;
            return true;
        }
        return false;
    }

    private String voiceIdleText(ChatMessage message, boolean outgoing) {
        String duration = voiceDurationText(message.fileData);
        String prefix = duration.length() == 0 ? "" : duration + " · ";
        return prefix + displayFileSize(message.fileSize) + " · "
                + (outgoing ? "已发送，可播放" : "点击播放，可另存");
    }

    private String voiceDurationText(byte[] data) {
        int seconds = wavDurationSeconds(data);
        return seconds <= 0 ? "" : formatDuration(seconds);
    }

    private String formatDuration(int seconds) {
        seconds = Math.max(0, seconds);
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    private int wavDurationSeconds(byte[] data) {
        if(data == null || data.length < 44) return -1;
        if(!asciiEquals(data, 0, "RIFF") || !asciiEquals(data, 8, "WAVE")) return -1;
        int byteRate = 0;
        int dataSize = 0;
        int offset = 12;
        while(offset + 8 <= data.length) {
            String chunkId = new String(data, offset, 4, StandardCharsets.US_ASCII);
            int chunkSize = littleEndianInt(data, offset + 4);
            if(chunkSize < 0) break;
            int chunkData = offset + 8;
            if("fmt ".equals(chunkId) && chunkData + 16 <= data.length) {
                byteRate = littleEndianInt(data, chunkData + 8);
            } else if("data".equals(chunkId)) {
                dataSize = Math.min(chunkSize, data.length - chunkData);
                break;
            }
            offset = chunkData + chunkSize + (chunkSize % 2);
        }
        if(byteRate <= 0 || dataSize <= 0) return -1;
        return Math.max(1, (int)Math.round(dataSize / (double)byteRate));
    }

    private boolean asciiEquals(byte[] data, int offset, String value) {
        if(data == null || offset < 0 || offset + value.length() > data.length) return false;
        for(int i=0;i<value.length();i++) {
            if((byte)value.charAt(i) != data[offset + i]) return false;
        }
        return true;
    }

    private int littleEndianInt(byte[] data, int offset) {
        if(data == null || offset < 0 || offset + 4 > data.length) return -1;
        return (data[offset] & 0xff)
                | ((data[offset + 1] & 0xff) << 8)
                | ((data[offset + 2] & 0xff) << 16)
                | ((data[offset + 3] & 0xff) << 24);
    }

    private AudioFormat playbackFormat(AudioFormat source) {
        int channels = source.getChannels() <= 0 ? 1 : source.getChannels();
        float sampleRate = source.getSampleRate() <= 0 ? 16000.0f : source.getSampleRate();
        int sampleSize = source.getSampleSizeInBits() <= 0 ? 16 : source.getSampleSizeInBits();
        boolean pcmSigned = AudioFormat.Encoding.PCM_SIGNED.equals(source.getEncoding());
        if(pcmSigned && sampleSize == 16 && !source.isBigEndian()) return source;
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                sampleRate, 16, channels, channels * 2, sampleRate, false);
    }

    private String audioFileExtension(String name) {
        String value = name == null ? "" : name.trim();
        int dot = value.lastIndexOf('.');
        if(dot < 0 || dot == value.length() - 1) return ".audio";
        String extension = value.substring(dot).replaceAll("[^A-Za-z0-9.]", "");
        if(extension.length() < 2 || extension.length() > 12) return ".audio";
        return extension;
    }

    private boolean openVoiceInSystemPlayer(ChatMessage message) {
        try {
            if(!Desktop.isDesktopSupported()
                    || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) return false;
            Path dir = currentUserDir != null
                    ? currentUserDir.resolve("voice-playback")
                    : Paths.get(System.getProperty("java.io.tmpdir"), "cncd-chat-voice-playback");
            Files.createDirectories(dir);
            Path temp = Files.createTempFile(dir, "voice_", audioFileExtension(message.fileName));
            Files.write(temp, message.fileData);
            temp.toFile().deleteOnExit();
            Desktop.getDesktop().open(temp.toFile());
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    class VoicePlayback {
        private ChatMessage message;
        private JButton button;
        private JLabel statusLabel;
        private VoiceWaveform waveform;
        private String idleText;
        private volatile boolean stopped = false;
        private volatile SourceDataLine line;
        private Thread thread;

        VoicePlayback(ChatMessage message, JButton button, JLabel statusLabel,
                VoiceWaveform waveform, String idleText) {
            this.message = message;
            this.button = button;
            this.statusLabel = statusLabel;
            this.waveform = waveform;
            this.idleText = idleText;
        }

        boolean isFor(ChatMessage other) {
            return message == other;
        }

        void start() {
            setUi("停止", "播放中 · " + idleText, true);
            thread = new Thread(new Runnable() {
                public void run() {
                    play();
                }
            }, "voice-playback-" + safeFileName(message.fileName));
            thread.setDaemon(true);
            thread.start();
        }

        void stopPlayback() {
            stopped = true;
            SourceDataLine currentLine = line;
            if(currentLine != null) {
                try {
                    currentLine.stop();
                    currentLine.close();
                } catch(Exception e) {
                }
            }
            if(thread != null) thread.interrupt();
            setUi("播放", "已停止 · " + idleText, false);
        }

        private void play() {
            String doneText = null;
            String errorText = null;
            try {
                streamWithJavaSound();
                if(!stopped) doneText = "播放完成 · " + idleText;
            } catch(UnsupportedAudioFileException e) {
                if(!stopped) {
                    if(openVoiceInSystemPlayer(message)) doneText = "已用系统播放器打开 · " + idleText;
                    else errorText = "当前格式不支持直接播放，请另存后打开";
                }
            } catch(Exception e) {
                if(!stopped) errorText = "播放失败：" + e.getMessage();
            } finally {
                SourceDataLine currentLine = line;
                line = null;
                try {
                    if(currentLine != null) {
                        if(!stopped) currentLine.drain();
                        currentLine.stop();
                        currentLine.close();
                    }
                } catch(Exception e) {
                }
                clearActiveVoicePlayback(this);
                if(!stopped) {
                    setUi("播放", errorText == null ? doneText : errorText, false);
                }
            }
        }

        private void streamWithJavaSound() throws Exception {
            BufferedInputStream input = new BufferedInputStream(new ByteArrayInputStream(message.fileData));
            AudioInputStream original = AudioSystem.getAudioInputStream(input);
            AudioInputStream stream = original;
            try {
                AudioFormat targetFormat = playbackFormat(original.getFormat());
                if(!targetFormat.matches(original.getFormat())) {
                    if(!AudioSystem.isConversionSupported(targetFormat, original.getFormat())) {
                        throw new UnsupportedAudioFileException("不支持的音频编码");
                    }
                    stream = AudioSystem.getAudioInputStream(targetFormat, original);
                }
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, targetFormat);
                if(!AudioSystem.isLineSupported(info)) {
                    throw new LineUnavailableException("系统没有可用的播放设备");
                }
                SourceDataLine output = (SourceDataLine)AudioSystem.getLine(info);
                line = output;
                output.open(targetFormat);
                output.start();
                byte[] buffer = new byte[4096];
                int read;
                while(!stopped && (read = stream.read(buffer, 0, buffer.length)) != -1) {
                    output.write(buffer, 0, read);
                }
            } finally {
                try {
                    stream.close();
                } catch(Exception e) {
                }
                if(stream != original) {
                    try {
                        original.close();
                    } catch(Exception e) {
                    }
                }
            }
        }

        private void setUi(final String buttonText, final String statusText, final boolean playing) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if(button != null) button.setText(buttonText);
                    if(statusLabel != null && statusText != null) statusLabel.setText(statusText);
                    if(waveform != null) waveform.setPlaying(playing);
                }
            });
        }
    }

    class VoiceWaveform extends JComponent {
        private boolean playing = false;
        private int phase = 0;
        private javax.swing.Timer timer;

        VoiceWaveform() {
            setPreferredSize(new Dimension(118, 30));
            setMinimumSize(new Dimension(90, 30));
        }

        void setPlaying(boolean playing) {
            this.playing = playing;
            if(playing) {
                if(timer == null) {
                    timer = new javax.swing.Timer(90, new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            phase++;
                            repaint();
                        }
                    });
                }
                if(!timer.isRunning()) timer.start();
            } else if(timer != null) {
                timer.stop();
            }
            repaint();
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int bars = 16;
            int gap = 4;
            int barWidth = 3;
            int totalWidth = bars * barWidth + (bars - 1) * gap;
            int startX = Math.max(0, (getWidth() - totalWidth) / 2);
            int centerY = getHeight() / 2;
            for(int i=0;i<bars;i++) {
                double wave = Math.sin((i + phase) * 0.65);
                int base = 8 + (i % 4) * 3;
                int height = playing ? base + (int)Math.round((wave + 1) * 5) : base;
                g2.setColor(playing ? PRIMARY_DARK : MUTED);
                g2.fillRoundRect(startX + i * (barWidth + gap), centerY - height / 2,
                        barWidth, height, barWidth, barWidth);
            }
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
            setMinimumSize(new Dimension(0, 20));
            setPreferredSize(new Dimension(0, 20));
        }

        void setCount(int count) {
            this.count = Math.max(0, count);
            revalidate();
            repaint();
        }

        public Dimension getPreferredSize() {
            if(count <= 0) return new Dimension(0, 20);
            String text = count > 99 ? "99+" : String.valueOf(count);
            FontMetrics fm = getFontMetrics(getFont());
            int diameter = text.length() == 1 ? 20 : Math.max(24, fm.stringWidth(text) + 10);
            return new Dimension(diameter, diameter);
        }

        protected void paintComponent(Graphics g) {
            if(count <= 0) return;
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            String text = count > 99 ? "99+" : String.valueOf(count);
            Dimension preferred = getPreferredSize();
            int diameter = Math.min(Math.min(preferred.width, preferred.height), Math.min(getWidth(), getHeight()));
            if(diameter <= 0) {
                g2.dispose();
                return;
            }
            int x0 = (getWidth() - diameter) / 2;
            int y0 = (getHeight() - diameter) / 2;
            g2.setColor(new Color(250, 81, 81));
            g2.fillOval(x0, y0, diameter, diameter);
            g2.setColor(Color.WHITE);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int x = x0 + (diameter - fm.stringWidth(text)) / 2;
            int y = y0 + (diameter + fm.getAscent() - fm.getDescent()) / 2 - 1;
            g2.drawString(text, x, y);
            g2.dispose();
        }
    }

    class ConversationRenderer extends JPanel implements ListCellRenderer<String> {
        private AvatarView avatar = new AvatarView("?", false);
        private JLabel nameLabel = new JLabel();
        private JLabel previewLabel = new JLabel();
        private JLabel timeLabel = new JLabel();
        private JLabel categoryLabel = new JLabel();
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
            categoryLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
            categoryLabel.setBorder(pad(1, 7, 1, 7));
            stateDot.setFont(new Font("Dialog", Font.BOLD, 9));
            stateDot.setBorder(pad(0, 0, 0, 5));

            JPanel namePack = new JPanel();
            namePack.setOpaque(false);
            namePack.setLayout(new BoxLayout(namePack, BoxLayout.X_AXIS));
            namePack.add(stateDot);
            namePack.add(nameLabel);

            JPanel metaPack = new JPanel(new FlowLayout(FlowLayout.RIGHT, SPACE_XS, 0));
            metaPack.setOpaque(false);
            metaPack.add(categoryLabel);
            metaPack.add(timeLabel);

            titleRow.setOpaque(false);
            titleRow.add(namePack, BorderLayout.CENTER);
            titleRow.add(metaPack, BorderLayout.EAST);
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
            categoryLabel.setText(conversationCategoryText(name));
            categoryLabel.setForeground(conversationStateColor(name));
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

        private String conversationCategoryText(String name) {
            if(BROADCAST_CHAT.equals(name)) return "广播";
            if(isGroupConversation(name)) return "群聊";
            if(sentFriendRequests.contains(name)) return "待验证";
            if(friends.contains(name)) return visibleUsers.contains(name) ? "好友" : "离线";
            return visibleUsers.contains(name) ? "在线" : "离线";
        }

        private String displayConversationName(String name) {
            if(BROADCAST_CHAT.equals(name)) return BROADCAST_CHAT;
            if(isGroupConversation(name)) return groupNameFromLabel(name);
            return displayNameWithRemark(name);
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
            if(message == null) return;
            String key = storeConversationMessage(message);
            if(isSelectedConversation(key)) {
                if(isMessageSearchActive()) renderConversationMessages(key);
                else appendMessageRow(message, true);
            }
            updateConversationPreview(message);
        }

        public void showMessages(java.util.List<ChatMessage> messages, String emptyTitle, String emptyText) {
            removeAll();
            showingEmptyState = false;
            if(messages == null || messages.size() == 0) {
                showEmptyState(emptyTitle, emptyText);
                revalidate();
                repaint();
                return;
            }
            for(int i=0;i<messages.size();i++) {
                appendMessageRow(messages.get(i), false);
            }
            revalidate();
            repaint();
            if(currentMessageSearchMatchId() != null) scrollToCurrentSearchMatch();
            else if(!isMessageSearchActive()) scrollToBottom();
        }

        private void appendMessageRow(ChatMessage message, boolean refresh) {
            if(showingEmptyState) {
                removeAll();
                showingEmptyState = false;
            }
            Component row = message.kind == MessageKind.SYSTEM
                    ? createSystemRow(message)
                    : createChatRow(message);
            add(decorateSearchRow(message, row));
            add(Box.createVerticalStrut(6));
            if(refresh) {
                revalidate();
                repaint();
                if(currentMessageSearchMatchId() != null) scrollToCurrentSearchMatch();
                else scrollToBottom();
            }
        }

        private Component decorateSearchRow(ChatMessage message, Component row) {
            if(!isMessageSearchMatch(message)) return row;
            boolean current = isCurrentMessageSearchMatch(message);
            Color fill = current ? new Color(255, 244, 201) : new Color(255, 250, 229);
            Color stroke = current ? WARNING : new Color(240, 213, 120);
            JPanel wrapper = new BubblePanel(fill, stroke, RADIUS_LG);
            wrapper.setLayout(new BorderLayout());
            wrapper.setBorder(pad(current ? 6 : 4, 8, current ? 6 : 4, 8));
            wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
            wrapper.putClientProperty("messageSearchId", message.localMessageId);
            wrapper.add(row, BorderLayout.CENTER);
            wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, wrapper.getPreferredSize().height));
            return wrapper;
        }

        private void scrollToBottom() {
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

        private void scrollToCurrentSearchMatch() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    String currentId = currentMessageSearchMatchId();
                    if(currentId == null) return;
                    Component target = findSearchComponent(ClientHistory.this, currentId);
                    if(target == null) return;
                    Rectangle bounds = SwingUtilities.convertRectangle(target.getParent(), target.getBounds(), ClientHistory.this);
                    bounds.grow(0, 12);
                    scrollRectToVisible(bounds);
                }
            });
        }

        private Component findSearchComponent(Container root, String id) {
            Component[] children = root.getComponents();
            for(int i=0;i<children.length;i++) {
                Component child = children[i];
                if(child instanceof JComponent) {
                    Object value = ((JComponent)child).getClientProperty("messageSearchId");
                    if(id.equals(value)) return child;
                }
                if(child instanceof Container) {
                    Component nested = findSearchComponent((Container)child, id);
                    if(nested != null) return nested;
                }
            }
            return null;
        }

        public void addInlineComponent(Component component) {
            if(component == null) return;
            if(showingEmptyState) {
                removeAll();
                showingEmptyState = false;
            }
            JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.add(component);
            add(row);
            add(Box.createVerticalStrut(8));
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

            if(message.recalled) {
                JLabel recalled = new JLabel(message.body == null ? "已撤回一条消息" : message.body);
                recalled.setFont(CHAT_TEXT_SMALL_FONT);
                recalled.setForeground(MUTED);
                recalled.setAlignmentX(outgoing ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
                block.add(recalled);
                return block;
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

            if(outgoing) installRecallPopup(bubble, message);

            block.add(name);
            block.add(Box.createVerticalStrut(SPACE_XS));
            block.add(bubble);
            if(outgoing && isFailureDeliveryStatus(message.deliveryStatus)) {
                JLabel fail = new JLabel(message.deliveryStatus);
                fail.setFont(CHAT_TEXT_SMALL_FONT);
                fail.setForeground(DANGER);
                fail.setAlignmentX(Component.RIGHT_ALIGNMENT);
                block.add(Box.createVerticalStrut(3));
                block.add(fail);
            }
            block.setMaximumSize(new Dimension(470, block.getPreferredSize().height));
            return block;
        }

        private void installRecallPopup(final JComponent component, final ChatMessage message) {
            component.addMouseListener(new MouseAdapter() {
                private void showMenu(MouseEvent e) {
                    if(!e.isPopupTrigger()) return;
                    JPopupMenu menu = new JPopupMenu();
                    JMenuItem recall = new JMenuItem("撤回");
                    recall.setEnabled(canRecallMessage(message));
                    recall.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent ae) { recallOwnMessage(message); }
                    });
                    menu.add(recall);
                    menu.show(component, e.getX(), e.getY());
                }
                public void mousePressed(MouseEvent e) { showMenu(e); }
                public void mouseReleased(MouseEvent e) { showMenu(e); }
            });
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
            if(isAudioFile(message.fileName) && message.fileData != null) {
                return createVoiceBubbleBlock(message, outgoing, name);
            }
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

        private Component createVoiceBubbleBlock(final ChatMessage message, final boolean outgoing, JLabel name) {
            JPanel block = new JPanel();
            block.setOpaque(false);
            block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));

            final BubblePanel card = new BubblePanel(outgoing ? OUTGOING_BUBBLE : INCOMING_BUBBLE,
                    outgoing ? new Color(125, 211, 88) : BORDER_LIGHT,
                    RADIUS_LG);
            card.setLayout(new BorderLayout(SPACE_MD, 0));
            card.setBorder(pad(11, 12, 11, 12));
            int cardWidth = availableBubbleWidth(300, 380);
            card.setMaximumSize(new Dimension(cardWidth, 88));
            card.setPreferredSize(new Dimension(cardWidth, 88));
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            card.setToolTipText("点击播放语音");

            final JButton playButton = createVoiceBubbleButton("播放", outgoing);
            final VoiceWaveform waveform = new VoiceWaveform();
            final String idleText = voiceIdleText(message, outgoing);
            final JLabel meta = new JLabel(idleText);
            meta.setFont(UI_FONT_SMALL);
            meta.setForeground(outgoing ? PRIMARY_DARK : MUTED);

            JPanel textPanel = new JPanel();
            textPanel.setOpaque(false);
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            JLabel title = new JLabel("语音消息");
            title.setFont(UI_FONT_BOLD);
            title.setForeground(TEXT);
            JLabel fileName = new JLabel(clipFileName(message.fileName));
            fileName.setFont(CHAT_TEXT_SMALL_FONT);
            fileName.setForeground(MUTED);
            textPanel.add(title);
            textPanel.add(Box.createVerticalStrut(2));
            textPanel.add(fileName);
            textPanel.add(Box.createVerticalStrut(SPACE_XS));
            textPanel.add(meta);

            JPanel center = new JPanel(new BorderLayout(SPACE_SM, 0));
            center.setOpaque(false);
            center.add(waveform, BorderLayout.WEST);
            center.add(textPanel, BorderLayout.CENTER);

            card.add(playButton, BorderLayout.WEST);
            card.add(center, BorderLayout.CENTER);

            if(!outgoing) {
                JButton saveButton = createVoiceBubbleButton("另存", false);
                saveButton.setPreferredSize(new Dimension(58, 32));
                saveButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        saveReceivedFile(message, null);
                        if(message.savedFile != null) {
                            meta.setText("已保存 · " + idleText);
                            meta.setForeground(SUCCESS);
                        }
                    }
                });
                card.add(saveButton, BorderLayout.EAST);
            }

            ActionListener playAction = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    toggleVoicePlayback(message, playButton, meta, waveform, idleText);
                }
            };
            playButton.addActionListener(playAction);
            card.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    toggleVoicePlayback(message, playButton, meta, waveform, idleText);
                }
            });

            card.setAlignmentX(outgoing ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
            block.add(name);
            block.add(Box.createVerticalStrut(SPACE_XS));
            block.add(card);
            block.setMaximumSize(new Dimension(520, 112));
            return block;
        }

        private JButton createVoiceBubbleButton(final String text, final boolean outgoing) {
            JButton button = new JButton(text) {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color fill = outgoing ? new Color(255, 255, 255, 165) : PRIMARY;
                    Color stroke = outgoing ? new Color(125, 211, 88) : PRIMARY_DARK;
                    if(!isEnabled()) {
                        fill = new Color(232, 237, 245);
                        stroke = BORDER_LIGHT;
                    }
                    g2.setColor(fill);
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIUS_LG, RADIUS_LG);
                    g2.setColor(stroke);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIUS_LG, RADIUS_LG);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            button.setFont(UI_FONT_BOLD);
            button.setForeground(outgoing ? PRIMARY_DARK : Color.WHITE);
            button.setFocusPainted(false);
            button.setOpaque(false);
            button.setContentAreaFilled(false);
            button.setBorder(pad(5, 10, 5, 10));
            button.setPreferredSize(new Dimension(58, 36));
            button.setMinimumSize(new Dimension(58, 36));
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return button;
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
                    + (outgoing ? " · 已发送" : " · 点击预览"));
            meta.setFont(UI_FONT_SMALL);
            meta.setForeground(outgoing ? PRIMARY_DARK : MUTED);
            meta.setBorder(pad(6, 2, 0, 2));
            card.add(meta, BorderLayout.SOUTH);

            Dimension preferred = new Dimension(
                    Math.max(160, imageIcon.getIconWidth() + 18),
                    imageIcon.getIconHeight() + 42);
            card.setPreferredSize(preferred);
            card.setMaximumSize(preferred);

            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            card.setToolTipText("双击预览并缩放图片");
            card.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if(e.getClickCount() >= 2 && SwingUtilities.isLeftMouseButton(e)) {
                        previewImageMessage(message);
                    }
                }
            });

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
            int dot = name.lastIndexOf('.');
            String suffix = dot > 0 && name.length() - dot <= 8 ? name.substring(dot) : "";
            int keepTail = suffix.length() > 0 ? suffix.length() : 7;
            int keepHead = Math.max(6, 17 - keepTail);
            return name.substring(0, Math.min(keepHead, name.length()))
                    + "..." + name.substring(Math.max(0, name.length() - keepTail));
        }

        private void saveReceivedFile(ChatMessage message, JLabel statusLabel) {
            showFileSaveMenu(message, statusLabel, null);
        }

        private void showFileSaveMenu(final ChatMessage message, final JLabel statusLabel, MouseEvent event) {
            Component invoker = event == null ? ChatClient.this : event.getComponent();
            int x = event == null ? Math.max(20, invoker.getWidth() / 2) : event.getX();
            int y = event == null ? Math.max(20, invoker.getHeight() / 2) : event.getY();
            showFileSaveMenu(message, statusLabel, invoker, x, y);
        }

        private void showFileSaveMenu(final ChatMessage message, final JLabel statusLabel,
                final Component invoker, int x, int y) {
            if(message == null || message.fileData == null) return;
            final Component parent = invoker == null ? ChatClient.this : invoker;
            JPopupMenu menu = new JPopupMenu();
            menu.setBorder(new CompoundBorder(new RoundedBorder(BORDER, RADIUS_MD), pad(SPACE_XS)));
            if(message.savedFile != null && message.savedFile.exists()) {
                menu.add(createConversationMenuItem("打开文件", new Runnable() {
                    public void run() { openSavedFile(message.savedFile); }
                }));
                menu.add(createConversationMenuItem("打开所在位置", new Runnable() {
                    public void run() { openContainingFolder(message.savedFile); }
                }));
                menu.addSeparator();
            }
            menu.add(createConversationMenuItem("另存为...", new Runnable() {
                public void run() { saveReceivedFileAs(message, statusLabel, parent); }
            }));
            menu.add(createConversationMenuItem("保存到默认下载目录", new Runnable() {
                public void run() { saveReceivedFileToDefaultDownloads(message, statusLabel); }
            }));
            menu.show(parent, Math.max(0, x), Math.max(0, y));
        }

        private void saveReceivedFileAs(ChatMessage message, JLabel statusLabel) {
            saveReceivedFileAs(message, statusLabel, ChatClient.this);
        }

        private void saveReceivedFileAs(ChatMessage message, JLabel statusLabel, Component parent) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("另存为");
            chooser.setSelectedFile(new File(message.fileName));
            chooser.setCurrentDirectory(new File(System.getProperty("user.home"), "Desktop"));
            Component owner = parent == null ? ChatClient.this : parent;
            if(chooser.showSaveDialog(owner) != JFileChooser.APPROVE_OPTION) return;
            saveFileBytesTo(message, chooser.getSelectedFile(), statusLabel);
        }

        private void saveReceivedFileToDefaultDownloads(ChatMessage message, JLabel statusLabel) {
            try {
                Path dir = Paths.get(System.getProperty("user.home"), "Downloads", "CNCD-Chat");
                Files.createDirectories(dir);
                File target = uniqueFile(dir.resolve(message.fileName).toFile());
                saveFileBytesTo(message, target, statusLabel);
                showInlineNotice("已保存到默认下载目录：" + target.getAbsolutePath(), PRIMARY_DARK);
            } catch(Exception e) {
                JOptionPane.showMessageDialog(ChatClient.this, "保存失败：" + e.getMessage(), "默认下载目录", JOptionPane.ERROR_MESSAGE);
            }
        }

        private File uniqueFile(File target) {
            if(target == null || !target.exists()) return target;
            String name = target.getName();
            String base = name;
            String ext = "";
            int dot = name.lastIndexOf('.');
            if(dot > 0) { base = name.substring(0, dot); ext = name.substring(dot); }
            File parent = target.getParentFile();
            for(int i=1;i<1000;i++) {
                File candidate = new File(parent, base + "(" + i + ")" + ext);
                if(!candidate.exists()) return candidate;
            }
            return target;
        }

        private void saveFileBytesTo(ChatMessage message, File selected, JLabel statusLabel) {
            if(selected == null) return;
            if(selected.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(ChatClient.this,
                        "文件已存在，是否覆盖？\n" + selected.getAbsolutePath(),
                        "确认覆盖", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if(overwrite != JOptionPane.YES_OPTION) return;
            }
            try {
                Files.write(selected.toPath(), message.fileData);
                message.savedFile = selected;
                if(statusLabel != null) { statusLabel.setText(fileStatusText(message, false)); statusLabel.setForeground(SUCCESS); }
                appendLog("[file saved] " + selected.getAbsolutePath());
            } catch(Exception e) {
                JOptionPane.showMessageDialog(ChatClient.this, "保存失败：" + e.getMessage(), "保存文件", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void openSavedFile(File file) {
            if(file == null || !file.exists()) return;
            try {
                if(Desktop.isDesktopSupported()) Desktop.getDesktop().open(file);
                else JOptionPane.showMessageDialog(ChatClient.this, "文件已保存到：\n" + file.getAbsolutePath());
            } catch(Exception e) {
                JOptionPane.showMessageDialog(ChatClient.this, "无法打开文件：" + e.getMessage(), "打开文件", JOptionPane.WARNING_MESSAGE);
            }
        }

        private void openContainingFolder(File file) {
            if(file == null) return;
            File dir = file.isDirectory() ? file : file.getParentFile();
            if(dir == null || !dir.exists()) return;
            try {
                if(Desktop.isDesktopSupported()) Desktop.getDesktop().open(dir);
                else JOptionPane.showMessageDialog(ChatClient.this, "所在位置：\n" + dir.getAbsolutePath());
            } catch(Exception e) {
                JOptionPane.showMessageDialog(ChatClient.this, "无法打开所在位置：" + e.getMessage(), "打开所在位置", JOptionPane.WARNING_MESSAGE);
            }
        }

        private void previewImageMessage(final ChatMessage message) {
            try {
                final BufferedImage image = ImageIO.read(new ByteArrayInputStream(message.fileData));
                if(image == null) {
                    saveReceivedFile(message, null);
                    return;
                }
                final int originalWidth = image.getWidth();
                final int originalHeight = image.getHeight();
                final double[] scale = new double[]{Math.min(1.0,
                        Math.min(780.0 / Math.max(1, originalWidth), 560.0 / Math.max(1, originalHeight)))};

                final JDialog dialog = new JDialog(ChatClient.this, "图片预览", true);
                dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                JPanel root = new JPanel(new BorderLayout(SPACE_MD, SPACE_MD));
                root.setBackground(CHAT_BACKGROUND);
                root.setBorder(pad(SPACE_LG));
                final JLabel imageLabel = new JLabel();
                imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                final JScrollPane imageScroll = createModernScrollPane(imageLabel, CHAT_BACKGROUND);
                root.add(imageScroll, BorderLayout.CENTER);

                JPanel bottom = new JPanel(new BorderLayout());
                bottom.setOpaque(false);
                final JLabel info = createHintLabel(clipFileName(message.fileName)
                        + " · " + displayFileSize(message.fileSize)
                        + " · " + originalWidth + "×" + originalHeight);
                JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, SPACE_SM, 0));
                actions.setOpaque(false);
                JButton zoomOut = createButton("缩小", false);
                JButton zoomIn = createButton("放大", false);
                JButton fit = createButton("适应", false);
                JButton save = createButton("保存", true);
                zoomOut.setPreferredSize(new Dimension(70, 36));
                zoomIn.setPreferredSize(new Dimension(70, 36));
                fit.setPreferredSize(new Dimension(70, 36));
                save.setPreferredSize(new Dimension(82, 36));
                final Runnable updateImage = new Runnable() {
                    public void run() {
                        int width = Math.max(1, (int)Math.round(originalWidth * scale[0]));
                        int height = Math.max(1, (int)Math.round(originalHeight * scale[0]));
                        Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                        imageLabel.setIcon(new ImageIcon(scaled));
                        info.setText(clipFileName(message.fileName) + " · "
                                + displayFileSize(message.fileSize) + " · "
                                + (int)Math.round(scale[0] * 100) + "%");
                        imageLabel.revalidate();
                    }
                };
                zoomOut.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        scale[0] = Math.max(0.1, scale[0] / 1.25);
                        updateImage.run();
                    }
                });
                zoomIn.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        scale[0] = Math.min(4.0, scale[0] * 1.25);
                        updateImage.run();
                    }
                });
                fit.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        scale[0] = Math.min(1.0, Math.min(780.0 / Math.max(1, originalWidth), 560.0 / Math.max(1, originalHeight)));
                        updateImage.run();
                    }
                });
                save.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        showFileSaveMenu(message, null, save, 0, save.getHeight());
                    }
                });
                actions.add(zoomOut);
                actions.add(zoomIn);
                actions.add(fit);
                actions.add(save);
                bottom.add(info, BorderLayout.WEST);
                bottom.add(actions, BorderLayout.EAST);
                root.add(bottom, BorderLayout.SOUTH);
                dialog.setContentPane(root);
                dialog.setSize(880, 680);
                dialog.setMinimumSize(new Dimension(420, 300));
                dialog.setLocationRelativeTo(ChatClient.this);
                updateImage.run();
                dialog.getRootPane().registerKeyboardAction(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        dialog.dispose();
                    }
                }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
                dialog.setVisible(true);
            } catch(Exception e) {
                JOptionPane.showMessageDialog(ChatClient.this,
                        "图片预览失败：" + e.getMessage(),
                        "图片预览", JOptionPane.ERROR_MESSAGE);
            }
        }

        public void clear() {
            removeAll();
            showEmptyState();
            revalidate();
            repaint();
        }

        private void showEmptyState() {
            showEmptyState("欢迎进入 CNCD Chat", "消息、文件、语音和通话记录会显示在这里");
        }

        private void showEmptyState(String title, String text) {
            removeAll();
            JPanel state = createEmptyState(title, text);
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
        private int avatarSize = 36;

        AvatarView(String name, boolean outgoing) {
            setAvatar(name, outgoing);
            setAvatarSize(36);
        }

        public void setAvatarSize(int size) {
            avatarSize = Math.max(28, size);
            Dimension d = new Dimension(avatarSize, avatarSize);
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
            revalidate();
            repaint();
        }

        public void setAvatar(String name, boolean outgoing) {
            this.name = name == null || name.length() == 0 ? "?" : name;
            this.outgoing = outgoing;
            repaint();
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.min(getWidth(), getHeight()) - 2;
            if(size <= 0) { g2.dispose(); return; }
            int x0 = (getWidth() - size) / 2;
            int y0 = (getHeight() - size) / 2;
            int radius = Math.max(RADIUS_MD, size / 4);
            BufferedImage avatarImage = loadAvatarImage(name);
            if(avatarImage != null) {
                Shape oldClip = g2.getClip();
                RoundRectangle2D.Float clip = new RoundRectangle2D.Float(x0, y0, size, size, radius, radius);
                g2.setClip(clip);
                g2.drawImage(avatarImage, x0, y0, size, size, null);
                g2.setClip(oldClip);
                g2.setColor(new Color(255, 255, 255, 80));
                g2.drawRoundRect(x0 + 1, y0 + 1, size - 2, size - 2, radius, radius);
                g2.dispose();
                return;
            }
            Color fill = outgoing ? PRIMARY : avatarColor(name);
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

        private BufferedImage loadAvatarImage(String source) {
            try {
                Properties profile = loadProfileForUser(source);
                if(!"image".equals(profile.getProperty("avatarMode", "letter"))) return null;
                String path = profile.getProperty("avatarPath", "");
                if(path.length() == 0) return null;
                File file = new File(path);
                if(!file.exists()) return null;
                return ImageIO.read(file);
            } catch(Exception e) {
                return null;
            }
        }

        private String avatarText(String source) {
            String trimmed = source == null ? "" : source.trim();
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
