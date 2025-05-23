package joseta.events;

import joseta.*;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.events.guild.member.*;
import net.dv8tion.jda.api.hooks.*;
import net.dv8tion.jda.api.utils.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;

import javax.imageio.*;

public class WelcomeMessage extends ListenerAdapter {
    private static Font font;
    private static BufferedImage welcomeImage;
    private static boolean imageLoaded = true;

    public static void initialize() {
        Path cachedImagePath = Paths.get("resources", "welcomeImageBase.png");
        
        try {
            welcomeImage = ImageIO.read(cachedImagePath.toFile());
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

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        User user = event.getUser();

        if (imageLoaded) sendWelcomeImage(event.getGuild(), Vars.welcomeChannel, user);
        else Vars.welcomeChannel.sendMessage("Bienvenue "+ event.getUser().getAsMention() + " !").queue();

        if (user.isBot()) event.getGuild().addRoleToMember(user, Vars.botRole).queue();
        else event.getGuild().addRoleToMember(user, Vars.memberRole).reason("Ajouter automatiquement lorsque le membre a rejoint.").queue();        
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        Vars.welcomeChannel.sendMessage("**"+ event.getUser().getName() + "** nous a quitté...").queue();
    }

    private void sendWelcomeImage(Guild guild, TextChannel channel, User user) {
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

    private BufferedImage getUserAvatar(User user, int size) throws IOException {
        BufferedImage avatar = ImageIO.read(new URL(user.getEffectiveAvatarUrl() + "?size=" + size));

        if (avatar.getWidth() > 128 || avatar.getHeight() > 128) avatar = resizeAvatar(avatar);

        return makeCircularAvatar(avatar);
    }

    private BufferedImage resizeAvatar(BufferedImage avatar) {
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

    private BufferedImage makeCircularAvatar(BufferedImage avatar) {
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

    private ByteArrayInputStream createWelcomeImage(String userName, int guildMemberCount, BufferedImage userAvatar) throws IOException {
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
