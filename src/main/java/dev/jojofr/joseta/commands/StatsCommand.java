package dev.jojofr.joseta.commands;

import dev.jojofr.joseta.annotations.InteractionModule;
import dev.jojofr.joseta.annotations.types.interaction.Interaction;
import dev.jojofr.joseta.annotations.types.interaction.SlashCommandInteraction;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.daos.MessageDao;
import dev.jojofr.joseta.database.daos.UserDao;
import dev.jojofr.joseta.database.entities.LeaderboardEntry;
import dev.jojofr.joseta.entities.GuildConfiguration;
import dev.jojofr.joseta.entities.messages.StatsMessage;
import dev.jojofr.joseta.utils.BotCache;
import dev.jojofr.joseta.utils.DiscordTimestamp;
import dev.jojofr.joseta.utils.TimeUtils;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.thumbnail.Thumbnail;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@InteractionModule
public class StatsCommand {
    public static final Map<Long, StatsMessage> statsMessages = new ConcurrentHashMap<>();
    
    @SlashCommandInteraction(name = "stats", description = "Affiche les statistiques de l'utilisateur.")
    public void stats(SlashCommandInteractionEvent event) {
        StatsMessage userExist = statsMessages.get(event.getUser().getIdLong());
        if (userExist != null && Instant.now().isBefore(userExist.timestamp.plusSeconds(30 * 60))) {
            int remainingMinutes = (int) (30 - (Instant.now().getEpochSecond() - userExist.timestamp.getEpochSecond()) / 60);
            event.reply("Vous avez déjà une interaction de statistiques en cours. Veuillez utiliser le menu existant ou attendre "+ remainingMinutes +" minutes avant d'en créer un nouveau.").setEphemeral(true).queue();
            return;
        }
        StatsMessage statsMessage = new StatsMessage(event.getGuild().getIdLong(), event.getUser(), 1307015890146955285L);
        // StatsMessage statsMessage = new StatsMessage(event.getGuild().getIdLong(), event.getUser(), event.getJDA().getSelfUser().getIdLong(), config.countingChannelId, config.countingSpecialChannelId);
        statsMessages.put(event.getUser().getIdLong(), statsMessage);
        
        event.deferReply().useComponentsV2().setAllowedMentions(Set.of()).queue(hook -> {
            Container statsContainer = createUserStatsContainer(statsMessage, event.getMember(), event.getGuild().getName());
            hook.editOriginalComponents(statsContainer).useComponentsV2().queue();
        });
    }
    
    @Interaction(id = "stats:nav:*")
    public void onNavigationButton(ButtonInteractionEvent event) {
        String[] parts = event.getComponentId().split(":");
        if (parts.length < 3) {
            event.reply("ID de bouton invalide. Ce menu est obsolète. Veuillez utiliser la commande `/stats` pour créer un nouveau menu de statistiques.").setEphemeral(true).queue();
            return;
        };
        
        long ownerId = Long.parseLong(parts[3]);
        if (event.getUser().getIdLong() != ownerId) {
            event.reply("Vous ne pouvez pas interagir avec ce menu, car vous n'êtes pas le propriétaire de l'interaction. Veuillez utiliser la commande `/stats` pour créer votre propre menu de statistiques.").setEphemeral(true).queue();
            return;
        }
        
        StatsMessage statsMessage = checkStatsMessage(event, ownerId);
        if (statsMessage == null) return;
        
        Container container = null;
        String buttonId = parts[2];
        if (buttonId.equals("self")) {
            statsMessage.isGlobal = false;
            container = createUserStatsContainer(statsMessage, event.getMember(), event.getGuild().getName());
        } else if (buttonId.equals("global")) {
            statsMessage.isGlobal = true;
            container = createGlobalStatsContainer(statsMessage, event.getGuild());
        }
        
        event.editComponents(container).useComponentsV2().queue();
    }
    
    @Interaction(id = "stats:leaderboard:nav:*")
    public void onLeaderboardNavigationButton(ButtonInteractionEvent event) {
        String[] parts = event.getComponentId().split(":");
        if (parts.length < 4) {
            event.reply("ID de bouton invalide. Ce menu est obsolète. Veuillez utiliser la commande `/stats` pour créer un nouveau menu de statistiques.").setEphemeral(true).queue();
            return;
        }
        
        long ownerId = Long.parseLong(parts[4]);
        if (event.getUser().getIdLong() != ownerId) {
            event.reply("Vous ne pouvez pas interagir avec ce menu, car vous n'êtes pas le propriétaire de l'interaction. Veuillez utiliser la commande `/stats` pour créer votre propre menu de statistiques.").setEphemeral(true).queue();
            return;
        }
        
        StatsMessage statsMessage = checkStatsMessage(event, ownerId);
        if (statsMessage == null) return;
        
        String buttonId = parts[3];
        switch (buttonId) {
            case "first" -> statsMessage.currentPage = 0;
            case "prev" -> statsMessage.previousPage();
            case "next" -> statsMessage.nextPage();
            case "last" -> statsMessage.currentPage = statsMessage.getLastPage();
        }
        
        event.editComponents(createGlobalStatsContainer(statsMessage, event.getGuild())).useComponentsV2().queue();
    }
    
