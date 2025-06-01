package joseta.utils.markov;

import joseta.*;

import arc.files.*;
import arc.math.*;
import arc.struct.*;
import arc.util.serialization.*;

/**
 * Translated F# to Java from: <a href=https://github.com/dweslynch/MarkovNextGen/blob/9e842bf5a1e5f15edbcec4b83a2b7bcffa2a54a7/MarkovNextGen/MarkovNextGen/Utilities.fs>MarkovNextGen/Utilities.fs</a>
 */
public class MarkovUtilities {
    private static final Rand rand = new Rand();

    /**
     * Static method for reading a chain from a file.
     * 
     * @param filename
     *       The name of the file containing the chain. 
     * @return
     *      An {@link ObjectMap} containing the chain, or an empty one if the file does not exist.
     */
    public static ObjectMap<String, MarkovLink> readChain(String filename) {
        Fi file = new Fi(filename);

        if (file.exists()) {
            Json json = new Json();
            return json.fromJson(new ObjectMap<String, MarkovLink>().getClass(), file);
        } else {
            JosetaBot.logger.warn("No PDO detected. Returning blank Dictionary<String, MarkovLink>.");
            return new ObjectMap<String, MarkovLink>();
        }
    }

    /**
     * Static method for writing a chain to a file.
     * 
     * @param filename
     *       The file name.
     * @param chain
     *       The chain to serialize.
     */
    public static void writeChain(String filename, ObjectMap<String, MarkovLink> chain) {
        Fi file = new Fi(filename);
        Json json = new Json();
        file.writeString(json.toJson(chain), false);
    }

    /**
     * Static method for adding a link to a chain.
     * 
     * @param chain
     *        The chain to add to.
     * @param word
     *        The key word.
     * @param link
     *        The list of potential words after.
     * @return
     *      A new {@link ObjectMap} containing the chain with the new link added.
     */
    public static ObjectMap<String, MarkovLink> addLink(ObjectMap<String, MarkovLink> chain, String word, Seq<String> link) {
        ObjectMap<String, MarkovLink> newChain = new ObjectMap<>(chain);

        if (word != null && !word.isEmpty()) {
            if (newChain.containsKey(word)) newChain.get(word).addAfter(link);
            else newChain.put(word, new MarkovLink(link));
        }

        return newChain;
    }

   /**
     * Static method for removing a link from a chain.
     * 
     * @param chain
     *        The chain to remove from.
     * @param word
     *        The key word to delete.
     * @return
     *       A new {@link ObjectMap} containing the chain without the specified word.
     */
    public static ObjectMap<String, MarkovLink> removeLink(ObjectMap<String, MarkovLink> chain, String word) {
        ObjectMap<String, MarkovLink> newChain = new ObjectMap<>(chain);
        
        if (newChain.containsKey(word)) newChain.remove(word);

        return newChain;
    }
    
    /**
     * Static method for removing all occurences of a word from a chain.
     * 
     * @param chain
     *        The chain to remove from.
     * @param word
     *        The word to delete.
     * @return
     *       A new {@link ObjectMap} containing the chain with the word removed.
     */
    public static ObjectMap<String, MarkovLink> removeAll(ObjectMap<String, MarkovLink> chain, String word) {
        ObjectMap<String, MarkovLink> newChain = new ObjectMap<>(chain);

        if (newChain.containsKey(word)) newChain.remove(word);
        for (var kvp : newChain) {
            if (kvp.value.getAfter().contains(word))
                kvp.value.getAfter().removeAll(w -> w.equals(word));
        }

        return newChain;
    }

    /**
     * Static method for merging two chains into a new one.
     * 
     * @param from
     *        The first chain.
     * @param to
     *       The second chain.
     * @return
     *        A new {@link ObjectMap} containing the merged chains.
     */
    public static ObjectMap<String, MarkovLink> merge(ObjectMap<String, MarkovLink> from, ObjectMap<String, MarkovLink> to) {
        ObjectMap<String, MarkovLink> target = new ObjectMap<>(to);

        for (var kvp : from) {
            // The entry exist, merge
            if (target.containsKey(kvp.key)) target.get(kvp.key).addAfter(kvp.value.getAfter());
            else { // Create a new entry
                MarkovLink link = new MarkovLink(kvp.value.getAfter());
                target.put(kvp.key, link);
            }
        }

        return target;
    }

    /**
     * Static method for printing a chain to console with nice formatting.
     * 
     * @param chain
     *        The chain to print.
     */
    public static void printChain(ObjectMap<String, MarkovLink> chain) {
        for (var kvp : chain) {
            StringBuilder sb = new StringBuilder(kvp.key);
            for (String after : kvp.value.getAfter()) {
                sb.append('\t').append(after);
            }

            System.out.println(sb.toString());
        }
    }

    /**
     * Static generation of markov string based on specified length and starting word.
     * 
     * @param chain
     *        The chain to use for generation.
     * @param length
     *        The number of words in the generated string.
     * @param word
     *        The starting word.
     * @return
     *        A new {@link ObjectMap} containing the generated Markov chain text.
     */
    public static String generate(ObjectMap<String, MarkovLink> chain, int length, String word) {
        Seq<String> keys = chain.keys().toSeq();
        String currentWord = word;
        StringBuilder genChain = new StringBuilder(currentWord);

        for (int i = 1; i < length - 1; i++) {
            if (chain.containsKey(currentWord)) {
                if (!chain.get(currentWord).getAfter().isEmpty()) {
                    currentWord = chain.get(currentWord).getRandomAfter();
                    genChain.append(' ').append(currentWord);
                }
            } else {
                currentWord = keys.get(rand.random(0, keys.size)); // New words
                genChain.append(", ").append(currentWord); // Add a coma since it's a new starting point
            }
        }

        return genChain.toString();
    }

    /**
     * Static generation of a markov string with automatic length.
     * 
     * @param chain
     *        The chain to use for generation.
     * @param word
     *        The starting word.
     * @return
     *        A new {@link ObjectMap} containing the generated Markov chain text.
     */
    public static String autoGenerate(ObjectMap<String, MarkovLink> chain, String word) {
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
     * Merges two files into one chain.
     *
     * @param from
     *       The name of the first file.
     * @param to
     *      The name of the second file.
     * @return
     *      A new {@link ObjectMap} containing the merged chains. 
     */
    public static ObjectMap<String, MarkovLink> mergeFrom(String from, String to) {
        ObjectMap<String, MarkovLink> chainFrom = readChain(from);
        ObjectMap<String, MarkovLink> chainTo = readChain(to);

        return merge(chainFrom, chainTo);
    }

    /**
     * Merges one chain file into another.
     *
     * @param from
     *       The name of the file to merge from.
     * @param to
     *      The name of the file to merge to.
     */
    public static void mergeTo(String from, String to) {
        ObjectMap<String, MarkovLink> merged = mergeFrom(from, to);
        writeChain(to, merged);
    }
}
