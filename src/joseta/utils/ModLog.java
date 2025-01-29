package joseta.utils;

import joseta.*;
import joseta.utils.struct.*;

import java.io.*;
import java.sql.*;
import java.time.*;

public final class ModLog {
    private static ModLog instance;

    private static final String urlDb = "jdbc:sqlite:resources/modlog.db";
    private static final String[] sanctionTypes = {"warn", "mute", "kick", "ban"};
    private static Connection conn;

    private ModLog() {
        initialize();
    }

    public static ModLog getInstance() {
        if (instance == null)
            instance = new ModLog();

        return instance;
    }

    public static void initialize() {
        File dbFile = new File("resources/modlog.db");
        try {
            if (!dbFile.exists()) {
                dbFile.createNewFile();
                
                conn = DriverManager.getConnection(urlDb);

                initializeTable();
            } else conn = DriverManager.getConnection(urlDb);
            
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not initialize the SQL table.", e);
        } catch (IOException e) {
            JosetaBot.logger.error("Could not create the 'modlog.db' file.", e);
        }
    }

    private static void initializeTable() throws SQLException {
        String lastValuesTable = "CREATE TABLE lastValues ("
                               + "name TEXT PRIMARY KEY,"
                               + "value INT DEFAULT -1"
                               + ")";
        
        String usersTable = "CREATE TABLE users ("
                          + "id BIGINT PRIMARY KEY,"
                          + "guildId BIGINT,"
                          + "totalSanctions INT"
                          + ")";

        String sanctionsTable = "CREATE TABLE sanctions (" 
                              + "id INT PRIMARY KEY," 
                              + "userId BIGINT FOREIGN KEY REFERENCES users(userId),"
                              + "moderatorId BIGINT,"
                              + "guildId BIGINT,"
                              + "reason TEXT,"
                              + "at TEXT,"
                              + "for BIGINT,"
                              + "expired BOOLEAN DEFAULT FALSE"
                              + ")";

        Statement stmt = conn.createStatement();
        stmt.execute(lastValuesTable);
        stmt.execute(usersTable);
        stmt.execute(sanctionsTable);

        for (String sanctionType : sanctionTypes) {
            String insertQuery = "INSERT INTO lastValues (name, value) VALUES (?, -1)";
            PreparedStatement pstmt = conn.prepareStatement(insertQuery);
            pstmt.setString(1, sanctionType);
            pstmt.executeUpdate();
        }
    }

