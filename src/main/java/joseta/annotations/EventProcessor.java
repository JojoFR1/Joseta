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

public class EventProcessor {
    private static final Map<Class<? extends GenericEvent>, Event> eventMethods = new HashMap<>();
    
    public static void initialize(JDA bot, String packageName) {
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(EventModule.class);
        
        for (Class<?> eventClass : classes) {
            try { for (Method method : eventClass.getMethods()) {
                joseta.annotations.types.Event eventAnnotation = method.getAnnotation(joseta.annotations.types.Event.class);
                if (eventAnnotation == null) continue;
                
                EventType eventType = eventAnnotation.type();
                method.setAccessible(true);
                eventMethods.put(eventType.getEventClass(), new joseta.annotations.interactions.Event(eventClass, method));
                
            }} catch (Exception e) { Log.warn("An error occurred while registering a command. {}", e); }
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
            
            Event eventAnnotation = eventMethods.get(event.getClass());
            
            if (eventAnnotation == null) {
                Log.warn("No event handler found for event of type: {}", event.getClass().getName());
                return;
            }
            
            try {
                Object o = eventAnnotation.getClazz().getDeclaredConstructor().newInstance();
                
                eventAnnotation.getMethod().invoke(o, event);
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
                Log.warn("An error occurred while processing an event. {}", e);
            }
        }
    }
}
