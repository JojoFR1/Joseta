package dev.jojofr.joseta.database.helper;

import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.daos.UserDao;
import dev.jojofr.joseta.database.entities.UserEntity;
import net.dv8tion.jda.api.entities.Member;

public class UserDatabase {
    
    public static void addTimeVoice(Member member, long guildId, long timeSpent) {
        Database.useExtensionAsync(UserDao.class, dao -> {
            if (dao.addTimeVoice(member.getIdLong(), guildId, timeSpent) == 0)
                dao.upsert(new UserEntity(member).setTimeVoice(timeSpent));
        });
    }
    
    public static void incrementCountingSuccess(Member member, long guildId) {
        Database.useExtensionAsync(UserDao.class, dao -> {
            if (dao.incrementCountingSuccess(member.getIdLong(), guildId) == 0)
                dao.upsert(new UserEntity(member).setCountingSuccess(1));
        });
    }
    
    public static void incrementCountingFail(Member member, long guildId) {
        Database.useExtensionAsync(UserDao.class, dao -> {
            if (dao.incrementCountingFail(member.getIdLong(), guildId) == 0)
                dao.upsert(new UserEntity(member).setCountingFail(1));
        });
    }
    
    public static void incrementCountingSpecialSuccess(Member member, long guildId) {
        Database.useExtensionAsync(UserDao.class, dao -> {
            if (dao.incrementCountingSpecialSuccess(member.getIdLong(), guildId) == 0)
                dao.upsert(new UserEntity(member).setCountingSpecialSuccess(1));
        });
    }
    
    public static void incrementCountingSpecialFail(Member member, long guildId) {
        Database.useExtensionAsync(UserDao.class, dao -> {
            if (dao.incrementCountingSpecialFail(member.getIdLong(), guildId) == 0)
                dao.upsert(new UserEntity(member).setCountingSpecialFail(1));
        });
    }
}
