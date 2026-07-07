import java.io.*;
import java.net.*;

public class DownloadLib {
    public static void main(String[] args) throws Exception {
        URL url = new URL("https://repo1.maven.org/maven2/com/github/sarxos/webcam-capture/0.3.12/webcam-capture-0.3.12.jar");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);
        System.out.println("Downloading...");
        InputStream in = conn.getInputStream();
        FileOutputStream out = new FileOutputStream("lib/webcam-capture.jar");
        byte[] buf = new byte[8192];
        int len;
        long total = 0;
        while((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
            total += len;
            System.out.print("\rDownloaded: " + total + " bytes");
        }
        out.close();
        in.close();
        System.out.println("\nDone!");
    }
}
