package joseta.commands.moderation;

import joseta.commands.*;
import joseta.database.*;
import joseta.database.entry.*;
import joseta.database.helper.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

public class UnwarnCommand extends ModCommand {
    private long warnId;
    
    public UnwarnCommand() {
        super("unwarn", "Retire l'avertissement d'un membre.");
        commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
            .addOptions(
                new OptionData(OptionType.USER, "user", "Le membre a unwarn.", true),
                new OptionData(OptionType.INTEGER, "warn_id", "L'identifiant du warn. Plus récent par défaut.", false, true)
            );
    }

    @Override
    public void runImpl(SlashCommandInteractionEvent event) {
        SanctionEntry entry;
        Databases databases = Databases.getInstance();

        if (warnId == -1) entry = SanctionDatabaseHelper.getLatestSanction(user.getIdLong(), event.getGuild().getIdLong(), 'W');
        else entry = databases.get(SanctionEntry.class, warnId);

        if (entry.getSanctionTypeId() != 'W') {
            event.reply("L'identifiant de l'avertissement n'est pas valide.").setEphemeral(true).queue();
            return;
        }

        databases.delete(entry);
        event.reply("Le membre a bien été unwarn.").setEphemeral(true).queue();
    }    


    @Override
    protected void getArgs(SlashCommandInteractionEvent event) {
        super.getArgs(event);

        warnId = event.getOption("warn_id", -1, OptionMapping::getAsLong).longValue();
    }
}
