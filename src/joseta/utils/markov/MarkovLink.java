package joseta.utils.markov;

import arc.math.*;
import arc.struct.*;

/**
 * A data structure representing a list of words that can come after a keyword. </br></br>
 * 
 * Translated F# to Java from: <a href=https://github.com/dweslynch/MarkovNextGen/blob/9e842bf5a1e5f15edbcec4b83a2b7bcffa2a54a7/MarkovNextGen/MarkovNextGen/Link.fs>MarkovNextGen/Link.fs</a>
 */

public class MarkovLink {
    private Rand rand = new Rand();
    private Seq<String> after;
    
    /**
     * Constructor that initializes the internal list based the contents of on an {@link Seq} of {@link String}s.
     *
     * @param list
     *        The {@link Seq} to initialize from.
     */
    public MarkovLink(Seq<String> list) {
        this.after = list;
    }

    /**
     * Constructor that initializes the internal list with a single {@link String}.
     *
     * @param string
     *        The {@link String} to initialize with.
     */
    public MarkovLink(String string) {
        this(Seq.with(string));
    }

    /**
     * Constructor that return an empty {@link MarkovLink}.
     */
    public MarkovLink() {
        this(Seq.with());
    }


    public Seq<String> getAfter() {
        return after;
    }
    public void setAfter(Seq<String> after) {
        this.after = after;
    }

    /**
     * Adds words to the current list.
     *
     * @param items
     *        The {@link Seq} of {@link String}s to add.
     */
    public void addAfter(Seq<String> items) {
        after.addAll(items);
    }

    /**
     * Adds a single {@link String} to the current list.
     *
     * @param item
     *        The {@link String}s to add.
     */
    public void addAfter(String item) {
        after.add(item);
    }

    public String getRandomAfter() {
        if (after.isEmpty()) return "";

        return after.get(rand.random(0, after.size - 1));
    }
}
