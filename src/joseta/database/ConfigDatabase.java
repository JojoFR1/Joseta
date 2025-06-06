package joseta.database;

import joseta.*;

import arc.files.*;
import arc.struct.*;
import arc.util.*;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.*;
import net.dv8tion.jda.api.hooks.*;

import java.sql.*;

public class ConfigDatabase extends ListenerAdapter {
    private static final String dbFileName = "resources/database/config.db";
    private static Connection conn;

    public static void initialize() {
        Fi dbFile = new Fi(dbFileName);
        try {
            if (!dbFile.exists()) {
                dbFile.write();

                conn = DriverManager.getConnection("jdbc:sqlite:" + dbFileName);
                initializeTable();
                populateTable();
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
                            + "welcomeJoinMessage TEXT DEFAULT 'Bienvenue {{user}} !',"
                            + "welcomeLeaveMessage TEXT DEFAULT '**{{userName}}** nous a quitté...',"
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

    private static void populateTable() {
        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO config (guildId) VALUES (?)")) {
            for (Guild guild : JosetaBot.bot.getGuilds()) {
                pstmt.setLong(1, guild.getIdLong());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not populate the config table.", e);
        }
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        long guildId = event.getGuild().getIdLong();
        if (getConfigEntry(guildId) != null) return; // Guild already has a config.
     
        addNewConfig(guildId);
        JosetaBot.logger.info("Added new config for guild ID: " + guildId);
    }


    public static void addNewConfig(long guildId) {
        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO config (guildId) VALUES (?)")) {
            pstmt.setLong(1, guildId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not add new config.", e);
        }
    }

    public static boolean updateConfig(ConfigEntry entry) {
        try (PreparedStatement pstmt = conn.prepareStatement(
            "UPDATE config SET welcomeEnabled = ?, welcomeChannelId = ?, welcomeJoinMessage = ?, welcomeLeaveMessage = ?, newMemberRoleId = ?, botRoleId = ?,"
            + "markovEnabled = ?, markovChannelBlackList = ?, markovCategoryBlackList = ?, "
            + "modLogEnabled = ? WHERE guildId = ?"
        )) {
            pstmt.setBoolean(1, entry.welcomeEnabled);
            pstmt.setLong(2, entry.welcomeChannelId);
            pstmt.setString(3, entry.welcomeJoinMessage);
            pstmt.setString(4, entry.welcomeLeaveMessage);
            pstmt.setLong(5, entry.newMemberRoleId);
            pstmt.setLong(6, entry.botRoleId);
            pstmt.setBoolean(7, entry.markovEnabled);
            pstmt.setString(8, String.join(",", entry.markovChannelBlackList.toArray(String.class)));
            pstmt.setString(9, String.join(",", entry.markovCategoryBlackList.toArray(String.class)));
            pstmt.setBoolean(10, entry.modLogEnabled);
            pstmt.setLong(11, entry.guildId);

            pstmt.executeUpdate();
            
            return true;
        } catch (SQLException e) {
            JosetaBot.logger.error("Error updating config for guild ID: " + entry.guildId, e);
        }

        return false;
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
                    rs.getString("welcomeJoinMessage"),
                    rs.getString("welcomeLeaveMessage"),
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
        public String welcomeJoinMessage;
        public String welcomeLeaveMessage;
        public long newMemberRoleId;
        public long botRoleId;
        
        public boolean markovEnabled;
        public Seq<Long> markovChannelBlackList;
        public Seq<Long> markovCategoryBlackList;
        
        public boolean modLogEnabled;

        public ConfigEntry(
            long guildId,
            boolean welcomeEnabled, long welcomeChannelId, String welcomeJoinMessage, String welcomeLeaveMessage, long newMemberRoleId, long botRoleId,
            boolean markovEnabled, Seq<Long> markovChannelBlackList, Seq<Long> markovCategoryBlackList,
            boolean modLogEnabled
        ) {
            this.guildId = guildId;

            this.welcomeEnabled = welcomeEnabled;
            this.welcomeChannelId = welcomeChannelId;
            this.welcomeJoinMessage = welcomeJoinMessage;
            this.welcomeLeaveMessage = welcomeLeaveMessage;
            this.newMemberRoleId = newMemberRoleId;
            this.botRoleId = botRoleId;

            this.markovEnabled = markovEnabled;
            this.markovChannelBlackList = markovChannelBlackList;
            this.markovCategoryBlackList = markovCategoryBlackList;
            
            this.modLogEnabled = modLogEnabled;
        }
    }
}
