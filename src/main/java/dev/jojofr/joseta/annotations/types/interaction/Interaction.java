package dev.jojofr.joseta.annotations.types.interaction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a method as an interaction.
 * <p>
 * The method must be inside a class that implements {@link dev.jojofr.joseta.annotations.InteractionModule InteractionModule}.
 * <p>
 * It is handled by the {@link dev.jojofr.joseta.annotations.InteractionProcessor InteractionProcessor}, which provides the event to the method.
 * <p>
 * The method must have a single {@link net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Interaction {
    /**
     * The interaction id. Defaults to an empty string, which will use the method name.
     * <p>
     * The value must be alphanumerical and not {@code null}, empty or outside the range of 1 to the max length defined
     * by the interaction type. (e.g. {@value net.dv8tion.jda.api.components.buttons.Button#ID_MAX_LENGTH} for buttons, {@value net.dv8tion.jda.api.components.selections.SelectMenu#ID_MAX_LENGTH} for select menus, etc.)
     * <p>
     * For the method to be called, the button id must match the id used when creating the button.
     */
    String id() default "";
    /**
     * Whether the interaction is only usable in guilds. Default to {@code true}.
     * <p>
     * If {@code true}, the interaction will not be usable in DMs.
     */
    boolean guildOnly() default true;
}
