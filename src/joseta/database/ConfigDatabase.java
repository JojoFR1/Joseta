package joseta.database;

import joseta.*;
import joseta.database.entry.*;

import arc.files.*;
import arc.struct.*;
import arc.util.*;

import net.dv8tion.jda.api.entities.*;

import java.net.*;
import java.sql.*;

public class ConfigDatabase {
    private static final String dbFileName = "resources/database/config.db";
    private static Connection conn;
    
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
            "UPDATE config SET welcomeEnabled = ?, welcomeChannelId = ?, welcomeImageEnabled = ?, welcomeImageUrl = ?, welcomeJoinMessage = ?, welcomeLeaveMessage = ?,"
            + "joinRoleId = ?, joinBotRoleId = ?, verifiedRoleId = ?, markovEnabled = ?, markovChannelBlackList = ?, markovCategoryBlackList = ?, "
            + "modLogEnabled = ?, autoResponseEnabled = ? WHERE guildId = ?"
        )) {
            int i = 1;
            pstmt.setBoolean(i++, entry.welcomeEnabled);
            pstmt.setLong(i++, entry.welcomeChannelId);
            pstmt.setBoolean(i++, entry.welcomeImageEnabled);
            pstmt.setString(i++, entry.welcomeImageUrl != null ? entry.welcomeImageUrl.toString() : null);
            pstmt.setString(i++, entry.welcomeJoinMessage);
            pstmt.setString(i++, entry.welcomeLeaveMessage);
            pstmt.setLong(i++, entry.joinRoleId);
            pstmt.setLong(i++, entry.joinBotRoleId);
            pstmt.setLong(i++, entry.verifiedRoleId);
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
                    rs.getLong("joinRoleId"),
                    rs.getLong("joinBotRoleId"),
                    rs.getLong("verifiedRoleId"),
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
}
