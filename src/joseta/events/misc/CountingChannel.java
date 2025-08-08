package joseta.events.misc;

import arc.util.*;
import joseta.database.*;
import joseta.database.entry.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

public class CountingChannel {
    private static boolean autoCheck = true;
    private static int lastNumber = -1;
    private static long lastAuthorId = -1;

    // Start with a number
    private static final Pattern numberRegex = Pattern.compile("^\\d+");

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

        ConfigEntry config = Database.get(ConfigEntry.class, message.getGuild().getIdLong());
        int number = -1;

        // Rule - Cannot use non-numeric characters if comments are disabled & has to start with a number
        Matcher numberMatcher = numberRegex.matcher(message.getContentRaw());
        if ((!config.isCountingCommentsEnabled() && numberMatcher.matches()) || (config.isCountingCommentsEnabled() && numberMatcher.find()))
            try { number = Integer.parseInt(numberMatcher.group()); }
            catch (NumberFormatException e) { Log.err("Failed to parse the number from the counting message.", e); }

        if (number == -1) {
            if (!config.isCountingCommentsEnabled())
                message.reply(message.getAuthor().getAsMention() + " vous devez uniquement utiliser des chiffres dans ce salon !").queue(
                    m -> m.delete().queueAfter(5, TimeUnit.SECONDS)
                );
            else
                message.reply(message.getAuthor().getAsMention() + " vous devez commencer par des chiffres dans ce salon !").queue(
                    m -> m.delete().queueAfter(5, TimeUnit.SECONDS)
                );
            message.delete().queue();
            return;
        }

        // Rule - Must increment the last number by 1
        if (number != lastNumber + 1) {
            message.reply(message.getAuthor().getAsMention() + " vous devez augmenter le nombre précédent par 1.").queue(
                m -> m.delete().queueAfter(5, TimeUnit.SECONDS)
            );
            message.delete().queue();
            return;
        }

        // Rule - Cannot count twice in a row
        if (message.getAuthor().getIdLong() == lastAuthorId) {
            message.reply(message.getAuthor().getAsMention() + " vous ne pouvez pas compter deux fois de suite !").queue(
                    m -> m.delete().queueAfter(5, TimeUnit.SECONDS)
            );
            message.delete().queue();
            return;
        }

        lastNumber += 1;
        lastAuthorId = message.getAuthor().getIdLong();
    }
}
