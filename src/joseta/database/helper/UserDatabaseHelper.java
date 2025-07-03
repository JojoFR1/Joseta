package joseta.database.helper;

import joseta.database.*;
import joseta.database.entry.*;

import net.dv8tion.jda.api.entities.*;

public class UserDatabaseHelper {

    public static UserEntry addUser(Member user) {
        Databases databases = Databases.getInstance();
        UserEntry entry = new UserEntry(user);
        return databases.createOrUpdate(entry);
    }
    
    public static int getUserSanctionCount(Member member, long guildId) {
        return getOrCreateUserEntry(member, guildId).getSanctionCount();
    }

    public static void updateUserSanctionCount(Member member, long guildId) {
        Databases databases = Databases.getInstance();
        UserEntry entry = getOrCreateUserEntry(member, guildId);
        databases.createOrUpdate(entry.incrementSanctionCount());
    }

    public static String getComposedId(long userId, long guildId) { return userId + "-" + guildId; }

    private static UserEntry getOrCreateUserEntry(Member member, long guildId) {
        Databases databases = Databases.getInstance();
        UserEntry entry = databases.get(UserEntry.class, getComposedId(member.getIdLong(), guildId));
        return entry == null ? addUser(member) : entry;
    }
}
