import java.io.*;
import java.net.*;

public class DownloadDeps {
    public static void main(String[] args) throws Exception {
        String[] urls = {
            "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar",
            "https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.7.36/slf4j-simple-1.7.36.jar"
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
