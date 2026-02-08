package dev.jojofr.joseta.commands;

import dev.jojofr.joseta.annotations.InteractionModule;
import dev.jojofr.joseta.annotations.types.ButtonInteraction;
import dev.jojofr.joseta.annotations.types.SelectMenuInteraction;
import dev.jojofr.joseta.annotations.types.SlashCommandInteraction;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.entities.ConfigurationMessage;
import dev.jojofr.joseta.utils.BotCache;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@InteractionModule
public class ConfigurationCommand {
    private static final Map<Long, ConfigurationMessage> configurationMessages = new HashMap<>();
    
    
    // A configuration menu using the new Components V2 system
    @SlashCommandInteraction(name = "config", description = "Configure les paramètres du bot.", permissions = Permission.MANAGE_SERVER)
    public void config(SlashCommandInteractionEvent event) {
        event.replyComponents(createMainMenuContainer(null)).useComponentsV2().queue(
            hook -> configurationMessages.put(hook.getCallbackResponse().getMessage().getIdLong(), new ConfigurationMessage(event.getGuild().getIdLong(), Instant.now()))
        );
    }
    
    // @Option(description = "Activer ou désactiver les réponses automatiques.") Boolean enabled
    @ButtonInteraction(id = "config:cat_autores")
    public void onConfigAutoResponseButton(ButtonInteractionEvent event) {
        ConfigurationMessage configurationMessage = checkConfigurationMessage(event, event.getMessageIdLong());
        if (configurationMessage == null) return;
        
        event.editComponents(createAutoResponseMenuContainer(configurationMessage)).useComponentsV2().queue();
    }
    
    @ButtonInteraction(id = "config:cat_counting")
    public void onConfigCountingButton(ButtonInteractionEvent event) {
        ConfigurationMessage configurationMessage = checkConfigurationMessage(event, event.getMessageIdLong());
        if (configurationMessage == null) return;
        
        event.editComponents(createCountingMenuContainer(configurationMessage)).useComponentsV2().queue();
    }
    
    @ButtonInteraction(id = "config:cat_markov")
    public void onConfigMarkovButton(ButtonInteractionEvent event) {
        ConfigurationMessage configurationMessage = checkConfigurationMessage(event, event.getMessageIdLong());
        if (configurationMessage == null) return;
        
        event.editComponents(createMarkovMenuContainer(configurationMessage)).useComponentsV2().queue();
    }
    
    @ButtonInteraction(id = "config:cat_moderation")
    public void onConfigModerationButton(ButtonInteractionEvent event) {
        ConfigurationMessage configurationMessage = checkConfigurationMessage(event, event.getMessageIdLong());
        if (configurationMessage == null) return;
        
        event.editComponents(createModerationMenuContainer(configurationMessage)).useComponentsV2().queue();
    }
    
    @ButtonInteraction(id = "config:cat_welcome")
    public void onConfigWelcomeButton(ButtonInteractionEvent event) {
        ConfigurationMessage configurationMessage = checkConfigurationMessage(event, event.getMessageIdLong());
        if (configurationMessage == null) return;
        
        event.editComponents(createWelcomeMenuContainer(configurationMessage)).useComponentsV2().queue();
    }
    
    
    @ButtonInteraction(id = "config:menu_back")
    public void onConfigBackButton(ButtonInteractionEvent event) {
        ConfigurationMessage configurationMessage = checkConfigurationMessage(event, event.getMessageIdLong());
        if (configurationMessage == null) return;
        
        configurationMessage.isMainMenu = true;
        event.editComponents(createMainMenuContainer(configurationMessage)).useComponentsV2().queue();
    }
    
