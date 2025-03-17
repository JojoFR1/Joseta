package joseta;

import joseta.commands.*;
import joseta.commands.Command;
import joseta.commands.admin.*;
import joseta.commands.misc.*;
import joseta.commands.moderation.*;
import joseta.events.*;
import joseta.utils.*;

import arc.struct.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.requests.*;
import net.dv8tion.jda.api.utils.*;
import net.dv8tion.jda.api.utils.cache.*;

import org.slf4j.*;

import java.util.concurrent.*;

import ch.qos.logback.classic.*;
import ch.qos.logback.classic.Logger;

public class JosetaBot {
    public static JDA bot;
    public static final Logger logger = (Logger) LoggerFactory.getLogger(JosetaBot.class);

    public static final Seq<Command> commands = Seq.with(
        new PingCommand(),
        new MultiInfoCommand(),

        new AdminCommand(),
        
        new WarnCommand(),
        new UnwarnCommand(),
        new TimeoutCommand(),
        new UntimeoutCommand(),
        new KickCommand(),
        new BanCommand(),
        new UnbanCommand(),
        new ModLogCommand()
    );

    /* Load - Before the bot load
     * Initialize - After the bot load (after awaitReady)
     */
    public static void main(String[] args) {
        registerShutdown();
        preLoad(args);
        
        bot = JDABuilder.createDefault(Vars.token)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MESSAGES,
                               GatewayIntent.GUILD_MESSAGE_REACTIONS,
                               GatewayIntent.GUILD_MEMBERS,
                               GatewayIntent.GUILD_EXPRESSIONS,
                               GatewayIntent.GUILD_MODERATION,
                               GatewayIntent.GUILD_VOICE_STATES,
                               GatewayIntent.SCHEDULED_EVENTS,
                               GatewayIntent.MESSAGE_CONTENT)
                .enableCache(CacheFlag.EMOJI,
                             CacheFlag.STICKER,
                             CacheFlag.SCHEDULED_EVENTS,
                             CacheFlag.VOICE_STATE)
                .addEventListeners(new CommandExecutor(),
                                   new WelcomeMessage(),
                                   new LogSystem(),
                                   new RulesAcceptEvent(),
                                   new ModLogButtonEvents(),
                                   new ModAutoComplete(),
                                   new AutoResponse())
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setActivity(Activity.watching("🇫🇷 Mindustry France."))
                .build();
        
        // Required to access guild and register commands.
        try {
            bot.awaitReady();
        } catch (InterruptedException e) {
            JosetaBot.logger.error("An error occured while waiting for the bot to connect.", e);
            System.exit(1);
        }

        initializeCommands();
        Vars.initialize(bot.getGuildById(Vars.testGuildId)); // TODO another way than that pls (SHOULD be fixed with config later)
        WelcomeMessage.initialize();
        ModLog.initialize();
    }

    private static void preLoad(String args[]) {
        if (args.length > 0) {
            Vars.setDebug(args[0].equals("--debug"));
            Vars.setServer(args[0].equals("--server"));
        }

        Vars.loadSecrets();

        if (Vars.isDebug || Vars.isServer) {
            Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.setLevel(Level.DEBUG);
            
            // The appender has to be referenced in logback.xml to 'exist', 
            // so instead of adding it if it's a server, it's removed if otherwise
            if (!Vars.isServer) rootLogger.detachAppender("FILE");
        }
    }
    
    private static void initializeCommands() {
        Seq<CommandData> commandsData = new Seq<>();
        commands.each(cmd -> commandsData.add(Commands.slash(cmd.name, cmd.description).addSubcommands(cmd.subcommands).addSubcommandGroups(cmd.subcommandGroupsData).addOptions(cmd.options).setDefaultPermissions(cmd.defaultPermissions)));

        // Add commands on a test guild - Instantly
        if (Vars.isDebug && Vars.testGuildId != -1)
            bot.getGuildById(Vars.testGuildId).updateCommands().addCommands(commandsData.toArray(CommandData.class)).queue();
        // Add global commands - Takes time
        else {
            bot.getGuilds().forEach(g -> g.updateCommands().addCommands().queue()); // Reset for the guilds command to avoid duplicates.
            bot.updateCommands().addCommands(commandsData.toArray(CommandData.class)).queue();
        }
    }

    private static void registerShutdown() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            JosetaBot.logger.info("Shutting down...");
            
            if (bot != null) {
                bot.setAutoReconnect(false);
                bot.shutdown();
                
                try {
                    if (!bot.awaitShutdown(10, TimeUnit.SECONDS)) {
                        JosetaBot.logger.warn("The shutdown 10 second limit was exceeded. Force shutting down...");    
                        bot.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    JosetaBot.logger.error("An error occured while waitin for the bot to shutdown. Force shutting down...", e);
                    bot.shutdownNow();
                }
            }
        }, "ShutdownThread"));
    } 
}
