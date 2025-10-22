package joseta.annotations;

import joseta.annotations.interactions.Command;
import joseta.annotations.interactions.Interaction;
import joseta.annotations.types.*;
import joseta.annotations.types.ContextInteraction;
import joseta.annotations.types.SlashCommandInteraction;
import joseta.utils.*;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.*;
import net.dv8tion.jda.api.events.interaction.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.events.interaction.component.*;
import net.dv8tion.jda.api.hooks.*;
import net.dv8tion.jda.api.interactions.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.interactions.commands.context.*;
import net.dv8tion.jda.api.interactions.commands.localization.*;
import org.jetbrains.annotations.*;
import org.reflections.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * Processor for scanning, registering interaction (such as {@link SlashCommandInteraction}, {@link ButtonInteraction}, ...) and handling them.
 * <p>
 * It scans the specified package for classes annotated with {@link InteractionModule} and registers their methods
 * annotated with {@link SlashCommandInteraction} as slash commands with the JDA bot instance.
 * <p>
 * The processor sets up event listeners to handle incoming interactions and invoke the corresponding command methods.
 */
public class InteractionProcessor {
    private static final Map<String, joseta.annotations.interactions.Interaction> interactionMethods = new HashMap<>();
    private static final LocalizationFunction localizedFunction = ResourceBundleLocalizationFunction.fromBundles("bundles/bundle",
        DiscordLocale.ENGLISH_US, DiscordLocale.ENGLISH_UK, DiscordLocale.FRENCH).build();

    /**
     * Initializes the interaction processor by scanning the specified package for classes annotated with {@link InteractionModule InteractionModule},
     * registering their commands with the provided JDA bot instance, and setting up event listeners.
     *
     * @param bot         The JDA bot instance to register commands with.
     * @param packageName The package name to scan for interaction modules.
     *                    It should contain classes annotated with {@link InteractionModule InteractionModule}.
     */
    public static void initialize(JDA bot, String packageName) {
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(InteractionModule.class);

        List<CommandData> commands = new ArrayList<>();

        //TODO Hello future me, sorry for not handling Permission yet, too lazy to do it now
        for (Class<?> commandClass : classes) {
            try { for (Method method : commandClass.getMethods()) {
                // TODO context commands
                SlashCommandInteraction commandInteraction = method.getAnnotation(SlashCommandInteraction.class);
                if (commandInteraction != null) {
                    processCommand(commandInteraction, commandClass, method, commands);
                    continue;
                }

                ContextInteraction contextInteraction = method.getAnnotation(ContextInteraction.class);
                if (contextInteraction != null) {
                    String name = contextInteraction.name();
                    if (name.isEmpty()) name = method.getName().toLowerCase();
                    net.dv8tion.jda.api.interactions.commands.Command.Type type = contextInteraction.type();
                    method.setAccessible(true);
                    commands.add(Commands.context(type, name));
                    interactionMethods.put(name, new Interaction(commandClass, method, name));
                    continue;
                }

                ButtonInteraction buttonInteraction = method.getAnnotation(ButtonInteraction.class);
                if (buttonInteraction != null) {
                    String id = buttonInteraction.id();
                    if (id.isEmpty()) id = method.getName().toLowerCase();
                    method.setAccessible(true);
                    interactionMethods.put(id, new Interaction(commandClass, method, id));
                    continue;
                }

                SelectMenuInteraction selectMenuInteraction = method.getAnnotation(SelectMenuInteraction.class);
                if (selectMenuInteraction != null) {
                    String id = selectMenuInteraction.id();
                    if (id.isEmpty()) id = method.getName().toLowerCase();
                    method.setAccessible(true);
                    interactionMethods.put(id, new Interaction(commandClass, method, id));
                    continue;
                }

                ModalInteraction modalInteraction = method.getAnnotation(ModalInteraction.class);
                if (modalInteraction != null) {
                    String id = modalInteraction.id();
                    if (id.isEmpty()) id = method.getName().toLowerCase();
                    method.setAccessible(true);
                    interactionMethods.put(id, new Interaction(commandClass, method, id));
                }
            }} catch (Exception e) { Log.warn("An error occurred while registering a command. {}", e); }
        }

        bot.updateCommands().addCommands(commands).queue();

        bot.addEventListener(new InteractionListener());
    }

    private static void processCommand(SlashCommandInteraction commandAnnotation, Class<?> commandClass, Method method, List<CommandData> commands) {
        String[] baseCommandName = commandAnnotation.name().isEmpty() ? null : commandAnnotation.name().split(" ");
        if (baseCommandName == null) {
            baseCommandName = method.getName().split("(?=\\p{Upper})");
            for (int i = 0; i < baseCommandName.length; i++) baseCommandName[i] = baseCommandName[i].toLowerCase();
        }

        String commandName = baseCommandName[0];
        String subcommandName;
        String subcommandGroupName;
        if (baseCommandName.length == 2) {
            subcommandGroupName = null;
            subcommandName = baseCommandName[1];
        } else if (baseCommandName.length >= 3) {
            subcommandGroupName = baseCommandName[1];
            subcommandName =  baseCommandName[2];
        } else { subcommandGroupName = null; subcommandName = null; }
        if (baseCommandName.length > 3) Log.warn("Command name too long (max 3 parts).");

        String fullCommandName = commandName + (subcommandGroupName != null ? " " + subcommandGroupName : "") + (subcommandName != null ? " " + subcommandName : "");

        SlashCommandData commandData;
        boolean commandExists = false;
        if (commands.stream().noneMatch(c -> c.getName().equals(commandName))) commandData = Commands.slash(commandName, commandAnnotation.description()).setLocalizationFunction(localizedFunction);
        else {
            commandData = (SlashCommandData) commands.stream().filter(c -> c.getName().equals(commandName)).findFirst().orElse(null);
            commandExists = true;
        }

        if (commandData == null) {
            Log.err("An error occurred while registering the command: " + fullCommandName);
            return;
        }

        method.setAccessible(true);
        Command command = new Command(commandClass, method, fullCommandName);
        interactionMethods.put(fullCommandName, command);

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

        Permission permission = commandAnnotation.permission();
        if (permission != Permission.UNKNOWN)
            commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(permission));

