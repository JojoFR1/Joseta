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

import java.awt.*;

@InteractionModule
public class StatsCommand {
    
    @SlashCommandInteraction(name = "stats", description = "Affiche les statistiques de l'utilisateur.")
    public void stats(SlashCommandInteractionEvent event) {
        event.deferReply().useComponentsV2().queue(hook -> {
            long guildId = event.getGuild().getIdLong();
            long userId = event.getUser().getIdLong();
//            long botId = event.getJDA().getSelfUser().getIdLong();
            long botId = 1307015890146955285L;
            ConfigurationEntity config = BotCache.getConfiguration(guildId);

            Database.useHandle(handle -> {
                UserEntity dbUser = handle.attach(UserDao.class).getById(userId, guildId);

                MessageDao messageDao = handle.attach(MessageDao.class);
                int messageCount = messageDao.getMemberMessageCount(userId, guildId);

                int  countingMessages;
                int chainBreaks;
                if (config.countingChannelId != null) {
                    countingMessages = messageDao.getMemberCountingMessageCount(userId, guildId, config.countingChannelId, botId);
                    chainBreaks = messageDao.getMemberChainBreakCount(userId, guildId, config.countingChannelId, botId);
                } else {
                    countingMessages = 0;
                    chainBreaks = 0;
                }

                int countingSpecialMessages;
                int chainBreaksSpecial;
                if (config.countingSpecialChannelId != null) {
                    countingSpecialMessages = messageDao.getMemberCountingMessageCount(userId, guildId, config.countingSpecialChannelId, botId);
                    chainBreaksSpecial = messageDao.getMemberChainBreakCount(userId, guildId, config.countingSpecialChannelId, botId);
                } else {
                    countingSpecialMessages = 0;
                    chainBreaksSpecial = 0;
                }

                event.getUser().retrieveProfile().queue(profile -> {
                    Color color = profile.getAccentColor();
                    if (color == null) color = event.getMember().getColors().getPrimary();
                    
                    Container statsContainer = createStatsContainer(event.getGuild(), event.getMember(), dbUser, messageCount, countingMessages, chainBreaks, countingSpecialMessages, chainBreaksSpecial, color);
                    hook.editOriginalComponents(statsContainer).useComponentsV2().queue();
                });
            });
        });
    }

    private Container createStatsContainer(Guild guild, Member member, UserEntity dbUser, int messageCount, int countingMessages, int chainBreaks, int countingSpecialMessages, int chainBreaksSpecial, Color color) {
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
                ### 👤 Profil
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
                ### 📊 Activité
                **%,d** messages
                **%s** en vocal
                """,
                messageCount, dbUser == null ? "0s" : TimeUtils.formatTime(dbUser.timeVoice / 1000)
            ),
            Separator.createDivider(Separator.Spacing.SMALL),

            TextDisplay.ofFormat(
                """
                ### 🔢 Comptage
                **%,d** nombre réussi
                **%,d** chaînes cassées
                **%.2f %%** de réussite
                ### ✨ Comptage spécial
                **%,d** nombre réussi
                **%,d** chaînes cassées
                **%.2f %%** de réussite
                """,
                countingMessages, chainBreaks, countingMessages == 0 ? 0 : (double) countingMessages / (countingMessages + chainBreaks) * 100,
                countingSpecialMessages, chainBreaksSpecial, countingSpecialMessages == 0 ? 0 : (double) countingSpecialMessages / (countingSpecialMessages + chainBreaksSpecial) * 100
            )
        ).withAccentColor(color);
    }
}
