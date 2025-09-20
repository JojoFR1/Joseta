package joseta.annotations.types;

import java.lang.annotation.*;

/**
 * Annotation to mark a method parameter as a slash command option.
 * <p>
 * The parameter must be inside a method that is annotated with {@link SlashCommandInteraction SlashCommand},
 * which is inside a class that implements {@link joseta.annotations.InteractionModule InteractionModule}.
 * <p>
 * It is handled by the {@link joseta.annotations.InteractionProcessor InteractionProcessor}, which provides the options values to the method.
 * If the option is not required, the parameter can be null.
 * <p>
 * The parameter type must be one of the following:
 * <ul>
 *     <li>String</li>
 *     <li>int or Integer</li>
 *     <li>long or Long</li>
 *     <li>boolean or Boolean</li>
 *     <li>double or Double</li>
 *     <li>{@link net.dv8tion.jda.api.entities.IMentionable IMentionable}</li>
 *     <li>{@link net.dv8tion.jda.api.entities.User User}</li>
 *     <li>{@link net.dv8tion.jda.api.entities.Member Member}</li>
 *     <li>{@link net.dv8tion.jda.api.entities.Role Role}</li>
 *     <li>{@link net.dv8tion.jda.api.entities.channel.Channel Channel}</li>
 *     <li>{@link net.dv8tion.jda.api.entities.Message.Attachment Attachment}</li>
 * </ul>
 * <p>
 * If the type isn't any of the above, it will be given the {@link net.dv8tion.jda.api.interactions.commands.OptionType OptionType} {@code UNKNOWN}
 * and the command will fail to register.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Option {
    /** The option name, 1-32 lowercase alphanumeric characters. - Default: parameter name */
    String name() default "";
    /** The option description, 1-100 characters - Default: "No description."*/
    String description() default "No description.";
    /** Whether this option is required - Default: false
     * <p> If false, parameter can be null.
     */
    boolean required() default false;
    /** Whether this option supports auto-complete - Default: false
     * <p> Only support parameter type of: String, Integer, or Long.
     * <p> If true, you must handle the auto-complete interaction in your code. NOT YET IMPLEMENTED.
     */
    boolean autoComplete() default false;
}