        if (!commandExists) commands.add(commandData);
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
                Log.warn("Unsupported parameter type " + type.getName() + " in command: " + command.getName());
                optionType = OptionType.UNKNOWN;
            }

            command.addParameter(new Command.Parameter(type, name, option.required(), (optionType.canSupportChoices() && option.autoComplete())));
            if (commandData != null) commandData.addOption(optionType, name, option.description(), option.required(), (optionType.canSupportChoices() && option.autoComplete()));
            else subcommandData.addOption(optionType, name, option.description(), option.required(), (optionType.canSupportChoices() && option.autoComplete()));
        }
    }

    private static class InteractionListener extends ListenerAdapter {

        @Override
        public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
            String commandName = event.getName();
            String subcommandGroup = event.getSubcommandGroup();
            String subcommand = event.getSubcommandName();

            if (subcommandGroup != null) commandName += " " + subcommandGroup;
            if (subcommand != null) commandName += " " + subcommand;

            Command command = (Command) interactionMethods.get(commandName);
            if (command == null) {
                Log.warn("Unknown command: " + event.getName());
                event.reply("Commande inconnue.").setEphemeral(true).queue();
                return;
            }

            try {
                Object o = command.getClazz().getDeclaredConstructor().newInstance();

                List<Object> args = new ArrayList<>();
                args.add(event); // First parameter is always the event
                for (Command.Parameter parameter : command.getParameters()) {
                    OptionMapping option = event.getOption(parameter.name());

                    if (option == null && parameter.required()) {
                        Log.warn("A required parameter is missing for command: " + command.getName() + ", parameter: " + parameter.name());
                        event.reply("Un paramètre obligatoire est manquant.").queue();
                        return;
                    }

                    if (option == null) {
                        args.add(null);
                        continue;
                    }

                    switch (option.getType()) {
                        case STRING -> args.add(option.getAsString());
                        case INTEGER -> args.add((parameter.type() == Long.class || parameter.type() == long.class) ? option.getAsLong() : option.getAsInt());
                        case BOOLEAN -> args.add(option.getAsBoolean());
                        case USER -> args.add(parameter.type().isAssignableFrom(User.class) ? option.getAsUser() : option.getAsMember());
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

        @Override
        public void onGenericContextInteraction(GenericContextInteractionEvent<?> event) {
            String commandName = event.getName();

            Interaction contextInteraction = interactionMethods.get(commandName);
            if (contextInteraction == null) {
                Log.warn("Unknown context interaction: " + event.getName());
                event.reply("Interaction inconnue.").setEphemeral(true).queue();
                return;
            }

            try {
                Object o = contextInteraction.getClazz().getDeclaredConstructor().newInstance();

                contextInteraction.getMethod().invoke(o, event);
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
                Log.err("An error occurred during command execution ({}):", contextInteraction.getName(), e);
            }
        }

        @Override
        public void onButtonInteraction(ButtonInteractionEvent event) {
            String buttonId = event.getComponentId();

            Interaction button = interactionMethods.get(buttonId);
            if (button == null) {
                Log.warn("Unknown button: " + event.getId());
                event.reply("Bouton inconnue.").setEphemeral(true).queue();
                return;
            }

            try {
                Object o = button.getClazz().getDeclaredConstructor().newInstance();

                button.getMethod().invoke(o, event);
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
                Log.err("An error occurred during command execution ({}):", button.getName(), e);
            }
        }

        @Override
        public void onGenericSelectMenuInteraction(GenericSelectMenuInteractionEvent event) {
            String selectMenuId = event.getComponentId();

            Interaction selectMenu = interactionMethods.get(selectMenuId);
            if (selectMenu == null) {
                Log.warn("Unknown select menu: " + event.getId());
                event.reply("Menu de sélection inconnu.").setEphemeral(true).queue();
                return;
            }

            try {
                Object o = selectMenu.getClazz().getDeclaredConstructor().newInstance();

                selectMenu.getMethod().invoke(o, event);
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
                Log.err("An error occurred during command execution ({}):", selectMenu.getName(), e);
            }
        }

        @Override
        public void onModalInteraction(ModalInteractionEvent event) {
            String modalId = event.getModalId();

            Interaction selectMenu = interactionMethods.get(modalId);
            if (selectMenu == null) {
                Log.warn("Unknown select menu: " + event.getId());
                event.reply("Menu de sélection inconnu.").setEphemeral(true).queue();
                return;
            }

            try {
                Object o = selectMenu.getClazz().getDeclaredConstructor().newInstance();

                selectMenu.getMethod().invoke(o, event);
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
                Log.err("An error occurred during command execution ({}):", selectMenu.getName(), e);
            }
        }
    }
}
