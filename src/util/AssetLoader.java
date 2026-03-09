package util;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class AssetLoader {
    public static BufferedImage loadImage(String path, int w, int h) {
        try {
            BufferedImage img = ImageIO.read(new File(path));
            if (img == null) {
                return placeholder(w, h);
            }
            Image scaled = img.getScaledInstance(w, h, Image.SCALE_FAST);
            BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            out.getGraphics().drawImage(scaled, 0, 0, null);
            return out;
        } catch (Exception e) {
            return placeholder(w, h);
        }
    }

    private static BufferedImage placeholder(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        var g = img.createGraphics();
        g.setColor(new java.awt.Color(40, 40, 40, 180));
        g.fillRect(0, 0, w, h);
        g.setColor(java.awt.Color.WHITE);
        g.drawRect(0, 0, w - 1, h - 1);
        g.drawLine(0, 0, w - 1, h - 1);
        g.drawLine(w - 1, 0, 0, h - 1);
        g.dispose();
        return img;
    }
}
