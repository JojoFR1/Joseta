package joseta.commands;

import joseta.database.*;
import joseta.database.ConfigDatabase.*;
import joseta.utils.*;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

public abstract class ModCommand extends Command {
    protected User user;
    protected Member member;
    protected String reason;
    protected long time;
    protected String defaultTime = "5m";
    
    /**
     * Define a specially made {@link SlashCommandData Slash Command} for moderation purpose that will later be registered on the bot initialization.
     * 
     * <p> No options, subcommands or subcommand groups - default permission (enabled for everyone).
     * 
     * <p>Predefined with the following arguments (MUST be defined or please check for null or override the {@link #getArgs(SlashCommandInteractionEvent) getArgs()} method):
     * <ul>
     * <li><b>user</b> - The user to sanction, required.</li>
     * <li><b>reason</b> - The reason of the sanction, default to "Raison par défaut".</li>
     * <li><b>time</b> - The time of the sanction, default to "5m".</li>
     * </ul>
     * 
     * <p>Predefined with the following checks (can be overridden in the {@link #check(SlashCommandInteractionEvent) check()} method):
     * <ul>
     * <li>Is the moderation enabled on the server.</li>
     * <li>Is the user not null or empty.</li>
     * <li>Is the user not the same as the member who executed the command.</li>
     * <li>Is the user not a bot or a system account.</li>
     * <li>Does the user have a role lower than the member who executed the command.</li>
     * <li>Is the user not the owner of the server.</li>
     * </ul>
     * 
     * <p><b>These values CANNOT be null or empty, otherwise an {@link IllegalArgumentException} will be thrown.</b>
     * @param name The command name, 1-32 lowercase alphanumeric characters.
     * @param description The command description, 1-100 characters.
     */
    protected ModCommand(String name, String description) {
        super(name, description, DefaultMemberPermissions.ENABLED);
    }
    /**
     * Define a specially made {@link SlashCommandData Slash Command} for moderation purpose that will later be registered on the bot initialization.
     * 
     * <p> No options, subcommands or subcommand groups.
     * 
     * <p>Predefined with the following arguments (MUST be defined or please check for null or override the {@link #getArgs(SlashCommandInteractionEvent) getArgs()} method):
     * <ul>
     * <li><b>user</b> - The user to sanction, required.</li>
     * <li><b>reason</b> - The reason of the sanction, default to "Raison par défaut".</li>
     * <li><b>time</b> - The time of the sanction, default to "5m".</li>
     * </ul>
     * 
     * <p>Predefined with the following checks (can be overridden in the {@link #check(SlashCommandInteractionEvent) check()} method):
     * <ul>
     * <li>Is the moderation enabled on the server.</li>
     * <li>Is the user not null or empty.</li>
     * <li>Is the user not the same as the member who executed the command.</li>
     * <li>Is the user not a bot or a system account.</li>
     * <li>Does the user have a role lower than the member who executed the command.</li>
     * <li>Is the user not the owner of the server.</li>
     * </ul>
     * 
     * <p><b>These values CANNOT be null or empty, otherwise an {@link IllegalArgumentException} will be thrown.</b>
     * @param name The command name, 1-32 lowercase alphanumeric characters.
     * @param description The command description, 1-100 characters.
     * @param defaultPermissions {@link DefaultMemberPermissions} representing the default permissions of this command.
     */
    protected ModCommand(String name, String description, DefaultMemberPermissions defaultPermissions) {
        super(name, description, defaultPermissions, new OptionData[0]);
    }

