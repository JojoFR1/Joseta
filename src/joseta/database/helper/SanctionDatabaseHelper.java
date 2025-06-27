package joseta.database.helper;

import joseta.*;
import joseta.database.*;
import joseta.database.entry.*;

import arc.struct.*;
import arc.util.*;

import net.dv8tion.jda.api.entities.*;

import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

public class SanctionDatabaseHelper {
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    public static void startScheduler(int minutes) { scheduler.scheduleAtFixedRate(SanctionDatabaseHelper::checkExpiredSanctions, 0, minutes, TimeUnit.MINUTES); }

    public static void addSanction(char sanctionType, Member member, long moderatorId, long guildId, String reason, long time) {
        try {
            Databases databases = Databases.getInstance();
            GuildEntry entry = databases.getGuildDao().queryForId(guildId);
            databases.getSanctionDao().create(
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

            databases.getGuildDao().update(entry.incrementLastSanctionId());
            UserDatabaseHelper.updateUserSanctionCount(member, guildId);
        } catch (SQLException e) {
            Log.err("Could not add sanction.", e);
        }
    }

    public static SanctionEntry getLatestSanction(long userId, long guildId, char sanctionType) {
        try {
            SanctionEntry entry = Databases.getInstance().getSanctionDao()
                .queryBuilder()
                .orderBy("id", false)
                .limit(1L)
                .where()
                .eq("userId", userId)
                .and()
                .eq("guildId", guildId)
                .and()
                .like("sanction", sanctionType + "%")
                .queryForFirst();

            return entry;
        } catch (SQLException e) {
            Log.err("Could not get latest sanction.", e);
            return null;
        }
    }

    public static Seq<SanctionEntry> getExpiredSanctions() {
        List<SanctionEntry> entries;
        try {
            entries = Databases.getInstance().getSanctionDao().queryBuilder()
                .where()
                .ge("for", 1)
                .and()
                .eq("expired", false)
                .query();
        } catch (SQLException e) {
            Log.err("Could not get expired sanctions.", e);
            return null;
        }

        return Seq.with(entries);
    }

    private static void checkExpiredSanctions() {
        try {
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
                try {
                    databases.getSanctionDao().update(sanction.setExpired(true));
                } catch (SQLException e) {
                    Log.err("Error while updating expired sanction.", e);
                }
            });
        } catch (SQLException e) {
            Log.err("Error while checking expired sanctions.", e);
        }
    }
}