    @ButtonInteraction(id = "config:save")
    public void onConfigSaveButton(ButtonInteractionEvent event) {
        ConfigurationMessage configurationMessage = checkConfigurationMessage(event, event.getMessageIdLong());
        if (configurationMessage == null || !configurationMessage.hasChanged) return;
        
        Database.createOrUpdate(configurationMessage.configuration);
        BotCache.guildConfigurations.put(configurationMessage.configuration.guildId, configurationMessage.configuration);
        configurationMessage.hasChanged = false;
        
        event.reply("La configuration du serveur a été enregistrée avec succès.").setEphemeral(true).queue();
    }
    
    
    private ConfigurationMessage checkConfigurationMessage(GenericComponentInteractionCreateEvent event, long id) {
        ConfigurationMessage configurationMessage = configurationMessages.get(id);
        if (configurationMessage == null || Instant.now().isAfter(configurationMessage.timestamp.plusSeconds(15 * 60))) {
            event.reply("Cette interaction a expiré. Veuillez réutiliser la commande pour obtenir un nouveau menu de configuration.").setEphemeral(true).queue();
            
            event.getMessage().editMessageComponents(TextDisplay.of("⚠️ Ce menu de configuration a expiré. Veuillez réutiliser la commande pour obtenir un nouveau menu."))
                .useComponentsV2().queue();
            configurationMessages.remove(id);
            return null;
        }
        
        configurationMessage.isMainMenu = false;
        return configurationMessage;
    }
    
    
    private Container createMainMenuContainer(ConfigurationMessage configurationMessage) {
        return Container.of(
            TextDisplay.of("# Configuration"),
            TextDisplay.of("Configuration du bot - Choisissez une catégorie à configurer :"),
            
            Section.of(
                Button.primary("config:cat_autores", "Configurer"), TextDisplay.of("### Réponse automatique"),
                TextDisplay.of("-# Permet de configurer les paramètres liés au système de réponses automatiques, qui envoie des messages automatiques en réponse à certains événements ou messages spécifiques.")),
            Section.of(
                Button.primary("config:cat_counting", "Configurer"), TextDisplay.of("### Comptage"),
                TextDisplay.of("-# Permet de configurer les paramètres liés au système de comptage), qui gère les salons de comptage et les règles associées.")),
            Section.of(
                Button.primary("config:cat_markov", "Configurer"), TextDisplay.of("### Markov"),
                TextDisplay.of("-# Permet de configurer les paramètres liés au système de génération de messages de Markov, qui génère des messages aléatoires basés sur les messages précédents du serveur.")),
            Section.of(
                Button.primary("config:cat_moderation", "Configurer"), TextDisplay.of("### Modération"),
                TextDisplay.of("-# Permet de configurer les paramètres liés aux fonctionnalités de modération, qui aident à maintenir l'ordre et la sécurité sur le serveur.")),
            Section.of(
                Button.primary("config:cat_welcome", "Configurer"), TextDisplay.of("### Bienvenue"),
                TextDisplay.of("-# Permet de configurer les paramètres liés au système de bienvenue, qui gère les messages de bienvenue, les rôles attribués aux nouveaux membres, etc.")),
            
            createBottomRow(configurationMessage)
        );
    }
    
    private Container createAutoResponseMenuContainer(ConfigurationMessage configurationMessage) {
        return Container.of(
            TextDisplay.of("# Configuration - Réponses automatique"),
            
            createToggleSection("Système de réponses automatique",
                "Permet d'activer ou de désactiver les réponses automatiques. Lorsque les réponses automatiques sont désactivées, les autres paramètres de réponse automatique seront ignorés.",
                "config:cat_autores:toggle", configurationMessage.configuration.autoResponseEnabled),
            
            createBottomRow(configurationMessage)
        );
    }
    
    private Container createCountingMenuContainer(ConfigurationMessage configurationMessage) {
        EntitySelectMenu channelSelectMenu = EntitySelectMenu.create("config:cat_counting:channel_select", EntitySelectMenu.SelectTarget.CHANNEL)
            .setPlaceholder("Sélectionnez un salon de comptage")
            .setChannelTypes(ChannelType.TEXT)
            .setDefaultValues(EntitySelectMenu.DefaultValue.channel(configurationMessage.configuration.countingChannelId))
            .build();
        
        return Container.of(
            TextDisplay.of("# Configuration - Comptage"),
            
            createToggleSection("Système de comptage",
                "Permet d'activer ou de désactiver le système de comptage. Lorsque le système de comptage est désactivé, les autres paramètres de comptage seront ignorés.",
                "config:cat_counting:toggle", configurationMessage.configuration.countingEnabled),
            
             createToggleSection("Commentaires de comptage",
                "Permet d'activer ou de désactiver les commentaires de comptage. Lorsque les commentaires de comptage sont activés, le bot enverra un message de commentaire à chaque fois qu'un nombre est correctement compté ou lorsqu'une erreur de comptage est commise.",
                "config:cat_counting:toggle_comments", configurationMessage.configuration.countingCommentsEnabled),
            
             createToggleSection("Pénalité en cas d'erreur de comptage",
                "Permet d'activer ou de désactiver la pénalité en cas d'erreur de comptage. Lorsque la pénalité en cas d'erreur de comptage est activée, les membres qui commettent une erreur de comptage seront temporairement empêchés de compter pendant une durée déterminée.",
                "config:cat_counting:toggle_penalty", configurationMessage.configuration.countingPenaltyEnabled),
            
            TextDisplay.of("### Salon de comptage"),
            TextDisplay.of("-# Permet de choisir le salon où les nombres doivent être comptés. Lorsque ce salon est sélectionné, le bot surveillera les messages envoyés dans ce salon pour vérifier s'ils contiennent des nombres correctement comptés et appliquera les règles de comptage en conséquence."),
            ActionRow.of(channelSelectMenu),
            
            createBottomRow(configurationMessage)
        );
    }
    
