package joseta.events;

import joseta.*;

import net.dv8tion.jda.api.events.guild.member.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.hooks.*;
import net.dv8tion.jda.api.utils.*;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;

import javax.imageio.*;

public class WelcomeMessage extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("testw")) return;

        try {
            event.reply("Hey").addFiles(FileUpload.fromData(modifyImage(event.getUser().getName()))).queue();
        } catch (Exception e) {
            Vars.logger.error("", e);
            return;
        }
    }

    private File modifyImage(String userName) throws Exception {
        BufferedImage image = ImageIO.read(new URL("https://probot.media/tZFvtyOXuC.png"));
            
        Graphics g = image.getGraphics();
        g.setFont(g.getFont().deriveFont(60f));
        g.drawString("Hi " + userName, 100, 100);
        g.dispose();

        File imageFile = new File("test.png");
        ImageIO.write(image, "png", imageFile);

        return imageFile;
    }
    
    // @Override
    // public void onGuildMemberJoin(GuildMemberJoinEvent event){
    // }
}
