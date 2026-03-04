package dev.jojofr.joseta.database.helper;

import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.entities.UserEntity;
import dev.jojofr.joseta.database.entities.UserEntity_;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class UserDatabase {

    public static UserEntity addUser(Member member) { return addUser(member.getUser(), member.getGuild().getIdLong()); }
    public static UserEntity addUser(User user, long guildId) {
        UserEntity userEntity = new UserEntity(user, guildId);
        return Database.createOrUpdate(userEntity);
    }
    
    public static UserEntity getOrCreate(Member member) { return getOrCreate(member.getUser(), member.getGuild().getIdLong()); }
    public static UserEntity getOrCreate(User user, long guildId) {
        UserEntity userEntity = Database.get(UserEntity.class, new UserEntity.UserId(user.getIdLong(), guildId));
        return userEntity != null ? userEntity : addUser(user, guildId);
    }
    
    public static void incrementSanctionCount(Member member) { incrementSanctionCount(member.getUser(), member.getGuild().getIdLong()); }
    public static void incrementSanctionCount(User user, long guildId) {
        UserEntity userEntity = getOrCreate(user, guildId);
        Database.createOrUpdate(userEntity.incrementSanctionCount());
    }
    
    public static void deleteGuildUsers(long guildId) {
        try (Session session = Database.getSession()) {
            Transaction tx = session.beginTransaction();
            Database.queryDelete(UserEntity.class, (cb, rt) -> cb.equal(rt.get(UserEntity_.id).get(UserEntity_.UserId_.guildId), guildId), session);
            tx.commit();
        }
    }
}
