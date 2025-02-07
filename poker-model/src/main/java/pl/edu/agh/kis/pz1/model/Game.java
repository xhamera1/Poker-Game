package pl.edu.agh.kis.pz1.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a poker game with a list of players, a deck of cards, and game state.
 * It handles game initialization, player addition, game state transitions,
 * and game rules such as blinds and pot management.
 */
public class Game extends GameAbstract{
    private List<Player> players;
    private Deck deck;
    private GameState gameState;
    private int currentPlayerIndex;
    private HandCompare handCompare;

    private int minPlayers = 2;
    private int maxPlayers = 4;
    private boolean started = false;

    private int dealerIndex = 0;
    private int smallBlindIndex;
    private int bigBlindIndex;
    private int smallBlindAmount = 20;
    private int bigBlindAmount = 40;
    private int pot = 0;
    private int currentBet = 0; // aktualna stawka do sprawdzenia
    private int playersRemaining; // ile graczy nie spasowało


    /**
     * Constructor to initialize the game with the specified game ID.
     * This method also initializes the deck and shuffles it.
     *
     * @param gameID The unique identifier for the game.
     */
    public Game(String gameID) {
        this.gameID = gameID;
        this.players = new ArrayList<>();
        this.deck = new Deck();
        deck.fabryki();
        deck.shuffle();
        this.gameState = GameState.WAITING_FOR_PLAYERS;
        this.currentPlayerIndex = 0;
        this.handCompare = new HandCompare();
    }


    /**
     * Adds a new player to the game. The game must be in the "waiting for players" state,
     * and the player must not exceed the maximum number of players.
     *
     * @param newPlayer The player to add to the game.
     * @throws IllegalStateException if the game is not in the "waiting for players" state,
     *                                or if the maximum player count is reached.
     */
    @Override
    public void addPlayer(Player newPlayer) {
        if (gameState != GameState.WAITING_FOR_PLAYERS) {
            throw new IllegalStateException("Cannot join: Game is not in a joinable state");
        }
        if (players.size() >= maxPlayers) {
            throw new IllegalStateException("Max players reached");
        }
        for (Player player : players) {
            if (Objects.equals(player.getPlayerId(), newPlayer.getPlayerId())) {
                throw new IllegalStateException("ERROR: Player with that ID is already in this game");
            }
        }
        players.add(newPlayer);
    }

    /**
     * Removes a player from the game based on the player's unique ID.
     * If no players remain after the removal, the game will be reset.
     *
     * @param playerId The ID of the player to remove from the game.
     */
    public void removePlayer(String playerId) {
        players.removeIf(p -> Objects.equals(p.getPlayerId(), playerId));
        if (players.isEmpty()) {
            resetGame();
        }
    }

    /**
     * Marks a player as ready to start the game. If all players are ready,
     * the game will automatically start.
     *
     * @param playerId The ID of the player to mark as ready.
     * @throws IllegalStateException if there is no player with the given ID.
     */
    public void setPlayerReady(String playerId) {
        Player p;
        try {
            p = getPlayerById(playerId);
        } catch (IllegalStateException e){
            throw new IllegalStateException("ERROR: NO PLAYER WITH ID : " + playerId + " IN THIS GAME");
        }
        p.setReady(true);
        if (canStartGame()) {
            startGameCycle();
        }
    }

    /**
     * Returns the number of players who are marked as ready.
     *
     * @return The number of ready players.
     */
    public int getNumberOfReadyPlayers(){
        int number = 0;
        for (Player player : players){
            if (player.isReady()){
                number++;
            }
        }
        return number;
    }

    /**
     * Checks whether the game can start. The game can start only if there are at least
     * the minimum required number of players, and all players are marked as ready.
     *
     * @return True if the game can start, false otherwise.
     */
    public boolean canStartGame() {
        if (players.size() < minPlayers) return false;
        for (Player p : players) {
            if (!p.isReady()) return false;
        }
        return true;
    }


    /**
     * Starts the game cycle, initializing the necessary steps for a new round of the game.
     * This includes setting up the dealer, small blind, and big blind players, resetting the game state,
     * and starting the initial stages of the round: posting blinds, dealing cards, and starting the first betting round.
     */
    public void startGameCycle() {
        started = true;

        dealerIndex = dealerIndex % players.size();
        smallBlindIndex = (dealerIndex + 1) % players.size();
        bigBlindIndex = (dealerIndex + 2) % players.size();

        pot = 0;
        currentBet = 0;
        resetPlayersForNewRound();

        gameState = GameState.POST_BLINDS;
        postBlinds();

        gameState = GameState.DEALING;
        dealCards();

        gameState = GameState.FIRST_BETTING_ROUND;
        startBettingRound();
    }


