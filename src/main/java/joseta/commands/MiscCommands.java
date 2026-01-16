package joseta.commands;

import joseta.annotations.*;
import joseta.annotations.types.*;
import joseta.events.MiscEvents;
import net.dv8tion.jda.api.events.interaction.command.*;

@InteractionModule
public class MiscCommands {
    // TODO Logic implementation
    @SlashCommandInteraction(name = "ping", description = "Obtenez le ping du bot.")
    public void ping(SlashCommandInteractionEvent event) {
        long startTime = System.currentTimeMillis();
        long gatewayPing = event.getJDA().getGatewayPing();
        
        event.reply("Pinging...").queue(reply ->
            reply.editOriginal("Pong! Latence: "+ (System.currentTimeMillis() - startTime) +"ms | Latence API : "+ gatewayPing +"ms").queue()
        );
    }
    
    @SlashCommandInteraction(name = "markov", description = "Génère un message aléatoire à partir des messages du serveur.")
    public void markov(SlashCommandInteractionEvent event) {
    
    }
    
    
    @SlashCommandInteraction(name = "multi", description = "Envoie le texte d'aide pour le multijoueur.")
    public void multiInfo(SlashCommandInteractionEvent event) { event.reply(MiscEvents.autoResponseMessage).queue(); }
    
    //#region Reminder Commands
    @SlashCommandInteraction(name = "reminder add", description = "Ajouter un rappel pour plus tard.")
    public void reminderAdd(SlashCommandInteractionEvent event,
                            @Option(description = "Le message du rappel.", required = true) String message,
                            @Option(description = "Le temps avant que vous recevez le rappel (M, w, d, h, m, s).", required = true) String time)
    {
        
    }
    
    @SlashCommandInteraction(name = "reminder list", description = "Liste vos rappels.")
    public void reminderList(SlashCommandInteractionEvent event) {
    
    }
    //#endregion
}
