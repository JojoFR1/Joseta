package joseta.annotations;

import joseta.annotations.interactions.Command;
import joseta.annotations.interactions.Interaction;
import joseta.annotations.types.*;
import joseta.utils.Log;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.interactions.commands.localization.ResourceBundleLocalizationFunction;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
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

    /**
     * Initializes the interaction processor by scanning the specified package for classes annotated with {@link InteractionModule},
     * registering their commands and setting up event listeners with the provided JDA bot instance.
     * <p>
     * By default, it uses English (US and UK) and French localization from resource bundles located in "bundles/bundle".
     *
     * @param bot          The JDA bot instance to register commands with.
     * @param packagesName The packages name to scan for interaction modules.
     *                     It should contain classes annotated with {@link InteractionModule}.
     */
    public static void initialize(JDA bot, String... packagesName) {
        LocalizationFunction localizationFunction = ResourceBundleLocalizationFunction.fromBundles("bundles/bundle",
            DiscordLocale.ENGLISH_US, DiscordLocale.ENGLISH_UK, DiscordLocale.FRENCH).build();
        
        initialize(bot, localizationFunction, packagesName);
    }
    
    /**
     * Initializes the interaction processor by scanning the specified package for classes annotated with {@link InteractionModule},
     * registering their commands and setting up event listeners with the provided JDA bot instance.
     *
     * @param bot                  The JDA bot instance to register commands with.
     * @param localizationFunction The localization function to use for command localization.
     * @param packagesName         The packages name to scan for interaction modules.
     *                             It should contain classes annotated with {@link InteractionModule}.
     */
    // TODO if error come from JDA we dont know which interaction caused it
    public static void initialize(JDA bot, LocalizationFunction localizationFunction, String... packagesName) {
        Reflections reflections = new Reflections((Object[]) packagesName);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(InteractionModule.class);

        List<CommandData> commands = new ArrayList<>();

        for (Class<?> commandClass : classes) {
            for (Method method : commandClass.getMethods()) { try {
                SlashCommandInteraction commandInteraction = method.getAnnotation(SlashCommandInteraction.class);
                if (commandInteraction != null) {
                    processCommand(commandInteraction, localizationFunction, commandClass, method, commands);
                    continue;
                }

                ContextCommandInteraction contextCommandInteraction = method.getAnnotation(ContextCommandInteraction.class);
                if (contextCommandInteraction != null) {
                    String name = contextCommandInteraction.name();
                    if (name.isEmpty()) name = method.getName().toLowerCase();

                    net.dv8tion.jda.api.interactions.commands.Command.Type type = contextCommandInteraction.type();
                    method.setAccessible(true);

                    CommandData commandData = Commands.context(type, name).setLocalizationFunction(localizationFunction);
                    
                    Permission[] permissions = contextCommandInteraction.permissions();
                    if (permissions.length > 0 && permissions[0] != Permission.UNKNOWN)
                        commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(permissions));
                    
                    if (contextCommandInteraction.guildOnly())
                        commandData.setContexts(InteractionContextType.GUILD);
                    else commandData.setContexts(contextCommandInteraction.contextTypes());
                    
                    commandData.setIntegrationTypes(contextCommandInteraction.integrationTypes());
                    commandData.setNSFW(contextCommandInteraction.nsfw());
                    
                    commands.add(commandData);
                    interactionMethods.put(name, new Interaction(commandClass, method, name, contextCommandInteraction.guildOnly()));
                    continue;
                }
                
                ButtonInteraction buttonInteraction = method.getAnnotation(ButtonInteraction.class);
                if (buttonInteraction != null) {
                    String id = buttonInteraction.id();
                    if (id.isEmpty()) id = method.getName().toLowerCase();
                    method.setAccessible(true);
                    interactionMethods.put(id, new Interaction(commandClass, method, id, buttonInteraction.guildOnly()));
                    continue;
                }

                SelectMenuInteraction selectMenuInteraction = method.getAnnotation(SelectMenuInteraction.class);
                if (selectMenuInteraction != null) {
                    String id = selectMenuInteraction.id();
                    if (id.isEmpty()) id = method.getName().toLowerCase();
                    method.setAccessible(true);
                    interactionMethods.put(id, new Interaction(commandClass, method, id, selectMenuInteraction.guildOnly()));
                    continue;
                }

                ModalInteraction modalInteraction = method.getAnnotation(ModalInteraction.class);
                if (modalInteraction != null) {
                    String id = modalInteraction.id();
                    if (id.isEmpty()) id = method.getName().toLowerCase();
                    method.setAccessible(true);
                    interactionMethods.put(id, new Interaction(commandClass, method, id, modalInteraction.guildOnly()));
                }
            } catch (Exception e) { Log.warn("An error occurred while registering an interaction. {}", e); }}
        }

        bot.updateCommands().addCommands(commands).queue();

        bot.addEventListener(new InteractionListener());
    }

    private static void processCommand(SlashCommandInteraction commandAnnotation, LocalizationFunction localizationFunction, Class<?> commandClass, Method method, List<CommandData> commands) {
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
        if (commands.stream().noneMatch(c -> c.getName().equals(commandName))) commandData = Commands.slash(commandName, commandAnnotation.description()).setLocalizationFunction(localizationFunction);
        else {
            commandData = (SlashCommandData) commands.stream().filter(c -> c.getName().equals(commandName)).findFirst().orElse(null);
            commandExists = true;
        }

        if (commandData == null) {
            Log.err("An error occurred while registering the command: " + fullCommandName);
            return;
        }

        method.setAccessible(true);
        Command command = new Command(commandClass, method, fullCommandName, commandAnnotation.guildOnly());
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

        Permission[] permissions = commandAnnotation.permissions();
        if (permissions.length > 0 && permissions[0] != Permission.UNKNOWN)
            commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(permissions));
        
        if (commandAnnotation.guildOnly())
            commandData.setContexts(InteractionContextType.GUILD);
        else commandData.setContexts(commandAnnotation.contextTypes());
        
        commandData.setIntegrationTypes(commandAnnotation.integrationTypes());
        commandData.setNSFW(commandAnnotation.nsfw());
        
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
        
        List<OptionData> optionsData = new ArrayList<>();

        for (Parameter parameter : parameters) {
            if (GenericEvent.class.isAssignableFrom(parameter.getType())) continue; // Skip the event parameter
            
            Option option = parameter.getAnnotation(Option.class);
            if (option == null) {
                Log.warn("Parameter {} in method {}.{}() is missing the @Option annotation. You might have forgotten to add it.", parameter.getName(), command.getClazz(), command.getMethod().getName());
                continue;
            }

            String name = option.name();
            if (name.isEmpty()) name = parameter.getName();
            // Separate at uppercase letters and convert to lowercase with underscores
            name = name.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();

            Class<?> type = parameter.getType();
            OptionType optionType;

            if (type == String.class) optionType = OptionType.STRING;
            else if (type == Integer.class || type == Long.class) optionType = OptionType.INTEGER;
            else if (type == Boolean.class) optionType = OptionType.BOOLEAN;
            else if (type == IMentionable.class) optionType = OptionType.MENTIONABLE; // Need to be before User, Member, Role and Channel
            else if (User.class.isAssignableFrom(type) || type == Member.class) optionType = OptionType.USER;
            else if (Channel.class.isAssignableFrom(type)) optionType = OptionType.CHANNEL;
            else if (type == Role.class) optionType = OptionType.ROLE;
            else if (type == Double.class) optionType = OptionType.NUMBER;
            else if (type == Message.Attachment.class) optionType = OptionType.ATTACHMENT;
            else {
                String warnMessage = "Unsupported parameter type {} in command: {}";
                if (type.isPrimitive())
                    warnMessage += " (primitive types are not supported, use their wrapper class instead)";
                
                Log.warn(warnMessage, type.getName(), command.getName());
                continue;
            }
            
            boolean autoComplete = option.autoComplete() && optionType.canSupportChoices();
            OptionData optionData = new OptionData(optionType, name, option.description(), option.required(), autoComplete);
            if (optionType == OptionType.CHANNEL && option.channelTypes().length > 0 && option.channelTypes()[0] != ChannelType.UNKNOWN)
                optionData.setChannelTypes(option.channelTypes());
            
            if (optionType == OptionType.INTEGER || optionType == OptionType.NUMBER) {
                if (option.minValue() != Long.MIN_VALUE)
                    optionData.setMinValue(option.minValue());
                if (option.maxValue() != Long.MAX_VALUE)
                    optionData.setMaxValue(option.maxValue());
            }
            
            if (optionType == OptionType.STRING) {
                if (option.minLength() >= 0)
                    optionData.setMinLength(option.minLength());
                if (option.maxLength() >= 1)
                    optionData.setMaxLength(option.maxLength());
            }
            
            command.addParameter(new Command.Parameter(type, name, option.required(), autoComplete));
            optionsData.add(optionData);
        }
        
        if (commandData != null) commandData.addOptions(optionsData);
        else subcommandData.addOptions(optionsData);
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
            
            if (command.isGuildOnly() && !event.isFromGuild()) {
                event.reply("Cette commande ne peut être utilisée que dans un serveur.").setEphemeral(true).queue();
                return;
            }
            
            try {
                Object o = command.getClazz().getDeclaredConstructor().newInstance();

                List<Object> args = new ArrayList<>();
                args.add(event); // First parameter is always the event
                for (Command.Parameter parameter : command.getParameters()) {
                    OptionMapping option = event.getOption(parameter.name());

                    if (option == null && parameter.required()) {
                        Log.warn("A required parameter is missing for command {}, parameter: {}", command.getName(), parameter.name());
                        event.reply("Un paramètre obligatoire est manquant.").queue();
                        return;
                    }

                    if (option == null) {
                        args.add(null);
                        continue;
                    }

                    switch (option.getType()) {
                        case STRING -> args.add(option.getAsString());
                        case INTEGER -> args.add(parameter.type() == Long.class ? option.getAsLong() : option.getAsInt());
                        case BOOLEAN -> args.add(option.getAsBoolean());
                        case USER -> args.add(User.class.isAssignableFrom(parameter.type()) ? option.getAsUser() : option.getAsMember());
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
            
            if (contextInteraction.isGuildOnly() && !event.isFromGuild()) {
                event.reply("Cette interaction ne peut être utilisée que dans un serveur.").setEphemeral(true).queue();
                return;
            }

            try {
                Object o = contextInteraction.getClazz().getDeclaredConstructor().newInstance();

                contextInteraction.getMethod().invoke(o, event);
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
                Log.err("An error occurred during context execution ({}):", contextInteraction.getName(), e);
            }
        }
        
        @Override
        public void onGenericComponentInteractionCreate(GenericComponentInteractionCreateEvent event) {
            String componentId = event.getComponentId();
            
            Interaction component = interactionMethods.get(componentId);
            if (component == null) {
                Log.warn("Unknown component: {}", componentId);
                event.reply("Composant inconnu.").setEphemeral(true).queue();
                return;
            }
            
            if (component.isGuildOnly() && !event.isFromGuild()) {
                event.reply("Ce composant ne peut être utilisé que dans un serveur.").setEphemeral(true).queue();
                return;
            }
            
            try {
                Object o = component.getClazz().getDeclaredConstructor().newInstance();

                component.getMethod().invoke(o, event);
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
                Log.err("An error occurred during component execution ({}):", component.getName(), e);
            }
        }
    }
}