    /**
     * Resets the players' states for a new round of the game. All players are unmarked as folded, unready,
     * and their current bets for the round are reset.
     */
    private void resetPlayersForNewRound() {
        for (Player p : players) {
            p.setFolded(false);
            p.setReady(false);
            p.setCurrentBetInThisRound(0);
        }
        playersRemaining = players.size();
    }

    /**
     * Posts the blinds for the game, deducting the small blind and big blind amounts from the respective players' stacks.
     */
    private void postBlinds() {
        Player sb = players.get(smallBlindIndex);
        Player bb = players.get(bigBlindIndex);

        postBet(sb, smallBlindAmount);
        postBet(bb, bigBlindAmount);

        currentBet = bigBlindAmount;
    }

    /**
     * Retrieves information about the current small blind and big blind players,
     * as well as the amounts that have been placed for each.
     *
     * @return A string with information about the small blind and big blind.
     */
    public String getInfoBlinds(){
        Player sb = players.get(smallBlindIndex);
        Player bb = players.get(bigBlindIndex);
        return "Small blind: " + sb.getPlayerId() + ", Big blind: " + bb.getPlayerId()
                + ". 20$ and 40$ have been placed";
    }

    /**
     * Handles the bet placement for a player in the current round.
     * The player's stack is reduced by the bet amount, and the bet is added to the pot.
     * If the player doesn't have enough chips, the bet is adjusted to their available stack.
     *
     * @param p The player placing the bet.
     * @param amount The amount of the bet.
     */
    private void postBet(Player p, int amount) {
        if (p.getStack() < amount) {
            amount = p.getStack();
        }
        p.placeBet(amount);
        p.setCurrentBetInThisRound(amount);
        pot += amount;
    }

    /**
     * Deals 5 cards to each player by drawing from a shuffled deck.
     * The deck is shuffled before cards are dealt to ensure randomness.
     * Each player receives 5 cards, which are assigned to their hand.
     */
    public void dealCards() {
        deck.shuffle();
        for (Player p : players) {
            Hand hand = new Hand();
            for (int i = 0; i < 5; i++) {
                hand.addCard(deck.getRandomCard());
            }
            p.setPlayerHand(hand);
        }
    }

    /**
     * Retrieves the hand of a specific player, displaying the cards they have been dealt.
     * If no cards have been dealt yet, it returns a message indicating that.
     *
     * @param playerID The ID of the player whose hand is being requested.
     * @return A string describing the player's hand, or a message indicating no cards have been dealt.
     */
    public String getPlayerHand(String playerID) {
        Player player;
        try {
            player = getPlayerById(playerID);
        } catch (IllegalStateException ex) {
            throw new IllegalStateException("ERROR: Player with ID " + playerID + " not found.");
        }
        if (player.getPlayerHand() == null) {
            return "No cards dealt yet.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("CARDS: ");
        for (Card card : player.getPlayerHand().getCards()) {
            sb.append(card.toString()).append(", ");
        }
        return sb.toString();
    }

    /**
     * Starts a new round of betting by setting the current player to the next in the sequence,
     * skipping players who have folded.
     */
    private void startBettingRound() {
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        } while (players.get(currentPlayerIndex).isFolded());
    }

    /**
     * Allows a player to fold during the betting round.
     * The player will not participate further in the current round, and the remaining number of active players is decreased.
     * If only one player remains, the game state is set to GAME_OVER.
     *
     * @param playerId The ID of the player who wishes to fold.
     */
    public void playerFold(String playerId) {
        Player p = getPlayerById(playerId);
        if (p.isFolded()) {
            throw new IllegalStateException("Player already folded");
        }
        p.setFolded(true);
        playersRemaining--;

        if (playersRemaining == 1) {
            gameState = GameState.GAME_OVER;
        } else {
            goToNextPlayerBetting();
        }
    }


    public void playerCall(String playerId) {
        validatePlayerAction(playerId);
        Player p = getPlayerById(playerId);
        int toCall = currentBet - p.getCurrentBetInThisRound();
        if (toCall > p.getStack()) {
            toCall = p.getStack();
        }
        p.placeBet(toCall);
        p.setCurrentBetInThisRound(p.getCurrentBetInThisRound() + toCall);
        pot += toCall;
        goToNextPlayerBetting();
    }


