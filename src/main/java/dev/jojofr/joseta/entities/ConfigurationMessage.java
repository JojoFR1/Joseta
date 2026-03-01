package dev.jojofr.joseta.entities;

import dev.jojofr.joseta.database.entities.ConfigurationEntity;
import dev.jojofr.joseta.utils.BotCache;

import java.time.Instant;

public class ConfigurationMessage {
    public ConfigurationEntity configuration;
    public boolean hasChanged = false;
    public boolean hasMarkovBlacklistChanged = false;
    public boolean isMainMenu = true;
    
    public Long currentRulesChannelId = null;
    public final Instant timestamp;
    
    public ConfigurationMessage(long guildId, Instant timestamp) {
        this.configuration = BotCache.getGuildConfiguration(guildId);
        this.timestamp = timestamp;
    }
}
