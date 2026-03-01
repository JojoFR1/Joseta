package dev.jojofr.joseta.database.helper;

import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.entities.UserEntity;
import dev.jojofr.joseta.database.entities.UserEntity_;
import net.dv8tion.jda.api.entities.Member;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class UserDatabase {

    public static UserEntity addUser(Member member) {
        UserEntity user = new UserEntity(member);
        return Database.createOrUpdate(user);
    }
    
    public static void incrementSanctionCount(Member member, long guildId) {
        UserEntity user = getOrCreate(member, guildId);
        Database.createOrUpdate(user.incrementSanctionCount());
    }
    
    public static UserEntity getOrCreate(Member member, long guildId) {
        UserEntity user = Database.get(UserEntity.class, new UserEntity.UserId(member.getIdLong(), guildId));
        return user != null ? user : addUser(member);
    }
    
    public static void deleteGuildUsers(long guildId) {
        try (Session session = Database.getSession()) {
            Transaction tx = session.beginTransaction();
            Database.queryDelete(UserEntity.class, (cb, rt) -> cb.equal(rt.get(UserEntity_.id).get(UserEntity_.UserId_.guildId), guildId), session);
            tx.commit();
        }
    }
}
