package joseta.commands;

import joseta.commands.games.*;

import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.events.interaction.component.*;
import net.dv8tion.jda.api.hooks.*;

public class GameCommand extends ListenerAdapter {
    private GuessTheBlock gtbGame = null;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("play")) return;

        switch (event.getOption("game").getAsString()) {
            case "gtb" -> playGtb(event);
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals("gtb-play")) {
            event.reply("Joueeeeee").queue();
        }
        if (event.getComponentId().equals("gtb-cancel")) {
            if (!event.getUser().getId().equals(gtbGame.launcherId)) {
                event.reply("Tu n'a pas lancer le jeu, donc tu ne peux pas annuler !")
                    .setEphemeral(true).queue();
                return;
            }

            gtbGame = null;
            event.getMessage().delete().queue();
            event.getChannel().sendMessage("Le jeu a été annulé !").queue();
        }
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        String guildName = event.getGuild().getName();
        String guildIcon = event.getGuild().getIconUrl();
        if (event.getComponentId().startsWith("gtb-")) {
            if (event.getComponentId().equals("gtb-mode")) {
                gtbGame.setMode(event.getValues().get(0));
            }
            if (event.getComponentId().equals("gtb-difficulty")) {
                gtbGame.setDifficulty(event.getValues().get(0));
            }

            event.editMessageEmbeds(gtbGame.config(guildName, guildIcon)).queue();
        }
    }

    private void playGtb(SlashCommandInteractionEvent event) {
        if (gtbGame != null) {
            event.reply("Un jeu est déjà en cours !").setEphemeral(true).queue();
            return;
        }

        gtbGame = new GuessTheBlock(event.getUser().getId());
        gtbGame.play(event);
    }
}
