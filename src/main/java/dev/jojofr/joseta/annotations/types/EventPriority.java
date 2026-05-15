package dev.jojofr.joseta.annotations.types;

/**
 * The priority levels for event handlers, determining the order of execution when multiple handlers are registered for the same event type.
 * <p>
 * Handlers with higher priority will be executed before those with lower priority.
 * <p>
 * Multiple handlers with the same priority will be executed in a random order.
 */
public enum EventPriority {
    /** The event will be executed before all other events with lower priority of the same type. */
    HIGH,
    /** The default priority level. The event will be executed after all other events with higher priority and before all other events with lower priority of the same type. */
    NORMAL,
    /** The event will be executed after all other events with higher and normal priority of the same type. */
    LOW,
    /** The event won't be registered at startup, effectively disabling it. */
    DISABLED
}