    /**
     * Define a specially made {@link SlashCommandData Slash Command} for moderation purpose that will later be registered on the bot initialization.
     * 
     * <p> Incompatible with subcommands or subcommand groups - default permission (enabled for everyone).
     * 
     * <p>Predefined with the following arguments (MUST be defined or please check for null or override the {@link #getArgs(SlashCommandInteractionEvent) getArgs()} method):
     * <ul>
     * <li><b>user</b> - The user to sanction, required.</li>
     * <li><b>reason</b> - The reason of the sanction, default to "Raison par défaut".</li>
     * <li><b>time</b> - The time of the sanction, default to "5m".</li>
     * </ul>
     * 
     * <p>Predefined with the following checks (can be overridden in the {@link #check(SlashCommandInteractionEvent) check()} method):
     * <ul>
     * <li>Is the moderation enabled on the server.</li>
     * <li>Is the user not null or empty.</li>
     * <li>Is the user not the same as the member who executed the command.</li>
     * <li>Is the user not a bot or a system account.</li>
     * <li>Does the user have a role lower than the member who executed the command.</li>
     * <li>Is the user not the owner of the server.</li>
     * </ul>
     * 
     * <p><b>These values CANNOT be null or empty, otherwise an {@link IllegalArgumentException} will be thrown.</b>
     * @param name The command name, 1-32 lowercase alphanumeric characters.
     * @param description The command description, 1-100 characters.
     * @param options Add up to 25 {@link OptionData Options} to this command, name must be unique and required options must be registered before non-required.
     */
    protected ModCommand(String name, String description, OptionData... options) {
        super(name, description, DefaultMemberPermissions.ENABLED, options);
    }
    /**
     * Define a specially made {@link SlashCommandData Slash Command} for moderation purpose that will later be registered on the bot initialization.
     * 
     * <p> Incompatible with options, no subcommand groups - default permission (enabled for everyone).
     * 
     * <p>Predefined with the following arguments (MUST be defined or please check for null or override the {@link #getArgs(SlashCommandInteractionEvent) getArgs()} method):
     * <ul>
     * <li><b>user</b> - The user to sanction, required.</li>
     * <li><b>reason</b> - The reason of the sanction, default to "Raison par défaut".</li>
     * <li><b>time</b> - The time of the sanction, default to "5m".</li>
     * </ul>
     * 
     * <p>Predefined with the following checks (can be overridden in the {@link #check(SlashCommandInteractionEvent) check()} method):
     * <ul>
     * <li>Is the moderation enabled on the server.</li>
     * <li>Is the user not null or empty.</li>
     * <li>Is the user not the same as the member who executed the command.</li>
     * <li>Is the user not a bot or a system account.</li>
     * <li>Does the user have a role lower than the member who executed the command.</li>
     * <li>Is the user not the owner of the server.</li>
     * </ul>
     * 
     * <p><b>These values CANNOT be null or empty, otherwise an {@link IllegalArgumentException} will be thrown.</b>
     * @param name The command name, 1-32 lowercase alphanumeric characters.
     * @param description The command description, 1-100 characters.
     * @param subcommands Add up to 25 {@link SubcommandData Subcommmands} to this command, name must be unique.
     */
    protected ModCommand(String name, String description, SubcommandData... subcommands) {
        super(name, description, DefaultMemberPermissions.ENABLED, subcommands);
    }
    /**
     * Define a specially made {@link SlashCommandData Slash Command} for moderation purpose that will later be registered on the bot initialization.
     * 
     * <p> Incompatible with options, no subcommands - default permission (enabled for everyone).
     * 
     * <p>Predefined with the following arguments (MUST be defined or please check for null or override the {@link #getArgs(SlashCommandInteractionEvent) getArgs()} method):
     * <ul>
     * <li><b>user</b> - The user to sanction, required.</li>
     * <li><b>reason</b> - The reason of the sanction, default to "Raison par défaut".</li>
     * <li><b>time</b> - The time of the sanction, default to "5m".</li>
     * </ul>
     * 
     * <p>Predefined with the following checks (can be overridden in the {@link #check(SlashCommandInteractionEvent) check()} method):
     * <ul>
     * <li>Is the moderation enabled on the server.</li>
     * <li>Is the user not null or empty.</li>
     * <li>Is the user not the same as the member who executed the command.</li>
     * <li>Is the user not a bot or a system account.</li>
     * <li>Does the user have a role lower than the member who executed the command.</li>
     * <li>Is the user not the owner of the server.</li>
     * </ul>
     * 
     * <p><b>These values CANNOT be null or empty, otherwise an {@link IllegalArgumentException} will be thrown.</b>
     * @param name The command name, 1-32 lowercase alphanumeric characters.
     * @param description The command description, 1-100 characters.
     * @param subcommandGroups Add up to 25 {@link SubcommandGroupData Subcommmand-Groups} to this command, name must be unique.
     */
    protected ModCommand(String name, String description, SubcommandGroupData... subcommandGroups) {
        super(name, description, DefaultMemberPermissions.ENABLED, subcommandGroups);
    }
    /**
     * Define a specially made {@link SlashCommandData Slash Command} for moderation purpose that will later be registered on the bot initialization.
     * 
     * <p> Incompatible with options - default permission (enabled for everyone).
     * 
     * <p>Predefined with the following arguments (MUST be defined or please check for null or override the {@link #getArgs(SlashCommandInteractionEvent) getArgs()} method):
     * <ul>
     * <li><b>user</b> - The user to sanction, required.</li>
     * <li><b>reason</b> - The reason of the sanction, default to "Raison par défaut".</li>
     * <li><b>time</b> - The time of the sanction, default to "5m".</li>
     * </ul>
     * 
     * <p>Predefined with the following checks (can be overridden in the {@link #check(SlashCommandInteractionEvent) check()} method):
     * <ul>
     * <li>Is the moderation enabled on the server.</li>
     * <li>Is the user not null or empty.</li>
     * <li>Is the user not the same as the member who executed the command.</li>
     * <li>Is the user not a bot or a system account.</li>
     * <li>Does the user have a role lower than the member who executed the command.</li>
     * <li>Is the user not the owner of the server.</li>
     * </ul>
     * 
     * <p><b>These values CANNOT be null or empty, otherwise an {@link IllegalArgumentException} will be thrown.</b>
     * @param name The command name, 1-32 lowercase alphanumeric characters.
     * @param description The command description, 1-100 characters.
     * @param subcommands Add up to 25 {@link SubcommandData Subcommmands} to this command, name must be unique.
     * @param subcommandGroups Add up to 25 {@link SubcommandGroupData Subcommmand-Groups} to this command, name must be unique.
     */
    protected ModCommand(String name, String description, SubcommandData[] subcommands, SubcommandGroupData... subcommandGroups) {
        super(name, description, DefaultMemberPermissions.ENABLED, subcommands, subcommandGroups);
    }

