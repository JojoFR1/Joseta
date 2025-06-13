package joseta.commands;

import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

public abstract class Command {
    public final String name;
    public final String description;
    public final DefaultMemberPermissions defaultPermissions;
    public final OptionData[] options;
    public final SubcommandData[] subcommands;
    public final SubcommandGroupData[] subcommandGroups;

    /**
     * Define a {@link SlashCommandData Slash Command} that will later be registered on the bot initialization.
     * 
     * <p> No options, subcommands or subcommand groups - default permission (enabled for everyone).
     * 
     * <p><b>These values CANNOT be null or empty, otherwise an {@link IllegalArgumentException} will be thrown.</b>
     * @param name The command name, 1-32 lowercase alphanumeric characters.
     * @param description The command description, 1-100 characters.
     */
    protected Command(String name, String description) {
        this(name, description, DefaultMemberPermissions.ENABLED);
    }
    /**
     * Define a {@link SlashCommandData Slash Command} that will later be registered on the bot initialization.
     * 
     * <p> No options, subcommands or subcommand groups.
     * 
     * <p><b>These values CANNOT be null or empty, otherwise an {@link IllegalArgumentException} will be thrown.</b>
     * @param name The command name, 1-32 lowercase alphanumeric characters.
     * @param description The command description, 1-100 characters.
     * @param defaultPermissions {@link DefaultMemberPermissions} representing the default permissions of this command.
     */
    protected Command(String name, String description, DefaultMemberPermissions defaultPermissions) {
        this(name, description, defaultPermissions, new OptionData[0]);
    }
    
    /**
     * Define a {@link SlashCommandData Slash Command} that will later be registered on the bot initialization.
     * 
     * <p> Incompatible with subcommands or subcommand groups - default permission (enabled for everyone).
     * 
     * <p><b>These values CANNOT be null or empty, otherwise an {@link IllegalArgumentException} will be thrown.</b>
     * @param name The command name, 1-32 lowercase alphanumeric characters.
     * @param description The command description, 1-100 characters.
     * @param options Add up to 25 {@link OptionData Options} to this command, name must be unique and required options must be registered before non-required.
     */
    protected Command(String name, String description, OptionData... options) {
        this(name, description, DefaultMemberPermissions.ENABLED, options);
    }
    /**
     * Define a {@link SlashCommandData Slash Command} that will later be registered on the bot initialization.
     * 
     * <p> Incompatible with options, no subcommand groups - default permission (enabled for everyone).
     * 
     * <p><b>These values CANNOT be null or empty, otherwise an {@link IllegalArgumentException} will be thrown.</b>
     * @param name The command name, 1-32 lowercase alphanumeric characters.
     * @param description The command description, 1-100 characters.
     * @param subcommands Add up to 25 {@link SubcommandData Subcommmands} to this command, name must be unique.
     */
    protected Command(String name, String description, SubcommandData... subcommands) {
        this(name, description, DefaultMemberPermissions.ENABLED, subcommands);
    }
    /**
     * Define a {@link SlashCommandData Slash Command} that will later be registered on the bot initialization.
     * 
     * <p> Incompatible with options, no subcommands - default permission (enabled for everyone).
     * 
     * <p><b>These values CANNOT be null or empty, otherwise an {@link IllegalArgumentException} will be thrown.</b>
     * @param name The command name, 1-32 lowercase alphanumeric characters.
     * @param description The command description, 1-100 characters.
     * @param subcommandGroups Add up to 25 {@link SubcommandGroupData Subcommmand-Groups} to this command, name must be unique.
     */
    protected Command(String name, String description, SubcommandGroupData... subcommandGroups) {
        this(name, description, DefaultMemberPermissions.ENABLED, subcommandGroups);
    }
    /**
     * Define a {@link SlashCommandData Slash Command} that will later be registered on the bot initialization.
     * 
     * <p> Incompatible with options - default permission (enabled for everyone).
     * 
     * <p><b>These values CANNOT be null or empty, otherwise an {@link IllegalArgumentException} will be thrown.</b>
     * @param name The command name, 1-32 lowercase alphanumeric characters.
     * @param description The command description, 1-100 characters.
     * @param subcommands Add up to 25 {@link SubcommandData Subcommmands} to this command, name must be unique.
     * @param subcommandGroups Add up to 25 {@link SubcommandGroupData Subcommmand-Groups} to this command, name must be unique.
     */
    protected Command(String name, String description, SubcommandData[] subcommands, SubcommandGroupData... subcommandGroups) {
        this(name, description, DefaultMemberPermissions.ENABLED, subcommands, subcommandGroups);
    }

