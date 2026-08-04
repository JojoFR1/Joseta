package dev.jojofr.joseta.events.misc;

import dev.jojofr.joseta.database.entities.ConfigurationEntity;
import dev.jojofr.joseta.utils.BotCache;
import dev.jojofr.joseta.utils.Log;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CountingChannel {
    private static boolean autoCheck = true;
    
    public static long lastNumber = -1;
    public static long lastAuthorId = -1;
    private static long lastTimestamp = -1;
    
    public static long specialLastNumber = -1;
    public static long specialLastAuthorId = -1;
    private static long specialLastTimestamp = -1;
    private static long lastSpecialModeChangeTimestamp = -1;
    public static CountingMode specialCountingMode = CountingMode.DECIMAL;
    
    public enum CountingMode { BINARY, OCTAL, DECIMAL, HEXADECIMAL, ROMAN, DOUBLE, POWER_OF_TWO }
    
    
    public static boolean preCheck(MessageChannelUnion channel, Message message, boolean special) {
        long curentNumber = special ? specialLastNumber : lastNumber;
        
        if (curentNumber == -1) { // Initialize the needed values on bot launch
            Message previousMessage = null;
            try {
                // Get the second last message that is not from a bot (from warning message) and isn't the user's own message
                List<Message> messages = channel.getIterableHistory().takeUntilAsync(10, m -> m.getAuthor().isBot() && m.getIdLong() != message.getIdLong()).get();
                //                      Size 1 is equivalent to empty (it's the first message sent)
                if (messages != null && messages.size() > 1) previousMessage = messages.get(1);
                else {
                    if (special) specialLastNumber = 0; else lastNumber = 0;
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
            
            ConfigurationEntity config = BotCache.getConfiguration(message.getGuild().getIdLong());
            long previousAuthordId = previousMessage.getAuthor().getIdLong();
            long previousNumber = parseNumber(previousMessage.getContentRaw().replace(" ", ""), config.countingCommentsEnabled);
            if (special)
                channel.sendMessage("Le comptage spécial ne peut pas être initialisé après un redémarrage du bot. Contacter un administrateur pour définir les valeurs de départ.").queue();
            long previousTimestamp = previousMessage.getTimeCreated().toInstant().toEpochMilli();
            
            if (special) {
                specialLastNumber = previousNumber;
                specialLastAuthorId = previousAuthordId;
                specialLastTimestamp = previousTimestamp;
            } else {
                lastNumber = previousNumber;
                lastAuthorId = previousAuthordId;
                lastTimestamp = previousTimestamp;
            }
            
            if (previousAuthordId == -1) {
                channel.sendMessage("Le comptage n'a pas pu être initialiser. Contacter un administrateur et continuer (vérification manuelle).").queue();
                autoCheck = false;
                return false;
            }
        }
        
        return true;
    }
    
    public static void check(MessageChannelUnion channel, Message message) {
        if (!autoCheck) return;
        
        if (!preCheck(channel, message, false)) return;
        
        ConfigurationEntity config = BotCache.getConfiguration(message.getGuild().getIdLong());
        long number = parseNumber(message.getContentStripped().replace(" ", ""), config.countingCommentsEnabled);
        
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
    
    // In a thread, where the bot switch "type" every X hours, can be: binary, octal, decimal, hexadecimal, roman, double, power of two
    public static void specialCheck(MessageChannelUnion channel, Message message) {
        if (!autoCheck) return;
        
        if (!preCheck(channel, message, true)) return;
        
        ConfigurationEntity config = BotCache.getConfiguration(message.getGuild().getIdLong());
        long number = parseSpecial(message.getContentStripped().replace(" ", ""), config.countingCommentsEnabled);
        
        if (number == specialLastNumber && message.getTimeCreated().toInstant().toEpochMilli() - specialLastTimestamp < 2000) {
            message.delete().queue();
            return;
        }
        
        // Rule - Cannot count twice in a row
        if (message.getAuthor().getIdLong() == specialLastAuthorId) {
            if (!config.countingPenaltyEnabled) {
                message.reply(message.getAuthor().getAsMention() + " vous ne pouvez pas compter deux fois de suite !").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
                message.delete().queue();
            } else {
                specialLastNumber = 0;
                message.addReaction(BotCache.CROSS_EMOJI).queue();
                message.reply(message.getAuthor().getAsMention() + " a cassé la chaîne ! Il fallait attendre que quelqu'un d'autre compte.\n\n-# Le comptage repart de 0.").queue();
                message.getMember().timeoutFor(lastNumber / 4, TimeUnit.MINUTES).reason("JstaDNR Comptage spécial cassé (Insignifiant) JstaDNR").queue();
            }
            return;
        }
        
        specialLastAuthorId = message.getAuthor().getIdLong();
        
        // Rule - Cannot use non-numeric characters if comments are disabled & has to start with a number
        if (number == -1) {
            String hasToString = config.countingCommentsEnabled ? "commencer par" : "uniquement utiliser";
            String type = switch (specialCountingMode) {
                case BINARY -> "binaire";
                case OCTAL -> "octal";
                case DECIMAL -> "décimal";
                case HEXADECIMAL -> "hexadécimal";
                case ROMAN -> "romain";
                case DOUBLE -> "double";
                case POWER_OF_TWO -> "puissance de 2";
            };
            if (!config.countingPenaltyEnabled) {
                message.reply(message.getAuthor().getAsMention() + " vous devez "+ hasToString +" des chiffres dans ce salon "+ type + "!").queue(
                    botMessage -> botMessage.delete().queueAfter(5, TimeUnit.SECONDS)
                );
                message.delete().queue();
            } else {
                lastNumber = 0;
                message.addReaction(BotCache.CROSS_EMOJI).queue();
                message.reply(message.getAuthor().getAsMention() + " a cassé la chaîne ! Il fallait "+ hasToString +" des chiffres "+ type +".\n\n-# Le comptage repart de 0.").queue();
                message.getMember().timeoutFor(lastNumber / 4, TimeUnit.MINUTES).reason("JstaDNR Comptage spécial cassé (Insignifiant) JstaDNR").queue();
            }
            return;
        }
        
        // Rule - Must increment the last number by 1
        long supposedNumber = specialLastNumber + 1;
        if (specialCountingMode == CountingMode.DOUBLE) supposedNumber = specialLastNumber * 2;
        else if (specialCountingMode == CountingMode.POWER_OF_TWO) supposedNumber = (long) Math.pow(2, specialLastNumber);
        
        if (number != supposedNumber) {
            String by = switch (specialCountingMode) {
                case BINARY, OCTAL, DECIMAL, HEXADECIMAL, ROMAN -> "1";
                case DOUBLE -> "le double du nombre précédent";
                case POWER_OF_TWO -> "la puissance de 2 suivante";
            };
            if (!config.countingPenaltyEnabled) {
                message.reply(message.getAuthor().getAsMention() + " vous devez augmenter le nombre précédent par "+ by +".").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
                message.delete().queue();
            } else {
                specialLastNumber = 0;
                message.addReaction(BotCache.CROSS_EMOJI).queue();
                message.reply(message.getAuthor().getAsMention() + " a cassé la chaîne ! Il fallait augmenter le nombre précédent par "+ by +".\n\n-# Le comptage repart de 0.").queue();
                message.getMember().timeoutFor(supposedNumber / 4, TimeUnit.MINUTES).reason("JstaDNR Comptage spécial cassé (Insignifiant) JstaDNR").queue();
            }
            return;
        }
        
        specialLastNumber = number;
        specialLastTimestamp = message.getTimeCreated().toInstant().toEpochMilli();
        message.addReaction(BotCache.CHECK_EMOJI).queue(
            v -> message.clearReactions().queueAfter(5, TimeUnit.SECONDS)
        );
        
        if (lastSpecialModeChangeTimestamp == -1 || System.currentTimeMillis() - lastSpecialModeChangeTimestamp > TimeUnit.HOURS.toMillis(6)) {
            CountingMode oldMode = specialCountingMode;
            changeSpecialMode();
            message.reply("Le mode de comptage spécial a changé ! Le nouveau mode est **"+ specialCountingMode.name().toLowerCase() +"** (anciennement **"+ oldMode.name().toLowerCase() +"**).").queue();
        }
    }
    
    
    // Start with a number
    private static final Pattern NUMBER_REGEX = Pattern.compile("^-?\\d+");
    private static final Pattern ZERO_WIDTH_SPACE_REGEX = Pattern.compile("[\\u200B\\u200C\\u200D\\uFEFF]");
    
    private static long parseNumber(String message, boolean commentsEnabled) {
        if (message.indexOf('\u200B') != -1 || message.indexOf('\u200C') != -1 || message.indexOf('\u200D') != -1 || message.indexOf('\uFEFF') != -1)
            message = ZERO_WIDTH_SPACE_REGEX.matcher(message).replaceAll("");
        
        long number = -1;
        Matcher numberMatcher = NUMBER_REGEX.matcher(message);
        if ((!commentsEnabled && numberMatcher.matches()) || (commentsEnabled && numberMatcher.find()))
            try { number = Long.parseLong(numberMatcher.group()); }
            catch (NumberFormatException e) { Log.err("Failed to parse the number from the counting message.", e); }
        
        return number;
    }
    
    private static final Pattern BINARY_REGEX = Pattern.compile("^[01]+");
    private static final Pattern OCTAL_REGEX = Pattern.compile("^[0-7]+");
    private static final Pattern HEXADECIMAL_REGEX = Pattern.compile("^[0-9a-fA-F]+");
    private static final Pattern ROMAN_REGEX = Pattern.compile("^[ivxlcdm]+", Pattern.CASE_INSENSITIVE);
    private static final Map<Character, Integer> ROMAN_VALUES = Map.of(
        'I', 1, 'V', 5, 'X', 10, 'L', 50, 'C', 100, 'D', 500, 'M', 1000
    );
    
    private static long parseSpecial(String message, boolean commentsEnabled) {
        if (message.indexOf('\u200B') != -1 || message.indexOf('\u200C') != -1 || message.indexOf('\u200D') != -1 || message.indexOf('\uFEFF') != -1)
            message = ZERO_WIDTH_SPACE_REGEX.matcher(message).replaceAll("");
        
        return switch (specialCountingMode) {
            case BINARY -> parseWithRadix(message, commentsEnabled, BINARY_REGEX, 2);
            case OCTAL -> parseWithRadix(message, commentsEnabled, OCTAL_REGEX, 8);
            case DECIMAL, DOUBLE, POWER_OF_TWO -> parseNumber(message, commentsEnabled);
            case HEXADECIMAL ->  parseWithRadix(message, commentsEnabled, HEXADECIMAL_REGEX, 16);
            case ROMAN -> parseRoman(message, commentsEnabled);
        };
    }
    
    private static long parseWithRadix(String message, boolean commentsEnabled, Pattern pattern, int radix) {
        long number = -1;
        Matcher matcher = pattern.matcher(message);
        boolean matched = commentsEnabled ? matcher.find() : matcher.matches();
        if (matched) {
            try { number = Long.parseLong(matcher.group(), radix); }
            catch (NumberFormatException e) { Log.err("Failed to parse the number from the counting message.", e); }
        }
        
        return number;
    }
    
    private static long parseRoman(String message, boolean commentsEnabled) {
        Matcher matcher = ROMAN_REGEX.matcher(message);
        boolean matched = commentsEnabled ? matcher.find() : matcher.matches();
        if (!matched) return -1;
        
        String upper = message.toUpperCase(Locale.ROOT);
        long number = 0;
        
        for (int i = 0; i < upper.length(); i++) {
            int current = ROMAN_VALUES.get(upper.charAt(i));
            int next = (i + 1 < upper.length()) ? ROMAN_VALUES.get(upper.charAt(i + 1)) : 0;
            number += (current < next) ? -current : current;
        }
        
        return number > 0 ? number : -1;
    }
    
    private static void changeSpecialMode() {
        // Select a random mode from the enum, different from the current one
        CountingMode[] modes = CountingMode.values();
        CountingMode newMode;
        do { newMode = modes[(int) (Math.random() * modes.length)]; } while (newMode == specialCountingMode);
        
        specialCountingMode = newMode;
        lastSpecialModeChangeTimestamp = System.currentTimeMillis();
    }
    
    public static void changeSpecialMode(String mode) {
        specialCountingMode = CountingMode.valueOf(mode.toUpperCase());
        lastSpecialModeChangeTimestamp = System.currentTimeMillis();
    }
}
