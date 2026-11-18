package dev.jojofr.joseta.commands;

import dev.jojofr.joseta.annotations.InteractionModule;
import dev.jojofr.joseta.annotations.types.interaction.Interaction;
import dev.jojofr.joseta.annotations.types.interaction.SlashCommandInteraction;
import dev.jojofr.joseta.database.entities.LeaderboardEntry;
import dev.jojofr.joseta.entities.GuildStatsCache;
import dev.jojofr.joseta.entities.GuildUserKey;
import dev.jojofr.joseta.entities.messages.StatsMessage;
import dev.jojofr.joseta.utils.BotCache;
import dev.jojofr.joseta.utils.DiscordTimestamp;
import dev.jojofr.joseta.utils.TimeUtils;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.components.thumbnail.Thumbnail;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.modals.Modal;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@InteractionModule
public class StatsCommand {
    public static final Map<GuildUserKey, StatsMessage> statsMessages = new ConcurrentHashMap<>();
    
    @SlashCommandInteraction(name = "stats", description = "Affiche les statistiques de l'utilisateur.")
    public void stats(SlashCommandInteractionEvent event) {
        GuildUserKey key = new GuildUserKey(event.getGuild(), event.getUser());
        StatsMessage userExist = statsMessages.get(key);
        if (userExist != null && Instant.now().isBefore(userExist.timestamp.plusSeconds(30 * 60))) {
            int remainingMinutes = (int) (30 - (Instant.now().getEpochSecond() - userExist.timestamp.getEpochSecond()) / 60);
            event.reply("Vous avez déjà une interaction de statistiques en cours. Veuillez utiliser le menu existant ou attendre "+ remainingMinutes +" minutes avant d'en créer un nouveau.").setEphemeral(true).queue();
            return;
        }
        event.deferReply().useComponentsV2().setAllowedMentions(Set.of()).queue(hook ->
            StatsMessage.createAsync(event.getGuild().getIdLong(), event.getUser(), 1307015890146955285L)
                .thenAccept(statsMessage -> {
                    statsMessages.put(key, statsMessage);
                    
                    Container statsContainer = createUserStatsContainer(statsMessage, event.getMember(), event.getGuild().getName());
                    hook.editOriginalComponents(statsContainer).useComponentsV2().queue();
                }).exceptionally(e -> {
                    hook.editOriginal("Une erreur est survenue lors de la récupération des statistiques. Veuillez réessayer plus tard.").queue();
                    return null;
                })
        );
    }
    
    @Interaction(id = "stats:nav:*")
    public void onNavigationButton(ButtonInteractionEvent event) {
        String[] parts = event.getComponentId().split(":");
        if (parts.length <= 3) {
            event.reply("ID de bouton invalide. Ce menu est obsolète. Veuillez utiliser la commande `/stats` pour créer un nouveau menu de statistiques.").setEphemeral(true).queue();
            return;
        }
        
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
        if (parts.length <= 4) {
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
            case "page" -> {
                Modal pageModal = Modal.create("stats:leaderboard:input:page:" + statsMessage.userId, "Aller à la page").addComponents(
                    Label.of(
                        "Définissez le numéro de page",
                        "La page que vous souhaitez afficher, entre 1 et "+ (statsMessage.getLastPage() + 1) +".",
                        TextInput.create("stats:leaderboard:nav:page:" + statsMessage.userId + ":input", TextInputStyle.SHORT)
                            .setPlaceholder("Entrez le numéro de page (1-"+ (statsMessage.getLastPage() + 1) +")")
                            .setMinLength(1)
                            .setMaxLength(5)
                            .setValue(String.valueOf(statsMessage.currentPage + 1))
                            .setRequired(true)
                            .build()
                )).build();
                event.replyModal(pageModal).queue();
            }
            case "next" -> statsMessage.nextPage();
            case "last" -> statsMessage.currentPage = statsMessage.getLastPage();
        }
        
        event.editComponents(createGlobalStatsContainer(statsMessage, event.getGuild())).useComponentsV2().queue();
    }
    
