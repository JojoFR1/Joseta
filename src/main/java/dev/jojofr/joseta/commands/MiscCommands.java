package dev.jojofr.joseta.commands;

import dev.jojofr.joseta.annotations.InteractionModule;
import dev.jojofr.joseta.annotations.types.SlashCommandInteraction;
import dev.jojofr.joseta.database.entities.ConfigurationEntity;
import dev.jojofr.joseta.events.MiscEvents;
import dev.jojofr.joseta.utils.BotCache;
import dev.jojofr.joseta.utils.markov.MarkovGen;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.time.OffsetDateTime;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@InteractionModule
public class MiscCommands {
    
    @SlashCommandInteraction(name = "chat", description = "Un chat d'intelligence artificielle.")
    public void chat(SlashCommandInteractionEvent event) {
        // Stop when its April 2nd
        OffsetDateTime now = event.getTimeCreated();
        if (now.getMonthValue() == 4 && now.getDayOfMonth() > 1) {
            event.reply("L'équipe de Mindustry France est navré de vous annoncer que cette fonctionnalité n'est plus disponible pour le moment. En effet, après une période de test très concluante, nous avons décidé de la retirer pour le moment afin de pouvoir la retravailler et l'améliorer pour vous offrir une meilleure expérience à l'avenir. Nous vous remercions de votre compréhension et de votre soutien continu !").queue();
            return;
        }
        
        event.reply("Grâce à **<@1485973922464661627>**, vous pouvez profiter d'une intelligence artificielle personnalisable et puissante dans votre navigateur web préféré en cliquant sur ce lien: <https://joseta.jojofr.dev/chat>.")
            .queue();
    }
    
    @SlashCommandInteraction(name = "surprendmoi", description = "Obtenez une surprise aléatoire.")
    public void surprise(SlashCommandInteractionEvent event) {
        // Stop when its April 2nd
        OffsetDateTime now = event.getTimeCreated();
        if (now.getMonthValue() == 4 && now.getDayOfMonth() > 1) {
            event.reply("L'équipe de Mindustry France est navré de vous annoncer que cette fonctionnalité n'est plus disponible pour le moment. En effet, après une période de test très concluante, nous avons décidé de la retirer pour le moment afin de pouvoir la retravailler et l'améliorer pour vous offrir une meilleure expérience à l'avenir. Nous vous remercions de votre compréhension et de votre soutien continu !").queue();
            return;
        }
        
        float chance = ThreadLocalRandom.current().nextFloat();
        
        Member member = event.getMember();
        if (member == null) return;
        
        if (chance < 1/1_000_000f) {
            member.timeoutFor(12, TimeUnit.HOURS).queue();
            event.reply("Surprise ! " + member.getAsMention() + " a été mis en exclusion pendant 12 heures ! (mais c'était ultra rare ! 1 chance sur 1 000 000 !)").queue();
        }
        else if (chance < 0.01) {
            member.getGuild().addRoleToMember(member, member.getGuild().getRoleById(1487494404863426701L)).queue();
            event.reply("Surprise ! " + member.getAsMention() + " a obtenu un rôle très spécial ! (1% de chance)").queue();
        }
        else if (chance < 0.10) {
            member.getGuild().addRoleToMember(member, member.getGuild().getRoleById(1483155563251499231L)).queue();
            event.reply("Surprise ! " + member.getAsMention() + " a obtenu un rôle spécial pendant 12 heures ! (10% de chance)").queue(
                success -> member.getGuild().removeRoleFromMember(member, member.getGuild().getRoleById(1483155563251499231L)).queueAfter(12, TimeUnit.HOURS)
            );
        } else if (chance < 0.20) {
            member.getGuild().addRoleToMember(member, member.getGuild().getRoleById(1346232611483156635L)).queue();
            event.reply("Surprise ! " + member.getAsMention() + " a obtenu un rôle cool pendant 6 heures ! (20% de chance)").queue(
                success -> member.getGuild().removeRoleFromMember(member, member.getGuild().getRoleById(1346232611483156635L)).queueAfter(6, TimeUnit.HOURS)
            );
        } else {
            float newChance = ThreadLocalRandom.current().nextFloat();
            
            if (newChance < 0.05) {
                member.timeoutFor(1, TimeUnit.HOURS).queue();
                event.reply("Surprise ! " + member.getAsMention() + " a été mis en exclusion pendant 1 heure ! (4% de chance)").queue();
            } else if  (newChance < 0.15) {
                member.timeoutFor(30, TimeUnit.MINUTES).queue();
                event.reply("Surprise ! " + member.getAsMention() + " a été mis en exclusion pendant 30 minutes ! (12% de chance)").queue();
            } else if (newChance < 0.20) {
                member.timeoutFor(15, TimeUnit.MINUTES).queue();
                event.reply("Surprise ! " + member.getAsMention() + " a été mis en exclusion pendant 15 minutes ! (16% de chance)").queue();
            } else if (newChance < 0.25) {
                member.getGuild().addRoleToMember(member, member.getGuild().getRoleById(1259874357384056852L)).queue();
                member.getGuild().removeRoleFromMember(member, member.getGuild().getRoleById(1235571503412543552L)).queue();
                event.reply("Surprise ! " + member.getAsMention() + " n'est plus vérifier ! (20% de chance)").queue();
            } else if (newChance < 0.30) {
                member.timeoutFor(10, TimeUnit.MINUTES).queue();
                event.reply("Surprise ! " + member.getAsMention() + " a été mis en exclusion pendant 10 minutes ! (24% de chance)").queue();
            } else if (newChance < 0.5) {
                member.timeoutFor(5, TimeUnit.MINUTES).queue();
                event.reply("Surprise ! " + member.getAsMention() + " a été mis en exclusion pendant 5 minutes ! (40% de chance)").queue();
            } else {
                member.timeoutFor(1, TimeUnit.MINUTES).queue();
                event.reply("Surprise ! " + member.getAsMention() + " a été mis en exclusion pendant 1 minute ! (80% de chance)").queue();
            }
        }
    }
    
