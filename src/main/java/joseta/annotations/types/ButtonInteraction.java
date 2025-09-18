package joseta.annotations.types;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ButtonInteraction {
    String id() default "";
}
