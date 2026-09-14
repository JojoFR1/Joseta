package dev.jojofr.joseta.events;

import dev.jojofr.joseta.annotations.EventModule;
import dev.jojofr.joseta.annotations.types.EventHandler;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.daos.UserDao;
import dev.jojofr.joseta.database.entities.UserEntity;
import dev.jojofr.joseta.entities.GuildConfiguration;
import dev.jojofr.joseta.events.channel.WelcomeChannel;
import dev.jojofr.joseta.utils.BotCache;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.util.concurrent.ConcurrentHashMap;

@EventModule
public class MiscEvents {
    
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
