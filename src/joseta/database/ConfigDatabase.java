package joseta.database;

import joseta.*;

import arc.files.*;
import arc.struct.*;
import arc.util.*;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.*;
import net.dv8tion.jda.api.hooks.*;

import java.net.*;
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
        String configTable =
        """
        CREATE TABLE config (
            guildId BIGINT PRIMARY KEY,
            welcomeEnabled BOOLEAN DEFAULT FALSE,
            welcomeChannelId BIGINT DEFAULT 0,
            welcomeImageEnabled BOOLEAN DEFAULT FALSE,
            welcomeImageUrl TEXT DEFAULT NULL,
            welcomeJoinMessage TEXT DEFAULT 'Bienvenue {{user}} !',
            welcomeLeaveMessage TEXT DEFAULT '**{{userName}}** nous a quitté...',
            newMemberRoleId BIGINT DEFAULT 0,
            newBotRoleId BIGINT DEFAULT 0,
            markovEnabled BOOLEAN DEFAULT FALSE,
            markovChannelBlackList TEXT DEFAULT '',
            markovCategoryBlackList TEXT DEFAULT '',
            modLogEnabled BOOLEAN DEFAULT FALSE,
            autoResponseEnabled BOOLEAN DEFAULT FALSE
        )""";

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
        if (getConfig(guildId) != null) return; // Guild already has a config.
     
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
            "UPDATE config SET welcomeEnabled = ?, welcomeChannelId = ?, welcomeImageEnabled = ?, welcomeImageUrl = ?, welcomeJoinMessage = ?, welcomeLeaveMessage = ?, newMemberRoleId = ?, newBotRoleId = ?,"
            + "markovEnabled = ?, markovChannelBlackList = ?, markovCategoryBlackList = ?, "
            + "modLogEnabled = ?, autoResponseEnabled = ? WHERE guildId = ?"
        )) {
            int i = 1;
            pstmt.setBoolean(i++, entry.welcomeEnabled);
            pstmt.setLong(i++, entry.welcomeChannelId);
            pstmt.setBoolean(i++, entry.welcomeImageEnabled);
            pstmt.setString(i++, entry.welcomeImageUrl != null ? entry.welcomeImageUrl.toString() : null);
            pstmt.setString(i++, entry.welcomeJoinMessage);
            pstmt.setString(i++, entry.welcomeLeaveMessage);
            pstmt.setLong(i++, entry.newMemberRoleId);
            pstmt.setLong(i++, entry.newBotRoleId);
            pstmt.setBoolean(i++, entry.markovEnabled);
            pstmt.setString(i++, entry.markovChannelBlackList.size != 0 ? Strings.join(",", entry.markovChannelBlackList.map(String::valueOf)) : "");
            pstmt.setString(i++, entry.markovCategoryBlackList.size != 0 ? Strings.join(",", entry.markovCategoryBlackList.map(String::valueOf)) : "");
            pstmt.setBoolean(i++, entry.moderationEnabled);
            pstmt.setBoolean(i++, entry.autoResponseEnabled);
            pstmt.setLong(i++, entry.guildId);

            pstmt.executeUpdate();
            
            return true;
        } catch (SQLException e) {
            JosetaBot.logger.error("Error updating config for guild ID: " + entry.guildId, e);
        }

        return false;
    }

    public static ConfigEntry getConfig(long guildId) {
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM config WHERE guildId = ?")) {
            pstmt.setLong(1, guildId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new ConfigEntry(
                    guildId,
                    rs.getBoolean("welcomeEnabled"),
                    rs.getLong("welcomeChannelId"),
                    rs.getBoolean("welcomeImageEnabled"),
                    rs.getString("welcomeImageUrl") != null ? URI.create(rs.getString("welcomeImageUrl")).toURL() : null,
                    rs.getString("welcomeJoinMessage"),
                    rs.getString("welcomeLeaveMessage"),
                    rs.getLong("newMemberRoleId"),
                    rs.getLong("newBotRoleId"),
                    rs.getBoolean("markovEnabled"),
                    parseLongArray(rs.getString("markovChannelBlackList").split(",")),
                    parseLongArray(rs.getString("markovCategoryBlackList").split(",")),
                    rs.getBoolean("modLogEnabled"),
                    rs.getBoolean("autoResponseEnabled")
                );
            }
            pstmt.close();
        } catch (SQLException e) {
            JosetaBot.logger.error("Error retrieving config entry for guild ID: " + guildId, e);
        } catch (MalformedURLException e) {
            JosetaBot.logger.error("Error parsing URL for guild ID: " + guildId, e);
        }

        return null;
    }

    private static Seq<Long> parseLongArray(String[] values) {
        Seq<Long> result = new Seq<>(values.length);
        for (String value : values) {
            if (!value.isEmpty()) result.add(Long.parseLong(value));
        }

        return result;
    }

    public static class ConfigEntry {
        public long guildId;

        //#region Welcome
        public boolean welcomeEnabled; // TODO maybe separate the leave and join enabled? + global enable?
        public long welcomeChannelId;
        public boolean welcomeImageEnabled;
        //TODO adapt welcome message for url & var like {{var}}
        public URL welcomeImageUrl; //todo hard to implement with text position (especially when only text config is available).
        public String welcomeJoinMessage;
        public String welcomeLeaveMessage;
        public long newMemberRoleId;
        public long newBotRoleId;
        //#endregion

        //#region Markov
        public boolean markovEnabled;
        public Seq<Long> markovChannelBlackList;
        public Seq<Long> markovCategoryBlackList;
        //#endregion
        
        public boolean moderationEnabled;

        public boolean autoResponseEnabled;

        public ConfigEntry(
            long guildId,

            boolean welcomeEnabled, long welcomeChannelId, boolean welcomeImageEnabled,
            URL welcomeImageUrl, String welcomeJoinMessage, String welcomeLeaveMessage,
            long newMemberRoleId, long newBotRoleId,

            boolean markovEnabled, Seq<Long> markovChannelBlackList, Seq<Long> markovCategoryBlackList,

            boolean moderationEnabled,

            boolean autoResponseEnabled
        ) {
            this.guildId = guildId;

            this.welcomeEnabled = welcomeEnabled;
            this.welcomeChannelId = welcomeChannelId;
            this.welcomeImageEnabled = welcomeImageEnabled;
            this.welcomeImageUrl = welcomeImageUrl;
            this.welcomeJoinMessage = welcomeJoinMessage;
            this.welcomeLeaveMessage = welcomeLeaveMessage;
            this.newMemberRoleId = newMemberRoleId;
            this.newBotRoleId = newBotRoleId;

            this.markovEnabled = markovEnabled;
            this.markovChannelBlackList = markovChannelBlackList;
            this.markovCategoryBlackList = markovCategoryBlackList;
            
            this.moderationEnabled = moderationEnabled;

            this.autoResponseEnabled = autoResponseEnabled;
        }

        public ConfigEntry setWelcomeEnabled(boolean welcomeEnabled) { this.welcomeEnabled = welcomeEnabled; return this; }
        public ConfigEntry setWelcomeChannelId(long welcomeChannelId) { this.welcomeChannelId = welcomeChannelId; return this; }
        public ConfigEntry setWelcomeImageEnabled(boolean welcomeImageEnabled) { this.welcomeImageEnabled = welcomeImageEnabled; return this; }
        public ConfigEntry setWelcomeImageUrl(URL welcomeImageUrl) { this.welcomeImageUrl = welcomeImageUrl; return this; }
        public ConfigEntry setWelcomeJoinMessage(String welcomeJoinMessage) { this.welcomeJoinMessage = welcomeJoinMessage; return this; }
        public ConfigEntry setWelcomeLeaveMessage(String welcomeLeaveMessage) { this.welcomeLeaveMessage = welcomeLeaveMessage; return this; }
        public ConfigEntry setNewMemberRoleId(long newMemberRoleId) { this.newMemberRoleId = newMemberRoleId; return this; }
        public ConfigEntry setNewBotRoleId(long newBotRoleId) { this.newBotRoleId = newBotRoleId; return this; }
        public ConfigEntry setMarkovEnabled(boolean markovEnabled) { this.markovEnabled = markovEnabled; return this; }
        public ConfigEntry setMarkovChannelBlackList(Seq<Long> markovChannelBlackList) { this.markovChannelBlackList = markovChannelBlackList; return this; }
        public ConfigEntry setMarkovCategoryBlackList(Seq<Long> markovCategoryBlackList) { this.markovCategoryBlackList = markovCategoryBlackList; return this; }
        public ConfigEntry setModerationEnabled(boolean moderationEnabled) { this.moderationEnabled = moderationEnabled; return this; }
        public ConfigEntry setAutoResponseEnabled(boolean autoResponseEnabled) { this.autoResponseEnabled = autoResponseEnabled; return this; }

        public ConfigEntry addMarkovChannelBlackList(long channelId) {
            if (!markovChannelBlackList.contains(channelId) && channelId != 0L) {
                markovChannelBlackList.add(channelId);
            }
            return this;
        }
        public ConfigEntry removeMarkovChannelBlackList(long channelId) {
            if (channelId != 0L) markovChannelBlackList.remove(channelId);
            return this;
        }
        public ConfigEntry addMarkovCategoryBlackList(long categoryId) {
            if (!markovCategoryBlackList.contains(categoryId) &&categoryId != 0L) {
                markovCategoryBlackList.add(categoryId);
            }
            return this;
        }
        public ConfigEntry removeMarkovCategoryBlackList(long categoryId) {
            if (categoryId != 0L) markovCategoryBlackList.remove(categoryId);
            return this;
        }
    }
}
