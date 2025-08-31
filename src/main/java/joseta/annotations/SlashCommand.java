package joseta.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SlashCommand {
    /** The command name, 1-32 lowercase alphanumeric characters */
    String name() default "";
    /** The command description, 1-100 characters - Default: "No description."*/
    String description() default "No description.";
}
