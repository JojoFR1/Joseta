package dev.jojofr.joseta.entities;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public record GuildUserKey(long guildId, long userId) {
    public GuildUserKey(Member member) { this(member.getGuild().getIdLong(), member.getIdLong()); }
    public GuildUserKey(Guild guild, User user) { this(guild.getIdLong(), user.getIdLong()); }
    public GuildUserKey(Guild guild, long userId) { this(guild.getIdLong(), userId); }
}
