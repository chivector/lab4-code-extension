package com.cncd.ch04.server;
import java.io.*;
import java.net.*;
import java.util.*;
public class ConnectionKeeper {
    private LinkedList clientList;
    private CommandParser cp;
    public ConnectionKeeper(CommandParser parser) {
        this.cp = parser;
        clientList = new LinkedList();
    }
    public void add(Socket s) {
        MainServer.connects++;
        ConnectedClient cc = new ConnectedClient(s, this);
        clientList.addLast(cc);
        broadcast("Server: " + cc.getNick() + " joined.");
        broadcastUserList();
    }
    public void remove(ConnectedClient cc) {
        String nick = cc.getNick();
        clientList.remove(cc);
        cc = null;
        broadcast("Server: " + nick + " left.");
        broadcastUserList();
    }
    public LinkedList users() {
        return clientList;
    }
    public void runCommand(ConnectedClient cc, String str) {
        cp.runCommand(cc, str);
    }
    public void sendTo(ConnectedClient sender, String user, String msg) {
        boolean found = false;
        for(int i =0;i<clientList.size();i++) {
            ConnectedClient receiver = (ConnectedClient)(clientList.get(i));
            if(user.equalsIgnoreCase(receiver.nick)) {
                receiver.sendMessage(msg);
                found = true;
                i = clientList.size()+5; // Stop the loop.
            }
        }
        if(!found) {
            sender.sendMessage("Unable to find user " + user);
        }
    }
    public void sendUserList(ConnectedClient cc) {
        cc.sendMessage(userListPayload());
        cc.sendMessage(userListHtml());
    }
    public void broadcastUserList() {
        broadcast(userListPayload());
    }
    private String userListPayload() {
        StringBuffer msg = new StringBuffer("__USERS__|");
        for(int i =0;i<clientList.size();i++) {
            ConnectedClient cc = (ConnectedClient)(clientList.get(i));
            if(i > 0) msg.append(",");
            msg.append(cc.getNick());
        }
        return msg.toString();
    }
    private String userListHtml() {
        String msg = "Current Connected Users: <br>";
        for(int i =0;i<clientList.size();i++) {
            ConnectedClient cc = (ConnectedClient)(clientList.get(i));
            msg += "*" + cc.getNick() + "<br>";
        }
        return msg;
    }
    public void broadcast(String msg) {
        for(int i =0;i<clientList.size();i++) {
            ConnectedClient cc = (ConnectedClient)(clientList.get(i));
            cc.sendMessage(msg);
        }
    }
}
