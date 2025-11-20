package joseta.commands;

import joseta.annotations.*;
import joseta.annotations.types.*;
import joseta.annotations.types.SlashCommandInteraction;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.components.actionrow.*;
import net.dv8tion.jda.api.components.label.*;
import net.dv8tion.jda.api.components.selections.*;
import net.dv8tion.jda.api.components.textdisplay.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.events.interaction.component.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.modals.*;

@InteractionModule
public class AdminCommands {
    // TODO Logic implementation
    //#region Rules Commands
    @SlashCommandInteraction(name = "admin rules send", description = "Envoie les règles dans un salon.")
    public void rulesSend(SlashCommandInteractionEvent event,
                          @Option(description = "Le salon où envoyez les règles.", required = true) GuildMessageChannel channel)
    {
        event.reply("Rules sent to " + channel.getAsMention()).queue();
    }
    
    @SlashCommandInteraction(name = "admin rules update", description = "Met à jour les règles dans un salon.")
    public void rulesUpdate(SlashCommandInteractionEvent event,
                            @Option(description = "Le salon où le message des règles se trouvent", required = true) GuildMessageChannel channel,
                            @Option(description = "L'identifiant du message.", required = true) long messageId)
    {
        event.reply("Rules updated. " + messageId).queue();
    }
    //#endregion
    
    //#region Counting Commands
    @SlashCommandInteraction(name = "admin counting set_number")
    public void countingSetNumber(SlashCommandInteractionEvent event, int number) {
        event.reply("Counting number set to " + number).queue();
    }
    
    @SlashCommandInteraction(name = "admin counting reset_number")
    public void countingResetNumber(SlashCommandInteractionEvent event) {
        event.reply("Counting number reset to 0").queue();
    }
    
    @SlashCommandInteraction(name = "admin counting reset_author")
    public void countingResetAuthor(SlashCommandInteractionEvent event) {
        event.reply("Counting author reset").queue();
    }
    //#endregion
}
