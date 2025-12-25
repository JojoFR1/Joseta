package joseta.database.helper;

import joseta.database.Database;
import joseta.database.entities.Guild;
import joseta.database.entities.Sanction;
import joseta.database.entities.Sanction_;
import net.dv8tion.jda.api.entities.Member;

import java.time.Instant;
import java.util.List;

public class SanctionDatabase {

    public static void addSanction(Sanction.SanctionType sanctionType, Member member, long moderatorId, long guildId, String reason, long expiryTime) {
        Guild guild = Database.get(Guild.class, guildId);
        Database.create(
            new Sanction(
                guildId,
                sanctionType,
                guild.lastSanctionId + 1,
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
                cb.and(cb.equal(rt.get(Sanction_.guildId), guildId),
                       cb.equal(rt.get(Sanction_.userId), userId),
                       cb.like(rt.get(Sanction_.sanctionId), String.valueOf(sanctionType.code))),
            (cb, rt) -> cb.desc(rt.get(Sanction_.sanctionId))
        ).setMaxResults(1).getSingleResult();
        
        return sanction;
    }
    
    public static List<Sanction> getExpired() {
        List<Sanction> sanctions = Database.querySelect(Sanction.class, (cb, rt) ->
            cb.and(cb.isNotNull(rt.get(Sanction_.expiryTime)),
                   cb.equal(rt.get(Sanction_.isExpired), false))
        ).getResultList();
        
        sanctions.removeIf(sanction -> !sanction.expiryTime.isBefore(Instant.now()));
        
        return sanctions;
    }
}
