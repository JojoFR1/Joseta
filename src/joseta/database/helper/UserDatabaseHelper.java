package joseta.database.helper;

import joseta.database.*;
import joseta.database.entry.*;

import net.dv8tion.jda.api.entities.*;

public class UserDatabaseHelper {

    public static void addUser(Member user) {
        Databases databases = Databases.getInstance();
        UserEntry entry = new UserEntry(user);
        databases.createOrUpdate(entry);
    }
    
    public static int getUserSanctionCount(Member member, long guildId) {
        Databases databases = Databases.getInstance();
        UserEntry entry = databases.get(UserEntry.class, getComposedId(member.getIdLong(), guildId));
        if (entry == null) {
            entry = new UserEntry(member);
            databases.create(entry);
        }

        return entry.getSanctionCount();
    }

    public static void updateUserSanctionCount(Member member, long guildId) {
        Databases databases = Databases.getInstance();

        if (databases.get(UserEntry.class, getComposedId(member.getIdLong(), guildId)) == null)
            UserDatabaseHelper.addUser(member);
        
        databases.createOrUpdate(
            databases.get(UserEntry.class, getComposedId(member.getIdLong(), guildId)).incrementSanctionCount()
        );
    }

    public static String getComposedId(long userId, long guildId) { return userId + "-" + guildId; }
}
