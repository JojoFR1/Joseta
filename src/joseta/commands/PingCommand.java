package joseta.commands;

import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.hooks.*;

import java.time.*;

public class PingCommand extends ListenerAdapter {
    
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("ping")) return;

        Instant startTime = Instant.now();

        event.reply("Pinging...").queue(reply -> {
            long latency = Duration.between(startTime, Instant.now()).toMillis();
            long gatewayPing = event.getJDA().getGatewayPing();

            reply.editOriginal(String.format("Pong! Latency: %dms | API Latency: %dms", latency, gatewayPing)).queue();
        });
    }
}