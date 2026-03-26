package dev.jojofr.joseta.utils;

import dev.jojofr.joseta.JosetaBot;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.entities.ConfigurationEntity;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class BotCache {
    private static final ConcurrentHashMap<Long, ConfigurationEntity> guildConfigurations = new ConcurrentHashMap<>();
    
    public static final Emoji CHECK_EMOJI, CROSS_EMOJI;
    private static final Random random = new Random();
    
    static {
        boolean debug = JosetaBot.debug;
        
        //                                                    Debug Emoji ID         Production Emoji ID
        CHECK_EMOJI = Emoji.fromCustom("yes", debug ? 1486803003229864147L : 1486803003229864147L, false);
        CROSS_EMOJI = Emoji.fromCustom("no", debug ? 1486803124411830372L : 1486803124411830372L, false);
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
    
    public static Random getRandom(long seed) {
        random.setSeed(seed + System.nanoTime());
        return random;
    }
    
    public static void putGuildConfiguration(long guildId, ConfigurationEntity config) { guildConfigurations.put(guildId, config); }
    public static void removeGuildConfiguration(long guildId) {
        guildConfigurations.remove(guildId);
    }
}
