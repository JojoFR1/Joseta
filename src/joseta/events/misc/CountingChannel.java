package joseta.events.misc;

import arc.util.*;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.*;

import java.util.*;
import java.util.concurrent.*;

public class CountingChannel {
    private static boolean autoCheck = true;
    private static int lastNumber = -1;
    private static long lastAuthorId = -1;

    public static boolean preCheck(MessageChannelUnion channel, Message message) {
        if (lastNumber == -1) { // Initialize the needed values on bot launch
            Message previousMessage = null;
            try {
                // Get the second last message that is not from a bot (from warning message) and isn't the user's own message
                List<Message> messages = channel.getIterableHistory().takeUntilAsync(10, m -> m.getAuthor().isBot() && m.getIdLong() != message.getIdLong()).get();
                //                      Size 1 is equivalent to empty (it's the first message sent)
                if (messages != null && messages.size() > 1) previousMessage = messages.get(1);
                else {
                    lastNumber = 0;
                    return true;
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.err("The counting channel could not be initialized.", e);
                // Error should be handled below
            }

            if (previousMessage == null) {
                channel.sendMessage("Le comptage n'a pas pu être initialiser. Contacter un administrateur et continuer (vérification manuelle).").queue();
                autoCheck = false;
                return false;
            }

            lastAuthorId = previousMessage.getAuthor().getIdLong();
            lastNumber = Integer.parseInt(previousMessage.getContentStripped().replace(" ", ""));
        };

        return true;
    }

    public static void check(MessageChannelUnion channel, Message message) {
        if (!autoCheck) return;

        if (!preCheck(channel, message)) return;

        // Rule - Cannot count twice in a row
        if (message.getAuthor().getIdLong() == lastAuthorId) {
            message.reply(message.getAuthor().getAsMention() + " vous ne pouvez pas compter deux fois de suite !").queue(
                    m -> m.delete().queueAfter(5, TimeUnit.SECONDS)
            );
            message.delete().queue();
            return;
        }

        // Rule - Cannot use non-numeric characters
        if (!message.getContentStripped().matches("\\d+")) {
            message.reply(message.getAuthor().getAsMention() + " vous devez uniquement utiliser des chiffres dans ce salon !").queue(
                    m -> m.delete().queueAfter(5, TimeUnit.SECONDS)
            );
            message.delete().queue();
            return;
        }

        // Rule - Must increment the last number by 1
        if (Integer.parseInt(message.getContentStripped().replace(" ", "")) != lastNumber + 1) {
            message.reply(message.getAuthor().getAsMention() + " vous devez incrémenter le nombre précédent de 1, aka. compter.").queue(
                    m -> m.delete().queueAfter(5, TimeUnit.SECONDS)
            );
            message.delete().queue();
            return;
        }

        lastNumber += 1;
        lastAuthorId = message.getAuthor().getIdLong();
        Log.info("GOOD");
    }
}
