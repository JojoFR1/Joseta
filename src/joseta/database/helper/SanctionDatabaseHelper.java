package joseta.database.helper;

import joseta.*;
import joseta.database.*;
import joseta.database.entry.*;

import arc.struct.*;

import net.dv8tion.jda.api.entities.*;
import org.hibernate.*;

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
                guildId,
                member.getIdLong(),
                moderatorId,
                reason,
                time
            )
        );

        Database.createOrUpdate(entry.incrementLastSanctionId());
        UserDatabaseHelper.updateUserSanctionCount(member, guildId);
    }

    public static SanctionEntry getLatestSanction(long userId, long guildId, String sanctionType) {
        SanctionEntry entry = Database.querySelect(SanctionEntry.class,
                (cb, rt) ->
                        cb.and(cb.equal(rt.get(SanctionEntry_.guildId), guildId),
                                cb.equal(rt.get(SanctionEntry_.userId), userId),
                                cb.like(rt.get(SanctionEntry_.sanctionId), sanctionType)),
                (cb, rt) -> cb.desc(rt.get(SanctionEntry_.sanctionId))
        ).setMaxResults(1).getSingleResult();

        return entry;
    }

    public static Seq<SanctionEntry> getExpiredSanctions() {
        List<SanctionEntry> entries = Database.querySelect(SanctionEntry.class, (cb, rt) ->
                cb.and(cb.isNotNull(rt.get(SanctionEntry_.expiryTime)),
                        cb.equal(rt.get(SanctionEntry_.isExpired), false))
        ).getResultList();

        return Seq.with(entries).retainAll(
            entry -> entry.getExpiryTime().isBefore(Instant.now())
        );
    }

    private static void checkExpiredSanctions() {
        SanctionDatabaseHelper.getExpiredSanctions().forEach(entry -> {
            Guild guild = JosetaBot.getBot().getGuildById(entry.getGuildId());

            if (entry.getSanctionTypeId() == 'B') {
                guild.retrieveBanList().queue(bans -> {
                    bans.forEach(ban -> {
                        if (ban.getUser().getIdLong() == entry.getUserId())
                            guild.unban(ban.getUser()).queue();
                    });
                });
            }

            try (Session session = Database.getSession()) {
                Transaction transaction = session.beginTransaction();
                Database.queryUpdate(SanctionEntry.class, (cb, rt) ->
                    cb.and(cb.equal(rt.get(SanctionEntry_.sanctionId), entry.getSanctionIdFull()),
                            cb.equal(rt.get(SanctionEntry_.guildId), entry.getGuildId())),
                    (cb, rt) -> cb.set(rt.get(SanctionEntry_.isExpired), true),
                    session
                ).executeUpdate();
                transaction.commit();
            }

            JosetaBot.getBot().retrieveUserById(entry.getUserId()).queue((user) ->
               user.openPrivateChannel().queue((channel) ->
                   channel.sendMessage("Votre __avertissement__ sur le serveur **`"+ guild.getName() +"`**  d'identifiant **`"+ entry.getSanctionIdFull() +"`** par <@"+ entry.getModeratorId() +"> du <t:"+ entry.getTimestamp().getEpochSecond() +":F> viens d'expirer.\n\n-# ***Ceci est un message automatique. Toutes contestations doivent se faire avec le modérateur responsable***").queue()
               )
            );
        });
    }
}
