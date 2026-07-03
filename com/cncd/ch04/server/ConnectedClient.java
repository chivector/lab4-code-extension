package com.cncd.ch04.server;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
public class ConnectedClient {
    private ConnectionKeeper ck;
    public String nick;
    public Date connectedTime;
    public String ipNumber;
    public int portNumber;
    public boolean verifyedBoolean = false;
    public int verifyedCount = 0;
    public String tmpNick = "";
    private ServerMsgSender msgSend;
    private ServerMsgListener msgList;
    private Socket sock;
    private boolean dropped = false;
    public boolean printMsg = false;
    private String lastBroadcast = "";
    private long lastBroadcastAt = 0;
    private int repeatCount = 0;
    private final String[] blockedWords = {"广告", "spam", "badword"};
    public ConnectedClient(Socket sock, ConnectionKeeper ck) {
        this.ck = ck;
        ipNumber = sock.getInetAddress().getHostAddress();
        portNumber = sock.getPort();
        this.sock = sock;
        try {
            this.sock.setKeepAlive(true);
            this.sock.setTcpNoDelay(true);
        } catch(SocketException se) {}
        msgSend = new ServerMsgSender(this.sock, this);
        msgList = new ServerMsgListener(this.sock, this);
        nick = "" + portNumber;
    }
    public ConnectionKeeper getConnectionKeeper() {
            return ck;
    }
    public String getNick() {
            return nick;
    }
    public void sendMessage(String str) {
        msgSend.addMessage(str);
    }
    public void sendTo(String user, String msg) {
        ck.sendTo(this, user, msg);
    }
    public void broadcastMessage(String str) {
        if(!isSpam(str)) ck.broadcast(str);
    }
    public synchronized void dropClient() {
        if(dropped) return;
        dropped = true;
        if(msgList != null) msgList.closeConnection();
        if(msgSend != null) msgSend.closeConnection();
        try {
            if(sock != null) sock.close();
        } catch(IOException ioe) {}
        ck.remove(this);
    }
    public void runCommand(String str) {
        ck.runCommand(this, str);
    }
    private boolean isSpam(String str) {
        String lower = str.toLowerCase();
        for(int i=0;i<blockedWords.length;i++) {
            if(lower.indexOf(blockedWords[i].toLowerCase()) >= 0) {
                sendMessage("Server: message blocked by content filter.");
                return true;
            }
        }
        long now = System.currentTimeMillis();
        if(str.equals(lastBroadcast) && now - lastBroadcastAt < 1500) {
            repeatCount++;
            if(repeatCount >= 2) {
                sendMessage("Server: repeated message blocked. Please slow down.");
                lastBroadcastAt = now;
                return true;
            }
        } else {
            repeatCount = 0;
            lastBroadcast = str;
        }
        lastBroadcastAt = now;
        return false;
    }
    public static void main(String arg[]) {
        MainServer ms = new MainServer(1984);
    }
    public void whoAmI() {
        String str = "<br>Connected Port: " + portNumber + "<br>" +
                     "Nick: " + nick + "<br>";
        sendMessage(str);
    }
}
class ServerMsgSender extends Thread {
    private Socket sock;
    private LinkedList msgList;
    private ConnectedClient cc;
    private boolean running = true;
    public ServerMsgSender(Socket sock, ConnectedClient cc) {
        this.sock = sock;
        this.cc = cc;
        collectInfo();
        msgList = new LinkedList();
        start();
    }
    public synchronized void addMessage(String str) {
        if(cc.printMsg) System.out.println("MsgSender.addMessage: " +str);
        msgList.addLast(str);
    }
    private synchronized String nextMessage() {
        if(msgList.size() == 0) return null;
        return (String)msgList.removeFirst();
    }
    private void collectInfo() {
    }
    public void run() {
        try {
            DataOutputStream dataOut = new DataOutputStream(sock.getOutputStream());
            while(running) {
                String toSend;
                while((toSend = nextMessage()) != null) {
                    dataOut.write(toSend.getBytes(StandardCharsets.UTF_8));
                    dataOut.write(MainServer.MSGENDCHAR);
                    dataOut.flush();
                    if(cc.printMsg) System.out.println("MsgSender.run: Sending: " + toSend);
                    sleep(10);
                }
                sleep(10);
            }
        } catch(Exception e) {
            String msg = e.getMessage();
            if(isNormalDisconnect(e)) {
                System.out.println("MsgSender.run Client disconnected nick: " + cc.nick);
                cc.dropClient();
            } else {
                System.out.println("MsgSender.run: Msg: " + msg);
                e.printStackTrace();
                cc.dropClient();
            }
        }
    }
    private boolean isNormalDisconnect(Exception e) {
        String msg = e.getMessage();
        return e instanceof SocketException
                || e instanceof EOFException
                || (msg != null && (msg.startsWith(MainServer.DISCONNECTED)
                || msg.startsWith(MainServer.DISCONNECTED_CLIENT)
                || msg.indexOf("closed") >= 0
                || msg.indexOf("中止") >= 0));
    }
    public void closeConnection() {
        running = false;
        try {
            if(sock != null) sock.close();
        } catch(IOException ioe) {}
    }
}
class ServerMsgListener extends Thread {
    private LinkedList msgList;
    private Socket sock;
    private ConnectedClient cc;
    private boolean running = true;
    public ServerMsgListener(Socket s, ConnectedClient cc) {
        msgList = new LinkedList();
        sock = s;
        this.cc = cc;
        start();
    }
    public void closeConnection() {
        running = false;
    }
    public void run() {
        try {
            BufferedInputStream buffIn = new BufferedInputStream(sock.getInputStream());
            DataInputStream dataIn = new DataInputStream(buffIn);
            while(running) {
                int c;
                boolean didRun = false;
                boolean isCommand = false;
                ByteArrayOutputStream strBuff = new ByteArrayOutputStream();
                sleep(10);
                while( (c=dataIn.read()) != 0xff) {
                    if(c == -1) throw new EOFException("Client closed connection");
                    if(!didRun && c==0xFD) {
                        isCommand = true;
                        didRun = true;
                        continue;
                    }
                    strBuff.write(c);
                    if(!didRun) didRun=true;
                }
                if(cc.verifyedCount>0 && !cc.verifyedBoolean && !isCommand) {
                    cc.verifyedCount--;
                    if(cc.verifyedCount==1) {
                        cc.sendMessage("You have failed to verify your nick");
                        cc.nick = "" + cc.portNumber;
                        cc.sendMessage("Your nick is " + cc.nick);
                    } else {
                        cc.sendMessage("type: \"/verify &lt;password&gt\" to verify your nick");
                    }
                }
                if(didRun) {
                    String payload = strBuff.toString(StandardCharsets.UTF_8.name());
                    String toSend = "" + cc.nick + ":" + payload;
                    if(cc.printMsg) System.out.println("MsgListenet.run Sending msg: " + toSend);
                    if(!isCommand) cc.broadcastMessage(toSend);
                    else cc.runCommand(payload);
                }
            }
        } catch(SocketException se) {
            if(se.getMessage() == null || se.getMessage().startsWith("Connection reset")
                    || se.getMessage().startsWith("Socket closed"))
                cc.dropClient();
        } catch(Exception e) {
            if(!(e instanceof EOFException)) e.printStackTrace();
            cc.dropClient();
        }
    }
}

