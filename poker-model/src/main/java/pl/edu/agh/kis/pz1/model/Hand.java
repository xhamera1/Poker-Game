package pl.edu.agh.kis.pz1.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a player's hand in the game.
 * A hand consists of a list of cards and is associated with a hand rank (the strength of the hand).
 * This class provides methods to add or remove cards, exchange cards, and retrieve the hand's cards and rank.
 */
public class Hand {
    private List<Card> cards;
    private HandRank handRank; // Attribute to store the hand's rank (strength of the hand)

    /**
     * Constructs an empty hand.
     * Initializes the hand with no cards.
     */
    public Hand() {
        this.cards = new ArrayList<>();
    }

    /**
     * Constructs a hand with an initial set of cards.
     *
     * @param initialCards The list of cards to initialize the hand with.
     */
    public Hand(List<Card> initialCards) {
        this.cards = new ArrayList<>(initialCards);
    }

    /**
     * Adds a card to the hand.
     *
     * @param card The card to be added to the hand.
     */
    public void addCard(Card card) {
        cards.add(card);
    }

    /**
     * Removes a card from the hand at the specified index.
     * Returns the removed card, or null if the index is invalid.
     *
     * @param index The index of the card to be removed.
     * @return The removed card, or null if the index is invalid.
     */
    public Card removeCard(int index) {
        if (index < 0 || index >= cards.size()) {
            return null;
        }
        return cards.remove(index);
    }

    /**
     * Returns a new list containing the cards in the hand.
     * The returned list is a copy to ensure the original hand is not modified.
     *
     * @return A list of cards in the hand.
     */
    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }

    /**
     * Returns the number of cards in the hand.
     *
     * @return The size of the hand.
     */
    public int size() {
        return cards.size();
    }

    /**
     * Exchanges a card in the hand with a new random card from the provided deck.
     * The card at the specified index is removed and replaced with a new card.
     *
     * @param index The index of the card to be exchanged.
     * @param deck The deck to draw the new card from.
     * @return true if the exchange was successful, false if the index is invalid or the deck is empty.
     */
    public boolean exchangeCard(int index, Deck deck) {
        if (index < 0 || index >= cards.size()) {
            return false;
        }
        Card newCard = deck.getRandomCard();
        if (newCard == null) {
            return false;
        }
        cards.remove(index);
        cards.add(newCard);
        return true;
    }

    /**
     * Returns the current hand rank (the strength of the hand).
     *
     * @return The hand's rank.
     */
    public HandRank getHandRank() {
        return handRank;
    }

    /**
     * Sets the hand's rank (the strength of the hand).
     *
     * @param handRank The rank to set for the hand.
     */
    public void setHandRank(HandRank handRank) {
        this.handRank = handRank;
    }
}
