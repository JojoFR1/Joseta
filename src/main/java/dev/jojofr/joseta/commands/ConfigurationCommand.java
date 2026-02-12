package dev.jojofr.joseta.commands;

import dev.jojofr.joseta.annotations.InteractionModule;
import dev.jojofr.joseta.annotations.types.ButtonInteraction;
import dev.jojofr.joseta.annotations.types.ModalInteraction;
import dev.jojofr.joseta.annotations.types.SelectMenuInteraction;
import dev.jojofr.joseta.annotations.types.SlashCommandInteraction;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.entities.ConfigurationMessage;
import dev.jojofr.joseta.events.misc.CountingChannel;
import dev.jojofr.joseta.utils.BotCache;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.modals.Modal;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@InteractionModule
public class ConfigurationCommand {
    private static final Map<Long, ConfigurationMessage> configurationMessages = new HashMap<>();
    
    // TODO need to check if user has permissions, to avoid random people using this menu.
    // TODO add a reset button?
    @SlashCommandInteraction(name = "config", description = "Configure les paramètres du bot.", permissions = Permission.MANAGE_SERVER)
    public void config(SlashCommandInteractionEvent event) {
        event.replyComponents(createMainMenuContainer(null)).useComponentsV2().queue(
            hook -> configurationMessages.put(hook.getCallbackResponse().getMessage().getIdLong(), new ConfigurationMessage(event.getGuild().getIdLong(), Instant.now()))
        );
    }
    
    @ButtonInteraction(id = "config:cat_autores") public void onConfigAutoResponseButton(ButtonInteractionEvent event) { onCategoryButton(event); }
    @ButtonInteraction(id = "config:cat_counting") public void onConfigCountingButton(ButtonInteractionEvent event) { onCategoryButton(event); }
    @ButtonInteraction(id = "config:cat_markov") public void onConfigMarkovButton(ButtonInteractionEvent event) { onCategoryButton(event); }
    @ButtonInteraction(id = "config:cat_moderation") public void onConfigModerationButton(ButtonInteractionEvent event) { onCategoryButton(event); }
    @ButtonInteraction(id = "config:cat_welcome") public void onConfigWelcomeButton(ButtonInteractionEvent event) { onCategoryButton(event); }
    @ButtonInteraction(id = "config:menu_back") public void onConfigBackButton(ButtonInteractionEvent event) { onCategoryButton(event); }
    private void onCategoryButton(ButtonInteractionEvent event) {
        ConfigurationMessage configurationMessage = checkConfigurationMessage(event, event.getMessageIdLong());
        if (configurationMessage == null) return;
        
        Container container;
        String buttonId = event.getComponentId();
        configurationMessage.isMainMenu = buttonId.equals("config:menu_back");
        switch (buttonId) {
            case "config:cat_autores" -> container = createAutoResponseMenuContainer(configurationMessage);
            case "config:cat_counting" -> container = createCountingMenuContainer(configurationMessage);
            case "config:cat_markov" -> container = createMarkovMenuContainer(configurationMessage);
            case "config:cat_moderation" -> container = createModerationMenuContainer(configurationMessage);
            case "config:cat_welcome" -> container = createWelcomeMenuContainer(configurationMessage);
            case "config:menu_back" -> container = createMainMenuContainer(configurationMessage);
            default -> container = null;
        }
        
        event.editComponents(container).useComponentsV2().queue();
    }
    
