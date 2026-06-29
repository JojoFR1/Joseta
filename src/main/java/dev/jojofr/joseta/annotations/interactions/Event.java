package dev.jojofr.joseta.annotations.interactions;

import dev.jojofr.joseta.annotations.types.EventPriority;
import dev.jojofr.joseta.utils.Log;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

/**
 * Represents an event with its associated class and method.
 */
public class Event {
    private final MethodHandle handle;
    private final EventPriority priority;
    private final boolean guildOnly;
    
    public Event(Class<?> clazz, Method method, EventPriority priority, boolean guildOnly) {
        this.priority = priority;
        this.guildOnly = guildOnly;
        
        MethodHandle handle = null;
        try {
            handle = MethodHandles.lookup().unreflect(method).bindTo(clazz.getDeclaredConstructor().newInstance());
        } catch (Exception e) {
            Log.err("Failed to pre-instantiate event class: " + clazz.getName(), e);
        }
        this.handle = handle;
    }
    
    public MethodHandle getHandle() { return handle; }
    public EventPriority getPriority() { return priority; }
    public boolean isGuildOnly() { return guildOnly; }
}
