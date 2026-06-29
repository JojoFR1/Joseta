package dev.jojofr.joseta.annotations;

import dev.jojofr.joseta.annotations.interactions.Command;
import dev.jojofr.joseta.annotations.interactions.Interaction;
import dev.jojofr.joseta.annotations.types.*;
import dev.jojofr.joseta.annotations.types.interaction.ContextCommandInteraction;
import dev.jojofr.joseta.annotations.types.interaction.SlashCommandInteraction;
import dev.jojofr.joseta.utils.Log;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.ICustomIdInteraction;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Processor for scanning, registering interaction ({@link SlashCommandInteraction}, {@link ContextCommandInteraction}, and {@link Interaction}) and handling them.
 * <p>
 * It scans the specified package for classes annotated with {@link InteractionModule} and registers their methods
 * annotated with {@link SlashCommandInteraction} as slash commands with the JDA bot instance.
 * <p>
 * The processor sets up event listeners to handle incoming interactions and invoke the corresponding command methods.
 */
public class InteractionProcessor {
    private static final Map<String, Interaction> interactionMethods = new HashMap<>();
    private static final Pattern NAME_REGEX = Pattern.compile("([a-z])([A-Z]+)");
    
    /**
     * Initializes the interaction processor by scanning the specified package for classes annotated with {@link InteractionModule},
     * registering their commands, and setting up event listeners with the provided JDA bot instance.
     *
     * @param bot   The JDA bot instance to register commands with.
     * @param index The Jandex index to use for scanning for {@link InteractionModule} classes and their annotated methods.
     */
    // TODO if error come from JDA we dont know which interaction caused it
    // TODO maybe cache the "global" event to avoid needing to recheck each time
    public static void initialize(JDA bot, Index index) {
        Set<Class<?>> classes = new HashSet<>();
        for (AnnotationInstance annotation : index.getAnnotations(DotName.createSimple(InteractionModule.class))) {
            String className = annotation.target().asClass().name().toString();
            try { classes.add(Class.forName(className)); } catch (ClassNotFoundException e) { Log.err("Failed to load interaction class: " + className, e); }
        }
        
        Map<String, CommandData> commands = new HashMap<>();
        for (Class<?> commandClass : classes) {
            for (Method method : commandClass.getDeclaredMethods()) { try {
                SlashCommandInteraction commandInteraction = method.getAnnotation(SlashCommandInteraction.class);
                if (commandInteraction != null) {
                    processCommand(commandInteraction, commandClass, method, commands);
                    continue;
                }

                ContextCommandInteraction contextCommandInteraction = method.getAnnotation(ContextCommandInteraction.class);
                if (contextCommandInteraction != null) {
                    String name = contextCommandInteraction.name();
                    if (name.isEmpty()) name = method.getName().toLowerCase();

                    net.dv8tion.jda.api.interactions.commands.Command.Type type = contextCommandInteraction.type();
                    method.setAccessible(true);

                    CommandData commandData = Commands.context(type, name);
                    
                    Permission[] permissions = contextCommandInteraction.permissions();
                    if (permissions.length > 0 && permissions[0] != Permission.UNKNOWN)
                        commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(permissions));
                    
                    if (contextCommandInteraction.guildOnly())
                        commandData.setContexts(InteractionContextType.GUILD);
                    else commandData.setContexts(contextCommandInteraction.contextTypes());
                    
                    commandData.setIntegrationTypes(contextCommandInteraction.integrationTypes());
                    commandData.setNSFW(contextCommandInteraction.nsfw());
                    
                    commands.put(name, commandData);
                    interactionMethods.put(name, new Interaction(commandClass, method, name, contextCommandInteraction.guildOnly()));
                    continue;
                }
                
                dev.jojofr.joseta.annotations.types.interaction.Interaction genericInteraction = method.getAnnotation(dev.jojofr.joseta.annotations.types.interaction.Interaction.class);
                if (genericInteraction != null) {
                    String id = genericInteraction.id();
                    if (id.isEmpty()) id = method.getName().toLowerCase();
                    method.setAccessible(true);
                    interactionMethods.put(id, new Interaction(commandClass, method, id, genericInteraction.guildOnly()));
                }
            } catch (Exception e) { Log.warn("An error occurred while registering an interaction.", e); }}
        }
        
