package joseta.annotations;

import java.lang.annotation.*;

/**
 * Annotation to mark a class as an event module.
 * <p>
 * Classes annotated with this annotation can have methods annotated with {@link joseta.annotations.types.Event Event}.
 * <p>
 * It is required for the {@link EventProcessor} to discover, register and handle the events.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EventModule {}
