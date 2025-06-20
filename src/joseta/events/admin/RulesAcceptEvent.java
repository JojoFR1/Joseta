package joseta.events.admin;

import joseta.*;
import joseta.database.*;
import joseta.database.entry.*;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.component.*;

import java.sql.*;

public class RulesAcceptEvent {
    
    public static void execute(ButtonInteractionEvent event) {
        ConfigEntry config;
        try {
            config = Databases.getInstance().getConfigDao().queryForId(event.getGuild().getIdLong());
        } catch (SQLException e) {
            JosetaBot.logger.error("Erreur lors de la récupération de la configuration du serveur {} : {}", event.getGuild().getId(), e.getMessage());
            event.reply("Une erreur est survenue lors de la récupération de la configuration du serveur.").setEphemeral(true).queue();
            return;
        }

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
