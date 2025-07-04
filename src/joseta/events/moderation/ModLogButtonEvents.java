package joseta.events.moderation;

import joseta.commands.moderation.*;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.component.*;

public class ModLogButtonEvents {

    public static void execute(ButtonInteractionEvent event) {
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
}
