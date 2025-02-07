package pl.edu.agh.kis.pz1.model;

import java.util.Objects;

/**
 * Class representing a playing card.
 * This class is immutable, meaning that once the card is created, its rank and suit cannot be changed.
 * It provides methods to access the card's rank and suit, and also overrides the equals and hashCode methods.
 * The rank and suit are represented by enumerations 'Rank' and 'Suit', respectively.
 */
public class Card {
    private final Rank rank;
    private final Suit suit;

    /**
     * Constructs a card with the specified rank and suit.
     *
     * @param r the rank of the card
     * @param s the suit of the card
     */
    public Card(Rank r, Suit s){
        this.rank = r;
        this.suit = s;
    }

    /**
     * Returns the rank of this card.
     *
     * @return the rank of the card
     */
    public Rank getRank(){
        return this.rank;
    }

    /**
     * Returns the suit of this card.
     *
     * @return the suit of the card
     */
    public Suit getSuit(){
        return this.suit;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * Two cards are considered equal if they have the same rank and suit.
     *
     * @param o the reference object with which to compare
     * @return true if this card is the same as the object argument; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return rank == card.rank && suit == card.suit;
    }

    /**
     * Returns a hash code value for the card.
     * This is based on the card's rank and suit, ensuring that cards with the same rank and suit have the same hash code.
     *
     * @return a hash code value for this card
     */
    @Override
    public int hashCode() {
        return Objects.hash(rank, suit);
    }

    /**
     * Returns a string representation of the card, in the format "rank of suit".
     *
     * @return a string representation of the card
     */
    @Override
    public String toString() {
        return rank + " of " + suit;
    }
}