    @ButtonInteraction(id = "config:save")
    public void onConfigSaveButton(ButtonInteractionEvent event) {
        ConfigurationMessage configurationMessage = checkConfigurationMessage(event, event.getMessageIdLong());
        if (configurationMessage == null || !configurationMessage.hasChanged) {
            event.reply("Aucune modification à enregistrer.").setEphemeral(true).queue();
            return;
        };
        
        Database.createOrUpdate(configurationMessage.configuration);
        BotCache.guildConfigurations.put(configurationMessage.configuration.guildId, configurationMessage.configuration);
        
        configurationMessage.hasChanged = false;
        configurationMessage.isMainMenu = true;
        
        event.reply("La configuration du serveur a été enregistrée avec succès.").setEphemeral(true).queue();
        event.getMessage().editMessageComponents(createMainMenuContainer(configurationMessage)).useComponentsV2().queue();
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
    
    @ButtonInteraction(id = "config:cat_counting:reset_number") public void onConfigCountingResetNumberButton(ButtonInteractionEvent event) { onResetButton(event); }
    @ButtonInteraction(id = "config:cat_counting:reset_author") public void onConfigCountingResetAuthorButton(ButtonInteractionEvent event) { onResetButton(event); }
    private void onResetButton(ButtonInteractionEvent event) {
        ConfigurationMessage configurationMessage = checkConfigurationMessage(event, event.getMessageIdLong());
        if (configurationMessage == null) return;
        
        String buttonId = event.getComponentId();
        switch (buttonId) {
            case "config:cat_counting:reset_number" -> {
                CountingChannel.lastNumber = 0;
                event.reply("Le dernier nombre du salon de comptage a été réinitialisé à 0.").setEphemeral(true).queue();
            }
            case "config:cat_counting:reset_author" -> {
                CountingChannel.lastAuthorId = -1L;
                event.reply("Le dernier auteur dans comptage a été réinitialiser.").setEphemeral(true).queue();
            }
        }
    }
    
    @ButtonInteraction(id = "config:cat_counting:set_number") public void onConfigCountingSetNumberButton(ButtonInteractionEvent event) { onEditMessageButton(event); }
    @ButtonInteraction(id = "config:cat_moderation:edit_rules") public void onConfigModerationEditRulesButton(ButtonInteractionEvent event) { onEditMessageButton(event); }
    @ButtonInteraction(id = "config:cat_welcome:edit_join_message") public void onConfigWelcomeEditJoinMessageButton(ButtonInteractionEvent event) { onEditMessageButton(event); }
    @ButtonInteraction(id = "config:cat_welcome:edit_leave_message") public void onConfigWelcomeEditLeaveMessageButton(ButtonInteractionEvent event) { onEditMessageButton(event); }
    private void onEditMessageButton(ButtonInteractionEvent event) {
        ConfigurationMessage configurationMessage = checkConfigurationMessage(event, event.getMessageIdLong());
        if (configurationMessage == null) return;
        
        String buttonId = event.getComponentId();
        switch (buttonId) {
            case "config:cat_counting:set_number" -> {
                String inputValue = String.valueOf(CountingChannel.lastNumber);
                Modal modal = Modal.create("config:cat_counting:set_number:modal", "Définir le nombre de comptage")
                    .addComponents(
                        Label.of(
                            "Nombre de comptage",
                            TextInput.create("config:cat_counting:set_number:modal:input", TextInputStyle.SHORT)
                                .setPlaceholder("Entrez un nombre entier.")
                                .setMinLength(1)
                                .setMaxLength(20)
                                .setValue(inputValue)
                                .build()
                        )
                    ).build();
                event.replyModal(modal).queue();
            }
            case "config:cat_moderation:edit_rules" -> {
                String description =
                    """
                    Les règles du serveur ont un format spécifique :
                    - Support total pour le markdown, utilisant celui de Discord.
                    - Les règles sont formatter par embed. Vous êtes libre de choisir la disposition et le style de l'embed.
                       - Chaque embed DOIT commencer par `---STARTEMBED---`, suivi de 3 valeurs de 0 a 255 pour la couleur, puis du contenu et enfin terminer par `---ENDEMBED---`.
                       - Exemple:
                       ```
                       ---STARTEMBED---
                       243, 118, 97
                       Contenu de l'embed...
                       ---ENDEMBED---```
                    
                    - Il n'y a pas de limite connu pour le moment.
                    - Le format interne du message risque fortement de changer.
                    - Un bouton de vérification sera toujours présent à la fin des règles. Il enlevera le rôle que le membre obtient en rejoignant le serveur, puis lui donnera le rôle considerer "vérifier" dans les paramètres de Bievenue.
                    """;
                
                event.replyModal(
                    createEditModal("config:cat_moderation:edit_rules:modal", "Modifier les règles du serveur", description,
                        "Règles du serveur", "Entrez les règles du serveur.", configurationMessage.configuration.rules, 4000)
                ).queue();
            }
            case "config:cat_welcome:edit_join_message" -> {
                event.replyModal(
                    createEditModal("config:cat_welcome:edit_join_message:modal", "Modifier le message de bienvenue", "Aucune description.",
                        "Message de bienvenue", "Entrez le message de bienvenue à envoyer lorsqu'un membre rejoint le serveur.", configurationMessage.configuration.welcomeJoinMessage)
                ).queue();
            }
            case "config:cat_welcome:edit_leave_message" -> {
                event.replyModal(
                    createEditModal("config:cat_welcome:edit_leave_message:modal", "Modifier le message de départ", "Aucune description.",
                        "Message de départ", "Entrez le message de départ à envoyer lorsqu'un membre quitte le serveur.", configurationMessage.configuration.welcomeLeaveMessage)
                ).queue();
            }
        }
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
    
    @ModalInteraction(id = "config:cat_counting:set_number:modal") public void onConfigCountingSetNumber(ModalInteractionEvent event) { onEditMessageModalSubmit(event); }
    @ModalInteraction(id = "config:cat_moderation:edit_rules:modal") public void onConfigModerationEditRulesModalSubmit(ModalInteractionEvent event) { onEditMessageModalSubmit(event); }
    @ModalInteraction(id = "config:cat_welcome:edit_join_message:modal") public void onConfigWelcomeEditJoinMessageModalSubmit(ModalInteractionEvent event) { onEditMessageModalSubmit(event); }
    @ModalInteraction(id = "config:cat_welcome:edit_leave_message:modal") public void onConfigWelcomeEditLeaveMessageModalSubmit(ModalInteractionEvent event) { onEditMessageModalSubmit(event); }
    private void onEditMessageModalSubmit(ModalInteractionEvent event) {
        ConfigurationMessage configurationMessage = checkConfigurationMessage(event, event.getMessage().getIdLong());
        if (configurationMessage == null) return;
        
        String modalId = event.getModalId();
        String inputId = switch (modalId) {
            case "config:cat_counting:set_number:modal" -> "config:cat_counting:set_number:modal:input";
            case "config:cat_moderation:edit_rules:modal" -> "config:cat_moderation:edit_rules:modal:input";
            case "config:cat_welcome:edit_join_message:modal" -> "config:cat_welcome:edit_join_message:modal:input";
            case "config:cat_welcome:edit_leave_message:modal" -> "config:cat_welcome:edit_leave_message:modal:input";
            default -> null;
        };
        if (inputId == null) return;
        
        String newValue = event.getValue(inputId).getAsString();
        switch (modalId) {
            case "config:cat_counting:set_number:modal" -> {
                try {
                    CountingChannel.lastNumber = Long.parseLong(newValue);
                    event.reply("Le nombre actuel du salon de comptage a été mis à jour à " + newValue + ".").setEphemeral(true).queue();
                    return;
                }
                catch (NumberFormatException e) {
                    event.reply("Veuillez entrer un nombre entier valide.").setEphemeral(true).queue();
                    return;
                }
            }
            case "config:cat_moderation:edit_rules:modal" -> configurationMessage.configuration.setRules(newValue);
            case "config:cat_welcome:edit_join_message:modal" -> configurationMessage.configuration.setWelcomeJoinMessage(newValue);
            case "config:cat_welcome:edit_leave_message:modal" -> configurationMessage.configuration.setWelcomeLeaveMessage(newValue);
        }
        
        configurationMessage.hasChanged = true;
        event.editComponents(switch(modalId) {
            case "config:cat_moderation:edit_rules:modal" -> createModerationMenuContainer(configurationMessage);
            case "config:cat_welcome:edit_join_message:modal", "config:cat_welcome:edit_leave_message:modal" -> createWelcomeMenuContainer(configurationMessage);
            default -> null;
        }).useComponentsV2().queue();
    }
    
    
    private ConfigurationMessage checkConfigurationMessage(GenericInteractionCreateEvent event, long id) {
        ConfigurationMessage configurationMessage = configurationMessages.get(id);
        if (configurationMessage == null || Instant.now().isAfter(configurationMessage.timestamp.plusSeconds(15 * 60))) {
            if (event instanceof GenericComponentInteractionCreateEvent componentEvent) {
                componentEvent.reply("Cette interaction a expiré. Veuillez réutiliser la commande pour obtenir un nouveau menu de configuration.").setEphemeral(true).queue();
                
                componentEvent.getMessage().editMessageComponents(TextDisplay.of("⚠️ Ce menu de configuration a expiré. Veuillez réutiliser la commande pour obtenir un nouveau menu."))
                    .useComponentsV2().queue();
            }
            configurationMessages.remove(id);
            return null;
        }
        
        return configurationMessage;
    }
    
    
    private Container createMainMenuContainer(ConfigurationMessage configurationMessage) {
        return Container.of(
            TextDisplay.of("# Configuration"),
            TextDisplay.of("Configuration du bot - Choisissez une catégorie à configurer :"),
            
            Section.of(
                Button.primary("config:cat_autores", "Configurer"), TextDisplay.of("### Réponse automatique"),
                TextDisplay.of("-# Le système de réponses automatiques.")),
            Section.of(
                Button.primary("config:cat_counting", "Configurer"), TextDisplay.of("### Comptage"),
                TextDisplay.of("-# Le système de comptage.")),
            Section.of(
                Button.primary("config:cat_markov", "Configurer"), TextDisplay.of("### Markov"),
                TextDisplay.of("-# Le système de génération de messages de Markov.")),
            Section.of(
                Button.primary("config:cat_moderation", "Configurer"), TextDisplay.of("### Modération"),
                TextDisplay.of("-# Le système de modération.")),
            Section.of(
                Button.primary("config:cat_welcome", "Configurer"), TextDisplay.of("### Bienvenue"),
                TextDisplay.of("-# Le système de bienvenue.")),
            
            createBottomRow(configurationMessage)
        );
    }
    
    private Container createAutoResponseMenuContainer(ConfigurationMessage configurationMessage) {
        return Container.of(
            TextDisplay.of("# Configuration - Réponses automatique"),
            
            createToggleSection("Système de réponses automatique",
                "Active ou désactive le système de réponses automatiques.",
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
                "Active ou désactive le système de comptage.",
                "config:cat_counting:toggle", configurationMessage.configuration.countingEnabled),
            
             createToggleSection("Commentaires de comptage",
                "Autorise ou non les commentaires sur les messages de comptage (après le nombre).",
                "config:cat_counting:toggle_comments", configurationMessage.configuration.countingCommentsEnabled, !configurationMessage.configuration.countingEnabled),
            
             createToggleSection("Pénalité en cas d'erreur de comptage",
                "Active ou désactive la pénalité en cas d'erreur de comptage (le compteur est réinitialisé à 0).",
                "config:cat_counting:toggle_penalty", configurationMessage.configuration.countingPenaltyEnabled, !configurationMessage.configuration.countingEnabled),
            
            TextDisplay.of("### Salon de comptage"),
            TextDisplay.of("-# Le salon où le comptage est actif."),
            ActionRow.of(channelSelectMenu),
            
            Section.of(
                Button.of(ButtonStyle.PRIMARY, "config:cat_counting:set_number", "Définir le nombre", Emoji.fromUnicode("\uD83D\uDD22"))
                    .withDisabled(!configurationMessage.configuration.countingEnabled),
                TextDisplay.of("### Définir le nombre de comptage"),
                TextDisplay.of("-# Définit le nombre actuel du salon de comptage à un nombre spécifique.")
            ),
            Section.of(
                Button.of(ButtonStyle.DANGER, "config:cat_counting:reset_number", "Réinitialiser le nombre", Emoji.fromUnicode("\uD83D\uDDD1️"))
                    .withDisabled(!configurationMessage.configuration.countingEnabled),
                TextDisplay.of("### Réinitialiser le nombre de comptage"),
                TextDisplay.of("-# Réinitialise le nombre actuel à 0.")
            ),
            Section.of(
                Button.of(ButtonStyle.DANGER, "config:cat_counting:reset_author", "Réinitialiser l'auteur du dernier nombre", Emoji.fromUnicode("\uD83D\uDC64"))
                    .withDisabled(!configurationMessage.configuration.countingEnabled),
                TextDisplay.of("### Réinitialiser l'auteur du dernier nombre"),
                TextDisplay.of("-# Réinitialise l'auteur du dernier nombre.")
            ),
            
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
                "Active ou désactive le système de génération de messages de Markov.",
                "config:cat_markov:toggle", configurationMessage.configuration.markovEnabled),
            
            
            TextDisplay.of("### Blacklist de Markov"),
            TextDisplay.of("Indisponible. En développement."),
            
            createBottomRow(configurationMessage)
        );
    }
    
    private Container createModerationMenuContainer(ConfigurationMessage configurationMessage) {
        return Container.of(
            TextDisplay.of("# Configuration - Modération"),
            
            createToggleSection("Système de modération",
                "Active ou désactive les commande de modération.",
                "config:cat_moderation:toggle", configurationMessage.configuration.moderationEnabled),
            
            Section.of(
                Button.primary("config:cat_moderation:edit_rules", "Modifier les règles du serveur"),
                TextDisplay.of("### Règles du serveur"),
                TextDisplay.of("-# Les règles du serveur, envoyées par le bot via la commande /admin rules send|update.")
            ),
            
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
                "Active ou désactive le système de bienvenue.",
                "config:cat_welcome:toggle", configurationMessage.configuration.welcomeEnabled),
            
            TextDisplay.of("### Salon de bienvenue"),
            TextDisplay.of("-# Le salon où les messages de bienvenue et de départ sont envoyés."),
            ActionRow.of(channelSelectMenu),
            
            createToggleSection("Image de bienvenue",
                "Active ou désactive l'image de bienvenue (une image avec le nom du membre qui rejoint et le nombre de membres du serveur).",
                "config:cat_welcome:toggle_image", configurationMessage.configuration.welcomeImageEnabled, !configurationMessage.configuration.welcomeEnabled),
            
            TextDisplay.of("### Message de bienvenue"),
            Section.of(
                Button.primary("config:cat_welcome:edit_join_message", "Modifier le message de bienvenue").withDisabled(configurationMessage.configuration.welcomeImageEnabled),
                TextDisplay.of("-# Le message envoyé lorsqu'un membre rejoint le serveur. Incompatible avec l'image de bienvenue, qui désactive ce message pour les membres qui rejoignent.")
            ),
            Section.of(
                Button.primary("config:cat_welcome:edit_leave_message", "Modifier le message de départ"),
                TextDisplay.of("-# Le message envoyé lorsqu'un membre quitte le serveur.")
            ),
            
            TextDisplay.of("### Rôles de bienvenue"),
            TextDisplay.of("-# Les rôles à attribuer aux membres lorsqu'ils rejoignent le serveur."),
            TextDisplay.of("-# - Rôle à attribuer à tous les nouveaux membres."),
            ActionRow.of(joinRoleSelectMenu),
            TextDisplay.of("-# - Rôle à attribuer aux nouveaux bots."),
            ActionRow.of(joinBotRoleSelectMenu),
            TextDisplay.of("-# - Rôle à attribuer aux membres vérifiés (après avoir passé la vérification)."),
            ActionRow.of(verifiedRoleSelectMenu),
            
            createBottomRow(configurationMessage)
        );
    }
    
