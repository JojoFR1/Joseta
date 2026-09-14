package dev.jojofr.joseta.events;

import dev.jojofr.joseta.annotations.EventModule;
import dev.jojofr.joseta.annotations.types.EventHandler;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.daos.MessageDao;
import dev.jojofr.joseta.database.entities.ConfigurationEntity;
import dev.jojofr.joseta.database.helper.MessageDatabase;
import dev.jojofr.joseta.events.channel.CountingChannel;
import dev.jojofr.joseta.utils.BotCache;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;

import java.util.regex.Pattern;

@EventModule
public class MessageEvents {
    // TODO improve, too many false positives
    private static final Pattern sentenceSplitter = Pattern.compile("[.?!,;:\\n]+");
    private static final Pattern questionPattern = Pattern.compile(
        "\\b(?:com*[ea]nt?|pos*ible*|m(?:oyen|ani[èeé]re)|fa[cç]on)\\b", Pattern.CASE_INSENSITIVE
    );
    private static final Pattern multiplayerPattern = Pattern.compile(
        "\\b(?:multi[ -]?(?:joeu?r|playeu?r*)?|co+p(?:eration|[ea]?ins?)?|amis?|po[eo]s?|[aà] (?:deux|[2-9]|[1-9][1-9]+|plu?si?e?u?rs?))\\b", Pattern.CASE_INSENSITIVE
    );
    
    //TODO unhardcode message
    public static final String autoResponseMessage =
        BotCache.AUTO_RESPONSE_EMOJI.getFormatted() + " Vous voulez héberger votre partie pour jouer avec des amis ?\nVous trouverez plus d'informations ici : <https://zetamap.fr/mindustry_hosting/>";
    
    @EventHandler
    public void onMessageReceived(MessageReceivedEvent event) {
        MessageDatabase.addNewMessage(event.getMessage());
        
        ConfigurationEntity config = BotCache.getConfiguration(event.getGuild().getIdLong());
        if (config.autoResponseEnabled) handleAutoResponse(event);
        
        if (config.countingEnabled && !event.getAuthor().isBot()) handleCounting(event, config);
    }
    
    private void handleAutoResponse(MessageReceivedEvent event) {
        String text = event.getMessage().getContentRaw();
        for (String sentence : sentenceSplitter.split(text))
            if (questionPattern.matcher(sentence).find() && multiplayerPattern.matcher(sentence).find()) {
                event.getMessage().reply(autoResponseMessage + "\n*Ceci est une réponse automatique possiblement hors-sujet.*").queue();
                return;
            }
    }
    
    private void handleCounting(MessageReceivedEvent event, ConfigurationEntity config) {
        Message message = event.getMessage();
        if ((message.getType() != MessageType.DEFAULT && message.getType() != MessageType.INLINE_REPLY) || message.getPoll() != null) return;
        
        MessageChannelUnion channel = event.getChannel();
        if (channel.getIdLong() == config.countingChannelId) CountingChannel.check(channel, message);
        else if (channel.getIdLong() == config.countingSpecialChannelId) CountingChannel.specialCheck(channel, message);
    }
    
    @EventHandler
    public void onMessageUpdate(MessageUpdateEvent event) {
        MessageDatabase.updateMessage(event.getMessage());
    }
    
    @EventHandler
    public void onMessageDelete(MessageDeleteEvent event) {
        MessageDatabase.deleteMessage(event.getMessageIdLong());
    }
    
    @EventHandler
    public void onMessageBulkDelete(MessageBulkDeleteEvent event) {
        for (String messageId : event.getMessageIds())
            MessageDatabase.deleteMessage(Long.parseLong(messageId));
    }
    
    @EventHandler
    public void onChannelDelete(ChannelDeleteEvent event) {
        Database.useExtension(MessageDao.class, dao -> dao.deleteByChannelId(event.getChannel().getIdLong()));
    }
}
