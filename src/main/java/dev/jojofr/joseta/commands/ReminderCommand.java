package dev.jojofr.joseta.commands;

import dev.jojofr.joseta.annotations.InteractionModule;
import dev.jojofr.joseta.annotations.types.ButtonInteraction;
import dev.jojofr.joseta.annotations.types.ModalInteraction;
import dev.jojofr.joseta.annotations.types.Option;
import dev.jojofr.joseta.annotations.types.SlashCommandInteraction;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.entities.ReminderEntity;
import dev.jojofr.joseta.database.entities.ReminderEntity_;
import dev.jojofr.joseta.database.helper.MessageDatabase;
import dev.jojofr.joseta.entities.ReminderListMessage;
import dev.jojofr.joseta.events.ScheduledEvents;
import dev.jojofr.joseta.utils.TimeParser;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.checkbox.Checkbox;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.modals.Modal;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@InteractionModule
public class ReminderCommand {
    
    @SlashCommandInteraction(name = "reminder add", description = "Ajouter un rappel pour plus tard.")
    public void reminderAdd(SlashCommandInteractionEvent event,
                            @Option(description = "Le message du rappel.", required = true) String message,
                            @Option(description = "Le temps avant que vous recevez le rappel (M, w, d, h, m, s).", required = true) String time,
                            @Option(description = "Si le rappel doit être envoyé en MP (par défaut dans le canal).") Boolean dm,
                            @Option(description = "Si le rappel doit être répété (par défaut non).") Boolean repeat)
    {
        if (dm == null) dm = false;
        if (repeat == null) repeat = false;
        long timeSeconds = TimeParser.parse(time);
        
        String noMentions = MessageDatabase.NO_MENTIONS_PATTERN.matcher(message).replaceAll("");
        String noUrl = MessageDatabase.NO_URL_PATTERN.matcher(noMentions).replaceAll("");
        message = noUrl.replace("`", "").replace("\\", "");
        
        long userId = event.getUser().getIdLong();
        if (message.length() > ScheduledEvents.REMINDER_MAX_MESSAGE_LENGTH - String.valueOf(userId).length()) {
            event.reply("Le message de rappel est trop long. La longueur maximale est de " + (Message.MAX_CONTENT_LENGTH - ScheduledEvents.REMINDER_PREMESSAGE.length()) + " caractères.").setEphemeral(true).queue();
            return;
        }
        
        ReminderEntity reminder = new ReminderEntity(event.getGuild().getIdLong(), event.getChannelIdLong(), userId, message, timeSeconds, dm, repeat);
        Database.create(reminder);
        event.reply("⏰ Votre rappel a été ajouté pour le <t:" + reminder.remindAt.getEpochSecond() + ":F> (<t:" + reminder.remindAt.getEpochSecond() + ":R>)."
            + (repeat ? " Il sera répété." : "") + (dm ? " Il vous sera envoyé en message privé." : "")).setEphemeral(true).queue();
        
        if (dm)
            event.getUser().openPrivateChannel().queue(
                privateChannel -> {
                    if (!privateChannel.canTalk()) {
                        event.reply("⚠️ Je n'ai pas pu vous envoyer de message privé pour votre rappel. Veuillez vérifier que je peux vous envoyer des messages privés.").setEphemeral(true).queue();
                        return;
                    }
                    
                    privateChannel.sendMessage("⏰ Nouveau rappel ajouté pour le <t:" + reminder.remindAt.getEpochSecond() + ":F> (<t:" + reminder.remindAt.getEpochSecond() + ":R>). Il vous sera envoyé ici.").queue();
                },
                fail -> event.reply("⚠️ Je n'ai pas pu vous envoyer de message privé pour votre rappel. Veuillez vérifier que je peux vous envoyer des messages privés.").setEphemeral(true).queue()
            );
    }
    
    
    private static final int REMINDER_PER_PAGE = 5;
    private static final Map<Long, ReminderListMessage> reminderListMessages = new HashMap<>();
    
