package pl.edu.agh.kis.pz1.model;

/**
 * Enum representing the ranks of cards in a deck, from TWO to ACE.
 * Each rank is associated with an integer value used for comparisons.
 */
public enum Rank {
    TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9), TEN(10),
    JACK(11), QUEEN(12), KING(13), ACE(14);

    private final int value;

    /**
     * Constructor for the Rank enum, assigning a specific integer value to each rank.
     *
     * @param value The integer value associated with the card's rank (e.g., 2 for TWO, 11 for JACK, etc.).
     */
    Rank(int value) {
        this.value = value;
    }

    /**
     * Gets the integer value representing the rank of the card.
     *
     * @return The value associated with the rank (e.g., 2 for TWO, 14 for ACE).
     */
    public int getValue() {
        return value;
    }
}
