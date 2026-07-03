package com.cncd.ch04.server;
import java.net.*;
import java.io.*;
public class MainServer extends Thread {
    public static final String DISCONNECTED = "Software caused connection abort";
    public static final String DISCONNECTED_CLIENT = "Socket closed";
    public static final String PORT_USED_ERROR = "Address already in use: JVM_Bind";
    public static long uptime = 0; 
    public static long connects = 0;
    public static final char MSGENDCHAR = 0xff;
    int port = 1984;
    int clients = 8;
    private boolean newPort = true;
    private ServerSocket sSock;
    private Socket sock;
    public ConnectionKeeper ck;
    public static DataSource ds;
    public static CommandParser cp;
    private static final int SERVER_BACKLOG = 50;
    public MainServer(int port) {
        this.port = port;
        ck = new ConnectionKeeper(MainServer.cp);
        MainServer.uptime = System.currentTimeMillis();
        start();
    }
    public void run() {
        while(true) {
            try {
                sSock = new ServerSocket();
                sSock.setReuseAddress(true);
                sSock.bind(new InetSocketAddress(port), SERVER_BACKLOG);
                if(newPort) {
                    System.out.println("Server Listening at port: " + sSock.getLocalPort());
                    newPort = false;
                }

                while(true) {
                    sock = sSock.accept();
                    sock.setKeepAlive(true);
                    sock.setTcpNoDelay(true);
                    ck.add(sock);
                }
            } catch(BindException be) {
                System.out.println("Port " + port + " is already used, attempting to use " + (port + 1));
                port += 1;
                newPort = true;
                closeServerSocket();
            } catch(Exception e) {
                System.out.println("Server accept loop recovered from: " + e.getMessage());
                e.printStackTrace();
                closeServerSocket();
                MainServer.sleep(500);
            }
        }
    }

    private void closeServerSocket() {
        try {
            if(sSock != null) sSock.close();
        } catch(Exception e) {}
    }
    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch(Exception e) {}
    }
    public static void main(String arg[]) {
        int port = 0;
        MainServer ms;
        MainServer.ds = new FileDataSource();
        MainServer.cp = new BroadcastCommandParser();
        MainServer.cp.setDataSource(MainServer.ds);
        if(arg.length!=1) {
            ms = new MainServer(3500);
            System.out.println("Usage: java jchat.server.MainServer <port>\nAttempting to use default port 3500");
        } else {
            try {
                port = Integer.parseInt(arg[0]);
            } catch(NumberFormatException nfe) {
                System.out.println("Attempting to use default port 3500");
                port = 3500;
            } finally {
                ms = new MainServer(port);
            }
        }
    }
}
