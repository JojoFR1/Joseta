package joseta.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Option {
    String name() default "";
    String description() default "No description.";
    boolean required() default false;
}
