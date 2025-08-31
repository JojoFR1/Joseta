package joseta.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Option {
    /** The option name, 1-32 lowercase alphanumeric characters */
    String name() default "";
    /** The option description, 1-100 characters - Default: "No description."*/
    String description() default "No description.";
    /** Whether this option is required - Default: false
     * <p> If false, parameter can be null.
     */
    boolean required() default false;
    /** Whether this option supports auto-complete - Default: false
     * <p> Only support parameter type of: String, Integer, or Long.
     * <p> If true, you must handle the auto-complete interaction in your code. NOT YET IMPLEMENTED.
     */
    boolean autoComplete() default false;
}
