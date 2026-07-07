import com.github.sarxos.webcam.*;
import java.awt.*;

public class TestWebcam {
    public static void main(String[] args) {
        System.out.println("Testing webcam capture...");
        Webcam webcam = Webcam.getDefault();
        if(webcam == null) {
            System.out.println("No webcam found");
            return;
        }
        System.out.println("Webcam found: " + webcam.getName());
        System.out.println("View sizes:");
        for(Dimension d : webcam.getViewSizes()) {
            System.out.println("  " + d);
        }
        try {
            webcam.setViewSize(new Dimension(320, 240));
            webcam.open();
            System.out.println("Webcam opened successfully");
            Thread.sleep(2000);
            System.out.println("Closing webcam...");
            webcam.close();
            System.out.println("Test completed");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
