package dev.jojofr.joseta.commands;

import dev.jojofr.joseta.annotations.InteractionModule;
import dev.jojofr.joseta.annotations.types.ButtonInteraction;
import dev.jojofr.joseta.annotations.types.Option;
import dev.jojofr.joseta.annotations.types.SlashCommandInteraction;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.entities.Configuration;
import dev.jojofr.joseta.database.entities.Sanction;
import dev.jojofr.joseta.database.entities.Sanction_;
import dev.jojofr.joseta.database.helper.SanctionDatabase;
import dev.jojofr.joseta.entities.ModlogMessage;
import dev.jojofr.joseta.utils.Log;
import dev.jojofr.joseta.utils.TimeParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.awt.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@InteractionModule
public class ModerationCommands {
    private static final int SANCTION_PER_PAGE = 5;
    private static final Map<Long, ModlogMessage> modlogMessages = new HashMap<>();
    
    @SlashCommandInteraction(name = "modlog", description = "Obtient l'historique de modérations d'un membre.", permissions = Permission.MODERATE_MEMBERS)
    public void modlog(SlashCommandInteractionEvent event,
                       @Option(description = "Le membre dont vous voulez voir l'historique de modération.") Member member)
    {
        if (member == null) member = event.getMember();

        dev.jojofr.joseta.database.entities.User userDb = Database.get(dev.jojofr.joseta.database.entities.User.class, new dev.jojofr.joseta.database.entities.User.UserId(member.getIdLong(), event.getGuild().getIdLong()));
        if (userDb == null || userDb.sanctionCount == 0) {
            event.reply("Aucun historique de modération trouvé pour " + member.getEffectiveName() + ".").setEphemeral(true).queue();
            return;
        }

        // Integer division always floors the result, so we add 1 if there's a remainder to ceil the value
        int lastPage = userDb.sanctionCount / SANCTION_PER_PAGE + (userDb.sanctionCount % SANCTION_PER_PAGE == 0 ? 0 : 1);

        // The user SHOULD have at least one sanction, but just in case
        MessageEmbed embed = generateEmbed(event.getGuild(), member.getUser(), 1, lastPage);
        if (embed == null) {
            event.reply("Aucun historique de modération trouvé pour " + member.getEffectiveName() + ".").setEphemeral(true).queue();
            return;
        }
        
        // Need this because Java is doing Java things
        Member finalMember = member;
        event.replyEmbeds(embed).setComponents(getModlogButtons(1, lastPage)).queue(
            hook -> modlogMessages.put(hook.getCallbackResponse().getMessage().getIdLong(), new ModlogMessage(finalMember.getUser(), lastPage, Instant.now()))
        );
    }
    
    private MessageEmbed generateEmbed(Guild guild, User user, int currentPage, int lastPage) {
        // Sort from newest to oldest
        List<Sanction> sanctions = Database.querySelect(Sanction.class, (cb, rt) ->
            cb.and(
                cb.equal(rt.get(Sanction_.id).get(Sanction_.SanctionId_.guildId), guild.getIdLong()),
                cb.equal(rt.get(Sanction_.userId), user.getIdLong())
            ),
            (cb, rt) -> cb.desc(rt.get(Sanction_.id).get(Sanction_.SanctionId_.sanctionNumber))
        ).setFirstResult((currentPage - 1) * SANCTION_PER_PAGE).setMaxResults(SANCTION_PER_PAGE).getResultList();
        
        if (sanctions.isEmpty()) return null;
        
        EmbedBuilder embedBuilder = new EmbedBuilder()
            .setTitle("Historique de modération de " + user.getEffectiveName() + " ┃ Page "+ currentPage +"/"+ lastPage)
            .setColor(Color.BLUE)
            .setFooter(guild.getName(), guild.getIconUrl())
            .setTimestamp(Instant.now());
        
        StringBuilder description = new StringBuilder();
        for (Sanction sanction : sanctions) {
            description.append("### ").append(sanction.sanctionType).append(" - #").append(sanction.getSanctionId());
            if (sanction.isExpired) description.append(" (Expirée)");
            
            description.append("\n>    - Modérateur: <@").append(sanction.moderatorId).append("> (`").append(sanction.moderatorId).append("`)")
                       .append("\n>    - Raison: ").append(sanction.reason)
                       .append("\n>    - Date: <t:").append(sanction.timestamp.getEpochSecond()).append(":F>");
            
            if (sanction.sanctionType != Sanction.SanctionType.KICK && sanction.expiryTime != null) description.append("\n>    - Expire: <t:").append(sanction.expiryTime.getEpochSecond()).append(":F>");
            description.append("\n");
        }
        
        embedBuilder.setDescription(description.toString());
        
        return embedBuilder.build();
    }
    
