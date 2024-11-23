package joseta.commands;

import joseta.commands.games.*;

import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.hooks.*;

public class GameCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("play")) return;

        switch (event.getOption("game").getAsString()) {
            case "gtb" -> GuessTheBlock.play(event);
        }
    }
}
