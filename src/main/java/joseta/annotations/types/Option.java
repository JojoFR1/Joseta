package joseta.annotations.types;

import java.lang.annotation.*;

/**
 * Annotation to mark a method parameter as a slash command option.
 * <p>
 * The parameter must be inside a method that is annotated with {@link SlashCommandInteraction SlashCommand},
 * which is inside a class that implements {@link joseta.annotations.InteractionModule InteractionModule}.
 * <p>
 * It is handled by the {@link joseta.annotations.InteractionProcessor InteractionProcessor}, which provides the options values to the method.
 * If the option is not required, the parameter can be {@code null}.
 * <p>
 * The parameter type must be one of the following:
 * <ul>
 *     <li>{@code String}</li>
 *     <li>{@code int} or {@code Integer}</li>
 *     <li>{@code long} or {@code Long}</li>
 *     <li>{@code boolean} or {@code Boolean}</li>
 *     <li>{@code double} or {@code Double}</li>
 *     <li>{@link net.dv8tion.jda.api.entities.IMentionable IMentionable}</li>
 *     <li>{@link net.dv8tion.jda.api.entities.User User}</li>
 *     <li>{@link net.dv8tion.jda.api.entities.Member Member}</li>
 *     <li>{@link net.dv8tion.jda.api.entities.Role Role}</li>
 *     <li>{@link net.dv8tion.jda.api.entities.channel.Channel Channel}</li>
 *     <li>{@link net.dv8tion.jda.api.entities.Message.Attachment Attachment}</li>
 * </ul>
 * <p>
 * If the type isn't any of the above, it will be given the type of {@link net.dv8tion.jda.api.interactions.commands.OptionType#UNKNOWN OptionType.UNKNOWN}
 * and the command will fail to register.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Option {
    /**
     * The option name. Defaults to an empty string, which will use the parameter name.
     * <p>
     * The value must be alphanumerical and not {@code null}, empty or outside the range of 1-{@value net.dv8tion.jda.api.interactions.commands.build.OptionData#MAX_NAME_LENGTH} characters long,
     * as defined by {@link net.dv8tion.jda.api.interactions.commands.build.OptionData#MAX_NAME_LENGTH MAX_NAME_LENGTH}.
     */
    String name() default "";
    /**
     * The option description. Defaults to "No description."
     * <p>
     * The value must not be {@code null}, empty or longer than {@value net.dv8tion.jda.api.interactions.commands.build.OptionData#MAX_DESCRIPTION_LENGTH} characters,
     * as defined by {@link net.dv8tion.jda.api.interactions.commands.build.OptionData#MAX_DESCRIPTION_LENGTH MAX_DESCRIPTION_LENGTH}.
     */
    String description() default "No description.";
    /**
     * Whether the user must set this option.
     * <p>
     * If {@code false}, the parameter can be {@code null}.
     */
    boolean required() default false;
    /**
     * Whether this option should support auto-complete interaction via {@link net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent CommandAutoCompleteInteractionEvent}.
     * <p>
     * Will only be applied if the {@link net.dv8tion.jda.api.interactions.commands.OptionType type of this option} support auto-complete.
     * <p>
 *   * If {@code true}, the auto-complete event must be handled in order to provide options to the user.
     */
    boolean autoComplete() default false;
    /**
     * The channel types restriction for this option.
     * <p>
     * Will only be applied if the {@link net.dv8tion.jda.api.interactions.commands.OptionType type of this option} is {@link net.dv8tion.jda.api.interactions.commands.OptionType#CHANNEL CHANNEL}.
     */
    net.dv8tion.jda.api.entities.channel.ChannelType[] channelTypes() default net.dv8tion.jda.api.entities.channel.ChannelType.UNKNOWN;
    /**
     * The minimum value which can be provided for this option.
     * <p>
     * Will only be applied if the {@link net.dv8tion.jda.api.interactions.commands.OptionType type of this option} is {@link net.dv8tion.jda.api.interactions.commands.OptionType#INTEGER INTEGER} or {@link net.dv8tion.jda.api.interactions.commands.OptionType#NUMBER NUMBER}.
     * <p> The value must be greater than or equal to {@link net.dv8tion.jda.api.interactions.commands.build.OptionData#MIN_NEGATIVE_NUMBER MIN_NEGATIVE_NUMBER}.
     */
    long minValue() default Long.MIN_VALUE;
    /**
     * The maximum value which can be provided for this option.
     * <p>
     * Will only be applied if the {@link net.dv8tion.jda.api.interactions.commands.OptionType type of this option} is {@link net.dv8tion.jda.api.interactions.commands.OptionType#INTEGER INTEGER} or {@link net.dv8tion.jda.api.interactions.commands.OptionType#NUMBER NUMBER}.
     * <p> The value must be less than or equal to {@link net.dv8tion.jda.api.interactions.commands.build.OptionData#MAX_POSITIVE_NUMBER MAX_POSITIVE_NUMBER}.
     */
    long maxValue() default Long.MAX_VALUE;
    /**
     * The minimum length of the string which can be provided for this option.
     * <p>
     * Will only be applied if the {@link net.dv8tion.jda.api.interactions.commands.OptionType type of this option} is {@link net.dv8tion.jda.api.interactions.commands.OptionType#STRING STRING}.
     * <p> The value must be positive.
     */
    int minLength() default -1;
    /**
     * The maximum length of the string which can be provided for this option.
     * <p>
     * Will only be applied if the {@link net.dv8tion.jda.api.interactions.commands.OptionType type of this option} is {@link net.dv8tion.jda.api.interactions.commands.OptionType#STRING STRING}.
     * <p> The value must be positive and less than or equal to {@value net.dv8tion.jda.api.interactions.commands.build.OptionData#MAX_STRING_OPTION_LENGTH},
     * as defined by {@link net.dv8tion.jda.api.interactions.commands.build.OptionData#MAX_STRING_OPTION_LENGTH MAX_STRING_OPTION_LENGTH}.
     */
    int maxLength() default -1;
}
