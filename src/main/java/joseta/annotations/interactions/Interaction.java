package joseta.annotations.interactions;

import java.lang.reflect.*;

public class Interaction {
    private final Class<?> clazz;
    private final Method method;
    private final String name;

    public Interaction(Class<?> clazz, Method method, String name) {
        this.clazz = clazz;
        this.method = method;
        this.name = name;
    }

    public Class<?> getClazz() { return clazz; }
    public Method getMethod() { return method; }
    public String getName() { return name; }
}