    private static void addNewUser(long userId, long  guildId) {
        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users "
                                                           + "(id, guildId, totalSanctions)"
                                                           + "VALUES (?, ?, 0)"))
        {
            pstmt.setLong(1, userId);
            pstmt.setLong(2, guildId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not create a new user.", e);
        }
    }

    public static void log(int sanctionTypeId, long userId, long moderatorId, long guildId, String reason, long time) {
        String sanctionType = sanctionTypes[sanctionTypeId/10 - 1];
        
        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO sanctions "
                                                           + "(id, userId, moderatorId, guildId, reason, at, for, expired) "
                                                           + "VALUES (?, ?, ?, ?, ?, ?, ?, FALSE)"))
        {
            int lastSanctionId = getLastSanctionId(sanctionType);
            pstmt.setInt(1, Integer.parseInt(Integer.toString(sanctionTypeId) + (lastSanctionId + 1)));
            pstmt.setLong(2, userId);
            pstmt.setLong(3, moderatorId);
            pstmt.setLong(4, guildId);
            pstmt.setString(5, reason);
            pstmt.setString(6, Instant.now().toString());
            pstmt.setLong(7, time);

            pstmt.executeUpdate();
            updateLastSanctionId(lastSanctionId + 1, sanctionType);
            updateUserTotalSanctions(userId, guildId);
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not log the new sanction.", e);
        }
    }

    public static Seq<Sanction> getUserLog(long userId, long guildId, int page, int maxPerPage) {
        Seq<Sanction> sanctions = new Seq<>();

        try (PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM sanctions WHERE userId = ? AND guildId = ?")) {
            pstmt.setLong(1, userId);
            pstmt.setLong(2, guildId);

            int i = -1;
            int startIndex = (page - 1) * maxPerPage;
            int endIndex = Math.min(startIndex + maxPerPage, getUserTotalSanctions(userId, guildId));

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                i++;
                if (i < startIndex) continue;
                if (i >= endIndex) break;
                sanctions.add(new Sanction(
                    rs.getLong("id"),
                    rs.getLong("userId"),
                    rs.getLong("moderatorId"),
                    rs.getLong("guildId"),
                    rs.getString("reason"),
                    Instant.parse(rs.getString("at")),
                    rs.getLong("for")
                ));
            }
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not get user sanction log.", e);;
        }

        return sanctions;
    }

    public static Seq<Sanction> getExpiredSanctions() {
        Seq<Sanction> sanctions = new Seq<>();

        try (PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM sanctions WHERE for >= 1 AND expired != TRUE")) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Instant at = Instant.parse(rs.getString("at"));
                long forTime = rs.getLong("for");
                if (at.plusSeconds(forTime).isBefore(Instant.now())) {
                    sanctions.add(new Sanction(
                        rs.getLong("id"),
                        rs.getLong("userId"),
                        rs.getLong("moderatorId"),
                        rs.getLong("guildId"),
                        rs.getString("reason"),
                        at,
                        forTime
                    ));
                }
            }
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not get expired sanctions.", e);
        }

        return sanctions;
    }

    public static void removeSanction(Sanction sanction) {
        try (PreparedStatement pstmt = conn.prepareStatement("UPDATE sanctions SET expired = TRUE WHERE id = ? AND guildId = ?")) {
            pstmt.setLong(1, sanction.id);
            pstmt.setLong(2, sanction.guildId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not remove the sanction.", e);
        }
    }

    public static class Sanction {
        public final long id;
        public final long userId;
        public final long moderatorId;
        public final long guildId;
        public final String reason;
        public final Instant at;
        public final long time; 

        public Sanction(long id, long userId, long moderatorId, long guildId, String reason, Instant at, long time) {
            this.id = id;
            this.userId = userId;
            this.moderatorId = moderatorId;
            this.guildId = guildId;
            this.reason = reason;
            this.at = at;
            this.time = time;
        }

        public int getSanctionTypeId() {
            return Integer.parseInt(Long.toString(id).substring(0, 2));
        }

        public boolean isExpired() {
            return at.plusSeconds(time).isBefore(Instant.now()) && time >= 1;
        }
    } 

    private static int getLastSanctionId(String sanctionType) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement("SELECT value FROM lastValues WHERE name = ?");
        pstmt.setString(1, sanctionType);

        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) return rs.getInt("value");
        else return -1;
    }

    private static void updateLastSanctionId(int newSanctionId, String sanctionType) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement("UPDATE lastValues SET value = ? WHERE name = ?");
        pstmt.setInt(1, newSanctionId);
        pstmt.setString(2, sanctionType);
        pstmt.executeUpdate();
    }

    public static int getUserTotalSanctions(long userId, long guildId) {
        int total = 0;
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT totalSanctions FROM users WHERE id = ? AND guildId = ?")) {
            pstmt.setLong(1, userId);
            pstmt.setLong(2, guildId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) total = rs.getInt("totalSanctions");
            else addNewUser(userId, guildId); // The user doesn't exist so add it, total will be 0 by default.
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not get user total sanctions.", e);
        }

        return total;
    }

    private static void updateUserTotalSanctions(long userId, long guildId) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement("UPDATE users SET totalSanctions = ? WHERE id = ? AND guildId = ?");
        pstmt.setInt(1, getUserTotalSanctions(userId, guildId) + 1); // It will create a new user if it doesn't exist.
        pstmt.setLong(2, userId);
        pstmt.setLong(3, guildId);
        pstmt.executeUpdate();
    }
}
