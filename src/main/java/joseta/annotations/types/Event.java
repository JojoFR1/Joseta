package joseta.annotations.types;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a method as an event handler.
 * <p>
 * The method must be inside a class that implements {@link joseta.annotations.EventModule EventModule}.
 * <p>
 * It is handled by the {@link joseta.annotations.EventProcessor EventProcessor}, which provides the event to the method.
 * <p>
 * The method must have a single parameter corresponding to the event type specified.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Event {
    /**
     * The event type to handle.
     * <p>
     * The method annotated with this annotation will be called when the specified event is fired.
     * <p>
     * The method must have a single parameter matching the event value of the specified event type.
     * <p> For example:
     * <ul>
     *     <li>If the event type is {@link joseta.generated.EventType#MESSAGE_RECEIVED EventType.MESSAGE_RECEIVED},
     *         the method parameter must be of type {@link net.dv8tion.jda.api.events.message.MessageReceivedEvent MessageReceivedEvent}.</li>
     *     <li>If the event type is {@link joseta.generated.EventType#GUILD_MEMBER_JOIN EventType.GUILD_MEMBER_JOIN},
     *         the method parameter must be of type {@link net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent GuildMemberJoinEvent}.</li>
     *     <li>And so on for each event type defined in {@link joseta.generated.EventType EventType}.</li>
     * </ul>
     * <p>
     * Warning: Be cautious of events that your bot's responses may trigger again, potentially causing infinite loops.
     * <p>
     * One common example is responding to a {@link joseta.generated.EventType#MESSAGE_RECEIVED EventType.MESSAGE_RECEIVED} event by sending a message,
     * which could trigger the same event repeatedly. To prevent this, consider implementing checks such as verifying if the message author is a bot and ignoring such messages.
     */
    joseta.generated.EventType type();
    /**
     * Whether the command is only usable in guilds. Default to {@code true}.
     * <p>
     * If {@code true}, the command will not be usable in DMs.
     */
    boolean guildOnly() default true;
}
