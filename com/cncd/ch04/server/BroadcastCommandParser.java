package com.cncd.ch04.server;
import java.util.*;
public class BroadcastCommandParser implements CommandParser {
    private final String NICK = "nick";
    private final String USERS = "users";
    private final String EXIT = "exit";
    private final String VERSION = "version";
    private final String VERIFY = "verify";
    private final String REGISTER = "register";
    private final String WHO_AM_I = "whoami";
    private final String MSG = "msg";
    private final String STATS = "stats";
    private final String FILE = "file";
    private final String HELP = "help";
        private final String tab = "&nbsp;&nbsp;&nbsp;";
    private DataSource ds;
    private final int sek = 1000;
    private final int min = 60*sek;
    private final int hours = 60*min;
    private final int days = 24*hours;
    public BroadcastCommandParser() {
        System.out.println("BroadcastCommandParser");
    }
    public  void runCommand(ConnectedClient cc, String str) {
        try {
            if(ds == null) {
                System.out.println("CommandParser: DataSoruce Missing");
                cc.sendMessage("Server: Your command didn't get parsed, The Server Admin knows why ;)");
            } else {
                StringTokenizer strTok = new StringTokenizer(str);
                if(!strTok.hasMoreTokens()) return;
                String command = strTok.nextToken();
                if(command.equalsIgnoreCase(NICK))
                    if(strTok.hasMoreTokens()) setNick(cc, strTok.nextToken());
                    else cc.sendMessage("usage: /nick <newNick>");
                else if (command.equalsIgnoreCase(USERS))
                    users(cc);
                else if (command.equalsIgnoreCase(EXIT))
                    exit(cc);
                else if (command.equalsIgnoreCase(VERIFY))
                    verifyNick(cc, strTok.nextToken());
                else if(command.equalsIgnoreCase(REGISTER))
                    registerNick(cc, strTok.nextToken(), strTok.nextToken());
                else if(command.equalsIgnoreCase(WHO_AM_I))
                    whoAmI(cc);
                else if(command.equalsIgnoreCase(MSG))
                    msg(cc, strTok.nextToken(), strTok);
                else if(command.equalsIgnoreCase(FILE))
                    file(cc, strTok);
                else if(command.equalsIgnoreCase(STATS))
                    stats(cc);
                else if(command.equalsIgnoreCase(HELP))
                    help(cc);
                else
                    cc.sendMessage("Unknown command: " + command + "<br>Type /help for command list.");
            }
        } catch(Exception e) {
            System.out.println("CommandParser: " + e.getMessage());
            cc.sendMessage("Invalid Command: " + str);
        }
    }
    private void stats(ConnectedClient cc) {
        long runningTime = System.currentTimeMillis() - MainServer.uptime;
        String str = "Server has been running for " + printTime(runningTime) + "<br>" + 
                     "User connects since uptime " + MainServer.connects + "<br>";
                     
                     
        cc.sendMessage(str);
    }
    private void help(ConnectedClient cc) {
        String str = "Available commands:<br>" +
                "/users - list online users<br>" +
                "/msg &lt;user&gt; &lt;message&gt; - send private message<br>" +
                "/file &lt;user&gt; &lt;filename&gt; &lt;base64&gt; - transfer file<br>" +
                "/stats - show server running state<br>" +
                "/whoami - show current connection info<br>" +
                "/nick &lt;newNick&gt; - change nickname<br>" +
                "/exit - disconnect from server<br>" +
                "/help - show this help";
        cc.sendMessage(str);
    }
    private String printTime(long time) {
        String str = "";
        if(time<sek) {
            str+="" + time + "ms";
            return str;
        }
        if(time>sek && time<min) {
            long t = time%sek;
            str+="" + (time/sek) + "sek " + printTime(t);
            return str;
        }
        if(time>min && time<hours) {
            long t = time%min;
            str+= "" + (time/min) + "min " + printTime(t);
            return str;
        }
        if(time>hours && time<days) {
            long t= time%hours;
            str+= "" + (time/hours) + "hours " + printTime(t);
        }
        return str;
    }
    private void msg(ConnectedClient cc, String user, StringTokenizer strTok) {
        StringBuffer strBuff = new StringBuffer();
        while(strTok.hasMoreTokens())
            strBuff.append(strTok.nextToken() + " ");
        if(strBuff.length() == 0) {
            cc.sendMessage("usage: /msg <user> <message>");
            return;
        }
        String msg = "<font color=\"#663399\">[private] " + cc.nick + ":"
                + strBuff.toString() + "</font>";
        cc.sendTo(user, msg);
        cc.sendMessage("<font color=\"#666666\">[private sent to " + user + "] "
                + strBuff.toString() + "</font>");
    }
    private void file(ConnectedClient cc, StringTokenizer strTok) {
        if(strTok.countTokens() < 3) {
            cc.sendMessage("usage: /file <user> <filename> <base64>");
            return;
        }
        String user = strTok.nextToken();
        String filename = strTok.nextToken();
        String data = strTok.nextToken();
        cc.sendTo(user, "__FILE__|" + cc.nick + "|" + filename + "|" + data);
        cc.sendMessage("Server: file " + filename + " sent to " + user);
    }
    private  void users(ConnectedClient cc) {
        cc.getConnectionKeeper().sendUserList(cc);
    }
    private  void setNick(ConnectedClient cc, String str) {
        //System.out.println("" + cc.nick + " is now known as " + str);
        /*cc.nick = str;
        cc.sendMessage("Server: Your are now known as " + str);*/
        cc.verifyedBoolean = false;
        boolean verify = ds.verifyUser(str, "");
        if(verify) {
            if(isNickFree(cc, str)) {
                cc.nick = str;
                cc.verifyedBoolean = true;
                cc.sendMessage("Server: You are now known as " + str);
                cc.getConnectionKeeper().broadcastUserList();
            } else 
                cc.sendMessage("nick " + str + " was allready taken");
            
            
        } else {
            cc.verifyedCount = 5;
            cc.tmpNick = str;
            cc.sendMessage("Nick " + str + " is registered so you have to " +
                            "verify that this nick is yours");
        }
    }
    private boolean isNickFree(ConnectedClient cc, String nick) {
        LinkedList users = (LinkedList)((cc.getConnectionKeeper().users()).clone());
        Iterator it = users.iterator();
        while(it.hasNext()) {
            ConnectedClient comp = ((ConnectedClient)(it.next()));
            String compNick = comp.getNick();
            if(nick.equalsIgnoreCase(compNick)) return false;
        }
        return true;
    }
    private void whoAmI(ConnectedClient cc) {
        cc.whoAmI();
    }
    private void registerNick(ConnectedClient cc, String nick, String pass) {
        if(pass.length()<4 || nick.length()<4) {
            cc.sendMessage("Your nick/password needs to be atleast 4 chars long");
        } else {
            if(ds.addUser(nick, pass)) {
                cc.sendMessage("User " + nick + " is now registered and set as your own");
                cc.nick = nick;
                cc.verifyedBoolean = true;
                cc.getConnectionKeeper().broadcastUserList();
            } else {
                cc.sendMessage("The username is allready taken");
            }
            
        }
    }
    private void verifyNick(ConnectedClient cc, String password) {
        if(ds.verifyUser(cc.tmpNick, password)) {
            cc.nick = cc.tmpNick;
            cc.verifyedBoolean = true;
            cc.getConnectionKeeper().broadcastUserList();
        } else {
            cc.nick = "" + cc.portNumber;
            cc.sendMessage("Invalid user/pass, your nick is set to " + cc.nick);
        }
    }
    private  void exit(ConnectedClient cc) {
        cc.sendMessage("Server: You are being disconected!");
        try { Thread.sleep(50); } catch(Exception e) {}
        cc.dropClient();
    }
    public void setDataSource(DataSource ds) {
        this.ds = ds;
    }
}
