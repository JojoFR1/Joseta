package joseta.database.helper;

import joseta.database.*;
import joseta.database.entry.*;

import net.dv8tion.jda.api.entities.*;

public class UserDatabaseHelper {

    public static UserEntry addUser(Member user) {
        UserEntry entry = new UserEntry(user);
        return Databases.createOrUpdate(entry);
    }
    
    public static int getUserSanctionCount(Member member, long guildId) {
        return getOrCreateUserEntry(member, guildId).getSanctionCount();
    }

    public static void updateUserSanctionCount(Member member, long guildId) {
        UserEntry entry = getOrCreateUserEntry(member, guildId);
        Databases.createOrUpdate(entry.incrementSanctionCount());
    }

    private static UserEntry getOrCreateUserEntry(Member member, long guildId) {
        UserEntry entry = Databases.get(UserEntry.class, UserEntry.getUserId(member.getIdLong(), guildId));
        return entry == null ? addUser(member) : entry;
    }
}
