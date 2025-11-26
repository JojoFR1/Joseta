package joseta.annotations.interactions;

import java.lang.reflect.Method;

/**
 * Represents an event with its associated class and method.
 */
public class Event {
    private final Class<?> clazz;
    private final Method method;
    private final boolean guildOnly;
    
    public Event(Class<?> clazz, Method method, boolean guildOnly) {
        this.clazz = clazz;
        this.method = method;
        this.guildOnly = guildOnly;
    }
    
    public Class<?> getClazz() { return clazz; }
    public Method getMethod() { return method; }
    public boolean isGuildOnly() { return guildOnly; }
}
