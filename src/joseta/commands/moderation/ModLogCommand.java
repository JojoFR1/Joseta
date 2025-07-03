package joseta.commands.moderation;

import joseta.commands.*;
import joseta.database.*;
import joseta.database.entry.*;
import joseta.database.helper.*;
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
import java.time.*;
import java.util.List;

import org.hibernate.query.criteria.*;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;

public class ModLogCommand extends ModCommand {
    private static final int SANCTION_PER_PAGE = 5;
    public static ObjectMap<Long, Member> userOfMessage = new ObjectMap<>();

    public ModLogCommand() {
        super("modlog", "Obtient l'historique de modérations d'un membre");
        commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
            .addOptions(new OptionData(OptionType.USER, "user", "Le membre pour qui obtenir l'historique."));
    }

    @Override
    protected void runImpl(SlashCommandInteractionEvent event) {
        sendEmbed(event, member, 1, (int) Math.ceil((double) UserDatabaseHelper.getUserSanctionCount(member, event.getGuild().getIdLong()) / SANCTION_PER_PAGE));
    }

    public static void sendEmbed(GenericInteractionCreateEvent event, Member member, int page, int lastPage) {
        MessageEmbed embed = generateEmbed(event.getGuild(), member, page);
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

            userOfMessage.put(msg.getIdLong(), member);
        }
        if (event instanceof ButtonInteractionEvent bevent) {
            bevent.editMessageEmbeds(embed).setActionRow(buttons).queue();
        }
    }

    public static MessageEmbed generateEmbed(Guild guild, Member member, int currentPage) {
        HibernateCriteriaBuilder criteriaBuilder = Databases.getInstance().getCriteriaBuilder();
        CriteriaQuery<SanctionEntry> query = criteriaBuilder.createQuery(SanctionEntry.class);
        Root<SanctionEntry> root = query.from(SanctionEntry.class);
        Predicate where = criteriaBuilder.conjunction();
        where = criteriaBuilder.and(where, criteriaBuilder.equal(root.get(SanctionEntry_.userId), member.getIdLong()));
        where = criteriaBuilder.and(where, criteriaBuilder.equal(root.get(SanctionEntry_.guildId), guild.getIdLong()));
        query.select(root).where(where);

        int offset = (currentPage - 1) * SANCTION_PER_PAGE;
        TypedQuery<SanctionEntry> typedQuery = Databases.getInstance().getSession().createQuery(query);
        typedQuery.setFirstResult(offset).setMaxResults(SANCTION_PER_PAGE);

        List<SanctionEntry> sanctions = Databases.getInstance().getSession()
            .createSelectionQuery(query).getResultList();

        // TODO change... change what past me ?
        int totalPages = (int) Math.ceil((double) UserDatabaseHelper.getUserSanctionCount(member, guild.getIdLong()) / SANCTION_PER_PAGE);

        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("Historique de modération de " + member.getEffectiveName() + " ┃ Page "+ currentPage +"/"+ totalPages)
            .setColor(Color.BLUE)
            .setFooter(guild.getName(), guild.getIconUrl())
            .setTimestamp(Instant.now());

        String description = "";
        if (sanctions.isEmpty()) description = "Oh ! Cet utilisateur n'a aucune sanction !";

        else for (SanctionEntry sanction : sanctions) {
            description += "### "+ sanction.getSanctionType() +" - #"+ sanction.getFullSanctionId();
            if (sanction.isExpired()) description += " (Expirée)";

            description += "\n>   - Responsable: <@"+ sanction.getModeratorId() +"> (`"+ sanction.getModeratorId() +"`)"
                         + "\n>   - Le: <t:"+ sanction.getTimestamp().getEpochSecond() +":F>";
            
            if (sanction.getSanctionTypeId() != 'K' && sanction.getExpiryTime() >= 1) description += "\n>  - Pendant: " + TimeParser.convertSecond(sanction.getExpiryTime());

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
