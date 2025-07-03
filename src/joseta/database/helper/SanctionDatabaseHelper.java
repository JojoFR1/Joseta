package joseta.database.helper;

import joseta.*;
import joseta.database.*;
import joseta.database.entry.*;

import arc.struct.*;

import net.dv8tion.jda.api.entities.*;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;

import org.hibernate.query.criteria.*;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;

public class SanctionDatabaseHelper {
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    public static void startScheduler(int minutes) { scheduler.scheduleAtFixedRate(SanctionDatabaseHelper::checkExpiredSanctions, 0, minutes, TimeUnit.MINUTES); }

    public static void addSanction(char sanctionType, Member member, long moderatorId, long guildId, String reason, long time) {
        Databases databases = Databases.getInstance();
        GuildEntry entry = databases.get(GuildEntry.class, guildId);
        databases.create(
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

        databases.createOrUpdate(entry.incrementLastSanctionId());
        UserDatabaseHelper.updateUserSanctionCount(member, guildId);
    }

    public static SanctionEntry getLatestSanction(long userId, long guildId, char sanctionType) {
        HibernateCriteriaBuilder criteriaBuilder = Databases.getInstance().getCriteriaBuilder();
        CriteriaQuery<SanctionEntry> query = criteriaBuilder.createQuery(SanctionEntry.class);
        Root<SanctionEntry> root = query.from(SanctionEntry.class);
        Predicate where = criteriaBuilder.conjunction();
        where = criteriaBuilder.and(where, criteriaBuilder.equal(root.get(SanctionEntry_.userId), userId));
        where = criteriaBuilder.and(where, criteriaBuilder.equal(root.get(SanctionEntry_.guildId), guildId));
        where = criteriaBuilder.and(where, criteriaBuilder.equal(root.get(SanctionEntry_.sanctionId), sanctionType + "%"));
        query.select(root).where(where).orderBy(criteriaBuilder.desc(root.get(SanctionEntry_.sanctionId)));

        TypedQuery<SanctionEntry> typedQuery = Databases.getInstance().getSession().createQuery(query);
        typedQuery.setMaxResults(1);

        SanctionEntry entry = typedQuery.getSingleResult();

        return entry;
    }

    public static Seq<SanctionEntry> getExpiredSanctions() {
        HibernateCriteriaBuilder criteriaBuilder = Databases.getInstance().getCriteriaBuilder();
        CriteriaQuery<SanctionEntry> query = criteriaBuilder.createQuery(SanctionEntry.class);
        Root<SanctionEntry> root = query.from(SanctionEntry.class);
        Predicate where = criteriaBuilder.conjunction();
        where = criteriaBuilder.and(where, criteriaBuilder.ge(root.get(SanctionEntry_.expiryTime), 1L));
        where = criteriaBuilder.and(where, criteriaBuilder.equal(root.get(SanctionEntry_.isExpired), false));
        query.select(root).where(where);

        List<SanctionEntry> entries = Databases.getInstance().getSession()
            .createSelectionQuery(query)
            .getResultList();
        
        return Seq.with(entries);
    }

    private static void checkExpiredSanctions() {
        Databases databases = Databases.getInstance();
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
            databases.createOrUpdate(sanction.setExpired(true));
        });
    }
}
