package joseta.database.helper;

import joseta.*;
import joseta.database.*;
import joseta.database.entry.*;

import net.dv8tion.jda.api.entities.*;

import java.sql.*;

public class UserDatabaseHelper {

    public static void addUser(Member user) {
        try {
            Databases databases = Databases.getInstance();
            UserEntry entry = new UserEntry(user);
            databases.getUserDao().createOrUpdate(entry);
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not add user to database.", e);
        }
    }
    
    public static int getUserSanctionCount(Member member, long guildId) {
        try {
            Databases databases = Databases.getInstance();
            UserEntry entry = databases.getUserDao().queryForId(getComposedId(member.getIdLong(), guildId));
            if (entry == null) {
                entry = new UserEntry(member);
                databases.getUserDao().create(entry);
            }

            return entry.getSanctionCount();
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not get user total sanctions.", e);
            return -1;
        }
    }

    public static void updateUserSanctionCount(Member member, long guildId) {
        try {
            Databases databases = Databases.getInstance();

            if (databases.getUserDao().queryForId(getComposedId(member.getIdLong(), guildId)) == null)
                UserDatabaseHelper.addUser(member);
            
            databases.getUserDao().update(
                databases.getUserDao().queryForId(getComposedId(member.getIdLong(), guildId)).incrementSanctionCount()
            );
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not update user sanction count.", e);
        }
    }

    public static String getComposedId(long userId, long guildId) { return userId + "-" + guildId; }
}
