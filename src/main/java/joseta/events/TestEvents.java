package joseta.events;

import joseta.annotations.EventModule;
import joseta.annotations.types.Event;
import joseta.generated.EventType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;

@EventModule
public class TestEvents {
    
    @Event(type = EventType.MESSAGE_RECEIVED)
    public void test(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        event.getChannel().sendMessage("Test event received!").queue();
    }
    
    @Event(type = EventType.MESSAGE_RECEIVED)
    public void testThree(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        event.getChannel().sendMessage("Test event 3 received!").queue();
    }
    @Event(type = EventType.MESSAGE_RECEIVED)
    public void testfour(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        event.getChannel().sendMessage("Test event FOUR received!").queue();
    }
    
    @Event(type = EventType.MESSAGE_UPDATE)
    public void testTwo(MessageUpdateEvent event) {
        event.getChannel().sendMessage("Test event two received!").queue();
    }
}
