package joseta.events;

import joseta.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.audit.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.*;
import net.dv8tion.jda.api.events.channel.*;
import net.dv8tion.jda.api.hooks.*;

import java.awt.*;
import java.lang.reflect.*;
import java.time.*;

public class LogSystem extends ListenerAdapter {
    // TODO batch log of same type to avoid spam
    // TODO not a priority, first get it working
    // private Seq<Log> logs = new Seq<>();

    // private void sendBatchLogs() {
    //     logs.each(log -> {
            
    //     });
    // }

    private <T extends GenericEvent> void sendLog(T event, Guild guild) {
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

        public final Class<? extends GenericEvent> eventClass;
        public final ActionType logType;
        public final String title, description;
        public final Color color;

        private EventTypes(Class<? extends GenericEvent> eventClass, ActionType type, String title, String description, Color color) {
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

    /**
     * Gets the guild from an event if possible
     * @param event The event to get the guild from
     * @return The guild, or null if the event doesn't have a guild
     */
    private <T extends GenericEvent> Guild getGuildFromEvent(T event) {
        try {
            Method getGuildMethod = event.getClass().getMethod("getGuild");
            return (Guild) getGuildMethod.invoke(event);
        } catch (Exception e) {
            // Method doesn't exist or can't be called
            return null;
        }
    }

    @Override
    public void onGenericEvent(GenericEvent event) {
        Guild guild = getGuildFromEvent(event);
        if (guild == null) {
            JosetaBot.logger.warn("Event doesn't have a guild: " + event);
            return;
        }

        sendLog(event, guild);
    }
}
