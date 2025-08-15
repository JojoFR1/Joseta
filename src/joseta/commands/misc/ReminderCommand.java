package joseta.commands.misc;

import arc.util.*;
import joseta.commands.*;
import joseta.commands.Command;
import joseta.database.*;
import joseta.database.entry.*;
import joseta.database.helper.*;
import joseta.utils.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

import java.time.*;
import java.util.*;

public class ReminderCommand extends Command {

    public ReminderCommand() {
        super("reminder", "Ajouter vous un rappel ou listez vos rappels");
        this.commandData.addSubcommands(
            new SubcommandData("add", "Ajouter vous un rappel")
                .addOption(OptionType.STRING, "message", "Le message du rappel", true)
                .addOption(OptionType.STRING, "time", "Le temps avant que vous recevez le rappels (M, w, d, h, m, s)", true),
            new SubcommandData("list", "List")
        );
    }

    @Override
    protected void runImpl(SlashCommandInteractionEvent event) {
        switch(event.getSubcommandName()) {
            case "add" -> {
                String message = event.getOption("message").getAsString();
                long time = TimeParser.parse(event.getOption("time").getAsString());
                Log.info(time);

                Database.createOrUpdate(new ReminderEntry(event.getGuild().getIdLong(), event.getChannelIdLong(), event.getUser().getIdLong(), message, time));
                event.reply("Rappel ajouté avec succès pour le <t:"+ Instant.now().getEpochSecond() + time +":F>.").setEphemeral(true).queue();
            }
            case "list" -> {
                event.reply("A VENIR").setEphemeral(true).queue();
            }
        }
    }
}
