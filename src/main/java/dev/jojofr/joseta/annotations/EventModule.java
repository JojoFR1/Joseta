package dev.jojofr.joseta.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as an event module.
 * <p>
 * Classes annotated with this annotation can have methods annotated with {@link dev.jojofr.joseta.annotations.types.Event Event}.
 * <p>
 * It is required for the {@link EventProcessor} to discover, register and handle the events.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EventModule {}
