package dev.jojofr.joseta.utils.markov;

import java.util.HashMap;

public class MarkovGen {
    public static HashMap<Long, MarkovGen> GUILD_CACHE = new HashMap<>();
    
    private final long guildId;
    
    public MarkovGen(long guildId) {
        this.guildId = guildId;
    }
    
    public static String generateMessage(long guildId) {
        MarkovGen markovGen = GUILD_CACHE.computeIfAbsent(guildId, MarkovGen::new);
        
        return "Cette fonctionnalité est en cours de développement.";
    }
}

