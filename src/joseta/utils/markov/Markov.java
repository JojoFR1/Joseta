package joseta.utils.markov;

import arc.files.*;
import arc.math.*;
import arc.struct.*;
import arc.util.serialization.*;

import java.util.regex.*;

/**
 * A markov generator. </br></br>
 * 
 * Translated F# to Java from: <a href=https://github.com/dweslynch/MarkovNextGen/blob/9e842bf5a1e5f15edbcec4b83a2b7bcffa2a54a7/MarkovNextGen/MarkovNextGen/Markov.fs>MarkovNextGen/Markov.fs</a>
 */
public class Markov {
    private final Rand rand = new Rand();
    private final String filename;
    private ObjectMap<String, MarkovLink> chain;

    public Markov() {
        this("markov.pdo");
    }

    public Markov(String filename) {
        this.filename = filename;
        this.chain = MarkovUtilities.readChain(filename);
    }

    public ObjectMap<String, MarkovLink> getChain() {
        return chain;
    }
    public void setChain(ObjectMap<String, MarkovLink> chain) {
        this.chain = chain;
    }

    private ObjectMap<String, MarkovLink> readChain() {
        Fi file = new Fi(filename);
        Json json = new Json();
        return json.fromJson(new ObjectMap<String, MarkovLink>().getClass(), file);
    }

    private void writeChain() {
        Fi file = new Fi(filename);
        Json json = new Json();
        file.writeString(json.toJson(chain), false);
    }

    /**
     * Adds a single line of text to the chain without updating the chain file.
     * 
     * @param string
     *        The line of text to be processed.
     */
    private void linkToChain(String string) {
        String[] words = string.split(" ");

        if (words.length > 1) {
            for (int i = 0; i < words.length - 2; i++) {
                String key = words[i];
                String value = words[i + 1];

                if (chain.containsKey(key)) chain.get(key).addAfter(value);
                else chain.put(key, new MarkovLink(value));
            }
        }
    }

    /**
     * Adds a link to the chain and writes to the chain file.
     * 
     * @param word
     *        The key word to add.
     * @param link
     *       The list of words that could follow it.
     */
    public void addToChain(String word, Seq<String> link) {
        if (word != null && !word.isEmpty()) {
            if (chain.containsKey(word)) chain.get(word).addAfter(link);
            else chain.put(word, new MarkovLink(link));
            writeChain();
        }
    }

    /**
     * Adds a single line of text to the chain and writes to the chain file.
     * 
     * @param string
     *        The line of text to be processed.
     */
    public void addToChain(String string) {
        linkToChain(string);
        writeChain();
    }

    /**
     * Adds multiple lines of text to the chain and writes to the chain file.
     * 
     * @param list
     *        The lines of text to be processed.
     */
    public void addToChain(Seq<String> list) {
        for (String string : list) linkToChain(string);
        writeChain();
    } 

    /**
     * Merges another chain into this one.
     * 
     * @param map
     *        The chain to be merged.
     */
    public void addToChain(ObjectMap<String, MarkovLink> map) {
        for (var kvp : map) {
            if (chain.containsKey(kvp.key)) chain.get(kvp.key).addAfter(kvp.value.getAfter());
            else chain.put(kvp.key, kvp.value);
        }
        writeChain();
    }

    /**
     * Writes the chain to a different file.
     * 
     * @param file
     *       The name of the output file.
     */
    public void dump(String file) {
        Fi fi = new Fi(file);
        Json json = new Json();
        fi.writeString(json.toJson(chain), false);
    }

    /**
     * Prints the chain to console
     */
    public void printChain() {
        // Using static utility since there *shouldn't* be a performance loss
        MarkovUtilities.printChain(chain);
    }

    /**
    * Generates a 'sentence' based on a specified length and starting word.
    * 
    * @param length
    *       The number of words to generate.
    * @param word
    *      The starting word.
    * @return
    *       A randomly generated 'sentence'.
    */
    public String generate(int length, String word) {
        Seq<String> keys = chain.keys().toSeq();
        String currentWord = word;
        StringBuilder genChain = new StringBuilder(currentWord);

        for (int i = 1; i < length -1; i++) {
            if (chain.containsKey(currentWord)) {
                if (!chain.get(currentWord).getAfter().isEmpty()) {
                    currentWord = chain.get(currentWord).getRandomAfter();
                    genChain.append(' ').append(currentWord);
                }
            } else {
                currentWord = keys.get(rand.random(0, keys.size - 1));
                genChain.append(", ").append(currentWord);
            }
        }

        return genChain.toString();
    }

    /**
     * Generates a string with automatic length based on a specified starting word.
     * 
     * @param word
     *       The starting word.
     * @return
     *       A randomly generated 'sentence'.
     */
    public String generate(String word) {
        String currentWord = word;
        StringBuilder genChain = new StringBuilder(currentWord);

        int i = 0;
        while (i < 50 && chain.containsKey(currentWord)) {
            if (!chain.get(currentWord).getAfter().isEmpty()) {
                currentWord = chain.get(currentWord).getRandomAfter();
                genChain.append(' ').append(currentWord);
                i++;
            }
        }

        return genChain.toString();
    }

    /**
     * Generates a 'sentence' based on a specified length and random starting word.
     * 
     * @param lenght
     *      The number of words to generate
     * @return
     *       A randomly generated 'sentence'.
     */
    public String generate(int lenght) {
        Seq<String> keys = chain.keys().toSeq();
        String word = keys.get(rand.random(0, keys.size));
        return generate(lenght, word);
    }

    /**
     * Generates a string with automatic length based on a random starting word.
     * 
     * @return
     *       A randomly generated 'sentence'.
     */
    public String generate() {
        Seq<String> keys = chain.keys().toSeq();
        String word = keys.get(rand.random(0, keys.size));
        return generate(word);
    }
}