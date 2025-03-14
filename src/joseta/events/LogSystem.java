package joseta.events;

import joseta.*;
import joseta.utils.func.*;

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

    private <T extends GenericEvent> void sendLog(T event, Guild guild) {
        EventType eventType = EventType.getFromEventObject(event);
        if (eventType == EventType.NONE) {
            JosetaBot.logger.warn("Unknown event type: " + event);
            return;
        }

        guild.retrieveAuditLogs()
            .type(eventType.logType)
            .limit(1)
            .queue(
                success -> {
                    if (success.isEmpty()) return;
                    
                    Vars.testChannel.sendMessageEmbeds(eventType.getEmbed(guild, event)).queue();
                },
                failure -> {
                    JosetaBot.logger.error("Error while logging.", failure);
                }
            );
    }

    public enum EventType {
        NONE(null, null, (guild, event) -> new EmbedBuilder().setDescription("Error").setFooter(guild.getName(), guild.getIconUrl()).setTimestamp(Instant.now()).build()),
        CHANNEL_CREATE(ChannelCreateEvent.class, ActionType.CHANNEL_CREATE, (guild, event) -> new EmbedBuilder().setTitle("Salon créé").setDescription("desc").setColor(Color.GREEN).setFooter(guild.getName(), guild.getIconUrl()).setTimestamp(Instant.now()).build()),
        CHANNEL_DELETE(ChannelDeleteEvent.class, ActionType.CHANNEL_DELETE, (guild, event) -> new EmbedBuilder().setTitle("Salon supprimé").setDescription("desc").setColor(Color.RED).setFooter(guild.getName(), guild.getIconUrl()).setTimestamp(Instant.now()).build());

        public final Class<? extends GenericEvent> eventClass;
        public final Func2<Guild, ? extends GenericEvent, MessageEmbed> embed;
        public final ActionType logType;

        private <T extends GenericEvent> EventType(Class<T> eventClass, ActionType type, Func2<Guild, T, MessageEmbed> embed) {
            this.eventClass = eventClass;
            this.embed = embed;
            this.logType = type;
        }
        
        @SuppressWarnings("unchecked")
        public <T extends GenericEvent> MessageEmbed getEmbed(Guild guild, T event) {
            if (eventClass.isInstance(event)) return ((Func2<Guild, T, MessageEmbed>) embed).get(guild, event);
            return new EmbedBuilder().setDescription("Error: Event type mismatch").build();
        }

        public static EventType getFromEventObject(Object event) {
            for (EventType ev : values()) {
                if (event.getClass() == ev.eventClass) return ev;
            }
            return EventType.NONE;
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
