package joseta.events;

import joseta.*;
import joseta.util.*;

import net.dv8tion.jda.api.events.guild.member.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.hooks.*;
import net.dv8tion.jda.api.utils.*;

import java.awt.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;

public class WelcomeMessage extends ListenerAdapter {
    private static final Font font = new Font("Arial", Font.PLAIN, 60);
    private static final Color color = Color.WHITE;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("testw")) return;

        try {
            createWelcomeImage(event.getUser().getName());
            event.reply("Hey").addFiles(FileUpload.fromData(new File("images/welcomeImage.png"))).queue();
        } catch (Exception e) {
            Vars.logger.error("", e);
            return;
        }
    }

    private void createWelcomeImage(String userName) throws Exception {
        BufferedImage image = CachedData.getWelcomeImage();
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
            g2d.setColor(color);
            g2d.setFont(font);
            g2d.drawString(String.format("Hi %s", userName), 100, 100);
        } finally {
            g2d.dispose();
        }

        ImageIO.write(processedImage, "png", new File("images/welcomeImage.png"));
    }
    
    // @Override
    // public void onGuildMemberJoin(GuildMemberJoinEvent event){
    // }
}
