package dev.jojofr.joseta.annotations.interactions;

import dev.jojofr.joseta.utils.Log;

import java.lang.reflect.Method;

/**
 * Represents an interaction (commands, button, ...) with its associated class, method, and name.
 */
public class Interaction {
    private final Class<?> clazz;
    private final Method method;
    private final String name;
    private final boolean guildOnly;
    
    private final Object instance;

    public Interaction(Class<?> clazz, Method method, String name, boolean guildOnly) {
        this.clazz = clazz;
        this.method = method;
        this.guildOnly = guildOnly;
        this.name = name;
        
        Object instance = null;
        try { instance = clazz.getDeclaredConstructor().newInstance(); } catch (Exception e) {
            Log.err("Failed to pre-instantiate interaction class: " + clazz.getName(), e);
        }
        this.instance = instance;
    }
    
    public Class<?> getClazz() { return clazz; }
    public Method getMethod() { return method; }
    public String getName() { return name; }
    public boolean isGuildOnly() { return guildOnly; }
    public Object getInstance() { return instance; }
}
