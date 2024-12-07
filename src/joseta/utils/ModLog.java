package joseta.utils;

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
        stmt.execute(sanctionsTable);

        for (String sanctionType : sanctionTypes) {
            String insertQuery = "INSERT INTO lastValues (name, value) VALUES (?, -1)";
            PreparedStatement pstmt = conn.prepareStatement(insertQuery);
            pstmt.setString(1, sanctionType);
            pstmt.executeUpdate();
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
        } catch (SQLException e) {
            e.printStackTrace();
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
}
