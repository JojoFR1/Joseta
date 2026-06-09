package dev.jojofr.joseta.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as an interaction module.
 * <p>
 * Classes annotated with this annotation can have methods annotated with {@link dev.jojofr.joseta.annotations.types.interaction.SlashCommandInteraction SlashCommandInteraction},
 * {@link dev.jojofr.joseta.annotations.types.interaction.ContextCommandInteraction ContextCommandInteraction } and {@link dev.jojofr.joseta.annotations.types.interaction.Interaction Interaction}, which will be registered as interactions.
 * <p>
 * It is required for the {@link InteractionProcessor} to discover, register, and handle these interactions.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface InteractionModule {}
