package dev.jojofr.joseta.database.helper;

import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.entities.GuildEntity;
import dev.jojofr.joseta.database.entities.SanctionEntity;
import dev.jojofr.joseta.database.entities.SanctionEntity_;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class SanctionDatabase {

    public static void addSanction(SanctionEntity.SanctionType sanctionType, Member member, long moderatorId, String reason, long expiryTime) { addSanction(sanctionType, member.getUser(), moderatorId, member.getGuild().getIdLong(), reason, expiryTime); }
    public static void addSanction(SanctionEntity.SanctionType sanctionType, User user, long moderatorId, long guildId, String reason, long expiryTime) {
        GuildEntity guild = Database.get(GuildEntity.class, guildId);
        Database.create(
            new SanctionEntity(
                guildId,
                guild.lastSanctionId + 1,
                sanctionType,
                user.getIdLong(),
                moderatorId,
                reason,
                expiryTime
            )
        );
        
        Database.update(guild.incrementLastSanctionId());
        UserDatabase.incrementSanctionCount(user, guildId);
    }
    
    public static SanctionEntity getLatest(long userId, long guildId, SanctionEntity.SanctionType sanctionType) {
        SanctionEntity sanction = Database.querySelect(SanctionEntity.class,
            (cb, rt) ->
                cb.and(cb.equal(rt.get(SanctionEntity_.id).get(SanctionEntity_.SanctionId_.guildId), guildId),
                       cb.equal(rt.get(SanctionEntity_.sanctionType), sanctionType.code),
                       cb.equal(rt.get(SanctionEntity_.userId), userId)),
            (cb, rt) -> cb.desc(rt.get(SanctionEntity_.id).get(SanctionEntity_.SanctionId_.sanctionNumber))
        ).setMaxResults(1).getSingleResult();
        
        return sanction;
    }
}
