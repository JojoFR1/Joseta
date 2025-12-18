package joseta.events;

import joseta.annotations.EventModule;
import joseta.annotations.types.Event;
import joseta.database.Database;
import joseta.database.entities.Configuration;
import joseta.events.misc.CountingChannel;
import joseta.generated.EventType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.regex.Pattern;

@EventModule
public class MiscEvents {
    // TODO improve, too many false positives
    private static final Pattern patternQuestion = Pattern.compile(
        "(?:\\b|[.,?!;:])(?:com*[ea]nt?|pos*ible|m(?:oyen|ani[èeé]re)|fa[cç]on)(?:\\b|[.,?!;:])",
        Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ
    );
    private static final Pattern patternMulti = Pattern.compile(
        "(?:\\b|[.,?!;:])(?:multi[ -]?(?:joueu?r|playeu?r)?|co*p(?:eration|[ea]?ins?)?|amis?|pot[oe]s?|(?:[aà] (?:deux|[2-9]|[1-9]+|plu?si?e?u?rs?)))(?:\\b|[.,?!;:])",
        Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ
    );
    
    //TODO unhardcode message & emoji
    public static final String autoResponseMessage = "<:doyouknowtheway:1338158294702755900> Vous voulez héberger votre partie pour jouer avec des amis ?\nVous trouverez plus d'informations ici : <https://zetamap.fr/mindustry_hosting/>";
    
    
    @Event(type = EventType.MESSAGE_RECEIVED)
    public void autoResponse(MessageReceivedEvent event) {
        Configuration config = Database.get(Configuration.class, event.getGuild().getIdLong());
        if (config == null || !config.autoResponseEnabled) return;
        
        String text = event.getMessage().getContentRaw();
        if (patternQuestion.matcher(text).find() && patternMulti.matcher(text).find())
            event.getMessage().reply(autoResponseMessage + "\n*Ceci est une réponse automatique possiblement hors-sujet.*").queue();
    }
    
    @Event(type = EventType.MESSAGE_RECEIVED)
    public void countingCheck(MessageReceivedEvent event) {
        Configuration config = Database.get(Configuration.class, event.getGuild().getIdLong());
        if (config == null || !config.countingEnabled) return;
        
        if (event.getAuthor().isBot() || event.getChannel().getIdLong() != config.countingChannelId) return;
        CountingChannel.check(event.getChannel(), event.getMessage());
    }
}
