package joseta.database;

import joseta.*;

import arc.files.*;
import arc.struct.*;
import arc.util.*;

import java.sql.*;

public class ConfigDatabase {
    private static final String dbFileName = "resources/database/config.db";
    private static Connection conn;

    public static void initialize() {
        Fi dbFile = new Fi(dbFileName);
        try {
            if (!dbFile.exists()) {
                dbFile.write();

                conn = DriverManager.getConnection("jdbc:sqlite:" + dbFileName);
                initializeTable();
            }
            else conn = DriverManager.getConnection("jdbc:sqlite:" + dbFileName);
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not initialize the SQL table.", e);
        } catch (ArcRuntimeException e) {
            JosetaBot.logger.error("Could not create the 'config.db' file.", e);
        }
    }

    private static void initializeTable() throws SQLException {
        String configTable = "CREATE TABLE configurations ("
                            + "guildId BIGINT PRIMARY KEY,"
                            + "welcomeEnabled BOOLEAN DEFAULT FALSE,"
                            + "welcomeChannelId BIGINT DEFAULT 0,"
                            + "newMemberRoleId BIGINT DEFAULT 0,"
                            + "botRoleId BIGINT DEFAULT 0,"
                            + "markovEnabled BOOLEAN DEFAULT FALSE,"
                            + "markovChannelBlackList TEXT DEFAULT '',"
                            + "markovCategoryBlackList TEXT DEFAULT '',"
                            + "modLogEnabled BOOLEAN DEFAULT FALSE,"
                            + "modLogChannelId BIGINT DEFAULT 0"
                            + ")";

        Statement stmt = conn.createStatement();
        stmt.execute(configTable);
        stmt.close();
    }

    public static void addNewConfig(long guildId) {
        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO config (guildId) VALUES (?)")) {
            pstmt.setLong(1, guildId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not add new config.", e);
        }
    }

    public static void updateConfig(ConfigEntry entry) {
        try (PreparedStatement pstmt = conn.prepareStatement(
            "UPDATE config SET welcomeEnabled = ?, welcomeChannelId = ?, newMemberRoleId = ?, botRoleId = ?,"
            + "markovEnabled = ?, markovChannelBlackList = ?, markovCategoryBlackList = ?, "
            + "modLogEnabled = ? WHERE guildId = ?"
        )) {
            pstmt.setBoolean(1, entry.welcomeEnabled);
            pstmt.setLong(2, entry.welcomeChannelId);
            pstmt.setLong(3, entry.newMemberRoleId);
            pstmt.setLong(4, entry.botRoleId);
            pstmt.setBoolean(5, entry.markovEnabled);
            pstmt.setString(6, String.join(",", entry.markovChannelBlackList.toArray(String.class)));
            pstmt.setString(7, String.join(",", entry.markovCategoryBlackList.toArray(String.class)));
            pstmt.setBoolean(8, entry.modLogEnabled);
            pstmt.setLong(9, entry.guildId);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            JosetaBot.logger.error("Error updating config for guild ID: " + entry.guildId, e);
        }
    }

    public static ConfigEntry getConfigEntry(long guildId) {
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM config WHERE guildId = ?")) {
            pstmt.setLong(1, guildId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new ConfigEntry(
                    guildId,
                    rs.getBoolean("welcomeEnabled"),
                    rs.getLong("welcomeChannelId"),
                    rs.getLong("newMemberRoleId"),
                    rs.getLong("botRoleId"),
                    rs.getBoolean("markovEnabled"),
                    parseLongArray(rs.getString("markovChannelBlackList").split(",")),
                    parseLongArray(rs.getString("markovCategoryBlackList").split(",")),
                    rs.getBoolean("modLogEnabled")
                );
            }
            pstmt.close();
        } catch (SQLException e) {
            JosetaBot.logger.error("Error retrieving config entry for guild ID: " + guildId, e);
        }

        return null;
    }

    private static Seq<Long> parseLongArray (String[] values) {
        Seq<Long> result = new Seq<>(values.length);
        for (String value : values) {
            result.add(Long.parseLong(value));
        }

        return result;
    }

    public static class ConfigEntry {
        public long guildId;

        public boolean welcomeEnabled;
        public long welcomeChannelId;
        public long newMemberRoleId;
        public long botRoleId;
        
        public boolean markovEnabled;
        public Seq<Long> markovChannelBlackList;
        public Seq<Long> markovCategoryBlackList;
        
        public boolean modLogEnabled;

        public ConfigEntry(
            long guildId,
            boolean welcomeEnabled, long welcomeChannelId, long newMemberRoleId, long botRoleId,
            boolean markovEnabled, Seq<Long> markovChannelBlackList, Seq<Long> markovCategoryBlackList,
            boolean modLogEnabled
        ) {
            this.guildId = guildId;

            this.welcomeEnabled = welcomeEnabled;
            this.welcomeChannelId = welcomeChannelId;
            this.newMemberRoleId = newMemberRoleId;
            this.botRoleId = botRoleId;

            this.markovEnabled = markovEnabled;
            this.markovChannelBlackList = markovChannelBlackList;
            this.markovCategoryBlackList = markovCategoryBlackList;
            
            this.modLogEnabled = modLogEnabled;
        }
    }
}
