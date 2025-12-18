package joseta.commands;

import joseta.annotations.*;
import joseta.annotations.types.*;
import joseta.annotations.types.SlashCommandInteraction;
import joseta.database.Database;
import joseta.database.entities.Configuration;
import joseta.events.misc.CountingChannel;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.components.actionrow.*;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.label.*;
import net.dv8tion.jda.api.components.selections.*;
import net.dv8tion.jda.api.components.textdisplay.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.events.interaction.component.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.modals.*;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@InteractionModule
public class AdminCommands {
    private static final ActionRow rulesAcceptButton = ActionRow.of(Button.success("btn-rules_accept", "Accepter"));
    
    //#region Rules Commands
    @SlashCommandInteraction(name = "admin rules send", description = "Envoie les règles dans un salon.")
    public void rulesSend(SlashCommandInteractionEvent event,
                          @Option(description = "Le salon où envoyez les règles.", required = true) GuildMessageChannel channel)
    {
        List<MessageEmbed> embeds = buildRulesEmbeds(event.getGuild());
        channel.sendMessageEmbeds(embeds).setComponents(rulesAcceptButton).queue(
            s -> event.reply("Les règles ont été envoyées avec succès dans " + channel.getAsMention() + ".").setEphemeral(true).queue(),
            f -> event.reply("Échec de l'envoi des règles dans " + channel.getAsMention() + ". Veuillez réessayer plus tard.").setEphemeral(true).queue()
        );
        
        //TODO save message ID to database to update later
    }
    
    @SlashCommandInteraction(name = "admin rules update", description = "Met à jour les règles dans un salon.")
    public void rulesUpdate(SlashCommandInteractionEvent event,
                            @Option(description = "Le salon où le message des règles se trouvent", required = true) GuildMessageChannel channel,
                            @Option(description = "L'identifiant du message.", required = true) Long messageId)
    {
        Message message = channel.retrieveMessageById(messageId).complete();
        if (message == null) {
            event.reply("Message inconnu, veuillez vérifier l'ID ou l'existence de ce message.").setEphemeral(true).queue();
            return;
        }
        if (message.getAuthor() != event.getJDA().getSelfUser()) {
            event.reply("Le message n'a pas été envoyé par le bot.").setEphemeral(true).queue();
            return;
        }
        
        List<MessageEmbed> embeds = buildRulesEmbeds(event.getGuild());
        message.editMessageEmbeds(embeds).setComponents(rulesAcceptButton).queue(
            s -> event.reply("Les règles ont été mises à jour avec succès dans " + channel.getAsMention() + ".").setEphemeral(true).queue(),
            f -> event.reply("Échec de la mise à jour des règles dans " + channel.getAsMention() + ". Veuillez réessayer plus tard.").setEphemeral(true).queue()
        );
    }
    
    @ButtonInteraction(id = "btn-rules_accept")
    public void rulesAccept(ButtonInteractionEvent event) {
        Configuration config = Database.get(Configuration.class, event.getGuild().getIdLong());
        
        Role joinRole, verifiedRole;
        if (config.joinRoleId == null || (joinRole = event.getGuild().getRoleById(config.joinRoleId)) == null) return;
        if (config.verifiedRoleId == null || (verifiedRole = event.getGuild().getRoleById(config.verifiedRoleId)) == null) return;
        
        event.getGuild().removeRoleFromMember(event.getUser(), joinRole).queue();
        event.getGuild().addRoleToMember(event.getUser(), verifiedRole).queue();
        event.deferEdit().queue();
    }
    
    private static final String RULES_EMBED_START = "---STARTEMBED---";
    private static final String RULES_EMBED_END = "---ENDEMBED---";
    private List<MessageEmbed> buildRulesEmbeds(Guild guild) {
        Configuration config = Database.get(Configuration.class, guild.getIdLong());
        
        String rules = config.rules;
        if (rules == null || rules.isBlank()) return List.of();
        
        List<MessageEmbed> embeds = new ArrayList<>();
        
        EmbedBuilder embedBuilder = null;
        StringBuilder embedDescription = null;
        Instant timestamp = Instant.now();
        
        String[] lines = rules.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            
            if (line.equals(RULES_EMBED_START)) {
                i++;
                String[] rgbValues = lines[i].split(",");
                Color color = new Color(
                    Integer.parseInt(rgbValues[0]),
                    Integer.parseInt(rgbValues[1]),
                    Integer.parseInt(rgbValues[2])
                );
                
                i++;
                String title = lines[i];
                embedBuilder = new EmbedBuilder()
                    .setTitle(title)
                    .setColor(color)
                    .setFooter(title.substring(title.indexOf('┃') + 1) + " - " + guild.getName(), guild.getIconUrl())
                    .setTimestamp(timestamp);
                
                embedDescription = new StringBuilder();
            } else if (line.equals(RULES_EMBED_END)) {
                embedBuilder.setDescription(embedDescription.toString());
                embeds.add(embedBuilder.build());
            } else embedDescription.append(line).append('\n');
        }
        
        return embeds;
    }
    //#endregion
    
    //#region Counting Commands
    @SlashCommandInteraction(name = "admin counting set_number", description = "Définit le nombre actuel pour le système de comptage.")
    public void countingSetNumber(SlashCommandInteractionEvent event, @Option(description = "Le nombre à définir.", required = true) Long number) {
        Configuration config = Database.get(Configuration.class, event.getGuild().getIdLong());
        if (!config.countingEnabled) {
            event.reply("Le comptage est désactivé sur ce serveur.").setEphemeral(true).queue();
            return;
        }
        
        CountingChannel.setNumber(number);
        event.reply("Le dernier nombre du salon de comptage a été mis à jour.").setEphemeral(true).queue();
    }
    
    @SlashCommandInteraction(name = "admin counting reset_number", description = "Réinitialise le nombre actuel à 0 pour le système de comptage.")
    public void countingResetNumber(SlashCommandInteractionEvent event) {
        Configuration config = Database.get(Configuration.class, event.getGuild().getIdLong());
        if (!config.countingEnabled) {
            event.reply("Le comptage est désactivé sur ce serveur.").setEphemeral(true).queue();
            return;
        }
        
        CountingChannel.setNumber(0L);
        event.reply("Le dernier nombre du salon de comptage a été réinitialisé à 0.").setEphemeral(true).queue();
    }
    
    @SlashCommandInteraction(name = "admin counting reset_author", description = "Réinitialise l'auteur du dernier nombre pour le système de comptage.")
    public void countingResetAuthor(SlashCommandInteractionEvent event) {
        Configuration config = Database.get(Configuration.class, event.getGuild().getIdLong());
        if (!config.countingEnabled) {
            event.reply("Le comptage est désactivé sur ce serveur.").setEphemeral(true).queue();
            return;
        }
        
        CountingChannel.setAuthorId(-1);
        event.reply("L'ID du dernier auteur dans comptage a été reinitialiser.").setEphemeral(true).queue();
    }
    //#endregion
}
