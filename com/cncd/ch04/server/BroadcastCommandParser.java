package com.cncd.ch04.server;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
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
    private final String MOMENT_PUT = "moment_put";
    private final String MOMENT_LIST = "moment_list";
    private final String MOMENT_DELETE = "moment_delete";
    private final String HELP = "help";
    private final String SERVER_MOMENT_PREFIX = "__SERVER_MOMENT__|";
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
                else if(command.equalsIgnoreCase(MOMENT_PUT))
                    momentPut(cc, strTok);
                else if(command.equalsIgnoreCase(MOMENT_LIST))
                    momentList(cc);
                else if(command.equalsIgnoreCase(MOMENT_DELETE))
                    momentDelete(cc, strTok);
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
                "/moment_list - sync moments history<br>" +
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
    private void momentPut(ConnectedClient cc, StringTokenizer strTok) {
        if(!strTok.hasMoreTokens()) {
            cc.sendMessage("usage: /moment_put <base64MomentRecord>");
            return;
        }
        String encodedMoment = strTok.nextToken();
        upsertMoment(encodedMoment);
    }
    private void momentList(ConnectedClient cc) {
        java.util.List lines = readMomentLines();
        for(int i=0;i<lines.size();i++) {
            cc.sendMessage(SERVER_MOMENT_PREFIX + lines.get(i));
        }
    }
    private void momentDelete(ConnectedClient cc, StringTokenizer strTok) {
        if(!strTok.hasMoreTokens()) {
            cc.sendMessage("usage: /moment_delete <base64MomentId>");
            return;
        }
        String id = decodeToken(strTok.nextToken());
        if(id.length() > 0) deleteMoment(id);
    }
    private synchronized void upsertMoment(String encodedMoment) {
        String id = momentId(encodedMoment);
        if(id.length() == 0) return;
        java.util.List lines = readMomentLines();
        boolean replaced = false;
        for(int i=0;i<lines.size();i++) {
            String line = (String)lines.get(i);
            if(id.equals(momentId(line))) {
                lines.set(i, encodedMoment);
                replaced = true;
                break;
            }
        }
        if(!replaced) lines.add(encodedMoment);
        writeMomentLines(lines);
    }
    private synchronized void deleteMoment(String id) {
        java.util.List lines = readMomentLines();
        Iterator it = lines.iterator();
        while(it.hasNext()) {
            String line = (String)it.next();
            if(id.equals(momentId(line))) it.remove();
        }
        writeMomentLines(lines);
    }
    private Path momentFile() {
        return Paths.get(System.getProperty("user.home"), ".mihalychat", "server", "moments.txt");
    }
    private java.util.List readMomentLines() {
        try {
            Path file = momentFile();
            if(!Files.exists(file)) return new ArrayList();
            return Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch(Exception e) {
            return new ArrayList();
        }
    }
    private void writeMomentLines(java.util.List lines) {
        try {
            Path file = momentFile();
            Files.createDirectories(file.getParent());
            Files.write(file, lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch(Exception e) {
            System.out.println("Moment store failed: " + e.getMessage());
        }
    }
    private String momentId(String encodedMoment) {
        try {
            String raw = decodeToken(encodedMoment);
            String[] parts = raw.split("\\t", -1);
            if(parts.length >= 2 && "M2".equals(parts[0])) return parts[1];
        } catch(Exception e) {
        }
        return "";
    }
    private String decodeToken(String value) {
        try {
            return new String(Base64.getDecoder().decode(value == null ? "" : value), StandardCharsets.UTF_8);
        } catch(Exception e) {
            return "";
        }
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
