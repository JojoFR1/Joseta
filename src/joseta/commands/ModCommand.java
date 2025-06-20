package joseta.commands;

import joseta.*;
import joseta.database.*;
import joseta.database.entry.*;
import joseta.utils.*;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;

import java.sql.*;

public abstract class ModCommand extends Command {
    protected User user;
    protected Member member;
    protected String reason;
    protected long time;
    protected String defaultTime = "5m";
    
    protected ModCommand(String name, String description) {
        super(name, description);
    }

    @Override
    protected void getArgs(SlashCommandInteractionEvent event) {
        user =   event.getOption("user", null, OptionMapping::getAsUser);
        member = event.getOption("user", null, OptionMapping::getAsMember);
        reason = event.getOption("reason", "Raison par défaut", OptionMapping::getAsString);
        time = TimeParser.parse(event.getOption("time", defaultTime, OptionMapping::getAsString));
    }
    
    @Override
    protected boolean check(SlashCommandInteractionEvent event) {
        ConfigEntry config;
        try {
            config = Databases.getInstance().getConfigDao().queryForId(event.getGuild().getIdLong());
        } catch (SQLException e) {
            JosetaBot.logger.error("Erreur lors de la récupération de la configuration du serveur {} : {}", event.getGuild().getId(), e.getMessage());
            event.reply("Une erreur est survenue lors de la récupération de la configuration du serveur.").setEphemeral(true).queue();
            return false;
        }
        
        if (!config.isModerationEnabled()) {
            event.reply("La modération est désactivée sur ce serveur.").setEphemeral(true).queue();
            return false;
        }
        
        if (user == null || member == null) {
            event.reply("Ce membre n'existe pas ou n'est pas présent sur le serveur. Vérifiez que l'identifiant est correct.").setEphemeral(true).queue();
            return false;
        }

        if (event.getMember().equals(member)) {
            event.reply("Ce membre est vous-même, vous ne pouvez pas vous auto-sanctionner").setEphemeral(true).queue();
            return false;
        }

        if (user.isBot() || user.isSystem()) {
            event.reply("Ce membre est un robot ou un compte système, vous ne pouvez pas le sanctionner.").setEphemeral(true).queue();
            return false;
        }
        
        if (event.getMember().getRoles().get(0).getPosition() < member.getRoles().get(0).getPosition()) {
            event.reply("Ce membre a un rôle supérieur au votre, vous ne pouvez pas le sanctionner.").setEphemeral(true).queue();
            return false;
        }

        if (member.isOwner()) {
            event.reply("Ce membre est le propriétaire du serveur, vous ne pouvez pas le sanctionner.").setEphemeral(true).queue();
            return false;
        }

        return super.check(event);
    }

    public final class SanctionType {
        public static final int WARN = 10;
        public static final int MUTE = 20;
        public static final int KICK = 30;
        public static final int BAN = 40;
    }
}
