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
     * The modal ID, 1-100 characters. - Default: method name
     * <p>
     * Must match the ID used when creating the modal.
     * <p>
     * The ID must not exceed the limit of {@link net.dv8tion.jda.api.modals.Modal#MAX_ID_LENGTH MAX_ID_LENGTH} defined as {@value net.dv8tion.jda.api.modals.Modal#MAX_ID_LENGTH}.
     */
    String id() default "";
}
