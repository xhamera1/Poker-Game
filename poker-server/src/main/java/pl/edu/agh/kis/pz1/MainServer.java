package pl.edu.agh.kis.pz1;

import pl.edu.agh.kis.pz1.model.Game;
import pl.edu.agh.kis.pz1.model.GameState;
import pl.edu.agh.kis.pz1.model.Player;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * MainServer class to handle the communication between clients and the server using Java NIO.
 * It listens for client connections, manages game states, and processes commands from clients.
 * The server supports multiple games and players, facilitating interaction via socket communication.
 */
public class MainServer {
    static final String INVALID_COMMAND_INFO = "ERROR : INVALID COMMAND";
    static final String CREATE_STRING = "CREATE";
    static final String EXCHANGE_STRING = "EXCHANGE";
    static final String ACCESS_ERROR = "ERROR: UNAUTHORIZED ACCESS";
    static final String ERROR_STRING = "ERROR: ";
    static final String GAME_ID_STRING = "\nGAME_ID: ";
    static final String STATE_STRING = "\nSTATE: ";
    static final String CURRENT_PLAYER_STRING = "\nCURRENT_PLAYER: ";
    static final String POT_STRING = "\nPOT: ";
    static final String PHASE_STRING = "\nPHASE: ";
    public static final int PORT = 9999;
    private Selector selector;
    private ServerSocketChannel serverChannel;
    private Map<String, Game> games = new HashMap<>();
    private Map<String, SocketChannel> clientGameMap = new HashMap<>();
    private static boolean shutdownRequested = false;

    /**
     * Constructor for MainServer class. This constructor is intentionally left empty.
     * Initialization is handled in the run() method.
     */
    public MainServer() {
        // Intentionally left empty. Initialization is handled elsewhere.
    }

    /**
     * Main entry point for the server application. This method starts the server by initializing
     * required components and then entering the server's main loop.
     *
     * @param args Command-line arguments (not used in this implementation).
     */
    public static void main(String[] args) {
        MainServer mainServer = new MainServer();
        mainServer.run();
    }

