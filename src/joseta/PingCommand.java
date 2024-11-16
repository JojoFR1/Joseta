package joseta;

import java.time.*;
import java.util.*;

import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.hooks.*;

public class PingCommand extends ListenerAdapter {
    
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("testo")) {
            if (!Arrays.stream(Vars.ownersId).anyMatch(event.getUser().getId()::equals)) {
                event.reply("T'es pas un owner, dégage.").queue();
                return;
            }
            
            event.reply(String.format("Coucou %s !", event.getUser().getAsMention())).queue();

            return;
        }
        else if (!event.getName().equals("ping")) return;

        Instant startTime = Instant.now();

        event.reply("Pinging...").queue(reply -> {
            long latency = Duration.between(startTime, Instant.now()).toMillis();
            long gatewayPing = event.getJDA().getGatewayPing();

            reply.editOriginal(String.format("Pong! Latency: %dms | API Latency: %dms", latency, gatewayPing)).queue();
        });
    }
}
