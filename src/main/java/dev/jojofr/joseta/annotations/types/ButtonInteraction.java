package dev.jojofr.joseta.annotations.types;

import dev.jojofr.joseta.annotations.InteractionModule;
import dev.jojofr.joseta.annotations.InteractionProcessor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a method as a button interaction.
 * <p>
 * The method must be inside a class that implements {@link InteractionModule InteractionModule}.
 * <p>
 * It is handled by the {@link InteractionProcessor InteractionProcessor}, which provides the event to the method.
 * <p>
 * The method must have a single parameter of type {@link net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent ButtonInteractionEvent}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ButtonInteraction {
    /**
     * The button id. Defaults to an empty string, which will use the method name.
     * <p>
     * The value must be alphanumerical and not {@code null}, empty or outside the range of 1-{@value net.dv8tion.jda.api.components.buttons.Button#ID_MAX_LENGTH} characters long,
     * as defined by {@link net.dv8tion.jda.api.components.buttons.Button#ID_MAX_LENGTH ID_MAX_LENGTH}.
     * <p>
     * For the method to be called, the button id must match the id used when creating the button.
     */
    String id() default "";
    /**
     * Whether the button, is only usable in guilds. Default to {@code true}.
     * <p>
     * If {@code true}, the button, will not be usable in DMs.
     */
    boolean guildOnly() default true;
}
