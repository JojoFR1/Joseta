package dev.jojofr.joseta.entities;

import net.dv8tion.jda.api.entities.User;

import java.time.Instant;

public class ReminderListMessage {
    public final Instant timestamp;
    
    public int currentPage;
    public final int lastPage;
    
    public ReminderListMessage(int lastPage) {
        this.timestamp = Instant.now();
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
