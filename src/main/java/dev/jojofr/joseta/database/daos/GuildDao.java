package dev.jojofr.joseta.database.daos;

import dev.jojofr.joseta.database.entities.GuildEntity;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindFields;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface GuildDao {
    @SqlUpdate("""
        INSERT INTO guilds (id, name, icon_url, owner_id)
        VALUES (:id, :name, :iconUrl, :ownerId)
        ON CONFLICT (id) DO UPDATE SET
            name = EXCLUDED.name,
            icon_url = EXCLUDED.icon_url,
            owner_id = EXCLUDED.owner_id
        """)
    void upsert(@BindFields GuildEntity guild);
    
    @SqlQuery("SELECT * FROM guilds WHERE id = :id")
    @RegisterFieldMapper(value = GuildEntity.class)
    GuildEntity getById(@Bind("id") long id);
    
    @SqlQuery("UPDATE guilds SET last_sanction_number = last_sanction_number + 1 WHERE id = :guildId RETURNING last_sanction_number")
    int nextSanctionNumber(@Bind("guildId") long guildId);
    
    @SqlUpdate("DELETE FROM guilds WHERE id = :id")
    void delete(@Bind("id") long id);
}
