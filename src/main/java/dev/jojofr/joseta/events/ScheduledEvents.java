package dev.jojofr.joseta.events;

import dev.jojofr.joseta.JosetaBot;
import dev.jojofr.joseta.commands.ConfigurationCommand;
import dev.jojofr.joseta.commands.ModerationCommands;
import dev.jojofr.joseta.commands.ReminderCommand;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.daos.ReminderDao;
import dev.jojofr.joseta.database.daos.SanctionDao;
import dev.jojofr.joseta.database.entities.ReminderEntity;
import dev.jojofr.joseta.database.entities.SanctionEntity;
import dev.jojofr.joseta.utils.Log;
import dev.jojofr.joseta.utils.function.Function;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledEvents {
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    
    public static void schedule() {
        // Check reminders every minute
        scheduler.scheduleAtFixedRate(ScheduledEvents::checkReminders, 0, 1, TimeUnit.MINUTES);
        // Check expired sanctions every 15 minutes
        scheduler.scheduleAtFixedRate(ScheduledEvents::checkExpiredSanctions, 0, 15, TimeUnit.MINUTES);
        // Check expired "Message" entities every 30 minutes
        scheduler.scheduleAtFixedRate(ScheduledEvents::checkExpiredMessages, 30, 30, TimeUnit.MINUTES);
    }
    
    
    public static final String REMINDER_PREMESSAGE = "⏰ Rappel pour <@%userid%>:\n ```%message%```";
    public static final int REMINDER_MAX_MESSAGE_LENGTH = Message.MAX_CONTENT_LENGTH - REMINDER_PREMESSAGE.replace("%message%", "").replace("%userid%", "").length();
    
    private static void checkReminders() {
        List<ReminderEntity> reminders = Database.withExtension(ReminderDao.class, dao -> dao.getExpiredReminders());
        if (reminders.isEmpty()) return;
        
        for (ReminderEntity reminder : reminders) {
            MessageChannel channel = (MessageChannel) JosetaBot.get().getGuildChannelById(reminder.channelId);
            boolean shouldDm = reminder.dm || channel == null;
            
            String message = REMINDER_PREMESSAGE.replace("%userid%", String.valueOf(reminder.userId))
                                                .replace("%message%", reminder.text);
            if (!shouldDm) {
                channel.sendMessage(message).setAllowedMentions(Collections.singleton(Message.MentionType.USER)).queue(
                    success -> {
                        if (!reminder.repeat) {
                            Database.useExtension(ReminderDao.class, dao -> dao.delete(reminder.id));
                            return;
                        }
                        
                        reminder.remindAt = Instant.now().plusSeconds(reminder.repeatAfter);
                        Database.useExtension(ReminderDao.class, dao -> dao.update(reminder));
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
                            
                            reminder.remindAt = Instant.now().plusSeconds(reminder.repeatAfter + 60 * 60 * 6); // 6 hours
                            Database.useExtension(ReminderDao.class, dao -> dao.update(reminder));
                            
                            if (channel == null) return;
                            channel.sendMessage("⚠️ "+ user.getAsMention() +", je n'ai pas pu t'envoyer un message privé pour ton rappel. Je réessayerai plus tard, vérifie que je peux t'envoyer des messages privés.").queue();
                            return;
                        }
                        
                        privateChannel.sendMessage(message).queue(
                            success -> {
                                if (!reminder.repeat) {
                                    Database.useExtension(ReminderDao.class, dao -> dao.delete(reminder.id));
                                    return;
                                }
                                
                                reminder.remindAt = Instant.now().plusSeconds(reminder.repeatAfter);
                                Database.useExtension(ReminderDao.class, dao -> dao.update(reminder));
                            }
                        );
                    }),
                failure -> {
                    Log.warn("Failed to retrieve user {} (ID: {}) for reminder ID {}. The reminder will be deleted.", failure, reminder.userId, reminder.id);
                    Database.useExtension(ReminderDao.class, dao -> dao.delete(reminder.id));
                });
        }
    }
    
    private static void checkExpiredSanctions() {
        List<SanctionEntity> sanctions = Database.withExtension(SanctionDao.class, dao -> dao.getExpiredSanctions());
        if (sanctions.isEmpty()) return;
        
        for (SanctionEntity sanction : sanctions) {
            Guild guild = JosetaBot.get().getGuildById(sanction.guildId);
            Database.useExtension(SanctionDao.class, dao -> dao.upsert(sanction.setExpired(true)));
            
            if (guild == null) continue;
            
            // Only ban need action on expiry, others are automatic
            JosetaBot.get().retrieveUserById(sanction.userId).queue(
                user -> {
                    if (sanction.type == SanctionEntity.SanctionType.BAN)
                        guild.unban(user).queue(
                            null,
                            failure -> Log.warn("Failed to unban user {} (ID: {}) on sanction expiry ID {}", failure, user.getAsTag(), user.getIdLong(), sanction.getSanctionId())
                        );
                    
                    user.openPrivateChannel().queue(
                        channel ->
                            channel.sendMessage("Votre sanction sur le serveur **`"+ guild.getName() +"`** d'identifiant **`"+ sanction.getSanctionId() +"`** du <t:"+ sanction.createdAt.getEpochSecond() +":F> a expiré.\n\n-# ***Ceci est un message automatique. Toutes contestations doivent se faire avec le modérateur responsable***").queue(
                                null,
                                failure -> Log.err("Failed to send private message to user {} (ID: {}) for expired sanction ID {}", failure, user.getAsTag(), user.getIdLong(), sanction.getSanctionId())
                            ),
                        failure -> Log.err("Failed to open private channel for user {} (ID: {}) for expired sanction ID {}", failure, user.getAsTag(), user.getIdLong(), sanction.getSanctionId())
                    );
                },
                failure -> Log.err("Failed to retrieve user {} (ID: {}) for expired sanction ID {}", failure, sanction.userId, sanction.getSanctionId())
            );
        }
    }
    
    private static void checkExpiredMessages() {
        removeExpiredMessages(ConfigurationCommand.configurationMessages, message -> message.timestamp);
        removeExpiredMessages(ModerationCommands.modlogMessages, message -> message.timestamp);
        removeExpiredMessages(ReminderCommand.reminderListMessages, message -> message.timestamp);
    }
    
    private static <T> void removeExpiredMessages(Map<?, T> messages, Function<Instant, T> instantGetter) {
        Instant expiration = Instant.now().minusSeconds(15 * 60);
        
        messages.entrySet().removeIf(entry ->
            entry.getValue() == null ||
            instantGetter.get(entry.getValue()).isBefore(expiration)
        );
    }
}
