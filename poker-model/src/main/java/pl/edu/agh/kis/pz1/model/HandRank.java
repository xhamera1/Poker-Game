package pl.edu.agh.kis.pz1.model;

import java.util.List;
import java.util.Objects;

/**
 * Represents a hand ranking in poker, consisting of the hand's type (e.g., STRAIGHT_FLUSH)
 * and a list of kickers (additional cards used for comparison in case of ties).
 */
public class HandRank {
    private final HandType type; // np. STRAIGHT_FLUSH
    private final List<Rank> kickers; // np. [KING, QUEEN, TEN]

    /**
     * Constructs a HandRank object with the given hand type and kickers.
     *
     * @param type    The type of the hand (e.g., FULL_HOUSE, STRAIGHT).
     * @param kickers A list of ranks representing the kickers (tiebreaker cards).
     */
    public HandRank(HandType type, List<Rank> kickers){
        this.type = type;
        this.kickers = kickers;
    }

    /**
     * Gets the type of the hand (e.g., FULL_HOUSE, STRAIGHT).
     *
     * @return The type of the hand.
     */
    public HandType getType() {
        return type;
    }

    /**
     * Gets the list of kickers for the hand. Kickers are used to break ties between two hands of the same type.
     *
     * @return A list of ranks representing the kickers of the hand.
     */
    public List<Rank> getKickers() {
        return kickers;
    }

    /**
     * Compares this HandRank object to another object for equality.
     * Two HandRank objects are considered equal if their hand type and kickers are the same.
     *
     * @param o The object to compare with.
     * @return True if the object is equal to this HandRank, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HandRank handRank = (HandRank) o;
        return type == handRank.type && kickers.equals(handRank.kickers);
    }

    /**
     * Returns a hash code value for this HandRank object.
     * The hash code is computed based on the hand type and kickers.
     *
     * @return The hash code for this HandRank object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, kickers);
    }
}