package joseta.commands.misc;

import joseta.commands.*;

import net.dv8tion.jda.api.events.interaction.command.*;

public class PingCommand extends Command {

    public PingCommand() {
        super("ping", "Obtenez le ping du bot.");
    }
    
    @Override
    public void runImpl(SlashCommandInteractionEvent event) {
        long startTime = System.currentTimeMillis();
        long gatewayPing = event.getJDA().getGatewayPing();

        event.reply("Pinging...").queue(reply -> {
            reply.editOriginal("Pong! Latency: "+ (System.currentTimeMillis() - startTime) +"ms | API Latency: "+ gatewayPing +"ms").queue();
        });
    }
}
