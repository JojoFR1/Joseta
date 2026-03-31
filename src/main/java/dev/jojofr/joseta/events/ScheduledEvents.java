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
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
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
        scheduler.scheduleAtFixedRate(ScheduledEvents::sendAd, 0, 3, TimeUnit.HOURS);
    }
    
    private static String[] messages = {
        """
Bonjour, bonsoir et bonne nuit !

Vous connaissez tous <@1307015890146955285>, mais saviez vous qu'il existait une version *fancy* ? Plus *extravagante* ? Plus *puissante* ?

**"""+ BotCache.ICON_EMOJI.getFormatted() +"""
\sMindustry France** à l'honneur de vous présenter **<@1485973922464661627>** ! La même chose que *Joseta* mais avec encore plus de fonctionnalité et plus de personnalisation pour ***seulement*** 4.99€/mois¹ !

-# ¹ d'après le contrat d'utilisation, plus de détail à <https://joseta.jojofr.dev/terms-of-use>.""",
        
        "Grâce à **<@1485973922464661627>**, profitez d'une IA personnalisé avec la commande `/chat` et rejoignez des centaines de millions d'autres utilisateurs !\n\n**Ne ratez pas cette opportunité incroyable de faire passer votre expérience avec <@1307015890146955285> au niveau supérieur !**",
        "Marre que <@1307015890146955285> soit toujours en \"*Ne pas déranger*\" sans pouvoir le personnaliser ? Grâce à **<@1485973922464661627>** profitez d'un bot Discord \"*En ligne*\", \"*Hors-ligne*\" ou en \"*Inactif*\" comme bon vous le sent !",
        "Voulez-vous être *surpris(e)* ? **<@1485973922464661627>** vous propose de la surprise ***garantie*** grâce à la commande `/surprendmoi` avec un algorithme d'aléatoire surpuissant !",
        "Rejoignez **plus de 350 millions** d'utilisateurs utilisant **<@1485973922464661627>** dans **plus de 100 000 serveurs** pour booster votre expérience au ***niveau supérieur*** ! Ajoutez **<@1485973922464661627>** à votre serveur dès aujourd'hui et profitez de fonctionnalités exclusives, d'une personnalisation avancée et d'une expérience utilisateur inégalée en cliquant sur ce lien: <https://joseta.jojofr.dev/invite> !",
        "Avec **<@1485973922464661627>** vous pouvez devenir encore plus *cool* que les autres grâce à la commande `/jeveuxetrecool` !\n-# (résultats non garantis)",
        "Vous oubliez toujours quelque chose ? <@1307015890146955285> trop basique pour vous ? Et bien **<@1485973922464661627>** est là pour vous !  Level up votre expérience grâce à des options hyper avancé de `/reminder` !",
        "Vos membres partent trop vite ? Ils ne restent pas ? Grâce à **<@1485973922464661627>** et son bienvenue, passez au luxe avec des images et des messages digne de la haute société directement dans votre serveur Discord !",
        """
Malheureusement, dû à des coûts technique trop avancés et de la surutilisation de nos services, nous sommes navrés de devoir vous annoncer que **<@1485973922464661627>** ne sera plus disponible dès à présent.

Nous vous présentons nos plus sincères excuses, mais ne craignez pas ! <@1307015890146955285> restera à vos services ! Cependant, si vous voulez gardez dans vos souvenirs **<@1485973922464661627>**, son code source est disponible [en cliquant ici](<https://github.com/JojoFR1/Joseta/tree/2026-april-fool>) et vous pouvez faire un donc [juste ici](<https://joseta.jojofr.dev/don>) pour nous soutenir !

Avec nos plus sincères salutations distingués, l'équipe de Mindustry France (et surtout Jojo).

-# et bon poisson d'avril :D\s
"""
    };
    private static int currentMessageIndex = 0;
    
    private static void sendAd() {
        long id = JosetaBot.debug ? 1219013344099303576L : 1219013344099303576L;
        NewsChannel channel = JosetaBot.get().getNewsChannelById(id);
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
    
    // APRIL FOOL send a "I forgot" message and giving another user's reminder (if it exists)
    private static void checkReminders() {
        List<ReminderEntity> allReminders = Database.getAll(ReminderEntity.class);
        List<ReminderEntity> reminders = allReminders.stream().filter(r -> r.remindAt.isBefore(Instant.now()) || r.remindAt.equals(Instant.now())).toList();
        if (reminders.isEmpty()) return;
        
        for (ReminderEntity reminder : reminders) {
            MessageChannel channel = (MessageChannel) JosetaBot.get().getGuildChannelById(reminder.channelId);
            if (channel == null) continue;
            
            ReminderEntity newReminder = allReminders.stream().filter(r -> r.userId != reminder.userId).findAny().orElse(null);
            if (newReminder == null) {
                channel.sendMessage("um, <@"+ reminder.userId +">, j'ai oublié que t'avais un reminder donc... tiens un autre reminder ? ... ah, y en a pas... bah... voila quoi, va te plaindre j'sais pas.").queue(
                    success -> Database.delete(reminder)
                );
                continue;
            }
            
            String message = REMINDER_PREMESSAGE.replace("%userid%", String.valueOf(reminder.userId))
                                                .replace("%message%", newReminder.message);
            
            channel.sendMessage("um, <@"+ reminder.userId +">, j'ai oublié que t'avais un reminder donc... tiens un autre reminder ?\n\n" + message).setAllowedMentions(Collections.singleton(Message.MentionType.USER)).queue(
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
