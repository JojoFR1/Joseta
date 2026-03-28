package dev.jojofr.joseta.events;

import dev.jojofr.joseta.JosetaBot;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.entities.ReminderEntity;
import dev.jojofr.joseta.database.entities.ReminderEntity_;
import dev.jojofr.joseta.database.entities.SanctionEntity;
import dev.jojofr.joseta.database.entities.SanctionEntity_;
import dev.jojofr.joseta.utils.BotCache;
import dev.jojofr.joseta.utils.Log;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledEvents {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
    
    public static void schedule() {
        // Check reminders every minute
        scheduler.scheduleAtFixedRate(ScheduledEvents::checkReminders, 0, 1, TimeUnit.MINUTES);
        // Check expired sanctions every 15 minutes
        scheduler.scheduleAtFixedRate(ScheduledEvents::checkExpiredSanctions, 0, 15, TimeUnit.MINUTES);
        // APRIL FOOL - Ad announcement every hour
        scheduler.scheduleAtFixedRate(ScheduledEvents::sendAd, 0, 1, TimeUnit.HOURS);
    }
    
    private static String[] messages = {
        """
Bonjour !

Vous connaissez tous <@1307015890146955285>, mais saviez vous qu'il existait une version *fancy* ?

**"""+ BotCache.ICON_EMOJI.getFormatted() +"""
\sMindustry France** vous présente **<@1485973922464661627>** ! C'est la même chose que *Joseta* mais avec encore plus de fonctionnalité pour ***seulement*** 4.99€/mois¹ !"""
    };
    private static int currentMessageIndex = 0;
    
    private static void sendAd() {
        long id = JosetaBot.debug ? 1020788444592611350L : 1219013344099303576L;
        TextChannel channel = JosetaBot.get().getTextChannelById(id);
        if (channel == null) {
            Log.err("Failed to send ad message, channel not found (ID: {})", id);
            return;
        }
        if (currentMessageIndex >= messages.length) return;
        
        String message = messages[currentMessageIndex];
        currentMessageIndex += 1;
        
        channel.sendMessage(message).queue();
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
                return;
            }
            
            if (channel == null) return;
            JosetaBot.get().retrieveUserById(reminder.userId).queue(
                user -> user.openPrivateChannel().queue(
                    privateChannel -> {
                        if (!privateChannel.canTalk()) {
                            Log.warn("Cannot send reminder DM to user {} (ID: {}) for reminder ID {} because the bot cannot talk in the private channel", user.getAsTag(), user.getIdLong(), reminder.id);
                            channel.sendMessage("⚠️ "+ user.getAsMention() +", je n'ai pas pu t'envoyer un message privé pour ton rappel. Je réessayerai dans 1 heure, vérifie que je peux t'envoyer des messages privés.").queue();
                            return;
                        }
                        
                        channel.sendMessage(message).queue(
                            success -> {
                                if (!reminder.repeat) {
                                    Database.delete(reminder);
                                    return;
                                }
                                
                                reminder.remindAt = Instant.now().plusSeconds(reminder.remindAfter);
                                Database.update(reminder);
                            }
                        );
                    }));
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
            
            Database.update(sanction.setExpired(true));
        }
    }
}
