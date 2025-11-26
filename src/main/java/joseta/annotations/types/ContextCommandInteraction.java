package joseta.annotations.types;

import java.lang.annotation.*;

/**
 * Annotation to mark a method as a context command interaction.
 * <p>
 * The method must be inside a class that implements {@link joseta.annotations.InteractionModule InteractionModule}.
 * <p>
 * It is handled by the {@link joseta.annotations.InteractionProcessor InteractionProcessor}, which provides the event to the method.
 * <p>
 * The method must have a single parameter of type {@link net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent GenericContextInteractionEvent}
 * or one of its subclasses: {@link net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent UserContextInteractionEvent}
 * or {@link net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent MessageContextInteractionEvent}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ContextCommandInteraction {
    /**
     * The context command name. Defaults to an empty string, which will use the method name (separated by capital letters).
     * <p>
     * The value must be alphanumerical and not null, empty or outside the range of 1-{@link net.dv8tion.jda.api.interactions.commands.build.CommandData#MAX_NAME_LENGTH} characters long,
     * as defined by {@link net.dv8tion.jda.api.interactions.commands.build.CommandData#MAX_NAME_LENGTH MAX_NAME_LENGTH}.
     */
    String name() default "";
    /**
     * The context command type. Defaults to {@link net.dv8tion.jda.api.interactions.commands.Command.Type#UNKNOWN Command.Type.UNKNOWN}.
     * <p>
     * Must be either {@link net.dv8tion.jda.api.interactions.commands.Command.Type#USER USER} or {@link net.dv8tion.jda.api.interactions.commands.Command.Type##MESSAGE MESSAGE}.
     */
    net.dv8tion.jda.api.interactions.commands.Command.Type type() default net.dv8tion.jda.api.interactions.commands.Command.Type.UNKNOWN;
    /**
     * The context command permissions. Defaults to {@link net.dv8tion.jda.api.Permission#UNKNOWN UNKNOWN} (no permission required).
     */
    net.dv8tion.jda.api.Permission[] permissions() default net.dv8tion.jda.api.Permission.UNKNOWN;
    boolean guildOnly() default true;
}