    // @Option(description = "Le membre ou rôle à ajouter à la blacklist pour la génération de messages de Markov.") IMentionable addMentionableBlacklist,
    // @Option(description = "Le membre ou rôle à retirer de la blacklist pour la génération de messages de Markov.") IMentionable removeMentionableBlacklist,
    // @Option(description = "Le salon, thread ou catégorie à ajouter à la blacklist pour la génération de messages de Markov.") GuildChannel addChannelBlacklist,
    // @Option(description = "Le salon, thread ou catégorie à retirer de la blacklist pour la génération de messages de Markov.") GuildChannel removeChannelBlacklist
    private Container createMarkovMenuContainer(ConfigurationMessage configurationMessage) {
        return Container.of(
            TextDisplay.of("# Configuration - Markov"),
            
            createToggleSection("Génération de messages de Markov",
                "Permet d'activer ou de désactiver la génération de messages de Markov. Lorsque la génération de messages de Markov est désactivée, les autres paramètres de Markov seront ignorés.",
                "config:cat_markov:toggle", configurationMessage.configuration.markovEnabled),
            
            
            TextDisplay.of("### Blacklist de Markov"),
            TextDisplay.of("En développement."),
            
            createBottomRow(configurationMessage)
        );
    }
    
    private Container createModerationMenuContainer(ConfigurationMessage configurationMessage) {
        return Container.of(
            TextDisplay.of("# Configuration - Modération"),
            
            createToggleSection("Système de modération",
                "Permet d'activer ou de désactiver les fonctionnalités de modération. Lorsque les fonctionnalités de modération sont désactivées, les autres paramètres de modération seront ignorés.",
                "config:cat_moderation:toggle", configurationMessage.configuration.moderationEnabled),
            
            createBottomRow(configurationMessage)
        );
    }
    
    private Container createWelcomeMenuContainer(ConfigurationMessage configurationMessage) {
        EntitySelectMenu channelSelectMenu = EntitySelectMenu.create("config:cat_welcome:channel_select", EntitySelectMenu.SelectTarget.CHANNEL)
            .setPlaceholder("Sélectionnez un salon de bienvenue")
            .setChannelTypes(ChannelType.TEXT)
            .setDefaultValues(EntitySelectMenu.DefaultValue.channel(configurationMessage.configuration.welcomeChannelId))
            .build();
        
        EntitySelectMenu joinRoleSelectMenu = EntitySelectMenu.create("config:cat_welcome:join_role_select", EntitySelectMenu.SelectTarget.ROLE)
            .setPlaceholder("Sélectionnez un rôle à attribuer aux nouveaux membres")
            .setDefaultValues(EntitySelectMenu.DefaultValue.role(configurationMessage.configuration.joinRoleId))
            .build();
        
        EntitySelectMenu joinBotRoleSelectMenu = EntitySelectMenu.create("config:cat_welcome:join_bot_role_select", EntitySelectMenu.SelectTarget.ROLE)
            .setPlaceholder("Sélectionnez un rôle à attribuer aux nouveaux bots")
            .setDefaultValues(EntitySelectMenu.DefaultValue.role(configurationMessage.configuration.joinBotRoleId))
            .build();
        
        EntitySelectMenu verifiedRoleSelectMenu = EntitySelectMenu.create("config:cat_welcome:verified_role_select", EntitySelectMenu.SelectTarget.ROLE)
            .setPlaceholder("Sélectionnez un rôle à attribuer aux membres vérifiés")
            .setDefaultValues(EntitySelectMenu.DefaultValue.role(configurationMessage.configuration.verifiedRoleId))
            .build();
        
        return Container.of(
            TextDisplay.of("# Configuration - Bienvenue"),
            
            createToggleSection("Système de bienvenue",
                "Permet d'activer ou de désactiver le système de bienvenue. Lorsque le système de bienvenue est désactivé, les autres paramètres de bienvenue seront ignorés.",
                "config:cat_welcome:toggle", configurationMessage.configuration.welcomeEnabled),
            
            TextDisplay.of("### Salon de bienvenue"),
            TextDisplay.of("-# Permet de choisir le salon où les messages de bienvenue seront envoyés."),
            ActionRow.of(channelSelectMenu),
            
            createToggleSection("Image de bienvenue",
                "Permet d'activer ou de désactiver l'envoi d'images de bienvenue. Lorsque cette option est activée, " +
                    "le bot enverra une image de bienvenue personnalisée avec le message de bienvenue.",
                "config:cat_welcome:toggle_image", configurationMessage.configuration.welcomeImageEnabled),
            
            TextDisplay.of("### Message de bienvenue"),
            Section.of(
                Button.primary("config:cat_welcome:edit_join_message", "Modifier le message de bienvenue"),
                TextDisplay.of("-# Permet de modifier le message qui sera envoyé lorsqu'un membre rejoint le serveur. " +
                    "Le message peut contenir les variables suivantes : {user} (mention du membre), {user_name} (nom du membre), {server} (nom du serveur).")
            ),
            Section.of(
                Button.primary("config:cat_welcome:edit_leave_message", "Modifier le message de départ"),
                TextDisplay.of("-# Permet de modifier le message qui sera envoyé lorsqu'un membre quitte le serveur. " +
                    "Le message peut contenir les variables suivantes : {user} (mention du membre), {user_name} (nom du membre), {server} (nom du serveur).")
            ),
            
            TextDisplay.of("### Rôles de bienvenue"),
            TextDisplay.of("-# Permet de choisir les rôles à attribuer aux nouveaux membres, aux nouveaux bots et aux membres vérifiés. Lorsque ces rôles sont sélectionnés, le bot les attribuera automatiquement aux membres concernés lorsqu'ils rejoindront le serveur ou seront vérifiés."),
            TextDisplay.of("- Rôle pour les nouveaux membres : attribué à tous les membres qui rejoignent le serveur, sauf s'ils sont des bots."),
            ActionRow.of(joinRoleSelectMenu),
            TextDisplay.of("- Rôle pour les nouveaux bots : attribué à tous les membres qui rejoignent le serveur et qui sont des bots."),
            ActionRow.of(joinBotRoleSelectMenu),
            TextDisplay.of("- Rôle pour les membres vérifiés : attribué à tous les membres qui sont vérifiés."),
            ActionRow.of(verifiedRoleSelectMenu),
            
            createBottomRow(configurationMessage)
        );
    }
    
