package joseta.database.helper;

import arc.util.*;
import joseta.*;
import joseta.database.*;
import joseta.database.entry.*;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;

public class ReminderDatabaseHelper {
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    public static void startScheduler(int minutes) { scheduler.scheduleAtFixedRate(ReminderDatabaseHelper::checkReminders, 0, minutes, TimeUnit.MINUTES); }

    public static List<ReminderEntry> getReminderEntries(long guildId, long userId) {
        List<ReminderEntry> reminders = Database.querySelect(ReminderEntry.class, (cb, rt) ->
            cb.and(cb.equal(rt.get(ReminderEntry_.guildId), guildId),
                    cb.equal(rt.get(ReminderEntry_.userId), userId))
        ).getResultList();

        return reminders;
    }

    private static void checkReminders() {
        List<ReminderEntry> entries = Database.getAll(ReminderEntry.class);

        for (ReminderEntry entry : entries) {
            if (entry.getTime().isBefore(Instant.now())) {
                JosetaBot.getBot().getTextChannelById(entry.getChannelId())
                    .sendMessage("Rappel pour <@" + entry.getUserId() + "> :\n\n" + entry.getMessage()).queue(
                        success -> Database.delete(entry)
                    );
            }
        }
    }
}
