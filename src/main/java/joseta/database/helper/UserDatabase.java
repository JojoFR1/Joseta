package joseta.database.helper;

import joseta.database.Database;
import joseta.database.entities.User;
import net.dv8tion.jda.api.entities.Member;

public class UserDatabase {

    public static User addUser(Member member) {
        User user = new User(member);
        return Database.createOrUpdate(user);
    }
    
    public static void incrementSanctionCount(Member member, long guildId) {
        User user = getOrCreate(member, guildId);
        Database.createOrUpdate(user.incrementSanctionCount());
    }
    
    public static User getOrCreate(Member member, long guildId) {
        User user = Database.get(User.class, new User.UserId(member.getIdLong(), guildId));
        return user != null ? user : addUser(member);
    }
}