    @Interaction(id = "stats:leaderboard:type:*")
    public void onLeaderboardTypeButton(ButtonInteractionEvent event) {
        String[] parts = event.getComponentId().split(":");
        if (parts.length < 4) {
            event.reply("ID de bouton invalide. Ce menu est obsolète. Veuillez utiliser la commande `/stats` pour créer un nouveau menu de statistiques.").setEphemeral(true).queue();
            return;
        }
        
        long ownerId = Long.parseLong(parts[4]);
        if (event.getUser().getIdLong() != ownerId) {
            event.reply("Vous ne pouvez pas interagir avec ce menu, car vous n'êtes pas le propriétaire de l'interaction. Veuillez utiliser la commande `/stats` pour créer votre propre menu de statistiques.").setEphemeral(true).queue();
            return;
        }
        
        StatsMessage statsMessage = checkStatsMessage(event, ownerId);
        if (statsMessage == null) return;
        
        String buttonId = parts[3];
        if (buttonId.equals("m") || buttonId.equals("v")) statsMessage.leaderboardType = buttonId.charAt(0);
        
        if (statsMessage.currentPage > statsMessage.getLastPage()) statsMessage.currentPage = statsMessage.getLastPage();
        
        event.editComponents(createGlobalStatsContainer(statsMessage, event.getGuild())).useComponentsV2().queue();
    }
    
    private StatsMessage checkStatsMessage(GenericInteractionCreateEvent event, long userId) {
        if (!(event instanceof IReplyCallback replyCallback)) return null;
        
        StatsMessage statsMessage = statsMessages.get(userId);
        if (statsMessage == null || Instant.now().isAfter(statsMessage.timestamp.plusSeconds(30 * 60))) {
            replyCallback.reply("Cette interaction a expiré. Veuillez réutiliser la commande `/stats` pour obtenir un nouveau menu.").setEphemeral(true).queue();
            statsMessages.remove(userId);
            return null;
        }
        
        return statsMessage;
    }

