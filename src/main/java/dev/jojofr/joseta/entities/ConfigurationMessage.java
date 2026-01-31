package dev.jojofr.joseta.entities;

import java.time.Instant;

public class ConfigurationMessage {
    public final Instant timestamp;
    
    public ConfigurationMessage(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
