package joseta.events;

import joseta.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.audit.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.channel.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.events.message.*;
import net.dv8tion.jda.api.hooks.*;

import java.awt.*;
import java.time.*;

public class LogSystem extends ListenerAdapter {

    @SuppressWarnings("unused")
    private void sendLog(Guild guild, String description){
        MessageEmbed embed = new EmbedBuilder()
            .build();
        
        Vars.testChannel.sendMessageEmbeds(embed).queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("logtest")) return;

        Guild guild = event.getGuild();

        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("Title")
            .setDescription("Putain la fonction aura besoin de plein de parametres/check pour bien tout faire")
            .setColor(Color.GRAY)
            .setFooter(guild.getName(), guild.getIconUrl())
            .setTimestamp(Instant.now());
        
        event.replyEmbeds(embed.build()).queue();
    }

    @Override
    public void onChannelCreate(ChannelCreateEvent event) {
        event.getGuild().retrieveAuditLogs()
            .type(ActionType.CHANNEL_CREATE)
            .limit(1)
            .queue(
            success -> {
                if (!success.isEmpty()) {
                    AuditLogEntry entry = success.get(0);
                    Vars.testChannel.sendMessage("CHANNEL CREATED - Channel: " + event.getChannel() + 
                        "\n| By: " + entry.getUser()).queue();
                }
            },
            failure -> {
                JosetaBot.logger.error("Error while logging (message delete).", failure);
            }
        );
    }

    @Override
    public void onChannelDelete(ChannelDeleteEvent event) {
        event.getGuild().retrieveAuditLogs()
            .type(ActionType.CHANNEL_DELETE)
            .limit(1)
            .queue(
            success -> {
                if (!success.isEmpty()) {
                    AuditLogEntry entry = success.get(0);
                    Vars.testChannel.sendMessage("CHANNEL DELETED - Channel: " + event.getChannel() + 
                        "\n| By: " + entry.getUser()).queue();
                }
            },
            failure -> {
                JosetaBot.logger.error("Error while logging (message delete).", failure);
            }
        );
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        // Message old = event.getChannel().retrieveMessageById(event.getMessageIdLong()).complete();
        event.getGuild().retrieveAuditLogs()
            .type(ActionType.MESSAGE_DELETE)
            .limit(1)
            .queue(
            success -> {
                if (!success.isEmpty()) {
                    AuditLogEntry entry = success.get(0);
                    String deletedBy = entry.getUser() != null ? entry.getUser().getAsTag() : "Unknown";
                    Vars.testChannel.sendMessage("MESSAGE DELETED - Channel: " + event.getChannel() + 
                        "\n| Deleted by: " + deletedBy + 
                        "\n| Message ID: " + event.getMessageId()).queue();
                        // "\n| Message: " + old).queue();
                }
            },
            failure -> {
                JosetaBot.logger.error("Error while logging (message delete).", failure);
            } 
        );
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        Message old = event.getChannel().retrieveMessageById(event.getMessageIdLong()).complete();
        Vars.testChannel.sendMessage("MESSAGE UPDATED - Channel: " + event.getChannel() + 
            "\n| By: " + event.getAuthor() + 
            "\n| Message (Old): " + old +
            "\n| Message (New): " + event.getMessage()).queue();
    }
}
