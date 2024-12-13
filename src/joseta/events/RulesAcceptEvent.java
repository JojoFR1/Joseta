package joseta.events;

import net.dv8tion.jda.api.events.interaction.component.*;
import net.dv8tion.jda.api.hooks.*;

public class RulesAcceptEvent extends ListenerAdapter {
    
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getComponentId().equals("rule-accept")) return;

        event.getGuild().addRoleToMember(event.getUser(), event.getGuild().getRoleById(1235571503412543552L)).queue();
        event.getGuild().removeRoleFromMember(event.getUser(), event.getGuild().getRoleById(1259874357384056852L)).queue();
        event.deferEdit().queue();
    }
}
