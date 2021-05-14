import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Cock {


    public static void main(String[] args) throws IOException {
        BufferedImage original = ImageIO.read(new File("nigga.jpg"));
        int width = original.getWidth();
        BufferedImage circleBuffer = new BufferedImage(width, width, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = circleBuffer.createGraphics();
        g2.setClip(new Ellipse2D.Float(0, 0, width, width));
        g2.drawImage(original, 0, 0, width, width, null);
        g2.dispose();
        File tempFile = new File("tempImage.png");
        ImageIO.write(circleBuffer, "png", tempFile);

    }
}
