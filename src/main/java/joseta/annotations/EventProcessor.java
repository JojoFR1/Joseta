package joseta.annotations;

import joseta.annotations.interactions.Event;
import joseta.generated.EventType;
import joseta.utils.Log;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.*;
import net.dv8tion.jda.api.events.guild.*;
import net.dv8tion.jda.api.events.http.*;
import net.dv8tion.jda.api.events.session.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Processor for scanning, registering events and handling them.
 * <p>
 * It scans the specified package for classes annotated with {@link EventModule} and registers their methods
 * annotated with {@link Event} as events with the JDA bot instance.
 * <p>
 * The processor sets up event listeners to handle incoming events and invoke the corresponding event methods.
 */
public class EventProcessor {
    private static final Map<Class<? extends GenericEvent>, List<Event>> eventMethods = new HashMap<>();
    
    /**
     * Initializes the event processor by scanning the specified package for classes annotated with {@link EventModule},
     * registering their events  and setting up event listeners with the provided JDA bot instance.
     *
     * @param bot         The JDA bot instance to register events with.
     * @param packagesName The packages name to scan for event modules.
     *                     It should contain classes annotated with {@link EventModule}.
     */
    public static void initialize(JDA bot, String... packagesName) {
        Reflections reflections = new Reflections((Object[]) packagesName);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(EventModule.class);
        
        for (Class<?> eventClass : classes) {
            for (Method method : eventClass.getMethods()) { try {
                joseta.annotations.types.Event eventAnnotation = method.getAnnotation(joseta.annotations.types.Event.class);
                if (eventAnnotation == null) continue;
                
                EventType eventType = eventAnnotation.type();
                method.setAccessible(true);
                
                Event event = new Event(eventClass, method, eventAnnotation.guildOnly());
                if (eventMethods.get(eventType.getEventClass()) == null)
                    eventMethods.put(eventType.getEventClass(), new ArrayList<>() {{ add(event); }});
                else
                    eventMethods.get(eventType.getEventClass()).add(event);
                
            } catch (Exception e) { Log.warn("An error occurred while registering an event. Skipping.", e); }}
        }
        
        
        bot.addEventListener(new EventProcessor.EventListener());
    }
    
    private static class EventListener extends ListenerAdapter {
        private static final List<Class<? extends GenericEvent>> blacklist = List.of(
            StatusChangeEvent.class,
            HttpRequestEvent.class,
            GatewayPingEvent.class,
            GuildReadyEvent.class,
            ReadyEvent.class,
            ShutdownEvent.class,
            SessionDisconnectEvent.class,
            SessionResumeEvent.class
        );
        
        @Override
        public void onGenericEvent(GenericEvent event) {
            if (blacklist.contains(event.getClass())) return;
            
            List<Event> eventAnnotations = eventMethods.get(event.getClass());
            
            if (eventAnnotations == null) return;
            
            for (Event eventAnnotation : eventAnnotations) {
                try {
                    if (eventAnnotation.isGuildOnly()
                        && !(Boolean) event.getClass().getMethod("isFromGuild").invoke(event)) continue;
                    Object o = eventAnnotation.getClazz().getDeclaredConstructor().newInstance();
                    
                    eventAnnotation.getMethod().invoke(o, event);
                } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
                    Log.warn("An error occurred while processing an event. {}", e);
                }
            }
        }
    }
}
