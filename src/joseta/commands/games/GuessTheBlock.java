package joseta.commands.games;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.*;

import java.awt.*;
import java.time.*;

public class GuessTheBlock {
    public final String launcherId;
    public String difficulty = "gtb-dmedium";
    public String mode = "gtb-mall";

    public GuessTheBlock(String launcherId) {
        this.launcherId = launcherId;
    } 

    public void play(SlashCommandInteractionEvent event) {
        String guildName = event.getGuild().getName();
        String guildIcon = event.getGuild().getIconUrl();

        event.replyEmbeds(config(guildName, guildIcon))
            .addActionRow(StringSelectMenu.create("gtb-mode")
                .addOptions(
                    SelectOption.of("Serpulo", "gtb-mserpulo"),
                    SelectOption.of("Erekir", "gtb-merekir"),
                    SelectOption.of("Toutes les Planètes", "gtb-mall")
                        .withDefault(true)
                ).build())
            .addActionRow(StringSelectMenu.create("gtb-difficulty")
                .addOptions(
                    SelectOption.of("Facile", "gtb-deasy"),
                    SelectOption.of("Normal", "gtb-dmedium")
                        .withDefault(true),
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
            .setTitle("Paramètres du jeu")
            .setColor(Color.CYAN)
            .setFooter("Guess the Block - " + guildName, guildIcon)
            .setTimestamp(Instant.now());
        
        difficultyEmbed.setDescription("Mode: " + modeText + "\nDifficulté: " + difficultyText);
        
        return difficultyEmbed.build();
    }

    public void setDifficulty(String newDifficulty) {
        difficulty = newDifficulty;
    }

    public void setMode(String newMode) {
        mode = newMode;
    }
}