    // TODO maybe allow something like wildcard for interaction IDs, would be cleaner but might be hard to implement
    @ButtonInteraction(id = "modlog-page_first") public void modlogButtonFirst(ButtonInteractionEvent event) { modlogPage(event); }
    @ButtonInteraction(id = "modlog-page_prev")  public void modlogButtonPrev(ButtonInteractionEvent event) { modlogPage(event); }
    @ButtonInteraction(id = "modlog-page_next")  public void modlogButtonNext(ButtonInteractionEvent event) { modlogPage(event); }
    @ButtonInteraction(id = "modlog-page_last")  public void modlogButtonLast(ButtonInteractionEvent event) { modlogPage(event); }
    
    private void modlogPage(ButtonInteractionEvent event) {
        ModlogMessage modlogMessage = modlogMessages.get(event.getMessageIdLong());
        // Check if the modlogMessage exists and if the timestamp is still valid (15 minutes)
        if (modlogMessage == null || Instant.now().isAfter(modlogMessage.timestamp.plusSeconds(15 * 60))) {
            event.reply("Cette interaction a expiré. Veuillez réutiliser la commande pour obtenir un nouvel historique de modération.").setEphemeral(true).queue();
            
            // Remove the button from the message
            event.getMessage().editMessageComponents().queue();
            modlogMessages.remove(event.getMessageIdLong());
            return;
        }

        String eventId = event.getComponentId();
        int currentPage = eventId.endsWith("first") ? 1
                        : eventId.endsWith("prev")  ? modlogMessage.previousPage()
                        : eventId.endsWith("next")  ? modlogMessage.nextPage()
                        : modlogMessage.lastPage;
        
        event.editMessageEmbeds(generateEmbed(event.getGuild(), modlogMessage.user, currentPage, modlogMessage.lastPage))
            .setComponents(getModlogButtons(currentPage, modlogMessage.lastPage)).queue();
    }
    
    private ActionRow getModlogButtons(int currentPage, int lastPage) {
        return ActionRow.of(
            Button.secondary("modlog-page_first", "⏪").withDisabled(currentPage == 1),
            Button.secondary("modlog-page_prev", "◀️").withDisabled(currentPage <= 1),
            Button.secondary("modlog-page_next", "▶️").withDisabled(currentPage >= lastPage),
            Button.secondary("modlog-page_last", "⏩").withDisabled(currentPage == lastPage)
        );
    }
    
    
    public static Map<MessageChannelUnion, Integer> pendingClear = new HashMap<>();
    
