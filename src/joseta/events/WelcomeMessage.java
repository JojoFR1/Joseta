package joseta.events;

import joseta.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.*;
import net.dv8tion.jda.api.events.interaction.command.*;
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
        //TODO Maybe best to load it in a separate area to be reused?
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, new File("resources/Audiowide-Regular.ttf")).deriveFont(20f);
        } catch (Exception e) {
            Vars.logger.error("Couldn't load font file. Defaulted to 'Arial'", e);
            font = new Font("Arial", Font.PLAIN, 30);
        }
    }

    // TODO temporary while testing.
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("testw")) return;
        if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) return;
        User user = event.getUser();

        try {
            BufferedImage avatar = makeCircularAvatar(ImageIO.read(new URL(user.getEffectiveAvatarUrl() + "?size=128")));
            int guildMemberCount = event.getGuild().getMemberCount();
            String userName = user.getGlobalName() + " ("+ user.getName() +")";
            ByteArrayInputStream image = createWelcomeImage(userName, guildMemberCount, avatar);

            event.reply(user.getAsMention()).addFiles(FileUpload.fromData(image, "welcome.png")).queue();
            
            Files.deleteIfExists(Paths.get("resources", "userAvatar.png"));
        } catch (Exception e) {
            Vars.logger.error("An error occured while proccessing welcome image.", e);
            return;
        }

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
            
            g2d.setFont(font);
            g2d.setColor(new Color(244, 204, 122));
            g2d.drawString(userName, 220, 80);
            
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawString(Integer.toString(guildMemberCount), 250, 130);
        } finally {
            g2d.dispose();
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(processedImage, "png", output);

        return new ByteArrayInputStream(output.toByteArray());
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
    
    // TODO just todo
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        event.getGuild().getTextChannelById("1243926805275217963").sendMessage("No way y a un nouveau.");
    }

    // TODO todo again
    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        event.getGuild().getTextChannelById("1243926805275217963").sendMessage("No way y a une personne qui a quitter.");
    }
}