    @SlashCommandInteraction(name = "reminder list", description = "Liste vos rappels.")
    public void reminderList(SlashCommandInteractionEvent event) {
        List<ReminderEntity> reminders = Database.querySelect(ReminderEntity.class,
            (cb, rt) ->
                cb.and(cb.equal(rt.get(ReminderEntity_.userId), event.getUser().getIdLong()),
                    cb.equal(rt.get(ReminderEntity_.guildId), event.getGuild().getIdLong())),
            (cb, rt) -> cb.asc(rt.get(ReminderEntity_.remindAt))
        ).getResultList();
        
        if (reminders.isEmpty()) {
            event.reply("Vous n'avez aucun rappel actif.").setEphemeral(true).queue();
            return;
        }
        
        // Integer division always floors the result, so we add 1 if there's a remainder to ceil the value
        int lastPage = reminders.size() / REMINDER_PER_PAGE + (reminders.size() % REMINDER_PER_PAGE == 0 ? 0 : 1);
        
        event.replyComponents(generateContainer(reminders, event.getGuild(), event.getUser(), 1, lastPage))
            .useComponentsV2().setEphemeral(true).queue(
                hook -> reminderListMessages.put(event.getUser().getIdLong(), new ReminderListMessage(reminders, lastPage))
            );
    }
    
    @ButtonInteraction(id = "reminders:page:*")
    public void reminderPage(ButtonInteractionEvent event) {
        ReminderListMessage reminderMessage = getReminderListMessage(event);
        if (reminderMessage == null) return;
        
        String eventId = event.getComponentId();
        int currentPage = eventId.endsWith("first") ? 1
            : eventId.endsWith("prev")  ? reminderMessage.previousPage()
            : eventId.endsWith("next")  ? reminderMessage.nextPage()
            : reminderMessage.lastPage;
        
        event.editComponents(generateContainer(reminderMessage.reminders, event.getGuild(), event.getUser(), currentPage, reminderMessage.lastPage))
            .useComponentsV2().queue();
    }
    
    @ButtonInteraction(id = "reminders:edit:*n")
    public void reminderEdit(ButtonInteractionEvent event) {
        ReminderListMessage reminderMessage = getReminderListMessage(event);
        if (reminderMessage == null) return;
        
        int reminderId = Integer.parseInt(event.getComponentId().substring("reminders:edit:".length(), event.getComponentId().length() - 1));
        String id = event.getCustomId() + ":modal";
        
        ReminderEntity reminder = reminderMessage.reminders.get(reminderId);
        
        Modal modal = Modal.create(id, "Modifier le rappel")
            .addComponents(
                Label.of(
                    "Modifier le message du rappel :",
                    TextInput.create(id + ":input", TextInputStyle.PARAGRAPH)
                        .setPlaceholder("Le message de votre rappel...")
                        .setMinLength(1)
                        .setMaxLength(ScheduledEvents.REMINDER_MAX_MESSAGE_LENGTH - String.valueOf(event.getUser().getIdLong()).length())
                        .setValue(reminder.message)
                        .build()
                ),
                
                Label.of(
                    "Envoyé en privé",
                    Checkbox.of(id + ":dm", reminder.dm)
                ),
                
                Label.of(
                    "Répété",
                    Checkbox.of(id + ":repeat", reminder.repeat)
                )
            ).build();

        event.replyModal(modal).queue();
    }
    
    @ModalInteraction(id = "reminders:edit:*n:modal")
    public void reminderEditModal(ModalInteractionEvent event) {
        ReminderListMessage reminderMessage = getReminderListMessage(event);
        if (reminderMessage == null) return;
        
        int reminderId = Integer.parseInt(event.getCustomId().substring("reminders:edit:".length(), event.getCustomId().length() - "n:modal".length()));
        ReminderEntity reminder = reminderMessage.reminders.get(reminderId);
        
        String newMessage = event.getValue(event.getCustomId() + ":input").getAsString();
        boolean newDm = event.getValue(event.getCustomId() + ":dm").getAsBoolean();
        boolean newRepeat = event.getValue(event.getCustomId() + ":repeat").getAsBoolean();
        
        Database.update(reminder.setMessage(newMessage).setDm(newDm).setRepeat(newRepeat));
        event.editComponents(generateContainer(reminderMessage.reminders, event.getGuild(), event.getUser(), reminderMessage.currentPage, reminderMessage.lastPage))
            .useComponentsV2().queue();
    }
    