    @ButtonInteraction(id = "config:cat_welcome:toggle") public void onConfigWelcomeToggleButton(ButtonInteractionEvent event) { onToggleButton(event); }
    @ButtonInteraction(id = "config:cat_welcome:toggle_image") public void onConfigWelcomeToggleImageButton(ButtonInteractionEvent event) { onToggleButton(event); }
    @ButtonInteraction(id = "config:cat_counting:toggle") public void onConfigCountingToggleButton(ButtonInteractionEvent event) { onToggleButton(event); }
    @ButtonInteraction(id = "config:cat_counting:toggle_comments") public void onConfigCountingToggleCommentsButton(ButtonInteractionEvent event) { onToggleButton(event); }
    @ButtonInteraction(id = "config:cat_counting:toggle_penalty") public void onConfigCountingTogglePenaltyButton(ButtonInteractionEvent event) { onToggleButton(event); }
    @ButtonInteraction(id = "config:cat_markov:toggle") public void onConfigMarkovToggleButton(ButtonInteractionEvent event) { onToggleButton(event); }
    @ButtonInteraction(id = "config:cat_moderation:toggle") public void onConfigModerationToggleButton(ButtonInteractionEvent event) { onToggleButton(event); }
    @ButtonInteraction(id = "config:cat_autores:toggle") public void onConfigAutoResponseToggleButton(ButtonInteractionEvent event) { onToggleButton(event); }
    
