package joseta.entities;

import net.dv8tion.jda.api.entities.User;

import java.time.Instant;

public class ModlogMessage {
    public final User user;
    public final Instant timestamp;
    
    public int currentPage;
    public final int lastPage;
    
    public ModlogMessage(User user, int lastPage, Instant timestamp) {
        this.user = user;
        this.timestamp = timestamp;
        this.currentPage = 1;
        this.lastPage = lastPage;
    }
    
    public int nextPage() {
        if (currentPage < lastPage) return ++currentPage;
        return currentPage;
    }
    
    public int previousPage() {
        if (currentPage > 1) return --currentPage;
        return currentPage;
    }
}
