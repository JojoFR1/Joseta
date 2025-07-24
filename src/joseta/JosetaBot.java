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
import arc.util.*;
import arc.util.Log.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.requests.*;
import net.dv8tion.jda.api.utils.*;

import org.slf4j.*;

import java.sql.*;
import java.time.*;
import java.util.concurrent.*;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class JosetaBot {
    private static JDA bot;

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
        new ClearCommand(),
        new ModLogCommand()
    );

    /* Load - Before the bot load
     * Initialize - After the bot load (after awaitReady)
     */
    public static void main(String[] args) throws SQLException {
        registerShutdown();
        preLoad(args);
        
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
        try { bot.awaitReady(); } catch (InterruptedException e) {
            Log.err("An error occurred while waiting for the bot to be ready (connected).", e);
            System.exit(1);
        }

        SlashCommandData[] commandsData = commands.map(cmd -> cmd.getCommandData()).toArray(SlashCommandData.class);
        bot.getGuilds().forEach(g -> g.updateCommands().addCommands().queue()); // Reset for the guilds command to avoid duplicates.
        bot.updateCommands().addCommands(commandsData).queue();

        WelcomeMessage.initialize(); //TODO Maybe change that?
        SanctionDatabaseHelper.startScheduler(15); // Check expired sanctions every 15 minutes.

        for (Guild guild : bot.getGuilds()) {
            if (Database.get(GuildEntry.class, guild.getIdLong()) == null)
                Database.create(new GuildEntry(guild));

            if (Database.get(ConfigEntry.class, guild.getIdLong()) == null)
                Database.create(new ConfigEntry(guild.getIdLong()));

            //TODO populate with config disabled by default + no markov black list defined

            if (Database.querySelect(MessageEntry.class, (cb, rt) -> cb.equal(rt.get(MessageEntry_.guildId), guild.getIdLong()))
                    .getResultList().isEmpty()) {
                Log.debug("Populating the Messages Database for guild: " + guild.getName() + " (" + guild.getId() + ")");
                MessagesDatabaseHelper.populateNewGuild(guild);
            }
        }
    }

    private static void preLoad(String[] args) {
        if (args.length > 0) {
            Vars.setDebug(args[0].equals("--debug"));
            Vars.setServer(args[0].equals("--server"));
        }

        Vars.loadSecrets();

        Log.useColors = false;
        Log.logger = new Log.LogHandler() {
            @Override public void log(LogLevel level, String text) {
                Logger logger = (Logger) LoggerFactory.getLogger(JosetaBot.class);
                
                switch (level) {
                    case debug: logger.debug(text); break;
                    case info: logger.info(text); break;
                    case warn: logger.warn(text); break;
                    case err: logger.error(text); break;
                    case none: default: break;
                };
        }};

        if (Vars.isDebug || Vars.isServer) {
            Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.setLevel(Level.DEBUG);
            Log.level = LogLevel.debug;

            // The appender has to be referenced in logback.xml to 'exist', 
            // so instead of adding it if it's a server, it's removed if otherwise
            if (!Vars.isServer) rootLogger.detachAppender("FILE");
        }
    }

    public static JDA getBot() {
        return bot;
    }

    private static void registerShutdown() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Log.info("Shutting down...");

            Database.getSession().close();
            
            if (bot != null) {
                bot.setAutoReconnect(false);
                bot.shutdown();

                try {
                    if (!bot.awaitShutdown(10, TimeUnit.SECONDS)) {
                        Log.warn("The shutdown 10 second limit was exceeded. Force shutting down...");    
                        bot.shutdownNow();
                        bot.awaitShutdown(Duration.ofSeconds(30));
                    }
                } catch (InterruptedException e) {
                    Log.err("An error occurred while waiting for the bot to shutdown. Force shutting down...", e);
                    bot.shutdownNow();
                }
            }
        }, "ShutdownThread"));
    } 
}
