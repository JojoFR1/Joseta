package dev.jojofr.joseta.database.helper;

import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.daos.GuildDao;
import dev.jojofr.joseta.database.daos.SanctionDao;
import dev.jojofr.joseta.database.daos.UserDao;
import dev.jojofr.joseta.database.entities.SanctionEntity;
import dev.jojofr.joseta.database.entities.UserEntity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class SanctionDatabase {

    public static void addSanction(SanctionEntity.SanctionType sanctionType, Member member, long moderatorId, String reason, long expiryTime) { addSanction(sanctionType, member.getUser(), moderatorId, member.getGuild().getIdLong(), reason, expiryTime); }
    public static void addSanction(SanctionEntity.SanctionType sanctionType, User user, long moderatorId, long guildId, String reason, long expiryTime) {
        Database.useHandle(handle -> {
            int sanctionNumber = handle.attach(GuildDao.class).nextSanctionNumber(guildId);
            
            // need to create the user if it doesn't exist yet
            UserDao userDao = handle.attach(UserDao.class);
            if (userDao.getById(user.getIdLong(), guildId) == null) userDao.upsert(new UserEntity(user, guildId));
            
            handle.attach(SanctionDao.class).upsert(
                new SanctionEntity(
                    guildId,
                    sanctionNumber,
                    sanctionType,
                    user.getIdLong(),
                    moderatorId,
                    reason,
                    expiryTime
                )
            );
            
            userDao.incrementSanctionCount(user.getIdLong(), guildId);
        });
    }
}
