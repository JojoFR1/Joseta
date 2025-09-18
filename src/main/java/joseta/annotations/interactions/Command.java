package joseta.annotations.interactions;

import java.lang.reflect.*;
import java.util.*;

public class Command extends Interaction {
    private final List<Parameter> parameters = new ArrayList<>();

    public Command(Class<?> clazz, Method method, String name) {
        super(clazz, method, name);
    }

    public List<Parameter> getParameters() { return parameters; }

    public void addParameter(Parameter parameter) { parameters.add(parameter); }

    public record Parameter(Class<?> type, String name, boolean required, boolean autoComplete) {}
}
