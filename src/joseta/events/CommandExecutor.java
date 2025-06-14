package joseta.events;

import joseta.*;
import joseta.commands.*;

import net.dv8tion.jda.api.events.interaction.command.*;

public class CommandExecutor {
    
    public static void execute(SlashCommandInteractionEvent event) {
        Command command = JosetaBot.commands.find(cmd -> cmd.name.equals(event.getName()));

        if (command != null) command.run(event);
        else event.reply("Unknow command.");
    }
}
