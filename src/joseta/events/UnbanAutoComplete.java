package joseta.events;

import net.dv8tion.jda.api.entities.Guild.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.hooks.*;
import net.dv8tion.jda.api.interactions.commands.*;

import java.util.*;
import java.util.stream.*;

public class UnbanAutoComplete extends ListenerAdapter {
    
    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (!event.getName().equals("unban") && !event.getFocusedOption().getName().equals("user")) return;

        List<Command.Choice> choices = Stream.of((Ban[]) event.getGuild().retrieveBanList().complete().toArray())
                                      .filter(ban -> ban.getUser().getName().startsWith(event.getFocusedOption().getValue()))
                                      .map(ban -> new Command.Choice(ban.getUser().getName(), ban.getUser().getId()))
                                      .collect(Collectors.toList());
                                      
        event.replyChoices(choices).queue();
    }
}
