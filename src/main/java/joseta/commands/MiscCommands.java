package joseta.commands;

import joseta.annotations.modules.*;
import joseta.annotations.types.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.*;

public class MiscCommands extends CommandModule {

    public MiscCommands(SlashCommandInteractionEvent event) { super(event); }

    @SlashCommand(description = "Check the bot's responsiveness")
    public void ping() {
        event.reply("Poong! "+ event.getJDA().getGatewayPing() +"ms " + event.getName()).queue();
    }

    @SlashCommand(description = "Responds with ping")
    public void pong(@Option User user) {
        if (user == null) {
            event.reply("l4p1n was here").queue();
            return;
        }

        event.reply("Piiiiiing! "+ event.getJDA().getGatewayPing() +"ms " + event.getName() + "hey " + user.getAsMention()).queue();
    }
}
