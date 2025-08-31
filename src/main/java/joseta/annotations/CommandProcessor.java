package joseta.annotations;

import joseta.annotations.modules.*;
import joseta.utils.*;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.hooks.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;
import org.jetbrains.annotations.*;
import org.reflections.*;

import java.lang.reflect.*;
import java.util.*;

import static org.reflections.scanners.Scanners.*;

/**
 * CommandProcessor is responsible for scanning, registering, and handling slash commands for the bot.
 */
public class CommandProcessor {
    private static final Map<String, Command> commandMethods = new HashMap<>();

    /**
     * Initializes the command processor to scan for commands in the specified package and register them with the bot.
     * @param bot The JDA bot instance
     * @param packageName The package to scan for commands
     */
    public static void initialize(JDA bot, String packageName) {
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> classes = reflections.get(SubTypes.of(CommandModule.class).asClass());

        List<SlashCommandData> commands = new ArrayList<>();

        for (Class<?> commandClass : classes) {
            try { for (Method method : commandClass.getMethods()) {
                SlashCommand commandAnnotation = method.getAnnotation(SlashCommand.class);
                if (commandAnnotation == null) continue;

                String commandName = commandAnnotation.name();
                if (commandName.isEmpty()) commandName = method.getName();

                SlashCommandData commandData = Commands.slash(commandName, commandAnnotation.description());

                method.setAccessible(true);
                Command command = new Command(commandClass, method, commandName);
                commandMethods.put(commandName, command);

                for (Parameter parameter : method.getParameters()) {
                    Option option = parameter.getAnnotation(Option.class);
                    if (option == null) continue;

                    String name = option.name();
                    if (name.isEmpty()) name = parameter.getName();

                    Class<?> type = parameter.getType();
                    OptionType optionType;

                    if (type == String.class) optionType = OptionType.STRING;
                    else if (type == int.class || type == Integer.class
                        || type == long.class || type == Long.class) optionType = OptionType.INTEGER;
                    else if (type == boolean.class || type == Boolean.class) optionType = OptionType.BOOLEAN;
                    else if (type == IMentionable.class) optionType = OptionType.MENTIONABLE; // Need to be before User, Member, Role and Channel
                    else if (type.isAssignableFrom(User.class) || type == Member.class) optionType = OptionType.USER;
                    else if (type.isAssignableFrom(Channel.class)) optionType = OptionType.CHANNEL;
                    else if (type == Role.class) optionType = OptionType.ROLE;
                    else if (type == double.class || type == Double.class) optionType = OptionType.NUMBER;
                    else if (type == Message.Attachment.class) optionType = OptionType.ATTACHMENT;
                    else {
                        Log.warn("Unsupported parameter type: " + type.getName() + " in command: " + command.getName());
                        optionType = OptionType.UNKNOWN;
                    }

                    command.addParameter(new Command.Parameter(type, name, option.required(), (optionType.canSupportChoices() && option.autoComplete())));
                    commandData.addOption(optionType, name, option.description(), option.required(), (optionType.canSupportChoices() && option.autoComplete()));

                }

                commands.add(commandData);
            }} catch (Exception e) { Log.warn("An error occurred while registering a command.", e); }
        }

        bot.updateCommands().addCommands(commands).queue(); // Reset for the guilds command to avoid duplicates.

        bot.addEventListener(new CommandProcessor.CommandListener());
    }


    private static class CommandListener extends ListenerAdapter {

        @Override
        public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
            Command command = commandMethods.get(event.getName());

            try {
                if (command == null) {
                    Log.warn("Unknown command: " + event.getName());
                    event.reply("Commande inconnue.").setEphemeral(true).queue();
                    return;
                }

                Object o = command.getClazz().getDeclaredConstructor(SlashCommandInteractionEvent.class).newInstance(event);

                List<Object> args = new ArrayList<>();
                for (Command.Parameter parameter : command.getParameters()) {
                    OptionMapping option = event.getOption(parameter.getName());

                    if (option == null && parameter.isRequired()) {
                        event.reply("Un paramètre obligatoire est manquant.").queue();
                        Log.warn("A required parameter is missing for command: " + command.getName() + ", parameter: " + parameter.getName());
                        return;
                    }

                    if (option == null) {
                        args.add(null);
                        continue;
                    }

                    switch (option.getType()) {
                        case STRING -> args.add(option.getAsString());
                        case INTEGER -> args.add((parameter.getType() == Long.class || parameter.getType() == long.class) ? option.getAsLong() : option.getAsInt());
                        case BOOLEAN -> args.add(option.getAsBoolean());
                        case USER -> args.add(parameter.getType().isAssignableFrom(User.class) ? option.getAsUser() : option.getAsMember());
                        case CHANNEL -> args.add(option.getAsChannel());
                        case ROLE -> args.add(option.getAsRole());
                        case MENTIONABLE -> args.add(option.getAsMentionable());
                        case NUMBER -> args.add(option.getAsDouble());
                        case ATTACHMENT -> args.add(option.getAsAttachment());
                        default -> args.add(null);
                    }
                }

                command.getMethod().invoke(o, args.toArray());
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
                Log.err("An error occurred during command execution ({}):", command.getName(), e);
            }
        }
    }
}
