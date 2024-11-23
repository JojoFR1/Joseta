package joseta.events;

import java.util.regex.Pattern;

import joseta.Vars;
import net.dv8tion.jda.api.entities.*;
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
            Vars.logger.debug("match");
            msg.reply(
                ":doyouknowtheway: Vous voulez héberger votre partie pour jouer avec des amis ?\nVous trouverez plus d'informations ici : https://discord.com/channels/1219005659194851389/1234869952519864321/1234869952519864321\n*Ceci est une réponse automatique possiblement hors-sujet.*"
            ).queue();
        }
    }
}
