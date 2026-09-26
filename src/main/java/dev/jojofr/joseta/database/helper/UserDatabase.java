package dev.jojofr.joseta.database.helper;

import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.daos.UserDao;
import dev.jojofr.joseta.database.entities.UserEntity;
import net.dv8tion.jda.api.entities.Member;

public class UserDatabase {
    
    public static void addTimeVoice(Member member, long timeSpent) {
        Database.useExtensionAsync(UserDao.class, dao -> dao.addTimeVoice((new UserEntity(member).setTimeVoice(timeSpent))));
    }
    public static void incrementCountingSuccess(Member member) {
        Database.useExtensionAsync(UserDao.class, dao -> dao.incrementCountingSuccess(new UserEntity(member)));
    }
    public static void incrementCountingFail(Member member) {
        Database.useExtensionAsync(UserDao.class, dao -> dao.incrementCountingFail(new UserEntity(member)));
    }
    public static void incrementCountingSpecialSuccess(Member member) {
        Database.useExtensionAsync(UserDao.class, dao -> dao.incrementCountingSpecialSuccess(new UserEntity(member)));
    }
    public static void incrementCountingSpecialFail(Member member) {
        Database.useExtensionAsync(UserDao.class, dao -> dao.incrementCountingSpecialFail(new UserEntity(member)));
    }
}
