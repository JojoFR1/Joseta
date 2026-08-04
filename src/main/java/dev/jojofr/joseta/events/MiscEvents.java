package dev.jojofr.joseta.events;

import dev.jojofr.joseta.annotations.EventModule;
import dev.jojofr.joseta.annotations.types.EventHandler;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.daos.MessageDao;
import dev.jojofr.joseta.database.daos.UserDao;
import dev.jojofr.joseta.database.entities.ConfigurationEntity;
import dev.jojofr.joseta.database.entities.UserEntity;
import dev.jojofr.joseta.database.helper.MessageDatabase;
import dev.jojofr.joseta.entities.GuildConfiguration;
import dev.jojofr.joseta.events.misc.CountingChannel;
import dev.jojofr.joseta.events.misc.WelcomeChannel;
import dev.jojofr.joseta.utils.BotCache;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@EventModule
public class MiscEvents {
    //#region Message
    @EventHandler
    public void onMessageReceived(MessageReceivedEvent event) {
        MessageDatabase.addNewMessage(event.getMessage());
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
    //#endregion
    
    
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
    public void autoResponse(MessageReceivedEvent event) {
        ConfigurationEntity config = BotCache.getConfiguration(event.getGuild().getIdLong());
        if (!config.autoResponseEnabled) return;
        
        String text = event.getMessage().getContentRaw();
        for (String sentence : sentenceSplitter.split(text))
            if (questionPattern.matcher(sentence).find() && multiplayerPattern.matcher(sentence).find()) {
                event.getMessage().reply(autoResponseMessage + "\n*Ceci est une réponse automatique possiblement hors-sujet.*").queue();
                return;
            }
    }
    
    
    @EventHandler
    public void countingCheck(MessageReceivedEvent event) {
        ConfigurationEntity config = BotCache.getConfiguration(event.getGuild().getIdLong());
        if (!config.countingEnabled) return;
        
        if (event.getAuthor().isBot()) return;
        
        if (event.getChannel().getIdLong() == config.countingChannelId) CountingChannel.check(event.getChannel(), event.getMessage());
        else if (event.getChannel().getIdLong() == config.countingSpecialChannelId) CountingChannel.specialCheck(event.getChannel(), event.getMessage());
    }
    
    
    @EventHandler
    public void memberJoin(GuildMemberJoinEvent event) {
        GuildConfiguration guildConfig = BotCache.getGuildConfiguration(event.getGuild().getIdLong());
        if (!guildConfig.configuration.welcomeEnabled) return;
        
        TextChannel channel = guildConfig.getWelcomeChannel(event.getGuild());
        if (channel == null) return;
        
        Role role = event.getUser().isBot() ? guildConfig.getJoinBotRole(event.getGuild()) : guildConfig.getJoinRole(event.getGuild());
        if (role != null) event.getGuild().addRoleToMember(event.getUser(), role).reason("Rôle d'arrivée automatique").queue();
        
        if (!guildConfig.configuration.welcomeImageEnabled) {
            WelcomeChannel.sendWelcomeMessage(guildConfig.configuration.welcomeJoinMessage, channel, event.getUser());
            return;
        }
        
        WelcomeChannel.renderWelcomeImage(event.getUser(), event.getGuild().getMemberCount()).thenAccept(image -> {
            if (image == null) {
                WelcomeChannel.sendWelcomeMessage(guildConfig.configuration.welcomeJoinMessage, channel, event.getUser());
                return;
            }
            channel.sendMessage(event.getUser().getAsMention()).addFiles(FileUpload.fromData(image, "welcome.png")).queue();
        });
    }
    
    @EventHandler
    public void memberRemove(GuildMemberRemoveEvent event) {
        GuildConfiguration guildConfig = BotCache.getGuildConfiguration(event.getGuild().getIdLong());
        if (!guildConfig.configuration.welcomeEnabled) return;
        
        TextChannel channel = guildConfig.getWelcomeChannel(event.getGuild());
        if (channel == null) return;
        
        if (guildConfig.configuration.welcomeLeaveMessage.isEmpty()) return;
        
        channel.sendMessage(guildConfig.configuration.welcomeLeaveMessage.replace("{{userName}}", event.getUser().getName())).queue();
    }
    
    
    private static final ConcurrentHashMap<Long, Long> userVoiceJoinTime = new ConcurrentHashMap<>();
    
    @EventHandler
    public void voiceChannelUpdate(GuildVoiceUpdateEvent event) {
        AudioChannelUnion joinedChannel = event.getChannelJoined();
        AudioChannelUnion leftChannel = event.getChannelLeft();
        
        // Left a voice channel
        if (leftChannel != null) {
            Long time = userVoiceJoinTime.remove(event.getMember().getIdLong());
            if (time != null) {
                long timeSpent = System.currentTimeMillis() - time;
                Database.useExtension(UserDao.class, dao -> {
                    if (dao.addTimeVoice(event.getMember().getIdLong(), event.getGuild().getIdLong(), timeSpent) == 0)
                        dao.upsert(new UserEntity(event.getMember()).setTimeVoice(timeSpent));
                });
            }
        }
        // Joined a voice channel
        if (joinedChannel != null) userVoiceJoinTime.put(event.getMember().getIdLong(), System.currentTimeMillis());
    }
}
