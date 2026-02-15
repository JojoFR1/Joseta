package dev.jojofr.joseta.events.misc;

import dev.jojofr.joseta.database.entities.Configuration;
import dev.jojofr.joseta.utils.BotCache;
import dev.jojofr.joseta.utils.Log;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CountingChannel {
    private static boolean autoCheck = true;
    
    public static long lastNumber = -1;
    public static long lastAuthorId = -1;
    private static long lastTimestamp = -1;
    
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
            
            Configuration config = BotCache.guildConfigurations.get(message.getGuild().getIdLong());
            lastAuthorId = previousMessage.getAuthor().getIdLong();
            lastNumber = parseNumber(previousMessage.getContentRaw().replace(" ", ""), config.countingCommentsEnabled);
            lastTimestamp = previousMessage.getTimeCreated().toInstant().toEpochMilli();
            
            if (lastAuthorId == -1) {
                channel.sendMessage("Le comptage n'a pas pu être initialiser. Contacter un administrateur et continuer (vérification manuelle).").queue();
                autoCheck = false;
                return false;
            }
        }
        
        return true;
    }
    
    public static void check(MessageChannelUnion channel, Message message) {
        if (!autoCheck) return;
        
        if (!preCheck(channel, message)) return;
        
        Configuration config = BotCache.guildConfigurations.get(message.getGuild().getIdLong());
        long number = parseNumber(message.getContentRaw().replace(" ", ""), config.countingCommentsEnabled);
        
        if (number == lastNumber && message.getTimeCreated().toInstant().toEpochMilli() - lastTimestamp < 2000) {
            message.delete().queue();
            return;
        }
        
        // Rule - Cannot count twice in a row
        if (message.getAuthor().getIdLong() == lastAuthorId) {
            if (!config.countingPenaltyEnabled) {
                message.reply(message.getAuthor().getAsMention() + " vous ne pouvez pas compter deux fois de suite !").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
                message.delete().queue();
            } else {
                lastNumber = 0;
                message.addReaction(BotCache.CROSS_EMOJI).queue();
                message.reply(message.getAuthor().getAsMention() + " a cassé la chaîne ! Il fallait attendre que quelqu'un d'autre compte.\n\n-# Le comptage repart de 0.").queue();
            }
            return;
        }
        
        lastAuthorId = message.getAuthor().getIdLong();
        
        // Rule - Cannot use non-numeric characters if comments are disabled & has to start with a number
        if (number == -1) {
            String hasToString = config.countingCommentsEnabled ? "commencer par" : "uniquement utiliser";
            if (!config.countingPenaltyEnabled) {
                message.reply(message.getAuthor().getAsMention() + " vous devez "+ hasToString +" des chiffres dans ce salon !").queue(
                    botMessage -> botMessage.delete().queueAfter(5, TimeUnit.SECONDS)
                );
                message.delete().queue();
            } else {
                lastNumber = 0;
                message.addReaction(BotCache.CROSS_EMOJI).queue();
                message.reply(message.getAuthor().getAsMention() + " a cassé la chaîne ! Il fallait "+ hasToString +" des chiffres.\n\n-# Le comptage repart de 0.").queue();
            }
            return;
        }
        
        // Rule - Must increment the last number by 1
        if (number != lastNumber + 1) {
            if (!config.countingPenaltyEnabled) {
                message.reply(message.getAuthor().getAsMention() + " vous devez augmenter le nombre précédent par 1.").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
                message.delete().queue();
            } else {
                lastNumber = 0;
                message.addReaction(BotCache.CROSS_EMOJI).queue();
                message.reply(message.getAuthor().getAsMention() + " a cassé la chaîne ! Il fallait augmenter le nombre précédent par 1.\n\n-# Le comptage repart de 0.").queue();
            }
            return;
        }
        
        lastNumber += 1;
        lastTimestamp = message.getTimeCreated().toInstant().toEpochMilli();
        message.addReaction(BotCache.CHECK_EMOJI).queue(
            v -> message.clearReactions().queueAfter(5, TimeUnit.SECONDS)
        );
    }
    
    
    // Start with a number
    private static final Pattern numberRegex = Pattern.compile("^-?\\d+");
    
    private static long parseNumber(String message, boolean commentsEnabled) {
        long number = -1;
        Matcher numberMatcher = numberRegex.matcher(message);
        if ((!commentsEnabled && numberMatcher.matches()) || (commentsEnabled && numberMatcher.find()))
            try { number = Long.parseLong(numberMatcher.group()); }
            catch (NumberFormatException e) { Log.err("Failed to parse the number from the counting message.", e); }
        
        return number;
    }
}