    @Interaction(id = "stats:leaderboard:type:*")
    public void onLeaderboardTypeButton(StringSelectInteractionEvent event) {
        String[] parts = event.getComponentId().split(":");
        if (parts.length <= 3) {
            event.reply("ID de bouton invalide. Ce menu est obsolète. Veuillez utiliser la commande `/stats` pour créer un nouveau menu de statistiques.").setEphemeral(true).queue();
            return;
        }
        
        long ownerId = Long.parseLong(parts[3]);
        if (event.getUser().getIdLong() != ownerId) {
            event.reply("Vous ne pouvez pas interagir avec ce menu, car vous n'êtes pas le propriétaire de l'interaction. Veuillez utiliser la commande `/stats` pour créer votre propre menu de statistiques.").setEphemeral(true).queue();
            return;
        }
        
        StatsMessage statsMessage = checkStatsMessage(event, ownerId);
        if (statsMessage == null) return;
        
        String buttonId = event.getSelectedOptions().getFirst().getValue();
        if (!buttonId.equals("m") && !buttonId.equals("v") && !buttonId.equals("c") && !buttonId.equals("s")) {
            event.reply("Type de classement invalide. Ce menu est obsolète. Veuillez utiliser la commande `/stats` pour créer un nouveau menu de statistiques.").setEphemeral(true).queue();
            return;
        }
        statsMessage.leaderboardType = buttonId.charAt(0);
        
        if (statsMessage.currentPage > statsMessage.getLastPage()) statsMessage.currentPage = statsMessage.getLastPage();
        
        event.editComponents(createGlobalStatsContainer(statsMessage, event.getGuild())).useComponentsV2().queue();
    }
    