    /**
     * Define a specially made {@link SlashCommandData Slash Command} for moderation purpose that will later be registered on the bot initialization.
     * 
     * <p> Incompatible with subcommands and subcommand groups.
     * 
     * <p>Predefined with the following arguments (MUST be defined or please check for null or override the {@link #getArgs(SlashCommandInteractionEvent) getArgs()} method):
     * <ul>
     * <li><b>user</b> - The user to sanction, required.</li>
     * <li><b>reason</b> - The reason of the sanction, default to "Raison par défaut".</li>
     * <li><b>time</b> - The time of the sanction, default to "5m".</li>
     * </ul>
     * 
     * <p>Predefined with the following checks (can be overridden in the {@link #check(SlashCommandInteractionEvent) check()} method):
     * <ul>
     * <li>Is the moderation enabled on the server.</li>
     * <li>Is the user not null or empty.</li>
     * <li>Is the user not the same as the member who executed the command.</li>
     * <li>Is the user not a bot or a system account.</li>
     * <li>Does the user have a role lower than the member who executed the command.</li>
     * <li>Is the user not the owner of the server.</li>
     * </ul>
     * 
     * <p><b>These values CANNOT be null or empty, otherwise an {@link IllegalArgumentException} will be thrown.</b>
     * @param name The command name, 1-32 lowercase alphanumeric characters.
     * @param description The command description, 1-100 characters.
     * @param defaultPermissions {@link DefaultMemberPermissions} representing the default permissions of this command.
     * @param options Add up to 25 {@link OptionData Options} to this command, name must be unique and required options must be registered before non-required.
     */
    protected ModCommand(String name, String description, DefaultMemberPermissions defaultPermissions, OptionData... options) {
        super(name, description, defaultPermissions, options);
    }
    /**
     * Define a specially made {@link SlashCommandData Slash Command} for moderation purpose that will later be registered on the bot initialization.
     * 
     * <p> Incompatible with options, no subcommand groups.
     * 
     * <p>Predefined with the following arguments (MUST be defined or please check for null or override the {@link #getArgs(SlashCommandInteractionEvent) getArgs()} method):
     * <ul>
     * <li><b>user</b> - The user to sanction, required.</li>
     * <li><b>reason</b> - The reason of the sanction, default to "Raison par défaut".</li>
     * <li><b>time</b> - The time of the sanction, default to "5m".</li>
     * </ul>
     * 
     * <p>Predefined with the following checks (can be overridden in the {@link #check(SlashCommandInteractionEvent) check()} method):
     * <ul>
     * <li>Is the moderation enabled on the server.</li>
     * <li>Is the user not null or empty.</li>
     * <li>Is the user not the same as the member who executed the command.</li>
     * <li>Is the user not a bot or a system account.</li>
     * <li>Does the user have a role lower than the member who executed the command.</li>
     * <li>Is the user not the owner of the server.</li>
     * </ul>
     * 
     * <p><b>These values CANNOT be null or empty, otherwise an {@link IllegalArgumentException} will be thrown.</b>
     * @param name The command name, 1-32 lowercase alphanumeric characters.
     * @param description The command description, 1-100 characters.
     * @param defaultPermissions {@link DefaultMemberPermissions} representing the default permissions of this command.
     * @param subcommands Add up to 25 {@link SubcommandData Subcommmands} to this command, name must be unique.
     */
    protected ModCommand(String name, String description, DefaultMemberPermissions defaultPermissions, SubcommandData... subcommands) {
        super(name, description, defaultPermissions, subcommands);
    }
    /**
     * Define a specially made {@link SlashCommandData Slash Command} for moderation purpose that will later be registered on the bot initialization.
     * 
     * <p> Incompatible with options, no subcommands
     * 
     * <p>Predefined with the following arguments (MUST be defined or please check for null or override the {@link #getArgs(SlashCommandInteractionEvent) getArgs()} method):
     * <ul>
     * <li><b>user</b> - The user to sanction, required.</li>
     * <li><b>reason</b> - The reason of the sanction, default to "Raison par défaut".</li>
     * <li><b>time</b> - The time of the sanction, default to "5m".</li>
     * </ul>
     * 
     * <p>Predefined with the following checks (can be overridden in the {@link #check(SlashCommandInteractionEvent) check()} method):
     * <ul>
     * <li>Is the moderation enabled on the server.</li>
     * <li>Is the user not null or empty.</li>
     * <li>Is the user not the same as the member who executed the command.</li>
     * <li>Is the user not a bot or a system account.</li>
     * <li>Does the user have a role lower than the member who executed the command.</li>
     * <li>Is the user not the owner of the server.</li>
     * </ul>
     * 
     * <p><b>These values CANNOT be null or empty, otherwise an {@link IllegalArgumentException} will be thrown.</b>
     * @param name The command name, 1-32 lowercase alphanumeric characters.
     * @param description The command description, 1-100 characters.
     * @param defaultPermissions {@link DefaultMemberPermissions} representing the default permissions of this command.
     * @param subcommandGroups Add up to 25 {@link SubcommandGroupData Subcommmand-Groups} to this command, name must be unique.
     */
    protected ModCommand(String name, String description, DefaultMemberPermissions defaultPermissions, SubcommandGroupData... subcommandGroups) {
        super(name, description, defaultPermissions, subcommandGroups);
    }
    /**
     * Define a specially made {@link SlashCommandData Slash Command} for moderation purpose that will later be registered on the bot initialization.
     * 
     * <p> Incompatible with options.
     * 
     * <p>Predefined with the following arguments (MUST be defined or please check for null or override the {@link #getArgs(SlashCommandInteractionEvent) getArgs()} method):
     * <ul>
     * <li><b>user</b> - The user to sanction, required.</li>
     * <li><b>reason</b> - The reason of the sanction, default to "Raison par défaut".</li>
     * <li><b>time</b> - The time of the sanction, default to "5m".</li>
     * </ul>
     * 
     * <p>Predefined with the following checks (can be overridden in the {@link #check(SlashCommandInteractionEvent) check()} method):
     * <ul>
     * <li>Is the moderation enabled on the server.</li>
     * <li>Is the user not null or empty.</li>
     * <li>Is the user not the same as the member who executed the command.</li>
     * <li>Is the user not a bot or a system account.</li>
     * <li>Does the user have a role lower than the member who executed the command.</li>
     * <li>Is the user not the owner of the server.</li>
     * </ul>
     * 
     * <p><b>These values CANNOT be null or empty, otherwise an {@link IllegalArgumentException} will be thrown.</b>
     * @param name The command name, 1-32 lowercase alphanumeric characters.
     * @param description The command description, 1-100 characters.
     * @param defaultPermissions {@link DefaultMemberPermissions} representing the default permissions of this command.
     * @param subcommands Add up to 25 {@link SubcommandData Subcommmands} to this command, name must be unique.
     * @param subcommandGroups Add up to 25 {@link SubcommandGroupData Subcommmand-Groups} to this command, name must be unique.
     */
    protected ModCommand(String name, String description, DefaultMemberPermissions defaultPermissions, SubcommandData[] subcommands, SubcommandGroupData... subcommandGroups) {
        super(name, description, defaultPermissions, subcommands, subcommandGroups);
    }

