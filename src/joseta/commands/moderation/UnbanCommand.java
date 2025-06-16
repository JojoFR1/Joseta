package joseta.commands.moderation;

import joseta.*;
import joseta.commands.*;
import joseta.database.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

public class UnbanCommand extends ModCommand {
    
    public UnbanCommand() {
        super("unban", "Débanir un membre.");
        commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS))
            .addOptions(new OptionData(OptionType.STRING, "user", "L'utilisateur a débanir.", true, true));
    }

    @Override
    public void runImpl(SlashCommandInteractionEvent event) {
        event.getGuild().unban(member).queue(
            success -> {
                event.reply("Le membre a bien été débani.").setEphemeral(true).queue();

                // A user can't have 2 ban active at the same time.
                ModLogDatabase.removeSanction(ModLogDatabase.getLatestSanction(user.getIdLong(), event.getGuild().getIdLong(), SanctionType.BAN));
            },
            failure -> {
                event.reply("Une erreur est survenue lors de l'éxecution de la commande. Veuillez contacter un administrateur.").setEphemeral(true).queue();
                JosetaBot.logger.error("Error while executing a command ('unban').", failure);
            }
        );
    }

    @Override
    protected boolean check(SlashCommandInteractionEvent event) {
        if (event.getGuild().retrieveBanList().complete().stream().noneMatch(ban -> ban.getUser().getIdLong() == user.getIdLong())) {
            event.reply("L'utilisateur n'est pas banni.").setEphemeral(true).queue();
            return false;
        }

        return super.check(event);
    }
}
