package joseta;

import ch.qos.logback.classic.*;
import io.github.cdimascio.dotenv.*;
import joseta.annotations.*;
import joseta.utils.*;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.hooks.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.requests.*;
import net.dv8tion.jda.api.utils.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

public class JosetaBot {
    private static boolean debug = false;

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();

        if (args.length > 0) {
            debug = args[0].equals("--debug");
            Log.setLevel(Level.DEBUG);
        }

        JDA bot = JDABuilder.createDefault(dotenv.get("TOKEN" + (debug ? "_DEV" : "")))
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .enableIntents(GatewayIntent.GUILD_MESSAGES,
                           GatewayIntent.GUILD_MEMBERS,
                           GatewayIntent.MESSAGE_CONTENT)
            .setStatus(OnlineStatus.DO_NOT_DISTURB)
            .setActivity(Activity.watching("🇫🇷 Mindustry France."))
            .build();

        registerShutdown(bot);

        try { bot.awaitReady(); } catch (InterruptedException e) {
            Log.err("An error occurred while waiting for the bot to be ready (connected).", e);
            System.exit(1);
        }

        CommandProcessor.initialize(bot, "joseta.commands");
    }

    private static void registerShutdown(JDA bot) {
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
}