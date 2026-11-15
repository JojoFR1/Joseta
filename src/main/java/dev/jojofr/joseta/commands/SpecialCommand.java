package dev.jojofr.joseta.commands;

import dev.jojofr.joseta.JosetaBot;
import dev.jojofr.joseta.anniversary.AnniversaryEventDao;
import dev.jojofr.joseta.anniversary.GachaCards;
import dev.jojofr.joseta.annotations.InteractionModule;
import dev.jojofr.joseta.annotations.types.Option;
import dev.jojofr.joseta.annotations.types.interaction.SlashCommandInteraction;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.utils.DiscordTimestamp;
import dev.jojofr.joseta.utils.Log;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.ToDoubleFunction;

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
    
    private static final Duration GACHA_COOLDOWN = Duration.ofMillis(6);
    
    private record RoleOutcome(long roleId, int minHours, int maxHours, double chance, String success, String failure, String logName) {}
    private static final List<RoleOutcome> ROLE_OUTCOMES = List.of(
        new RoleOutcome(FAKE_ADMIN_ROLE_ID, 36, 72, 1,
            "Surprise ! {user} a obtenu un rôle **très** spécial pendant {hours} ! ({chance} de chance)",
            "Surprise--... ah, je n'ai pas pu vous donner le rôle **très** spécial... dommage ! ({chance} de chance)",
            "fake admin"
        ),
        new RoleOutcome(FAKE_MOD_ROLE_ID, 24, 48, 3,
            "Surprise ! {user} a obtenu un rôle *moins spécial* que le **très** spécial mais tout autant __spécial__ pendant {hours} ! ({chance} de chance)",
            "Surprise--... ah, je n'ai pas pu vous donner le rôle *moins spécial* que le **très** spécial mais tout autant __spécial__... dommage ! ({chance} de chance)",
            "fake mod"
        ),
        new RoleOutcome(FAKE_SPECIAL_ROLE_ID, 12, 36, 6,
            "Surprise ! {user} est maintenant **\"spécial\"** pendant {hours} ! ({chance} de chance)",
            "Surprise--... ah, je n'ai pas pu vous rendre **\"spécial\"**... dommage ! ({chance} de chance)",
            "fake special"
        ),
        new RoleOutcome(FAKE_VIP_ROLE_ID, 6, 24, 12,
            "Surprise ! {user} est maintenant **(pas) important** pendant {hours} ! ({chance} de chance)",
            "Surprise--... ah, je n'ai pas pu vous rendre **(pas) important**... dommage ! ({chance} de chance)",
            "fake vip"
        ),
        new RoleOutcome(COOL_ROLE_ID, 2, 16, 18,
            "Surprise ! {user} est maintenant **cool** pendant {hours} ! ({chance} de chance)",
            "Surprise--... ah, je n'ai pas pu vous rendre **cool**, mais au fond vous êtes déjà cool ! ({chance} de chance)",
            "cool"
        )
    );
    
    private static final double NEGATIVE_CHANCE = 100 - ROLE_OUTCOMES.stream().mapToDouble(RoleOutcome::chance).sum();
    private record Penalty(double chance, Duration restriction, Duration discordTimeout) {};
    private static final List<Penalty> PENALTIES = List.of(
        new Penalty(5, Duration.ofHours(2), Duration.ofHours(1)),
        new Penalty(10, Duration.ofHours(1), Duration.ofMinutes(30)),
        new Penalty(5, Duration.ofMinutes(30), Duration.ofMinutes(15)),
        new Penalty(5, null, null), // Unverified
        new Penalty(5, Duration.ofMinutes(15), Duration.ofMinutes(10)),
        new Penalty(20, Duration.ofMinutes(10), Duration.ofMinutes(5)),
        new Penalty(50, Duration.ofMinutes(5), Duration.ofMinutes(1))
    );
    
    static {
        double penaltyTotal = PENALTIES.stream().mapToDouble(Penalty::chance).sum();
        if (NEGATIVE_CHANCE < 0 || Math.abs(penaltyTotal - 100) > 1e-9) {
            throw new IllegalStateException("Event odds are inconsistent: role outcomes leave " + NEGATIVE_CHANCE + "% for penalties, but penalties add up to " + penaltyTotal + "% instead of 100%");
        }
    }
    
    private static final double COOL_BAD_CHANCE = 50;
    private static final double COOL_RESTRICT_CHANCE = 80;
    private static final Duration COOL_DURATION = Duration.ofHours(1);
    private static final Duration COOL_RESTRICTION = Duration.ofHours(1);
    private static final Duration COOL_DISCORD_TIMEOUT = Duration.ofMinutes(5);
    
    @SlashCommandInteraction(name = "surprendmoi", description = "Obtenez une surprise aléatoirement aléatoire !")
    public void surprise(SlashCommandInteractionEvent event) {
        if (isBlocked(event, true)) return;
        
        RoleOutcome outcome = pick(ROLE_OUTCOMES, RoleOutcome::chance, ThreadLocalRandom.current().nextDouble() * 100);
        if (outcome != null) {
            giveRandomRole(event, outcome);
            return;
        }
        
        Penalty penalty = pick(PENALTIES, Penalty::chance, ThreadLocalRandom.current().nextDouble() * 100);
        if (penalty == null) penalty = PENALTIES.get(PENALTIES.size() - 1);
        applyPenalty(event, penalty);
    }
    
    @SlashCommandInteraction(name = "jeveuxetrecool", description = "Tu veux être cool ?")
    public void cool(SlashCommandInteractionEvent event) {
        if (isBlocked(event, true)) return;
        
        double restrictChance = COOL_BAD_CHANCE * COOL_RESTRICT_CHANCE / 100;
        double timeoutChance = COOL_BAD_CHANCE - restrictChance;
        double coolChance = 100 - COOL_BAD_CHANCE;
        double roll = ThreadLocalRandom.current().nextDouble() * 100;
        String mention = event.getUser().getAsMention();
        
        if (roll < restrictChance) {
            restrict(event, COOL_RESTRICTION);
            event.reply("Dommage... votre accès aux services Premium™ de l'événement a été **restreint** pour " + formatDuration(COOL_RESTRICTION) + ".\n\n-# Ceci avait " + formatPercentage(restrictChance) + " de chance de se produire.").queue();
        } else if (roll < COOL_BAD_CHANCE) {
            timeoutOrFallback(event, COOL_DISCORD_TIMEOUT,
                () -> event.reply("Terrible... " + mention + ", vous avez été mis en **exclusion temporaire** pour " + formatDuration(COOL_DISCORD_TIMEOUT) + " par le bot.\n\n-# Ceci avait " + formatPercentage(timeoutChance) + " de chance de se produire.").queue(),
                error -> {
                    restrict(event, COOL_RESTRICTION);
                    event.reply("Terribl-... ah, vous êtes trop puissant pour moi... du coup votre accès aux services Premium™ de l'événement a été **restreint** pour " + formatDuration(COOL_RESTRICTION) + ".\n\n-# Ceci avait " + formatPercentage(timeoutChance) + " de chance de se produire.").queue();
                    Log.warn("Failed to timeout user {} in guild {}", error, event.getUser().getIdLong(), event.getGuild().getIdLong());
                }
            );
            return;
        } else {
            addTemporaryRole(event, COOL_ROLE_ID, "cool", COOL_DURATION,
                () -> event.reply("Incroyable ! " + mention + ", tu es maintenant **cool** pendant " + formatDuration(COOL_DURATION) + " !\n\n-# Ceci avait " + formatPercentage(coolChance) + " de chance de se produire.").queue(),
                () -> event.reply("Dommage... le bot n'a pas pu vous rendre **cool**, mais au fond vous êtes déjà cool !\n\n-# Ceci avait " + formatPercentage(coolChance) + " de chance de se produire.").queue()
            );
        }
    }
    
    @SlashCommandInteraction(name = "jepeux", description = "Vérifie si vos services Premium™ de l'événement sont actuellement restreints.")
    public void isEventTimeout(SlashCommandInteractionEvent event) {
        if (isBlocked(event, false)) return;
        
        DiscordTimestamp timeoutEnd = JosetaBot.getUserEventTimeout(event.getUser().getIdLong());
        if (timeoutEnd != null)
            event.reply("Votre accès aux services Premium™ de l'événement est actuellement **restreint** jusqu'au "+ timeoutEnd.longFull() +" ("+ timeoutEnd.relative() +").").queue();
        else event.reply("Votre accès aux services Premium™ de l'événement est actuellement **autorisé**.").queue();
    }
    
    private static final String[] MAGIC_8_BALL_RESPONSES = {
        "Oui.",
        "Non.",
        "Peut-être.",
        "Certainement !",
        "Je ne pense pas.",
        "Demande à nouveau plus tard.",
        "C'est certain.",
        "Je ne peux pas te le dire maintenant.",
        "Les signes indiquent que oui.",
        "Les signes indiquent que non.",
        "Absolument !",
        "Pas du tout.",
        "C'est possible.",
        "Je ne suis pas sûr.",
        "Oui, mais avec des conditions.",
        "Non, mais il y a de l'espoir.",
        "Je ne peux pas prédire ça.",
        "C'est une question difficile.",
        "Je ne suis pas autorisé à répondre à cette question."
    };
    @SlashCommandInteraction(name = "doisje", description = "Posez une question et vous recevrez une réponse garantie")
    public void should(SlashCommandInteractionEvent event, @Option(name = "question", description = "La question à poser", required = true) String question) {
        if (isBlocked(event, true)) return;
        
        String response = MAGIC_8_BALL_RESPONSES[ThreadLocalRandom.current().nextInt(MAGIC_8_BALL_RESPONSES.length)];
        event.reply(event.getUser().getAsMention() + ", vous avez demandé : \"" + question + "\"\n\nRéponse : " + response).queue();
    }
    
    @SlashCommandInteraction(name = "chat", description = "Discutez avec une IA surpuissante !")
    public void chat(SlashCommandInteractionEvent event) {
        if (isBlocked(event, true)) return;
        
        event.reply("Profitez d'une intelligence artificielle surpuissante et personnalisable directement dans votre navigateur web ! Rendez-vous sur <https://joseta.jojofr.dev/chat> pour commencer à discuter avec Joseta.").queue();
    }
    
    
    @SlashCommandInteraction(name = "gacha", description = "Tentez votre chance pour obtenir un rôle spécial !")
    public void gacha(SlashCommandInteractionEvent event) {
        if (isBlocked(event, true)) return;
        
        Guild guild = event.getGuild();
        Member member = event.getMember();
        long userId = event.getUser().getIdLong();
        Instant now = Instant.now();
        
        AnniversaryEventDao.PullResult pull;
        List<Integer> owned;
        try {
            AnniversaryEventDao dao = dao();
            pull = dao.pull(userId, now, GACHA_COOLDOWN, GachaCards::roll);
            if (pull == null) {
                Instant next = dao.lastPull(userId).orElse(now).plus(GACHA_COOLDOWN);
                event.reply("Vous avez déjà tenté votre chance récemment ! Prochain tirage possible " + DiscordTimestamp.from(next).relative() + " (" + DiscordTimestamp.from(next).longFull() + ").").setEphemeral(true).queue();
                return;
            }
            owned = dao.ownedCards(userId);
        } catch (Exception e) {
            Log.warn("Failed to perform gacha pull for user {} in guild {}", e, userId, guild.getIdLong());
            event.reply("Une erreur est survenue lors de votre tirage. Veuillez réessayer plus tard.").setEphemeral(true).queue();
            return;
        }
        
        AnniversaryEventDao.PullResult finalPull = pull;
        GachaCards.Card card = GachaCards.byId(finalPull.cardId());
        boolean isNew = finalPull.copies() == 1;
        boolean setCompleted = isNew && GachaCards.isSetComplete(owned, card.set());
        Instant next = now.plus(GACHA_COOLDOWN);
        
        Role role = guild.getRoleById(SPECIAL_ROLE_ID);
        boolean needsRole = role != null && GachaCards.anySetComplete(owned) && !member.getRoles().contains(role);
        
        StringBuilder message = new StringBuilder(isNew ? "**Nouvelle photo !**" : "Doublon (x" + finalPull.copies() + ")");
        if (setCompleted) message.append("\n\uD83C\uDFC6 **Set ").append(card.set() + 1).append(" complété !**");
        message.append("\nProchain tirage possible ").append(DiscordTimestamp.from(next).relative()).append(" (").append(DiscordTimestamp.from(next).longFull()).append(").");
        
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("\uD83C\uDFB4 " + card.name())
            .setDescription(message)
            .addField("Rareté", card.rarity().label() + " (" + formatPercentage(GachaCards.chanceOf(card)) + " de chance)", true)
            .addField("Collection", gachaProgress(owned), true)
            .setFooter(guild.getName() + " - Tirage n°" + finalPull.totalPulls(), guild.getIconUrl())
            .setTimestamp(now);
        if (!card.imageUrl().isEmpty()) embed.setImage("https://jojofr.dev/" + card.imageUrl());
        
        String intro = event.getUser().getAsMention() + " a tenté sa chance !";
        if (!needsRole) {
            event.reply(intro).addEmbeds(embed.build()).queue();
            return;
        }
        
        guild.addRoleToMember(member, role).queue(
            success -> {
                embed.appendDescription("\n\uD83C\uDF96️ Vous obtenez le rôle " + role.getAsMention() + " !");
                event.reply(intro).addEmbeds(embed.build()).queue();
            },
            error -> {
                Log.warn("Failed to give gacha role to user {} in guild {}", error, userId, guild.getIdLong());
                embed.appendDescription("\n⚠ Le bot n'a pas pu vous donner le rôle " + role.getAsMention() + ", une nouvelle tentative aura lieu à votre prochain tirage.");
                event.reply(intro).addEmbeds(embed.build()).queue();
            }
        );
    }
    
    
    private void giveRandomRole(SlashCommandInteractionEvent event, RoleOutcome outcome) {
        Duration duration = Duration.ofHours(ThreadLocalRandom.current().nextLong(outcome.minHours(), outcome.maxHours()));
        String chance = formatPercentage(outcome.chance());
        addTemporaryRole(event, outcome.roleId(), outcome.logName(), duration,
            () -> event.reply(formatTemplate(outcome.success(), event, duration, chance)).queue(),
            () -> event.reply(formatTemplate(outcome.failure(), event, duration, chance)).queue()
        );
    }
    
    private void applyPenalty(SlashCommandInteractionEvent event, Penalty penalty) {
        Guild guild = event.getGuild();
        Member member = event.getMember();
        String userMention = event.getUser().getAsMention();
        double bandChance = NEGATIVE_CHANCE * penalty.chance() / 100;
        
        if (penalty.discordTimeout() == null && penalty.restriction() == null) {
            guild.addRoleToMember(member, guild.getRoleById(UNVERIFIED_ROLE_ID)).queue();
            guild.removeRoleFromMember(member, guild.getRoleById(MEMBER_ROLE_ID)).queue();
            event.reply("Surprise ! " + userMention + ", vous n'êtes plus vérifié ! (" + formatPercentage(bandChance) + " de chance)").queue();
            return;
        }
        
        String chance = formatPercentage(bandChance / 2);
        Duration restriction = penalty.restriction();
        Duration discordTimeout = penalty.discordTimeout();
        
        if (ThreadLocalRandom.current().nextBoolean()) {
            restrict(event, restriction);
            event.reply("Surprise ! " + userMention + ", votre accès aux services Premium™ de l'événement a été **restreint** pendant " + formatDuration(restriction) + " ! (" + chance + " de chance)").queue();
        } else {
            timeoutOrFallback(event, discordTimeout,
                () -> event.reply("Surprise ! " + userMention + ", vous avez été mis en **exclusion temporaire** pendant " + formatDuration(discordTimeout) + " ! (" + chance + " de chance)").queue(),
                error -> {
                    restrict(event, discordTimeout);
                    event.reply("Surprise--... ah, vous êtes trop puissant pour moi... du coup votre accès aux services Premium™ de l'événement a été **restreint** pendant " + formatDuration(discordTimeout) + " ! (" + chance + " de chance)").queue();
                    Log.warn("Failed to timeout user {} in guild {}", error, event.getUser().getIdLong(), event.getGuild().getIdLong());
                }
            );
        }
    }
    
    private static void timeoutOrFallback(SlashCommandInteractionEvent event, Duration duration, Runnable runnable, Consumer<Throwable> error) {
        try {
            event.getMember().timeoutFor(duration.toMinutes(), TimeUnit.MINUTES).reason("JstaDNR Event Anniversaire JstaDNR").queue(
                success -> runnable.run(),
                error
            );
        } catch (RuntimeException e) {
            error.accept(e);
        }
    }
    
    
    private static AnniversaryEventDao dao() { return Database.getJdbi().onDemand(AnniversaryEventDao.class); }
    
    private static boolean isBlocked(SlashCommandInteractionEvent event, boolean checkRestriction) {
        if (!JosetaBot.isEventActive()) {
            event.reply("L'équipe de Mindustry France est navrée de vous annoncer que cette fonctionnalité n'est plus disponible pour le moment.").setEphemeral(true).queue();
            return true;
        }
        if (!checkRestriction) return false;
        
        DiscordTimestamp timeoutEnd = JosetaBot.getUserEventTimeout(event.getUser().getIdLong());
        if (timeoutEnd == null) return false;
        
        event.reply("Votre accès aux services Premium™ de l'événement est actuellement **restreint** jusqu'au " + timeoutEnd.longFull() + " ("+ timeoutEnd.relative() +").").setEphemeral(true).queue();
        return true;
    }
    
    private static void restrict(SlashCommandInteractionEvent event, Duration duration) {
        JosetaBot.putUserInEventTimeout(event.getUser().getIdLong(), duration.toMinutes(), TimeUnit.MINUTES);
    }
    
    private void addTemporaryRole(SlashCommandInteractionEvent event, long roleId, String logName, Duration duration, Runnable onSuccess, Runnable onFailure) {
        Guild guild = event.getGuild();
        Member member = event.getMember();
        long userId = event.getUser().getIdLong();
        
        Role role = guild.getRoleById(roleId);
        if (role == null) {
            Log.warn("Role '{}' ({}) does not exist in guild {}", new IllegalStateException("Role not found"), logName, roleId, guild.getIdLong());
            onFailure.run();
            return;
        }
        
        try {
            dao().upsertTemporaryRole(guild.getIdLong(), userId, roleId, Instant.now().plus(duration));
        } catch (Exception e) {
            Log.warn("Failed to store temporary role '{}' ({}) for user {} in guild {}", e, logName, roleId, userId, guild.getIdLong());
            onFailure.run();
            return;
        }
        
        guild.addRoleToMember(member, role).queue(
            success -> onSuccess.run(),
            error -> {
                Log.warn("Failed to add temporary role '{}' ({}) to user {} in guild {}", error, logName, roleId, userId, guild.getIdLong());
                onFailure.run();
            }
        );
    }
    
    private static <T> T pick(List<T> items, ToDoubleFunction<T> width, double roll) {
        for (T item : items) {
            double bandwidth = width.applyAsDouble(item);
            if (roll < bandwidth) return item;
            roll -= bandwidth;
        }
        return null;
    }
    
    private static String formatTemplate(String template, SlashCommandInteractionEvent event, Duration duration, String chance) {
        return template.replace("{user}", event.getUser().getAsMention())
                       .replace("{hours}", formatDuration(duration))
                       .replace("{chance}", chance);
    }
    private static String formatPercentage(double percent) {
        BigDecimal value = BigDecimal.valueOf(percent).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
        return value.toPlainString().replace(".", ",") + "%";
    }
    private static String formatDuration(Duration duration) {
        long minutes = duration.toMinutes();
        if (minutes % 60 == 0) {
            long hours = minutes / 60;
            return hours + (hours > 1 ? " heures" : " heure");
        }
        return minutes + (minutes > 1 ? " minutes" : " minute");
    }
    
    private static String gachaProgress(List<Integer> owned) {
        StringBuilder sb = new StringBuilder();
        for (int set = 0; set < GachaCards.SET_COUNT; set++) {
            if (set > 0) sb.append('\n');
            sb.append("Set ").append(set + 1).append(": ")
                .append(GachaCards.ownedInSet(owned, set)).append('/').append(GachaCards.SET_SIZE);
        }
        return sb.toString();
    }
}

