package joseta.events.misc;

import joseta.*;
import joseta.database.*;
import joseta.database.entry.*;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.events.guild.member.*;
import net.dv8tion.jda.api.utils.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;

import javax.imageio.*;

public class WelcomeMessage {
    private static Font font;
    private static BufferedImage welcomeImage;
    private static boolean imageLoaded;

    public static void initialize() {
        Path cachedImagePath = Paths.get("resources", "welcomeImageBase.png");
        
        try {
            welcomeImage = ImageIO.read(cachedImagePath.toFile());
            imageLoaded = true;
        } catch (IOException e) {
            JosetaBot.logger.error("WelcomeMessage - An error occured while reading the base welcome image or font.", e);
            imageLoaded = false;
        }

        try {
            font = Font.createFont(Font.TRUETYPE_FONT, new File("resources/Audiowide-Regular.ttf")).deriveFont(25f);
        } catch (IOException e) {
            JosetaBot.logger.error("WelcomeMessage - The font could not be loaded. Defaulted to 'Arial'", e);
            font = new Font("Arial", Font.PLAIN, 30);        
        } catch (FontFormatException e) {
            JosetaBot.logger.error("WelcomeMessage - The font has a wrong format. Defaulted to 'Arial'", e);
            font = new Font("Arial", Font.PLAIN, 30);
        }
    }

    public static void executeGuildMemberJoin(GuildMemberJoinEvent event) {
        ConfigEntry config = ConfigDatabase.getConfig(event.getGuild().getIdLong());
        User user = event.getUser();

        TextChannel channel;
        Role botRole = null, memberRole = null; // Safe to be null, no check needed
        if (!config.welcomeEnabled) return;
        if (config.welcomeChannelId == 0L || (channel = event.getGuild().getChannelById(TextChannel.class, config.welcomeChannelId)) == null) {
            JosetaBot.logger.warn("WelcomeMessage - The welcome channel is not set or does not exist in the guild: " + event.getGuild().getName());
            return;
        }
        if (!user.isBot() && (config.joinRoleId == 0L || (memberRole = event.getGuild().getRoleById(config.joinRoleId)) == null)) {
            JosetaBot.logger.warn("WelcomeMessage - The new member role is not set or does not exist in the guild: " + event.getGuild().getName());
            return;
        }
        if (user.isBot() && (config.joinBotRoleId == 0L || (botRole = event.getGuild().getRoleById(config.joinBotRoleId)) == null)) {
            JosetaBot.logger.warn("WelcomeMessage - The bot role is not set or does not exist in the guild: " + event.getGuild().getName());
            return;
        }

        if (imageLoaded && config.welcomeImageEnabled) sendWelcomeImage(event.getGuild(), channel, user);
        else if (!config.welcomeJoinMessage.isEmpty()) channel.sendMessage(config.welcomeJoinMessage.replace("{{user}}", event.getUser().getAsMention())).queue();

        if (user.isBot()) event.getGuild().addRoleToMember(user, botRole).queue();
        else event.getGuild().addRoleToMember(user, memberRole).reason("Ajouter automatiquement lorsque le membre a rejoint.").queue();        
    }

    public static void executeGuildMemberRemove(GuildMemberRemoveEvent event) {
        ConfigEntry config = ConfigDatabase.getConfig(event.getGuild().getIdLong());
        TextChannel channel;
        if (!config.welcomeEnabled) return;
        if (config.welcomeChannelId == 0L || (channel = event.getGuild().getChannelById(TextChannel.class, config.welcomeChannelId))== null) {
            JosetaBot.logger.warn("WelcomeMessage - The welcome channel is not set or does not exist in the guild: " + event.getGuild().getName());
            return;
        }

         if (!config.welcomeLeaveMessage.isEmpty()) channel.sendMessage(config.welcomeLeaveMessage.replace("{{userName}}", event.getUser().getName())).queue();
    }

    private static void sendWelcomeImage(Guild guild, TextChannel channel, User user) {
        String name = "@"+user.getName();
        String userName = user.getGlobalName() != null ? user.getGlobalName() + " ("+ name +")" : name;        
        
        try {
            BufferedImage avatar = getUserAvatar(user, 128);

            ByteArrayInputStream image = createWelcomeImage(userName, guild.getMemberCount(), avatar);
            channel.sendMessage(user.getAsMention()).addFiles(FileUpload.fromData(image, "welcome.png")).queue();
            
            Files.deleteIfExists(Paths.get("resources", "userAvatar.png"));    
        } catch (MalformedURLException e) {
            JosetaBot.logger.error("WelcomeImage - An error occured with the user avatar URL.", e);
        } catch (IOException e) {
            JosetaBot.logger.error("WelcomeImage - Could not read/write the base/generated image.", e);
        }
    }

    private static BufferedImage getUserAvatar(User user, int size) throws IOException {
        BufferedImage avatar = ImageIO.read(new URL(user.getEffectiveAvatarUrl() + "?size=" + size));

        if (avatar.getWidth() > 128 || avatar.getHeight() > 128) avatar = resizeAvatar(avatar);

        return makeCircularAvatar(avatar);
    }

    private static BufferedImage resizeAvatar(BufferedImage avatar) {
        BufferedImage resized = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g2d.drawImage(avatar, 0, 0, 128, 128, null);
        g2d.dispose();

        return resized;
    }

    private static BufferedImage makeCircularAvatar(BufferedImage avatar) {
        BufferedImage circular = new BufferedImage(avatar.getWidth(), avatar.getWidth(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = circular.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        Ellipse2D.Double clip = new Ellipse2D.Double(0, 0, avatar.getWidth(), avatar.getWidth());
        g2d.setClip(clip);

        g2d.drawImage(avatar, 0, 0, null);
        g2d.dispose();

        return circular;
    }

    private static ByteArrayInputStream createWelcomeImage(String userName, int guildMemberCount, BufferedImage userAvatar) throws IOException {
        BufferedImage processedImage = new BufferedImage(
            welcomeImage.getWidth(),
            welcomeImage.getHeight(),
            BufferedImage.TYPE_INT_ARGB
        );
        
        Graphics2D g2d = processedImage.createGraphics();
        try {
            // Configure rendering hints
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                                RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            g2d.drawImage(welcomeImage, 0, 0, null);
            g2d.drawImage(userAvatar, 62, 14, null);
            
            // Adapative font size based of name lenght - support the maximum of 67 characters.
            g2d.setFont(font);
            FontMetrics fm = g2d.getFontMetrics();
            int userNameWidth = fm.stringWidth(userName);
            // 383 is the available space in pixel for the text.
            float widthRatio = userNameWidth > 383 ? 383.0f / userNameWidth : 1.0f;

            g2d.setFont(font.deriveFont((float) Math.floor(25.0f * widthRatio)));
            g2d.setColor(new Color(244, 204, 122));
            g2d.drawString(userName, 222, 44);

            g2d.setFont(font.deriveFont(20f));
            g2d.setColor(new Color(155, 255, 169));
            g2d.drawString(Integer.toString(guildMemberCount), 510, 124);
            
        } finally {
            g2d.dispose();
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(processedImage, "png", output);

        return new ByteArrayInputStream(output.toByteArray());
    }
}
