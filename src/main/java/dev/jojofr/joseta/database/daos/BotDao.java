package dev.jojofr.joseta.database.daos;

import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.time.Instant;

public interface BotDao {
    @SqlUpdate("UPDATE bot SET last_online = NOW()")
    void setLastOnline();
    
    @SqlQuery("SELECT last_online FROM bot")
    Instant getLastOnline();
}
