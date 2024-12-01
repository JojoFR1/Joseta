package joseta.commands;

import joseta.*;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.hooks.*;

import java.io.*;
import java.time.*;

import com.google.gson.*;

public class ModCommands extends ListenerAdapter {
    
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String cmdName = event.getName();
        if (!cmdName.equals("mute")
         && !cmdName.equals("unmute")
         && !cmdName.equals("kick")
         && !cmdName.equals("ban")
         && !cmdName.equals("unban")) return;
        
         // TODO Warn 
        Member member = event.getOption("user").getAsMember();
        String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : "Default reason";
        long time = parseTime(event.getOption("time") != null ? event.getOption("time").getAsString() : "5m");

        switch (event.getName()) {
            case "mute" -> mute(event, member, reason, time);
            case "unmute" -> unmute(event, member);
            case "kick" -> kick(event, member, reason);
            case "ban" -> ban(event, member, reason, time);
            case "unban" -> unban(event, member);
        }
    }

    // TODO handle higher role than you
    // TODO handle unmute non muted member 
    // TODO handle mute higher than 28 days
    // TODO handle self sanction
    // TODO handle bot sanction
    // TODO add a way to access/see the logs of a member

    private void mute(SlashCommandInteractionEvent event, Member member, String reason, long time) {
        event.reply("Mute- " + member + "\n" + reason + "\n" + time).queue();
        // member.timeoutFor(time, TimeUnit.SECONDS).queue();

        log(member.getIdLong(), reason, time, event.getUser().getIdLong(), SanctionType.MUTE);
    }

    private void unmute(SlashCommandInteractionEvent event, Member member) {
        event.reply("Unmute- " + member).queue();
        member.removeTimeout().queue();
    }

    private void kick(SlashCommandInteractionEvent event, Member member, String reason) {
        event.reply("Kick - " + member + "\n" + reason).queue();
        // member.kick();

        log(member.getIdLong(), reason, -1, event.getUser().getIdLong(), SanctionType.KICK);
    }
    
    private void ban(SlashCommandInteractionEvent event, Member member, String reason, long time) {
        event.reply("Ban- " + member + "\n" + reason + "\n" + time).queue();
        // member.ban(0, null)


        log(member.getIdLong(), reason, time, event.getUser().getIdLong(), SanctionType.BAN);
    }

    private void unban(SlashCommandInteractionEvent event, Member member) {
        event.reply("Unban- " + member).queue();
    }

    // sanctionId: 10 (Warn), 20 (Mute), 30 (Kick), 40 (Ban)
    private void log(long userId, String reason, long time, long moderatorId, int sanctionTypeId) {
        //? Pretty printing is temporary while testing. Should be removed to reduce size.
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            JsonObject rootNode = gson.fromJson(new FileReader("resources/modlog.json"), JsonObject.class);
            JsonObject userNode = rootNode.getAsJsonObject(Long.toString(userId));
            if (userNode == null) {
                userNode = new JsonObject();
                rootNode.add(Long.toString(userId), userNode);
            }

            String sanctionType = sanctionTypeId == SanctionType.WARN ? "warns"
                                : sanctionTypeId == SanctionType.MUTE ? "mutes"
                                : sanctionTypeId == SanctionType.KICK ? "kicks"
                                : "bans";
            JsonArray sanctionArray = userNode.getAsJsonArray(sanctionType);
            if (sanctionArray == null) {
                sanctionArray = new JsonArray();
                userNode.add(sanctionType, sanctionArray);
            }

            String lastSanctionIdString = "last" + sanctionType.substring(0, 1).toUpperCase() + sanctionType.substring(1, sanctionType.length() - 1) + "Id";
            int sanctionId = rootNode.get(lastSanctionIdString).getAsInt();

            rootNode.addProperty(lastSanctionIdString, sanctionId + 1);

            JsonObject newSanction = new JsonObject();
            newSanction.addProperty("id", sanctionId + 1);
            newSanction.addProperty("reason", reason);
            if (sanctionType != "kicks") newSanction.addProperty("for", time);
            newSanction.addProperty("at", Instant.now().toString());
            newSanction.addProperty("by", moderatorId);

            sanctionArray.add(newSanction);


            FileWriter writer = new FileWriter("resources/modlog.json");
            gson.toJson(rootNode, writer);
            writer.close();
        } catch (Exception e) {
            // TODO more catch to have more precise error logging. Not using global 'Exception'.
            Vars.logger.error("Error while logging a sanction.", e);
        }
    }

    private final class SanctionType {
        static final int WARN = 10;
        static final int MUTE = 20;
        static final int KICK = 30;
        static final int BAN = 40;
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