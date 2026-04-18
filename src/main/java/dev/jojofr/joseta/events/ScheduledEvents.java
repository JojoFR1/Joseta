package dev.jojofr.joseta.events;

import dev.jojofr.joseta.JosetaBot;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.entities.ReminderEntity;
import dev.jojofr.joseta.database.entities.ReminderEntity_;
import dev.jojofr.joseta.database.entities.SanctionEntity;
import dev.jojofr.joseta.database.entities.SanctionEntity_;
import dev.jojofr.joseta.utils.Log;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.time.Instant;
import java.util.Collections;
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
        List<ReminderEntity> reminders = Database.querySelect(ReminderEntity.class, (cb, rt) ->
            cb.lessThanOrEqualTo(rt.get(ReminderEntity_.remindAt), Instant.now())).getResultList();
        if (reminders.isEmpty()) return;
        
        for (ReminderEntity reminder : reminders) {
            MessageChannel channel = (MessageChannel) JosetaBot.get().getGuildChannelById(reminder.channelId);
            boolean shouldDm = reminder.dm || channel == null;
            
            String message = REMINDER_PREMESSAGE.replace("%userid%", String.valueOf(reminder.userId))
                                                .replace("%message%", reminder.message);
            if (!shouldDm) {
                channel.sendMessage(message).setAllowedMentions(Collections.singleton(Message.MentionType.USER)).queue(
                    success -> {
                        if (!reminder.repeat) {
                            Database.delete(reminder);
                            return;
                        }
                        
                        reminder.remindAt = Instant.now().plusSeconds(reminder.remindAfter);
                        Database.update(reminder);
                    },
                    failure -> Log.err("Failed to send reminder message for reminder ID {}, in channel ID {}", failure, reminder.id, reminder.channelId)
                );
                continue;
            }
            
            JosetaBot.get().retrieveUserById(reminder.userId).queue(
                user -> user.openPrivateChannel().queue(
                    privateChannel -> {
                        if (!privateChannel.canTalk()) {
                            Log.warn("Cannot send reminder DM to user {} (ID: {}) for reminder ID {} because the bot cannot talk in the private channel", user.getAsTag(), user.getIdLong(), reminder.id);
                            
                            reminder.remindAt = Instant.now().plusSeconds(reminder.remindAfter + 60 * 60 * 6); // 6 hours
                            Database.update(reminder);
                            
                            if (channel == null) return;
                            channel.sendMessage("⚠️ "+ user.getAsMention() +", je n'ai pas pu t'envoyer un message privé pour ton rappel. Je réessayerai plus tard, vérifie que je peux t'envoyer des messages privés.").queue();
                            return;
                        }
                        
                        privateChannel.sendMessage(message).queue(
                            success -> {
                                if (!reminder.repeat) {
                                    Database.delete(reminder);
                                    return;
                                }
                                
                                reminder.remindAt = Instant.now().plusSeconds(reminder.remindAfter);
                                Database.update(reminder);
                            }
                        );
                    }),
                failure -> {
                    Log.warn("Failed to retrieve user {} (ID: {}) for reminder ID {}. The reminder will be deleted.", failure, reminder.userId, reminder.id);
                    Database.delete(reminder);
                });
        }
    }
    
    private static void checkExpiredSanctions() {
        List<SanctionEntity> sanctions = Database.querySelect(SanctionEntity.class, (cb, rt) -> cb.and(
            cb.equal(rt.get(SanctionEntity_.permanent), false),
            cb.equal(rt.get(SanctionEntity_.isExpired), false),
            cb.lessThanOrEqualTo(rt.get(SanctionEntity_.expiryTime), Instant.now())
        )).getResultList();
        if (sanctions.isEmpty()) return;
        
        for (SanctionEntity sanction : sanctions) {
            Guild guild = JosetaBot.get().getGuildById(sanction.id.guildId());
            Database.update(sanction.setExpired(true));
            
            if (guild == null) continue;
            
            // Only ban need action on expiry, others are automatic
            JosetaBot.get().retrieveUserById(sanction.userId).queue(
                user -> {
                    if (sanction.sanctionType == SanctionEntity.SanctionType.BAN)
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
        }
    }
}
