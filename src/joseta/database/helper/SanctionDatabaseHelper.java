package joseta.database.helper;

import joseta.*;
import joseta.database.*;
import joseta.database.entry.*;

import arc.struct.*;

import net.dv8tion.jda.api.entities.*;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;

public class SanctionDatabaseHelper {
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    public static void startScheduler(int minutes) { scheduler.scheduleAtFixedRate(SanctionDatabaseHelper::checkExpiredSanctions, 0, minutes, TimeUnit.MINUTES); }

    public static void addSanction(char sanctionType, Member member, long moderatorId, long guildId, String reason, long time) {
        GuildEntry entry = Database.get(GuildEntry.class, guildId);
        Database.create(
            new SanctionEntry(
                entry.getLastSanctionId() + 1,
                sanctionType,
                member.getIdLong(),
                moderatorId,
                guildId,
                reason,
                Instant.now(),
                time
            )
        );

        Database.createOrUpdate(entry.incrementLastSanctionId());
        UserDatabaseHelper.updateUserSanctionCount(member, guildId);
    }

    public static SanctionEntry getLatestSanction(long userId, long guildId, String sanctionType) {
        SanctionEntry entry = Database.querySelect(SanctionEntry.class,
                (cb, rt) ->
                        cb.and(cb.equal(rt.get(SanctionEntry_.userId), userId),
                                cb.equal(rt.get(SanctionEntry_.guildId), guildId),
                                cb.like(rt.get(SanctionEntry_.sanctionId), sanctionType)),
                (cb, rt) -> cb.desc(rt.get(SanctionEntry_.sanctionId))
        ).setMaxResults(1).getSingleResult();

        return entry;
    }

    public static Seq<SanctionEntry> getExpiredSanctions() {
        List<SanctionEntry> entries = Database.querySelect(SanctionEntry.class, (cb, rt) ->
                cb.and(cb.ge(rt.get(SanctionEntry_.expiryTime), 1L),
                        cb.equal(rt.get(SanctionEntry_.isExpired), false))
        ).getResultList();

        return Seq.with(entries);
    }

    private static void checkExpiredSanctions() {
        SanctionDatabaseHelper.getExpiredSanctions().forEach(sanction -> {
            if (sanction.getSanctionTypeId() == 'B') {
                Guild guild = JosetaBot.getBot().getGuildById(sanction.getGuildId());
                guild.retrieveBanList().queue(bans -> {
                    bans.forEach(ban -> {
                        if (ban.getUser().getIdLong() == sanction.getUserId())
                            guild.unban(ban.getUser()).queue();
                    });
                });
            }
            Database.createOrUpdate(sanction.setExpired(true));
        });
    }
}
