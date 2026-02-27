package dev.jojofr.joseta.annotations;

import dev.jojofr.joseta.annotations.interactions.Event;
import dev.jojofr.joseta.annotations.types.EventHandler;
import dev.jojofr.joseta.generated.EventType;
import dev.jojofr.joseta.utils.Log;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GatewayPingEvent;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.StatusChangeEvent;
import net.dv8tion.jda.api.events.http.HttpRequestEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.SessionDisconnectEvent;
import net.dv8tion.jda.api.events.session.SessionResumeEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.Interaction;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Processor for scanning, registering events and handling them.
 * <p>
 * It scans the specified package for classes annotated with {@link EventModule} and registers their methods
 * annotated with {@link EventHandler} as events with the JDA bot instance.
 * <p>
 * The processor sets up event listeners to handle incoming events and invoke the corresponding event methods.
 */
// TODO cache the instances of the classes containing the events to optimize performance (event though in previous test this did not change much), but some
//      events might need to have an object instance per event and not global
// TODO i feel like having one function = one event is nice for readability and organization but i think it could be bad for performance, having to create
//      an object each time and execute the function one after the other
public class EventProcessor {
    // TODO Maybe use a Set, it is supposedly faster and i dont care about order or duplicates - but if i do implement a priority system then order might matter needing a list ordered by priority (pre ordered instead of oredering each time)
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
                EventHandler eventAnnotation = method.getAnnotation(EventHandler.class);
                if (eventAnnotation == null) continue;
                
                EventHandler.EventPriority priority = eventAnnotation.priority();
                if (priority == EventHandler.EventPriority.DISABLED) {
                    Log.warn("Event {}.{} is disabled, skipping registration.", eventClass.getName(), method.getName());
                    continue;
                }
                
                EventType eventType = eventAnnotation.type();
                method.setAccessible(true);
                
                Event event = new Event(eventClass, method, priority, eventAnnotation.guildOnly());
                if (eventMethods.get(eventType.getEventClass()) == null)
                    eventMethods.put(eventType.getEventClass(), new ArrayList<>(List.of(event)));
                else
                    eventMethods.get(eventType.getEventClass()).add(event);
                
            } catch (Exception e) { Log.warn("An error occurred while registering an event. Skipping.", e); }}
        }
        
        for (List<Event> events : eventMethods.values())
            events.sort(Comparator.comparingInt(e -> e.getPriority().ordinal()));
        
        bot.addEventListener(new EventProcessor.EventListener());
    }
    
    private static class EventListener extends ListenerAdapter {
        private static final Set<Class<? extends GenericEvent>> blacklist = Set.of(
            StatusChangeEvent.class,
            HttpRequestEvent.class,
            GatewayPingEvent.class,
            ReadyEvent.class,
            ShutdownEvent.class,
            SessionDisconnectEvent.class,
            SessionResumeEvent.class
        );
        
        @Override
        public void onGenericEvent(GenericEvent event) {
            long startTime = System.currentTimeMillis();
            if (blacklist.contains(event.getClass())) return;
            
            List<Event> eventAnnotations = eventMethods.get(event.getClass());
            if (eventAnnotations == null) return;
            
            for (Event eventAnnotation : eventAnnotations) {
                try {
                    if (eventAnnotation.isGuildOnly() && !isFromGuild(event)) continue;
                    
                    Object o = eventAnnotation.getClazz().getDeclaredConstructor().newInstance();
                    eventAnnotation.getMethod().invoke(o, event);
                } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
                    Log.warn("An error occurred before or while executing an event.", e);
                } catch (Exception e) {
                    Log.warn("An unexpected error occurred while executing the event {}.{}", e, eventAnnotation.getMethod().getClass().getName(), eventAnnotation.getMethod().getName());
                }
            }
            
            long endTime = System.currentTimeMillis();
            Log.debug("Event {} processed in {} ms", event.getClass().getSimpleName(), (endTime - startTime));
        }
        
        private boolean isFromGuild(GenericEvent event) {
            if (event instanceof Interaction interactionEvent) return interactionEvent.isFromGuild();
            if (event instanceof GenericMessageEvent messageEvent) return messageEvent.isFromGuild();
            return false;
        }
    }
}
