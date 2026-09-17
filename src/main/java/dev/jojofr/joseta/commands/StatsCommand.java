package dev.jojofr.joseta.commands;

import dev.jojofr.joseta.annotations.InteractionModule;
import dev.jojofr.joseta.annotations.types.interaction.SlashCommandInteraction;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.daos.MessageDao;
import dev.jojofr.joseta.database.daos.UserDao;
import dev.jojofr.joseta.database.entities.ConfigurationEntity;
import dev.jojofr.joseta.database.entities.UserEntity;
import dev.jojofr.joseta.utils.DiscordTimestamp;
import dev.jojofr.joseta.utils.StringUtils;
import dev.jojofr.joseta.utils.TimeUtils;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.thumbnail.Thumbnail;
import dev.jojofr.joseta.entities.GuildConfiguration;
import dev.jojofr.joseta.utils.BotCache;
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

            if (config.countingChannelId != null) {
                int countingMessages = messageDao.getMemberCountingMessageCount(event.getUser().getIdLong(), event.getGuild().getIdLong(), config.countingChannelId, event.getJDA().getSelfUser().getIdLong());
                int chainBreaks = messageDao.getMemberChainBreakCount(event.getUser().getIdLong(), event.getGuild().getIdLong(), config.countingChannelId, event.getJDA().getSelfUser().getIdLong());
            }

            Container statsContainer = createStatsContainer(event.getGuild(), event.getMember(), dbUser, messageCount);

            event.replyComponents(statsContainer).useComponentsV2().queue();
        });
    }
    
    private Container createStatsContainer(Member member, UserEntity dbUser, int messageCount) {
        DiscordTimestamp timestampCreated = DiscordTimestamp.from(member.getTimeCreated());
        DiscordTimestamp timestampJoined = DiscordTimestamp.from(member.getTimeJoined());
        return Container.of(
            Section.of(
                Thumbnail.fromUrl(member.getEffectiveAvatarUrl()),
                TextDisplay.of("# Statistiques de " + member.getEffectiveName()
                    + "\n-# À rejoint Discord le " + timestampCreated.longFull() + " (" + timestampCreated.relative() + ")"
                    + "\n-# _ _                serveur le " + timestampJoined.longFull() + " (" + timestampJoined.relative() + ")")
            ),
            
            TextDisplay.of("Messages envoyés : " + StringUtils.formatNumber(messageCount)
                + "\n- dont X dans comptage (Y special)"
                + "\n- dont X cassages de chaînes (Y special)"
                + "\n- pour un ratio de Z (compte/cassages) et W (comptesp/cassagesp)"
            ),
            
            TextDisplay.of("Temps passé en vocal : " + (dbUser == null ? "0s" : TimeUtils.formatTime(dbUser.timeVoice / 1000)))
        );
    }
}
