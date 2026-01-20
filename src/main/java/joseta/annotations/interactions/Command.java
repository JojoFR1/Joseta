package joseta.annotations.interactions;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a command interaction with its associated class, method, name and parameters.
 */
public class Command extends Interaction {
    private final List<Parameter> parameters = new ArrayList<>();

    public Command(Class<?> clazz, Method method, String name, boolean guildOnly) {
        super(clazz, method, name, guildOnly);
    }

    public List<Parameter> getParameters() { return parameters; }

    public void addParameter(Parameter parameter) { parameters.add(parameter); }

    public record Parameter(Class<?> type, String name, boolean required, boolean autoComplete) {}
}
