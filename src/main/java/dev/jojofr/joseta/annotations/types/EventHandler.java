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
