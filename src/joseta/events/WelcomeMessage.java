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
    static {
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, new File("resources/Audiowide-Regular.ttf")).deriveFont(25f);
        } catch (Exception e) {
            Vars.logger.error("Couldn't load font file. Defaulted to 'Arial'", e);
            font = new Font("Arial", Font.PLAIN, 30);
        }
    }

    // TODO Un-hardcode the channel ID
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        TextChannel channel = event.getGuild().getTextChannelById("1256989659448348673");
        User user = event.getUser();

        try {
            String name = "@"+user.getName();
            String globalName = user.getGlobalName();
            String userName = globalName == null ? name : globalName + " ("+ name +")";
            
            int guildMemberCount = event.getGuild().getMemberCount();
            BufferedImage avatar = makeCircularAvatar(ImageIO.read(new URL(user.getEffectiveAvatarUrl() + "?size=128")));

            ByteArrayInputStream image = createWelcomeImage(userName, guildMemberCount, avatar);
            channel.sendMessage(user.getAsMention()).addFiles(FileUpload.fromData(image, "welcome.png")).queue();
            
            Files.deleteIfExists(Paths.get("resources", "userAvatar.png"));
        } catch (Exception e) {
            Vars.logger.error("An error occured while proccessing welcome image.", e);
            return;
        }
    }

    // TODO Un-hardcode the channel ID
    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        event.getGuild().getTextChannelById("1256989659448348673").sendMessage("**"+ event.getUser().getName() + "** nous a quitté...").queue();
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

    private ByteArrayInputStream createWelcomeImage(String userName, int guildMemberCount, BufferedImage userAvatar) throws Exception {
        BufferedImage image = Vars.welcomeImage;
        BufferedImage processedImage = new BufferedImage(
            image.getWidth(),
            image.getHeight(),
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

            g2d.drawImage(image, 0, 0, null);
            g2d.drawImage(userAvatar, 62, 14, null);
            
            // Adapative font size based of name lenght - support the maximum of 67 characters.
            g2d.setFont(font);
            FontMetrics fm = g2d.getFontMetrics();
            int userNameWidth = fm.stringWidth(userName);
            int imageWidth = image.getWidth() - 222 - 7; // Position of the te - offset
            float widhtRatio = userNameWidth > imageWidth ? (float) imageWidth / userNameWidth : 1.0f;

            g2d.setFont(font.deriveFont(25f * widhtRatio));
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
