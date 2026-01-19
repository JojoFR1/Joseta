package joseta.database.helper;

import joseta.database.Database;
import joseta.database.entities.User;
import joseta.database.entities.User_;
import net.dv8tion.jda.api.entities.Member;
import org.hibernate.Session;
import org.hibernate.Transaction;

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
    
    public static void deleteGuildUsers(long guildId) {
        try (Session session = Database.getSession()) {
            Transaction tx = session.beginTransaction();
            Database.queryDelete(User.class, (cb, rt) -> cb.equal(rt.get(User_.id).get(User_.UserId_.guildId), guildId), session);
            tx.commit();
        }
    }
}