    @SlashCommandInteraction(name = "clear", description = "Supprime un nombre de messages dans le salon actuel.", permissions = Permission.MESSAGE_MANAGE)
    public void clear(SlashCommandInteractionEvent event,
                      @Option(description = "Le nombre de messages à supprimer.", minValue = 1, maxValue = 100, required = true) Integer amount)
    {
        Configuration config = Database.get(Configuration.class, event.getGuild().getIdLong());
        
        if (!config.moderationEnabled) {
            event.reply("La modération est désactivée sur ce serveur.").setEphemeral(true).queue();
            return;
        }
        
        MessageChannelUnion channel = event.getChannel();
        if (amount >= 25) {
            event.reply("Vous allez supprimer " + amount + " messages. Êtes-vous sûr ?").setComponents(
                ActionRow.of(Button.success("btn-clear_confirm", "Confirmer"))
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
                    hook -> hook.deleteOriginal().queueAfter(5, TimeUnit.SECONDS)
                );
            } catch (InterruptedException | ExecutionException e) {
                event.reply("Une erreur est survenue lors de l'exécution de la commande.").setEphemeral(true).queue();
                Log.err("Error while executing a command ('clear').", e);
            }
        }
    }
    
    @ButtonInteraction(id = "btn-clear_confirm")
    public void clearConfirm(ButtonInteractionEvent event) {
        if (pendingClear.isEmpty()) return;
        if (!pendingClear.containsKey(event.getChannel())) {
            event.reply("Aucune action en attente pour ce salon.").setEphemeral(true).queue();
            return;
        }
        
        MessageChannelUnion channel = event.getChannel();
        int amount = pendingClear.get(channel);
        try {
            List<Message> messages = channel.getIterableHistory().takeAsync(amount).get();
            if (messages.isEmpty()) {
                event.reply("Aucun message à supprimer.").setEphemeral(true).queue();
                pendingClear.remove(channel);
                return;
            }
            
            channel.purgeMessages(messages);
            event.reply(amount + " messages ont été supprimés.").setEphemeral(true).queue(
                hook -> hook.deleteOriginal().queueAfter(5, TimeUnit.SECONDS)
            );
            pendingClear.remove(channel);
        } catch (InterruptedException | ExecutionException e) {
            event.reply("Une erreur est survenue lors de l'exécution de la commande.").setEphemeral(true).queue();
            Log.err("Error while executing a command ('clear').", e);
        }
    }
    
    @SlashCommandInteraction(name = "warn", description = "Averti un membre.", permissions = Permission.MODERATE_MEMBERS)
    public void warn(SlashCommandInteractionEvent event,
                     @Option(description = "Le membre à avertir.", required = true) Member member,
                     @Option(description = "La raison de l'avertissement.") String reason,
                     @Option(description = "La durée avant expiration de l'avertissement (s, m, h, d, w).") String time)
    {
        if (!check(event, member)) return;
        
        long timeSeconds;
        if (time != null && !time.isEmpty()) timeSeconds = TimeParser.parse(time);
        else timeSeconds = 300; // Default 5 minutes
        event.reply("Le membre a bien été averti.").setEphemeral(true).queue();
        
        member.getUser().openPrivateChannel().queue(
            channel -> channel.sendMessage("Vous avez été averti sur le serveur **`" + event.getGuild().getName() + "`** par " + event.getUser().getAsMention() +
                " pour la raison suivante : " + reason + ".\nCette sanction expirera dans: <t:" + (Instant.now().getEpochSecond() + timeSeconds) +
                ":R>.\n\n-# ***Ceci est un message automatique. Toutes contestations doivent se faire avec le modérateur responsable.***"
            ).queue(null, f -> event.getHook().editOriginal("Le membre a bien été expulsé... mais impossible d'envoyer un message privé à " + member.getAsMention() + ".").queue())
        );
        
        SanctionDatabase.addSanction(Sanction.SanctionType.WARN, member, event.getUser().getIdLong(), event.getGuild().getIdLong(), reason, timeSeconds);
    }
    
    @SlashCommandInteraction(name = "unwarn", description = "Retire un avertissement d'un membre.", permissions = Permission.MODERATE_MEMBERS)
    public void unwarn(SlashCommandInteractionEvent event,
                       @Option(description = "Le membre à retirer l'avertissement.", required = true) Member member,
                       @Option(description = "L'identifiant du warn. Le plus récent par défaut.", autoComplete = true) Long warnId)
    {
        event.reply("Fonctionnalité non implémentée pour le moment.").setEphemeral(true).queue();
    }
    
    @SlashCommandInteraction(name = "timeout", description = "Met un membre en timeout.", permissions = Permission.MODERATE_MEMBERS)
    public void timeout(SlashCommandInteractionEvent event,
                        @Option(description = "Le membre à mettre en timeout.", required = true) Member member,
                        @Option(description = "La durée du timeout (s, m, h, d, w).") String time,
                        @Option(description = "La raison du timeout.") String reason)
    {
        if (!check(event, member)) return;
        
        long timeSeconds;
        if (time != null && !time.isEmpty()) timeSeconds = TimeParser.parse(time);
        else timeSeconds = 300; // Default 5 minutes
        
        member.timeoutFor(TimeParser.parse(time), TimeUnit.SECONDS).reason(reason).queue(
            s -> {
                event.reply("Le membre a bien été mis en timeout.").setEphemeral(true).queue();
                
                member.getUser().openPrivateChannel().queue(
                    channel -> channel.sendMessage("Vous avez été mis en timeout sur le serveur **`" + event.getGuild().getName() + "`** par " + event.getUser().getAsMention() +
                        " pour la raison suivante : " + reason + ".\nCette sanction expirera dans: <t:" + (Instant.now().getEpochSecond() + timeSeconds) +
                        ":R>.\n\n-# ***Ceci est un message automatique. Toutes contestations doivent se faire avec le modérateur responsable.***"
                    ).queue(null, f -> event.getHook().editOriginal("Le membre a bien été expulsé... mais impossible d'envoyer un message privé à " + member.getAsMention() + ".").queue())
                );
                
                SanctionDatabase.addSanction(Sanction.SanctionType.TIMEOUT, member, event.getUser().getIdLong(), event.getGuild().getIdLong(), reason, timeSeconds);
            },
            f -> {
                event.reply("Une erreur est survenue lors de l'exécution de la commande.").setEphemeral(true).queue();
                Log.err("Error while executing a command ('timeout').", f);
            }
        );
    }
    
    @SlashCommandInteraction(name = "untimeout", description = "Retire un membre du timeout.", permissions = Permission.MODERATE_MEMBERS)
    public void untimeout(SlashCommandInteractionEvent event,
                          @Option(description = "Le membre à retirer du timeout.", required = true) Member member)
    {
        if (!check(event, member)) return;
        
        member.removeTimeout().queue(
            s -> {
                event.reply("Le membre a bien été retiré du timeout.").setEphemeral(true).queue();

                // A member can't have 2 timeout active at the same time.
                Sanction sanction = SanctionDatabase.getLatest(member.getIdLong(), event.getGuild().getIdLong(), Sanction.SanctionType.TIMEOUT);
                Database.update(sanction.setExpired(true));
            },
            f -> {
                event.reply("Une erreur est survenue lors de l'exécution de la commande.").setEphemeral(true).queue();
                Log.err("Error while executing a command ('untimeout').", f);
            }
        );
    }
    
    @SlashCommandInteraction(name = "kick", description = "Expulse un membre du serveur.", permissions = Permission.KICK_MEMBERS)
    public void kick(SlashCommandInteractionEvent event,
                     @Option(description = "Le membre à expulser.", required = true) Member member,
                     @Option(description = "La raison de l'expulsion.") String reason)
    {
        if (!check(event, member)) return;
        
        member.kick().reason(reason).queue(
            s -> {
                event.reply("Le membre a bien été expulsé.").setEphemeral(true).queue();
                
                member.getUser().openPrivateChannel().queue(
                    channel -> channel.sendMessage(
                        "Vous avez été expulsé du serveur **`" + event.getGuild().getName() + "`** par " + event.getUser().getAsMention() +
                            " pour la raison suivante : " + reason + ".\n\n-# ***Ceci est un message automatique. Toutes contestations doivent se faire avec le modérateur responsable.***"
                    ).queue(null, f -> event.getHook().editOriginal("Le membre a bien été expulsé... mais impossible d'envoyer un message privé à " + member.getAsMention() + ".").queue())
                );
                
                SanctionDatabase.addSanction(Sanction.SanctionType.KICK, member, event.getUser().getIdLong(), event.getGuild().getIdLong(), reason, -1);
            },
            f -> {
                event.reply("Une erreur est survenue lors de l'exécution de la commande.").setEphemeral(true).queue();
                Log.err("Error while executing a command ('kick').", f);
            }
        );
    }
    
    @SlashCommandInteraction(name = "ban", description = "Bannit un membre du serveur.", permissions = Permission.BAN_MEMBERS)
    public void ban(SlashCommandInteractionEvent event,
                    @Option(description = "Le membre à bannir.", required = true) Member member,
                    @Option(description = "La durée du bannissement (s, m, h, d, w).") String time,
                    @Option(description = "La période de suppression des messages.") String clearTime,
                    @Option(description = "La raison du bannissement.") String reason)
    {
        if (!check(event, member)) return;
        
        long timeSeconds;
        if (time != null && !time.isEmpty()) timeSeconds = TimeParser.parse(time);
        else timeSeconds = 300; // Default 5 minutes
        
        int clearTimeSeconds = 3600; // Default 1 hour
        if (clearTime != null && !clearTime.isEmpty()) clearTimeSeconds = (int) TimeParser.parse(clearTime);
        
        member.ban(clearTimeSeconds, TimeUnit.SECONDS).reason(reason).queue(
            s -> {
                event.reply("Le membre a bien été banni.").setEphemeral(true).queue();
                member.getUser().openPrivateChannel().queue(
                    channel -> channel.sendMessage(
                        "Vous avez été banni sur le serveur **`" + event.getGuild().getName() + "`** par " + event.getUser().getAsMention() +
                            " pour la raison suivante : " + reason + ".\nCette sanction expirera dans: <t:" + (Instant.now().getEpochSecond() + timeSeconds) +
                            ":R>.\n\n-# ***Ceci est un message automatique. Toutes contestations doivent se faire avec le modérateur responsable.***"
                    ).queue(null, f -> event.getHook().editOriginal("Le membre a bien été expulsé... mais impossible d'envoyer un message privé à " + member.getAsMention() + ".").queue())
                );
                
                SanctionDatabase.addSanction(Sanction.SanctionType.BAN, member, event.getUser().getIdLong(), event.getGuild().getIdLong(), reason, TimeParser.parse(time));
            },
            f -> {
                event.reply("Une erreur est survenue lors de l'exécution de la commande.").setEphemeral(true).queue();
                Log.err("Error while executing a command ('ban').", f);
            }
        );
    }
    
    @SlashCommandInteraction(name = "unban", description = "Débannit un membre du serveur.", permissions = Permission.BAN_MEMBERS)
    public void unban(SlashCommandInteractionEvent event,
                      @Option(description = "L'identifiant du membre à débannir.", required = true, autoComplete = true) String userId)
    {
        if (!check(event, null)) return;
        
        long userIdLong;
        try {
            userIdLong = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            event.reply("L'identifiant utilisateur fourni est invalide.").setEphemeral(true).queue();
            return;
        }
        
        if (event.getGuild().retrieveBanList().stream().noneMatch(b -> b.getUser().getIdLong() == userIdLong)) {
            event.reply("Cette utilisateur n'est pas banni.").setEphemeral(true).queue();
            return;
        }
        
        event.getGuild().unban(UserSnowflake.fromId(userIdLong)).queue(
            s -> {
                event.reply("Le membre a bien été débanni.").setEphemeral(true).queue();

                Sanction sanction = SanctionDatabase.getLatest(userIdLong, event.getGuild().getIdLong(), Sanction.SanctionType.BAN);
                Database.update(sanction.setExpired(true));
            },
            f -> {
                event.reply("Une erreur est survenue lors de l'exécution de la commande.").setEphemeral(true).queue();
                Log.err("Error while executing a command ('unban').", f);
            }
        );
    }
    
    private boolean check(SlashCommandInteractionEvent event, Member member) {
        Configuration config = Database.get(Configuration.class, event.getGuild().getIdLong());
        Member executor = event.getMember();
        
        if (!config.moderationEnabled) {
            event.reply("La modération est désactivée sur ce serveur.").setEphemeral(true).queue();
            return false;
        }
        if (member == null) {
            event.reply("Ce membre n'existe pas ou n'est pas présent sur le serveur. Vérifiez que l'identifiant est correct.").setEphemeral(true).queue();
            return false;
        }
        if (executor.equals(member)) {
            event.reply("Ce membre est vous-même, vous ne pouvez pas vous auto-sanctionner").setEphemeral(true).queue();
            return false;
        }
        if (member.getUser().isBot() || member.getUser().isSystem()) {
            event.reply("Ce membre est un robot ou un compte système, vous ne pouvez pas le sanctionner.").setEphemeral(true).queue();
            return false;
        }
        if (member.getRoles().isEmpty() && !executor.getRoles().isEmpty()) {
            return true;
        }
        if (executor.getRoles().isEmpty() || executor.getRoles().getFirst().getPosition() < member.getRoles().getFirst().getPosition()) {
            event.reply("Ce membre a un rôle supérieur au votre, vous ne pouvez pas le sanctionner.").setEphemeral(true).queue();
            return false;
        }
        if (member.isOwner()) {
            event.reply("Ce membre est le propriétaire du serveur, vous ne pouvez pas le sanctionner.").setEphemeral(true).queue();
            return false;
        }
        
        return true;
    }
}
