package joseta.commands.moderation;

import joseta.commands.*;
import joseta.database.*;
import joseta.database.entry.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

public class UnwarnCommand extends ModCommand {
    private int warnId;
    
    public UnwarnCommand() {
        super("unwarn", "Retire l'avertissement d'un membre.");
        commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
            .addOptions(
                new OptionData(OptionType.USER, "user", "Le membre a unwarn.", true),
                new OptionData(OptionType.STRING, "warn_id", "L'identifiant du warn. Plus récent par défaut.", false, true)
            );
    }

    @Override
    public void runImpl(SlashCommandInteractionEvent event) {
        SanctionEntry sanction = null;
        if (warnId == -1)
            sanction = ModLogDatabase.getLatestSanction(user.getIdLong(), event.getGuild().getIdLong(), SanctionType.WARN);
        else 
            //TODO support giving an ID 
            sanction = ModLogDatabase.getSanctionById(warnId, SanctionType.WARN);

        ModLogDatabase.removeSanction(sanction);

        event.reply("Le membre a bien été unwarn.").setEphemeral(true).queue();
    }    


    @Override
    protected void getArgs(SlashCommandInteractionEvent event) {
        super.getArgs(event);

        // warnId = event.getOption("warn_id", -1, OptionMapping::getAsInt);
        warnId = -1;
    }
}
