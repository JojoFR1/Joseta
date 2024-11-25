package joseta.commands;

import joseta.commands.games.*;

import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.events.interaction.component.*;
import net.dv8tion.jda.api.hooks.*;

public class GameCommand extends ListenerAdapter {
    private static GuessTheBlock gtbGame = null;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("play")) return;

        switch (event.getOption("game").getAsString()) {
            case "gtb" -> playGtb(event);
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (gtbGame != null && event.getComponentId().startsWith("gtb")) {
            gtbGame.onButtonInteraction(event);

            // Doesn't work in the gtbGame function
            if (event.getComponentId().equals("gtb-cancel")) {
                event.getMessage().delete().queue();
            }
        }
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (gtbGame != null && event.getComponentId().startsWith("gtb"))
            gtbGame.onStringSelectInteraction(event);
    }

    private void playGtb(SlashCommandInteractionEvent event) {
        if (gtbGame != null) {
            event.reply("Un jeu est déjà en cours !").setEphemeral(true).queue();
            return;
        }

        gtbGame = new GuessTheBlock(event.getUser().getId());
        gtbGame.start(event);
    }

    public static void cancelGtb() {
        gtbGame = null;
    }
}
