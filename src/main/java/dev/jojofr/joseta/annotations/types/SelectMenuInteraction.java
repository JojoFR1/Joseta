package dev.jojofr.joseta.annotations.types;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a method as a select menu interaction.
 * <p>
 * The method must be inside a class that implements {@link dev.jojofr.joseta.annotations.InteractionModule InteractionModule}.
 * <p>
 * It is handled by the {@link dev.jojofr.joseta.annotations.InteractionProcessor InteractionProcessor}, which provides the event to the method.
 * <p>
 * The method must have a single parameter of type {@link net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent GenericSelectMenuInteractionEvent}
 * or one of its subclasses: {@link net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent StringSelectInteractionEvent}
 * or {@link net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent EntitySelectInteractionEvent}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SelectMenuInteraction {
    /**
     * The select menu id. Defaults to an empty string, which will use the method name.
     * <p>
     * The value must be alphanumerical and not {@code null}, empty or outside the range of 1-{@value net.dv8tion.jda.api.components.selections.SelectMenu#ID_MAX_LENGTH} characters long,
     * as defined by {@link net.dv8tion.jda.api.components.selections.SelectMenu#ID_MAX_LENGTH ID_MAX_LENGTH}.
     * <p>
     * For the method to be called, the select menu id must match the id used when creating the select menu.
     */
    String id() default "";
    /**
     * Whether the select menu is only usable in guilds. Default to {@code true}.
     * <p>
     * If {@code true}, the select menu will not be usable in DMs.
     */
    boolean guildOnly() default true;
}
