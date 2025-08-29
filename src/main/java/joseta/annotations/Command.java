package joseta.annotations;

import java.lang.reflect.*;
import java.util.*;

public class Command {
    private final Class<?> clazz;
    private final Method method;
    private final String name;
    private final List<Parameter> parameters = new ArrayList<>();

    public Command(Class<?> clazz, Method method, String name) {
        this.clazz = clazz;
        this.method = method;
        this.name = name;
    }

    public Class<?> getClazz() { return clazz; }
    public Method getMethod() { return method; }
    public String getName() { return name; }
    public List<Parameter> getParameters() { return parameters; }

    public void addParameter(Parameter parameter) { parameters.add(parameter); }

    public static class Parameter {
        private final Class<?> type;
        private final String name;
        private final boolean required;

        public Parameter(Class<?> type, String name, boolean required) {
            this.type = type;
            this.name = name;
            this.required = required;
        }

        public Class<?> getType() { return type; }
        public String getName() { return name; }
        public boolean isRequired() { return required; }
    }
}
