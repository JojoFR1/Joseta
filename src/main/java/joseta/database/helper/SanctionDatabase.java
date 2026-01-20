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
