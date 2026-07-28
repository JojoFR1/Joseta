package dev.jojofr.joseta.database;

import org.testcontainers.postgresql.PostgreSQLContainer;

final class SharedPostgresContainer extends PostgreSQLContainer {
    private static SharedPostgresContainer instance;
    
    public SharedPostgresContainer() {
        super("postgres:18");
    }
    
    static synchronized SharedPostgresContainer getInstance() {
        if (instance == null) {
            System.out.print("Starting shared PostgreSQL container for tests...\n");
            instance = new SharedPostgresContainer();
            instance.start();
        }
        
        return instance;
    }
}
