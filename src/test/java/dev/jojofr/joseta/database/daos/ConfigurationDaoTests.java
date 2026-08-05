package dev.jojofr.joseta.database.daos;

import dev.jojofr.joseta.database.AbstractDaoTest;
import dev.jojofr.joseta.database.entities.ConfigurationEntity;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class ConfigurationDaoTests extends AbstractDaoTest<ConfigurationEntity, ConfigurationDao> {
    
    @Override
    protected Class<ConfigurationDao> getDaoClass() { return ConfigurationDao.class; }
    
    @Override
    protected ConfigurationEntity buildDefault() { return new ConfigurationEntity(1L); }
    
    @Override
    protected ConfigurationEntity buildFull() {
        ConfigurationEntity config = new ConfigurationEntity(1L);
        config.welcomeEnabled = true;
        config.welcomeImageEnabled = true;
        config.welcomeChannelId = 123L;
        config.welcomeJoinMessage = "Welcome {{user}}!";
        config.welcomeLeaveMessage = "Goodbye {{userName}}!";
        config.joinRoleId = 456L;
        config.joinRoleBotId = 789L;
        config.roleVerifiedId = 101112L;
        config.markovEnabled = true;
        config.moderationEnabled = false;
        config.moderationHoneypotEnabled = true;
        config.moderationHoneypotChannelId = 192021L;
        config.rules = "Be nice!";
        config.autoResponseEnabled = true;
        config.countingEnabled = true;
        config.countingCommentsEnabled = true;
        config.countingPenaltyEnabled = true;
        config.countingChannelId = 222324L;
        config.countingSpecialChannelId = 252627L;
        
        return config;
    }
    
    @Override
    protected ConfigurationEntity buildUpdated() {
        ConfigurationEntity updatedConfig = new ConfigurationEntity(1L);
        updatedConfig.welcomeEnabled = false;
        updatedConfig.welcomeImageEnabled = false;
        updatedConfig.welcomeChannelId = 321L;
        updatedConfig.welcomeJoinMessage = "Bienvenue {{user}}!";
        updatedConfig.welcomeLeaveMessage = "Au revoir {{userName}}!";
        updatedConfig.joinRoleId = 654L;
        updatedConfig.joinRoleBotId = 987L;
        updatedConfig.roleVerifiedId = 211101L;
        updatedConfig.markovEnabled = false;
        updatedConfig.moderationEnabled = true;
        updatedConfig.moderationHoneypotEnabled = false;
        updatedConfig.moderationHoneypotChannelId = 120291L;
        updatedConfig.rules = "Soit cool !";
        updatedConfig.autoResponseEnabled = false;
        updatedConfig.countingEnabled = false;
        updatedConfig.countingCommentsEnabled = false;
        updatedConfig.countingPenaltyEnabled = false;
        updatedConfig.countingChannelId = 423222L;
        updatedConfig.countingSpecialChannelId = 726252L;
        
        return updatedConfig;
    }
    
    @Override
    protected void upsert(ConfigurationDao dao, ConfigurationEntity entity) { dao.upsert(entity); }
    @Override
    protected ConfigurationEntity fetch(ConfigurationDao dao) { return dao.getByGuildId(1L); }
    
    @Override
    protected Set<String> excludedFields() { return Set.of("guildId"); }
}
