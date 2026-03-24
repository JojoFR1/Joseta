package dev.jojofr.joseta.entities;

import dev.jojofr.joseta.database.entities.ReminderEntity;

import java.time.Instant;
import java.util.List;

public class ReminderListMessage {
    public final List<ReminderEntity> reminders;
    public final Instant timestamp;
    
    public int currentPage;
    public final int lastPage;
    
    public ReminderListMessage(List<ReminderEntity> reminders, int lastPage) {
        this.reminders = reminders;
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
