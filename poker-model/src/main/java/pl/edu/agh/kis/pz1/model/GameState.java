package pl.edu.agh.kis.pz1.model;

/**
 * Enum representing the different states of a game.
 * The game progresses through these states during its lifecycle.
 * Each state reflects a specific phase or action within the game.
 */
public enum GameState {
    WAITING_FOR_PLAYERS,   // Waiting for a minimum number of players to join and be ready
    POST_BLINDS,           // Collecting blinds (initial forced bets)
    DEALING,               // Dealing the cards to players
    FIRST_BETTING_ROUND,   // First betting round after cards are dealt
    EXCHANGE_PHASE,        // Phase where players can exchange cards
    SECOND_BETTING_ROUND,  // Second betting round after the exchange
    SHOWDOWN,              // Showdown phase where players reveal their cards and a winner is determined
    FINISHED,              // End of the round
    WAITING_FOR_NEXT_ROUND, // Waiting for the next round, players can join, leave, or confirm readiness
    GAME_OVER;             // Game is over, no more rounds can be played
}
