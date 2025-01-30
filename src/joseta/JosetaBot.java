package joseta;

import joseta.commands.*;
import joseta.commands.ModCommand.*;
import joseta.commands.admin.*;
import joseta.commands.misc.*;
import joseta.commands.moderation.*;
import joseta.events.*;
import joseta.utils.*;
import joseta.utils.struct.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.requests.*;
import net.dv8tion.jda.api.utils.*;

import org.slf4j.*;

import java.util.concurrent.*;

import ch.qos.logback.classic.*;
import ch.qos.logback.classic.Logger;

public class JosetaBot {
    private static JDA bot;
    public static final Logger logger = (Logger) LoggerFactory.getLogger(JosetaBot.class);

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static final Seq<Command> commands = Seq.with(
        new PingCommand(),
        new MultiInfoCommand(),

        new AdminCommand(),
        
        new WarnCommand(),
        new UnwarnCommand(),
        new MuteCommand(),
        new UnmuteCommand(),
        new KickCommand(),
        new BanCommand(),
        new UnbanCommand(),
        new ModLogCommand()
    );

    //TODO Clean this up
    public static void main(String[] args) {
        registerShutdown();

        if (args.length > 0) {
            Vars.setDebug(args[0].equals("--debug"));
            Vars.setServer(args[0].equals("--server"));
        }
        preLoad();
        
        bot = JDABuilder.createDefault(Vars.token)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MESSAGES,
                               GatewayIntent.GUILD_MEMBERS,
                               GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new CommandExecutor(),
                                   new WelcomeMessage(),
                                   new RulesAcceptEvent(),
                                   new ModLogButtonEvents(),
                                   new ModAutoComplete(), //TODO fix
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

        //TODO move this somewhere else
        scheduler.scheduleAtFixedRate(JosetaBot::checkExpiredSanctions, 0, 60, TimeUnit.SECONDS);

        initializeCommands();
        ModLog.initialize();
    }

    private static void preLoad() {
        Vars.loadSecrets();

        if (Vars.isDebug || Vars.isServer) {
            Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.setLevel(Level.DEBUG);
            
            // The appender has to be referenced in logback.xml to 'exist', 
            // so instead of adding it if it's a server, it's removed if otherwise
            if (!Vars.isServer) rootLogger.detachAppender("FILE");
        }

        Vars.cacheWelcomeImage();
    }

    private static void initializeCommands() {
        Seq<CommandData> commandsData = new Seq<>();
        commands.each(cmd -> commandsData.add(Commands.slash(cmd.name, cmd.description).addSubcommands(cmd.subcommands).addOptions(cmd.options).setDefaultPermissions(cmd.defaultPermissions)));

        // Add commands on a test guild - Instantly
        if (Vars.isDebug && Vars.testGuildId != -1)
            bot.getGuildById(Vars.testGuildId).updateCommands().addCommands(commandsData.toArray(CommandData.class)).queue();
        // Add global commands - Takes time
        else {
            bot.getGuilds().forEach(g -> g.updateCommands().addCommands().queue()); // Reset for the guilds command to avoid duplicates.
            bot.updateCommands().addCommands(commandsData.toArray(CommandData.class)).queue();
        }
    }


    private static void checkExpiredSanctions() {
        ModLog.getExpiredSanctions().each(sanction -> {
            if (sanction.getSanctionTypeId() == SanctionType.BAN) {
                Guild guild = bot.getGuildById(sanction.guildId);
                guild.retrieveBanList().queue(bans -> {
                    bans.forEach(ban -> {
                        if (ban.getUser().getIdLong() == sanction.userId)
                            guild.unban(ban.getUser()).queue();
                    });
                });
            }
            ModLog.removeSanction(sanction);
        });
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
