package joseta.commands.admin;

import joseta.*;
import joseta.commands.Command;

import arc.struct.*;
import arc.util.*;

import joseta.database.*;
import joseta.database.entry.*;
import joseta.events.misc.*;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.interactions.components.*;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.io.*;
import java.nio.charset.*;
import java.time.*;

public class AdminCommand extends Command {
    private StandardGuildMessageChannel channel;
    private long messageId;
    
    public AdminCommand() {
        super("admin", "Commande administratives.");
        commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
            .addSubcommandGroups(
                new SubcommandGroupData("rules", "Catégorie règles")
                    .addSubcommands(
                        new SubcommandData("send", "Envoie les règles dans un salon.")
                            .addOption(OptionType.CHANNEL, "channel", "Le salon où envoyez les règles.", true),
                        new SubcommandData("update", "Envoie les règles dans un salon.")
                            .addOption(OptionType.CHANNEL,  "channel", "Le salon où envoyez les règles.", true)
                            .addOption(OptionType.STRING,  "message_id", "L'identifiant du message où vous voulez envoyez règles.", true)
                    ),
                new SubcommandGroupData("counting", "Catégorie comptage")
                    .addSubcommands(
                        new SubcommandData("set_number", "Remplace le dernier nombre du salon de comptage.")
                            .addOption(OptionType.INTEGER, "number", "Le nombre à définir comme dernier nombre du salon de comptage.", true),
                        new SubcommandData("reset_number", "Réinitialise le dernier nombre du salon de comptage a 0."),
                        new SubcommandData("reset_author", "Réinitialise l'auteur du dernier message du salon de comptage.")
                    )
            );
    }

    @Override
    protected void runImpl(SlashCommandInteractionEvent event) {
        switch (event.getSubcommandGroup()) {
            case "rules" -> {
                MessageEmbed[] embeds = getEmbeds(event.getGuild());

                if (event.getSubcommandName().equals("update")) {
                    Message message = channel.retrieveMessageById(messageId).complete();
                    if (message == null) {
                        event.reply("Unknown message, please check the ID or the existence of this message.").setEphemeral(true).queue();
                        return;
                    }
                    if (message.getAuthor() != JosetaBot.getBot().getSelfUser()) {
                        event.reply("Message not sent by the bot.").setEphemeral(true).queue();
                        return;
                    }
                    message.editMessageEmbeds(embeds).setComponents(
                        ActionRow.of(Button.success("b-rules_accept", "Accepter"))
                    ).queue();

                    event.reply("Rules updated succesfully.").setEphemeral(true).queue();
                    return;
                }

                Message msg = channel.sendMessage("").setEmbeds(embeds).addActionRow(
                    Button.success("b-rules_accept", "Accepter")
                ).complete();

                msg.getIdLong();
                channel.getIdLong();

                event.reply("Rules sent succesfully.").setEphemeral(true).queue();
            }
            case "counting" -> {
                ConfigEntry config = Database.get(ConfigEntry.class, event.getGuild().getIdLong());
                if (!config.isCountingEnabled()) {
                    event.reply("Le salon de comptage n'est pas activé sur ce serveur.").setEphemeral(true).queue();
                    return;
                }

                if (event.getSubcommandName().equals("reset_author")) {
                    CountingChannel.setAuthorId(-1);
                    event.reply("L'ID du dernier auteur dans comptage a été reinitialiser.").setEphemeral(true).queue();
                    return;
                }

                long number = 0;

                if (event.getSubcommandName().equals("set_number"))
                    number = event.getOption("number", 0L, OptionMapping::getAsLong);

                CountingChannel.setNumber(number);
                event.reply("Le dernier nombre du salon de comptage a été mis à jour.").setEphemeral(true).queue();
            }
        }
    }

    private MessageEmbed[] getEmbeds(Guild guild) {
        String iconUrl = guild.getIconUrl();
        Instant timestamp = Instant.now();
        Seq<MessageEmbed> embeds = new Seq<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("resources/rules.txt"), StandardCharsets.UTF_8))) {
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
            Log.err("An error occured during rules embed building.", e);
            return null;
        }

        return embeds.toArray(MessageEmbed.class);
    }

    @Override
    protected void getArgs(SlashCommandInteractionEvent event) {
        if (event.getSubcommandName().equals("rules")) {
            channel = event.getOption("channel", null, OptionMapping::getAsChannel).asStandardGuildMessageChannel();
            if (event.getSubcommandName().equals("update")) messageId = Long.parseLong(event.getOption("message_id", null, OptionMapping::getAsString));
        }
    }
}
