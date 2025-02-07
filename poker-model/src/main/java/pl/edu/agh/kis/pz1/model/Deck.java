package pl.edu.agh.kis.pz1.model;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class representing a deck of cards.
 * Provides methods to create a deck, add cards, shuffle the deck,
 * and retrieve a random card.
 */
public class Deck {
    private final SecureRandom rand = new SecureRandom();
    private ArrayList<Card> cardDeck;

    /**
     * Constructs an empty deck.
     */
    public Deck(){
        this.cardDeck = new ArrayList<>();
    }

    /**
     * Returns the current list of cards in the deck.
     *
     * @return An ArrayList of cards representing the deck.
     */
    public List<Card> getDeck(){
        return this.cardDeck;
    }


    /**
     * Adds a card to the deck.
     *
     * @param c The card to be added to the deck.
     */
    public void addCard(Card c){
        this.cardDeck.add(c);
    }





    /**
     * Retrieves a random card from the deck and removes it.
     * If the deck is empty, returns null.
     *
     * @return A randomly selected card from the deck, or null if the deck is empty.
     */
    public Card getRandomCard() {
        if (cardDeck.isEmpty()) {
            throw new IllegalStateException("The deck is empty!");
        }
        int randIndex = rand.nextInt(cardDeck.size());
        return cardDeck.remove(randIndex);
    }


    /**
     *  Method checks if the card if is deck,
     *  if deck contains the card it returns false
     *  if it doesn't, card is added and method returns true.
     **/
    public boolean addCartToDeck(Card card){
        if(cardDeck.contains(card)){
            return false;
        }
        cardDeck.add(card);
        return true;
    }

    /**
     * Fills the deck with a full set of 52 standard playing cards.
     * The deck will contain one card for each combination of pl.edu.agh.kis.pz1.game.rank and pl.edu.agh.kis.pz1.game.suit.
     *
     * @return The newly populated deck.
     */
    public List<Card> fabryki(){
        for (Rank r : Rank.values()){
            for (Suit s : Suit.values()){
                Card card = new Card(r, s);
                cardDeck.add(card);
            }
        }
        return cardDeck;
    }


    /**
     * Shuffles the provided deck.
     *
     * @param d The deck to be shuffled.
     * @return The shuffled deck.
     */
    public List<Card> shuffle(List<Card> d){
        Collections.shuffle(d);
        return d;
    }

    /**
     * Shuffles the current deck. If the deck is empty, it doesn't do anything
     *
     * @return The shuffled deck or empty deck if it was empty.
     */
    public List<Card> shuffle(){
        if (this.cardDeck.isEmpty()){
            return this.cardDeck;
        }
        Collections.shuffle(this.cardDeck);
        return this.cardDeck;
    }


    /**
     * Checks if deck is empty
     * @return true if deck is empty, false if it is not.
     */
    public boolean isEmpty() {
        return cardDeck.isEmpty();
    }



}