    private Container createUserStatsContainer(StatsMessage statsMessage, Member member, String guildName) {
        DiscordTimestamp timestampCreated = DiscordTimestamp.from(member.getTimeCreated());
        DiscordTimestamp timestampJoined = DiscordTimestamp.from(member.getTimeJoined());
        
        StringBuilder countingContent = new StringBuilder();
        if (statsMessage.config.countingChannelId != null) {
            countingContent.append(
                """
                ### 🔢 Comptage
                **%,d** nombre réussi
                **%,d** chaînes cassées
                **%.2f %%** de réussite
                """.formatted(statsMessage.countingMessages, statsMessage.chainBreaks, statsMessage.getSuccessRate(statsMessage.countingMessages, statsMessage.chainBreaks))
            );
        }
        if (statsMessage.config.countingSpecialChannelId != null || statsMessage.guildId == 1219005659194851389L) {
            countingContent.append("### ✨ Comptage spécial");
            if (statsMessage.countingSpecialMessagesLegacy > 0 || statsMessage.chainBreaksSpecialLegacy > 0)
                countingContent.append(" (+ ancien)");
            countingContent.append("\n");
            
            countingContent.append("**%,d** nombre réussi".formatted(statsMessage.countingSpecialMessages));
            if (statsMessage.countingSpecialMessagesLegacy > 0)
                countingContent.append(" (**+%,d**)".formatted(statsMessage.countingSpecialMessagesLegacy));
            countingContent.append("\n");
            
            countingContent.append("**%,d** chaînes cassées".formatted(statsMessage.chainBreaksSpecial));
            if (statsMessage.chainBreaksSpecialLegacy > 0)
                countingContent.append(" (**+%,d**)".formatted(statsMessage.chainBreaksSpecialLegacy));
            countingContent.append("\n");
            
            countingContent.append("**%.2f %%** de réussite".formatted(statsMessage.getSuccessRate(statsMessage.countingSpecialMessages, statsMessage.chainBreaksSpecial)));
            if (statsMessage.countingSpecialMessagesLegacy > 0)
                countingContent.append(" (**%.2f %%**)".formatted(statsMessage.getSuccessRate(statsMessage.countingSpecialMessagesLegacy, statsMessage.chainBreaksSpecialLegacy)));
        }
        
        return Container.of(
            Section.of(
                Thumbnail.fromUrl(member.getEffectiveAvatarUrl()),
                TextDisplay.ofFormat(
                    """
                    ## Statistiques de %s
                    -# Statistiques sur **%s**
                    """, member.getEffectiveName(), guildName
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
                statsMessage.messageCount, statsMessage.getVoiceTime()
            ),
            Separator.createDivider(Separator.Spacing.SMALL),
            
            TextDisplay.of(countingContent.toString()),
            Separator.createDivider(Separator.Spacing.LARGE),
            
            createNavigationRow(statsMessage)
        ).withAccentColor(statsMessage.color);
    }
    
    private Container createGlobalStatsContainer(StatsMessage statsMessage, Guild guild) {
        GuildConfiguration guildConfiguration = BotCache.getGuildConfiguration(guild.getIdLong());
        
        StringBuilder leaderboardContent = new StringBuilder();
        leaderboardContent.append("### 🏆 Classement des ");
        
        List<LeaderboardEntry> leaderboardEntries = new ArrayList<>(10);
        if (statsMessage.leaderboardType == 'm') {
            leaderboardEntries = Database.withExtension(MessageDao.class, dao -> dao.getMessageLeaderboard(guild.getIdLong(), 10, statsMessage.currentPage * 10));
            leaderboardContent.append("messages");
        }
        else if (statsMessage.leaderboardType == 'v') {
            leaderboardEntries = Database.withExtension(UserDao.class, dao -> dao.getVoiceLeaderboard(guild.getIdLong(), 10, statsMessage.currentPage * 10));
            leaderboardContent.append("temps vocal");
        }
        leaderboardContent.append(" (page %d/%d)\n".formatted(statsMessage.currentPage + 1, statsMessage.getLastPage() + 1));
        
        Button typeSwitch = Button.success("stats:leaderboard:type:" + (statsMessage.leaderboardType == 'm' ? 'v' : 'm') + ":" + statsMessage.userId, "Classement " + (statsMessage.leaderboardType == 'm' ? "temps vocal" : "messages"));
        
        for (int i = 0; i < leaderboardEntries.size(); i++) {
            LeaderboardEntry entry = leaderboardEntries.get(i);
            if (statsMessage.leaderboardType == 'm')
                leaderboardContent.append("%d. <@%d> · %,d messages\n".formatted(i + 1 + (statsMessage.currentPage * 10), entry.userId(), entry.count()));
            else if (statsMessage.leaderboardType == 'v')
                leaderboardContent.append("%d. <@%d> · %s\n".formatted(i + 1 + (statsMessage.currentPage * 10), entry.userId(), TimeUtils.formatTime(entry.count() / 1000)));
        }
        
        return Container.of(
            Section.of(
                Thumbnail.fromUrl(guild.getIconUrl()),
                TextDisplay.ofFormat(
                    """
                    ## Statistiques globales
                    -# Statistiques sur **%s**
                    """, guild.getName()
                )
            ),
            Separator.createDivider(Separator.Spacing.SMALL),
            
            TextDisplay.ofFormat(
                """
                ### 📊 Activité
                **%,d** messages
                **%s** en vocal
                """,
                guildConfiguration.totalMessages, TimeUtils.formatTime(guildConfiguration.totalVoiceTime / 1000)
            ),
            Separator.createDivider(Separator.Spacing.SMALL),
            
            TextDisplay.of(leaderboardContent.toString()),
            ActionRow.of(
                Button.secondary("stats:leaderboard:nav:first:" + statsMessage.userId, "⏪").withDisabled(statsMessage.currentPage == 0),
                Button.secondary("stats:leaderboard:nav:prev:" + statsMessage.userId, "◀️").withDisabled(statsMessage.currentPage <= 0),
                typeSwitch,
                Button.secondary("stats:leaderboard:nav:next:" + statsMessage.userId, "▶️").withDisabled(statsMessage.currentPage >= statsMessage.getLastPage()),
                Button.secondary("stats:leaderboard:nav:last:" + statsMessage.userId, "⏩").withDisabled(statsMessage.currentPage == statsMessage.getLastPage())
            ),
            Separator.createDivider(Separator.Spacing.LARGE),
            
            createNavigationRow(statsMessage)
        ).withAccentColor(guildConfiguration.accentColor);
    }
    
    private ActionRow createNavigationRow(StatsMessage statsMessage) {
        return ActionRow.of(
            Button.primary("stats:nav:self:" + statsMessage.userId, "Statistiques personnelles").withDisabled(statsMessage == null || !statsMessage.isGlobal),
            Button.primary("stats:nav:global:" + statsMessage.userId, "Statistiques globales").withDisabled(statsMessage == null || statsMessage.isGlobal)
        );
    }
}
