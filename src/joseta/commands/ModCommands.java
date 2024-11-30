package joseta.commands;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.hooks.*;

import java.io.*;

public class ModCommands extends ListenerAdapter {
    
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String cmdName = event.getName();
        if (!cmdName.equals("mute")
         && !cmdName.equals("kick")
         && !cmdName.equals("ban")
         && !cmdName.equals("unban")) return;

        Member user = event.getOption("user").getAsMember();
        String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : "Default reason";
        long time = parseTime(event.getOption("time") != null ? event.getOption("time").getAsString() : "5m");

        switch (event.getName()) {
            case "mute" -> mute(event, user, reason, time);
            case "kick" -> kick(event, user, reason);
            case "ban" -> ban(event, user, reason, time);
            case "unban" -> unban(event, user);
        }
    }

    private void mute(SlashCommandInteractionEvent event, Member member, String reason, long time) {
        event.reply("Mute- " + member + "\n" + reason + "\n" + time).queue();
        
    }

    private void kick(SlashCommandInteractionEvent event, Member member, String reason) {
        event.reply("Kick - " + member + "\n" + reason).queue();
    }

    private void ban(SlashCommandInteractionEvent event, Member member, String reason, long time) {
        event.reply("Ban- " + member + "\n" + reason + "\n" + time).queue();
    }

    private void unban(SlashCommandInteractionEvent event, Member member) {
        event.reply("Unban- " + member).queue();
    }

    // sanctionId: 10x (Warn), 20x (Mute), 30x (Ban)
    private void log(long userId, long moderatorId, String sanctionId, String time) throws IOException {
        
    }

    private long parseTime(String time) {
        int maximumTime = 28 * 24 * 60 * 60;
        long parsedTime = 0;
        int value = 0;
        
        for (char c : time.toCharArray()) {
            if (c >= '0' && c <= '9')
                value = value * 10 + (c - '0');
            
            else switch (c) {
                case 'w': parsedTime += 7 * 24 * 60 * 60 * value; value = 0; break;
                case 'd': parsedTime +=     24 * 60 * 60 * value; value = 0; break;
                case 'h': parsedTime +=          60 * 60 * value; value = 0; break;
                case 'm': parsedTime +=               60 * value; value = 0; break;
                case 's': parsedTime +=                    value; value = 0; break;
                default:  parsedTime +=                    value; value = 0; break;
            }    
        }

        if (parsedTime > maximumTime) return -1;

        return parsedTime;
    }
}
