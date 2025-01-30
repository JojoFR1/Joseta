package joseta.commands;

import joseta.utils.*;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

import java.util.regex.*;

public abstract class ModCommand extends Command {
    protected User user;
    protected Member member;
    protected String reason;
    protected long time;
    protected String defaultTime = "5m";

    private final Pattern numPattern = Pattern.compile("\\d+");
    
    protected ModCommand(String name, String description) {
        super(name, description, DefaultMemberPermissions.ENABLED);
    }
    protected ModCommand(String name, String description, DefaultMemberPermissions defaultPermissions) {
        super(name, description, defaultPermissions, new OptionData[0]);
    }
    protected ModCommand(String name, String description, OptionData... options) {
        super(name, description, DefaultMemberPermissions.ENABLED, options);
    }
    protected ModCommand(String name, String description, SubcommandData... subcommands) {
        super(name, description, DefaultMemberPermissions.ENABLED, subcommands);
    }
    protected ModCommand(String name, String description, DefaultMemberPermissions defaultPermissions, OptionData... options) {
        super(name, description, defaultPermissions, options);
    }
    protected ModCommand(String name, String description, DefaultMemberPermissions defaultPermissions, SubcommandData... subcommands) {
        super(name, description, defaultPermissions, subcommands);
    }

    @Override
    protected void getArgs(SlashCommandInteractionEvent event) {
        String userInput = event.getOption("user", event.getUser().getId(), OptionMapping::getAsString);
        String userId = userInput.replaceAll("[<@!>]", "");
        if (!numPattern.matcher(userId).matches()) return; // An ID can only be numbers.

        user = event.getJDA().getUserById(userId);
        if (user != null) member = event.getGuild().getMember(user);
        
        reason = event.getOption("reason", "Raison par défaut", OptionMapping::getAsString);
        time = Strings.parseTime(event.getOption("time", defaultTime, OptionMapping::getAsString));
    }
    
    @Override
    protected boolean check(SlashCommandInteractionEvent event) {
        if (user == null) {
            event.reply("Ce membre n'existe pas. Vérifiez que l'identifiant est correct.").setEphemeral(true).queue();
            return false;
        }

        if (event.getMember().equals(member)) {
            event.reply("Ce membre est vous-même, vous ne pouvez pas vous auto-sanctionner").setEphemeral(true).queue();
            return false;
        }

        if (user.isBot() || user.isSystem()) {
            event.reply("Ce membre est un robot ou un compte système, vous ne pouvez pas le sanctionner.").setEphemeral(true).queue();
            return false;
        }
        
        if (event.getMember().getRoles().get(0).getPosition() < member.getRoles().get(0).getPosition()) {
            event.reply("Ce membre a un rôle supérieur au votre, vous ne pouvez pas le sanctionner.").setEphemeral(true).queue();
            return false;
        }

        return super.check(event);
    }

    public final class SanctionType {
        public static final int WARN = 10;
        public static final int MUTE = 20;
        public static final int KICK = 30;
        public static final int BAN = 40;
    }
}
