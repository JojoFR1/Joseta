package joseta.commands;

import joseta.annotations.*;
import joseta.annotations.modules.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.*;

public class MiscCommands extends CommandModule {

    public MiscCommands(SlashCommandInteractionEvent event) { super(event); }

    @SlashCommand(description = "Check the bot's responsiveness")
    public void ping() {
        event.reply("Poong! "+ event.getJDA().getGatewayPing() +"ms " + event.getName()).queue();
    }

    @SlashCommand(description = "Responds with ping")
    public void pong(@Option(required = false) User user) {
        if (user == null) {
            event.reply("l4p1n was here").queue();
            return;
        }
        event.reply("Piiiiiing! "+ event.getJDA().getGatewayPing() +"ms " + event.getName() + "hey " + user.getAsMention()).queue();
    }
}
