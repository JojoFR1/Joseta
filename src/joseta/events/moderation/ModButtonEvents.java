package joseta.events.moderation;

import arc.util.*;
import joseta.commands.moderation.*;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.*;
import net.dv8tion.jda.api.events.interaction.component.*;

import java.util.*;
import java.util.concurrent.*;

public class ModButtonEvents {

    public static void execute(ButtonInteractionEvent event) {
        if (event.getComponentId().startsWith("mod-log")) executeModLog(event);
        else if (event.getComponentId().equals("mod-clear_confirm")) executeModClear(event);
    }

    private static void executeModLog(ButtonInteractionEvent event) {
        String[] embedTitle = event.getMessage().getEmbeds().get(0).getTitle().split("/");
        int currentPage = Integer.parseInt(embedTitle[0].split("Page ")[1]);
        int lastPage = Integer.parseInt(embedTitle[1]);
        
        String eventId = event.getComponentId();
        int newPage = eventId.endsWith("first") ? 1
                    : eventId.endsWith("prev")  ? currentPage - 1
                    : eventId.endsWith("next")  ? currentPage + 1
                    : lastPage;

        Member member = ModLogCommand.userOfMessage.get(event.getMessageIdLong());
        
        ModLogCommand.sendEmbed(event, member, newPage, lastPage);
    }

    private static void executeModClear(ButtonInteractionEvent event) {
        if (ClearCommand.pendingClear.isEmpty()) return;

        MessageChannelUnion channel = event.getChannel();
        int amount = ClearCommand.pendingClear.get(channel);
        try {
            List<Message> messages = channel.getIterableHistory().takeAsync(amount).get();
            if (messages.isEmpty()) {
                event.reply("Aucun message à supprimer.").setEphemeral(true).queue();
                return;
            }
            channel.purgeMessages(messages);
            event.reply(amount + " messages ont été supprimés.").setEphemeral(true).queue(
                    hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS)
            );
        } catch (InterruptedException | ExecutionException e) {
            event.reply("Une erreur est survenue lors de la suppression des messages.").setEphemeral(true).queue();
            Log.err("Error while executing a command ('clear').", e);
        }
    }
}
