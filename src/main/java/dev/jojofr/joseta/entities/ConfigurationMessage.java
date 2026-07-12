package dev.jojofr.joseta.entities;

import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.daos.MessageDao;
import dev.jojofr.joseta.database.entities.ConfigurationEntity;
import dev.jojofr.joseta.utils.BotCache;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class ConfigurationMessage {
    public ConfigurationEntity configuration;
    public Set<Long> pendingMarkovUserBlacklist;
    public Set<Long> pendingMarkovRoleBlacklist;
    public Set<Long> pendingMarkovChannelBlacklist;
    
    public boolean hasChanged = false;
    public boolean hasMarkovBlacklistChanged = false;
    public boolean isMainMenu = true;
    
    public Long currentRulesChannelId = null;
    public final Instant timestamp;
    
    public ConfigurationMessage(long guildId, Instant timestamp) {
        this.configuration = new ConfigurationEntity(BotCache.getGuildConfiguration(guildId));
        
        this.pendingMarkovUserBlacklist = new HashSet<>();
        this.pendingMarkovRoleBlacklist = new HashSet<>();
        this.pendingMarkovChannelBlacklist = new HashSet<>();
        
        Database.useExtension(MessageDao.MarkovBlacklistDao.class, dao -> {
            this.pendingMarkovUserBlacklist.addAll(dao.getIds(guildId, MessageDao.EntityType.USER));
            this.pendingMarkovRoleBlacklist.addAll(dao.getIds(guildId, MessageDao.EntityType.ROLE));
            this.pendingMarkovChannelBlacklist.addAll(dao.getIds(guildId, MessageDao.EntityType.CHANNEL));
        });
        
        this.timestamp = timestamp;
    }
}
