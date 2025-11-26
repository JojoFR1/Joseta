package joseta.annotations.types;

import joseta.generated.EventType;

import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;

/**
 * Annotation to mark a method as an event handler.
 * <p>
 * The method must be inside a class that implements {@link joseta.annotations.EventModule EventModule}.
 * <p>
 * It is handled by the {@link joseta.annotations.EventProcessor EventProcessor}, which provides the event to the method.
 * <p>
 * The method must have a single parameter corresponding to the event type specified.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Event {
    /** The type of event to handle.
     * <p>
     * The parameter of the method must match the event value of the event type specified here and not the enum type itself.
     * <p>
     * For example, if the event type is {@link joseta.generated.EventType#MESSAGE_RECEIVED EventType.MESSAGE_RECEIVED},
     * the method parameter must be of type {@link net.dv8tion.jda.api.events.message.MessageReceivedEvent MessageReceivedEvent} and not {@link joseta.generated.EventType EventType}.
     * <p>
     * Be careful with some event that the bot response may trigger itself, for example a {@link joseta.generated.EventType#MESSAGE_RECEIVED EventType.MESSAGE_RECEIVED}
     * with the bot sending a message in response will trigger the event again, causing an infinite loop (or until rate limit is hit).
     * An easy way to avoid this (in this example) is to check if the author of the message is a bot and ignore it.
     */
    EventType type();
    boolean guildOnly() default true;
}