    @Interaction(id = "stats:leaderboard:input:*")
    public void onLeaderboardPageInput(ModalInteractionEvent event) {
        String[] parts = event.getCustomId().split(":");
        if (parts.length <= 4) {
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
        
        String inputValue = event.getValue("stats:leaderboard:nav:page:" + statsMessage.userId + ":input").getAsString();
        int pageNumber;
        try {
            pageNumber = Integer.parseInt(inputValue);
        } catch (NumberFormatException e) {
            event.reply("Numéro de page invalide. Veuillez entrer un nombre entier entre 1 et " + (statsMessage.getLastPage() + 1) + ".").setEphemeral(true).queue();
            return;
        }
        
        if (pageNumber < 1 || pageNumber > statsMessage.getLastPage() + 1) {
            event.reply("Numéro de page invalide. Veuillez entrer un nombre entier entre 1 et " + (statsMessage.getLastPage() + 1) + ".").setEphemeral(true).queue();
            return;
        }
        
        statsMessage.currentPage = pageNumber - 1;
        event.editComponents(createGlobalStatsContainer(statsMessage, event.getGuild())).useComponentsV2().queue();
    }
    
    private StatsMessage checkStatsMessage(GenericInteractionCreateEvent event, long userId) {
        if (!(event instanceof IReplyCallback replyCallback)) return null;
        
        GuildUserKey key = new GuildUserKey(event.getGuild(), userId);
        StatsMessage statsMessage = statsMessages.get(key);
        if (statsMessage == null || Instant.now().isAfter(statsMessage.timestamp.plusSeconds(30 * 60))) {
            replyCallback.reply("Cette interaction a expiré. Veuillez réutiliser la commande `/stats` pour obtenir un nouveau menu.").setEphemeral(true).queue();
            statsMessages.remove(key);
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
                """.formatted(statsMessage.getCountingSuccess(), statsMessage.getCountingFail(), statsMessage.getSuccessRate(statsMessage.getCountingSuccess(), statsMessage.getCountingFail()))
            );
        }
        if (statsMessage.config.countingSpecialChannelId != null) {
            countingContent.append(
                """
                ### ✨ Comptage spécial
                **%,d** nombre réussi
                **%,d** chaînes cassées
                **%.2f %%** de réussite
                """.formatted(statsMessage.getCountingSpecialSuccess(), statsMessage.getCountingSpecialFail(), statsMessage.getSuccessRate(statsMessage.getCountingSpecialSuccess(), statsMessage.getCountingSpecialFail()))
            );
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
                -# Le temps de vocal est traqué uniquement depuis le 28 Juillet 2026
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
        GuildStatsCache guildStatsCache = BotCache.getGuildStatsCache(guild.getIdLong());
        Color accentColor = BotCache.getGuildConfiguration(guild.getIdLong()).accentColor;
        
        StringBuilder leaderboardContent = new StringBuilder();
        String leaderboardType = switch (statsMessage.leaderboardType) {
            case 'm' -> "messages";
            case 'v' -> "temps de vocal";
            case 'c' -> "comptage";
            case 's' -> "comptage spécial";
            default -> "inconnu";
        };
        leaderboardContent.append("### 🏆 Classement des ").append(leaderboardType).append('\n');
        if (statsMessage.leaderboardType == 'v') leaderboardContent.append("-# Le temps de vocal est traqué uniquement depuis le 28 Juillet 2026\n");
        
        StringSelectMenu typeSelect = StringSelectMenu.create("stats:leaderboard:type:" + statsMessage.userId)
            .addOption("Classement messages", "m", "Classement des messages")
            .addOption("Classement temps vocal", "v", "Classement du temps de vocal")
            .addOption("Classement comptage", "c", "Classement du comptage")
            .addOption("Classement comptage spécial", "s", "Classement du comptage spécial")
            .setPlaceholder("Sélectionnez le type de classement")
            .setDefaultValues(String.valueOf(statsMessage.leaderboardType))
            .build();
        
        int startIndex = statsMessage.currentPage * 10;
        int endIndex = startIndex + 10;
        List<LeaderboardEntry> pageEntry = switch (statsMessage.leaderboardType) {
            case 'm' -> guildStatsCache.getMessageLeaderboard(startIndex, endIndex);
            case 'v' -> guildStatsCache.getVoiceLeaderboard(startIndex, endIndex);
            case 'c' -> guildStatsCache.getCountingLeaderboard(startIndex, endIndex);
            case 's' -> guildStatsCache.getCountingSpecialLeaderboard(startIndex, endIndex);
            default -> List.of();
        };
        for (int i = 0; i < pageEntry.size(); i++) {
            LeaderboardEntry entry = pageEntry.get(i);
            if (statsMessage.leaderboardType == 'm')
                leaderboardContent.append("%d. <@%d> · %,d messages\n".formatted(i + 1 + (statsMessage.currentPage * 10), entry.id(), entry.count()));
            else if (statsMessage.leaderboardType == 'v')
                leaderboardContent.append("%d. <@%d> · %s\n".formatted(i + 1 + (statsMessage.currentPage * 10), entry.id(), TimeUtils.formatTime(entry.count() / 1000)));
        }
        
        return Container.of(
            Section.of(
                Thumbnail.fromUrl(guild.getIconUrl()),
                TextDisplay.ofFormat(
                    """
                    ## Statistiques globales
                    -# Statistiques sur **%s** (Prochaine mise à jour %s)
                    """, guild.getName(), DiscordTimestamp.from(guildStatsCache.timestamp.plusSeconds(12 * 60 * 60)).relative()
                )
            ),
            Separator.createDivider(Separator.Spacing.SMALL),
            
            TextDisplay.ofFormat(
                """
                ### 📊 Activité
                -# Le temps de vocal est traqué uniquement depuis le 28 Juillet 2026
                **%,d** messages
                **%s** en vocal
                """,
                guildStatsCache.totalMessages, TimeUtils.formatTime(guildStatsCache.totalVoiceTime / 1000)
            ),
            Separator.createDivider(Separator.Spacing.SMALL),
            
            TextDisplay.of(leaderboardContent.toString()),
            ActionRow.of(
                Button.secondary("stats:leaderboard:nav:first:" + statsMessage.userId, "⏪").withDisabled(statsMessage.currentPage == 0),
                Button.secondary("stats:leaderboard:nav:prev:" + statsMessage.userId, "◀️").withDisabled(statsMessage.currentPage <= 0),
                Button.primary("stats:leaderboard:nav:page:" + statsMessage.userId, "Page " + (statsMessage.currentPage + 1) + "/" + (statsMessage.getLastPage() + 1)),
                Button.secondary("stats:leaderboard:nav:next:" + statsMessage.userId, "▶️").withDisabled(statsMessage.currentPage >= statsMessage.getLastPage()),
                Button.secondary("stats:leaderboard:nav:last:" + statsMessage.userId, "⏩").withDisabled(statsMessage.currentPage == statsMessage.getLastPage())
            ),
            ActionRow.of(typeSelect),
            Separator.createDivider(Separator.Spacing.LARGE),
            
            createNavigationRow(statsMessage)
        ).withAccentColor(accentColor);
    }
    
    private ActionRow createNavigationRow(StatsMessage statsMessage) {
        return ActionRow.of(
            Button.primary("stats:nav:self:" + statsMessage.userId, "Statistiques personnelles").withDisabled(!statsMessage.isGlobal),
            Button.primary("stats:nav:global:" + statsMessage.userId, "Statistiques globales").withDisabled(statsMessage.isGlobal)
        );
    }
}
