package joseta.commands;

import joseta.annotations.InteractionModule;
import joseta.annotations.types.Option;
import joseta.annotations.types.SlashCommandInteraction;
import joseta.database.Database;
import joseta.database.entities.Reminder;
import joseta.events.MiscEvents;
import joseta.events.ScheduledEvents;
import joseta.utils.TimeParser;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.time.Instant;

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
    
    @SlashCommandInteraction(name = "reminder list", description = "Liste vos rappels.")
    public void reminderList(SlashCommandInteractionEvent event) {
        event.reply("Cette fonctionnalité n'est pas encore implémentée.").setEphemeral(true).queue();
    }
    //#endregion
}