    private ActionRow createBottomRow(ConfigurationMessage configurationMessage) {
        return ActionRow.of(
            Button.primary("config:menu_back", "Retour au menu principal").withDisabled(configurationMessage == null || configurationMessage.isMainMenu),
            Button.success("config:save", "Enregistrer les modifications").withDisabled(configurationMessage == null || !configurationMessage.hasChanged)
        );
    }
    
    private Section createToggleSection(String label, String description, String id, boolean enabled) { return createToggleSection(label, description, id, enabled, false);}
    private Section createToggleSection(String label, String description, String id, boolean enabled, boolean buttonDisabled) {
        return Section.of(
            Button.of(
                enabled ? ButtonStyle.SUCCESS : ButtonStyle.DANGER,
                id,
                enabled ? "Activer" : "Désactiver",
                enabled ? BotCache.CHECK_EMOJI : BotCache.CROSS_EMOJI
            ).withDisabled(buttonDisabled),
            TextDisplay.of("### " + label),
            TextDisplay.of("-# " + description)
        );
    }
    
    private Modal createEditModal(String id, String title, String description, String inputTitle, String inputPlaceholder, String inputValue) { return createEditModal(id, title, description, inputTitle, inputPlaceholder, inputValue, 2000); }
    private Modal createEditModal(String id, String title, String description, String inputTitle, String inputPlaceholder, String inputValue, int inputMaxLength) {
        return Modal.create(id, title)
            .addComponents(
                TextDisplay.of(description),
                Label.of(
                    inputTitle,
                    TextInput.create(id + ":input", TextInputStyle.PARAGRAPH)
                        .setPlaceholder(inputPlaceholder)
                        .setMinLength(1)
                        .setMaxLength(inputMaxLength)
                        .setValue(inputValue == null || inputValue.isEmpty() ? null : inputValue)
                        .build()
                )
            ).build();
    }
}
