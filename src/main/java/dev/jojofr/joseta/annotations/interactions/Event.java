package dev.jojofr.joseta.annotations.interactions;

import java.lang.reflect.Method;

/**
 * Represents an event with its associated class and method.
 */
public class Event {
    private final Class<?> clazz;
    private final Method method;
    private final dev.jojofr.joseta.annotations.types.EventHandler.EventPriority priority;
    private final boolean guildOnly;
    
    public Event(Class<?> clazz, Method method, dev.jojofr.joseta.annotations.types.EventHandler.EventPriority priority, boolean guildOnly) {
        this.clazz = clazz;
        this.method = method;
        this.priority = priority;
        this.guildOnly = guildOnly;
    }
    
    public Class<?> getClazz() { return clazz; }
    public Method getMethod() { return method; }
    public dev.jojofr.joseta.annotations.types.EventHandler.EventPriority getPriority() { return priority; }
    public boolean isGuildOnly() { return guildOnly; }
}
