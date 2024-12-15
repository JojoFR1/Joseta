package joseta.commands;

import joseta.utils.*;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

public abstract class ModCommand extends Command {
    protected static final ModLog modLog = ModLog.getInstance();
    protected User user;
    protected Member member;
    protected String reason;
    protected long time;
    
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
        user =   event.getOption("user")   != null ? event.getOption("user").getAsUser() : event.getUser();
        member = event.getOption("user")   != null ? event.getOption("user").getAsMember() : event.getMember();
        reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : "Raison par défaut";
        time = Strings.parseTime(event.getOption("time") != null ? event.getOption("time").getAsString() : "5m");
    }
    
    @Override
    protected boolean check(SlashCommandInteractionEvent event) {
        if (event.getMember().equals(member)) {
            event.reply("Mais t'es con pourquoi tu t'auto sanctionne ?").queue();
            return false;
        }

        if (user.isBot() || user.isSystem()) {
            event.reply("C'est un bot ou un truc systeme imbecile").queue();
            return false;
        }
        
        if (event.getMember().getRoles().get(0).getPosition() < member.getRoles().get(0).getPosition()) {
            event.reply("This user has a higher role than you bozo").queue();
            return false;
        }

        return super.check(event);
    }

    protected final class SanctionType {
        public static final int WARN = 10;
        public static final int MUTE = 20;
        public static final int KICK = 30;
        public static final int BAN = 40;
    }
}
