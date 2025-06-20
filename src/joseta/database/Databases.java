package joseta.database;

import joseta.*;
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

    private Dao<ConfigEntry, Long> configDao;
    private Dao<GuildEntry, Long> guildDao;
    private Dao<UserEntry, Long> userDao;
    private Dao<MessageEntry, Long> messageDao;
    private Dao<SanctionEntry, Long> sanctionDao;

    private Databases() {}

    public static Databases getInstance() {
        if (instance == null) {
            instance = new Databases();
        }
        return instance;
    }

    public boolean initialize() {
        if (connectionSource != null) {
            JosetaBot.logger.warn("Database connection already was initialized but was reinitialized. Initializiong will be skipped.");
            return true;
        }

        try {
            connectionSource = new JdbcConnectionSource(databaseUrl);
            TableInfo.
            TableUtils.createTableIfNotExists(connectionSource, ConfigEntry.class);
            configDao = DaoManager.createDao(connectionSource, ConfigEntry.class);

            TableUtils.createTableIfNotExists(connectionSource, GuildEntry.class);
            TableUtils.createTableIfNotExists(connectionSource, UserEntry.class);
            TableUtils.createTableIfNotExists(connectionSource, MessageEntry.class);
            TableUtils.createTableIfNotExists(connectionSource, SanctionEntry.class);

            guildDao = DaoManager.createDao(connectionSource, GuildEntry.class);
            userDao = DaoManager.createDao(connectionSource, UserEntry.class);
            messageDao = DaoManager.createDao(connectionSource, MessageEntry.class);
            sanctionDao = DaoManager.createDao(connectionSource, SanctionEntry.class);
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not initialize the database connection.", e);
            return false;
        }

        return true;
    }

    public ConnectionSource getConnectionSource() {
        return connectionSource;
    }

    public Dao<ConfigEntry, Long> getConfigDao() {
        return configDao;
    }

    public Dao<GuildEntry, Long> getGuildDao() {
        return guildDao;
    }

    public Dao<UserEntry, Long> getUserDao() {
        return userDao;
    }

    public Dao<MessageEntry, Long> getMessageDao() {
        return messageDao;
    }

    public Dao<SanctionEntry, Long> getSanctionDao() {
        return sanctionDao;
    }
}
