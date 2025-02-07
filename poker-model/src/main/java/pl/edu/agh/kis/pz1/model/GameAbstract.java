package pl.edu.agh.kis.pz1.model;

/**
 * Abstract class representing a game.
 * This class serves as a base for different types of games. It contains a unique game identifier (gameID)
 * and an abstract method to add players to the game.
 * Concrete subclasses must implement the {@link #addPlayer(Player)} method.
 */
abstract class GameAbstract {
    protected String gameID;

    /**
     * Adds a new player to the game.
     * This method must be implemented by subclasses to handle adding a player to a specific type of game.
     *
     * @param newPlayer The player to be added to the game.
     */
    public abstract void addPlayer(Player newPlayer);
}
