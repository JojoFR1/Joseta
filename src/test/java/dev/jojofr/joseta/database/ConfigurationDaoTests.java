package dev.jojofr.joseta.database;

import dev.jojofr.joseta.database.daos.ConfigurationDao;
import dev.jojofr.joseta.database.daos.GuildDao;
import dev.jojofr.joseta.database.entities.ConfigurationEntity;
import dev.jojofr.joseta.database.entities.GuildEntity;
import org.assertj.core.api.SoftAssertions;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class ConfigurationDaoTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18");
    
    static Jdbi jdbi;
    
    @BeforeAll
    static void setup() throws IllegalAccessException {
        assertBuiltConfigurationHasNoDefaultValues();
        
        Flyway.configure()
            .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
            .locations("classpath:database")
            .validateMigrationNaming(true)
            .loggers("slf4j")
            .load()
            .migrate();
        
        jdbi = Jdbi.create(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        jdbi.installPlugin(new SqlObjectPlugin());
        jdbi.installPlugin(new PostgresPlugin());
        
        jdbi.useExtension(GuildDao.class, dao -> dao.upsert(new GuildEntity(1L, "Guild", "url", 999L)));
    }
    
    @Test
    void upsertPersistsEveryFieldOnInsert() {
        ConfigurationDao dao = jdbi.onDemand(ConfigurationDao.class);
        
        ConfigurationEntity original = buildConfigurationEntity();
        dao.upsert(original);
        
        ConfigurationEntity fetched = dao.getByGuildId(1L);
        
        assertThat(fetched).usingRecursiveComparison().isEqualTo(original);
    }
    
    @Test
    void upsertPersistsEveryFieldOnUpdate() throws IllegalAccessException {
        ConfigurationDao dao = jdbi.onDemand(ConfigurationDao.class);
        
        ConfigurationEntity original = buildConfigurationEntity();
        ConfigurationEntity updated = buildUpdatedConfigurationEntity();
        assertEveryFieldChanged(original, updated);
        
        dao.upsert(original);
        
        ConfigurationEntity updated = new ConfigurationEntity(1L);
        updated.welcomeEnabled = false;
        updated.welcomeImageEnabled = false;
        updated.welcomeChannelId = 321L;
        updated.welcomeJoinMessage = "Bienvenue {{user}}!";
        updated.welcomeLeaveMessage = "Au revoir {{userName}}!";
        updated.joinRoleId = 654L;
        updated.joinRoleBotId = 987L;
        updated.roleVerifiedId = 211101L;
        updated.markovEnabled = false;
        updated.moderationEnabled = true;
        updated.moderationHoneypotEnabled = false;
        updated.moderationHoneypotChannelId = 120291L;
        updated.rules = "Soit cool !";
        updated.autoResponseEnabled = false;
        updated.countingEnabled = false;
        updated.countingCommentsEnabled = false;
        updated.countingPenaltyEnabled = false;
        updated.countingChannelId = 423222L;
        dao.upsert(updated);
        
        ConfigurationEntity fetched = dao.getByGuildId(1L);
        
        assertThat(fetched).usingRecursiveComparison().isEqualTo(updated);
    }
    
    static void assertBuiltConfigurationHasNoDefaultValues() throws IllegalAccessException {
        ConfigurationEntity base = new ConfigurationEntity(1L);
        ConfigurationEntity built = buildConfigurationEntity();
        
        SoftAssertions softly = new SoftAssertions();
        
        for (Field field : ConfigurationEntity.class.getDeclaredFields()) {
            field.setAccessible(true);
            
            if (field.getName().equals("guildId")) continue;
            if (field.getName().equals("markovBlacklistIds")) continue;
            
            Object baseValue = field.get(base);
            Object builtValue = field.get(built);
            
            softly.assertThat(builtValue).as("Field '%s' still has its default value", field.getName())
                .isNotEqualTo(baseValue);
        }
        
        softly.assertAll();
    }
    
    static void assertEveryFieldChanged(Object original, Object updated) throws IllegalAccessException {
        SoftAssertions softly = new SoftAssertions();
        
        for (Field field : original.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            
            switch (field.getName()) {
                case "guildId", "markovBlacklistIds" -> {
                    continue;
                }
            }
            
            softly.assertThat(field.get(updated))
                .as("Field '%s' was not changed in the updated fixture", field.getName())
                .isNotEqualTo(field.get(original)).isNotNull();
        }
        
        softly.assertAll();
    }
    
    static ConfigurationEntity buildConfigurationEntity() {
        ConfigurationEntity defaultConfig = new ConfigurationEntity(1L);
        defaultConfig.welcomeEnabled = true;
        defaultConfig.welcomeImageEnabled = true;
        defaultConfig.welcomeChannelId = 123L;
        defaultConfig.welcomeJoinMessage = "Welcome {{user}}!";
        defaultConfig.welcomeLeaveMessage = "Goodbye {{userName}}!";
        defaultConfig.joinRoleId = 456L;
        defaultConfig.joinRoleBotId = 789L;
        defaultConfig.roleVerifiedId = 101112L;
        defaultConfig.markovEnabled = true;
        defaultConfig.moderationEnabled = false;
        defaultConfig.moderationHoneypotEnabled = true;
        defaultConfig.moderationHoneypotChannelId = 192021L;
        defaultConfig.rules = "Be nice!";
        defaultConfig.autoResponseEnabled = true;
        defaultConfig.countingEnabled = true;
        defaultConfig.countingCommentsEnabled = true;
        defaultConfig.countingPenaltyEnabled = true;
        defaultConfig.countingChannelId = 222324L;
        
        return defaultConfig;
    }
    
    static ConfigurationEntity buildUpdatedConfigurationEntity() {
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
        
        return updatedConfig;
    }
}
