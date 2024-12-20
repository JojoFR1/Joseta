package joseta.events;

import joseta.commands.moderation.*;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.component.*;
import net.dv8tion.jda.api.hooks.*;

public class ModLogButtonEvents extends ListenerAdapter {

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getComponentId().startsWith("modlog-page-b-")) return;

        String[] embedTitle = event.getMessage().getEmbeds().get(0).getTitle().split("/");
        int currentPage = Integer.parseInt(embedTitle[0].split("Page ")[1]);
        int lastPage = Integer.parseInt(embedTitle[1]);
        
        String eventId = event.getComponentId();
        int newPage = eventId.endsWith("first") ? 1
                    : eventId.endsWith("prev")  ? currentPage - 1
                    : eventId.endsWith("next")  ? currentPage + 1
                    : lastPage;

        User user = ModLogCommand.userOfMessage.get(event.getMessageIdLong());
        
        ModLogCommand.sendEmbed(event, user, newPage, lastPage);
    }
}
