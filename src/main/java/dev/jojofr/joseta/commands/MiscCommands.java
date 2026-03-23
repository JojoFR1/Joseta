package dev.jojofr.joseta.commands;

import dev.jojofr.joseta.annotations.InteractionModule;
import dev.jojofr.joseta.annotations.types.SlashCommandInteraction;
import dev.jojofr.joseta.database.entities.ConfigurationEntity;
import dev.jojofr.joseta.events.MiscEvents;
import dev.jojofr.joseta.utils.BotCache;
import dev.jojofr.joseta.utils.markov.MarkovGen;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@InteractionModule
public class MiscCommands {
    
    @SlashCommandInteraction(name = "ping", description = "Obtenez le ping du bot.")
    public void ping(SlashCommandInteractionEvent event) {
        long startTime = System.currentTimeMillis();
        long gatewayPing = event.getJDA().getGatewayPing();
        
        event.reply("Pinging...").queue(reply ->
            reply.editOriginal("Pong! Latence: "+ (System.currentTimeMillis() - startTime) +"ms | Latence API : "+ gatewayPing +"ms").queue()
        );
    }
    
    /**
     * Original idea by l4p1n in <a href=https://git.l4p1n.ch/l4p1n-bot/bot-rust/src/commit/8afea76f37fa1468e829c37366534e6b345bdc94/bot/AppCommands/MarkovCommand.cs>l4p1n-bot/MarkovCommand.cs</a>
     */
    @SlashCommandInteraction(name = "markov", description = "Génère un message aléatoire à partir des messages du serveur.")
    public void markov(SlashCommandInteractionEvent event) {
        ConfigurationEntity config = BotCache.getGuildConfiguration(event.getGuild().getIdLong());
        if (!config.markovEnabled) {
            event.reply("La génération de messages Markov est désactivée sur ce serveur.").setEphemeral(true).queue();
            return;
        }
        
        event.deferReply().queue(
            hook -> {
                String generatedMessage = MarkovGen.generateMessage(event.getGuild().getIdLong());
                
                hook.editOriginal(generatedMessage).queue();
            }
        );
    }
    
    
    @SlashCommandInteraction(name = "multi", description = "Envoie le texte d'aide pour le multijoueur.")
    public void multiInfo(SlashCommandInteractionEvent event) { event.reply(MiscEvents.autoResponseMessage).queue(); }
}
