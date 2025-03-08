package joseta.events;

import joseta.*;
import joseta.utils.struct.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.audit.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.*;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.channel.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.hooks.*;

import java.awt.*;
import java.time.*;

public class LogSystem extends ListenerAdapter {
    // TODO batch log of same type to avoid spam
    // TODO not a priority, first get it working
    // private Seq<Log> logs = new Seq<>();

    // private void sendBatchLogs() {
    //     logs.each(log -> {
            
    //     });
    // }

    private void sendLog(Log log){
        log.guild.retrieveAuditLogs()
            .type(log.type)
            .limit(1)
            .queue(
                success -> {
                    if (success.isEmpty()) return;

                    EmbedBuilder embed = new EmbedBuilder()
                        .setTitle(log.title)
                        .setDescription(log.description)
                        .setColor(log.color)
                        .setFooter(log.guild.getName(), log.guild.getIconUrl())
                        .setTimestamp(Instant.now());
        
                    Vars.testChannel.sendMessageEmbeds(embed.build()).queue();        
                },
                failure -> {
                    JosetaBot.logger.error("Error while logging.", failure);
                }
            );
    }

    private class Log {
        public final ActionType type;
        public final Guild guild;
        public final String title, description;
        public final Color color;

        public Log(ActionType type, Guild guild, String title, String description, Color color) {
            this.type = type;
            this.guild = guild;
            this.title = title;
            this.description = description;
            this.color = color;
        }
    }

    @Override
    public void onGenericChannel(GenericChannelEvent event) {
        if (event instanceof ChannelCreateEvent) {
            sendLog(new Log(ActionType.CHANNEL_CREATE, event.getGuild(), "Salon créé", event.getChannel().getName(), Color.GREEN));
        } else if (event instanceof ChannelDeleteEvent) {
            sendLog(new Log(ActionType.CHANNEL_DELETE, event.getGuild(), "Salon supprimé", event.getChannel().getName(), Color.RED));
        }
    }
}
