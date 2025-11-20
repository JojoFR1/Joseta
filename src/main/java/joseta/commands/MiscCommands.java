package joseta.commands;

import joseta.annotations.*;
import joseta.annotations.types.*;
import joseta.generated.EventType;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@InteractionModule @EventModule
public class MiscCommands {

    @SlashCommandInteraction(description = "Check the bot's responsiveness")
    public void ping(SlashCommandInteractionEvent event) {
        event.reply("Poong! "+ event.getJDA().getGatewayPing() +"ms " + event.getName()).queue();
    }

    @SlashCommandInteraction(description = "Responds with ping")
    public void pong(SlashCommandInteractionEvent event, @Option User user) {
        if (user == null) {
            event.reply("l4p1n was here").queue();
            return;
        }

        event.reply("Piiiiiing! "+ event.getJDA().getGatewayPing() +"ms " + event.getName() + "hey " + user.getAsMention()).queue();
    }
    
    @Event(type = EventType.MESSAGE_RECEIVED)
    public void test(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        
        event.getChannel().sendMessage("Hello " + event.getAuthor().getAsMention() + "!").queue();
    }
}
