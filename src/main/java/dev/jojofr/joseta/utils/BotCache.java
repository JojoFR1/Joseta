package dev.jojofr.joseta.utils;

import dev.jojofr.joseta.JosetaBot;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.entities.ConfigurationEntity;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.util.concurrent.ConcurrentHashMap;

public class BotCache {
    private static ConcurrentHashMap<Long, ConfigurationEntity> guildConfigurations = new ConcurrentHashMap<>();
    
    public static final Emoji CHECK_EMOJI, CROSS_EMOJI;
    
    static {
        boolean debug = JosetaBot.debug;
        
        //                                                    Debug Emoji ID         Production Emoji ID
        CHECK_EMOJI = Emoji.fromCustom("yes", debug ? 1459377029328801832L : 1451286173791031337L, false);
        CROSS_EMOJI = Emoji.fromCustom("no", debug ? 1459377027747680266L : 1451286184817987719L, false);
    }
    
    public static ConfigurationEntity getGuildConfiguration(long guildId) {
        return guildConfigurations.computeIfAbsent(guildId, id -> {
            ConfigurationEntity config = Database.get(ConfigurationEntity.class, id);
            if (config == null) {
                config = new ConfigurationEntity(id);
                Database.create(config);
            }
            return config;
        });
    }
    
    public static void putGuildConfiguration(long guildId, ConfigurationEntity config) {
        guildConfigurations.put(guildId, config);
    }
    
    public static void removeGuildConfiguration(long guildId) {
        guildConfigurations.remove(guildId);
    }
}
