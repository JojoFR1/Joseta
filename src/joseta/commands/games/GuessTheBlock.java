package joseta.commands.games;

import joseta.*;
import joseta.commands.*;
import joseta.utils.math.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.events.interaction.component.*;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.*;

import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.List;

public class GuessTheBlock {
    public final String launcherUserId;
    public String difficulty = "gtb-dmedium";
    public String mode = "gtb-mall";

    public GuessTheBlock(String launcherUserId) {
        this.launcherUserId = launcherUserId;
    } 

    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals("gtb-play")) {
            event.editMessage("Joueeeeee").queue();
        }
        if (event.getComponentId().equals("gtb-cancel")) {
            if (!event.getUser().getId().equals(this.launcherUserId)) {
                event.reply("Tu n'a pas lancer le jeu, donc tu ne peux pas annuler !")
                    .setEphemeral(true).queue();
                return;
            }

            // This doesn't work here for some reason.
            // event.getMessage().delete().queue();
            event.getChannel().sendMessage("The game was cancelled!").queue();
            GameCommand.cancelGtb();
        }
    }

    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        String guildName = event.getGuild().getName();
        String guildIcon = event.getGuild().getIconUrl();

        if (event.getComponentId().equals("gtb-mode"))
            this.setMode(event.getValues().get(0));

        if (event.getComponentId().equals("gtb-difficulty"))
            this.setDifficulty(event.getValues().get(0));

        event.editMessageEmbeds(this.config(guildName, guildIcon)).queue();
    }


    public void start(SlashCommandInteractionEvent event) {
        String guildName = event.getGuild().getName();
        String guildIcon = event.getGuild().getIconUrl();

        event.replyEmbeds(config(guildName, guildIcon))
            .addActionRow(StringSelectMenu.create("gtb-mode")
                .addOptions(
                    SelectOption.of("Serpulo", "gtb-mserpulo"),
                    SelectOption.of("Erekir", "gtb-merekir"),
                    SelectOption.of("Toutes les Planètes", "gtb-mall")
                        .withDefault(false)
                ).build())
            .addActionRow(StringSelectMenu.create("gtb-difficulty")
                .addOptions(
                    SelectOption.of("Facile", "gtb-deasy"),
                    SelectOption.of("Normal", "gtb-dmedium")
                        .withDefault(false),
                    SelectOption.of("Difficile", "gtb-dhard")
                ).build()
            ).addActionRow(
                Button.success("gtb-play", "Jouer"),
                Button.danger("gtb-cancel", "Annuler")
            )
        .queue();
    }

    public MessageEmbed config(String guildName, String guildIcon) {
        String difficultyText = difficulty.equals("gtb-deasy") ? "Facile"
                                : difficulty.equals("gtb-dmedium") ? "Normal"
                                : "Difficile";
        String modeText = mode.equals("gtb-mserpulo") ? "Serpulo"
                            : mode.equals("gtb-merekir") ? "Erekir"
                            : "Toutes les Planètes";

        EmbedBuilder difficultyEmbed = new EmbedBuilder()
            .setTitle("❔┃Guess the Block")
            .setColor(Color.CYAN)
            .setFooter("Guess the Block - " + guildName, guildIcon)
            .setTimestamp(Instant.now());
        
        difficultyEmbed.setDescription(
            "## Paramètres du jeu\n"
          + "- **Mode:** " + modeText + "\n"
          + "- **Difficulté:** " + difficultyText
        );

        return difficultyEmbed.build();
    }


    public MessageEmbed play(String guildName, String guildIcon) {
        String planet = mode.equals("gtb-mserpulo") ? "serpulo" 
                        : mode.equals("gtb-merekir") ? "erekir"
                        : "all";
        try {
            List<String> blocks = Files.readAllLines(Path.of("resources/gtb", planet + ".txt"));
            
            Rand rand = new Rand(System.currentTimeMillis());
            String block = blocks.get(rand.nextInt(blocks.size()));
            Vars.logger.info(block);
            
            EmbedBuilder playEmbed =  new EmbedBuilder()
                .setTitle("❔┃Guess the Block")
                .setColor(Color.GREEN)
                .setFooter("Guess the Block - " + guildName, guildIcon)
                .setTimestamp(Instant.now());
    
            playEmbed.setDescription("");

            return playEmbed.build();

        } catch (IOException e) {
            Vars.logger.error("Could not read the block file.", e);
            return null;
        }
    }

    public void setDifficulty(String newDifficulty) {
        difficulty = newDifficulty;
    }

    public void setMode(String newMode) {
        mode = newMode;
    }
}
