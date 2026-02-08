package dev.jojofr.joseta;

import ch.qos.logback.classic.Level;
import io.github.cdimascio.dotenv.Dotenv;
import dev.jojofr.joseta.annotations.EventProcessor;
import dev.jojofr.joseta.annotations.InteractionProcessor;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.events.ScheduledEvents;
import dev.jojofr.joseta.utils.DotenvDebug;
import dev.jojofr.joseta.utils.Log;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

public class JosetaBot {
    private static JDA botInstance;
    public static JDA get() { return botInstance; }
    
    public static boolean debug = false;
    
    /* TODO Reintroduce the database (with PostgreSQL), could try new libraries and new implementation
     *   - Move rules from a text file to the database
     *   - Allow to change the welcome image (would need to allow to customize the text location and content) - could use file upload modal
     *   - Allow to add custom Regex for auto-response
     * TODO support translations for logging and error messages?
     * TODO (very unlikely) add a web dashboard
     * TODO try to optimize the bot to reduce memory usage and CPU usage and improve responsiveness
     * TODO modernize old code to be more clean and similar to the newer code
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            debug = args[0].equals("--debug");
            Log.setLevel(Level.DEBUG);
        }
        Dotenv dotenv = new DotenvDebug().load(debug);
        
        if (!Database.initialize("dev.jojofr.joseta.database.entities", dotenv.get("DATABASE_USER"), dotenv.get("DATABASE_PASSWORD"), dotenv.get("DATABASE_HOST"), dotenv.get("DATABASE_NAME"))) {
            Log.err("Database initialization failed. Exiting...");
            System.exit(1);
        }

        botInstance = JDABuilder.createDefault(dotenv.get("TOKEN"))
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .enableIntents(GatewayIntent.GUILD_MESSAGES,
                           GatewayIntent.GUILD_MEMBERS,
                           GatewayIntent.MESSAGE_CONTENT)
            .setStatus(OnlineStatus.DO_NOT_DISTURB)
            .setActivity(Activity.watching("🇫🇷 Mindustry France."))
            .build();

        InteractionProcessor.initialize(botInstance, "dev.jojofr.joseta.commands");
        EventProcessor.initialize(botInstance, "dev.jojofr.joseta.events", "dev.jojofr.joseta.commands");
        
        registerShutdown(botInstance);

        // Wait for JDA to be ready and connected
        try { botInstance.awaitReady(); } catch (InterruptedException e) {
            Log.err("An error occurred while waiting for the instance to be ready (connected).", e);
            System.exit(1);
        }
        
        ScheduledEvents.schedule();
        
        //TODO check for guilds, members, etc. and add them to the database if they don't exist, or remove them if they don't exist anymore
    }
    
    private static void registerShutdown(JDA bot) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            bot.setAutoReconnect(false);
            bot.shutdown();

            try {
                if (!bot.awaitShutdown(10, TimeUnit.SECONDS)) {
                    Log.warn("The shutdown 10 second limit was exceeded. Force shutting down...");
                    bot.shutdownNow();

                    // TODO Not sure if that works
                    if (!bot.awaitShutdown(1, TimeUnit.MINUTES)) {
                        Log.err("The bot did not shutdown after the forced shutdown. Exiting...");
                        OkHttpClient client = bot.getHttpClient();
                        client.connectionPool().evictAll();
                        client.dispatcher().executorService().shutdown();
                    }
                }
            } catch (InterruptedException e) {
                Log.err("An error occurred while waiting for the bot to shutdown. Force shutting down...", e);
                bot.shutdownNow();
            }
        }, "ShutdownThread"));
    }
}