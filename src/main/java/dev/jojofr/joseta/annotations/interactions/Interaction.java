package dev.jojofr.joseta.annotations.interactions;

import java.lang.reflect.Method;

/**
 * Represents an interaction (commands, button, ...) with its associated class, method and name.
 */
public class Interaction {
    private final Class<?> clazz;
    private final Method method;
    private final String name;
    private final boolean guildOnly;

    public Interaction(Class<?> clazz, Method method, String name, boolean guildOnly) {
        this.clazz = clazz;
        this.method = method;
        this.guildOnly = guildOnly;
        this.name = name;
    }
    
    public Class<?> getClazz() { return clazz; }
    public Method getMethod() { return method; }
    public String getName() { return name; }
    public boolean isGuildOnly() { return guildOnly; }
}
