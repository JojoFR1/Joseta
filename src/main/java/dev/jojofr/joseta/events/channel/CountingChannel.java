package dev.jojofr.joseta.events.channel;

import dev.jojofr.joseta.JosetaBot;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.daos.MessageDao;
import dev.jojofr.joseta.database.entities.ConfigurationEntity;
import dev.jojofr.joseta.database.entities.MessageEntity;
import dev.jojofr.joseta.utils.BotCache;
import dev.jojofr.joseta.utils.Log;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CountingChannel {
    private static boolean autoCheck = true;
    
    public static long lastNumber = -1;
    public static long lastAuthorId = -1;
    private static long lastMessageId = -1;
    private static long lastTimestamp = -1;
    
    public static long specialLastNumber = -1;
    public static long specialLastAuthorId = -1;
    private static long specialLastMessageId = -1;
    private static long specialLastTimestamp = -1;
    private static long lastSpecialModeChangeTimestamp = -1;
    public static CountingMode specialCountingMode = null, lastSpecialCountingMode = null;
    
    public enum CountingMode { BINARY, OCTAL, HEXADECIMAL, BASE36, ROMAN;
        
        @Override
        public String toString() {
            return switch (this) {
                case BINARY -> "Binaire";
                case OCTAL -> "Octal";
                case HEXADECIMAL -> "Hexadécimal";
                case BASE36 -> "Base 36";
                case ROMAN -> "Romain";
            };
        }
        
        public static CountingMode fromString(String mode) {
            return switch (mode.toLowerCase(Locale.ROOT)) {
                case "binaire", "binary" -> BINARY;
                case "octal" -> OCTAL;
                case "hexadécimal", "hexadecimal" -> HEXADECIMAL;
                case "base 36", "base36" -> BASE36;
                case "romain", "roman" -> ROMAN;
                default -> null;
            };
        }
    }
    
    
    public static boolean preCheck(MessageChannelUnion channel, Message message, boolean special) {
        long curentNumber = special ? specialLastNumber : lastNumber;
        
        if (curentNumber == -1) { // Initialize the needed values on bot launch
            MessageEntity previousMessage = Database.withExtension(MessageDao.class, dao ->
                dao.getLastCountingMessage(message.getGuildIdLong(), message.getChannelIdLong(), JosetaBot.get().getSelfUser().getIdLong(), message.getIdLong()));
            
            // First message in the channel, so no previous message to check
            if (previousMessage == null) {
                if (special) {
                    changeSpecialMode();
                    channel.sendMessage("Le mode de comptage spécial a été initialisé ! Le mode actuel est **"+ specialCountingMode +"**.").queue();
                    specialLastNumber = 1;
                } else lastNumber = 0;
                
                return false;
            }
            
            if (special) {
                MessageEntity modeChangeMessage = Database.withExtension(MessageDao.class, dao ->
                    dao.getLastCountingModeChangeMessage(message.getGuildIdLong(), message.getChannelIdLong(), JosetaBot.get().getSelfUser().getIdLong()));
                
                Log.debug("Last counting mode change message: {}", modeChangeMessage);
                // No previous mode, so new channel
                if (modeChangeMessage == null) {
                    changeSpecialMode();
                    channel.sendMessage("Le mode de comptage spécial a été initialisé ! Le mode actuel est **"+ specialCountingMode +"**.").queue();
                    specialLastNumber = 1;
                    lastSpecialModeChangeTimestamp = System.currentTimeMillis();
                } else {
                    String content = modeChangeMessage.content;
                    
                    int firstAsterisk = content.indexOf("**") + 2;
                    String currentMode = content.substring(firstAsterisk, content.indexOf("**", firstAsterisk));
                    specialCountingMode = CountingMode.fromString(currentMode);
                    lastSpecialModeChangeTimestamp = modeChangeMessage.createdAt.toEpochMilli();
                    
                    if (!content.contains("initialisé")) {
                        int secondAsterisk = content.indexOf("**", firstAsterisk + 2) + 2;
                        String previousMode = content.substring(secondAsterisk, content.indexOf("**", secondAsterisk));
                        lastSpecialCountingMode = CountingMode.fromString(previousMode);
                    }
                }
            }
            
            ConfigurationEntity config = BotCache.getConfiguration(message.getGuild().getIdLong());
            String content = previousMessage.content.replace(" ", "");
            
            long previousAuthorId = previousMessage.authorId;
            long previousNumber = special ? parseSpecial(content, config.countingCommentsEnabled) : parseNumber(content, config.countingCommentsEnabled);
            if (previousNumber == -1) previousNumber = 0;
            long previousTimestamp = previousMessage.createdAt.toEpochMilli();
            long previousMessageId = previousMessage.id;
            
            if (special) {
                if (specialCountingMode != null) specialLastNumber = previousNumber;
                specialLastAuthorId = previousAuthorId;
                specialLastTimestamp = previousTimestamp;
                specialLastMessageId = previousMessageId;
            } else {
                lastNumber = previousNumber;
                lastAuthorId = previousAuthorId;
                lastTimestamp = previousTimestamp;
                lastMessageId = previousMessageId;
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
            // Check if user is new (< 7 days join) and has less than 5 messages in counting channel, if so, don't apply the penalty and just delete the message
            if (!config.countingPenaltyEnabled || message.getMember().getTimeJoined().isAfter(OffsetDateTime.now().minusDays(7))) {
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
            if (!config.countingPenaltyEnabled || message.getMember().getTimeJoined().isAfter(OffsetDateTime.now().minusDays(7))) {
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
            if (!config.countingPenaltyEnabled || message.getMember().getTimeJoined().isAfter(OffsetDateTime.now().minusDays(7))) {
                message.reply(message.getAuthor().getAsMention() + " vous devez augmenter le nombre précédent par 1.").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
                message.delete().queue();
            } else {
                lastNumber = 0;
                message.addReaction(BotCache.CROSS_EMOJI).queue();
                message.reply(message.getAuthor().getAsMention() + " a cassé la chaîne ! Il fallait augmenter le nombre précédent par 1.\n\n-# Le comptage repart de 0.").queue();
            }
            return;
        }

        channel.retrieveMessageById(lastMessageId).queue(
            lastMessage -> lastMessage.clearReactions().queue(),
            failure -> Log.err("Failed to retrieve the last counting message to clear reactions.", failure)
        );
        message.addReaction(BotCache.CHECK_EMOJI).queue();
        
        lastNumber += 1;
        lastTimestamp = message.getTimeCreated().toInstant().toEpochMilli();
        lastMessageId = message.getIdLong();
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
            if (!config.countingPenaltyEnabled || message.getMember().getTimeJoined().isAfter(OffsetDateTime.now().minusDays(7))) {
                message.reply(message.getAuthor().getAsMention() + " vous ne pouvez pas compter deux fois de suite !").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
                message.delete().queue();
            } else {
                specialLastNumber = 0;
                message.addReaction(BotCache.CROSS_EMOJI).queue();
                message.reply(message.getAuthor().getAsMention() + " a cassé la chaîne ! Il fallait attendre que quelqu'un d'autre compte.\n\n-# Le comptage repart de 0, en mode **"+ specialCountingMode +"**.").queue();
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
                case HEXADECIMAL -> "hexadécimal";
                case BASE36 -> "en base 36";
                case ROMAN -> "romain";
            };
            if (!config.countingPenaltyEnabled || message.getMember().getTimeJoined().isAfter(OffsetDateTime.now().minusDays(7))) {
                message.reply(message.getAuthor().getAsMention() + " vous devez "+ hasToString +" des chiffres dans ce salon "+ type + "!").queue(
                    botMessage -> botMessage.delete().queueAfter(5, TimeUnit.SECONDS)
                );
                message.delete().queue();
            } else {
                specialLastNumber = 0;
                message.addReaction(BotCache.CROSS_EMOJI).queue();
                message.reply(message.getAuthor().getAsMention() + " a cassé la chaîne ! Il fallait "+ hasToString +" des chiffres "+ type +".\n\n-# Le comptage repart de 0, en mode **"+ specialCountingMode +"**.").queue();
            }
            return;
        }
        
        // Rule - Must increment the last number by 1
        long supposedNumber = specialLastNumber + 1;
        if (number != supposedNumber) {
            if (!config.countingPenaltyEnabled || message.getMember().getTimeJoined().isAfter(OffsetDateTime.now().minusDays(7))) {
                message.reply(message.getAuthor().getAsMention() + " vous devez augmenter le nombre précédent par 1.").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
                message.delete().queue();
            } else {
                specialLastNumber = 0;
                message.addReaction(BotCache.CROSS_EMOJI).queue();
                message.reply(message.getAuthor().getAsMention() + " a cassé la chaîne ! Il fallait augmenter le nombre précédent par 1.\n\n-# Le comptage repart de 0, en mode **"+ specialCountingMode +"**.").queue();
            }
            return;
        }
        
        channel.retrieveMessageById(specialLastMessageId).queue(
            lastMessage -> lastMessage.clearReactions().queue(),
            failure -> Log.err("Failed to retrieve the last special counting message to clear reactions.", failure)
        );
        message.addReaction(BotCache.CHECK_EMOJI).queue();
        
        specialLastNumber = number;
        specialLastTimestamp = message.getTimeCreated().toInstant().toEpochMilli();
        specialLastMessageId = message.getIdLong();
        
        
        if (lastSpecialModeChangeTimestamp == -1 || System.currentTimeMillis() - lastSpecialModeChangeTimestamp > TimeUnit.HOURS.toMillis(4)) {
            String oldMode = specialCountingMode.toString();
            changeSpecialMode();
            String mode = specialCountingMode.toString();
            channel.sendMessage("Le mode de comptage spécial a changé ! Le nouveau mode est **"+ mode +"** (anciennement **"+ oldMode +"**).").queue();
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
    private static final Pattern BASE36_REGEX = Pattern.compile("^[0-9a-zA-Z]+");
    private static final Pattern ROMAN_REGEX = Pattern.compile("^[ivxlcdm]+", Pattern.CASE_INSENSITIVE);
    private static final Map<Character, Integer> ROMAN_VALUES = Map.of(
        'I', 1, 'V', 5, 'X', 10, 'L', 50, 'C', 100, 'D', 500, 'M', 1000
    );
    
    private static long parseSpecial(String message, boolean commentsEnabled) {
        if (message.indexOf('\u200B') != -1 || message.indexOf('\u200C') != -1 || message.indexOf('\u200D') != -1 || message.indexOf('\uFEFF') != -1)
            message = ZERO_WIDTH_SPACE_REGEX.matcher(message).replaceAll("");
        
        if (specialCountingMode == null) return -1L;
        
        return switch (specialCountingMode) {
            case BINARY -> parseWithRadix(message, commentsEnabled, BINARY_REGEX, 2);
            case OCTAL -> parseWithRadix(message, commentsEnabled, OCTAL_REGEX, 8);
            case HEXADECIMAL -> parseWithRadix(message, commentsEnabled, HEXADECIMAL_REGEX, 16);
            case BASE36 -> parseWithRadix(message, commentsEnabled, BASE36_REGEX, 36);
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
        
        String upper = matcher.group().toUpperCase(Locale.ROOT);
        long number = 0;
        for (int i = 0; i < upper.length(); i++) {
            int current = ROMAN_VALUES.get(upper.charAt(i));
            number += (i + 1 < upper.length() && current < ROMAN_VALUES.get(upper.charAt(i + 1))) ? -current : current;
        }
        
        // Parse the number to roman and check if it matches the original input to ensure it's a valid roman numeral
        StringBuilder romanBuilder = new StringBuilder();
        long tempNumber = number;
        while (tempNumber > 0) {
            if (tempNumber >= 1000) { romanBuilder.append('M'); tempNumber -= 1000; }
            else if (tempNumber >= 900) { romanBuilder.append("CM"); tempNumber -= 900; }
            else if (tempNumber >= 500) { romanBuilder.append('D'); tempNumber -= 500; }
            else if (tempNumber >= 400) { romanBuilder.append("CD"); tempNumber -= 400; }
            else if (tempNumber >= 100) { romanBuilder.append('C'); tempNumber -= 100; }
            else if (tempNumber >= 90) { romanBuilder.append("XC"); tempNumber -= 90; }
            else if (tempNumber >= 50) { romanBuilder.append('L'); tempNumber -= 50; }
            else if (tempNumber >= 40) { romanBuilder.append("XL"); tempNumber -= 40; }
            else if (tempNumber >= 10) { romanBuilder.append('X'); tempNumber -= 10; }
            else if (tempNumber >= 9) { romanBuilder.append("IX"); tempNumber -= 9; }
            else if (tempNumber >= 5) { romanBuilder.append('V'); tempNumber -= 5; }
            else if (tempNumber >= 4) { romanBuilder.append("IV"); tempNumber -= 4; }
            else { romanBuilder.append('I'); tempNumber -= 1; }
        }
        
        if (!romanBuilder.toString().equals(upper)) return -1;
        
        return number > 0 ? number : -1;
    }
    
    private static void changeSpecialMode() {
        // Select a random mode from the enum, different from the current one
        CountingMode[] modes = CountingMode.values();
        CountingMode newMode;
        do { newMode = modes[(int) (Math.random() * modes.length)]; } while (newMode == specialCountingMode || newMode == lastSpecialCountingMode);
        
        lastSpecialCountingMode = specialCountingMode;
        specialCountingMode = newMode;
        lastSpecialModeChangeTimestamp = System.currentTimeMillis();
    }
    
    public static void changeSpecialMode(String mode) {
        lastSpecialCountingMode = specialCountingMode;
        specialCountingMode = CountingMode.fromString(mode);
        lastSpecialModeChangeTimestamp = System.currentTimeMillis();
    }
}
