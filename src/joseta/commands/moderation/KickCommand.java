package joseta.commands.moderation;

import joseta.commands.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

public class KickCommand extends ModCommand {
    
    public KickCommand() {
        super("kick", "Exclue un membre.",
            DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS),
            new OptionData(OptionType.USER, "user", "Le membre a exclure.", true),
            new OptionData(OptionType.STRING, "reason", "La raison de l'exclusion.")
        );
    }

    @Override
    public void runImpl(SlashCommandInteractionEvent event) {
        member.kick().reason(reason).queue();

        event.reply("Le membre a bien été expulsé.").queue();

        modLog.log(SanctionType.KICK, member.getIdLong(), event.getUser().getIdLong(), event.getGuild().getIdLong(), reason, -1);
    }
}
