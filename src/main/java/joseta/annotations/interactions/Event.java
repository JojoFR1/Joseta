package joseta.annotations.interactions;

import java.lang.reflect.Method;

public class Event {
    private final Class<?> clazz;
    private final Method method;
    
    public Event(Class<?> clazz, Method method) {
        this.clazz = clazz;
        this.method = method;
    }
    
    public Class<?> getClazz() { return clazz; }
    public Method getMethod() { return method; }
}
