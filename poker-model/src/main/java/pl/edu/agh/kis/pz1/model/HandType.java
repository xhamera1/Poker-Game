package pl.edu.agh.kis.pz1.model;

/**
 * Enum representing the different types of poker hands, ordered by their rank (highest to lowest).
 * Each hand type is associated with a numerical value for easy comparison.
 */
public enum HandType {
    RoyalFlush(10),     // poker krolewski (kolor i pod rzad 10,jupek,dama,krol,as)
    StraightFlush(9),   // poker (strit w koloerz - kolor i pod rzad 5 kart)
    FourOfAKind(8),     // kareta ( 4 takie same karty)
    FullHouse(7),       // ful (trojka i para)
    Flush(6),           // kolor ( taki sam suit-kolor)
    Straight(5),        // strit ( 5 kart pod rzad)
    ThreeOfAKind(4),    // trojka
    TwoPair(3),         // dwie pary
    OnePair(2),         // para
    HighCard(1);        // wysoka karta


    private final int value;

    /**
     * Constructs a HandType with a specific value.
     *
     * @param value The numerical value representing the rank of the hand type.
     */
    HandType(int value) {
        this.value = value;
    }

    /**
     * Returns the value associated with this hand type.
     * The higher the value, the stronger the hand.
     *
     * @return The value of the hand type.
     */
    public int getValue() {
        return value;
    }
}
