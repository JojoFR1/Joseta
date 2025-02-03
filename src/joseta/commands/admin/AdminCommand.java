package joseta.commands.admin;

import joseta.*;
import joseta.commands.Command;
import joseta.utils.struct.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.io.*;
import java.nio.charset.*;
import java.time.*;

public class AdminCommand extends Command {
    private TextChannel channel;
    
    public AdminCommand() {
        super("admin", "Commande administratives.",
            DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR),
            new SubcommandData("send-rules", "Envoie les règles dans un salon.")
                .addOption(OptionType.CHANNEL, "channel", "Le salon où envoyez les règles.", true)
            
        );
    }

    @Override
    protected void runImpl(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        String iconUrl = guild.getIconUrl();

        Instant timestamp = Instant.now();
        Seq<MessageEmbed> embeds = new Seq<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("resources/rules.txt"), StandardCharsets.UTF_8), 8192)) {
            String description = "";
            EmbedBuilder embed = null;
            String line;

            while ((line = reader.readLine()) != null) {

                if (line.equals("---STARTEMBED---")) { // Start a new embed
                    String[] rgbValues = reader.readLine().split(", ");
                    Color color = new Color(Integer.parseInt(rgbValues[0]), Integer.parseInt(rgbValues[1]), Integer.parseInt(rgbValues[2]));

                    String title = reader.readLine();
                    embed = new EmbedBuilder()
                        .setTitle(title)
                        .setColor(color)       // Required due to some emojis taking more than 1 characters.
                        .setFooter(title.substring(title.indexOf('┃') + 1) + " - " + guild.getName(), iconUrl)
                        .setTimestamp(timestamp);
                    
                    description = "";
                } else if (line.equals("---ENDEMBED---")) { // Current embed is finished, save it.
                    embed.setDescription(description);
                    embeds.add(embed.build());
                } else description += line + "\n"; // Simple line to add.
            }
        } catch (IOException e) {
            JosetaBot.logger.error("An error occured during rules embed building.", e);
            return;
        }
        
        channel.sendMessage("").addEmbeds(embeds.toArray(MessageEmbed.class))
            .addActionRow(Button.success("rule-accept", "Accepter")).queue();
        
        event.reply("Rules sent succesfully.").setEphemeral(true).queue();
    }

    @Override
    protected void getArgs(SlashCommandInteractionEvent event) {
        try {
            channel = event.getOption("channel").getAsChannel().asTextChannel();
        } catch (IllegalStateException e) {
            event.reply("Le salon n'est pas un salon de texte !").queue();
            return;
        }
    }
}
