package dev.jojofr.joseta.commands;

import dev.jojofr.joseta.annotations.InteractionModule;
import dev.jojofr.joseta.annotations.types.Option;
import dev.jojofr.joseta.annotations.types.interaction.SlashCommandInteraction;
import dev.jojofr.joseta.database.entities.ConfigurationEntity;
import dev.jojofr.joseta.events.MessageEvents;
import dev.jojofr.joseta.utils.BotCache;
import dev.jojofr.joseta.utils.markov.MarkovGen;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

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
        ConfigurationEntity config = BotCache.getConfiguration(event.getGuild().getIdLong());
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
    
    private static final String[] answers = {
        "Oui.", "Non.", "Peut-être.", "Je ne sais pas.", "Certainement.", "Absolument pas.", "C'est possible.",
        "Je ne peux pas répondre à ça.", "Demande à nouveau plus tard.", "Je ne suis pas sûr.",
        "C'est une bonne question.", "Je ne peux pas te dire ça.",
        "Je ne suis pas autorisé à répondre à cette question.", "Je ne suis pas sûr de comprendre la question."
    };
    @SlashCommandInteraction(name = "demande", description = "Posez une question au bot.")
    public void demande(SlashCommandInteractionEvent event, @Option(name = "question", required = true) String question) {
        String answer = answers[ThreadLocalRandom.current().nextInt(answers.length)];
        event.reply("Question: " + question + "\nRéponse: " + answer).setAllowedMentions(Set.of()).queue();
    }
    
    @SlashCommandInteraction(name = "multi", description = "Envoie le texte d'aide pour le multijoueur.")
    public void multiInfo(SlashCommandInteractionEvent event) { event.reply(MessageEvents.autoResponseMessage).queue(); }
    
    @SlashCommandInteraction(name = "surprendmoi", description = "Obtenez une surprise aléatoirement aléatoire !")
    public void surprises(SlashCommandInteractionEvent event) {
        event.reply("Les addictions aux jeux d'argents et d'hasard sont dangereuses, si vous avez besoin d'aide, n'hésitez pas à contacter des associations spécialisées comme <https://www.joueurs-info-service.fr/> ou <https://sos-jeu.ch/>.").queue();
    }
    
    @SlashCommandInteraction(name = "gacha", description = "Tentez votre chance pour obtenir un rôle spécial !")
    public void gacha(SlashCommandInteractionEvent event) {
        event.reply("Les addictions aux jeux d'argents et d'hasard sont dangereuses, si vous avez besoin d'aide, n'hésitez pas à contacter des associations spécialisées comme <https://www.joueurs-info-service.fr/> ou <https://sos-jeu.ch/>.").queue();
    }
}
