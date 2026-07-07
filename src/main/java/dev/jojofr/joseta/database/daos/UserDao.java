package dev.jojofr.joseta.database.daos;

import dev.jojofr.joseta.database.entities.UserEntity;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindFields;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface UserDao {
    @SqlUpdate("""
        INSERT INTO users (id, guild_id, name, avatar_url, creation_date, sanction_count)
        VALUES (:id, :guildId, :name, :avatarUrl, :creationDate, :sanctionCount)
    """)
    void upsert(@BindFields UserEntity user);
    
    @SqlUpdate("UPDATE users SET sanction_count = sanction_count + 1 WHERE id = :id AND guild_id = :guildId")
    void incrementSanctionCount(@Bind("id") long id, @Bind("guildId") long guildId);
    
    @SqlQuery("SELECT * FROM users WHERE id = :id AND guild_id = :guildId")
    @RegisterFieldMapper(value = UserEntity.class)
    UserEntity getById(@Bind("id") long id, @Bind("guildId") long guildId);
    
    @SqlUpdate("DELETE FROM users WHERE id = :id AND guild_id = :guildId")
    void delete(@Bind("id") long id, @Bind("guildId") long guildId);
}
