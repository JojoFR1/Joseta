package joseta.commands;

import joseta.utils.*;
import joseta.utils.struct.*;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

public abstract class ModCommand extends Command {
    protected final ModLog modLog = ModLog.getInstance();
    protected User user;
    protected Member member;
    protected String reason;
    protected long time;
    
    protected ModCommand(String name, String description) {
        super(name, description, Seq.with(), DefaultMemberPermissions.ENABLED);
    }
    protected ModCommand(String name, String description, Seq<OptionData> options) {
        super(name, description, options, DefaultMemberPermissions.ENABLED);
    }
    protected ModCommand(String name, String description, DefaultMemberPermissions defaultPermissions) {
        super(name, description, Seq.with(), defaultPermissions);
    }
    protected ModCommand(String name, String description, Seq<OptionData> options, DefaultMemberPermissions defaultPermissions) {
        super(name, description, options, defaultPermissions);
    }

    @Override
    protected void getArgs(SlashCommandInteractionEvent event) {
        user = event.getOption("user").getAsUser();
        member = event.getOption("user").getAsMember();
        reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : "Default reason";
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
