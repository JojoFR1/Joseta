package dev.jojofr.joseta.utils;

import dev.jojofr.joseta.JosetaBot;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.daos.ConfigurationDao;
import dev.jojofr.joseta.database.daos.MessageDao;
import dev.jojofr.joseta.database.entities.ConfigurationEntity;
import dev.jojofr.joseta.entities.GuildConfiguration;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.util.concurrent.ConcurrentHashMap;

public class BotCache {
    private static final ConcurrentHashMap<Long, GuildConfiguration> guildConfigurations = new ConcurrentHashMap<>();
    
    public static final Emoji CHECK_EMOJI, CROSS_EMOJI, AUTO_RESPONSE_EMOJI;
    
    static {
        boolean debug = JosetaBot.debug;
        
        //                                                    Debug Emoji ID         Production Emoji ID
        CHECK_EMOJI = Emoji.fromCustom("yes", debug ? 1459377029328801832L : 1451286173791031337L, false);
        CROSS_EMOJI = Emoji.fromCustom("no", debug ? 1459377027747680266L : 1451286184817987719L, false);
        AUTO_RESPONSE_EMOJI = Emoji.fromCustom("doyouknowtheway", debug ? 1533590589705289960L : 1533590364080832703L, false);
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
            guildConfig.markovBlacklistIds = Database.withExtension(MessageDao.MarkovBlacklistDao.class, dao -> dao.getAllIds(id));
            
            return guildConfig;
        });
    }
    
    public static ConfigurationEntity getConfiguration(long guildId) { return getGuildConfiguration(guildId).configuration; }
    
    public static void putGuildConfiguration(long guildId, GuildConfiguration guildConfig) { guildConfigurations.put(guildId, guildConfig); }
    public static void removeGuildConfiguration(long guildId) { guildConfigurations.remove(guildId); }
}
