package joseta.annotations;

import joseta.annotations.types.*;

import java.lang.annotation.*;

/**
 * Annotation to mark a class as an interaction module.
 * <p>
 * change comment mentioning interface to annotation
 * <p>
 * Classes annotated with this annotation can have methods annotated with {@link SlashCommandInteraction SlashCommand},
 * {@link ButtonInteraction Button}, and other interaction-related annotations.
 * <p>
 * It is required for the {@link InteractionProcessor CommandProcessor} to discover, register and handle these interactions.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface InteractionModule {}
