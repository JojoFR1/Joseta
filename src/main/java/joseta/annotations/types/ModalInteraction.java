package joseta.annotations.types;

import java.lang.annotation.*;

/**
 * Annotation to mark a method as a modal interaction.
 * <p>
 * The method must be inside a class that implements {@link joseta.annotations.InteractionModule InteractionModule}.
 * <p>
 * It is handled by the {@link joseta.annotations.InteractionProcessor InteractionProcessor}, which provides the event to the method.
 * <p>
 * The method must have a single parameter of type {@link net.dv8tion.jda.api.events.interaction.ModalInteractionEvent ModalInteractionEvent}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ModalInteraction {
    /**
     * The modal id. Defaults to an empty string, which will use the method name.
     * <p>
     * The value must be alphanumerical and not {@code null}, empty or outside the range of 1-{@value net.dv8tion.jda.api.modals.Modal#MAX_ID_LENGTH} characters long,
     * as defined by {@link net.dv8tion.jda.api.modals.Modal#MAX_ID_LENGTH MAX_ID_LENGTH}.
     * <p>
     * For the method to be called, the modal id must match the id used when creating the modal.
     */
    String id() default "";
    /**
     * Whether the modal is only usable in guilds. Default to {@code true}.
     * <p>
     * If {@code true}, the modal will not be usable in DMs.
     */
    boolean guildOnly() default true;
}
