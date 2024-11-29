package joseta.events;

import java.util.regex.Pattern;

import joseta.Vars;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.*;

public class AutoResponse extends ListenerAdapter {
    private static final Pattern patternQuestion = Pattern.compile(
        "(?:\\b|[.,?!;:])(?:com*[ea]nt?|pos*ible|m(?:oyen|ani[èeé]re)|fa[cç]on)(?:\\b|[.,?!;:])",
        Pattern.CASE_INSENSITIVE|Pattern.CANON_EQ
    );
    private static final Pattern patternMulti = Pattern.compile(
        "(?:\\b|[.,?!;:])(?:multi[ -]?(?:joueu?r|playeu?r)?|co*p(?:eration|[ea]?ins?)?|amis?|pot[oe]s?|(?:[aà] (?:deux|[2-9]|[1-9]+|plu?si?e?u?rs?)))(?:\\b|[.,?!;:])",
        Pattern.CASE_INSENSITIVE|Pattern.CANON_EQ
    );
    private final String message = "<:doyouknowtheway:1241824114952372344> Vous voulez héberger votre partie pour jouer avec des amis ?\nVous trouverez plus d'informations ici : <https://zetamap.fr/mindustry_hosting/>";
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        String text = msg.getContentRaw();
        
        if (patternQuestion.matcher(text).find() && patternMulti.matcher(text).find()) {
            Vars.logger.debug("Multiplayer regex match.");
            msg.reply(message + "\n*Ceci est une réponse automatique possiblement hors-sujet.*").queue();
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("multi")) return;

        event.reply(message).queue();
    }
}
