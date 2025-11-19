package joseta.annotations;

import java.lang.annotation.*;

/**
 * Annotation to mark a class as an interaction module.
 * <p>
 * Classes annotated with this annotation can have methods annotated with {@link joseta.annotations.types.SlashCommandInteraction SlashCommandInteraction},
 * {@link joseta.annotations.types.ButtonInteraction ButtonInteraction} and other interaction related annotations.
 * <p>
 * It is required for the {@link InteractionProcessor} to discover, register and handle these interactions.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface InteractionModule {}
