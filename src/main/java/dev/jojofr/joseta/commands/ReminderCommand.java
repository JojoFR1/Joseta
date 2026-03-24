package dev.jojofr.joseta.commands;

import dev.jojofr.joseta.annotations.InteractionModule;
import dev.jojofr.joseta.annotations.types.ButtonInteraction;
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
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.hibernate.query.SelectionQuery;

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
                            @Option(description = "Le temps avant que vous recevez le rappel (M, w, d, h, m, s).", required = true) String time)
    {
        long timeSeconds = TimeParser.parse(time);
        
        String noMentions = MessageDatabase.NO_MENTIONS_PATTERN.matcher(message).replaceAll("");
        String noUrl = MessageDatabase.NO_URL_PATTERN.matcher(noMentions).replaceAll("");
        message = noUrl.replace("`", "").replace("\\", "");
        
        long userId = event.getUser().getIdLong();
        if (message.length() > ScheduledEvents.REMINDER_MAX_MESSAGE_LENGTH - String.valueOf(userId).length()) {
            event.reply("Le message de rappel est trop long. La longueur maximale est de " + (Message.MAX_CONTENT_LENGTH - ScheduledEvents.REMINDER_PREMESSAGE.length()) + " caractères.").setEphemeral(true).queue();
            return;
        }
        
        Instant remindAt = Instant.now().plusSeconds(timeSeconds);
        Database.create(new ReminderEntity(event.getGuild().getIdLong(), event.getChannelIdLong(), userId, message, remindAt));
        event.reply("Votre rappel a été ajouté pour le <t:" + remindAt.getEpochSecond() + ":F> (<t:" + remindAt.getEpochSecond() + ":R>).").setEphemeral(true).queue();
    }
    
    
    private static final int REMINDER_PER_PAGE = 5;
    private static final Map<Long, ReminderListMessage> reminderListMessages = new HashMap<>();
    
    @SlashCommandInteraction(name = "reminder list", description = "Liste vos rappels.")
    public void reminderList(SlashCommandInteractionEvent event) {
        SelectionQuery<ReminderEntity> query = Database.querySelect(ReminderEntity.class,
            (cb, rt) ->
                cb.and(cb.equal(rt.get(ReminderEntity_.userId), event.getUser().getIdLong()),
                    cb.equal(rt.get(ReminderEntity_.guildId), event.getGuild().getIdLong())),
            (cb, rt) -> cb.asc(rt.get(ReminderEntity_.remindAt))
        );
        int reminderAmount = Math.toIntExact(query.getResultCount());
        if (reminderAmount == 0) {
            event.reply("Vous n'avez aucun rappel actif.").setEphemeral(true).queue();
            return;
        }
        
        // Integer division always floors the result, so we add 1 if there's a remainder to ceil the value
        int lastPage = reminderAmount / REMINDER_PER_PAGE + (reminderAmount % REMINDER_PER_PAGE == 0 ? 0 : 1);
        
        Container container = generateContainer(query, event.getGuild(), event.getUser(), 1, lastPage);
        if (container == null) {
            event.reply("Vous n'avez aucun rappel actif.").setEphemeral(true).queue();
            return;
        }
        
        event.replyComponents(container).useComponentsV2().setEphemeral(true).queue(
            hook -> reminderListMessages.put(event.getUser().getIdLong(), new ReminderListMessage(lastPage))
        );
    }
    
    @ButtonInteraction(id = "reminders:page:*")
    public void reminderPage(ButtonInteractionEvent event) {
        ReminderListMessage reminderMessage = reminderListMessages.get(event.getUser().getIdLong());
        // Check if the reminderMessage exists and if the timestamp is still valid (15 minutes)
        if (reminderMessage == null || Instant.now().isAfter(reminderMessage.timestamp.plusSeconds(15 * 60))) {
            event.reply("Cette interaction a expiré. Veuillez réutiliser la commande pour obtenir une nouvelle liste.").setEphemeral(true).queue();
            
            // Remove the button from the message
            event.getMessage().editMessageComponents().queue();
            reminderListMessages.remove(event.getUser().getIdLong());
            return;
        }
        
        String eventId = event.getComponentId();
        int currentPage = eventId.endsWith("first") ? 1
            : eventId.endsWith("prev")  ? reminderMessage.previousPage()
            : eventId.endsWith("next")  ? reminderMessage.nextPage()
            : reminderMessage.lastPage;
        
        SelectionQuery<ReminderEntity> query = Database.querySelect(ReminderEntity.class,
            (cb, rt) ->
                cb.and(cb.equal(rt.get(ReminderEntity_.userId), event.getUser().getIdLong()),
                    cb.equal(rt.get(ReminderEntity_.guildId), event.getGuild().getIdLong())),
            (cb, rt) -> cb.asc(rt.get(ReminderEntity_.remindAt))
        );
        
        event.editComponents(generateContainer(query, event.getGuild(), event.getUser(), currentPage, reminderMessage.lastPage))
            .useComponentsV2().queue();
    }
    
    private Container generateContainer(SelectionQuery<ReminderEntity> query, Guild guild, User user, int currentPage, int lastPage) {
        List<ReminderEntity> reminders = query.setFirstResult((currentPage - 1) * REMINDER_PER_PAGE).setMaxResults(REMINDER_PER_PAGE).getResultList();
        
        if (reminders.isEmpty()) return null;
        
        List<ContainerChildComponent> components = new ArrayList<>();
        components.add(TextDisplay.of("### Liste des rappels de " + user.getEffectiveName() + " ┃ Page " +  currentPage + "/"+ lastPage));
        
        int reminderNum = REMINDER_PER_PAGE * (currentPage - 1) + 1;
        StringBuilder sb = new StringBuilder();
        for (ReminderEntity reminder : reminders) {
            sb.append(reminderNum).append(". <t:").append(reminder.remindAt.getEpochSecond()).append(":F> (<t:").append(reminder.remindAt.getEpochSecond()).append(":R>)\n");
            sb.append("> ```").append(reminder.message).append("```\n\n");
            
            components.add(TextDisplay.of(sb.toString()));
            components.add(ActionRow.of(
                Button.primary("reminders:edit:" + i, "Modifier").withEmoji(Emoji.fromUnicode("✏️")),
                Button.danger("reminders:delete:" + i, "Supprimer").withEmoji(Emoji.fromUnicode("🗑️"))
            ));
            sb.setLength(0);
            
            reminderNum++;
        }
        
        components.add(getPagesButton(currentPage, lastPage));
        
        return Container.of(components).withAccentColor(new Color(100, 169, 205));
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
