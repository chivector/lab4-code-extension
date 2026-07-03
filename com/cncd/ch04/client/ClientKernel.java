package com.cncd.ch04.client;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.swing.SwingUtilities;
public class ClientKernel {
    public static final char MSGENDCHAR = 0xff;
    public static final char EXIT = 0xFE;
    public static final char NICK = 0xFD;
    public static final char COMMAND = 0xFD;
    
    private String serverAd;
    private int port;
    private Socket sock;
    private boolean isConnected = false;
    private boolean dropMe = false;
    private LinkedList clients;
    private LinkedList pendingMessages;
    public String nick;
    public boolean printMsg = true;
    private ClientMsgSender cms;
    private ClientMsgListener cml;
    /** Creates a new instance of ClientKernel */
    public ClientKernel(String server, int port) {
        this.port = port;
        nick = "" + port;
        serverAd = server;
        clients = new LinkedList();
        pendingMessages = new LinkedList();
        connect();
        if(isConnected) {
            cms = new ClientMsgSender(this, sock);
            cml = new ClientMsgListener(this, sock);
        }
    }
    public void connect() {
        try {
            sock = new Socket(serverAd, port);
            isConnected = true;
        } catch(IOException ioe ) {
            ioe.printStackTrace();
        }
    }
    public int getPort() {
        return port;
    }
    public boolean setNick(String nick) {
        this.nick = nick;
        sendMessage("" + ClientKernel.COMMAND + "nick " + nick);
        return true;
    }
    public int getLocalPort() {
        return sock.getLocalPort();
    }
    public void dropMe() {
        System.out.println("Drop ME!!!");
        cms.drop();
        cml.drop();
        dropMe = true;
        while(cml.hasStoped() && cms.hasStoped()) pause(5);
    }
    public void sendMessage(String str) {
        if(!dropMe && str != null && str.length() > 0) {
            if(str.charAt(0) == '/')
                cms.addMessage("" + ClientKernel.COMMAND + str.substring(1) );
            else cms.addMessage(str);
        }
    }
    public void addClient(ChatClient c) {
        clients.add(c);
        while(pendingMessages.size() > 0) {
            dispatchMsg(c, (String)pendingMessages.removeFirst());
        }
    }
    public void removeClient(ChatClient c) {
        clients.remove(c);
    }
    public void pause(int time) {
        try {
            Thread.sleep(time);
        } catch(Exception e) {}
    }
    public synchronized void storeMsg(String str) {
        Object[] client = clients.toArray();
        if(client.length == 0) {
            pendingMessages.addLast(str);
            return;
        }
        for(int i=0;i<client.length;i++) {
            ChatClient chatClient = (ChatClient)(client[i]);
            dispatchMsg(chatClient, str);
        }
    }
    private void dispatchMsg(final ChatClient chatClient, final String str) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if(str.startsWith(ChatClient.USERS_PREFIX)) {
                    chatClient.updateUsers(str.substring(ChatClient.USERS_PREFIX.length()));
                } else if(str.startsWith(ChatClient.FILE_PREFIX)) {
                    String[] parts = str.split("\\|", 4);
                    if(parts.length == 4) chatClient.receiveFile(parts[1], parts[2], parts[3]);
                } else if(str.startsWith(ChatClient.SERVER_MOMENT_ITEM_PREFIX)) {
                    chatClient.receiveServerMoment(str.substring(ChatClient.SERVER_MOMENT_ITEM_PREFIX.length()));
                } else {
                    chatClient.addMsg(str);
                }
            }
        });
    }
    public boolean isConnected() {
        return isConnected;
    }
    public static void main(String args[]) {
        new ClientKernel("localhost", 1984);
    }
}
class ClientMsgSender extends Thread {
    private Socket s;
    private ClientKernel ck;
    private LinkedList msgList;
    private boolean running = true;
    private boolean hasStoped = false;
    public ClientMsgSender(ClientKernel ck, Socket s) {
        this.ck = ck;
        this.s  = s;
        msgList = new LinkedList();
        start();
    }
    public synchronized void addMessage(String msg) {
        msgList.addLast(msg);
    }
    public void drop() {
        running = false;
    }
    public boolean hasStoped() {
        return hasStoped;
    }
    public void run() {
        try {
            DataOutputStream dataOut = new DataOutputStream(s.getOutputStream());
            while(running) {
                while(msgList.size()>0) {
                    String msg = ((String)(msgList.removeFirst()));
                    if(msg.length() > 0 && msg.charAt(0) == ClientKernel.COMMAND) {
                        dataOut.write(ClientKernel.COMMAND);
                        dataOut.write(msg.substring(1).getBytes(StandardCharsets.UTF_8));
                    } else {
                        dataOut.write(msg.getBytes(StandardCharsets.UTF_8));
                    }
                    dataOut.write(ClientKernel.MSGENDCHAR);
                    dataOut.flush();
                }
                sleep(10);
            }
            dataOut.write(ClientKernel.EXIT);
            dataOut.close();
        } catch(Exception ioe) {
            ioe.printStackTrace();
        } finally {
            hasStoped = true;
        }
    }
}
class ClientMsgListener extends Thread{
    private ClientKernel ck;
    private Socket s;
    private boolean running = true;
    private boolean hasStoped = false;
    public ClientMsgListener(ClientKernel ck, Socket s) {
        this.ck = ck;
        this.s  = s;
        start();
    }
    public void drop() {
        running = false;
    }
    public boolean hasStoped() {
        return hasStoped;
    }
    public void run() {
        try {
                BufferedInputStream buffIn = new BufferedInputStream(s.getInputStream());
                DataInputStream dataIn = new DataInputStream(buffIn);
                while(running) {
                    ByteArrayOutputStream strBuff = new ByteArrayOutputStream();
                    int c;
                    while( (c=dataIn.read()) != ClientKernel.MSGENDCHAR) {
                        if(c == -1) throw new EOFException("Server closed connection");
                        strBuff.write(c);
                    }
                    ck.storeMsg(strBuff.toString(StandardCharsets.UTF_8.name()));
                }
                dataIn.close();
                buffIn.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        } finally {
            hasStoped = true;
        }
    }
}
