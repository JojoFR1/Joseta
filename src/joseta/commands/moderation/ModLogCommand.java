package joseta.commands.moderation;

import joseta.commands.*;
import joseta.utils.*;
import joseta.utils.ModLog.*;
import joseta.utils.struct.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Message.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.interactions.components.buttons.*;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.time.*;

public class ModLogCommand extends ModCommand {
    private static final int SANCTION_PER_PAGE = 5;
    public static ObjectMap<Long, User> userOfMessage = new ObjectMap<>();

    public ModLogCommand() {
        super("modlog", "Obtient l'historique de modérations d'un membre",
            DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS),
            new OptionData(OptionType.USER, "user", "Le membre pour qui obtenir l'historique. Défaut: éxecuteur de la commande.")
        );
    }

    @Override
    protected void runImpl(SlashCommandInteractionEvent event) {
        MessageEmbed embed = generateEmbed(event.getGuild(), user, 1);
        Message msg = event.replyEmbeds(embed).addActionRow(
            Button.secondary("modlog-page-b-first", "Page first"),
            Button.secondary("modlog-page-b-prev", "Page prev"),
            Button.secondary("modlog-page-b-next", "Page next"),
            Button.secondary("modlog-page-b-last", "Page last")
        ).complete().retrieveOriginal().complete();

        userOfMessage.put(msg.getIdLong(), user);
    }

    public static MessageEmbed generateEmbed(Guild guild, User user, int currentPage) {
        Seq<Sanction> sanctions = modLog.getUserLog(user.getIdLong(), currentPage, SANCTION_PER_PAGE);
        int totalPages = (int) Math.ceil((double) modLog.getUserTotalSanctions(user.getIdLong()) / SANCTION_PER_PAGE);

        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("Historique de modération de " + user.getName() + " ┃ Page "+ currentPage +"/"+ totalPages)
            .setColor(Color.BLUE)
            .setFooter(guild.getName(), guild.getIconUrl())
            .setTimestamp(Instant.now());

        String description = "";

        if (sanctions.isEmpty()) description = "Cette utilisateur n'a aucune sanction !";

        else for (Sanction sanction : sanctions) {
            int sanctionTypeId = Integer.parseInt(Long.toString(sanction.id).substring(0,2));
            String sanctionType = sanctionTypeId == SanctionType.WARN ? "Warn"
                                : sanctionTypeId == SanctionType.MUTE ? "Mute"
                                : sanctionTypeId == SanctionType.KICK ? "Kick"
                                : "Ban";

            description += "### "+ sanctionType +" - "+ sanction.id
                        + "\n>   - Responsable: <@"+ sanction.moderatorId +"> (`"+ sanction.moderatorId +"`)"
                        + "\n>   - Le: <t:"+ sanction.at.getEpochSecond() +":F>";
            
            if (sanctionTypeId != SanctionType.KICK) description += "\n>  - Pendant: " + Strings.convertSecond(sanction.time);
            
            
            description += "\n>   - Raison: " + sanction.reason + "\n\n";
        }

        embed.setDescription(description);

        return embed.build();
    }

    @Override
    protected boolean check(SlashCommandInteractionEvent event) {
        return true;
    }
}
