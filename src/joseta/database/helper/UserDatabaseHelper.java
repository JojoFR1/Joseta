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
    
    public static int getUserSanctionCount(long userId, long guildId) {
        try {
            Databases databases = Databases.getInstance();
            UserEntry entry = databases.getUserDao().queryBuilder()
                .where()
                .eq("id", userId)
                .and()
                .eq("guildId", guildId)
                .queryForFirst();

            return entry.getSanctionCount();
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not get user total sanctions.", e);
            return -1;
        }
    }

    public static void updateUserSanctionCount(long userId, long guildId) {
        try {
            Databases databases = Databases.getInstance();
            databases.getUserDao().update(
                databases.getUserDao().queryBuilder()
                    .where()
                    .eq("id", userId)
                    .and()
                    .eq("guildId", guildId)
                    .queryForFirst()
                .incrementSanctionCount()
            );
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not update user sanction count.", e);
        }
    }
}
