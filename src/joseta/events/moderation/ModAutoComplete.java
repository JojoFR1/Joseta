package joseta.events.moderation;

import arc.struct.*;

import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;

import java.util.stream.*;

public class ModAutoComplete {
    private static final Seq<String> cmds = Seq.with("unwarn", "unban");
    private static final Seq<String> opts = Seq.with("user", "warn_id");

    // TODO rework this
    public static void executeCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (!cmds.contains(event.getName()) && !opts.contains(event.getFocusedOption().getName())) return;

        String userInput = event.getFocusedOption().getValue().toLowerCase();

        if (event.getFocusedOption().getName().equals("user")) userAutoComplete(event, userInput);        
        // TODO Warn ID auto complete
        // else {
        //     event.replyChoices(
                
        //     ).queue();
        // }
    }

    private static void userAutoComplete(CommandAutoCompleteInteractionEvent event, String userInput) {
        event.getGuild().retrieveBanList().queue(bans -> {
            event.replyChoices(bans.stream()
                .filter(ban -> ban.getUser().getEffectiveName().toLowerCase().contains(userInput))
                .map(ban -> new Command.Choice(ban.getUser().getEffectiveName(), ban.getUser().getId()))
                .limit(25)
                .collect(Collectors.toList())
            ).queue();
        });
    }
}
