package joseta.commands.moderation;

import arc.struct.*;
import arc.util.*;
import joseta.commands.Command;
import joseta.database.*;
import joseta.database.entry.*;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.interactions.components.buttons.*;

import java.util.*;
import java.util.concurrent.*;

public class ClearCommand extends Command {
    private int amount;
    public static ObjectIntMap<MessageChannelUnion> pendingClear = new ObjectIntMap<>();

    public ClearCommand() {
        super("clear", "Supprimer des messages dans le salon actuel.");
        commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE))
                .addOptions(
                        new OptionData(OptionType.INTEGER, "amount", "Le nombre de messages à supprimer (1-100).", true)
                            .setRequiredRange(1, 100)
                );
    }

    @Override
    public void runImpl(SlashCommandInteractionEvent event) {
        MessageChannelUnion channel = event.getChannel();
        if (amount >= 25) {
            event.reply("Vous allez supprimer " + amount + " messages. Êtes-vous sûr ?").addActionRow(
                    Button.success("mod-clear_confirm", "Confirmer")
            ).setEphemeral(true).queue(
                    hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS, v -> pendingClear.remove(channel))
            );

            pendingClear.put(channel, amount);
        } else {
            try {
                List<Message> messages = channel.getIterableHistory().takeAsync(amount).get();
                if (messages.isEmpty()) {
                    event.reply("Aucun message à supprimer.").setEphemeral(true).queue();
                    return;
                }

                channel.purgeMessages(messages);
                event.reply(amount + " messages ont été supprimés.").setEphemeral(true).queue(
                        hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS)
                );
            } catch (InterruptedException | ExecutionException e) {
                event.reply("Une erreur est survenue lors de la suppression des messages.").setEphemeral(true).queue();
                Log.err("Error while executing a command ('clear').", e);
            }
        }
    }

    @Override
    protected void getArgs(SlashCommandInteractionEvent event) {
        amount = event.getOption("amount", 0, OptionMapping::getAsInt);
    }

    @Override
    protected boolean check(SlashCommandInteractionEvent event) {
        ConfigEntry config = Database.get(ConfigEntry.class, event.getGuild().getIdLong());

        if (!config.isModerationEnabled()) {
            event.reply("La modération est désactivée sur ce serveur.").setEphemeral(true).queue();
            return false;
        }

        return true;
    }
}
