package joseta;

import ch.qos.logback.classic.*;
import io.github.cdimascio.dotenv.*;
import joseta.annotations.Command;
import joseta.annotations.modules.*;
import joseta.annotations.*;
import joseta.utils.*;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.hooks.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.requests.*;
import net.dv8tion.jda.api.utils.*;
import org.reflections.*;

import java.io.*;
import java.lang.reflect.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;

import static org.reflections.scanners.Scanners.*;

public class JosetaBot {
    private static boolean debug = false;
    private static final Map<String, Command> commandMethods = new HashMap<>();

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();


        if (args.length > 0) {
            debug = args[0].equals("--debug");
            Log.setLevel(Level.DEBUG);
        }

        debug = true;

        JDA bot = JDABuilder.createDefault(dotenv.get("TOKEN" + (debug ? "_DEV" : "")))
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .enableIntents(GatewayIntent.GUILD_MESSAGES,
                           GatewayIntent.GUILD_MEMBERS,
                           GatewayIntent.MESSAGE_CONTENT)
            .addEventListeners(
                new ListenerAdapter() {
                    @Override
                    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
                        Log.info("HELOO");
                        Command command = commandMethods.get(event.getName());

                        try {
                            if (command != null) {
                                Object o = command.getClazz().getDeclaredConstructor(SlashCommandInteractionEvent.class).newInstance(event);

                                List<Object> args = new ArrayList<>();
                                for (Command.Parameter parameter : command.getParameters()) {
                                    OptionMapping option = event.getOption(parameter.getName());

                                    if (option == null && parameter.isRequired()) {
                                        event.reply("TODO").queue();
                                        Log.warn("aziijfzeijezfjfijfdfsjkdfkjdfkj");
                                        return;
                                    }

                                    if (option == null) {
                                        args.add(null);
                                        continue;
                                    }

                                    switch (option.getType()) {
                                        case STRING -> args.add(option.getAsString());
                                        case INTEGER -> args.add((parameter.getType() == Long.class || parameter.getType() == long.class) ? option.getAsLong() : option.getAsInt());
                                        case NUMBER -> args.add(option.getAsDouble());
                                        case BOOLEAN -> args.add(option.getAsBoolean());
                                        case USER -> args.add(parameter.getType().isAssignableFrom(User.class) ? option.getAsUser() : option.getAsMember());
                                        case ROLE -> args.add(option.getAsRole());
                                        case MENTIONABLE -> args.add(
                                            parameter.getType().isAssignableFrom(User.class) ? option.getAsUser() :
                                            parameter.getType().isAssignableFrom(Member.class) ? option.getAsMember() :
                                            parameter.getType().isAssignableFrom(Role.class) ? option.getAsRole() : option.getAsMentionable()
                                        );
                                        case CHANNEL -> args.add(option.getAsChannel());
                                        case ATTACHMENT -> args.add(option.getAsAttachment());
                                        default -> args.add(null);
                                    }
                                }

                                command.getMethod().invoke(o, args.toArray());
                            }
                        } catch (IllegalAccessException | InvocationTargetException | InstantiationException |
                                 NoSuchMethodException e) {
                            Log.err("An error occurred during command execution ({}):", command.getName(), e);
                        }
                    }
                }
            )
            .setStatus(OnlineStatus.DO_NOT_DISTURB)
            .setActivity(Activity.watching("🇫🇷 Mindustry France."))
            .build();

        try { bot.awaitReady(); } catch (InterruptedException e) {
            Log.err("An error occurred while waiting for the bot to be ready (connected).", e);
            System.exit(1);
        }

        try {
            test(bot, "joseta.commands");
        } catch (InvocationTargetException | IllegalAccessException | IOException e) {
            throw new RuntimeException(e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            bot.setAutoReconnect(false);
            bot.shutdown();

            try {
                if (!bot.awaitShutdown(10, TimeUnit.SECONDS)) {
                    Log.warn("The shutdown 10 second limit was exceeded. Force shutting down...");
                    bot.shutdownNow();
                    bot.awaitShutdown();
                }
            } catch (InterruptedException e) {
                Log.err("An error occurred while waiting for the bot to shutdown. Force shutting down...", e);
                bot.shutdownNow();
            }
        }, "ShutdownThread"));
    }

    public static void test(JDA bot, String packageName) throws InvocationTargetException, IllegalAccessException, IOException {
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> classes = reflections.get(SubTypes.of(CommandModule.class).asClass());

        List<SlashCommandData> commands = new ArrayList<>();
        for (Class<?> commandClass : classes) {
            for (Method method : commandClass.getMethods()) {
                SlashCommand commandAnnotation = method.getAnnotation(SlashCommand.class);
                if (commandAnnotation != null) {
                    String commandName = commandAnnotation.name();
                    if (commandName.isEmpty()) commandName = method.getName();
                    SlashCommandData commandData = Commands.slash(commandName, commandAnnotation.description());

                    method.setAccessible(true);
                    Command command = new Command(commandClass, method, commandName);
                    commandMethods.put(commandName, command);

                    for (Parameter parameter : method.getParameters()) {
                        Option option = parameter.getAnnotation(Option.class);
                        if (option != null) {
                            String name = option.name();
                            if (name.isEmpty()) name = parameter.getName();

                            Class<?> type = parameter.getType();

                            OptionType optionType = OptionType.STRING;

                            // Missing MENTIONABLE
                            if (type == String.class) optionType = OptionType.STRING;
                            else if (type == int.class || type == Integer.class) optionType = OptionType.INTEGER;
                            else if (type == long.class || type == Long.class) optionType = OptionType.INTEGER;
                            else if (type == double.class || type == Double.class) optionType = OptionType.NUMBER;
                            else if (type == boolean.class || type == Boolean.class) optionType = OptionType.BOOLEAN;
                            else if (type == User.class || type == Member.class) optionType = OptionType.USER;
                            else if (type == Role.class) optionType = OptionType.ROLE;
                            else if (type == Message.Attachment.class) optionType = OptionType.ATTACHMENT;
                            else if (type.isAssignableFrom(Channel.class)) optionType = OptionType.CHANNEL;
                            else {
                                Log.warn("Unsupported parameter type: " + type.getName() + " in command: " + command.getName());
                            }

                            command.addParameter(new Command.Parameter(type, name, option.required()));
                            commandData.addOption(optionType, name, option.description(), option.required());
                        }
                    }

                    commands.add(commandData);
                }
            }
        }

        bot.updateCommands().addCommands(commands).queue(); // Reset for the guilds command to avoid duplicates.
    }
}