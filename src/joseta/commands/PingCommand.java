package joseta.commands;

import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.hooks.*;

public class PingCommand extends ListenerAdapter {
    
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("ping")) return;

        long startTime = System.currentTimeMillis();
        long gatewayPing = event.getJDA().getGatewayPing();

        event.reply("Pinging...").queue(reply -> {
            reply.editOriginal("Pong! Latency: "+ (System.currentTimeMillis() - startTime) +"ms | API Latency: "+ gatewayPing +"ms").queue();
        });
    }
}
