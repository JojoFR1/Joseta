package joseta.events.misc;

import joseta.database.*;
import joseta.database.entry.*;

import arc.util.*;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.*;

import java.sql.*;
import java.util.regex.*;

public class AutoResponseEvent {
    private static final Pattern patternQuestion = Pattern.compile(
        "(?:\\b|[.,?!;:])(?:com*[ea]nt?|pos*ible|m(?:oyen|ani[èeé]re)|fa[cç]on)(?:\\b|[.,?!;:])",
        Pattern.CASE_INSENSITIVE|Pattern.CANON_EQ
    );
    private static final Pattern patternMulti = Pattern.compile(
        "(?:\\b|[.,?!;:])(?:multi[ -]?(?:joueu?r|playeu?r)?|co*p(?:eration|[ea]?ins?)?|amis?|pot[oe]s?|(?:[aà] (?:deux|[2-9]|[1-9]+|plu?si?e?u?rs?)))(?:\\b|[.,?!;:])",
        Pattern.CASE_INSENSITIVE|Pattern.CANON_EQ
    );
    //TODO unhardcode message & emoji
    public static final String message = "<:doyouknowtheway:1338158294702755900> Vous voulez héberger votre partie pour jouer avec des amis ?\nVous trouverez plus d'informations ici : <https://zetamap.fr/mindustry_hosting/>";

    public static void execute(MessageReceivedEvent event) {
        ConfigEntry config;
        try {
            config = Databases.getInstance().getConfigDao().queryForId(event.getGuild().getIdLong());
        } catch (SQLException e) {
            Log.err("Erreur lors de la récupération de la configuration du serveur @ : @", event.getGuild().getId(), e.getMessage());
            event.getChannel().sendMessage("Une erreur est survenue lors de la récupération de la configuration du serveur.").queue();
            return;
        }
        
        if (!config.isAutoResponseEnabled()) return;
        
        Message msg = event.getMessage();
        String text = msg.getContentRaw();
        
        if (patternQuestion.matcher(text).find() && patternMulti.matcher(text).find()) {
            Log.debug("Multiplayer regex match.");
            msg.reply(message + "\n*Ceci est une réponse automatique possiblement hors-sujet.*").queue();
        }

    }
}
