package dev.jojofr.joseta.commands;

import dev.jojofr.joseta.annotations.InteractionModule;
import dev.jojofr.joseta.annotations.types.interaction.SlashCommandInteraction;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.daos.MessageDao;
import dev.jojofr.joseta.database.daos.UserDao;
import dev.jojofr.joseta.database.entities.ConfigurationEntity;
import dev.jojofr.joseta.database.entities.UserEntity;
import dev.jojofr.joseta.utils.BotCache;
import dev.jojofr.joseta.utils.DiscordTimestamp;
import dev.jojofr.joseta.utils.TimeUtils;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.thumbnail.Thumbnail;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@InteractionModule
public class StatsCommand {
    
    @SlashCommandInteraction(name = "stats", description = "Affiche les statistiques de l'utilisateur.")
    public void stats(SlashCommandInteractionEvent event) {
        ConfigurationEntity config = BotCache.getConfiguration(event.getGuild().getIdLong());

        Database.useHandle(handle -> {
            UserEntity dbUser = handle.attach(UserDao.class).getById(event.getUser().getIdLong(), event.getGuild().getIdLong());

            MessageDao messageDao = handle.attach(MessageDao.class);
            int messageCount = messageDao.getMemberMessageCount(event.getUser().getIdLong(), event.getGuild().getIdLong());

            int  countingMessages = 0;
            int chainBreaks = 0;
            if (config.countingChannelId != null) {
                countingMessages = messageDao.getMemberCountingMessageCount(event.getUser().getIdLong(), event.getGuild().getIdLong(), config.countingChannelId, event.getJDA().getSelfUser().getIdLong());
                chainBreaks = messageDao.getMemberChainBreakCount(event.getUser().getIdLong(), event.getGuild().getIdLong(), config.countingChannelId, event.getJDA().getSelfUser().getIdLong());
            }

            int countingSpecialMessages = 0;
            int chainBreaksSpecial = 0;
            if (config.countingSpecialChannelId != null) {
                countingSpecialMessages = messageDao.getMemberCountingMessageCount(event.getUser().getIdLong(), event.getGuild().getIdLong(), config.countingSpecialChannelId, event.getJDA().getSelfUser().getIdLong());
                chainBreaksSpecial = messageDao.getMemberChainBreakCount(event.getUser().getIdLong(), event.getGuild().getIdLong(), config.countingSpecialChannelId, event.getJDA().getSelfUser().getIdLong());
            }

            Container statsContainer = createStatsContainer(
                    event.getGuild(), event.getMember(), dbUser, messageCount, countingMessages, chainBreaks, countingSpecialMessages, chainBreaksSpecial);

            event.replyComponents(statsContainer).useComponentsV2().queue();
        });
    }

    private Container createStatsContainer(Guild guild, Member member, UserEntity dbUser, int messageCount, int countingMessages, int chainBreaks, int countingSpecialMessages, int chainBreaksSpecial) {
        DiscordTimestamp timestampCreated = DiscordTimestamp.from(member.getTimeCreated());
        DiscordTimestamp timestampJoined = DiscordTimestamp.from(member.getTimeJoined());
        return Container.of(
            Section.of(
                Thumbnail.fromUrl(member.getEffectiveAvatarUrl()),
                TextDisplay.ofFormat(
                    """
                    ## Statistiques de %s
                    -# Statistiques sur **%s**
                    """, member.getEffectiveName(), guild.getName()
                )
            ),
            Separator.createDivider(Separator.Spacing.SMALL),

            TextDisplay.ofFormat(
                """
                ### Profil
                **Compte créé**
                %s · %s
                
                **A rejoint le serveur**
                %s · %s
                """,
                timestampCreated.longDate(), timestampCreated.relative(),
                timestampJoined.longDate(), timestampJoined.relative()
            ),
            Separator.createDivider(Separator.Spacing.SMALL),

            TextDisplay.ofFormat(
                """
                ### Activité
                **%,d** messages
                **%s** en vocal
                """,
                messageCount, dbUser == null ? "0s" : TimeUtils.formatTime(dbUser.timeVoice / 1000)
            ),
            Separator.createDivider(Separator.Spacing.SMALL),

            TextDisplay.ofFormat(
                """
                ### Comptage
                **%,d** nombre réussi
                **%,d** chaînes cassées
                **%.1f %%** de réussite
                ### Comptage spécial
                **%,d** nombre réussi
                **%,d** chaînes cassées
                **%.1f %%** de réussite
                """,
                countingMessages, chainBreaks, countingMessages == 0 ? 0 : (double) countingMessages / (countingMessages + chainBreaks) * 100,
                countingSpecialMessages, chainBreaksSpecial, countingSpecialMessages == 0 ? 0 : (double) countingSpecialMessages / (countingSpecialMessages + chainBreaksSpecial) * 100
            )
        ).withAccentColor(member.getColors().getPrimary());
    }
}
