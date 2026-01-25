package dev.jojofr.joseta.annotations.interactions;

import java.lang.reflect.Method;

/**
 * Represents an interaction (commands, button, ...) with its associated class, method and name.
 */
public class Interaction extends Event {
    private final String name;

    public Interaction(Class<?> clazz, Method method, String name, boolean guildOnly) {
        super(clazz, method, guildOnly);
        this.name = name;
    }
    
    public String getName() { return name; }
}