        bot.updateCommands().addCommands(commands.values()).queue();
        bot.addEventListener(new InteractionListener());
    }

    private static void processCommand(SlashCommandInteraction commandAnnotation, Class<?> commandClass, Method method, Map<String, CommandData> commands) {
        String[] baseCommandName = commandAnnotation.name().isEmpty() ? null : commandAnnotation.name().split(" ");
        if (baseCommandName == null) {
            baseCommandName = method.getName().split("(?=\\p{Upper})");
            for (int i = 0; i < baseCommandName.length; i++) baseCommandName[i] = baseCommandName[i].toLowerCase();
        }
        
        String commandName = baseCommandName[0], subcommandName, subcommandGroupName;
        if (baseCommandName.length > 3) Log.warn("Command {} too long (max 3 parts).", commandName);
        
        // Required to be initialized in if statements because they need to be effectively final for the lambda expressions later
        if (baseCommandName.length == 2) {
            subcommandGroupName = "";
            subcommandName = baseCommandName[1];
        } else if (baseCommandName.length >= 3) {
            subcommandGroupName = baseCommandName[1];
            subcommandName = baseCommandName[2];
        } else {
            subcommandGroupName = "";
            subcommandName = "";
        }
        String fullCommandName = commandName;
        if (!subcommandGroupName.isEmpty()) fullCommandName += " " + subcommandGroupName;
        if (!subcommandName.isEmpty()) fullCommandName += " " + subcommandName;
        
        SlashCommandData commandData = (SlashCommandData) commands.computeIfAbsent(commandName, name -> Commands.slash(name, commandAnnotation.description()));
        
        method.setAccessible(true);
        Command command = new Command(commandClass, method, fullCommandName, commandAnnotation.guildOnly());
        interactionMethods.put(fullCommandName, command);
        
        if (!subcommandName.isEmpty()) {
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
            if (!subcommandGroupName.isEmpty()) {
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
                Log.warn("Parameter {} is missing the @Option annotation. You might have forgotten to add it.", parameter.getName());
                continue;
            }
            
            String name = option.name();
            if (name.isEmpty()) name = parameter.getName();
            // Separate at uppercase letters and convert to lowercase with underscores
            name = NAME_REGEX.matcher(name).replaceAll("$1_$2").toLowerCase();
            
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
            long startTime = System.nanoTime();
            String commandName = event.getName();
            String subcommandGroup = event.getSubcommandGroup();
            String subcommand = event.getSubcommandName();

            if (subcommandGroup != null) commandName += " " + subcommandGroup;
            if (subcommand != null) commandName += " " + subcommand;
            
            Command command = (Command) interactionMethods.get(commandName);
            if (command == null) {
                Log.warn("Unknown command: " + event.getName());
                event.reply("Commande `"+ commandName +"` inconnue. Veuillez contacter un développeur si l'erreur persiste.").setEphemeral(true).queue();
                return;
            }
            
            if (command.isGuildOnly() && !event.isFromGuild()) {
                event.reply("Cette commande ne peut être utilisée que dans un serveur.").setEphemeral(true).queue();
                return;
            }
            
            try {
                Object[] args = new Object[command.getParameters().size() + 1];
                args[0] = event; // First parameter is always the event
                int i = 1;
                for (Command.Parameter parameter : command.getParameters()) {
                    OptionMapping option = event.getOption(parameter.name());

                    if (option == null && parameter.required()) {
                        Log.warn("A required parameter is missing for command {}, parameter: {}", command.getName(), parameter.name());
                        event.reply("Un paramètre obligatoire est manquant. Veuillez contacter un développeur si l'erreur persiste.").queue();
                        return;
                    }
                    
                    // The option is optional and the user did not provide anything
                    if (option == null) {
                        args[i++] = null;
                        continue;
                    }

                    switch (option.getType()) {
                        case STRING -> args[i++] = option.getAsString();
                        case INTEGER -> {
                            if (parameter.type() == Long.class) args[i++] = option.getAsLong();
                            else args[i++] = option.getAsInt();
                        }
                        case BOOLEAN -> args[i++] = option.getAsBoolean();
                        case USER -> args[i++] = User.class.isAssignableFrom(parameter.type()) ? option.getAsUser() : option.getAsMember();
                        case CHANNEL -> args[i++] = option.getAsChannel();
                        case ROLE -> args[i++] = option.getAsRole();
                        case MENTIONABLE -> args[i++] = option.getAsMentionable();
                        case NUMBER -> args[i++] = option.getAsDouble();
                        case ATTACHMENT -> args[i++] = option.getAsAttachment();
                        default -> args[i++] = null;
                    }
                }
                
                command.getHandle().invokeExact(args);
            } catch (InsufficientPermissionException e) {
                event.reply("Je n'ai pas les permissions requises (" + e.getPermission().getName() + ") pour exécuter `" + event.getName() + "`.").setEphemeral(true).queue();
            } catch (Throwable e) {
                Log.err("An error occurred during command execution ({}):", e, command.getName());
            }
            
            long endTime = System.nanoTime();
            Log.debug("Command {} processed in {} ms", event.getName(), (endTime - startTime) / 1_000_000.0);
        }

        @Override
        public void onGenericContextInteraction(GenericContextInteractionEvent<?> event) {
            long startTime = System.nanoTime();
            String commandName = event.getName();

            Interaction contextInteraction = interactionMethods.get(commandName);
            if (contextInteraction == null) {
                Log.warn("Unknown context interaction: " + event.getName());
                event.reply("Interaction `"+ commandName +"` inconnue. Veuillez contacter un développeur si l'erreur persiste.").setEphemeral(true).queue();
                return;
            }
            
            if (contextInteraction.isGuildOnly() && !event.isFromGuild()) {
                event.reply("Cette interaction ne peut être utilisée que dans un serveur.").setEphemeral(true).queue();
                return;
            }

            try {
                contextInteraction.getHandle().invokeExact(event);
            } catch (Exception e) {
                if (e instanceof InsufficientPermissionException ie) {
                    event.reply("Je n'ai pas les permissions requises (" + ie.getPermission().getName() + ") pour exécuter `" + event.getName() + "`.").setEphemeral(true).queue();
                    return;
                }
                
                Log.err("An error occurred during command execution ({}):", e, contextInteraction.getName());
            } catch (Throwable e) {
                Log.err("An error occurred during command execution ({}):", e, contextInteraction.getName());
            }
            
            long endTime = System.nanoTime();
            Log.debug("Context interaction {} processed in {} ms", event.getName(), (endTime - startTime) / 1_000_000.0);
        }
        
        @Override
        public void onGenericInteractionCreate(GenericInteractionCreateEvent event) {
            if (event instanceof SlashCommandInteractionEvent || event instanceof GenericContextInteractionEvent)
                return;
            
            long startTime = System.nanoTime();
            
            if (!(event.getInteraction() instanceof ICustomIdInteraction customIdInteraction)) {
                Log.warn("Received an interaction that is not a ICustomIdInteraction: {}", event.getClass().getName());
                return;
            }
            String interactionId = customIdInteraction.getCustomId();
            
            if (!(event instanceof IReplyCallback replyCallback)) {
                Log.warn("Received an interaction that is not a IReplyCallback: {}", event.getClass().getName());
                return;
            }
            
            Interaction interaction = interactionMethods.get(interactionId);
            if (interaction == null) {
                String globalInteractionId = "";
                for (String key : interactionMethods.keySet()) {
                    if (key.startsWith("*")) {
                        if (interactionId.endsWith(key.substring(1))) {
                            globalInteractionId = key;
                            break;
                        }
                    } else if (key.endsWith("*")) {
                        if (interactionId.startsWith(key.substring(0, key.length() - 1))) {
                            globalInteractionId = key;
                            break;
                        }
                    } else if (key.contains("*")) {
                        String[] parts = key.split("\\*", 2);
                        if (interactionId.startsWith(parts[0]) && interactionId.endsWith(parts[1])) {
                            globalInteractionId = key;
                            break;
                        }
                    }
                }
                
                interaction = interactionMethods.get(globalInteractionId);
            }
            
            // The interaction 100% doesn't exist
            if (interaction == null) {
                Log.warn("Unknown interaction: {}", interactionId);
                replyCallback.reply("Composant `"+ interactionId +"` inconnu. Veuillez contacter un développeur si l'erreur persiste.").setEphemeral(true).queue();
                return;
            }
            
            if (interaction.isGuildOnly() && !event.isFromGuild()) {
                replyCallback.reply("Ce composant ne peut être utilisé que dans un serveur.").setEphemeral(true).queue();
                return;
            }
            
            try {
                interaction.getHandle().invokeExact(event);
            } catch (Exception e) {
                if (e instanceof InsufficientPermissionException ie) {
                    replyCallback.reply("Je n'ai pas les permissions requises (" + ie.getPermission().getName() + ") pour exécuter `" + interaction.getName() + "`.").setEphemeral(true).queue();
                    return;
                }
                
                Log.err("An error occurred during command execution ({}):", e, interaction.getName());
            } catch (Throwable e) {
                Log.err("An error occurred during command execution ({}):", e, interaction.getName());
            }
            
            long endTime = System.nanoTime();
            Log.debug("Component interaction {} processed in {} ms", interactionId, (endTime - startTime) / 1_000_000.0);
        }
    }
}
