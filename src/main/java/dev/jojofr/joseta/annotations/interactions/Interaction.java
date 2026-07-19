package dev.jojofr.joseta.annotations.interactions;

import dev.jojofr.joseta.utils.Log;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

/**
 * Represents an interaction (commands, button, ...) with its associated class, method, and name.
 */
public class Interaction {
    protected MethodHandle handle;
    private final String name;
    private final boolean guildOnly;
    
    public Interaction(Class<?> clazz, Method method, String name, boolean guildOnly) {
        this.guildOnly = guildOnly;
        this.name = name;
        
        MethodHandle handle = null;
        try {
            handle = MethodHandles.lookup().unreflect(method).bindTo(clazz.getDeclaredConstructor().newInstance());
        } catch (Exception e) {
            Log.err("Failed to pre-instantiate interaction class: " + clazz.getName(), e);
        }
        this.handle = handle;
    }
    
    public MethodHandle getHandle() { return handle; }
    public String getName() { return name; }
    public boolean isGuildOnly() { return guildOnly; }
}
