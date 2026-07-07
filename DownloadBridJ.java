import java.io.*;
import java.net.*;

public class DownloadBridJ {
    public static void main(String[] args) throws Exception {
        String urlStr = "https://repo1.maven.org/maven2/com/nativelibs4java/bridj/0.7.0/bridj-0.7.0.jar";
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        String fileName = urlStr.substring(urlStr.lastIndexOf("/") + 1);
        System.out.println("Downloading " + fileName + "...");
        InputStream in = conn.getInputStream();
        FileOutputStream out = new FileOutputStream("lib/" + fileName);
        byte[] buf = new byte[8192];
        int len;
        long total = 0;
        while((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
            total += len;
        }
        out.close();
        in.close();
        System.out.println("Done: " + total + " bytes");
    }
}
