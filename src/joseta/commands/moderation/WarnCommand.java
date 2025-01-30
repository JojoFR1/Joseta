package joseta.commands.moderation;

import joseta.commands.*;
import joseta.utils.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

public class WarnCommand extends ModCommand {

    public WarnCommand() {
        super("warn", "Avertir un membre.",
            DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS),
            new OptionData(OptionType.USER, "user", "Le membre a avertir.", true),
            new OptionData(OptionType.STRING, "reason", "La raison de l'avertissement."),
            new OptionData(OptionType.STRING, "time", "La durée avant expiration de l'avertissement (s, m, h, d, w).")
        );
        defaultTime = "inf";
    }

    @Override
    public void runImpl(SlashCommandInteractionEvent event) {
        //TODO faillure?
        ModLog.log(SanctionType.WARN, member.getIdLong(), event.getUser().getIdLong(), event.getGuild().getIdLong(), reason, time);
        
        event.reply("Le membre a bien été averti.").setEphemeral(true).queue();
    }
}
