package joseta.events;

import java.util.regex.Pattern;

import joseta.Vars;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.*;

public class AutoResponse extends ListenerAdapter {
    private static Pattern patternQuestion = Pattern.compile(
        "(?:\\b|[.,?!;:])(?:com*[ea]nt?|si|pos*ible)(?:\\b|[.,?!;:])|\\?",
        Pattern.CASE_INSENSITIVE|Pattern.CANON_EQ
    );
    private static Pattern patternMulti = Pattern.compile(
        "(?:\\b|[.,?!;:])(?:multi[ -]?(?:joueu?r|playeu?r)?|coop(?:eration)?|(?:[aà] (?:deux|[2-9]|[1-9]+|plu?si?e?u?rs?)))(?:\\b|[.,?!;:])",
        Pattern.CASE_INSENSITIVE|Pattern.CANON_EQ
    );
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        String text = msg.getContentRaw();
        
        if (patternQuestion.matcher(text).find() && patternMulti.matcher(text).find()) {
            Vars.logger.debug("Multiplayer regex match.");
            msg.reply(getMessage()).queue();
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("multi")) return;

        event.reply(getMessage()).queue();
    }

    private String getMessage() {
        return "<:doyouknowtheway:1241824114952372344> Vous voulez héberger votre partie pour jouer avec des amis ?\nVous trouverez plus d'informations ici : <https://zetamap.fr/mindustry_hosting/>\n*Ceci est une réponse automatique possiblement hors-sujet.*";
    }
}
