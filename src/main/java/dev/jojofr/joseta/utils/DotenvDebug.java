package dev.jojofr.joseta.utils;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvBuilder;
import io.github.cdimascio.dotenv.DotenvEntry;
import io.github.cdimascio.dotenv.DotenvException;
import io.github.cdimascio.dotenv.internal.DotenvParser;
import io.github.cdimascio.dotenv.internal.DotenvReader;

import java.util.*;

import static java.util.stream.Collectors.*;


/**
 * A custom Dotenv loader that supports a debug mode by appending {@code _DEV} to keys when retrieving values.
 * <p>
 * It is a copy of {@link io.github.cdimascio.dotenv.DotenvBuilder} with modifications to support debug mode. It does not
 * do anything else.
 */
public class DotenvDebug extends DotenvBuilder {
    private String filename = ".env";
    private String directoryPath = "./";
    private boolean systemProperties = false;
    private boolean throwIfMissing = true;
    private boolean throwIfMalformed = true;
    
    static public Dotenv load(boolean debug) { return new DotenvDebug().loadImpl(debug); }
    
    private Dotenv loadImpl(boolean debug) throws DotenvException {
        final var reader = new DotenvParser(
                                new DotenvReader(directoryPath, filename),
                                throwIfMissing, throwIfMalformed);
        final List<DotenvEntry> env = reader.parse();
        if (systemProperties) {
            env.forEach(it -> System.setProperty(it.getKey(), it.getValue()));
        }
        
        return new DotenvDebugImpl(env, debug);
    }
    
    static class DotenvDebugImpl implements Dotenv {
        private final Map<String, String> envVars;
        private final Set<DotenvEntry> set;
        private final Set<DotenvEntry> setInFile;
        private final boolean debug;
        
        public DotenvDebugImpl(final List<DotenvEntry> envVars, boolean debug) {
            final Map<String, String> envVarsInFile =
                envVars.stream()
                    .collect(toMap(DotenvEntry::getKey, DotenvEntry::getValue, (a, b) -> b));
            
            this.envVars = new HashMap<>(envVarsInFile);
            this.envVars.putAll(System.getenv());
            
            this.set =
                this.envVars.entrySet()
                    .stream()
                    .map(it -> new DotenvEntry(it.getKey(), it.getValue()))
                    .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));
            
            this.setInFile =
                envVarsInFile.entrySet()
                    .stream()
                    .map(it -> new DotenvEntry(it.getKey(), it.getValue()))
                    .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));
            
            this.debug = debug;
        }
        
        @Override
        public Set<DotenvEntry> entries() {
            return set;
        }
        
        @Override
        public Set<DotenvEntry> entries(final Dotenv.Filter filter) {
            return filter == null ? entries() : setInFile;
        }
        
        @Override
        public String get(String key) {
            key = debug ? key + "_DEV" : key;
            final String value = System.getenv(key);
            return value == null ? envVars.get(key) : value;
        }
        
        @Override
        public String get(String key, String defaultValue) {
            key = debug ? key + "_DEV" : key;
            final String value = this.get(key);
            return value == null ? defaultValue : value;
        }
    }
}
