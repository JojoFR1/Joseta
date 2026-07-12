package dev.jojofr.joseta.database.daos;

import dev.jojofr.joseta.database.entities.ConfigurationEntity;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindFields;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface ConfigurationDao {
    @SqlUpdate("""
        INSERT INTO configurations (
            guild_id,
            welcome_enabled, welcome_image_enabled, welcome_channel_id, welcome_join_message, welcome_leave_message,
            join_role_id, join_role_bot_id, role_verified_id,
            markov_enabled,
            moderation_enabled, moderation_honeypot_enabled, moderation_honeypot_channel_id, rules,
            auto_response_enabled,
            counting_enabled, counting_comments_enabled, counting_penalty_enabled, counting_channel_id
        ) VALUES (
            :guildId,
            :welcomeEnabled, :welcomeImageEnabled, :welcomeChannelId, :welcomeJoinMessage, :welcomeLeaveMessage,
            :joinRoleId, :joinRoleBotId, :roleVerifiedId,
            :markovEnabled,
            :moderationEnabled, :moderationHoneypotEnabled, :moderationHoneypotChannelId, :rules,
            :autoResponseEnabled,
            :countingEnabled, :countingCommentsEnabled, :countingPenaltyEnabled, :countingChannelId
        ) ON CONFLICT (guild_id) DO UPDATE SET
            welcome_enabled = EXCLUDED.welcome_enabled,
            welcome_image_enabled = EXCLUDED.welcome_image_enabled,
            welcome_channel_id = EXCLUDED.welcome_channel_id,
            welcome_join_message = EXCLUDED.welcome_join_message,
            welcome_leave_message = EXCLUDED.welcome_leave_message,
            join_role_id = EXCLUDED.join_role_id,
            join_role_bot_id = EXCLUDED.join_role_bot_id,
            role_verified_id = EXCLUDED.role_verified_id,
            markov_enabled = EXCLUDED.markov_enabled,
            moderation_enabled = EXCLUDED.moderation_enabled,
            moderation_honeypot_enabled = EXCLUDED.moderation_honeypot_enabled,
            moderation_honeypot_channel_id = EXCLUDED.moderation_honeypot_channel_id,
            rules = EXCLUDED.rules,
            auto_response_enabled = EXCLUDED.auto_response_enabled,
            counting_enabled = EXCLUDED.counting_enabled,
            counting_comments_enabled = EXCLUDED.counting_comments_enabled,
            counting_penalty_enabled = EXCLUDED.counting_penalty_enabled,
            counting_channel_id = EXCLUDED.counting_channel_id
    """)
    void upsert(@BindFields ConfigurationEntity configurationEntity);
    
    @SqlQuery("SELECT * FROM configurations WHERE guild_id = :guildId")
    @RegisterFieldMapper(value = ConfigurationEntity.class)
    ConfigurationEntity getByGuildId(long guildId);
}
