package dev.jojofr.joseta.database.helper;

import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.entities.Guild;
import dev.jojofr.joseta.database.entities.Sanction;
import dev.jojofr.joseta.database.entities.Sanction_;
import net.dv8tion.jda.api.entities.Member;

public class SanctionDatabase {

    public static void addSanction(Sanction.SanctionType sanctionType, Member member, long moderatorId, long guildId, String reason, long expiryTime) {
        Guild guild = Database.get(Guild.class, guildId);
        Database.create(
            new Sanction(
                guildId,
                guild.lastSanctionId + 1,
                sanctionType,
                member.getIdLong(),
                moderatorId,
                reason,
                expiryTime
            )
        );
        
        Database.update(guild.incrementLastSanctionId());
        UserDatabase.incrementSanctionCount(member, guildId);
    }
    
    public static Sanction getLatest(long userId, long guildId, Sanction.SanctionType sanctionType) {
        Sanction sanction = Database.querySelect(Sanction.class,
            (cb, rt) ->
                cb.and(cb.equal(rt.get(Sanction_.id).get(Sanction_.SanctionId_.guildId), guildId),
                       cb.equal(rt.get(Sanction_.sanctionType), sanctionType.code),
                       cb.equal(rt.get(Sanction_.userId), userId)),
            (cb, rt) -> cb.desc(rt.get(Sanction_.id).get(Sanction_.SanctionId_.sanctionNumber))
        ).setMaxResults(1).getSingleResult();
        
        return sanction;
    }
}
