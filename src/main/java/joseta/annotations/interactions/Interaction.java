package joseta.annotations.interactions;

import java.lang.reflect.*;

/**
 * Represents an interaction (commands, button, ...) with its associated class, method and name.
 */
public class Interaction extends Event {
    private final String name;

    public Interaction(Class<?> clazz, Method method, String name) {
        super(clazz, method);
        this.name = name;
    }
    
    public String getName() { return name; }
}