    @Override
    protected void getArgs(SlashCommandInteractionEvent event) {
        user =   event.getOption("user", null, OptionMapping::getAsUser);
        member = event.getOption("user", null, OptionMapping::getAsMember);
        reason = event.getOption("reason", "Raison par défaut", OptionMapping::getAsString);
        time = TimeParser.parse(event.getOption("time", defaultTime, OptionMapping::getAsString));
    }
    
    @Override
    protected boolean check(SlashCommandInteractionEvent event) {
        ConfigEntry config = ConfigDatabase.getConfig(event.getGuild().getIdLong());
        if (!config.moderationEnabled) {
            event.reply("La modération est désactivée sur ce serveur.").setEphemeral(true).queue();
            return false;
        }
        
        if (user == null || member == null) {
            event.reply("Ce membre n'existe pas ou n'est pas présent sur le serveur. Vérifiez que l'identifiant est correct.").setEphemeral(true).queue();
            return false;
        }

        if (event.getMember().equals(member)) {
            event.reply("Ce membre est vous-même, vous ne pouvez pas vous auto-sanctionner").setEphemeral(true).queue();
            return false;
        }

        if (user.isBot() || user.isSystem()) {
            event.reply("Ce membre est un robot ou un compte système, vous ne pouvez pas le sanctionner.").setEphemeral(true).queue();
            return false;
        }
        
        if (event.getMember().getRoles().get(0).getPosition() < member.getRoles().get(0).getPosition()) {
            event.reply("Ce membre a un rôle supérieur au votre, vous ne pouvez pas le sanctionner.").setEphemeral(true).queue();
            return false;
        }

        if (member.isOwner()) {
            event.reply("Ce membre est le propriétaire du serveur, vous ne pouvez pas le sanctionner.").setEphemeral(true).queue();
            return false;
        }

        return super.check(event);
    }

    public final class SanctionType {
        public static final int WARN = 10;
        public static final int MUTE = 20;
        public static final int KICK = 30;
        public static final int BAN = 40;
    }
}
