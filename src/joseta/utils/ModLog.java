package joseta.utils;

import joseta.*;
import joseta.utils.struct.*;

import java.io.*;
import java.sql.*;
import java.time.*;

public final class ModLog {
    private static ModLog instance;

    private final String urlDb = "jdbc:sqlite:resources/modlog.db";
    private final String[] sanctionTypes = {"warn", "mute", "kick", "ban"};
    private Connection conn;

    private ModLog() {
        File dbFile = new File("resources/modlog.db");
        try {
            if (!dbFile.exists()) {
                dbFile.createNewFile();
                
                conn = DriverManager.getConnection(urlDb);

                initializeTable();
            } else conn = DriverManager.getConnection(urlDb);
            
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ModLog getInstance() {
        if (instance == null)
            instance = new ModLog();

        return instance;
    }

    private void initializeTable() throws SQLException {
        String lastValuesTable = "CREATE TABLE lastValues ("
                               + "name TEXT PRIMARY KEY,"
                               + "value INT DEFAULT -1"
                               + ")";
        
        String usersTable = "CREATE TABLE users ("
                          + "id BIGINT PRIMARY KEY,"
                          + "totalSanctions INT"
                          + ")";

        String sanctionsTable = "CREATE TABLE sanctions (" 
                              + "id INT PRIMARY KEY," 
                              + "userId BIGINT,"
                              + "moderatorId BIGINT,"
                              + "reason TEXT,"
                              + "at TEXT,"
                              + "for BIGINT"
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

    private void addNewUser(long userId) {
        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users "
                                                           + "(id, totalSanctions)"
                                                           + "VALUES (?, 0)"))
        {
            pstmt.setLong(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Vars.logger.error("Could not create a new user.", e);
        }
    }

    public void log(int sanctionTypeId, long userId, long moderatorId, String reason, long time) {
        String sanctionType = sanctionTypes[sanctionTypeId/10 - 1];
        
        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO sanctions "
                                                           + "(id, userId, moderatorId, reason, at, for) "
                                                           + "VALUES (?, ?, ?, ?, ?, ?)"))
        {
            int lastSanctionId = getLastSanctionId(sanctionType);
            pstmt.setInt(1, Integer.parseInt(Integer.toString(sanctionTypeId) + (lastSanctionId + 1)));
            pstmt.setLong(2, userId);
            pstmt.setLong(3, moderatorId);
            pstmt.setString(4, reason);
            pstmt.setString(5, Instant.now().toString());
            pstmt.setLong(6, time);

            pstmt.executeUpdate();
            updateLastSanctionId(lastSanctionId + 1, sanctionType);
            updateUserTotalSanctions(userId);
        } catch (SQLException e) {
            Vars.logger.error("Could not add new sanction", e);
        }
    }

    public Seq<Sanction> getUserLog(long userId, int page, int maxPerPage) {
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM sanctions WHERE userId = ?")) {
            pstmt.setLong(1, userId);
            Seq<Sanction> sanctions = new Seq<>();

            int i = -1;
            int startIndex = (page - 1) * maxPerPage;
            int endIndex = Math.min(startIndex + maxPerPage, getUserTotalSanctions(userId));

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                i++;
                if (i < startIndex) continue;
                if (i >= endIndex) break;
                sanctions.add(new Sanction(
                    rs.getLong("id"),
                    rs.getLong("moderatorId"),
                    rs.getString("reason"),
                    Instant.parse(rs.getString("at")),
                    rs.getLong("for")
                ));
            }
            return sanctions;
        } catch (SQLException e) {
            Vars.logger.error("Could not get user sanction log.", e);;
            return null;
        }
    }

    public class Sanction {
        public final long id;
        public final long moderatorId;
        public final String reason;
        public final Instant at;
        public final long time; 

        public Sanction(long id, long moderatorId, String reason, Instant at, long time) {
            this.id = id;
            this.moderatorId = moderatorId;
            this.reason = reason;
            this.at = at;
            this.time = time;
        }
    } 

    private int getLastSanctionId(String sanctionType) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement("SELECT value FROM lastValues WHERE name = ?");
        pstmt.setString(1, sanctionType);

        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) return rs.getInt("value");
        else return -1;
    }

    private void updateLastSanctionId(int newSanctionId, String sanctionType) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement("UPDATE lastValues SET value = ? WHERE name = ?");
        pstmt.setInt(1, newSanctionId);
        pstmt.setString(2, sanctionType);
        pstmt.executeUpdate();
    }

    public int getUserTotalSanctions(long userId) {
        int total = 0;
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT totalSanctions FROM users WHERE id = ?")) {
            pstmt.setLong(1, userId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) total = rs.getInt("totalSanctions");
            else addNewUser(userId);
        } catch (SQLException e) {
            Vars.logger.error("Could not get user total sanctions.", e);
        }

        return total;
    }

    private void updateUserTotalSanctions(long userId) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement("UPDATE users SET totalSanctions = ? WHERE id = ?");
        pstmt.setInt(1, getUserTotalSanctions(userId) + 1);
        pstmt.setLong(2, userId);
        pstmt.executeUpdate();
    }
}
