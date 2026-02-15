package dev.jojofr.joseta.events.misc;

import dev.jojofr.joseta.utils.Log;
import net.dv8tion.jda.api.entities.User;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

public class WelcomeChannel {
    private static Font font;
    private static BufferedImage welcomeImage;
    public static boolean imageLoaded;
    
    static {
        try {
            // TODO should be configurable URL? either stay static or allow custom per server
            InputStream imgStream = WelcomeChannel.class.getResourceAsStream("/welcomeImageBase.png");
            if (imgStream == null)
                throw new IOException("Could not load image resource: /welcomeImageBase.png.");
            
            welcomeImage = ImageIO.read(imgStream);
            imageLoaded = true;
        } catch (IOException e) {
            imageLoaded = false;
        }
        
        try {
            InputStream is = WelcomeChannel.class.getResourceAsStream("/Audiowide-Regular.ttf");
            if (is == null)
                throw new IOException("Could not load font resource: /Audiowide-Regular.ttf.");
            
            font = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(25f);
        } catch (IOException | FontFormatException e) {
            Log.err("Could not load the welcome image font, defaulting to Arial.", e);
            font = new Font("Arial", Font.PLAIN, 30);
        }
    }
    
    public static byte[] getWelcomeImage(User user, int memberCount) throws IOException {
        String name = "@" + user.getName();
        String userName = user.getGlobalName() != null ? user.getGlobalName() + " (" + name + ")" : name;
        
        BufferedImage image, avatar;
        image = new BufferedImage(welcomeImage.getWidth(), welcomeImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        avatar = getUserAvatar(user);
        
        Graphics2D g2d = image.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            
            g2d.drawImage(welcomeImage, 0, 0, image.getWidth(), image.getHeight(), null);
            g2d.drawImage(avatar, 62, 14, null);
            
            g2d.setFont(font);
            FontMetrics fm = g2d.getFontMetrics();
            int nameWidth = fm.stringWidth(userName);
            // 383 is the available width for the name text
            float widthRatio = nameWidth > 383 ? 383.0f / nameWidth : 1.0f;
            widthRatio = (float) Math.floor(widthRatio * 25.0f);
            
            g2d.setFont(font.deriveFont(widthRatio));
            g2d.setColor(new Color(244, 204, 122));
            g2d.drawString(userName, 222, 44);
            
            g2d.setFont(font.deriveFont(20f));
            g2d.setColor(new Color(155, 255, 169));
            g2d.drawString(Integer.toString(memberCount), 510, 124);
        } finally {
            g2d.dispose();
        }
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        
        return output.toByteArray();
    }
    
    public static BufferedImage getUserAvatar(User user) {
        BufferedImage image;
        try {
            image = ImageIO.read(user.getEffectiveAvatar().download(128).get());
        } catch (InterruptedException | ExecutionException | IOException e) {
            Log.warn("Failed to download avatar for user " + user.getIdLong(), e);
            return null;
        }
        
        if (image.getWidth() != 128 || image.getHeight() != 128) {
            BufferedImage resized = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = resized.createGraphics();
            try {
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                
                g2d.drawImage(image, 0, 0, 128, 128, null);
            } finally { g2d.dispose(); }
            
            image = resized;
        }
        
        BufferedImage circular = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = circular.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            
            Ellipse2D.Double ellipse = new Ellipse2D.Double(0, 0, 128, 128);
            g2d.setClip(ellipse);
            
            g2d.drawImage(image, 0, 0, null);
        } finally { g2d.dispose(); }
        
        return circular;
    }
}
