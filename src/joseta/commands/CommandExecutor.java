package joseta.commands;

import joseta.*;

import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.hooks.*;

public class CommandExecutor extends ListenerAdapter {
    
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        Command command = JosetaBot.commands.find(cmd -> cmd.name.equals(event.getName()));

        if (command != null) command.run(event);
        else event.reply("Unknow command.");
    }
}
