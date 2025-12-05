package joseta.commands;

import joseta.annotations.InteractionModule;
import joseta.annotations.types.Option;
import joseta.annotations.types.SlashCommandInteraction;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@InteractionModule
public class ModerationCommands {
    // TODO Logic implementation
    
    //TODO does it need permission?
    @SlashCommandInteraction(name = "modlog", description = "Obtient l'historique de modérations d'un membre.")
    public void modlog(SlashCommandInteractionEvent event,
                       @Option(description = "Le membre dont vous voulez voir l'historique de modération.") Member member)
    {
    
    }
    
    @SlashCommandInteraction(name = "clear", description = "Supprime un nombre de messages dans le salon actuel.", permissions = Permission.MESSAGE_MANAGE)
    public void clear(SlashCommandInteractionEvent event,
                      @Option(description = "Le nombre de messages à supprimer.", minValue = 1, maxValue = 100, required = true) int amount)
    {
    
    }
    
    @SlashCommandInteraction(name = "warn", description = "Averti un membre.", permissions = Permission.MODERATE_MEMBERS)
    public void warn(SlashCommandInteractionEvent event,
                     @Option(description = "Le membre à avertir.", required = true) Member member,
                     @Option(description = "La raison de l'avertissement.") String reason,
                     @Option(description = "La durée avant expiration de l'avertissement (s, m, h, d, w).") String time,
                     int test)
    {
    
    }
    
    @SlashCommandInteraction(name = "unwarn", description = "Retire un avertissement d'un membre.", permissions = Permission.MODERATE_MEMBERS)
    public void unwarn(SlashCommandInteractionEvent event,
                       @Option(description = "Le membre à retirer l'avertissement.", required = true) Member member,
                       @Option(description = "L'identifiant du warn. Le plus récent par défaut.", autoComplete = true) long warnId)
    {
    
    }
    
    @SlashCommandInteraction(name = "timeout", description = "Met un membre en timeout.", permissions = Permission.MODERATE_MEMBERS)
    public void timeout(SlashCommandInteractionEvent event,
                        @Option(description = "Le membre à mettre en timeout.", required = true) Member member,
                        @Option(description = "La durée du timeout (s, m, h, d, w).") String time,
                        @Option(description = "La raison du timeout.") String reason)
    {
    
    }
    
    @SlashCommandInteraction(name = "untimeout", description = "Retire un membre du timeout.", permissions = Permission.MODERATE_MEMBERS)
    public void untimeout(SlashCommandInteractionEvent event,
                          @Option(description = "Le membre à retirer du timeout.", required = true) Member member)
    {
    
    }
    
    @SlashCommandInteraction(name = "kick", description = "Expulse un membre du serveur.", permissions = Permission.KICK_MEMBERS)
    public void kick(SlashCommandInteractionEvent event,
                     @Option(description = "Le membre à expulser.", required = true) Member member,
                     @Option(description = "La raison de l'expulsion.") String reason)
    {
    
    }
    
    @SlashCommandInteraction(name = "ban", description = "Bannit un membre du serveur.", permissions = Permission.BAN_MEMBERS)
    public void ban(SlashCommandInteractionEvent event,
                    @Option(description = "Le membre à bannir.", required = true) User user,
                    @Option(description = "La durée du bannissement (s, m, h, d, w).") String time,
                    @Option(description = "La période de suppression des messages.") String clearTime,
                    @Option(description = "La raison du bannissement.") String reason)
    {
    
    }
    
    @SlashCommandInteraction(name = "unban", description = "Débannit un membre du serveur.", permissions = Permission.BAN_MEMBERS)
    public void unban(SlashCommandInteractionEvent event,
                      @Option(description = "L'identifiant du membre à débannir.", required = true, autoComplete = true) long userId)
    {
    
    }
}
