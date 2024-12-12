package joseta.commands;

import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

public abstract class Command {
    public final String name;
    public final String description;
    public final DefaultMemberPermissions defaultPermissions;
    public final OptionData[] options; // Not compatible with subcommands.
    public final SubcommandData[] subcommands; // Not compatible with options.

    protected Command(String name, String description) {
        this(name, description, DefaultMemberPermissions.ENABLED);
    }
    protected Command(String name, String description, DefaultMemberPermissions defaultPermissions) {
        this(name, description, defaultPermissions, new OptionData[0]);
    }
    protected Command(String name, String description, OptionData... options) {
        this(name, description, DefaultMemberPermissions.ENABLED, options);
    }
    protected Command(String name, String description, SubcommandData... subcommands) {
        this(name, description, DefaultMemberPermissions.ENABLED, subcommands);
    }
    protected Command(String name, String description, DefaultMemberPermissions defaultPermissions, OptionData... options) {
        this.name = name;
        this.description = description;
        this.options = options;
        this.subcommands = new SubcommandData[0];
        this.defaultPermissions = defaultPermissions;
    }
    protected Command(String name, String description, DefaultMemberPermissions defaultPermissions, SubcommandData... subcommands) {
        this.name = name;
        this.description = description;
        this.options = new OptionData[0];
        this.subcommands = subcommands;
        this.defaultPermissions = defaultPermissions;
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
