package joseta.events;

import joseta.database.helper.*;
import joseta.events.admin.*;
import joseta.events.database.*;
import joseta.events.misc.*;
import joseta.events.moderation.*;

import net.dv8tion.jda.api.events.channel.*;
import net.dv8tion.jda.api.events.guild.*;
import net.dv8tion.jda.api.events.guild.member.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.events.interaction.component.*;
import net.dv8tion.jda.api.events.message.*;
import net.dv8tion.jda.api.hooks.*;

public class EventHandler extends ListenerAdapter {
    
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        CommandExecutor.execute(event);
    }
    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        ModAutoComplete.executeCommandAutoCompleteInteraction(event);
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        WelcomeMessage.executeGuildMemberJoin(event);
        UserDatabaseHelper.addUser(event.getMember());
    }
    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        WelcomeMessage.executeGuildMemberRemove(event);
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        ConfigEvents.executeGuildJoin(event);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        AutoResponseEvent.execute(event);
        
        MessageEvents.executeMessageReceived(event);
    }
    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        MessageEvents.executeMessageUpdate(event);
    }
    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        MessageEvents.executeMessageDelete(event);
    }
    @Override
    public void onMessageBulkDelete(MessageBulkDeleteEvent event) {
        MessageEvents.executeMessageBulkDelete(event);
    }
    
    @Override
    public void onChannelDelete(ChannelDeleteEvent event) {
        MessageEvents.executeChannelDelete(event);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();
        if (componentId.equals("b-rules_accept")) RulesAcceptEvent.execute(event);
        else if (componentId.startsWith("mod-")) ModButtonEvents.execute(event);
    }

    // @Override
    // public void onGenericEvent(GenericEvent genericEvent) {
    //     Event event = JosetaBot.events.find(e -> genericEvent.getClass().isInstance(e));

    //     if (event != null) event.execute(genericEvent);
    // }
}
