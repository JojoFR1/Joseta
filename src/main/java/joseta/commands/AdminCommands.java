package joseta.commands;

import joseta.annotations.*;
import joseta.annotations.types.*;
import net.dv8tion.jda.api.components.actionrow.*;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.events.interaction.component.*;

@InteractionModule
public class AdminCommands {

    @SlashCommandInteraction(name = "rules update")
    public void rulesUpdate(SlashCommandInteractionEvent event, @Option(required = true) Role role) {
        event.reply("Rules updated. " + role).queue();
    }

    @SlashCommandInteraction(name = "rules send admin")
    public void rulesSendAdmin(SlashCommandInteractionEvent event, @Option(required = true) String test) {
        event.reply("Admin rules sent." + test)
            .addComponents(ActionRow.of(
                Button.primary("rules_accept", "Accept Rules"),
                Button.danger("rules_decline", "Decline Rules")
            ))
            .queue();
    }

    @ButtonInteraction(id = "rules_accept")
    public void rulesAcceptButton(ButtonInteractionEvent event) {
        event.reply("You accepted the rules.").queue();
    }

    @SlashCommandInteraction(name = "rules send test")
    public void rulesSend(SlashCommandInteractionEvent event) {
        event.reply("Rules sent.").queue();
    }

    @SlashCommandInteraction(name = "test")
    public void testCommand(SlashCommandInteractionEvent event) {
        event.reply("Test command executed.").queue();
    }
}
