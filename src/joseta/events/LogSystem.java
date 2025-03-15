package joseta.events;

import joseta.*;
import joseta.utils.func.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.audit.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.*;
import net.dv8tion.jda.api.events.channel.*;
import net.dv8tion.jda.api.events.channel.update.*;
import net.dv8tion.jda.api.hooks.*;

import java.awt.*;

public class LogSystem extends ListenerAdapter {

    @Override
    public void onGenericEvent(GenericEvent event) {
        EventType eventType = EventType.getFromEvent(event);
        if (eventType == null) {
            JosetaBot.logger.warn("Unknown event type: " + event);
            return;
        }

        Vars.testChannel.sendMessageEmbeds(eventType.getEmbed(event)).queue();
    }

    public enum EventType {
        CHANNEL_CREATE(ChannelCreateEvent.class,
                       event -> Vars.getDefaultEmbed(Color.GREEN, event.getGuild())
                                  .setTitle("Salon créé")
                                  .setDescription("Salon créé: " + event.getChannel().getAsMention() + " by " + retrieveModerator(event.getGuild(), ActionType.CHANNEL_CREATE).getAsMention())
                                  .build()
        ),
        CHANNEL_DELETE(ChannelDeleteEvent.class,
                       event -> Vars.getDefaultEmbed(Color.RED, event.getGuild())
                                  .setTitle("Salon supprimé")
                                  .setDescription("desc" + " by " + retrieveModerator(event.getGuild(), ActionType.CHANNEL_DELETE).getAsMention())
                                  .build()
        ),
        CHANNEL_UPDATE_TOPIC(ChannelUpdateTopicEvent.class,
                             event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                        .setTitle("Salon mis a jour (Topic)")
                                        .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.CHANNEL_UPDATE).getAsMention())
                                        .build()
        ),
        CHANNEL_UPDATE_SLOWMODE(ChannelUpdateSlowmodeEvent.class,
                                event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                           .setTitle("Salon mis a jour (Slowmode)")
                                           .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.CHANNEL_UPDATE).getAsMention())
                                           .build()
        ),
        CHANNEL_UPDATE_NAME(ChannelUpdateNameEvent.class,
                            event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                       .setTitle("Salon renommé")
                                       .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.CHANNEL_UPDATE).getAsMention())
                                       .build()
        ),
        CHANNEL_UPDATE_NSFW(ChannelUpdateNSFWEvent.class,
                            event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                       .setTitle("Salon mis a jour (NSFW)")
                                       .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.CHANNEL_UPDATE).getAsMention())
                                       .build()
        );

        private final Class<? extends GenericEvent> eventClass;
        private final Func<? extends GenericEvent, MessageEmbed> embed;
        
        private <T extends GenericEvent> EventType(Class<T> eventClass, Func<T, MessageEmbed> embed) {
            this.eventClass = eventClass;
            this.embed = embed;
        }
        
        @SuppressWarnings("unchecked")
        public <T extends GenericEvent> MessageEmbed getEmbed(T event) {
            if (eventClass.isInstance(event)) return ((Func<T, MessageEmbed>) embed).get(event);
            return new EmbedBuilder().setDescription("Error: Event type mismatch").build();
        }

        public static EventType getFromEvent(GenericEvent event) {
            for (EventType eventType : values())
                if (event.getClass() == eventType.eventClass) return eventType;
            return null;
        }
    }

    private static User retrieveModerator(Guild guild, ActionType actionType) {
        return guild.retrieveAuditLogs()
                .type(actionType)
                .limit(1)
                .complete()
                .get(0)
                .getUser();
    }
}