    @ButtonInteraction(id = "reminders:delete:*n")
    public void reminderDelete(ButtonInteractionEvent event) {
        ReminderListMessage reminderMessage = getReminderListMessage(event);
        if (reminderMessage == null) return;
        
        int reminderId = Integer.parseInt(event.getComponentId().substring("reminders:delete:".length(), event.getComponentId().length() - 1));
        Database.delete(reminderMessage.reminders.get(reminderId));
        reminderMessage.reminders.remove(reminderId);
        
        event.reply("Le rappel a bien été supprimé.").setEphemeral(true).queue();
        event.getMessage().editMessageComponents(generateContainer(reminderMessage.reminders, event.getGuild(), event.getUser(), reminderMessage.currentPage, reminderMessage.lastPage))
            .useComponentsV2().queue();
    }
    
    private Container generateContainer(List<ReminderEntity> reminders, Guild guild, User user, int currentPage, int lastPage) {
        List<ContainerChildComponent> components = new ArrayList<>();
        components.add(TextDisplay.of("### Liste des rappels de " + user.getEffectiveName() + " ┃ Page " +  currentPage + "/"+ lastPage));
        
        StringBuilder sb = new StringBuilder();
        
        int index = (currentPage - 1) * REMINDER_PER_PAGE;
        for (int i = index; i < index + REMINDER_PER_PAGE; i++) {
            if (i >= reminders.size()) break;
            
            ReminderEntity reminder = reminders.get(i);
            
            sb.append(i + 1).append(". <t:").append(reminder.remindAt.getEpochSecond()).append(":F> (<t:").append(reminder.remindAt.getEpochSecond()).append(":R>");
            
            if (reminder.repeat) sb.append(", répété");
            if (reminder.dm) sb.append(", en MP");
            sb.append(")\n").append("> ```").append(reminder.message).append("```\n\n");
            
            components.add(TextDisplay.of(sb.toString()));
            components.add(ActionRow.of(
                Button.primary("reminders:edit:" + i + "n", "Modifier").withEmoji(Emoji.fromUnicode("✏️")),
                Button.danger("reminders:delete:" + i + "n", "Supprimer").withEmoji(Emoji.fromUnicode("🗑️"))
            ));
            sb.setLength(0);
        }
        
        components.add(getPagesButton(currentPage, lastPage));
        
        return Container.of(components).withAccentColor(new Color(100, 169, 205));
    }
    
    private ReminderListMessage getReminderListMessage(GenericInteractionCreateEvent event) {
        if (!(event instanceof IReplyCallback replyCallback)) return null;
        
        ReminderListMessage reminderMessage = reminderListMessages.get(event.getUser().getIdLong());
        if (reminderMessage == null || Instant.now().isAfter(reminderMessage.timestamp.plusSeconds(15 * 60))) {
            replyCallback.reply("Cette interaction a expiré. Veuillez réutiliser la commande pour obtenir une nouvelle liste.").setEphemeral(true).queue();
            
            // Remove the button from the message
            if (event instanceof GenericComponentInteractionCreateEvent componentEvent)
                componentEvent.getMessage().editMessageComponents(TextDisplay.of("⚠️ Cette liste de rappels a expirer. Veuillez réutiliser la commande pour obtenir une nouvelle liste."))
                    .useComponentsV2().queue();

            reminderListMessages.remove(event.getUser().getIdLong());
        }
        return reminderMessage;
    }
    
    private ActionRow getPagesButton(int currentPage, int lastPage) {
        return ActionRow.of(
            Button.secondary("reminders:page:first", "⏪").withDisabled(currentPage == 1),
            Button.secondary("reminders:page:prev", "◀️").withDisabled(currentPage <= 1),
            Button.secondary("reminders:page:next", "▶️").withDisabled(currentPage >= lastPage),
            Button.secondary("reminders:page:last", "⏩").withDisabled(currentPage == lastPage)
        );
    }
}
