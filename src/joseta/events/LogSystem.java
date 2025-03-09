package joseta.events;

import joseta.*;
import joseta.utils.struct.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.audit.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.channel.*;
import net.dv8tion.jda.api.events.channel.update.*;
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

    private <T extends Event> void sendLog(Guild guild, T event){
        EventTypes eventType = EventTypes.getFromEventObject(event);
        if (eventType == null) {
            JosetaBot.logger.warn("Unknown event type: " + event);
            return;
        }

        guild.retrieveAuditLogs()
            .type(eventType.logType)
            .limit(1)
            .queue(
                success -> {
                    if (success.isEmpty()) return;
                    // TODO an actual description using the available info
                    Vars.testChannel.sendMessageEmbeds(eventType.getEmbed()
                        .setFooter(guild.getName(), guild.getIconUrl())
                        .build()
                    ).queue();
                },
                failure -> {
                    JosetaBot.logger.error("Error while logging.", failure);
                }
            );
    }

    public enum EventTypes {
        CHANNEL_CREATE(ChannelCreateEvent.class, ActionType.CHANNEL_CREATE, "Salon créé", "desc", Color.GREEN),
        CHANNEL_DELETE(ChannelDeleteEvent.class, ActionType.CHANNEL_DELETE, "Salon supprimé", "desc", Color.RED);

        public final Class<? extends Event> eventClass;
        public final ActionType logType;
        public final String title, description;
        public final Color color;

        private EventTypes(Class<? extends Event> eventClass, ActionType type, String title, String description, Color color) {
            this.eventClass = eventClass;
            this.logType = type;
            this.title = title;
            this.description = description;
            this.color = color;
        }

        public static EventTypes getFromEventObject(Object event) {
            for (EventTypes ev : values()) {
                if (event.getClass() == ev.eventClass) return ev;
            }
            return null;
        }

        public EmbedBuilder getEmbed() {
            return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(color)
                .setTimestamp(Instant.now());
        }
    }

    @Override
    public void onChannelCreate(ChannelCreateEvent event) {
        sendLog(event.getGuild(), event);
    }

    @Override
    public void onChannelDelete(ChannelDeleteEvent event) {
        sendLog(event.getGuild(), event);
    }

    @Override
    public void onChannelUpdateName(ChannelUpdateNameEvent event) {
        sendLog(event.getGuild(), event);
    }
}
