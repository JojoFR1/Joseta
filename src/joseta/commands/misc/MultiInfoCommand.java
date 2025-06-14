package joseta.commands.misc;

import joseta.commands.*;
import joseta.events.misc.*;

import net.dv8tion.jda.api.events.interaction.command.*;

public class MultiInfoCommand extends Command {

    public MultiInfoCommand() {
        super("multi", "Envoie le texte d'aide pour le multijoueur.");
    }

    @Override
    protected void runImpl(SlashCommandInteractionEvent event) {
        event.reply(AutoResponseEvent.message).queue();
    }
}
