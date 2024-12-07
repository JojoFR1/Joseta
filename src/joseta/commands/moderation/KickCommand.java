package joseta.commands.moderation;

import joseta.commands.ModCommand;
import joseta.utils.struct.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

public class KickCommand extends ModCommand {
    
    public KickCommand() {
        super("kick", "Exclue un membre",
            Seq.with(
                new OptionData(OptionType.USER, "user", "Membre a exclure", true),
                new OptionData(OptionType.STRING, "reason", "La raison de l'exclusion")
            ),
            DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS)
        );
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        super.run(event);
                
        event.reply("Kick - " + member + "\n" + reason).queue();
        // member.kick().reason(reason).queue();

        modLog.log(SanctionType.KICK, member.getIdLong(), event.getUser().getIdLong(), reason, -1);
    }
}
