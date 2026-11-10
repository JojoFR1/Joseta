package dev.jojofr.joseta.utils;

import dev.jojofr.joseta.JosetaBot;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.daos.ConfigurationDao;
import dev.jojofr.joseta.database.daos.MarkovBlacklistDao;
import dev.jojofr.joseta.database.daos.MessageDao;
import dev.jojofr.joseta.database.daos.UserDao;
import dev.jojofr.joseta.database.entities.ConfigurationEntity;
import dev.jojofr.joseta.database.entities.LeaderboardEntry;
import dev.jojofr.joseta.entities.GuildConfiguration;
import dev.jojofr.joseta.entities.GuildStatsCache;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class BotCache {
    private static final ConcurrentHashMap<Long, GuildConfiguration> guildConfigurations = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, GuildStatsCache> guildStatsCaches = new ConcurrentHashMap<>();
    
    public static final Emoji CHECK_EMOJI, CROSS_EMOJI, AUTO_RESPONSE_EMOJI, ICON_EMOJI;
    
    static {
        boolean debug = JosetaBot.debug;
        
        //                                                    Debug Emoji ID         Production Emoji ID
        CHECK_EMOJI = Emoji.fromCustom("yes", 1486803003229864147L, false);
        CROSS_EMOJI = Emoji.fromCustom("no", 1486803124411830372L, false);
        AUTO_RESPONSE_EMOJI = Emoji.fromCustom("doyouknowtheway", debug ? 1533590589705289960L : 1533590364080832703L, false);
        ICON_EMOJI = Emoji.fromCustom("mindustryfrance", 1487469879702327336L, false);
    }
    
    public static GuildConfiguration getGuildConfiguration(long guildId) {
        return guildConfigurations.computeIfAbsent(guildId, id -> {
            ConfigurationEntity config = Database.withExtension(ConfigurationDao.class, dao -> dao.getByGuildId(id));
            if (config == null) {
                config = new ConfigurationEntity(id);
                
                ConfigurationEntity finalConfig = config;
                Database.useExtension(ConfigurationDao.class, dao -> dao.upsert(finalConfig));
            }
            
            GuildConfiguration guildConfig = new GuildConfiguration(config);
            guildConfig.markovBlacklistIds = Database.withExtension(MarkovBlacklistDao.class, dao -> dao.getAllIds(id));
            
            return guildConfig;
        });
    }
    
    public static ConfigurationEntity getConfiguration(long guildId) { return getGuildConfiguration(guildId).configuration; }
    
    public static void putGuildConfiguration(long guildId, GuildConfiguration guildConfig) { guildConfigurations.put(guildId, guildConfig); }
    public static void removeGuildConfiguration(long guildId) { guildConfigurations.remove(guildId); }
    
    public static GuildStatsCache getGuildStatsCache(long guildId) {
        return guildStatsCaches.computeIfAbsent(guildId, id ->
            Database.withHandle(handle -> {
                ConfigurationEntity config = getConfiguration(guildId);
                long[] excludedChannels = { config.countingChannelId, config.countingSpecialChannelId, 1534307776963022848L };
                
                int totalMessages = handle.attach(MessageDao.class).getGuildMessageCountExcludingChannels(guildId, excludedChannels);
                long totalVoiceTime = handle.attach(UserDao.class).getTotalTimeVoice(guildId);
                List<LeaderboardEntry> messageLeaderboard = handle.attach(MessageDao.class).getMessageLeaderboard(guildId, 50, 0, excludedChannels);
                List<LeaderboardEntry> voiceLeaderboard = handle.attach(UserDao.class).getVoiceLeaderboard(guildId, 50, 0);
                
                return new GuildStatsCache(id, totalMessages, totalVoiceTime, messageLeaderboard, voiceLeaderboard);
            })
        );
    }
    
    public static void checkExpiredStatsCache(int expirationSeconds) {
        Instant expiration = Instant.now().minusSeconds(expirationSeconds);
        
        guildStatsCaches.entrySet().removeIf(entry ->
            entry.getValue() == null || entry.getValue().timestamp.isBefore(expiration)
        );
    }
}
