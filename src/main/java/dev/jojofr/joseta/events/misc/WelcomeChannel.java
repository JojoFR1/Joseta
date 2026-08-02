package dev.jojofr.joseta.events.misc;

import dev.jojofr.joseta.utils.Log;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class WelcomeChannel {
    // TODO redo
    private static Font font;
    public static BufferedImage welcomeImage;
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
    
    // NEW
    private static final int AVATAR_SIZE = 128;
    
    private static final int AVATAR_X = 62;
    private static final int AVATAR_Y = 14;
    
    private static final int NAME_X = 222;
    private static final int NAME_Y = 44;
    private static final int NAME_MAX_WIDTH = 383;
    private static final Color NAME_COLOR = new Color(244, 204, 122);
    
    private static final int MEMBER_COUNT_X = 510;
    private static final int MEMBER_COUNT_Y = 124;
    private static final Color MEMBER_COUNT_COLOR = new Color(155, 255, 169);
    
    public static void sendWelcomeMessage(String message, TextChannel channel, User user) {
        if (message.isEmpty()) return;
        
        channel.sendMessage(message.replace("{{user}}", user.getAsMention())).queue();
    }
    
    public static CompletableFuture<byte[]> renderWelcomeImage(User user, int memberCount) {
        return downloadAvatar(user).thenApply(avatar -> renderImage(welcomeImage, avatar, user, memberCount));
    }
    
    private static byte[] renderImage(BufferedImage template, BufferedImage avatar, User user, int memberCount) {
        int templateWidth = template.getWidth(), templateHeight = template.getHeight();
        BufferedImage image = new BufferedImage(templateWidth, templateHeight, BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D g2d = image.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            
            g2d.drawImage(template, 0, 0, image.getWidth(), image.getHeight(), null);
            
            if (avatar != null) g2d.drawImage(makeCircular(avatar), AVATAR_X, AVATAR_Y, null);
            
            drawName(g2d, user, font);
            drawMemberCount(g2d, memberCount, font);
        } finally { g2d.dispose(); }
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try { ImageIO.write(image, "png", output); }
        catch (IOException e) {
            Log.err("Failed to write welcome image to output stream", e);
            return null;
        }
        
        return output.toByteArray();
    }
    
    private static void drawName(Graphics2D g2d, User user, Font font) {
        String name = "@" + user.getName();
        String userName = user.getGlobalName() != null ? user.getGlobalName() + " (" + name + ")" : name;
        
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int nameWidth = fm.stringWidth(userName);
        
        float widthRatio = nameWidth > NAME_MAX_WIDTH ? (float) NAME_MAX_WIDTH / nameWidth : 1.0f;
        float fittedSize = (float) Math.floor(widthRatio * font.getSize2D());
        
        g2d.setFont(font.deriveFont(fittedSize));
        g2d.setColor(NAME_COLOR);
        g2d.drawString(userName, NAME_X, NAME_Y);
    }
    
    private static void drawMemberCount(Graphics2D g2d, int memberCount, Font font) {
        g2d.setFont(font.deriveFont(20f));
        g2d.setColor(MEMBER_COUNT_COLOR);
        g2d.drawString(Integer.toString(memberCount), MEMBER_COUNT_X, MEMBER_COUNT_Y);
    }
    
    public static CompletableFuture<BufferedImage> downloadAvatar(User user) {
        return user.getEffectiveAvatar().download(AVATAR_SIZE).thenApply(in -> {
            BufferedImage image;
            try { image = ImageIO.read(in); }
            catch (IOException e) {
                throw new CompletionException("Failed to read avatar image for user " + user.getIdLong(), e);
            }
            
            if (image.getWidth() != AVATAR_SIZE || image.getHeight() != AVATAR_SIZE) {
                BufferedImage resized = new BufferedImage(AVATAR_SIZE, AVATAR_SIZE, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = resized.createGraphics();
                try {
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.drawImage(image, 0, 0, AVATAR_SIZE, AVATAR_SIZE, null);
                } finally { g2d.dispose(); }
                
                image = resized;
            }
            
            return image;
        }).exceptionally(e -> {
            Log.err("Failed to download avatar for user " + user.getIdLong(), e);
            return null;
        });
    }
    
    private static BufferedImage makeCircular(BufferedImage image) {
        int width = image.getWidth(), height = image.getHeight();
        BufferedImage circular = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = circular.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.WHITE);
            g2d.fill(new Ellipse2D.Float(0, 0, width, height));
            g2d.setComposite(AlphaComposite.SrcIn);
            g2d.drawImage(image, 0, 0, null);
        } finally { g2d.dispose(); }
        
        return circular;
    }
}
