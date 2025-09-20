package joseta.commands;

import joseta.annotations.*;
import joseta.annotations.types.*;
import net.dv8tion.jda.api.components.actionrow.*;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.label.*;
import net.dv8tion.jda.api.components.selections.*;
import net.dv8tion.jda.api.components.textdisplay.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.events.interaction.component.*;
import net.dv8tion.jda.api.modals.*;

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
                Button.danger("rules_decline", "Decline Rules")),
                ActionRow.of(
                EntitySelectMenu.create("test_select", EntitySelectMenu.SelectTarget.USER)
                    .setPlaceholder("Select a user")
                    .setRequiredRange(1, 1)
                    .build()),
                ActionRow.of(
                StringSelectMenu.create("test_string_select")
                    .setPlaceholder("Select an option")
                    .setRequiredRange(1, 1)
                    .addOption("Option 1", "option_1", "This is the first option")
                    .addOption("Option 2", "option_2", "This is the second option")
                    .addOption("Option 3", "option_3", "This is the third option")
                    .build()
            ))
            .queue();
    }

    @ButtonInteraction(id = "rules_accept")
    public void rulesAcceptButton(ButtonInteractionEvent event) {
        event.reply("You accepted the rules.").queue();
    }

    @SelectMenuInteraction(id = "test_select")
    public void testSelectMenu(EntitySelectInteractionEvent event) {
        event.reply("You selected: " + event.getValues()).queue();
    }

    @SelectMenuInteraction(id = "test_string_select")
    public void testStringSelectMenu(StringSelectInteractionEvent event) {
        event.reply("You selected: " + event.getValues()).queue();
    }

    @SlashCommandInteraction(name = "rules send test")
    public void rulesSend(SlashCommandInteractionEvent event) {
        event.reply("Rules sent.").queue();
    }

    @SlashCommandInteraction(name = "test")
    public void testCommand(SlashCommandInteractionEvent event) {
        // event.reply("Test command executed.").queue();

        event.replyModal(Modal.create("test_modal", "Test Modal")
            .addComponents(
                TextDisplay.of("""
                    hello world
                    
                    this is so cool
                    """),
                Label.of("Role", EntitySelectMenu.create("role_select", EntitySelectMenu.SelectTarget.ROLE).build())
            )
            .build()).queue();
    }

    @ModalInteraction(id = "test_modal")
    public void testModal(ModalInteractionEvent event) {
        event.reply("Modal submitted.").queue();
    }
}
