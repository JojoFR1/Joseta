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

import org.hibernate.*;
import org.hibernate.boot.registry.*;
import org.hibernate.cfg.*;
import org.slf4j.*;

import java.sql.*;
import java.util.concurrent.*;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import jakarta.persistence.*;

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
        new ModLogCommand()
    );

    /* Load - Before the bot load
     * Initialize - After the bot load (after awaitReady)
     */
    public static void main(String[] args) throws SQLException {
        registerShutdown();
        preLoad(args);

        dbTets();
        System.exit(0);
        
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
        try { bot.awaitReady(); } catch (InterruptedException e) {
            Log.err("An error occured while waiting for the bot to be ready (connected).", e);
            System.exit(1);
        }

        SlashCommandData[] commandsData = commands.map(cmd -> cmd.getCommandData()).toArray(SlashCommandData.class);
        bot.getGuilds().forEach(g -> g.updateCommands().addCommands().queue()); // Reset for the guilds command to avoid duplicates.
        bot.updateCommands().addCommands(commandsData).queue();

        WelcomeMessage.initialize(); //TODO Maybe chane that?
        SanctionDatabaseHelper.startScheduler(15); // Check expired sanctions every 15 minutes.

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

    public static void dbTets() {
        Configuration configuration = new Configuration();
        
        configuration.addAnnotatedClass(TestTable.class);
        configuration.setProperty("hibernate.connection.driver_class", "org.sqlite.JDBC");
        configuration.setProperty("hibernate.connection.url", "jdbc:sqlite:resources/test.db");
        configuration.setProperty("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect");
        configuration.setProperty("hibernate.hbm2ddl.auto", "update");
        configuration.setProperty("hibernate.show_sql", "true");
        
        StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
        SessionFactory sessionFactory = configuration.buildSessionFactory(builder.build());

        Session s = sessionFactory.openSession();
        Transaction tx = s.beginTransaction();
        s.persist(new TestTable(9, "Test", "This is a test table."));
        
        tx.commit();
    }

    @Entity
    public static class TestTable {
        @Id
        private long id;
        private String name;
        private String description;

        public TestTable() {}
        public TestTable(long id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
        
        public long getId() {
            return id;
        }
        public void setId(long id) {
            this.id = id;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getDescription() {
            return description;
        }
        public void setDescription(String description) {
            this.description = description;
        }

    }

    private static void preLoad(String args[]) {
        if (args.length > 0) {
            Vars.setDebug(args[0].equals("--debug"));
            Vars.setServer(args[0].equals("--server"));
        }

        Vars.loadSecrets();
        
        // Disable ORMLite debug logs, because it's spamming
        com.j256.ormlite.logger.Logger.setGlobalLogLevel(com.j256.ormlite.logger.Level.WARNING);

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

        // com.j256.ormlite.logger.Logger.;
        if (Vars.isDebug || Vars.isServer) {
            Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.setLevel(Level.DEBUG);
            Log.level = LogLevel.debug;
            
            ((Logger) LoggerFactory.getLogger("log4j.logger.com.j256.ormlite")).setLevel(Level.INFO);

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
            
            if (bot != null) {
                bot.setAutoReconnect(false);
                bot.shutdown();

                try {
                    if (!bot.awaitShutdown(10, TimeUnit.SECONDS)) {
                        Log.warn("The shutdown 10 second limit was exceeded. Force shutting down...");    
                        bot.shutdownNow();
                        bot.awaitShutdown();
                    }
                } catch (InterruptedException e) {
                    Log.err("An error occured while waitin for the bot to shutdown. Force shutting down...", e);
                    bot.shutdownNow();
                }
            }
        }, "ShutdownThread"));
    } 
}