    /**
     * Initializes and runs the server. It sets up the server socket channel, binds it to the port,
     * and starts listening for incoming client connections. The method also processes incoming
     * commands and communicates with clients based on the game state.
     */
    public void run(){
        try{
            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(PORT));
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("NONBLOCKING SERVER ON PORT: " + PORT);

            ByteBuffer buffer = ByteBuffer.allocate(1024);

            while (!shutdownRequested){
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()){
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (key.isAcceptable()){
                        handleAccept(key);
                    } else if (key.isReadable()){
                        handleRead(key, buffer);
                    }
                }
            }

        } catch (IOException e){
            System.err.println("Blad: " + e.getMessage());
        }
    }

    /**
     * Handles the acceptance of a new client connection. This method is called when a new client
     * attempts to connect to the server. The connection is established, and the client is registered
     * for read operations in the NIO selector.
     *
     * @param key The SelectionKey associated with the ServerSocketChannel, which indicates that the
     *            channel is ready to accept a new client connection.
     * @throws IOException If an I/O error occurs while accepting the client connection.
     */
    public void handleAccept(SelectionKey key) throws IOException{
        try{
            ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
            SocketChannel clientChannel = ssc.accept();
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ);
            System.out.println("NEW CLIENT: " + clientChannel.getRemoteAddress());
        } catch (IOException e) {
            System.err.println("ERROR WHILE ACCEPTING CLIENT: " + e.getMessage());
        }
    }

    /**
     * Handles reading data from a connected client. This method is called when the server is ready
     * to read incoming data from a client. It reads the data into a buffer, processes the message,
     * and optionally sends a response back to the client.
     *
     * @param key    The SelectionKey associated with the client channel, indicating that it is ready
     *               for reading.
     * @param buffer The ByteBuffer used for reading the data from the client channel.
     */
    public void handleRead(SelectionKey key, ByteBuffer buffer) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        buffer.clear();
        try {
            int bytesRead = clientChannel.read(buffer);
            if (bytesRead == -1) {
                System.out.println("CLIENT DISCONNECTED: " + clientChannel.getRemoteAddress());
                disconnectClient(clientChannel);
                return;
            }
            buffer.flip();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            String message = new String(data, StandardCharsets.UTF_8).trim();
            System.out.println("FROM CLIENT: " + message);

            String response = handleMessage(message, clientChannel);
            if (response != null) {
                sendMessage(clientChannel, response);
            }
        } catch (IOException e) {
            System.err.println("ERROR WHILE READING FROM CLIENT: " + e.getMessage());
            disconnectClient(clientChannel);
        }
    }

    /**
     * Disconnects a client from the server. This method is called when a client disconnects,
     * either intentionally or due to an error. It removes the client from the game, cleans up
     * associated data, and closes the client channel.
     *
     * @param clientChannel The SocketChannel representing the client to be disconnected.
     */
    public void disconnectClient(SocketChannel clientChannel) {
        String playerId = null;
        for (Map.Entry<String, SocketChannel> entry : clientGameMap.entrySet()) {
            if (entry.getValue().equals(clientChannel)) {
                playerId = entry.getKey();
                break;
            }
        }

        if (playerId != null) {
            final String finalPlayerId = playerId;
            for (Map.Entry<String, Game> gameEntry : games.entrySet()) {
                Game game = gameEntry.getValue();
                if (game.getPlayers().stream().anyMatch(p -> p.getPlayerId().equals(finalPlayerId))) {
                    removePlayerFromGame(gameEntry.getKey(), finalPlayerId);
                    break;
                }
            }
            try{
                clientGameMap.remove(playerId);
            } catch (Exception e){
                System.err.println("ERROR WHILE CLOSING CLIENT CONNECTION: " + e.getMessage());
            }
        }

        try {
            clientChannel.close();
            System.out.println("CLIENT DISCONNECTED");
        } catch (IOException e) {
            System.err.println("ERROR WHILE CLOSING CLIENT CONNECTION: " + e.getMessage());
        }
    }

    /**
     * Handles an incoming message from a client, processes the command, and returns an appropriate response.
     * This method interprets the message, validates the command, and routes the request to the appropriate
     * handler based on the command type (e.g., CREATE, JOIN, READY, FOLD, etc.).
     *
     * Commands that can be handled:
     * - GAME_ID PLAYER_ID CREATE               : Creates a new game.
     * - GAME_ID PLAYER_ID JOIN amount          : Joins an existing game.
     * - GAME_ID PLAYER_ID READY                : Marks the player as ready to play.
     * - GAME_ID PLAYER_ID FOLD                 : The player folds the hand.
     * - GAME_ID PLAYER_ID CALL                 : The player calls the current bet.
     * - GAME_ID PLAYER_ID CHECK                : The player checks (does not raise or fold).
     * - GAME_ID PLAYER_ID RAISE raiseValue     : The player raises the current bet by a specified value.
     * - GAME_ID PLAYER_ID EXCHANGE 2,3         : The player exchanges specified cards.
     * - GAME_ID PLAYER_ID STATUS               : The player requests the current game status.
     * - GAME_ID PLAYER_ID LEAVE                : The player leaves the game.
     *
     * @param message The command message sent by the client, which includes the game ID, player ID, and the command.
     * @param sc      The SocketChannel representing the connection to the client.
     * @return A response message based on the command. If the command is invalid or an error occurs, an error message is returned.
     */
    public String handleMessage(String message, SocketChannel sc) {
        message = message.toUpperCase();
        String[] tokens = message.split(" ");

        if (tokens.length > 4 || tokens.length < 3) {
            return INVALID_COMMAND_INFO;
        }

        String gameId = tokens[0];
        String playerId = tokens[1];
        String command = tokens[2];

        Set<String> validCommands = Set.of(CREATE_STRING, "JOIN", "READY", "FOLD", "CALL",
                "CHECK", "RAISE", EXCHANGE_STRING, "STATUS", "LEAVE", "CARDS");
        if (!validCommands.contains(command)) {
            return INVALID_COMMAND_INFO;
        }

        List<Integer> indexes = new ArrayList<>();
        if (tokens.length == 4 && Objects.equals(command, EXCHANGE_STRING)) {
            try {
                indexes = parseIndexesIfNeeded(tokens);
            } catch (IllegalArgumentException e) {
                return e.getMessage();
            }
        }

        Game game = games.get(gameId);
        if (game == null && !Objects.equals(command, CREATE_STRING)) {
            return "GAME NOT FOUND";
        }

        if (Objects.equals(command, CREATE_STRING)) {
            return handleCreateGame(gameId);
        }

        if (!isPlayerAuthorized(playerId, sc) && !Objects.equals(command, "JOIN")) {
            return ACCESS_ERROR;
        }

        return switch (command) {
            case "JOIN" -> handleJoin(game, playerId, tokens, sc);
            case "READY" -> handleReady(game, playerId);
            case "CARDS" -> handleCards(game, playerId);
            case "STATUS" -> handleStatus(game, playerId);
            case "CALL" -> handleCall(game, playerId, sc);
            case "RAISE" -> handleRaise(game, playerId, tokens, sc);
            case "CHECK" -> handleCheck(game, playerId, sc);
            case EXCHANGE_STRING -> handleExchange(game, playerId, indexes, sc);
            case "FOLD" -> handleFold(game, playerId);
            case "LEAVE" -> handleLeave(gameId, playerId);
            default -> INVALID_COMMAND_INFO;
        };
    }

    /**
     * Parses the card exchange indexes from the command if provided. This method processes a comma-separated
     * string of card indexes and checks for validity. It ensures that all indexes are integers within the
     * valid range of [0-4], as only 5 cards can be exchanged in a poker hand.
     *
     * @param tokens The command split into tokens, where the third token is expected to be "EXCHANGE"
     *               and the fourth token contains the card indexes.
     * @return A list of valid card indexes (integers) that the player wants to exchange.
     * @throws IllegalArgumentException If the provided card indexes are invalid, out of range, or in an
     *                                  incorrect format.
     */
    public List<Integer> parseIndexesIfNeeded(String[] tokens) {
        List<Integer> indexes = new ArrayList<>();
        if (tokens.length == 4 && Objects.equals(tokens[2], EXCHANGE_STRING)) {
            try {
                String[] strIndexes = tokens[3].split(",");

                for (String strIndex : strIndexes) {
                    String trimmedIndex = strIndex.trim();

                    if (!trimmedIndex.matches("\\d+")) {
                        throw new IllegalArgumentException("ERROR: INVALID CARD INDEXES. Please provide valid integers separated by commas.");
                    }

                    int index = Integer.parseInt(trimmedIndex);
                    if (index < 0 || index >= 5) {
                        throw new IllegalArgumentException("ERROR: CARD INDEX OUT OF RANGE. Valid range is [0-4].");
                    }

                    indexes.add(index);
                }

            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("ERROR: INVALID CARD INDEXES. Please provide valid integers separated by commas.");
            }

            if (indexes.size() > 4) {
                throw new IllegalArgumentException("ERROR: TOO MANY CARDS TO EXCHANGE. You can exchange a maximum of 4 cards.");
            }
        }
        return indexes;
    }



    /**
     * Handles the creation of a new game. This method checks if a game with the given game ID already exists.
     * If not, it creates a new game and adds it to the list of active games.
     *
     * @param gameId The ID of the game to be created.
     * @return A message indicating whether the game was successfully created or if a game with the same ID already exists.
     */
    public String handleCreateGame(String gameId) {
        if (games.containsKey(gameId)) {
            return "GAME WITH THAT ID HAS BEEN DONE YET";
        }
        Game game = new Game(gameId);
        games.put(gameId, game);
        System.out.println("CREATED NEW GAME : " + gameId);
        return "CREATED NEW GAME: " + gameId;
    }


    /**
     * Handles the "JOIN" command, where a player joins an existing game by providing their player ID and
     * an initial betting amount. The method verifies if the game is in a valid state to accept new players
     * and if the provided betting amount is valid.
     *
     * @param game The game the player is attempting to join.
     * @param playerId The unique identifier of the player.
     * @param tokens The command tokens split by space, where the fourth token is expected to be the amount.
     * @param sc The SocketChannel representing the connection with the client.
     * @return A response string indicating the result of the "JOIN" command, including any errors or success messages.
     */
    public String handleJoin(Game game, String playerId, String[] tokens, SocketChannel sc) {
        if (tokens.length < 4) {
            return "ERROR: INVALID COMMAND (JOIN + amount)";
        }
        int amount;
        try {
            amount = Integer.parseInt(tokens[3]);
        } catch (NumberFormatException e) {
            return "ERROR: INVALID AMOUNT";
        }

        try {
            if (game.getGameState() == GameState.WAITING_FOR_PLAYERS) {
                game.addPlayer(new Player(playerId, amount));
                clientGameMap.put(playerId, sc);
                return game.getGameID() + " " + playerId + " : PLAYER ADDED, " + game.getPlayers().size() + "/4 PLAYERS";
            } else {
                return game.getGameID() + " ERROR: Cannot join: Game has already started";
            }
        } catch (IllegalStateException ex) {
            return game.getGameID() + ERROR_STRING + ex.getMessage();
        }
    }

    /**
     * Handles the "READY" command, marking the player as ready for the game to start.
     * If all players are marked as ready, the game will notify the players to start.
     *
     * @param game The game the player is interacting with.
     * @param playerId The unique identifier of the player.
     * @return A response string indicating the result of the "READY" command, including player status.
     */
    public String handleReady(Game game, String playerId) {
        try {
            game.setPlayerReady(playerId);
        } catch (IllegalStateException ex) {
            return ERROR_STRING + ex.getMessage();
        }
        if (game.getNumberOfReadyPlayers() == 0) {
            notifyGameStartToPlayers(game);
            return " ";
        }
        return "INFO: PLAYER " + playerId + " SET TO READY. READY PLAYERS: "
                + game.getNumberOfReadyPlayers() + "/" + game.getPlayers().size();
    }

    /**
     * Handles the "CARDS" command, retrieving and displaying the player's current hand in the game.
     *
     * @param game The game the player is interacting with.
     * @param playerId The unique identifier of the player.
     * @return A string representing the player's hand in the game, or an error message if there is an issue.
     */
    public String handleCards(Game game, String playerId) {
        try {
            String hand = game.getPlayerHand(playerId);
            return "GAME_ID: " + game.getGameID() + " PLAYER_ID: " + playerId + " CARDS: \n" + hand;
        } catch (IllegalStateException ex) {
            return ERROR_STRING + ex.getMessage();
        }
    }

    /**
     * Handles the "STATUS" command, which provides the current state of the game, including the game ID,
     * the current state of the game (e.g., waiting for players, in progress), the current player, the blinds,
     * and the pot amount.
     *
     * @param game The game whose status is being requested.
     * @param playerId The unique identifier of the player requesting the status.
     * @return A string containing the current status of the game, or an error message if an exception occurs.
     */
    public String handleStatus(Game game, String playerId) {
        try {
            Player currentPlayer = game.getPlayers().get(game.getCurrentPlayerIndex());
            return GAME_ID_STRING + game.getGameID() +
                    STATE_STRING + game.getGameState() +
                    "\nPLAYER_ID: " + playerId +
                    "\nBLINDS: " + game.getInfoBlinds() +
                    CURRENT_PLAYER_STRING + currentPlayer.getPlayerId() +
                    POT_STRING + game.getPot();
        } catch (IllegalStateException ex) {
            return ERROR_STRING + ex.getMessage();
        }
    }

    /**
     * Handles the "FOLD" command, allowing a player to fold (quit the current round) during a betting round.
     * The player can only fold if the game is in one of the betting rounds (either first or second betting round).
     * If the last player folds, the game is concluded.
     *
     * @param game The game in which the player is folding.
     * @param playerId The unique identifier of the player who is folding.
     * @return A string indicating the result of the fold action, or an error message if the fold is not allowed.
     */
    public String handleFold(Game game, String playerId) {
        try {
            if (game.getGameState() != GameState.FIRST_BETTING_ROUND && game.getGameState() != GameState.SECOND_BETTING_ROUND) {
                return "ERROR: You can only fold in betting rounds";
            }
            game.playerFold(playerId);
            String foldNotification = game.getFoldNotification(playerId);
            notifyAllPlayers(game, foldNotification);
            if (game.getPlayersRemaining() == 1) {
                handleLastPlayerStanding(game);
            }
            return " ";
        } catch (IllegalStateException ex) {
            return ERROR_STRING + ex.getMessage();
        }
    }


    /**
     * Handles the "LEAVE" command, allowing a player to leave a game. The player is removed from the game,
     * and their status is updated accordingly.
     *
     * @param gameId The ID of the game the player is leaving.
     * @param playerId The unique identifier of the player leaving the game.
     * @return A string indicating that the player has left the game.
     */
    public String handleLeave(String gameId, String playerId) {
        removePlayerFromGame(gameId, playerId);
        return "PLAYER " + playerId + " LEFT GAME " + gameId;
    }


    /**
     * Handles the "CALL" command, where a player matches the current bet in the round.
     * The method checks if the player is authorized to make the call and updates the game state accordingly.
     *
     * @param game The game in which the player is calling.
     * @param playerId The unique identifier of the player making the call.
     * @param sc The SocketChannel representing the connection with the player.
     * @return A string indicating the result of the call action, or an error message if the call is not valid.
     */
    public String handleCall(Game game, String playerId, SocketChannel sc) {
        try {
            if (!isPlayerAuthorized(playerId, sc)) {
                return ACCESS_ERROR;
            }
            game.playerCall(playerId);
            notifyGameStateToPlayers(game);

            if (game.processBettingRound()) {
                notifyBettingRoundFinished(game);
            }
            return " ";
        } catch (IllegalStateException ex) {
            return ERROR_STRING + ex.getMessage();
        }
    }

    /**
     * Handles the "RAISE" command, allowing a player to increase their current bet during a betting round.
     * The player can only raise the bet if they are authorized and the amount is valid.
     * After raising, the game state is updated and the round is processed if necessary.
     *
     * @param game The game in which the raise action is taking place.
     * @param playerId The unique identifier of the player raising the bet.
     * @param tokens The array of tokens split from the player's message, containing the raise amount.
     * @param sc The socket channel representing the player making the raise.
     * @return A string indicating the result of the raise action, or an error message if the raise is not allowed.
     */
    public String handleRaise(Game game, String playerId, String[] tokens, SocketChannel sc) {
        if (tokens.length < 4) {
            return "ERROR: INVALID COMMAND (RAISE + amount)";
        }

        try {
            if (!isPlayerAuthorized(playerId, sc)) {
                return ACCESS_ERROR;
            }
            int raiseAmount = Integer.parseInt(tokens[3]);
            game.playerRaise(playerId, raiseAmount);
            notifyGameStateToPlayers(game);

            if (game.processBettingRound()) {
                notifyBettingRoundFinished(game);
            }
            return " ";
        } catch (NumberFormatException e) {
            return "ERROR: INVALID RAISE AMOUNT";
        } catch (IllegalStateException ex) {
            return ERROR_STRING + ex.getMessage();
        }
    }

    /**
     * Handles the "CHECK" command, allowing a player to check (i.e., pass their turn without betting) during a betting round.
     * The player can only check if they are authorized and the game state allows it.
     * After checking, the game state is updated and the round is processed if necessary.
     *
     * @param game The game in which the check action is taking place.
     * @param playerId The unique identifier of the player checking the bet.
     * @param sc The socket channel representing the player making the check.
     * @return A string indicating the result of the check action, or an error message if the check is not allowed.
     */
    public String handleCheck(Game game, String playerId, SocketChannel sc) {
        try {
            if (!isPlayerAuthorized(playerId, sc)) {
                return ACCESS_ERROR;
            }
            game.playerCheck(playerId);
            notifyGameStateToPlayers(game);

            if (game.processBettingRound()) {
                notifyBettingRoundFinished(game);
            }
            return " ";
        } catch (IllegalStateException ex) {
            return ERROR_STRING + ex.getMessage();
        }
    }

    /**
     * Handles the "EXCHANGE" command, allowing a player to exchange a certain number of their cards.
     * The player must provide valid card indexes and can exchange up to 4 cards. The player can only exchange cards
     * if they are authorized, and the game allows the exchange.
     * If the player has folded, the exchange will still proceed, but the folded status will be noted.
     *
     * @param game The game in which the exchange action is taking place.
     * @param playerId The unique identifier of the player requesting the card exchange.
     * @param indexes A list of card indexes to be exchanged.
     * @param sc The socket channel representing the player making the exchange request.
     * @return A string indicating the result of the exchange action, including the player's new hand, or an error message if the exchange is not allowed.
     */
    public String handleExchange(Game game, String playerId, List<Integer> indexes, SocketChannel sc) {
        try {
            Player player = game.getPlayerById(playerId);
            if (player.isFolded()) {
                notifyPlayerExchangeIfFolded(game, playerId);
                game.exchangeCards(playerId, indexes);
                return " ";
            }
            if (!isPlayerAuthorized(playerId, sc)) {
                return ACCESS_ERROR;
            }
            if (indexes.size() > 4) {
                return "ERROR: You can exchange max 4 cards";
            }
            List<Integer> allowedIndexes = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                allowedIndexes.add(i);
            }
            for (int ind : indexes) {
                if (!allowedIndexes.contains(ind)) {
                    return "ERROR: Invalid indexes of cards";
                }
            }
            game.exchangeCards(playerId, indexes);
            String hand = game.getPlayerHand(playerId);
            notifyPlayerExchange(game, playerId);
            return "GAME_ID: " + game.getGameID() + " PLAYER_ID: " + playerId + " EXCHANGED HAND: \n" + hand;
        } catch (IllegalStateException ex) {
            return ERROR_STRING + ex.getMessage();
        }
    }



    /**
     * Sends a message to the specified client over the given SocketChannel.
     * The message is first trimmed and encoded in UTF-8 before being sent.
     *
     * @param clientChannel The SocketChannel to which the message will be sent.
     * @param message The message to be sent to the client.
     */
    public void sendMessage(SocketChannel clientChannel, String message){
        try{
            ByteBuffer buffer = ByteBuffer.wrap((message.trim()).getBytes(StandardCharsets.UTF_8));
            clientChannel.write(buffer);
        } catch (IOException e){
            System.err.println("ERROR WHILE SENDING MESSAGE TO CLIENT: " + e.getMessage());
        }
    }

    /**
     * Notifies all players in the game about the current game state.
     * This includes information about the game ID, current state, current player, pot, and betting round contributions.
     * The information is sent to each player via their corresponding SocketChannel.
     *
     * @param game The game whose state is being notified to players.
     */
    public void notifyGameStateToPlayers(Game game) {
        for (Player player : game.getPlayers()) {
            String playerId = player.getPlayerId();
            SocketChannel clientChannel = clientGameMap.get(playerId);
            if (clientChannel != null) {
                StringBuilder notification = new StringBuilder();
                notification.append(GAME_ID_STRING).append(game.getGameID())
                        .append(STATE_STRING).append(game.getGameState())
                        .append(CURRENT_PLAYER_STRING).append(game.getPlayers().get(game.getCurrentPlayerIndex()).getPlayerId())
                        .append(POT_STRING).append(game.getPot())
                        .append("\nBETTING ROUND CONTRIBUTIONS:");

                for (Player p : game.getPlayers()) {
                    notification.append("\nPLAYER_ID: ").append(p.getPlayerId())
                            .append(" BET: ").append(p.getCurrentBetInThisRound());
                }

                sendMessage(clientChannel, notification.toString());
            }
        }
    }

    /**
     * Notifies all players about the start of the game.
     * This includes information about the game ID, small blind, big blind, current player, phase, pot, and each player's hand.
     * The information is sent to each player via their corresponding SocketChannel.
     *
     * @param game The game that has started.
     */
    public void notifyGameStartToPlayers(Game game) {
        for (Player player : game.getPlayers()) {
            String playerId = player.getPlayerId();
            SocketChannel clientChannel = clientGameMap.get(playerId);
            if (clientChannel != null) {
                StringBuilder startMessage = new StringBuilder();
                startMessage.append("\nGAME STARTED!")
                        .append(GAME_ID_STRING).append(game.getGameID())
                        .append("\nSMALL_BLIND: ").append(game.getPlayers().get(game.getSmallBlindIndex()).getPlayerId())
                        .append("\nBIG_BLIND: ").append(game.getPlayers().get(game.getBigBlindIndex()).getPlayerId())
                        .append(CURRENT_PLAYER_STRING).append(game.getPlayers().get(game.getCurrentPlayerIndex()).getPlayerId())
                        .append(PHASE_STRING).append(game.getGameState())
                        .append(POT_STRING).append(game.getPot())
                        .append("\nYOUR CARDS: ").append(game.getPlayerHand(playerId));
                sendMessage(clientChannel, startMessage.toString());
            }
        }
    }

    /**
     * Notifies all players about a specific player's card exchange action.
     * This includes the player ID and the game ID, notifying everyone that the player has finished exchanging cards.
     * The information is sent to each player via their corresponding SocketChannel.
     *
     * @param game The game where the exchange occurred.
     * @param playerId The unique identifier of the player who exchanged their cards.
     */
    public void notifyPlayerExchange(Game game, String playerId) {
        for (Player player : game.getPlayers()) {
            String targetPlayerId = player.getPlayerId();
            SocketChannel clientChannel = clientGameMap.get(targetPlayerId);
            if (clientChannel != null) {
                StringBuilder exchangeMessage = new StringBuilder();
                exchangeMessage.append("\nPLAYER ").append(playerId).append(" FINISHED EXCHANGING CARDS.")
                        .append(GAME_ID_STRING).append(game.getGameID())
                        .append(CURRENT_PLAYER_STRING).append(game.getPlayers().get(game.getCurrentPlayerIndex()).getPlayerId())
                        .append(PHASE_STRING).append(game.getGameState())
                        .append(POT_STRING).append(game.getPot());
                sendMessage(clientChannel, exchangeMessage.toString());
            }
        }
    }

    /**
     * Notifies all players that a specific player has skipped their turn because they have folded.
     * This notification includes game details such as the current game ID, the current player, the game state, and the pot.
     * The message is sent to each player's SocketChannel.
     *
     * @param game The game whose status is being notified.
     * @param playerId The player who folded and skipped their turn.
     */
    public void notifyPlayerExchangeIfFolded(Game game, String playerId) {
        for (Player player : game.getPlayers()) {
            String targetPlayerId = player.getPlayerId();
            SocketChannel clientChannel = clientGameMap.get(targetPlayerId);
            if (clientChannel != null) {
                StringBuilder exchangeMessage = new StringBuilder();
                exchangeMessage.append("\nPLAYER ").append(playerId).append(" SKIPPED BECAUSE HE IS FOLDED.")
                        .append(GAME_ID_STRING).append(game.getGameID())
                        .append(CURRENT_PLAYER_STRING).append(game.getPlayers().get(game.getCurrentPlayerIndex()).getPlayerId())
                        .append(PHASE_STRING).append(game.getGameState())
                        .append(POT_STRING).append(game.getPot());
                sendMessage(clientChannel, exchangeMessage.toString());
            }
        }
    }

    /**
     * Notifies all players that a betting round has finished.
     * The message depends on the current game state, and can indicate the conclusion of the first betting round,
     * the second betting round, or a game-over situation where a winner is determined.
     *
     * @param game The game whose betting round is being notified.
     */
    public void notifyBettingRoundFinished(Game game) {
        for (Player player : game.getPlayers()) {
            String playerId = player.getPlayerId();
            SocketChannel clientChannel = clientGameMap.get(playerId);
            if (clientChannel != null) {
                StringBuilder message = new StringBuilder();

                if (game.getGameState() == GameState.EXCHANGE_PHASE) {
                    message.append("\nFIRST BETTING ROUND FINISHED!")
                            .append(GAME_ID_STRING).append(game.getGameID())
                            .append(STATE_STRING).append(game.getGameState())
                            .append("\nPHASE: EXCHANGE CARDS")
                            .append(CURRENT_PLAYER_STRING).append(game.getPlayers().get(game.getCurrentPlayerIndex()).getPlayerId());
                } else if (game.getGameState() == GameState.SHOWDOWN) {
                    message.append("\nSECOND BETTING ROUND FINISHED!")
                            .append(GAME_ID_STRING).append(game.getGameID())
                            .append(STATE_STRING).append(game.getGameState());
                } else if (game.getGameState() == GameState.GAME_OVER) {
                    Player winner = game.getWinner();
                    if (winner != null) {
                        message.append("\n## WINNER ANNOUNCEMENT ##")
                                .append(GAME_ID_STRING).append(game.getGameID())
                                .append("\nTHE WINNER IS: ").append(winner.getPlayerId())
                                .append("\nWINNER'S HAND: ").append(game.getPlayerHand(winner.getPlayerId()))
                                .append("\nHAND RANK: ").append(game.getWinnerHandRank(winner).getType())
                                .append("\nKICKERS: ").append(game.getWinnerHandRank(winner).getKickers())
                                .append("\nPOT WON: ").append(game.getPot());
                    } else {
                        message.append("\nERROR: NO WINNER DETERMINED.");
                    }
                }

                sendMessage(clientChannel, message.toString());
            }
        }

        if (game.getGameState() == GameState.GAME_OVER) {
            resetGameOnServer(game);
        }
    }

    /**
     * Resets the state of the game on the server when the game is over.
     * This includes clearing the game's players, resetting the game state, and removing the game from the server's game map.
     *
     * @param game The game to be reset on the server.
     */
    public void resetGameOnServer(Game game) {
        System.out.println("Resetting game: " + game.getGameID());
        game.resetAfterGame();
        notifyAllPlayers(game, "Game reset. You can now join a new round.");
    }

    /**
     * Handles the game over scenario by notifying all players that the betting round has finished,
     * and then resetting the game state for the next round or session.
     *
     * @param game The game that is over and needs to be reset.
     */
    public void handleGameOver(Game game) {
        notifyBettingRoundFinished(game);
        game.resetAfterGame(); // Reset gry
    }

    /**
     * Handles the case where there is only one player remaining in the game.
     * It declares that player as the winner, sends a message to all players, and resets the game.
     *
     * @param game The game where the last player is standing.
     */
    public void handleLastPlayerStanding(Game game) {
        Player winner = game.getLastPlayerStanding();

        if (winner != null) {
            String message = "Game Over! The winner is: " + winner.getPlayerId() + " with a pot of " + game.getPot() + " chips!";
            System.out.println(message);
            notifyAllPlayers(game, message);
        }

        game.resetAfterGame();
    }


    /**
     * Checks if a player is authorized to perform actions in the game based on their socket channel.
     * The player is authorized if their player ID is associated with the provided socket channel.
     *
     * @param playerId The player ID to be checked.
     * @param sc The socket channel of the player.
     * @return true if the player is authorized to act, false otherwise.
     */
    public boolean isPlayerAuthorized(String playerId, SocketChannel sc) {
        return Objects.equals(clientGameMap.get(playerId), sc);
    }

    /**
     * Removes a player from a game by their player ID.
     * If the game has no more players, it will be removed from the active games.
     *
     * @param gameId The ID of the game from which the player should be removed.
     * @param playerId The ID of the player to be removed.
     */
    public void removePlayerFromGame(String gameId, String playerId) {
        Game game = games.get(gameId);
        if (game != null) {
            game.removePlayer(playerId);
            clientGameMap.remove(playerId);

            if (game.getPlayers().isEmpty()) {
                games.remove(gameId);
                System.out.println("GAME " + gameId + " REMOVED AS ALL PLAYERS LEFT.");
            }
        }
    }


    /**
     * Sends each player's hand (cards) and the current pot value to all players in the game.
     *
     * @param game The game instance containing the players and the current game state.
     */
    public void sendHandPlayers(Game game) {
        for (Player player : game.getPlayers()) {
            String playerId = player.getPlayerId();
            SocketChannel clientChannel = clientGameMap.get(playerId);
            if (clientChannel != null) {
                String handMessage = "PLAYER_ID: " + playerId +
                        "\nYOUR CARDS: " + game.getPlayerHand(playerId) +
                        "\nCURRENT POT: " + game.getPot();
                sendMessage(clientChannel, handMessage);
            }
        }
    }

    /**
     * Sends a message to all players in the game.
     *
     * @param game The game instance containing all players.
     * @param message The message to be sent to all players.
     */
    public void notifyAllPlayers(Game game, String message) {
        for (Player player : game.getPlayers()) {
            String playerId = player.getPlayerId();
            SocketChannel clientChannel = clientGameMap.get(playerId);
            if (clientChannel != null) {
                sendMessage(clientChannel, message);
            }
        }
    }

    /**
     * Sets the selector for handling non-blocking I/O operations.
     *
     * @param selector The selector to be set.
     */
    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    /**
     * Gets the current selector used for non-blocking I/O operations.
     *
     * @return The selector instance being used.
     */
    public Selector getSelector() {
        return selector;
    }

    /**
     * Gets the server socket channel used for accepting client connections.
     *
     * @return The server socket channel instance.
     */
    public ServerSocketChannel getServerChannel() {
        return serverChannel;
    }

    /**
     * Sets the server socket channel used for accepting client connections.
     *
     * @param serverChannel The server socket channel to be set.
     */
    public void setServerChannel(ServerSocketChannel serverChannel) {
        this.serverChannel = serverChannel;
    }

    /**
     * Gets the map of games currently managed by the server.
     *
     * @return The map of games, where the key is the game ID and the value is the game instance.
     */
    public Map<String, Game> getGames() {
        return games;
    }

    /**
     * Sets the map of games managed by the server.
     *
     * @param games The map of games to be set, where the key is the game ID and the value is the game instance.
     */
    public void setGames(Map<String, Game> games) {
        this.games = games;
    }

    /**
     * Gets the map of client channels mapped to player IDs.
     *
     * @return The map of client channels, where the key is the player ID and the value is the corresponding client channel.
     */
    public Map<String, SocketChannel> getClientGameMap() {
        return clientGameMap;
    }

    /**
     * Sets the map of client channels mapped to player IDs.
     *
     * @param clientGameMap The map of client channels to be set, where the key is the player ID and the value is the corresponding client channel.
     */
    public void setClientGameMap(Map<String, SocketChannel> clientGameMap) {
        this.clientGameMap = clientGameMap;
    }

    /**
     * Initiates server shutdown.
     */
    public static void shutdownServer() {
        shutdownRequested = true;
    }



}

