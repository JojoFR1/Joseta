package dev.jojofr.joseta.annotations.types;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a method as an event handler.
 * <p>
 * The method must be inside a class that implements {@link dev.jojofr.joseta.annotations.EventModule EventModule}.
 * <p>
 * It is handled by the {@link dev.jojofr.joseta.annotations.EventProcessor EventProcessor}, which provides the event to the method.
 * <p>
 * The method must have a single parameter corresponding to the event type specified.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandler {
    /**
     * The event type to handle.
     * <p>
     * The method annotated with this annotation will be called when the specified event is fired.
     * <p>
     * The method must have a single parameter matching the event value of the specified event type.
     * <p> For example:
     * <ul>
     *     <li>If the event type is {@link dev.jojofr.joseta.generated.EventType#MESSAGE_RECEIVED EventType.MESSAGE_RECEIVED},
     *         the method parameter must be of type {@link net.dv8tion.jda.api.events.message.MessageReceivedEvent MessageReceivedEvent}.</li>
     *     <li>If the event type is {@link dev.jojofr.joseta.generated.EventType#GUILD_MEMBER_JOIN EventType.GUILD_MEMBER_JOIN},
     *         the method parameter must be of type {@link net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent GuildMemberJoinEvent}.</li>
     *     <li>And so on for each event type defined in {@link dev.jojofr.joseta.generated.EventType EventType}.</li>
     * </ul>
     * <p>
     * Warning: Be cautious of events that your bot's responses may trigger again, potentially causing infinite loops.
     * <p>
     * One common example is responding to a {@link dev.jojofr.joseta.generated.EventType#MESSAGE_RECEIVED EventType.MESSAGE_RECEIVED} event by sending a message,
     * which could trigger the same event repeatedly. To prevent this, consider implementing checks such as verifying if the message author is a bot and ignoring such messages.
     */
    dev.jojofr.joseta.generated.EventType type();
    
    /**
     * The priority of the event handler. Default to {@link EventPriority#NORMAL EventPriority.NORMAL}.
     * <p>
     * If multiple event handlers are registered for the same event type, they will be executed in the order of their priority, with higher priority handlers being executed first.
     */
    EventPriority priority() default EventPriority.NORMAL;
    
    /**
     * Whether the command is only usable in guilds. Default to {@code true}.
     * <p>
     * If {@code true}, the command will not be usable in DMs.
     */
    boolean guildOnly() default true;
    
    /**
     * The priority levels for event handlers, determining the order of execution when multiple handlers are registered for the same event type.
     * <p>
     * Handlers with higher priority will be executed before those with lower priority.
     * <p>
     * Multiple handlers with the same priority will be executed in a random order.
     */
    enum EventPriority {
        /** The event will be executed before all other events with lower priority of the same type. */
        HIGH,
        /** The default priority level. The event will be executed after all other events with higher priority and before all other events with lower priority of the same type. */
        NORMAL,
        /** The event will be executed after all other events with higher and normal priority of the same type. */
        LOW,
        /** The event won't be registered at startup, effectively disabling it. */
        DISABLED
    }
}
