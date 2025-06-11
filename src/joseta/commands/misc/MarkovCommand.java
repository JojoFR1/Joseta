package joseta.commands.misc;

import joseta.commands.Command;
import joseta.database.*;
import joseta.database.ConfigDatabase.*;
import joseta.utils.markov.*;

import arc.files.*;
import arc.struct.*;

import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.*;

/**
 * Original idea by l4p1n in <a href=https://git.l4p1n.ch/l4p1n-bot/bot-rust/src/branch/trunk/bot/AppCommands/MarkovCommand.cs>l4p1n-bot/MarkovCommand.cs</a>
 */
public class MarkovCommand extends Command {
    private final int MIN_ENTRIES = 100;
    
    public MarkovCommand() {
        super("markov", "Génère un message aléatoire à partir des messages du serveur.");
    }

    @Override
    protected void runImpl(SlashCommandInteractionEvent event) {
        ConfigEntry config = ConfigDatabase.getConfig(event.getGuild().getIdLong());
        if (!config.markovEnabled) {
            event.reply("La génération de messages aléatoires est désactivée sur ce serveur.").setEphemeral(true).queue();
            return;
        }

        InteractionHook hook = event.deferReply().complete();
        Seq<MarkovMessagesDatabase.MessageEntry> entries = MarkovMessagesDatabase.getMessageEntries(event.getGuild().getIdLong()).retainAll(entry -> entry != null);

        if (entries.size < MIN_ENTRIES) {
            hook.editOriginal("Il n'y a pas assez de messages !").queue();
            return;
        }
        
        Markov markov = new Markov(event.getGuild().getId() + event.getChannelId()+".pdo");
        markov.addToChain(entries.map(entry -> entry.content));
        String output = markov.generate();
        
        hook.editOriginal(output).queue();
        
        Fi fi = new Fi(event.getGuild().getId() + event.getChannelId()+".pdo");
        if (fi.exists()) fi.delete();
    }
}
