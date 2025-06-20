package joseta.database;

import joseta.database.entry.*;

import java.sql.*;

import com.j256.ormlite.dao.*;
import com.j256.ormlite.jdbc.*;
import com.j256.ormlite.support.*;
import com.j256.ormlite.table.*;

public class Databases {
    private static final String databaseUrl = "jdbc:sqlite:resources/bot.db";
    private static Databases instance;
    
    private ConnectionSource connectionSource;

    private Databases() throws SQLException {
        connectionSource = new JdbcConnectionSource(databaseUrl);
        
        TableUtils.createTableIfNotExists(connectionSource, ConfigEntry.class);
        TableUtils.createTableIfNotExists(connectionSource, GuildEntry.class);
        TableUtils.createTableIfNotExists(connectionSource, UserEntry.class);
        TableUtils.createTableIfNotExists(connectionSource, MessageEntry.class);
        TableUtils.createTableIfNotExists(connectionSource, SanctionEntry.class);
    }

    public static Databases getInstance() throws SQLException {
        if (instance == null) {
            instance = new Databases();
        }
        return instance;
    }

    public ConnectionSource getConnectionSource() { return connectionSource; }

    public Dao<ConfigEntry, Long> getConfigDao() throws SQLException {
        return DaoManager.createDao(connectionSource, ConfigEntry.class);
    }

    public Dao<GuildEntry, Long> getGuildDao() throws SQLException{
        return DaoManager.createDao(connectionSource, GuildEntry.class);
    }

    public Dao<UserEntry, Long> getUserDao() throws SQLException {
        return DaoManager.createDao(connectionSource, UserEntry.class);
    }

    public Dao<MessageEntry, Long> getMessageDao() throws SQLException {
        return DaoManager.createDao(connectionSource, MessageEntry.class);
    }

    public Dao<SanctionEntry, Long> getSanctionDao() throws SQLException {
        return DaoManager.createDao(connectionSource, SanctionEntry.class);
    }
}
