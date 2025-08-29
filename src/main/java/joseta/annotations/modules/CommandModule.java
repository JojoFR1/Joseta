package joseta.annotations.modules;

import net.dv8tion.jda.api.events.interaction.command.*;

public abstract class CommandModule {
    protected SlashCommandInteractionEvent event;

    public CommandModule(SlashCommandInteractionEvent event) {
        this.event = event;
    }
}
