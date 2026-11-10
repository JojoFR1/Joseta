package dev.jojofr.joseta.commands;

import dev.jojofr.joseta.JosetaBot;
import dev.jojofr.joseta.annotations.InteractionModule;
import dev.jojofr.joseta.annotations.types.interaction.SlashCommandInteraction;
import dev.jojofr.joseta.utils.DiscordTimestamp;
import dev.jojofr.joseta.utils.Log;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@InteractionModule
public class SpecialCommand {
    
    @SlashCommandInteraction(name = "surprendmoi", description = "Obtenez une surprise aléatoire.")
    public void surprise(SlashCommandInteractionEvent event) {
        if (!JosetaBot.isEventActive()) {
            event.reply("L'équipe de Mindustry France est navré de vous annoncer que cette fonctionnalité n'est plus disponible pour le moment.").setEphemeral(true).queue();
            return;
        }
        DiscordTimestamp timeoutEnd = JosetaBot.getUserEventTimeout(event.getUser().getIdLong());
        if (timeoutEnd != null) {
            event.reply("Votre accès aux services Premium™ de l'événement est actuellement **restreint** jusqu'au " + timeoutEnd.longFull() + " ("+ timeoutEnd.relative() +").").setEphemeral(true).queue();
            return;
        }
        
        float chance = ThreadLocalRandom.current().nextFloat();
        
    }
    
    @SlashCommandInteraction(name = "jeveuxetrecool")
    public void cool(SlashCommandInteractionEvent event) {
        if (!JosetaBot.isEventActive()) {
            event.reply("L'équipe de Mindustry France est navré de vous annoncer que cette fonctionnalité n'est plus disponible pour le moment.").setEphemeral(true).queue();
            return;
        }
        DiscordTimestamp timeoutEnd = JosetaBot.getUserEventTimeout(event.getUser().getIdLong());
        if (timeoutEnd != null) {
            event.reply("Votre accès aux services Premium™ de l'événement est actuellement **restreint** jusqu'au " + timeoutEnd.longFull() + " ("+ timeoutEnd.relative() +").").setEphemeral(true).queue();
            return;
        }
        
        float chance = ThreadLocalRandom.current().nextFloat();
        // 50% chance to be put in timeout, then 80% for an "event timeout" (which is purely bot side), and 20% chance for a Discord timeout
        if (chance <= 0.5f) {
            chance = ThreadLocalRandom.current().nextFloat();
            if (chance <= 0.8f) {
                JosetaBot.putUserInEventTimeout(event.getUser().getIdLong(), Instant.now().plusSeconds(3600));
                event.reply("Dommage... votre accès aux services Premium™ de l'événement a été **restreint** pour 1 heures.\n\n-# Ceci avait 40% de chance de se produire.").queue();
            } else {
                event.getGuild().timeoutFor(event.getMember(), 5, TimeUnit.MINUTES).reason("JstaDNR Event Anniversaire JstaDNR").queue(
                    ignore -> event.reply("Terrible... vous avez été mis en **exclusion temporaire** pour 5 minutes par le bot.\n\n-# Ceci avait 10% de chance de se produire.").queue(),
                    error -> {
                        JosetaBot.putUserInEventTimeout(event.getUser().getIdLong(), Instant.now().plusSeconds(3600));
                        event.reply("Terribl-... ah, vous êtes trop puissant pour moi... du coup votre accès aux services Premium™ de l'événement a été **restreint** pour 1 heures.\n\n-# Ceci avait 10% de chance de se produire.").queue();
                        Log.warn("Failed to timeout user " + event.getUser().getIdLong() + " in guild " + event.getGuild().getIdLong(), error);
                    }
                );
            }
            return;
        }
        
        event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(1346232611483156635L)).queue(
            success -> {
                event.reply("Incroyable ! " + event.getUser().getAsMention() + ", tu es maintenant **cool** pendant 1 heures !\n\n-# Ceci avait 50% de chance de se produire.").queue();
                event.getGuild().removeRoleFromMember(event.getMember(), event.getGuild().getRoleById(1346232611483156635L)).queueAfter(1, TimeUnit.HOURS);
            },
            error -> {
                event.reply("Dommage... le bot n'a pas pu vous rendre **cool**, mais au fond vous êtes déjà cool !\n\n-# Ceci avait 50% de chance de se produire.").queue();
                Log.warn("Failed to add 'cool' role to user " + event.getUser().getIdLong() + " in guild " + event.getGuild().getIdLong(), error);
            }
        );
    }
    
    @SlashCommandInteraction(name = "jepeux")
    public void isEventTimeout(SlashCommandInteractionEvent event) {
        if (!JosetaBot.isEventActive()) {
            event.reply("L'équipe de Mindustry France est navré de vous annoncer que cette fonctionnalité n'est plus disponible pour le moment.").setEphemeral(true).queue();
            return;
        }
        DiscordTimestamp timeoutEnd = JosetaBot.getUserEventTimeout(event.getUser().getIdLong());
        if (timeoutEnd != null)
            event.reply("Votre accès aux services Premium™ de l'événement est actuellement **restreint** jusqu'au "+ timeoutEnd.longFull() +" ("+ timeoutEnd.relative() +").").queue();
        else event.reply("Votre accès aux services Premium™ de l'événement est actuellement **autorisé**.").queue();
    }
}

