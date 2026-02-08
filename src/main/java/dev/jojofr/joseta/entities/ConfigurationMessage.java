package dev.jojofr.joseta.entities;

import dev.jojofr.joseta.database.entities.Configuration;
import dev.jojofr.joseta.utils.BotCache;

import java.time.Instant;

public class ConfigurationMessage {
    public Configuration configuration;
    public boolean hasChanged = false;
    public boolean isMainMenu = true;
    
    public final Instant timestamp;
    
    public ConfigurationMessage(long guildId, Instant timestamp) {
        this.configuration = BotCache.guildConfigurations.get(guildId);
        this.timestamp = timestamp;
    }
}
