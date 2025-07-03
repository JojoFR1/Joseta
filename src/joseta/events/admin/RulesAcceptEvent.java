package joseta.events.admin;

import joseta.database.*;
import joseta.database.entry.*;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.component.*;

public class RulesAcceptEvent {
    
    public static void execute(ButtonInteractionEvent event) {
        ConfigEntry config = Databases.getInstance().get(ConfigEntry.class, event.getGuild().getIdLong());

        Role joinRole, verifiedRole;
        if (config.getJoinRoleId() == 0L || (joinRole = event.getGuild().getRoleById(config.getJoinRoleId())) == null) {
            return;
        }
        if (config.getVerifiedRoleId() == 0L || (verifiedRole = event.getGuild().getRoleById(config.getVerifiedRoleId())) == null) {
            return;
        }

        event.getGuild().addRoleToMember(event.getUser(), verifiedRole).queue();
        event.getGuild().removeRoleFromMember(event.getUser(), joinRole).queue();
        event.deferEdit().queue();
    }
}
