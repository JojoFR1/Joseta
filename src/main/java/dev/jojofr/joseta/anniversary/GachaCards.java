package dev.jojofr.joseta.anniversary;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GachaCards {
    public static final int SET_SIZE = 6;
    
    public enum Rarity {
        COMMON("Commune", 25),
        UNCOMMON("Peu Commune", 15),
        RARE("Rare", 7),
        LEGENDARY("Légendaire", 3);
        
        private final String label;
        private final int weight;
        
        Rarity(String label, int weight) {
            this.label = label;
            this.weight = weight;
        }
        
        public String label() { return label; }
        public int weight() { return weight; }
    }
    public record Card(int id, String name, Rarity rarity, String imageUrl) {
        public int set() { return id / SET_SIZE; }
    }
    
    public static final List<Card> CARDS = List.of(
        new Card(0, "Câlin", Rarity.COMMON, "set_1_1.jpg"),
        new Card(1, "Regard qui tue", Rarity.UNCOMMON, "set_1_2.jpg"),
        new Card(2, "Photoshoot", Rarity.LEGENDARY, "set_1_3.jpg"),
        new Card(3, "Meow", Rarity.RARE, "set_1_4.jpg"),
        new Card(4, "Relax", Rarity.COMMON, "set_1_5.jpg"),
        new Card(5, "J'suis la boss", Rarity.COMMON, "set_1_6.jpg"),
        
        new Card(6, "Machine à laver", Rarity.RARE, "set_2_1.jpg"),
        new Card(7, "Baby", Rarity.LEGENDARY, "set_2_2.jpg"),
        new Card(8, "Look at me", Rarity.COMMON, "set_2_3.jpg"),
        new Card(9, "Pas content", Rarity.UNCOMMON, "set_2_4.jpg"),
        new Card(10, "Code", Rarity.COMMON, "set_2_5.jpg"),
        new Card(11, "Catception", Rarity.COMMON, "set_2_6.jpg")
    );
    public static final int SET_COUNT = CARDS.size() / SET_SIZE;
    private static final int TOTAL_WEIGHT = CARDS.stream().mapToInt(card -> card.rarity().weight()).sum();
    
    public static Card byId(int id) {
        return CARDS.get(id);
    }
    
    public static int roll() {
        int roll = ThreadLocalRandom.current().nextInt(TOTAL_WEIGHT);
        for (Card card : CARDS) {
            roll -= card.rarity().weight();
            if (roll < 0) return card.id();
        }
        throw new IllegalStateException("Roll exceeded total weight, this should never happen.");
    }
    
    public static double chanceOf(Card card) {
        return 100.0 * card.rarity().weight() / TOTAL_WEIGHT;
    }
    
    public static int ownedInSet(Collection<Integer> ownedCardIds, int set) {
        int count = 0;
        for (int id : ownedCardIds) {
            if (id / SET_SIZE == set) count++;
        }
        return count;
    }
    
    public static boolean isSetComplete(Collection<Integer> ownedCardIds, int set) {
        return ownedInSet(ownedCardIds, set) == SET_SIZE;
    }
    
    public static boolean anySetComplete(Collection<Integer> ownedCardIds) {
        for (int set = 0; set < SET_COUNT; set++) {
            if (ownedInSet(ownedCardIds, set) == SET_SIZE) return true;
        }
        return false;
    }
}
