package joseta.commands;

import joseta.utils.struct.*;

import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

public abstract class Command {
    public final String name;
    public final String description;
    public final Seq<OptionData> options;
    public final DefaultMemberPermissions defaultPermissions;
    protected boolean shouldRun = true;

    protected Command(String name, String description) {
        this(name, description, Seq.with(), DefaultMemberPermissions.ENABLED);
    }
    protected Command(String name, String description, Seq<OptionData> options) {
        this(name, description, options, DefaultMemberPermissions.ENABLED);
    }
    protected Command(String name, String description, DefaultMemberPermissions defaultPermissions) {
        this(name, description, Seq.with(), defaultPermissions);
    }
    protected Command(String name, String description, Seq<OptionData> options, DefaultMemberPermissions defaultPermissions) {
        this.name = name;
        this.description = description;
        this.options = options;
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
