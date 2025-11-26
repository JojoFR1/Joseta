package joseta.annotations.types;

import java.lang.annotation.*;

/**
 * Annotation to mark a method as a slash command.
 * <p>
 * The method must be inside a class that implements {@link joseta.annotations.InteractionModule InteractionModule}.
 * <p>
 * It is handled by the {@link joseta.annotations.InteractionProcessor InteractionProcessor}, which provides the event and options to the method.
 * <p>
 * The method must have a single parameter of type {@link net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent SlashCommandInteractionEvent},
 * followed by a maximum of 25 parameters annotated with {@link Option Option}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SlashCommandInteraction {
    /** The command name, 1-32 lowercase alphanumeric characters. - Default: method name (separated by capital letters)
     * <p>
     * Each command and subcommand has their own methods associated with them, you can see a subcommand as its own command under the main command umbrella.
     * <p>
     * The name can be formatted as follows:
     *     <ul>
     *         <li>{@code command} (cannot be used alone if there are subcommands)</li>
     *         <li>{@code command subcommand}</li>
     *         <li>{@code command subcommandGroup subcommand}</li>
     *     </ul>
     */
    String name() default "";
    /** The command description, 1-100 characters - Default: "No description."*/
    String description() default "No description.";
    /** The required permission to use the command (enable for this permission) - Default: {@link net.dv8tion.jda.api.Permission#UNKNOWN UNKNOWN} (no permission required)
     * <p>
     *  Applied to the base command. If subcommands specify their own permission, it will override the previous one.
     */
    net.dv8tion.jda.api.Permission[] permissions() default net.dv8tion.jda.api.Permission.UNKNOWN;
    boolean guildOnly() default true;
}
