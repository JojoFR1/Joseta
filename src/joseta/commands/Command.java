package joseta.commands;

import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

public abstract class Command {
    protected SlashCommandData commandData;

    private Command() {}

    public Command(String name, String description) {
        this.commandData = Commands.slash(name,description);
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

    public SlashCommandData getCommandData() {
        return commandData;
    }

    public String getName() {
        return commandData.getName();
    }
}
