import java.io.*;
import java.net.*;

public class DownloadAllDeps {
    public static void main(String[] args) throws Exception {
        String[] urls = {
            "https://repo1.maven.org/maven2/net/java/dev/jna/jna/5.13.0/jna-5.13.0.jar",
            "https://repo1.maven.org/maven2/net/java/dev/jna/jna-platform/5.13.0/jna-platform-5.13.0.jar"
        };
        for(String urlStr : urls) {
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
}
