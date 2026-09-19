package dev.jojofr.joseta.commands;

import dev.jojofr.joseta.annotations.InteractionModule;
import dev.jojofr.joseta.annotations.types.interaction.Interaction;
import dev.jojofr.joseta.annotations.types.interaction.SlashCommandInteraction;
import dev.jojofr.joseta.entities.StatsMessage;
import dev.jojofr.joseta.utils.DiscordTimestamp;
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
import java.util.Map;
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
        
        event.deferReply().useComponentsV2().queue(hook -> {
            Container statsContainer = createUserStatsContainer(statsMessage, event.getMember(), event.getGuild().getName());
            hook.editOriginalComponents(statsContainer).useComponentsV2().queue();
        });
    }
    
    @Interaction(id = "stats:nav:*")
    public void onNavigationButton(ButtonInteractionEvent event) {
        StatsMessage statsMessage = checkStatsMessage(event, event.getUser().getIdLong());
        if (statsMessage == null) return;
        
        Container container = null;
        String buttonId = event.getComponentId();
        if (buttonId.equals("stats:nav:self")) {
            statsMessage.isGlobal = false;
            container = createUserStatsContainer(statsMessage, event.getMember(), event.getGuild().getName());
        } else if (buttonId.equals("stats:nav:global")) {
            statsMessage.isGlobal = true;
            container = createGlobalStatsContainer(statsMessage, event.getGuild());
        }
        
        event.editComponents(container).useComponentsV2().queue();
    }
    
    private StatsMessage checkStatsMessage(GenericInteractionCreateEvent event, long userId) {
        if (!(event instanceof IReplyCallback replyCallback)) return null;
        
        StatsMessage statsMessage = statsMessages.get(userId);
        if (statsMessage == null || Instant.now().isAfter(statsMessage.timestamp.plusSeconds(30 * 60))) {
            replyCallback.reply("Cette interaction a expiré. Veuillez réutiliser la commande pour obtenir un nouveau menu.").setEphemeral(true).queue();
            statsMessages.remove(userId);
            return null;
        }
        
        return statsMessage;
    }

    private Container createUserStatsContainer(StatsMessage statsMessage, Member member, String guildName) {
        DiscordTimestamp timestampCreated = DiscordTimestamp.from(member.getTimeCreated());
        DiscordTimestamp timestampJoined = DiscordTimestamp.from(member.getTimeJoined());
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
                statsMessage.countingMessages, statsMessage.chainBreaks, statsMessage.getSuccessRate(statsMessage.countingMessages, statsMessage.chainBreaks),
                statsMessage.countingSpecialMessages, statsMessage.chainBreaksSpecial, statsMessage.getSuccessRate(statsMessage.countingSpecialMessages, statsMessage.chainBreaksSpecial)
            ),
            
            createNavigationRow(statsMessage)
        ).withAccentColor(statsMessage.color);
    }
    
    private Container createGlobalStatsContainer(StatsMessage statsMessage, Guild guild) {
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
            
            createNavigationRow(statsMessage)
        );
    }
    
    private ActionRow createNavigationRow(StatsMessage statsMessage) {
        return ActionRow.of(
            Button.primary("stats:nav:self", "Statistiques personnelles").withDisabled(statsMessage == null || !statsMessage.isGlobal),
            Button.primary("stats:nav:global", "Statistiques globales").withDisabled(statsMessage == null || statsMessage.isGlobal)
        );
    }
}
