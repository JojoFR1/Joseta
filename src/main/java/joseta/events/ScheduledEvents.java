package joseta.events;

import joseta.JosetaBot;
import joseta.database.Database;
import joseta.database.entities.Reminder;
import joseta.database.entities.Reminder_;
import joseta.utils.Log;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledEvents {
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    
    static {
        scheduler.scheduleAtFixedRate(ScheduledEvents::checkReminders, 0, 1, TimeUnit.MINUTES);
    }
    
    
    public static final String REMINDER_PREMESSAGE = "⏰ Rappel pour <@%userid%>:\n ```%message%```";
    public static final int REMINDER_MAX_MESSAGE_LENGTH = Message.MAX_CONTENT_LENGTH - REMINDER_PREMESSAGE.replace("%message%", "").replace("%userid%", "").length();
    
    private static void checkReminders() {
        List<Reminder> reminders = Database.querySelect(Reminder.class, (cb, rt) -> cb.lessThanOrEqualTo(rt.get(Reminder_.remindAt), Instant.now())).getResultList();
        if (reminders.isEmpty()) return;
        
        for (Reminder reminder : reminders) {
            GuildChannel channel = JosetaBot.get().getGuildChannelById(reminder.channelId);
            if (channel instanceof GuildMessageChannel msgChannel) {
                msgChannel.sendMessage(REMINDER_PREMESSAGE
                        .replace("%userid%", String.valueOf(reminder.userId))
                        .replace("%message%", reminder.message)
                ).queue(
                    success -> Database.delete(reminder),
                    failure -> Log.err("Failed to send reminder message for reminder ID {}, in channel ID {}", failure, reminder.id, reminder.channelId)
                );
            }
        }
    }
}
