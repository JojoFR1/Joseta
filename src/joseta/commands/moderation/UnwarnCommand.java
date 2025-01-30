package joseta.commands.moderation;

import joseta.commands.*;
import joseta.utils.*;
import joseta.utils.ModLog.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

public class UnwarnCommand extends ModCommand {
    private int warnId;
    
    public UnwarnCommand() {
        super("unwarn", "Retire l'avertissement d'un membre.",
            DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS),
            new OptionData(OptionType.STRING, "user", "Le membre a bannir.", true, true),
            new OptionData(OptionType.STRING, "warn_id", "L'identifiant du warn. Plus récent par défaut.", false, true)
        );        
    }

    @Override
    public void runImpl(SlashCommandInteractionEvent event) {
        Sanction sanction = null;
        if (warnId == -1)
            sanction = ModLog.getLatestSanction(user.getIdLong(), event.getGuild().getIdLong(), SanctionType.WARN);
        else 
            //TODO support giving an ID 
            sanction = ModLog.getSanctionById(warnId, SanctionType.WARN);

        ModLog.removeSanction(sanction);

        event.reply("Le membre a bien été unwarn.").setEphemeral(true).queue();
    }    


    @Override
    protected void getArgs(SlashCommandInteractionEvent event) {
        super.getArgs(event);

        // warnId = event.getOption("warn_id", -1, OptionMapping::getAsInt);
        warnId = -1;
    }
}
