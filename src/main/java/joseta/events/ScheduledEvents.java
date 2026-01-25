package joseta.events;

import joseta.JosetaBot;
import joseta.database.Database;
import joseta.database.entities.Reminder;
import joseta.database.entities.Reminder_;
import joseta.database.entities.Sanction;
import joseta.database.entities.Sanction_;
import joseta.utils.Log;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledEvents {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    public static void schedule() {
        // Check reminders every minute
        scheduler.scheduleAtFixedRate(ScheduledEvents::checkReminders, 0, 1, TimeUnit.MINUTES);
        // Check expired sanctions every 15 minutes
        scheduler.scheduleAtFixedRate(ScheduledEvents::checkExpiredSanctions, 0, 15, TimeUnit.MINUTES);
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
    
    private static void checkExpiredSanctions() {
        List<Sanction> sanctions = Database.querySelect(Sanction.class, (cb, rt) -> cb.and(
            cb.equal(rt.get(Sanction_.isExpired), false),
            cb.lessThanOrEqualTo(rt.get(Sanction_.expiryTime), Instant.now())
        )).getResultList();
        if (sanctions.isEmpty()) return;
        
        for (Sanction sanction : sanctions) {
            Guild guild = JosetaBot.get().getGuildById(sanction.id.guildId());
            if (guild == null) continue;
            
            // Only ban need action on expiry, others are automatic
            JosetaBot.get().retrieveUserById(sanction.userId).queue(
                user -> {
                    if (sanction.sanctionType == Sanction.SanctionType.BAN)
                        guild.unban(user).queue(
                            null,
                            failure -> Log.warn("Failed to unban user {} (ID: {}) on sanction expiry ID {}", failure, user.getAsTag(), user.getIdLong(), sanction.id)
                        );
                    
                    user.openPrivateChannel().queue(
                        channel ->
                            channel.sendMessage("Votre sanction sur le serveur **`"+ guild.getName() +"`** d'identifiant **`"+ sanction.getSanctionId() +"`** du <t:"+ sanction.timestamp.getEpochSecond() +":F> a expiré.\n\n-# ***Ceci est un message automatique. Toutes contestations doivent se faire avec le modérateur responsable***").queue(
                                null,
                                failure -> Log.err("Failed to send private message to user {} (ID: {}) for expired sanction ID {}", failure, user.getAsTag(), user.getIdLong(), sanction.id)
                            ),
                        failure -> Log.err("Failed to open private channel for user {} (ID: {}) for expired sanction ID {}", failure, user.getAsTag(), user.getIdLong(), sanction.id)
                    );
                },
                failure -> Log.err("Failed to retrieve user {} (ID: {}) for expired sanction ID {}", failure, sanction.userId, sanction.id)
            );
            
            Database.update(sanction.setExpired(true));
        }
    }
}