    public void playerCheck(String playerId) {
        validatePlayerAction(playerId);
        Player p = getPlayerById(playerId);
        if (p.getCurrentBetInThisRound() < currentBet) {
            throw new IllegalStateException("Cannot check, must call or fold.");  // nie mozna dac check jak w obecnej rundzie istneieje jzu stawka
        }
        goToNextPlayerBetting();
    }


    public void playerRaise(String playerId, int amount) {
        validatePlayerAction(playerId);
        Player p = getPlayerById(playerId);
        int toCall = currentBet - p.getCurrentBetInThisRound() + amount;
        if (toCall > p.getStack()) {
            throw new IllegalStateException("Not enough chips to raise by " + amount);
        }
        p.placeBet(toCall);
        p.setCurrentBetInThisRound(p.getCurrentBetInThisRound() + toCall);
        pot += toCall;
        currentBet = p.getCurrentBetInThisRound();
        goToNextPlayerBetting();
    }

    private void goToNextPlayerBetting() {
        int startIndex = currentPlayerIndex;
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            Player p = players.get(currentPlayerIndex);

            if (!p.isFolded() && p.getStack() > 0) {
                break; // Znaleziono odpowiedniego gracza
            }
        } while (currentPlayerIndex != startIndex);

        if (bettingRoundFinished()) {
            finishBettingRound();
        }
    }

    private void validatePlayerAction(String playerId) {
        if (!players.get(currentPlayerIndex).getPlayerId().equals(playerId)) {
            throw new IllegalStateException("It's not this player's turn.");
        }
        if (gameState != GameState.FIRST_BETTING_ROUND && gameState != GameState.SECOND_BETTING_ROUND) {
            throw new IllegalStateException("Action not allowed in the current game state.");
        }
    }

    private boolean bettingRoundFinished() {
        if (playersRemaining == 1) return true;

        if (currentBet == 0) {
            return false;
        }

        for (Player p : players) {
            if (!p.isFolded() && p.getCurrentBetInThisRound() < currentBet) {
                return false;
            }
        }
        return true;
    }

    void finishBettingRound() {
        if (playersRemaining == 1) {
            gameState = GameState.FINISHED;
            awardPotToLastStanding();
        } else {
            switch (gameState) {
                case FIRST_BETTING_ROUND:
                    gameState = GameState.EXCHANGE_PHASE;
                    currentPlayerIndex = -1;
                    do {
                        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
                    } while (players.get(currentPlayerIndex).isFolded());
                    break;
                case SECOND_BETTING_ROUND:
                    gameState = GameState.SHOWDOWN;
                    determineWinner();
                    break;
                default:
                    break;
            }
        }
    }

    public boolean processBettingRound() {
        if (bettingRoundFinished()) {
            finishBettingRound();
            return true;
        }
        return false;
    }

    public boolean isBettingRoundFinished() {
        if (playersRemaining == 1) {
            return true; // Tylko jeden gracz, koniec gry.
        }

        for (Player p : players) {
            if (!p.isFolded() && p.getCurrentBetInThisRound() < currentBet) {
                return false; // Runda betowania wciąż trwa.
            }
        }
        return true;
    }

    public void exchangeCards(String playerId, List<Integer> cardIndices) {
        if (gameState != GameState.EXCHANGE_PHASE) {
            throw new IllegalStateException("Not exchange phase");
        }

        while (players.get(currentPlayerIndex).isFolded()) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            if (currentPlayerIndex == 0) {
                finishExchangePhase();
                return;
            }
        }

        Player currentPlayer = players.get(currentPlayerIndex);
        if (!Objects.equals(currentPlayer.getPlayerId(), playerId)) {
            throw new IllegalStateException("Not this player's turn");
        }
        if (cardIndices.size() > 4) {
            throw new IllegalStateException("Cannot exchange more than 4 cards");
        }
        for (int index : cardIndices) {
            if (index < 0 || index >= 5) {
                throw new IllegalStateException("Invalid card index: " + index);
            }
        }
        Hand hand = currentPlayer.getPlayerHand();
        cardIndices.sort(Collections.reverseOrder());
        for (int index : cardIndices) {
            hand.removeCard(index);
            hand.addCard(deck.getRandomCard());
        }
        currentPlayerIndex++;
        if (currentPlayerIndex >= players.size()) {
            finishExchangePhase();
        }
    }

    public void finishExchangePhase() {
        gameState = GameState.SECOND_BETTING_ROUND;
        currentBet = 0;

        for (Player p : players) {
            if (!p.isFolded()) {
                p.setCurrentBetInThisRound(0);
            }
        }

        startBettingRound();
    }

    public void determineWinner() {
        Player winner = null;
        HandRank bestRank = null;
        for (Player p : players) {
            if (!p.isFolded()) {
                HandRank hr = handCompare.checkHand(p.getPlayerHand().getCards());
                if (bestRank == null || handCompare.compareHandRank(hr, bestRank) > 0) {
                    bestRank = hr;
                    winner = p;
                }
            }
        }
        if (winner != null) {
            winner.addWinnings(pot);
        }
        gameState = GameState.GAME_OVER;
    }

    public void resetGame() {
        resetGameState();
    }

    public void resetAfterGame() {
        resetGameState();
    }

    private void resetGameState() {
        this.gameState = GameState.WAITING_FOR_PLAYERS;
        this.deck = new Deck();
        deck.fabryki();
        deck.shuffle();
        this.currentPlayerIndex = 0;
        this.pot = 0;
        this.currentBet = 0;
        this.players.forEach(player -> {
            player.setReady(false);
            player.setFolded(false);
            player.setCurrentBetInThisRound(0);
            player.setPlayerHand(null);
        });
        this.started = false;
    }

    public Player getWinner() {
        Player winner = null;
        HandRank bestRank = null;
        for (Player p : players) {
            if (!p.isFolded()) {
                HandRank hr = handCompare.checkHand(p.getPlayerHand().getCards());
                if (bestRank == null || handCompare.compareHandRank(hr, bestRank) > 0) {
                    bestRank = hr;
                    winner = p;
                }
            }
        }
        return winner;
    }

    public HandRank getWinnerHandRank(Player winner){
        return handCompare.checkHand(winner.getPlayerHand().getCards());
    }

    public void awardPotToLastStanding() {
        for (Player p : players) {
            if (!p.isFolded()) {
                p.addWinnings(pot);
                break;
            }
        }
        resetGame();
    }

    public Player getPlayerById(String playerId) {
        for (Player player : players){
            if (Objects.equals(player.getPlayerId(), playerId)){
                return player;
            }
        }
        throw new IllegalStateException("No such player");
    }

    public int getActivePlayersCount() {
        int count = 0;
        for (Player p : players) {
            if (!p.isFolded()) {
                count++;
            }
        }
        return count;
    }

    public Player getLastPlayerStanding() {
        for (Player p : players) {
            if (!p.isFolded()) {
                return p;
            }
        }
        return null;
    }

    public String getFoldNotification(String playerId) {
        StringBuilder notification = new StringBuilder();
        notification.append("PLAYER ").append(playerId).append(" FOLDED.\n");
        notification.append("CURRENT PLAYER: ")
                .append(players.get(currentPlayerIndex).getPlayerId());
        return notification.toString();
    }



    // gettery, settery :

    public String getGameID() {
        return gameID;
    }

    public void setGameID(String gameID) {
        this.gameID = gameID;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public Deck getDeck() {
        return deck;
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }

    public HandCompare getHandCompare() {
        return handCompare;
    }

    public void setHandCompare(HandCompare handCompare) {
        this.handCompare = handCompare;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public int getDealerIndex() {
        return dealerIndex;
    }

    public void setDealerIndex(int dealerIndex) {
        this.dealerIndex = dealerIndex;
    }

    public int getSmallBlindIndex() {
        return smallBlindIndex;
    }

    public void setSmallBlindIndex(int smallBlindIndex) {
        this.smallBlindIndex = smallBlindIndex;
    }

    public int getBigBlindIndex() {
        return bigBlindIndex;
    }

    public void setBigBlindIndex(int bigBlindIndex) {
        this.bigBlindIndex = bigBlindIndex;
    }

    public int getSmallBlindAmount() {
        return smallBlindAmount;
    }

    public void setSmallBlindAmount(int smallBlindAmount) {
        this.smallBlindAmount = smallBlindAmount;
    }

    public int getBigBlindAmount() {
        return bigBlindAmount;
    }

    public void setBigBlindAmount(int bigBlindAmount) {
        this.bigBlindAmount = bigBlindAmount;
    }

    public int getPot() {
        return pot;
    }

    public void setPot(int pot) {
        this.pot = pot;
    }

    public int getCurrentBet() {
        return currentBet;
    }

    public void setCurrentBet(int currentBet) {
        this.currentBet = currentBet;
    }

    public int getPlayersRemaining() {
        return playersRemaining;
    }

    public void setPlayersRemaining(int playersRemaining) {
        this.playersRemaining = playersRemaining;
    }
}
