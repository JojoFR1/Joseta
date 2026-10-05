package dev.jojofr.joseta.events;

import dev.jojofr.joseta.JosetaBot;
import dev.jojofr.joseta.annotations.EventModule;
import dev.jojofr.joseta.annotations.types.EventHandler;
import dev.jojofr.joseta.database.helper.UserDatabase;
import dev.jojofr.joseta.entities.GuildConfiguration;
import dev.jojofr.joseta.entities.GuildUserKey;
import dev.jojofr.joseta.events.channel.WelcomeChannel;
import dev.jojofr.joseta.utils.BotCache;
import dev.jojofr.joseta.utils.Log;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
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
    
    
    private static final ConcurrentHashMap<GuildUserKey, Long> userVoiceJoinTime = new ConcurrentHashMap<>();
    
    @EventHandler
    public void voiceChannelUpdate(GuildVoiceUpdateEvent event) {
        AudioChannelUnion joinedChannel = event.getChannelJoined();
        AudioChannelUnion leftChannel = event.getChannelLeft();
        
        GuildUserKey key = new GuildUserKey(event.getMember());
        // Left a voice channel
        if (leftChannel != null) {
            Long time = userVoiceJoinTime.remove(key);
            if (time == null) return;
            
            long timeSpent = System.currentTimeMillis() - time;
            UserDatabase.addTimeVoice(event.getMember(), timeSpent);
        }
        // Joined a voice channel
        if (joinedChannel != null) userVoiceJoinTime.put(key, System.currentTimeMillis());
    }
    
    public static void clearVoiceJoinTime() {
        for (GuildUserKey key : userVoiceJoinTime.keySet()) {
            Long time = userVoiceJoinTime.remove(key);
            if (time == null) continue;
            
            long timeSpent = System.currentTimeMillis() - time;
            
            Guild guild = JosetaBot.get().getGuildById(key.guildId());
            if (guild == null) {
                Log.warn("Guild with ID {} not found while clearing voice join time for user ID {}", key.guildId(), key.userId());
                continue;
            }
            
            Member member = guild.getMemberById(key.userId());
            if (member == null) {
                Log.warn("Member with ID {} not found in guild ID {} while clearing voice join time", key.userId(), key.guildId());
                continue;
            }
            
            UserDatabase.addTimeVoice(member, timeSpent);
        }
    }
}
