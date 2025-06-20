package joseta.commands.moderation;

import joseta.*;
import joseta.commands.*;
import joseta.database.*;
import joseta.database.entry.*;
import joseta.utils.*;

import arc.struct.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.events.interaction.component.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.interactions.components.*;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.interactions.*;

import java.awt.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.List;

import com.j256.ormlite.stmt.*;

public class ModLogCommand extends ModCommand {
    private static final int SANCTION_PER_PAGE = 5;
    public static ObjectMap<Long, User> userOfMessage = new ObjectMap<>();

    public ModLogCommand() {
        super("modlog", "Obtient l'historique de modérations d'un membre");
        commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
            .addOptions(new OptionData(OptionType.USER, "user", "Le membre pour qui obtenir l'historique."));
    }

    @Override
    protected void runImpl(SlashCommandInteractionEvent event) {
        sendEmbed(event, user, 1, (int) Math.ceil((double) ModLogDatabase.getUserTotalSanctions(user.getIdLong(), event.getGuild().getIdLong()) / SANCTION_PER_PAGE));
    }

    public static void sendEmbed(GenericInteractionCreateEvent event, User user, int page, int lastPage) {
        MessageEmbed embed = generateEmbed(event.getGuild(), user, page);
        ItemComponent[] buttons = {
            Button.secondary("modlog-page-b-first", "⏪").withDisabled(page == 1),
            Button.secondary("modlog-page-b-prev", "◀️").withDisabled(page <= 1),
            Button.secondary("modlog-page-b-next", "▶️").withDisabled(page >= lastPage),
            Button.secondary("modlog-page-b-last", "⏩").withDisabled(page == lastPage)
        };

        if (event instanceof SlashCommandInteractionEvent cevent) {
            ReplyCallbackAction callback = cevent.replyEmbeds(embed);

            // Only when the user has no sanction that it will start like this.
            if (!embed.getDescription().startsWith("Oh !")) callback.addActionRow(buttons);

            Message msg = callback.complete().retrieveOriginal().complete();

            userOfMessage.put(msg.getIdLong(), user);
        }
        if (event instanceof ButtonInteractionEvent bevent) {
            bevent.editMessageEmbeds(embed).setActionRow(buttons).queue();
        }
    }

    public static MessageEmbed generateEmbed(Guild guild, User user, int currentPage) {
        List<SanctionEntry> sanctions;
        try {
            QueryBuilder<SanctionEntry, Long> queryBuilder = Databases.getInstance().getSanctionDao().queryBuilder();
            queryBuilder.where()
                .eq("userId", user.getIdLong())
                .and()
                .eq("guildId", guild.getIdLong());

            long offset = (currentPage - 1) * SANCTION_PER_PAGE;

            queryBuilder.offset(offset).limit((long) SANCTION_PER_PAGE);

            sanctions = queryBuilder.query();

        } catch (SQLException e) {
            JosetaBot.logger.error("Erreur lors de la récupération des sanctions pour l'utilisateur {} dans le serveur {} : {}", user.getId(), guild.getId(), e.getMessage());
            return new EmbedBuilder()
                .setTitle("Erreur")
                .setDescription("Une erreur est survenue lors de la récupération des sanctions.")
                .setColor(Color.RED)
                .build();
        }

        int totalPages = (int) Math.ceil((double) ModLogDatabase.getUserTotalSanctions(user.getIdLong(), guild.getIdLong()) / SANCTION_PER_PAGE);

        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("Historique de modération de " + user.getName() + " ┃ Page "+ currentPage +"/"+ totalPages)
            .setColor(Color.BLUE)
            .setFooter(guild.getName(), guild.getIconUrl())
            .setTimestamp(Instant.now());

        String description = "";
        if (sanctions.isEmpty()) description = "Oh ! Cet utilisateur n'a aucune sanction !";

        else for (SanctionEntry sanction : sanctions) {
            int sanctionTypeId = Integer.parseInt(Long.toString(sanction.getId()).substring(0,2));
            String sanctionType = sanctionTypeId == SanctionType.WARN ? "Warn"
                                : sanctionTypeId == SanctionType.MUTE ? "Mute"
                                : sanctionTypeId == SanctionType.KICK ? "Kick"
                                : "Ban";

            description += "### "+ sanctionType +" - "+ sanction.getId();
            if (sanction.isExpired()) description += " (Expirée)";

            description += "\n>   - Responsable: <@"+ sanction.getModeratorId() +"> (`"+ sanction.getModeratorId() +"`)"
                         + "\n>   - Le: <t:"+ sanction.getTimestamp().getEpochSecond() +":F>";
            
            if (sanctionTypeId != SanctionType.KICK && sanction.getExpiryTime() >= 1) description += "\n>  - Pendant: " + TimeParser.convertSecond(sanction.getExpiryTime());

            description += "\n>   - Raison: " + sanction.getReason() + "\n\n";
        }

        embed.setDescription(description);

        return embed.build();
    }

    @Override
    protected boolean check(SlashCommandInteractionEvent event) {
        return true;
    }

    @Override
    protected void getArgs(SlashCommandInteractionEvent event) {
        super.getArgs(event);

        if (user == null || member == null) {
            user = event.getUser();
            member = event.getMember();
        }
    }
}
