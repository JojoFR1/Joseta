package dev.jojofr.joseta.annotations.types;

import dev.jojofr.joseta.annotations.InteractionModule;
import dev.jojofr.joseta.annotations.InteractionProcessor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a method as a context command interaction.
 * <p>
 * The method must be inside a class that implements {@link InteractionModule InteractionModule}.
 * <p>
 * It is handled by the {@link InteractionProcessor InteractionProcessor}, which provides the event to the method.
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
     * The value must be alphanumerical and not {@code null}, empty or outside the range of 1-{@link net.dv8tion.jda.api.interactions.commands.build.CommandData#MAX_NAME_LENGTH} characters long,
     * as defined by {@link net.dv8tion.jda.api.interactions.commands.build.CommandData#MAX_NAME_LENGTH MAX_NAME_LENGTH}.
     */
    String name() default "";
    /**
     * The context command type. Defaults to {@link net.dv8tion.jda.api.interactions.commands.Command.Type#UNKNOWN Command.Type.UNKNOWN}.
     * <p>
     * Must be either {@link net.dv8tion.jda.api.interactions.commands.Command.Type#USER USER} or {@link net.dv8tion.jda.api.interactions.commands.Command.Type#MESSAGE MESSAGE}.
     */
    net.dv8tion.jda.api.interactions.commands.Command.Type type() default net.dv8tion.jda.api.interactions.commands.Command.Type.UNKNOWN;
    /**
     * The context command permissions. Defaults to {@link net.dv8tion.jda.api.Permission#UNKNOWN UNKNOWN} (no permission required).
     */
    net.dv8tion.jda.api.Permission[] permissions() default net.dv8tion.jda.api.Permission.UNKNOWN;
    /**
     * The context command context types. Defaults to {@link net.dv8tion.jda.api.interactions.InteractionContextType#GUILD GUILD} and {@link net.dv8tion.jda.api.interactions.InteractionContextType#BOT_DM BOT_DM}.
     * <p>
     * The values must not be {@code null} or empty.
     * <p>
     * Sets the contexts in which the context command can be used. This only has an effect if this context command is registered globally.
     */
    net.dv8tion.jda.api.interactions.InteractionContextType[] contextTypes() default {net.dv8tion.jda.api.interactions.InteractionContextType.GUILD, net.dv8tion.jda.api.interactions.InteractionContextType.BOT_DM};
    /**
     * The context command integration types. Defaults to {@link net.dv8tion.jda.api.interactions.IntegrationType#GUILD_INSTALL GUILD_INSTALL}.
     * <p>
     * The values must not be {@code null} or empty.
     * <p>
     * Sets the integration types on which the context command can be installed on. This only has an effect if this context command is registered globally.
     */
    net.dv8tion.jda.api.interactions.IntegrationType[] integrationTypes() default net.dv8tion.jda.api.interactions.IntegrationType.GUILD_INSTALL;
    /**
     * Whether the context command is only usable in guilds. Default to {@code true}.
     * <p>
     * If {@code true}, the context command will not be usable in DMs.
     */
    boolean guildOnly() default true;
    /**
     * Whether the context command is marked as NSFW. Default to {@code false}.
     * <p>
     * If {@code true}, the context command will only be usable in NSFW (age-restricted) channels.
     * <p>
     * Note: Age-restricted commands will not show up in direct messages by default unless the user has enabled them in their settings.
     *
     * @see <a href="https://support.discord.com/hc/en-us/articles/10123937946007" target="_blank">Age-Restricted Commands FAQ</a>
     */
    boolean nsfw() default false;
}
