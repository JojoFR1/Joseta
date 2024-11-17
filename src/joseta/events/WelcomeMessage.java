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

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("testw")) return;

        try {
            modifyImage(event.getUser().getName());
            event.reply("Hey").addFiles(FileUpload.fromData(new File("images/welcomeImage.png"))).queue();
        } catch (Exception e) {
            Vars.logger.error("", e);
            return;
        }
    }

    private void modifyImage(String userName) throws Exception {
        BufferedImage image = CachedData.getWelcomeImage();
        BufferedImage processedImage = new BufferedImage(
            image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR
        );
            
        Graphics2D g2d = processedImage.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.setFont(g2d.getFont().deriveFont(60f));
        g2d.drawString("Hi " + userName, 100, 100);
        g2d.dispose();

        try (BufferedOutputStream output = new BufferedOutputStream(
                new FileOutputStream("images/welcomeImage.png"))) {
            ImageIO.write(processedImage, "png", output);
        }
    }
    
    // @Override
    // public void onGuildMemberJoin(GuildMemberJoinEvent event){
    // }
}
