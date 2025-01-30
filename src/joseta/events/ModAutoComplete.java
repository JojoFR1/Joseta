package joseta.events;

import joseta.utils.struct.*;

import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.hooks.*;
import net.dv8tion.jda.api.interactions.commands.*;

import java.util.stream.*;

public class ModAutoComplete extends ListenerAdapter {
    private static final Seq<String> cmds = Seq.with("ban", "kick", "mute", "unmute", "warn", "unwarn", "unban");
    private static final Seq<String> opts = Seq.with("user", "warn_id");

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (!cmds.contains(event.getName()) && !opts.contains(event.getFocusedOption().getName())) return;

        String userInput = event.getFocusedOption().getValue().toLowerCase();

        if (event.getFocusedOption().getName().equals("user")) userAutoComplete(event, userInput);        
        // TODO Warn ID auto complete
        // else {
        //     event.replyChoices(
                
        //     ).queue();
        // }
    }

    private void userAutoComplete(CommandAutoCompleteInteractionEvent event, String userInput) {
        if (event.getName().equals("unban")) {
            event.getGuild().retrieveBanList().queue(bans -> {
                event.replyChoices(bans.stream()
                    .filter(ban -> ban.getUser().getEffectiveName().toLowerCase().contains(userInput))
                    .map(ban -> new Command.Choice(ban.getUser().getEffectiveName(), ban.getUser().getId()))
                    .limit(25)
                    .collect(Collectors.toList())
                ).queue();
            });
            return;
        }

        event.replyChoices(event.getGuild().getMembers().stream()
            .filter(member -> member.getUser().getName().toLowerCase().startsWith(userInput) || (member.getNickname() != null && member.getNickname().toLowerCase().startsWith(userInput)))
            .map(member -> new Command.Choice(member.getEffectiveName(), member.getUser().getId()))
            .limit(25)
            .collect(Collectors.toList())
        ).queue();
    }
}
