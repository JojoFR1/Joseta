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
    /**
     * The command name. Defaults to an empty string, which will use the method name (separated by capital letters).
     * <p>
     * The value must be alphanumerical and not {@code null}, empty or outside the range of 1-{@value net.dv8tion.jda.api.interactions.commands.build.CommandData#MAX_NAME_LENGTH} characters long,
     * as defined by {@link net.dv8tion.jda.api.interactions.commands.build.CommandData#MAX_NAME_LENGTH MAX_NAME_LENGTH}.
     * <p>
     * When using subcommands or subcommand groups, the full command name must be provided, including spaces.
     * <p> For example:
     * <ul>
     *     <li>For a subcommand: {@code "maincommand subcommand"}</li>
     *     <li>For a subcommand group: {@code "maincommand subcommandgroup subcommand"}</li>
     * </ul>
     * <p>
     * Each command, subcommand and subcommand group has their own methods associated with them, you can see a subcommand
     * as its own command under the main command umbrella, and a subcommand group as a container for subcommands.
     * <p>
     * A base command will never exist alone if it has subcommands or subcommand groups, it will always be used with one of them.
     */
    String name() default "";
    /**
     * The command description. Defaults to "No description."
     * <p>
     * The value must not be {@code null}, empty or longer than {@value net.dv8tion.jda.api.interactions.commands.build.CommandData#MAX_DESCRIPTION_LENGTH} characters,
     * as defined by {@link net.dv8tion.jda.api.interactions.commands.build.CommandData#MAX_DESCRIPTION_LENGTH MAX_DESCRIPTION_LENGTH}.
     */
    String description() default "No description.";
    /**
     * The command permissions. Defaults to {@link net.dv8tion.jda.api.Permission#UNKNOWN UNKNOWN} (no permission required).
     * <p>
     * Applies to the base command. The most recent permission specified in the command hierarchy will apply.
     */
    net.dv8tion.jda.api.Permission[] permissions() default net.dv8tion.jda.api.Permission.UNKNOWN;
    /**
     * The command context types. Defaults to {@link net.dv8tion.jda.api.interactions.InteractionContextType#GUILD GUILD} and {@link net.dv8tion.jda.api.interactions.InteractionContextType#BOT_DM BOT_DM}.
     * <p>
     * The values must not be {@code null} or empty.
     * <p>
     * Sets the contexts in which the command can be used. This only has an effect if this command is registered globally.
     * <p>
     * Applies to the base command. The most recent context types specified in the command hierarchy will apply.
     */
    net.dv8tion.jda.api.interactions.InteractionContextType[] contextTypes() default net.dv8tion.jda.api.interactions.InteractionContextType.GUILD;
    /**
     * The command integration types. Defaults to {@link net.dv8tion.jda.api.interactions.IntegrationType#GUILD_INSTALL GUILD_INSTALL}.
     * <p>
     * The values must not be {@code null} or empty.
     * <p>
     * Sets the integration types on which the command can be installed on. This only has an effect if this command is registered globally.
     * <p>
     * Applies to the base command. The most recent integration types specified in the command hierarchy will apply.
     */
    net.dv8tion.jda.api.interactions.IntegrationType[] integrationTypes() default net.dv8tion.jda.api.interactions.IntegrationType.GUILD_INSTALL;
    /**
     * Whether the command is only usable in guilds. Default to {@code true}.
     * <p>
     * If {@code true}, the command will not be usable in DMs.
     */
    boolean guildOnly() default true;
    /**
     * Whether the command is marked as NSFW. Default to {@code false}.
     * <p>
     * If {@code true}, the command will only be usable in NSFW (age-restricted) channels.
     * <p>
     * Note: Age-restricted commands will not show up in direct messages by default unless the user has enabled them in their settings.
     *
     * @see <a href="https://support.discord.com/hc/en-us/articles/10123937946007" target="_blank">Age-Restricted Commands FAQ</a>
     */
    boolean nsfw() default false;
}
