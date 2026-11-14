package dev.jojofr.joseta.commands;

import dev.jojofr.joseta.JosetaBot;
import dev.jojofr.joseta.annotations.InteractionModule;
import dev.jojofr.joseta.annotations.types.interaction.SlashCommandInteraction;
import dev.jojofr.joseta.utils.DiscordTimestamp;
import dev.jojofr.joseta.utils.Log;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@InteractionModule
public class SpecialCommand {
    private static final long FAKE_ADMIN_ROLE_ID = 1569651790843215954L;
    private static final long FAKE_MOD_ROLE_ID = 1571287699023728830L;
    private static final long FAKE_SPECIAL_ROLE_ID = 1569651860577718313L;
    private static final long SPECIAL_ROLE_ID = 1483155563251499231L;
    private static final long COOL_ROLE_ID = 1346232611483156635L;
    private static final long FAKE_VIP_ROLE_ID = 1569659426418335824L;
    
    private static final long MEMBER_ROLE_ID = 1235571503412543552L;
    private static final long UNVERIFIED_ROLE_ID = 1259874357384056852L;
    
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
        Member member = event.getMember();
        Guild guild = event.getGuild();
        if (chance < 0.01f) {
            long randomDelay = ThreadLocalRandom.current().nextLong(36, 72);
            guild.addRoleToMember(member, guild.getRoleById(FAKE_ADMIN_ROLE_ID)).queue(
                success -> {
                    event.reply("Surprise ! " + event.getUser().getAsMention() + " a obtenu un rôle **très** spécial pendant " + randomDelay + " heures ! (1% de chance)").queue();
                    guild.removeRoleFromMember(member, guild.getRoleById(FAKE_ADMIN_ROLE_ID)).queueAfter(randomDelay, TimeUnit.HOURS);
                },
                error -> {
                    event.reply("Surprise--... ah, je n'ai pas pu vous donner le rôle **très** spécial... dommage ! (1% de chance)").queue();
                    Log.warn("Failed to add 'fake admin' role to user " + event.getUser().getIdLong() + " in guild " + event.getGuild().getIdLong(), error);
                }
            );
        } else if (chance < 0.05f) {
            long randomDelay = ThreadLocalRandom.current().nextLong(24, 48);
            guild.addRoleToMember(member, guild.getRoleById(FAKE_MOD_ROLE_ID)).queue(
                success -> {
                    event.reply("Surprise ! " + event.getUser().getAsMention() + " a obtenu un rôle *moins spécial* que le **très** spécial mais tout autant __spécial__ pendant " + randomDelay + " heures ! (5% de chance)").queue();
                    guild.removeRoleFromMember(member, guild.getRoleById(FAKE_MOD_ROLE_ID)).queueAfter(randomDelay, TimeUnit.HOURS);
                },
                error -> {
                    event.reply("Surprise--... ah, je n'ai pas pu vous donner le rôle *moins spécial* que le **très** spécial mais tout autant __spécial__... dommage ! (5% de chance)").queue();
                    Log.warn("Failed to add 'fake mod' role to user " + event.getUser().getIdLong() + " in guild " + event.getGuild().getIdLong(), error);
                }
            );
        } else if (chance < 0.10f) {
            long randomDelay = ThreadLocalRandom.current().nextLong(12, 36);
            guild.addRoleToMember(member, guild.getRoleById(FAKE_SPECIAL_ROLE_ID)).queue(
                success -> {
                    event.reply("Surprise ! " + event.getUser().getAsMention() + " est maintenant **\"spécial\"** pendant " + randomDelay + " heures ! (10% de chance)").queue();
                    guild.removeRoleFromMember(member, guild.getRoleById(FAKE_SPECIAL_ROLE_ID)).queueAfter(randomDelay, TimeUnit.HOURS);
                },
                error -> {
                    event.reply("Surprise--... ah, je n'ai pas pu vous rendre **\"spécial\"**... dommage ! (10% de chance)").queue();
                    Log.warn("Failed to add 'fake special' role to user " + event.getUser().getIdLong() + " in guild " + event.getGuild().getIdLong(), error);
                }
            );
        } else if (chance < 0.20f) {
            long randomDelay = ThreadLocalRandom.current().nextLong(6, 24);
            guild.addRoleToMember(member, guild.getRoleById(FAKE_VIP_ROLE_ID)).queue(
                success -> {
                    event.reply("Surprise ! " + event.getUser().getAsMention() + " est maintenant **(pas) important** pendant " + randomDelay + " heures ! (20% de chance)").queue();
                    guild.removeRoleFromMember(member, guild.getRoleById(FAKE_VIP_ROLE_ID)).queueAfter(randomDelay, TimeUnit.HOURS);
                },
                error -> {
                    event.reply("Surprise--... ah, je n'ai pas pu vous rendre **(pas) important**... dommage ! (20% de chance)").queue();
                    Log.warn("Failed to add 'fake vip' role to user " + event.getUser().getIdLong() + " in guild " + event.getGuild().getIdLong(), error);
                }
            );
        } else if (chance < 0.30f) {
            long randomDelay = ThreadLocalRandom.current().nextLong(2, 16);
            guild.addRoleToMember(member, guild.getRoleById(COOL_ROLE_ID)).queue(
                success -> {
                    event.reply("Surprise ! " + event.getUser().getAsMention() + " est maintenant **cool** pendant " + randomDelay + " heures ! (20% de chance)").queue();
                    guild.removeRoleFromMember(member, guild.getRoleById(COOL_ROLE_ID)).queueAfter(randomDelay, TimeUnit.HOURS);
                },
                error -> {
                    event.reply("Surprise--... ah, je n'ai pas pu vous rendre **cool**, mais au fond vous êtes déjà cool ! (20% de chance)").queue();
                    Log.warn("Failed to add 'cool' role to user " + event.getUser().getIdLong() + " in guild " + event.getGuild().getIdLong(), error);
                }
            );
        } else {
            chance = ThreadLocalRandom.current().nextFloat();
            
            if (chance < 0.05f) {
                if (ThreadLocalRandom.current().nextBoolean()) {
                    JosetaBot.putUserInEventTimeout(event.getUser().getIdLong(), 2, TimeUnit.HOURS);
                    event.reply("Surprise ! " + event.getUser().getAsMention() + ", votre accès aux services Premium™ de l'événement a été **restreint** pendant 2 heures ! (XXXXX% de chance)").queue();
                } else {
                    member.timeoutFor(1, TimeUnit.HOURS).queue(
                        success -> event.reply("Surprise ! " + event.getUser().getAsMention() + ", vous avez été mis en **exclusion temporaire** pendant 1 heure ! (XXXXX% de chance)").queue(),
                        error -> {
                            JosetaBot.putUserInEventTimeout(event.getUser().getIdLong(), 1, TimeUnit.HOURS);
                            event.reply("Surprise--... ah, vous êtes trop puissant pour moi... du coup votre accès aux services Premium™ de l'événement a été **restreint** pendant 1 heures ! (XXXXX% de chance)").queue();
                            Log.warn("Failed to timeout user " + event.getUser().getIdLong() + " in guild " + event.getGuild().getIdLong(), error);
                        }
                    );
                }
            } else if (chance < 0.15f) {
                if (ThreadLocalRandom.current().nextBoolean()) {
                    JosetaBot.putUserInEventTimeout(event.getUser().getIdLong(), 1, TimeUnit.HOURS);
                    event.reply("Surprise ! " + event.getUser().getAsMention() + ", votre accès aux services Premium™ de l'événement a été **restreint** pendant 1 heure ! (XXXXX% de chance)").queue();
                } else {
                    member.timeoutFor(30, TimeUnit.MINUTES).queue(
                        success -> event.reply("Surprise ! " + event.getUser().getAsMention() + ", vous avez été mis en **exclusion temporaire** pendant 30 minutes ! (XXXXX% de chance)").queue(),
                        error -> {
                            JosetaBot.putUserInEventTimeout(event.getUser().getIdLong(), 30, TimeUnit.MINUTES);
                            event.reply("Surprise--... ah, vous êtes trop puissant pour moi... du coup votre accès aux services Premium™ de l'événement a été **restreint** pendant 30 minutes ! (XXXXX% de chance)").queue();
                            Log.warn("Failed to timeout user " + event.getUser().getIdLong() + " in guild " + event.getGuild().getIdLong(), error);
                        }
                    );
                }
            } else if (chance < 0.20f) {
                if (ThreadLocalRandom.current().nextBoolean()) {
                    JosetaBot.putUserInEventTimeout(event.getUser().getIdLong(), 30, TimeUnit.MINUTES);
                    event.reply("Surprise ! " + event.getUser().getAsMention() + ", votre accès aux services Premium™ de l'événement a été **restreint** pendant 30 minutes ! (XXXXX% de chance)").queue();
                } else {
                    member.timeoutFor(15, TimeUnit.MINUTES).queue(
                        success -> event.reply("Surprise ! " + event.getUser().getAsMention() + ", vous avez été mis en **exclusion temporaire** pendant 15 minutes ! (XXXXX% de chance)").queue(),
                        error -> {
                            JosetaBot.putUserInEventTimeout(event.getUser().getIdLong(), 15, TimeUnit.MINUTES);
                            event.reply("Surprise--... ah, vous êtes trop puissant pour moi... du coup votre accès aux services Premium™ de l'événement a été **restreint** pendant 15 minutes ! (XXXXX% de chance)").queue();
                            Log.warn("Failed to timeout user " + event.getUser().getIdLong() + " in guild " + event.getGuild().getIdLong(), error);
                        }
                    );
                }
            } else if (chance < 0.25f) {
                guild.addRoleToMember(member, guild.getRoleById(UNVERIFIED_ROLE_ID)).queue();
                guild.removeRoleFromMember(member, guild.getRoleById(MEMBER_ROLE_ID)).queue();
                event.reply("Surprise ! " + event.getUser().getAsMention() + ", n'est plus vérifié ! (XXXXX% de chance)").queue();
            } else if (chance < 0.30f) {
                if (ThreadLocalRandom.current().nextBoolean()) {
                    JosetaBot.putUserInEventTimeout(event.getUser().getIdLong(), 15, TimeUnit.MINUTES);
                    event.reply("Surprise ! " + event.getUser().getAsMention() + ", votre accès aux services Premium™ de l'événement a été **restreint** pendant 15 minutes ! (XXXXX% de chance)").queue();
                } else {
                    member.timeoutFor(10, TimeUnit.MINUTES).queue(
                        success -> event.reply("Surprise ! " + event.getUser().getAsMention() + ", vous avez été mis en **exclusion temporaire** pendant 10 minutes ! (XXXXX% de chance)").queue(),
                        error -> {
                            JosetaBot.putUserInEventTimeout(event.getUser().getIdLong(), 10, TimeUnit.MINUTES);
                            event.reply("Surprise--... ah, vous êtes trop puissant pour moi... du coup votre accès aux services Premium™ de l'événement a été **restreint** pendant 10 minutes ! (XXXXX% de chance)").queue();
                            Log.warn("Failed to timeout user " + event.getUser().getIdLong() + " in guild " + event.getGuild().getIdLong(), error);
                        }
                    );
                }
            } else if (chance < 0.50f) {
                if (ThreadLocalRandom.current().nextBoolean()) {
                    JosetaBot.putUserInEventTimeout(event.getUser().getIdLong(), 10, TimeUnit.MINUTES);
                    event.reply("Surprise ! " + event.getUser().getAsMention() + ", votre accès aux services Premium™ de l'événement a été **restreint** pendant 10 minutes ! (XXXXX% de chance)").queue();
                } else {
                    member.timeoutFor(30, TimeUnit.MINUTES).queue(
                        success -> event.reply("Surprise ! " + event.getUser().getAsMention() + ", vous avez été mis en **exclusion temporaire** pendant 5 minutes ! (XXXXX% de chance)").queue(),
                        error -> {
                            JosetaBot.putUserInEventTimeout(event.getUser().getIdLong(), 5, TimeUnit.MINUTES);
                            event.reply("Surprise--... ah, vous êtes trop puissant pour moi... du coup votre accès aux services Premium™ de l'événement a été **restreint** pendant 5 minutes ! (XXXXX% de chance)").queue();
                            Log.warn("Failed to timeout user " + event.getUser().getIdLong() + " in guild " + event.getGuild().getIdLong(), error);
                        }
                    );
                }
            } else {
                if (ThreadLocalRandom.current().nextBoolean()) {
                    JosetaBot.putUserInEventTimeout(event.getUser().getIdLong(), 5, TimeUnit.MINUTES);
                    event.reply("Surprise ! " + event.getUser().getAsMention() + ", votre accès aux services Premium™ de l'événement a été **restreint** pendant 5 minutes ! (XXXXX% de chance)").queue();
                } else {
                    member.timeoutFor(1, TimeUnit.MINUTES).queue(
                        success -> event.reply("Surprise ! " + event.getUser().getAsMention() + ", vous avez été mis en **exclusion temporaire** pendant 1 minute ! (XXXXX% de chance)").queue(),
                        error -> {
                            JosetaBot.putUserInEventTimeout(event.getUser().getIdLong(), 1, TimeUnit.MINUTES);
                            event.reply("Surprise--... ah, vous êtes trop puissant pour moi... du coup votre accès aux services Premium™ de l'événement a été **restreint** pendant 1 minute ! (XXXXX% de chance)").queue();
                            Log.warn("Failed to timeout user " + event.getUser().getIdLong() + " in guild " + event.getGuild().getIdLong(), error);
                        }
                    );
                }
            }
        }
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
        if (chance < 0.5f) {
            chance = ThreadLocalRandom.current().nextFloat();
            if (chance < 0.8f) {
                JosetaBot.putUserInEventTimeout(event.getUser().getIdLong(), 1, TimeUnit.HOURS);
                event.reply("Dommage... votre accès aux services Premium™ de l'événement a été **restreint** pour 1 heures.\n\n-# Ceci avait 40% de chance de se produire.").queue();
            } else {
                event.getGuild().timeoutFor(event.getMember(), 5, TimeUnit.MINUTES).reason("JstaDNR Event Anniversaire JstaDNR").queue(
                    ignore -> event.reply("Terrible... vous avez été mis en **exclusion temporaire** pour 5 minutes par le bot.\n\n-# Ceci avait 10% de chance de se produire.").queue(),
                    error -> {
                        JosetaBot.putUserInEventTimeout(event.getUser().getIdLong(), 1, TimeUnit.HOURS);
                        event.reply("Terribl-... ah, vous êtes trop puissant pour moi... du coup votre accès aux services Premium™ de l'événement a été **restreint** pour 1 heures.\n\n-# Ceci avait 10% de chance de se produire.").queue();
                        Log.warn("Failed to timeout user " + event.getUser().getIdLong() + " in guild " + event.getGuild().getIdLong(), error);
                    }
                );
            }
            return;
        }
        
        event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(COOL_ROLE_ID)).queue(
            success -> {
                event.reply("Incroyable ! " + event.getUser().getAsMention() + ", tu es maintenant **cool** pendant 1 heures !\n\n-# Ceci avait 50% de chance de se produire.").queue();
                event.getGuild().removeRoleFromMember(event.getMember(), event.getGuild().getRoleById(COOL_ROLE_ID)).queueAfter(1, TimeUnit.HOURS);
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