    @SlashCommandInteraction(name = "jeveuxetrecool")
    public void cool(SlashCommandInteractionEvent event) {
        // Stop when its April 2nd
        OffsetDateTime now = event.getTimeCreated();
        if (now.getMonthValue() == 4 && now.getDayOfMonth() > 1) {
            event.reply("L'équipe de Mindustry France est navré de vous annoncer que cette fonctionnalité n'est plus disponible pour le moment. En effet, après une période de test très concluante, nous avons décidé de la retirer pour le moment afin de pouvoir la retravailler et l'améliorer pour vous offrir une meilleure expérience à l'avenir. Nous vous remercions de votre compréhension et de votre soutien continu !").queue();
            return;
        }
        
        float chance = ThreadLocalRandom.current().nextFloat();
        
        if (chance <= 0.5) {
            event.reply(event.getUser().getAsMention() + ", désolé, tu n'es pas assez cool pour obtenir ce rôle !").queue();
            event.getMember().timeoutFor(5, TimeUnit.MINUTES).queue();
            return;
        }
        
        event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(1346232611483156635L)).queue(
            success -> event.getGuild().removeRoleFromMember(event.getMember(), event.getGuild().getRoleById(1346232611483156635L)).queueAfter(1, TimeUnit.HOURS)
        );
        event.reply(event.getUser().getAsMention() + ", tu es maintenant cool pendant 1 heures !").queue();
    }
    
    @SlashCommandInteraction(name = "ping", description = "Obtenez le ping du bot.")
    public void ping(SlashCommandInteractionEvent event) {
        // Stop when its April 2nd
        OffsetDateTime now = event.getTimeCreated();
        if (now.getMonthValue() == 4 && now.getDayOfMonth() > 1) {
            event.reply("L'équipe de Mindustry France est navré de vous annoncer que cette fonctionnalité n'est plus disponible pour le moment. En effet, après une période de test très concluante, nous avons décidé de la retirer pour le moment afin de pouvoir la retravailler et l'améliorer pour vous offrir une meilleure expérience à l'avenir. Nous vous remercions de votre compréhension et de votre soutien continu !").queue();
            return;
        }
        
        long startTime = System.currentTimeMillis();
        long gatewayPing = event.getJDA().getGatewayPing();
        
        Random random = new Random();
        random.setSeed(startTime + gatewayPing);
        
        event.reply("Pinging...").queue(reply ->
            reply.editOriginal("Pong! Latence: "+ random.nextInt(random.nextInt(5, 100), random.nextInt(100, 1500)) +"ms | Latence API : "+ -gatewayPing +"ms").queue()
        );
    }
    
    /**
     * Original idea by l4p1n in <a href=https://git.l4p1n.ch/l4p1n-bot/bot-rust/src/commit/8afea76f37fa1468e829c37366534e6b345bdc94/bot/AppCommands/MarkovCommand.cs>l4p1n-bot/MarkovCommand.cs</a>
     */
    @SlashCommandInteraction(name = "markov", description = "Génère un message aléatoire à partir des messages du serveur.")
    public void markov(SlashCommandInteractionEvent event) {
        // Stop when its April 2nd
        OffsetDateTime now = event.getTimeCreated();
        if (now.getMonthValue() == 4 && now.getDayOfMonth() > 1) {
            event.reply("L'équipe de Mindustry France est navré de vous annoncer que cette fonctionnalité n'est plus disponible pour le moment. En effet, après une période de test très concluante, nous avons décidé de la retirer pour le moment afin de pouvoir la retravailler et l'améliorer pour vous offrir une meilleure expérience à l'avenir. Nous vous remercions de votre compréhension et de votre soutien continu !").queue();
            return;
        }
        
        ConfigurationEntity config = BotCache.getGuildConfiguration(event.getGuild().getIdLong());
        if (!config.markovEnabled) {
            event.reply("Hein... quoi? ou? comment? aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa").setEphemeral(true).queue();
            return;
        }
        
        event.deferReply().queue(
            hook -> {
                String generatedMessage = MarkovGen.generateMessage(event.getGuild().getIdLong());
                
                hook.editOriginal(generatedMessage).queue();
            }
        );
    }
    
    
    @SlashCommandInteraction(name = "multi", description = "Envoie le texte d'aide pour le multijoueur.")
    public void multiInfo(SlashCommandInteractionEvent event) { event.reply(MiscEvents.autoResponseMessage).queue(); }
}