    private void onToggleButton(ButtonInteractionEvent event) {
        ConfigurationMessage configurationMessage = checkConfigurationMessage(event, event.getMessageIdLong());
        if (configurationMessage == null) return;
        
        // Toggle the relevant setting based on the button ID
        String buttonId = event.getComponentId();
        switch (buttonId) {
            case "config:cat_welcome:toggle" -> configurationMessage.configuration.setWelcomeEnabled(!configurationMessage.configuration.welcomeEnabled);
            case "config:cat_welcome:toggle_image" -> configurationMessage.configuration.setWelcomeImageEnabled(!configurationMessage.configuration.welcomeImageEnabled);
            case "config:cat_counting:toggle" -> configurationMessage.configuration.setCountingEnabled(!configurationMessage.configuration.countingEnabled);
            case "config:cat_counting:toggle_comments" -> configurationMessage.configuration.setCountingCommentsEnabled(!configurationMessage.configuration.countingCommentsEnabled);
            case "config:cat_counting:toggle_penalty" -> configurationMessage.configuration.setCountingPenaltyEnabled(!configurationMessage.configuration.countingPenaltyEnabled);
            case "config:cat_markov:toggle" -> configurationMessage.configuration.setMarkovEnabled(!configurationMessage.configuration.markovEnabled);
            case "config:cat_moderation:toggle" -> configurationMessage.configuration.setModerationEnabled(!configurationMessage.configuration.moderationEnabled);
            case "config:cat_autores:toggle" -> configurationMessage.configuration.setAutoResponseEnabled(!configurationMessage.configuration.autoResponseEnabled);
        }
        
        configurationMessage.hasChanged = true;
        event.editComponents(switch (buttonId) {
            case "config:cat_welcome:toggle", "config:cat_welcome:toggle_image" -> createWelcomeMenuContainer(configurationMessage);
            case "config:cat_counting:toggle", "config:cat_counting:toggle_comments", "config:cat_counting:toggle_penalty" -> createCountingMenuContainer(configurationMessage);
            case "config:cat_markov:toggle" -> createMarkovMenuContainer(configurationMessage);
            case "config:cat_moderation:toggle" -> createModerationMenuContainer(configurationMessage);
            case "config:cat_autores:toggle" -> createAutoResponseMenuContainer(configurationMessage);
            default -> null;
        }).useComponentsV2().queue();
    }
    
    @SelectMenuInteraction(id = "config:cat_welcome:channel_select") public void onConfigWelcomeChannelSelect(EntitySelectInteractionEvent event) { onSelectMenu(event); }
    @SelectMenuInteraction(id = "config:cat_welcome:join_role_select") public void onConfigWelcomeJoinRoleSelect(EntitySelectInteractionEvent event) { onSelectMenu(event); }
    @SelectMenuInteraction(id = "config:cat_welcome:join_bot_role_select") public void onConfigWelcomeJoinBotRoleSelect(EntitySelectInteractionEvent event) { onSelectMenu(event); }
    @SelectMenuInteraction(id = "config:cat_welcome:verified_role_select") public void onConfigWelcomeVerifiedRoleSelect(EntitySelectInteractionEvent event) { onSelectMenu(event); }
    @SelectMenuInteraction(id = "config:cat_counting:channel_select") public void onConfigCountingChannelSelect(EntitySelectInteractionEvent event) { onSelectMenu(event); }
    
    private void onSelectMenu(EntitySelectInteractionEvent event) {
        ConfigurationMessage configurationMessage = checkConfigurationMessage(event, event.getMessageIdLong());
        if (configurationMessage == null) return;
        
        String menuId = event.getComponentId();
        List<IMentionable> selectedValues = event.getValues();
        Long selectedId = selectedValues.isEmpty() ? null : selectedValues.getFirst().getIdLong();
        switch (menuId) {
            case "config:cat_welcome:channel_select" -> configurationMessage.configuration.setWelcomeChannelId(selectedId);
            case "config:cat_welcome:join_role_select" -> configurationMessage.configuration.setJoinRoleId(selectedId);
            case "config:cat_welcome:join_bot_role_select" -> configurationMessage.configuration.setJoinBotRoleId(selectedId);
            case "config:cat_welcome:verified_role_select" -> configurationMessage.configuration.setVerifiedRoleId(selectedId);
            case "config:cat_counting:channel_select" -> configurationMessage.configuration.setCountingChannelId(selectedId);
        }
        
        configurationMessage.hasChanged = true;
        event.editComponents(switch (menuId) {
            case "config:cat_welcome:channel_select", "config:cat_welcome:join_role_select", "config:cat_welcome:join_bot_role_select", "config:cat_welcome:verified_role_select" -> createWelcomeMenuContainer(configurationMessage);
            case "config:cat_counting:channel_select" -> createCountingMenuContainer(configurationMessage);
            default -> null;
        }).useComponentsV2().queue();
    }
    
    private ActionRow createBottomRow(ConfigurationMessage configurationMessage) {
        return ActionRow.of(
            Button.primary("config:menu_back", "Retour au menu principal").withDisabled(configurationMessage == null || configurationMessage.isMainMenu),
            Button.success("config:save", "Enregistrer les modifications").withDisabled(configurationMessage == null || !configurationMessage.hasChanged)
        );
    }
    
    private Section createToggleSection(String label, String description, String id, boolean enabled) {
        return Section.of(
            Button.of(
                enabled ? ButtonStyle.SUCCESS : ButtonStyle.DANGER,
                id,
                enabled ? "Activer" : "Désactiver",
                enabled ? BotCache.CHECK_EMOJI : BotCache.CROSS_EMOJI
            ),
            TextDisplay.of("### " + label),
            TextDisplay.of("-# " + description)
        );
    }
}
