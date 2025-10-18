package joseta.annotations.types;

import java.lang.annotation.*;

import net.dv8tion.jda.api.interactions.commands.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ContextInteraction {
    /** The command name, 1-32 lowercase alphanumeric characters. - Default: method name (separated by capital letters). */
    String name();
    /** The command type, must not be {@link net.dv8tion.jda.api.interactions.commands.Command.Type#SLASH SLASH}. */
    Command.Type type();
}
