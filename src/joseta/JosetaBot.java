package joseta;

import joseta.commands.*;
import joseta.commands.admin.*;
import joseta.commands.misc.*;
import joseta.commands.moderation.*;
import joseta.database.*;
import joseta.database.entry.*;
import joseta.database.helper.*;
import joseta.events.*;
import joseta.events.misc.*;

import arc.struct.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.requests.*;
import net.dv8tion.jda.api.utils.*;

import org.slf4j.*;

import java.sql.*;
import java.util.concurrent.*;

import ch.qos.logback.classic.*;
import ch.qos.logback.classic.Logger;

public class JosetaBot {
    public static JDA bot;
    public static final Logger logger = (Logger) LoggerFactory.getLogger(JosetaBot.class);

    public static final Seq<Command> commands = Seq.with(
        new AdminCommand(),
        new ConfigCommand(),

        new PingCommand(),
        new MultiInfoCommand(),
        new MarkovCommand(),
        
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
    public static void main(String[] args) throws SQLException {
        registerShutdown();
        preLoad(args);

        Databases.getInstance(); // Should do the first initialization.        
        
        bot = JDABuilder.createDefault(Vars.token)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MESSAGES,
                               GatewayIntent.GUILD_MEMBERS,
                               GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new EventHandler())
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
        WelcomeMessage.initialize();
        SanctionDatabaseHelper.startScheduler();

        // TODO that ugly, pls help pinpin
        for (Guild guild : bot.getGuilds()) {
            if (Databases.getInstance().getGuildDao().queryForEq("guildId", guild.getIdLong()).isEmpty())
                Databases.getInstance().getGuildDao().create(new GuildEntry(guild));

            if (Databases.getInstance().getConfigDao().queryForEq("guildId", guild.getIdLong()).isEmpty())
                Databases.getInstance().getConfigDao().create(new ConfigEntry(guild.getIdLong()));

            //TODO populate with config disabled by default + no markov black list defined
            if (Databases.getInstance().getMessageDao().queryForEq("guildId", guild.getIdLong()).isEmpty()) {
                MessagesDatabaseHelper.populateNewGuild(guild);
                MarkovMessagesDatabaseHelper.populateNewGuild(guild);
            }
        }
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
        SlashCommandData[] commandsData = commands.map(cmd -> cmd.getCommandData()).toArray(SlashCommandData.class);
        
        bot.getGuilds().forEach(g -> g.updateCommands().addCommands().queue()); // Reset for the guilds command to avoid duplicates.
        bot.updateCommands().addCommands(commandsData).queue();
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
                        bot.awaitShutdown();
                    }
                } catch (InterruptedException e) {
                    JosetaBot.logger.error("An error occured while waitin for the bot to shutdown. Force shutting down...", e);
                    bot.shutdownNow();
                }
            }
        }, "ShutdownThread"));
    } 
}
