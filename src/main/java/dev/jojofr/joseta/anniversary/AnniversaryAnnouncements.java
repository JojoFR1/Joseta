package dev.jojofr.joseta.anniversary;

import dev.jojofr.joseta.JosetaBot;
import dev.jojofr.joseta.utils.BotCache;
import dev.jojofr.joseta.utils.Log;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;

import java.time.*;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class AnniversaryAnnouncements {
    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");
    private static final LocalDate START_DATE = LocalDate.of(2026, 11, 15);
    private static final ZonedDateTime EVENT_END = ZonedDateTime.of(LocalDateTime.of(2026, 11, 23, 0, 0, 0, 0), PARIS);
    
    private static final long CHANNEL_ID = 1219013344099303576L;
    
    private static final Duration MIN_GAP = Duration.ofHours(2);
    
    private static final String PREMIUM = "<@1485973922464661627>";
    private static final String JOSETA = "<@1307015890146955285>";
    
    private record Announcement(String key, ZonedDateTime dueAt, String message) {}
    private static ZonedDateTime day(int dayOffset, int hour, int minute) {
        return START_DATE.plusDays(dayOffset).atTime(hour, minute).atZone(PARIS);
    }
    
    private static final List<Announcement> ANNOUNCEMENTS = List.of(
        new Announcement("launch", day(0, 18, 0), """
{icon} **{premium} est de retour !**

Vous pensiez l'avoir perdue à jamais ? Pour fêter l'anniversaire de {joseta}, **{premium}** revient pour **une semaine entière**, et cette fois-ci c'est ***gratuit***¹ !

Au programme : de la surprise, de la *cool-itude* et d'autres... **surprises**. Restez attentifs, on vous en dévoile un peu plus chaque jour.

-# ¹ Oui, vraiment. Le contrat d'utilisation est toujours disponible à <https://joseta.jojofr.dev/terms-of-use>, on vous déconseille de le lire."""),
        new Announcement("surprise", day(1, 12, 30), """
Un peu de piment dans votre journée ? Essayez `/surprendmoi` !

Un rôle **très** spécial, un rôle *moins* spécial... ou une petite **restriction** de vos services Premium™, voire pire. Le tout avec un algorithme d'aléatoire surpuissant, bien sûr.

Vous vous demandez si vous êtes restreint(e) ? `/jepeux` vous le dira.

-# Aucun membre n'a été maltraité durant la conception de cette fonctionnalité."""),
        new Announcement("gacha", day(2, 19, 0), """
**Nouveauté exclusive !** Découvrez `/gacha` : collectionnez **12 photos exclusives du chat de Jojo**, réparties en **2 sets de 6**.

Chaque set complété vous offre un rôle spécial **permanent**. Attention, certaines photos sont bien plus rares que d'autres... et vous ne pouvez pas tirer en continu : `/gacha` vous dira quand revenir.

-# Aucun chat n'a été maltraité non plus (il a juste dû poser)."""),
        new Announcement("cool", day(3, 12, 30), """
Vous voulez devenir plus *cool* que les autres ? `/jeveuxetrecool` est là pour vous !

Mais soyons honnêtes : ça peut très bien mal tourner.

-# (résultats toujours non garantis)"""),
        new Announcement("teaser", day(4, 19, 0), """
Alors, **qui sera le premier à compléter un set ?** 👀

Les rôles permanents n'attendent que vous, et les photos les plus rares se cachent encore. Continuez à tirer avec `/gacha` !

-# Les doublons ne servent à rien, la revente ou l'échange n'est pas possible."""),
        new Announcement("doisje", day(5, 19, 0), """
Parce que **{premium}** pense à tout, voici deux dernières nouveautés pour bien finir la semaine !

🎱 `/doisje` : posez votre question et recevez une réponse ***garantie*** par notre intelligence supérieure¹. Jamais contredite (sauf par elle-même).

🤖 `/chat` : discutez avec une IA ***surpuissante*** directement dans votre navigateur, sans rien installer ! Déjà adoptée par plus de **500 millions** d'utilisateurs² → <https://joseta.jojofr.dev/chat>

-# ¹ Les réponses de `/doisje` n'engagent en aucun cas l'équipe de Mindustry France.
-# ² Chiffre non vérifié, comme tous les autres."""),
        new Announcement("reviews", day(6, 12, 30), """
⭐⭐⭐⭐⭐ **Ce que disent nos utilisateurs de {premium}**

> « J'ai tiré 54 fois avant de compléter mon premier set. Je suis fatigué mais heureux. »
> - Jojo, développeur (et testeur malchanceux)

> « J'ai demandé à être cool et j'ai été restreint. 10/10. »
> - un membre très réel

> « Je n'ai toujours pas la photo légendaire. »
> - tout le monde

-# Avis 100 % authentiques, vérifiés par notre équipe d'experts."""),
        new Announcement("lastday", day(6, 20, 0), """
**La fin de l'événement approche !**

Il vous reste quelques tirages pour compléter votre collection et décrocher le rôle spécial permanent. `/gacha`, `/surprendmoi`, `/jeveuxetrecool`, `/chat`, `/doisje` : profitez-en tant que {premium} est encore là !"""),
        new Announcement("finale", EVENT_END, """
Malheureusement, dû à des coûts techniques trop avancés et à la surutilisation de nos services, nous sommes navrés de vous annoncer que **{premium}** ne sera plus disponible dès à présent.

Nous vous présentons nos plus sincères excuses, mais ne craignez pas ! {joseta} restera à votre service, et les rôles obtenus grâce au gacha sont à vous **pour toujours**.

Merci à toutes et tous d'avoir participé à cet anniversaire !

Avec nos plus sincères salutations distinguées, l'équipe de Mindustry France (et surtout Jojo).

-# et bon anniversaire Joseta :D""")
    );
    
    static {
        Set<String> keys = new HashSet<>();
        ZonedDateTime previous = null;
        for (Announcement announcement : ANNOUNCEMENTS) {
            if (!keys.add(announcement.key()))
                throw new IllegalStateException("Duplicate announcement key: " + announcement.key());
            if (announcement.message().length() > Message.MAX_CONTENT_LENGTH - 100)
                throw new IllegalStateException("Announcement message too long: " + announcement.key());
            
            ZonedDateTime due = announcement.dueAt();
            if (previous != null && !due.isAfter(previous))
                throw new IllegalStateException("Announcement dueAt not in order: " + announcement.key());
            previous = due;
        }
    }
    
    public static void tick(AnniversaryEventDao dao) {
        try {
            ZonedDateTime now = ZonedDateTime.now(PARIS);
            
            Optional<Instant> lastSent = dao.lastAnnouncementAt();
            if (lastSent.isPresent() && Duration.between(lastSent.get(), now.toInstant()).compareTo(MIN_GAP) < 0) return;
            
            Set<String> sent = new HashSet<>(dao.sentAnnouncements());
            for (Announcement announcement : ANNOUNCEMENTS) {
                if (sent.contains(announcement.key())) continue;
                if (now.isBefore(announcement.dueAt())) continue;
                
                send(dao, announcement);
                return;
            }
        } catch (Throwable t) {
            Log.err("Error while checking anniversary announcements", t);
        }
    }
    
    private static void send(AnniversaryEventDao dao, Announcement announcement) {
        NewsChannel channel = JosetaBot.get().getNewsChannelById(CHANNEL_ID);
        if (channel == null) {
            Log.err("Failed to send anniversary announcement '{}', channel not found (ID: {})", announcement.key(), CHANNEL_ID);
            return;
        }
        
        if (dao.claimAnnouncement(announcement.key, Instant.now()) == 0) return;
        
        channel.sendMessage(render(announcement.message())).queue(
            null,
            error -> {
                dao.releaseAnnouncement(announcement.key);
                Log.err("Failed to send anniversary announcement '{}'", announcement.key(), error);
            }
        );
    }
    
    private static String render(String message) {
        long end = EVENT_END.toEpochSecond();
        return message.replace("{premium}", PREMIUM)
                      .replace("{joseta}", JOSETA)
                      .replace("{icon}", BotCache.ICON_EMOJI.getFormatted())
                      .replace("{end}", "<t:" + end + ":F>")
                      .replace("{end_relative}", "<t:" + end + ":R>");
    }
}
