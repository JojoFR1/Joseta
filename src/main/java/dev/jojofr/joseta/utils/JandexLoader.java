package dev.jojofr.joseta.utils;

import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public class JandexLoader {

    public static Index load() {
        try (InputStream inputStream = JandexLoader.class.getClassLoader().getResourceAsStream("META-INF/jandex.idx")) {
            if (inputStream == null)
                throw new IllegalStateException("Jandex index file ('META-INF/jandex.idx') not found in the classpath.");
            
            return new IndexReader(inputStream).read();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read Jandex index file ('META-INF/jandex.idx').", e);
        }
    }
}
