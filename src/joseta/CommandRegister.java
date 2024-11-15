package joseta;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.session.*;
import net.dv8tion.jda.api.hooks.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

public class CommandRegister extends ListenerAdapter {
    
    @Override
    public void onReady(ReadyEvent event) {
        Guild guild = event.getJDA().getGuildById("1219005659194851389");

        guild.updateCommands().addCommands(
            Commands.slash("ping", "Pong.")
        ).queue();
    }
}
