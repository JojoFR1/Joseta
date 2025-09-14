package joseta.annotations;

import joseta.annotations.modules.*;
import joseta.annotations.types.*;
import joseta.utils.*;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.hooks.*;
import net.dv8tion.jda.api.interactions.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.interactions.commands.localization.*;
import org.reflections.*;

import java.lang.reflect.*;
import java.util.*;

import static org.reflections.scanners.Scanners.*;

/**
 * CommandProcessor is responsible for scanning, registering, and handling slash commands for the bot.
 */
public class CommandProcessor {
    private static final Map<String, Command> commandMethods = new HashMap<>();
    private static final LocalizationFunction localizedFunction = ResourceBundleLocalizationFunction.fromBundles("bundle",
        DiscordLocale.ENGLISH_US, DiscordLocale.ENGLISH_UK, DiscordLocale.FRENCH).build();


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

                String[] baseCommandName = commandAnnotation.name().isEmpty() ? null : commandAnnotation.name().split(" ");
                if (baseCommandName == null) baseCommandName = method.getName().split("(?=\\p{Upper})");

                String commandName = baseCommandName[0];
                String subcommandName;
                String subcommandGroupName;
                if (baseCommandName.length == 2) {
                    subcommandGroupName = null;
                    subcommandName = baseCommandName[1];
                } else if (baseCommandName.length >= 3) {
                    subcommandGroupName = baseCommandName[1];
                    subcommandName =  baseCommandName[2];
                } else {
                    subcommandGroupName = null;
                    subcommandName = null;
                }
                if (baseCommandName.length > 3) Log.warn("Command name too long (max 3 parts).");

                SlashCommandData commandData;
                boolean commandExists = false;
                if (commandMethods.keySet().stream().noneMatch(n -> n.startsWith(commandName))) commandData = Commands.slash(commandName, commandAnnotation.description()).setLocalizationFunction(localizedFunction);
                else {
                    commandData = commands.stream().filter(c -> c.getName().equals(commandName)).findFirst().orElse(null);
                    commandExists = true;
                }

                if (commandData == null) {
                    Log.err("An error occurred while registering the command: " + commandName);
                    continue;
                }

                method.setAccessible(true);
                String fullCommandName = commandName + (subcommandGroupName != null ? " " + subcommandGroupName : "") + (subcommandName != null ? " " + subcommandName : "");
                Command command = new Command(commandClass, method, fullCommandName);
                commandMethods.put(fullCommandName, command);

                if (subcommandName != null) {
                    boolean subcommandExists = true;
                    SubcommandData subcommandData = commandData.getSubcommands().stream().filter(s -> s.getName().equals(subcommandName)).findFirst().orElse(null);
                    if (subcommandData == null) {
                        subcommandExists = false;
                        subcommandData = new SubcommandData(subcommandName, commandAnnotation.description());
                    }

                    addParameters(method.getParameters(), command, subcommandData);

                    boolean subcommandGroupExists = true;
                    boolean hasSubcommandGroup = false;
                    SubcommandGroupData subcommandGroupData = null;
                    if (subcommandGroupName != null) {
                        subcommandGroupData = commandData.getSubcommandGroups().stream().filter(sg -> sg.getName().equals(subcommandGroupName)).findFirst().orElse(null);
                        if (subcommandGroupData == null) {
                            subcommandGroupExists = false;
                            subcommandGroupData = new SubcommandGroupData(subcommandGroupName, commandAnnotation.description());
                        }
                        hasSubcommandGroup = true;

                        subcommandGroupData.addSubcommands(subcommandData);
                    }

                    if (hasSubcommandGroup && !subcommandGroupExists) commandData.addSubcommandGroups(subcommandGroupData);
                    else if (!subcommandExists) commandData.addSubcommands(subcommandData);
                }
                else addParameters(method.getParameters(), command, commandData);

                if (!commandExists) commands.add(commandData);
            }} catch (Exception e) { Log.warn("An error occurred while registering a command.", e); }
        }

        bot.updateCommands().addCommands(commands).queue(); // Reset for the guilds command to avoid duplicates.

        bot.addEventListener(new CommandListener());
    }

    private static void addParameters(Parameter[] parameters, Command command, Object commandObject) {
        SlashCommandData commandData = null;
        SubcommandData subcommandData = null;
        if (commandObject instanceof SlashCommandData slashCommandData) commandData = slashCommandData;
        else if (commandObject instanceof SubcommandData subcommandData_) subcommandData = subcommandData_;
        else {
            Log.err("Invalid command object type: " + commandObject.getClass().getName());
            return;
        }

        for (Parameter parameter : parameters) {
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
            if (commandData != null) commandData.addOption(optionType, name, option.description(), option.required(), (optionType.canSupportChoices() && option.autoComplete()));
            else subcommandData.addOption(optionType, name, option.description(), option.required(), (optionType.canSupportChoices() && option.autoComplete()));
        }
    }

    private static class CommandListener extends ListenerAdapter {

        @Override
        public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
            String commandName = event.getName();
            String subcommandGroup = event.getSubcommandGroup();
            String subcommand = event.getSubcommandName();

            if (subcommandGroup != null) commandName += " " + subcommandGroup;
            if (subcommand != null) commandName += " " + subcommand;

            Command command = commandMethods.get(commandName);
            if (command == null) {
                Log.warn("Unknown command: " + event.getName());
                event.reply("Commande inconnue.").setEphemeral(true).queue();
                return;
            }

            try {
                Object o = command.getClazz().getDeclaredConstructor(SlashCommandInteractionEvent.class).newInstance(event);

                List<Object> args = new ArrayList<>();
                for (Command.Parameter parameter : command.getParameters()) {
                    OptionMapping option = event.getOption(parameter.getName());

                    if (option == null && parameter.isRequired()) {
                        Log.warn("A required parameter is missing for command: " + command.getName() + ", parameter: " + parameter.getName());
                        event.reply("Un paramètre obligatoire est manquant.").queue();
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
