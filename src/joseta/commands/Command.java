package joseta.commands;

import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

public abstract class Command {
    protected String name;
    protected String description;
    protected DefaultMemberPermissions defaultPermissions = DefaultMemberPermissions.ENABLED;
    protected OptionData[] options = new OptionData[0];
    protected SubcommandData[] subcommands = new SubcommandData[0];
    protected SubcommandGroupData[] subcommandGroups = new SubcommandGroupData[0];
    protected boolean isNSFW = false;
    protected InteractionContextType[] contexts = new InteractionContextType[0];
    protected IntegrationType[] integrationTypes = new IntegrationType[0];

    public Command() {}

    public Command(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public SlashCommandData build() {
        SlashCommandData commandData = Commands.slash(name, description)
            .setDefaultPermissions(defaultPermissions)
            .addOptions(options)
            .addSubcommands(subcommands)
            .addSubcommandGroups(subcommandGroups)
            .setNSFW(isNSFW)
            .setContexts(contexts)
            .setIntegrationTypes(integrationTypes);

        return commandData;
    }


    public final void run(SlashCommandInteractionEvent event) {
        getArgs(event);
        if (!check(event)) return;

        runImpl(event);
    }

    protected abstract void runImpl(SlashCommandInteractionEvent event);

    protected void getArgs(SlashCommandInteractionEvent event) {}
    
    protected boolean check(SlashCommandInteractionEvent event) {
        return true;
    }
    

    public String getName() {
        return name;
    }

    public Command setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }


    public Command setDescription(String description) {
        this.description = description;
        return this;
    }


    public DefaultMemberPermissions getDefaultPermissions() {
        return defaultPermissions;
    }


    public Command setDefaultPermissions(DefaultMemberPermissions defaultPermissions) {
        this.defaultPermissions = defaultPermissions;
        return this;
    }


    public OptionData[] getOptions() {
        return options;
    }


    public Command addOptions(OptionData... options) {
        this.options = options;
        return this;
    }


    public SubcommandData[] getSubcommands() {
        return subcommands;
    }


    public Command addSubcommands(SubcommandData... subcommands) {
        this.subcommands = subcommands;
        return this;
    }


    public SubcommandGroupData[] getSubcommandGroups() {
        return subcommandGroups;
    }


    public Command addSubcommandGroups(SubcommandGroupData... subcommandGroups) {
        this.subcommandGroups = subcommandGroups;
        return this;
    }


    public boolean isNSFW() {
        return isNSFW;
    }


    public Command setNSFW(boolean isNSFW) {
        this.isNSFW = isNSFW;
        return this;
    }


    public InteractionContextType[] getContexts() {
        return contexts;
    }


    public Command setContexts(InteractionContextType... contexts) {
        this.contexts = contexts;
        return this;
    }


    public IntegrationType[] getIntegrationTypes() {
        return integrationTypes;
    }


    public Command setIntegrationTypes(IntegrationType... integrationTypes) {
        this.integrationTypes = integrationTypes;
        return this;
    }

    
}
