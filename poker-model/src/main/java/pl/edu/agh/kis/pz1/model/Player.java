package pl.edu.agh.kis.pz1.model;

/**
 * Represents a player in a poker game.
 * A player has an ID, a hand of cards, a stack of chips, and some states like whether they've folded or are ready.
 */
public class Player {
    private String playerId;
    private Hand playerHand;
    private int stack;
    private boolean folded;
    private boolean ready;
    private int currentBetInThisRound;

    /**
     * Constructor for creating a player with a specified ID and initial stack of chips.
     *
     * @param playerId     The unique identifier of the player.
     * @param initialStack The initial number of chips the player has.
     */
    public Player(String playerId, int initialStack) {
        this.playerId = playerId;
        this.stack = initialStack;
        this.folded = false;
        this.ready = false;
        this.currentBetInThisRound = 0; // Na starcie rundy gracz nie postawił nic
    }

    /**
     * Constructor for creating a player with a specified ID, hand of cards, and initial stack of chips.
     *
     * @param playerId     The unique identifier of the player.
     * @param hand         The player's hand of cards.
     * @param initialStack The initial number of chips the player has.
     */
    public Player(String playerId, Hand hand, int initialStack) {
        this.playerId = playerId;
        this.playerHand = hand;
        this.stack = initialStack;
        this.folded = false;
        this.ready = false;
        this.currentBetInThisRound = 0;
    }

    /**
     * Returns the hand rank of the player.
     *
     * @return The HandRank object that represents the strength of the player's hand.
     */
    public HandRank getHandRank() {
        return playerHand.getHandRank();
    }

    /**
     * Sets the hand rank of the player.
     *
     * @param handRank The HandRank object representing the strength of the player's hand.
     */
    public void setHandRank(HandRank handRank) {
        this.playerHand.setHandRank(handRank);
    }

    /**
     * Checks if the player is ready to play.
     *
     * @return true if the player is ready, false otherwise.
     */
    public boolean isReady() {
        return ready;
    }

    /**
     * Sets the player's stack to a new value.
     *
     * @param stack The new stack value.
     */
    public void setStack(int stack) {
        this.stack = stack;
    }

    /**
     * Sets the player's readiness status.
     *
     * @param ready The new readiness status (true if ready, false otherwise).
     */
    public void setReady(boolean ready) {
        this.ready = ready;
    }

    /**
     * Sets the player's folded status.
     *
     * @param folded The new folded status (true if the player has folded, false otherwise).
     */
    public void setFolded(boolean folded) {
        this.folded = folded;
    }

    /**
     * Gets the player's unique identifier.
     *
     * @return The player's ID.
     */
    public String getPlayerId() {
        return playerId;
    }

    /**
     * Gets the player's hand of cards.
     *
     * @return The player's hand as a Hand object.
     */
    public Hand getPlayerHand() {
        return playerHand;
    }

    /**
     * Sets the player's hand of cards.
     *
     * @param hand The Hand object representing the player's cards.
     */
    public void setPlayerHand(Hand hand){
        this.playerHand = hand;
    }

    /**
     * Gets the player's current stack (remaining chips).
     *
     * @return The player's current stack.
     */
    public int getStack() {
        return stack;
    }

    /**
     * Method to place a bet. Deducts the specified amount from the player's stack.
     *
     * @param amount The amount to bet.
     * @return true if the player has enough chips, false otherwise.
     */
    public boolean placeBet(int amount) {
        if (amount > stack) {
            return false; // gracz nie ma wystarczających środków
        }
        stack -= amount;
        return true;
    }

    /**
     * Adds winnings to the player's stack.
     *
     * @param amount The amount to add to the stack.
     */
    public void addWinnings(int amount) {
        stack += amount;
    }

    /**
     * Sets the player's unique identifier.
     *
     * @param playerId The player's unique ID (e.g., name or identifier).
     */
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    /**
     * Folds the player's hand, marking them as having quit the current round.
     */
    public void fold() {
        this.folded = true;
    }

    /**
     * Checks if the player has folded (quit the round).
     *
     * @return true if the player has folded, false otherwise.
     */
    public boolean isFolded() {
        return folded;
    }


    /**
     * Gets the amount the player has bet in the current round.
     *
     * @return The current bet amount for this round.
     */
    public int getCurrentBetInThisRound() {
        return currentBetInThisRound;
    }

    /**
     * Sets the amount the player has bet in the current round.
     *
     * @param currentBetInThisRound The amount to set as the player's current bet.
     */
    public void setCurrentBetInThisRound(int currentBetInThisRound) {
        this.currentBetInThisRound = currentBetInThisRound;
    }
}
