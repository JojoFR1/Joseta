package joseta.annotations.types;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SelectMenuInteraction {
    String id() default "";
    boolean guildOnly() default true;
}
