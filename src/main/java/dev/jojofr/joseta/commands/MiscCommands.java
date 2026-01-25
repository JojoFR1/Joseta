package dev.jojofr.joseta.commands;

import dev.jojofr.joseta.annotations.InteractionModule;
import dev.jojofr.joseta.annotations.types.Option;
import dev.jojofr.joseta.annotations.types.SlashCommandInteraction;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.entities.Configuration;
import dev.jojofr.joseta.database.entities.Reminder;
import dev.jojofr.joseta.events.MiscEvents;
import dev.jojofr.joseta.events.ScheduledEvents;
import dev.jojofr.joseta.utils.TimeParser;
import dev.jojofr.joseta.utils.markov.MarkovGen;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.time.Instant;

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
        Configuration config = Database.get(Configuration.class, event.getGuild().getIdLong());
        if (config == null || !config.markovEnabled) {
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
    
    //#region Reminder Commands
    @SlashCommandInteraction(name = "reminder add", description = "Ajouter un rappel pour plus tard.")
    public void reminderAdd(SlashCommandInteractionEvent event,
                            @Option(description = "Le message du rappel.", required = true) String message,
                            @Option(description = "Le temps avant que vous recevez le rappel (M, w, d, h, m, s).", required = true) String time)
    {
        long timeSeconds = TimeParser.parse(time);
        
        long userId = event.getUser().getIdLong();
        if (message.length() > ScheduledEvents.REMINDER_MAX_MESSAGE_LENGTH - String.valueOf(userId).length()) {
            event.reply("Le message de rappel est trop long. La longueur maximale est de " + (Message.MAX_CONTENT_LENGTH - ScheduledEvents.REMINDER_PREMESSAGE.length()) + " caractères.").setEphemeral(true).queue();
            return;
        }
        
        Instant remindAt = Instant.now().plusSeconds(timeSeconds);
        Database.create(new Reminder(event.getGuild().getIdLong(), event.getChannelIdLong(), userId, message, remindAt));
        event.reply("Votre rappel a été ajouté pour le <t:" + remindAt.getEpochSecond() + ":F> (<t:" + remindAt.getEpochSecond() + ":R>).").setEphemeral(true).queue();
    }
    
    // TODO Logic implementation
    @SlashCommandInteraction(name = "reminder list", description = "Liste vos rappels.")
    public void reminderList(SlashCommandInteractionEvent event) {
        event.reply("Cette fonctionnalité n'est pas encore implémentée.").setEphemeral(true).queue();
    }
    //#endregion
}