    /**
     * Define a {@link SlashCommandData Slash Command} that will later be registered on the bot initialization.
     * 
     * <p> Incompatible with subcommands and subcommand groups.
     * 
     * <p><b>These values CANNOT be null or empty, otherwise an {@link IllegalArgumentException} will be thrown.</b>
     * @param name The command name, 1-32 lowercase alphanumeric characters.
     * @param description The command description, 1-100 characters.
     * @param defaultPermissions {@link DefaultMemberPermissions} representing the default permissions of this command.
     * @param options Add up to 25 {@link OptionData Options} to this command, name must be unique and required options must be registered before non-required.
     */
    protected Command(String name, String description, DefaultMemberPermissions defaultPermissions, OptionData... options) {
        this(name, description, defaultPermissions, options, new SubcommandData[0], new SubcommandGroupData[0]);
    }
    /**
     * Define a {@link SlashCommandData Slash Command} that will later be registered on the bot initialization.
     * 
     * <p> Incompatible with options, no subcommand groups.
     * 
     * <p><b>These values CANNOT be null or empty, otherwise an {@link IllegalArgumentException} will be thrown.</b>
     * @param name The command name, 1-32 lowercase alphanumeric characters.
     * @param description The command description, 1-100 characters.
     * @param defaultPermissions {@link DefaultMemberPermissions} representing the default permissions of this command.
     * @param subcommands Add up to 25 {@link SubcommandData Subcommmands} to this command, name must be unique.
     */
    protected Command(String name, String description, DefaultMemberPermissions defaultPermissions, SubcommandData... subcommands) {
        this(name, description, defaultPermissions, new OptionData[0], subcommands, new SubcommandGroupData[0]);
    }
    /**
     * Define a {@link SlashCommandData Slash Command} that will later be registered on the bot initialization.
     * 
     * <p> Incompatible with options, no subcommands.
     * 
     * <p><b>These values CANNOT be null or empty, otherwise an {@link IllegalArgumentException} will be thrown.</b>
     * @param name The command name, 1-32 lowercase alphanumeric characters.
     * @param description The command description, 1-100 characters.
     * @param defaultPermissions {@link DefaultMemberPermissions} representing the default permissions of this command.
     * @param subcommandGroups Add up to 25 {@link SubcommandGroupData Subcommmand-Groups} to this command, name must be unique.
     */
    protected Command(String name, String description, DefaultMemberPermissions defaultPermissions, SubcommandGroupData... subcommandGroups) {
        this(name, description, defaultPermissions, new OptionData[0], new SubcommandData[0], subcommandGroups);
    }
    /**
     * Define a {@link SlashCommandData Slash Command} that will later be registered on the bot initialization.
     * 
     * <p> Incompatible with options.
     * 
     * <p><b>These values CANNOT be null or empty, otherwise an {@link IllegalArgumentException} will be thrown.</b>
     * @param name The command name, 1-32 lowercase alphanumeric characters.
     * @param description The command description, 1-100 characters.
     * @param defaultPermissions {@link DefaultMemberPermissions} representing the default permissions of this command.
     * @param subcommands Add up to 25 {@link SubcommandData Subcommmands} to this command, name must be unique.
     * @param subcommandGroups Add up to 25 {@link SubcommandGroupData Subcommmand-Groups} to this command, name must be unique.
     */
    protected Command(String name, String description, DefaultMemberPermissions defaultPermissions, SubcommandData[] subcommands, SubcommandGroupData... subcommandGroups) {
        this(name, description, defaultPermissions, new OptionData[0], subcommands, subcommandGroups);
    }

    /**
     * Private constructor to initialize the command with all parameters.
     * 
     * <p> {@link OptionData Options} and {@link SubcommandData Subcommmands}/{@link SubcommandGroupData Subcommmand-Groups} will never be together due to the current implementation.
     * 
     * <p> Do NOT use this constructor directly, use the other constructors instead.
     */
    private Command(String name, String description, DefaultMemberPermissions defaultPermission, OptionData[] options, SubcommandData[] subcommands, SubcommandGroupData[] subcommandGroups) {
        this.name = name;
        this.description = description;
        this.defaultPermissions = defaultPermission;
        this.options = options;
        this.subcommands = subcommands;
        this.subcommandGroups = subcommandGroups;
    }

    public final void run(SlashCommandInteractionEvent event) {
        getArgs(event);
        if (!check(event)) return;

        runImpl(event);
    }

    protected abstract void runImpl(SlashCommandInteractionEvent event);

    protected void getArgs(SlashCommandInteractionEvent event) {}
    
    protected boolean check(SlashCommandInteractionEvent event) {
        return true;
    }
}
