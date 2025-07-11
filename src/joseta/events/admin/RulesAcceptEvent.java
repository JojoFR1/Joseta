package joseta.events.admin;

import joseta.database.*;
import joseta.database.entry.*;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.component.*;

public class RulesAcceptEvent {
    
    public static void execute(ButtonInteractionEvent event) {
        ConfigEntry config = ConfigDatabase.getConfig(event.getGuild().getIdLong());

        Role joinRole, verifiedRole;
        if (config.joinRoleId == 0L || (joinRole = event.getGuild().getRoleById(config.joinRoleId)) == null) {
            return;
        }
        if (config.verifiedRoleId == 0L || (verifiedRole = event.getGuild().getRoleById(config.verifiedRoleId)) == null) {
            return;
        }

        event.getGuild().addRoleToMember(event.getUser(), verifiedRole).queue();
        event.getGuild().removeRoleFromMember(event.getUser(), joinRole).queue();
        event.deferEdit().queue();
    }
}
