package pl.edu.agh.kis.pz1;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import pl.edu.agh.kis.pz1.model.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;  // mocki udaja pprawdziwe obiekty

class MainServerTest {
    private MainServer server;
    private SocketChannel mockClientChannel; // mock obiektu socketchanl ktory symuluje poalcznie klienta


    @BeforeEach
    void setUp() {
        server = new MainServer();
        mockClientChannel = mock(SocketChannel.class);
    }

    @Test
    @DisplayName("Test akceptacji nowego polaczenia")
    void testHandleAccept() throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(0)); //dowolnyu dostepny port
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        SocketChannel clientChannel = SocketChannel.open();
        clientChannel.connect(serverChannel.getLocalAddress());

        SelectionKey mockKey = mock(SelectionKey.class);
        when(mockKey.channel()).thenReturn(serverChannel);


        server.setSelector(selector);

        server.handleAccept(mockKey);

        assertTrue(clientChannel.isConnected(), "Klient powinien być połączony z serwerem.");

        clientChannel.close();
        serverChannel.close();
        selector.close();
    }

    @Test
    @DisplayName("Test odbierania i przetwarzania wiadomości")
    void testHandleRead() throws IOException {
        SelectionKey mockKey = mock(SelectionKey.class);
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        when(mockKey.channel()).thenReturn(mockClientChannel);
        when(mockClientChannel.read(buffer)).thenAnswer(invocation -> {
            buffer.put("GAME1 PLAYER1 CREATE".getBytes());
            return "GAME1 PLAYER1 CREATE".length();
        });

        server.handleRead(mockKey, buffer);

        verify(mockClientChannel, times(1)).write(Mockito.any(ByteBuffer.class));
    }

    @Test
    @DisplayName("Test komendy CREATE - tworzenie nowej gry")
    void testHandleCreateGame() {
        String response = server.handleCreateGame("GAME1");
        assertEquals("CREATED NEW GAME: GAME1", response, "Powinien zwrócić poprawną informację o stworzeniu gry");
    }

    @Test
    @DisplayName("Test usunięcia gracza z gry")
    void testRemovePlayerFromGame() {
        Game game = new Game("GAME1");
        Player player = new Player("PLAYER1", 1000);
        game.addPlayer(player);

        server.getGames().put("GAME1", game);

        server.removePlayerFromGame("GAME1", "PLAYER1");

        assertTrue(game.getPlayers().isEmpty(), "Lista graczy powinna być pusta");
    }

    @Test
    @DisplayName("Test ustawiania i pobierania obiektu Selector")
    void testSetAndGetSelector() {
        Selector selector = mock(Selector.class);
        server.setSelector(selector);

        assertEquals(selector, server.getSelector(), "Getter powinien zwrócić prawidłowy obiekt Selector");
    }

    @Test
    @DisplayName("Test ustawiania i pobierania obiektu ServerSocketChannel")
    void testSetAndGetServerChannel() {
        ServerSocketChannel serverChannel = mock(ServerSocketChannel.class);
        server.setServerChannel(serverChannel);

        assertEquals(serverChannel, server.getServerChannel(), "Getter powinien zwrócić prawidłowy obiekt ServerSocketChannel");
    }

    @Test
    @DisplayName("Test ustawiania i pobierania mapy gier")
    void testSetAndGetGames() {
        Map<String, Game> games = new HashMap<>();
        games.put("GAME1", new Game("GAME1"));

        server.setGames(games);

        assertEquals(games, server.getGames(), "Getter powinien zwrócić prawidłową mapę gier");
        assertEquals(1, server.getGames().size(), "Mapa powinna zawierać jedną grę");
    }

    @Test
    @DisplayName("Test ustawiania i pobierania mapy klientów")
    void testSetAndGetClientGameMap() {
        Map<String, SocketChannel> clientGameMap = new HashMap<>();
        clientGameMap.put("PLAYER1", mock(SocketChannel.class));

        server.setClientGameMap(clientGameMap);

        assertEquals(clientGameMap, server.getClientGameMap(), "Getter powinien zwrócić prawidłową mapę klientów");
        assertEquals(1, server.getClientGameMap().size(), "Mapa powinna zawierać jednego klienta");
    }


    @Test
    @DisplayName("Test obsługi nieznanej komendy")
    void testHandleMessageWithInvalidCommand() {
        SocketChannel mockSocketChannel = mock(SocketChannel.class);
        String response = server.handleMessage("GAME1 PLAYER1 INVALID", mockSocketChannel);

        assertEquals("ERROR : INVALID COMMAND", response, "Powinna zostać zwrócona informacja o błędnej komendzie");
    }

    @Test
    @DisplayName("Test komendy CREATE gdy gra już istnieje")
    void testHandleCreateGameWhenGameExists() {
        server.handleCreateGame("GAME1");
        String response = server.handleCreateGame("GAME1");

        assertEquals("GAME WITH THAT ID HAS BEEN DONE YET", response, "Powinna zostać zwrócona informacja, że gra już istnieje");
    }

    @Test
    @DisplayName("Test obsługi JOIN bez podania kwoty")
    void testHandleJoinWithoutAmount() {
        SocketChannel mockSocketChannel = mock(SocketChannel.class);
        server.handleCreateGame("GAME1");
        Game game = server.getGames().get("GAME1");

        String response = server.handleJoin(game, "PLAYER1", new String[]{"GAME1", "PLAYER1", "JOIN"}, mockSocketChannel);

        assertEquals("ERROR: INVALID COMMAND (JOIN + amount)", response, "Powinna zostać zwrócona informacja o brakującej kwocie");
    }

    @Test
    @DisplayName("Test obsługi READY komendy")
    void testHandleReadyCommand() {
        server.handleCreateGame("GAME1");
        Game game = server.getGames().get("GAME1");
        game.addPlayer(new Player("PLAYER1", 1000));

        String response = server.handleReady(game, "PLAYER1");

        assertTrue(response.contains("INFO: PLAYER PLAYER1 SET TO READY"), "Powinna zostać ustawiona gotowość gracza");
    }

    @Test
    @DisplayName("Test rozłączenia klienta")
    void testDisconnectClient() throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(0));
        SocketChannel clientChannel = SocketChannel.open();
        clientChannel.connect(serverChannel.getLocalAddress());

        server.getClientGameMap().put("PLAYER1", clientChannel);

        server.disconnectClient(clientChannel);

        assertFalse(server.getClientGameMap().containsKey("PLAYER1"),
                "Gracz powinien zostać usunięty z mapy klientów");
        assertFalse(clientChannel.isOpen(),
                "Kanał klienta powinien zostać zamknięty");

        clientChannel.close();
        serverChannel.close();
    }


    @Test
    @DisplayName("Test usunięcia gracza z gry, gdy gra staje się pusta")
    void testRemovePlayerFromGameWhenGameIsEmpty() {
        Game game = new Game("GAME1");
        game.addPlayer(new Player("PLAYER1", 1000));
        server.getGames().put("GAME1", game);

        server.removePlayerFromGame("GAME1", "PLAYER1");

        assertFalse(server.getGames().containsKey("GAME1"), "Gra powinna zostać usunięta, gdy nie ma w niej graczy");
    }

    @Test
    @DisplayName("Test usunięcia gracza z gry z wieloma graczami")
    void testRemovePlayerFromGameWithOtherPlayers() {
        Game game = new Game("GAME1");
        game.addPlayer(new Player("PLAYER1", 1000));
        game.addPlayer(new Player("PLAYER2", 1000));
        server.getGames().put("GAME1", game);

        server.removePlayerFromGame("GAME1", "PLAYER1");

        assertEquals(1, game.getPlayers().size(), "Gra powinna zawierać jednego gracza");
        assertEquals("PLAYER2", game.getPlayers().get(0).getPlayerId(), "Pozostały gracz powinien być PLAYER2");
    }

    @Test
    @DisplayName("Test parsowania indeksów kart do wymiany")
    void testParseIndexesIfNeededValid() {
        String[] tokens = {"GAME1", "PLAYER1", "EXCHANGE", "1,2,3"};

        List<Integer> indexes = server.parseIndexesIfNeeded(tokens);

        assertEquals(List.of(1, 2, 3), indexes, "Powinna zostać zwrócona lista prawidłowych indeksów kart");
    }

    @Test
    @DisplayName("Test parsowania zbyt wielu indeksów")
    void testParseIndexesIfNeededTooManyIndexes() {
        String[] tokens = {"GAME1", "PLAYER1", "EXCHANGE", "0,1,2,3,4"};

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> server.parseIndexesIfNeeded(tokens));

        assertEquals("ERROR: TOO MANY CARDS TO EXCHANGE. You can exchange a maximum of 4 cards.", exception.getMessage());
    }

    @Test
    @DisplayName("Test parsowania niepoprawnych indeksów")
    void testParseIndexesIfNeededInvalid() {
        String[] tokens = {"GAME1", "PLAYER1", "EXCHANGE", "a,2,3"};

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> server.parseIndexesIfNeeded(tokens));

        assertEquals("ERROR: INVALID CARD INDEXES. Please provide valid integers separated by commas.", exception.getMessage());
    }

    @Test
    @DisplayName("Test obsługi IOException w handleAccept")
    void testHandleAcceptIOException() throws IOException {
        SelectionKey mockKey = mock(SelectionKey.class);
        ServerSocketChannel mockChannel = mock(ServerSocketChannel.class);

        when(mockKey.channel()).thenReturn(mockChannel);
        when(mockChannel.accept()).thenThrow(new IOException("Test IOException"));

        assertDoesNotThrow(() -> server.handleAccept(mockKey),
                "Metoda handleAccept powinna obsłużyć IOException bez wyrzucania wyjątku");
    }

    @Test
    @DisplayName("Test ustawiania i pobierania PORTU")
    void testPortConfiguration() {
        assertEquals(9999, MainServer.PORT, "Port powinien być ustawiony na 9999");
    }


    @Test
    @DisplayName("Test obsługi IOException w handleRead")
    void testHandleReadIOException() throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(0));
        SocketChannel clientChannel = SocketChannel.open();
        clientChannel.connect(serverChannel.getLocalAddress());

        Selector selector = Selector.open();
        serverChannel.configureBlocking(false);
        clientChannel.configureBlocking(false);

        SelectionKey key = clientChannel.register(selector, SelectionKey.OP_READ);
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        clientChannel.close();

        assertDoesNotThrow(() -> server.handleRead(key, buffer),
                "Metoda handleRead powinna obsłużyć IOException bez wyrzucania wyjątku");

        serverChannel.close();
        selector.close();
    }

    @Test
    @DisplayName("Test obsługi nieautoryzowanego gracza")
    void testHandleMessageUnauthorizedPlayer() {
        server.handleCreateGame("GAME1");
        Game game = server.getGames().get("GAME1");
        game.addPlayer(new Player("PLAYER1", 1000));

        SocketChannel mockSocketChannel = mock(SocketChannel.class);

        String response = server.handleMessage("GAME1 PLAYER1 CALL", mockSocketChannel);

        assertEquals("ERROR: UNAUTHORIZED ACCESS", response, "Powinno zwrócić błąd nieautoryzowanego dostępu");
    }

    @Test
    @DisplayName("Test obsługi nieznanej komendy")
    void testHandleMessageUnknownCommand() {
        SocketChannel mockSocketChannel = mock(SocketChannel.class);

        String response = server.handleMessage("GAME1 PLAYER1 UNKNOWN", mockSocketChannel);

        assertEquals("ERROR : INVALID COMMAND", response, "Powinna zostać zwrócona informacja o nieznanej komendzie");
    }


    @Test
    @DisplayName("Test obsługi pustej wiadomości w handleRead")
    void testHandleReadEmptyMessage() throws IOException {
        SelectionKey mockKey = mock(SelectionKey.class);
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        when(mockKey.channel()).thenReturn(mockClientChannel);
        when(mockClientChannel.read(buffer)).thenReturn(0);

        assertDoesNotThrow(() -> server.handleRead(mockKey, buffer),
                "Metoda handleRead powinna obsłużyć pustą wiadomość bez wyjątku");
    }


    @Test
    @DisplayName("Test rozłączenia klienta przy bytesRead == -1")
    void testHandleReadClientDisconnect() throws IOException {
        // Przygotowanie prawdziwego kanału
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(0));
        SocketChannel clientChannel = SocketChannel.open();
        clientChannel.connect(serverChannel.getLocalAddress());

        Selector selector = Selector.open();
        clientChannel.configureBlocking(false);
        SelectionKey key = clientChannel.register(selector, SelectionKey.OP_READ);
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        // Symulacja sytuacji, gdy klient odłącza się (bytesRead == -1)
        clientChannel.close(); // Kanał jest zamykany, co symuluje disconnect

        assertDoesNotThrow(() -> server.handleRead(key, buffer),
                "Metoda handleRead powinna obsłużyć rozłączenie klienta bez wyrzucania wyjątku");

        // Weryfikacja czy kanał jest zamknięty
        assertFalse(clientChannel.isOpen(), "Kanał klienta powinien być zamknięty po rozłączeniu");

        // Sprzątanie zasobów
        serverChannel.close();
        selector.close();
    }

    @Test
    @DisplayName("Test wysyłania wiadomości do wszystkich graczy")
    void testNotifyAllPlayers() throws IOException {
        // Przygotowanie gry i graczy
        Game game = new Game("GAME1");
        Player player1 = new Player("PLAYER1", 1000);
        Player player2 = new Player("PLAYER2", 1000);
        game.addPlayer(player1);
        game.addPlayer(player2);

        // Dodanie graczy do mapy klientów z mockowanymi kanałami
        SocketChannel mockClientChannel1 = mock(SocketChannel.class);
        SocketChannel mockClientChannel2 = mock(SocketChannel.class);
        server.getGames().put("GAME1", game);
        server.getClientGameMap().put("PLAYER1", mockClientChannel1);
        server.getClientGameMap().put("PLAYER2", mockClientChannel2);

        // Wywołanie metody testowanej
        server.notifyAllPlayers(game, "Test Message");

        // Weryfikacja czy metoda write została wywołana dla obu graczy
        verify(mockClientChannel1, times(1)).write(Mockito.any(ByteBuffer.class));
        verify(mockClientChannel2, times(1)).write(Mockito.any(ByteBuffer.class));
    }

    @Test
    @DisplayName("Test obsługi spasowania gracza (FOLD)")
    void testHandleFoldInBettingRound() {
        Game game = new Game("GAME1");
        Player player = new Player("PLAYER1", 1000);
        game.setGameState(GameState.WAITING_FOR_PLAYERS);
        game.addPlayer(player);

        server.getGames().put("GAME1", game);
        game.setGameState(GameState.FIRST_BETTING_ROUND);
        String response = server.handleFold(game, "PLAYER1");

        assertTrue(game.getPlayers().get(0).isFolded(), "Gracz powinien być oznaczony jako złożony (folded)");
        assertEquals(" ", response, "Powinien zwrócić pustą odpowiedź po poprawnym spasowaniu.");
    }

    @Test
    @DisplayName("Test spasowania poza fazą licytacji")
    void testHandleFoldOutsideBettingRound() {
        Game game = new Game("GAME1");

        // Ustawienie początkowego stanu na WAITING_FOR_PLAYERS, aby dodać gracza
        game.setGameState(GameState.WAITING_FOR_PLAYERS);
        Player player = new Player("PLAYER1", 1000);
        game.addPlayer(player);

        // Zmiana stanu na EXCHANGE_PHASE (poza fazą licytacji)
        game.setGameState(GameState.EXCHANGE_PHASE);

        server.getGames().put("GAME1", game);

        // Wywołanie metody handleFold i weryfikacja odpowiedzi
        String response = server.handleFold(game, "PLAYER1");

        assertEquals("ERROR: You can only fold in betting rounds", response,
                "Powinien zwrócić błąd przy próbie spasowania poza fazą licytacji.");
        assertFalse(player.isFolded(), "Gracz nie powinien być oznaczony jako folded.");
    }

    @Test
    @DisplayName("Test obsługi opuszczenia gry przez gracza")
    void testHandleLeave() {
        Game game = new Game("GAME1");
        Player player = new Player("PLAYER1", 1000);
        game.addPlayer(player);

        server.getGames().put("GAME1", game);
        String response = server.handleLeave("GAME1", "PLAYER1");

        assertTrue(game.getPlayers().isEmpty(), "Gracz powinien zostać usunięty z gry.");
        assertEquals("PLAYER PLAYER1 LEFT GAME GAME1", response, "Powinna zostać zwrócona poprawna informacja o opuszczeniu gry.");
    }

    @Test
    @DisplayName("Test wysyłania wiadomości do klienta")
    void testSendMessage() throws IOException {
        when(mockClientChannel.write(any(ByteBuffer.class))).thenReturn(0);

        server.sendMessage(mockClientChannel, "Test Message");

        verify(mockClientChannel, times(1)).write(Mockito.any(ByteBuffer.class));
    }


    @Test
    @DisplayName("Test resetowania gry na serwerze")
    void testResetGameOnServer() {
        Game game = mock(Game.class);
        when(game.getGameID()).thenReturn("GAME1");

        server.resetGameOnServer(game);

        verify(game, times(1)).resetAfterGame();
    }

    @Test
    @DisplayName("Test handleJoin - niewystarczająca liczba tokenów")
    void testHandleJoinMissingAmount() {
        Game game = mock(Game.class);
        String response = server.handleJoin(game,  "PLAYER1", new String[]{"GAME1", "PLAYER1", "JOIN"}, mock(SocketChannel.class));

        assertEquals("ERROR: INVALID COMMAND (JOIN + amount)", response, "Powinna zostać zwrócona informacja o brakującej kwocie");
    }

    @Test
    @DisplayName("Test handleJoin - nieprawidłowa kwota")
    void testHandleJoinInvalidAmount() {
        Game game = mock(Game.class);
        String response = server.handleJoin(game, "PLAYER1", new String[]{"GAME1", "PLAYER1", "JOIN", "abc"}, mock(SocketChannel.class));

        assertEquals("ERROR: INVALID AMOUNT", response, "Powinna zostać zwrócona informacja o nieprawidłowej kwocie");
    }

    @Test
    @DisplayName("Test handleJoin - poprawne dołączenie gracza")
    void testHandleJoinSuccess() {
        Game mockGame = mock(Game.class);
        SocketChannel mockSocketChannel = mock(SocketChannel.class);

        when(mockGame.getGameState()).thenReturn(GameState.WAITING_FOR_PLAYERS);
        when(mockGame.getGameID()).thenReturn("GAME1");
        when(mockGame.getPlayers()).thenReturn(new ArrayList<>(List.of(new Player("PLAYER1", 1000))));

        String response = server.handleJoin(mockGame, "PLAYER2", new String[]{"GAME1", "PLAYER2", "JOIN", "500"}, mockSocketChannel);

        assertEquals("GAME1 PLAYER2 : PLAYER ADDED, 1/4 PLAYERS", response, "Powinna zostać zwrócona informacja o poprawnym dołączeniu gracza");
        verify(mockGame, times(1)).addPlayer(any(Player.class));
        assertTrue(server.getClientGameMap().containsKey("PLAYER2"), "Gracz powinien zostać dodany do mapy klientów");
    }

    @Test
    @DisplayName("Test handleJoin - gra już się rozpoczęła")
    void testHandleJoinGameAlreadyStarted() {
        Game mockGame = mock(Game.class);
        when(mockGame.getGameState()).thenReturn(GameState.FIRST_BETTING_ROUND);
        when(mockGame.getGameID()).thenReturn("GAME1");

        String response = server.handleJoin(mockGame, "PLAYER1", new String[]{"GAME1", "PLAYER1", "JOIN", "500"}, mock(SocketChannel.class));

        assertEquals("GAME1 ERROR: Cannot join: Game has already started", response, "Powinna zostać zwrócona informacja o rozpoczętej grze");
        verify(mockGame, never()).addPlayer(any(Player.class));
    }

    @Test
    @DisplayName("Test handleJoin - wyjątek IllegalStateException")
    void testHandleJoinIllegalStateException() {
        Game mockGame = mock(Game.class);
        when(mockGame.getGameState()).thenReturn(GameState.WAITING_FOR_PLAYERS);
        when(mockGame.getGameID()).thenReturn("GAME1");

        doThrow(new IllegalStateException("Game is full")).when(mockGame).addPlayer(any(Player.class));

        String response = server.handleJoin(mockGame, "PLAYER1", new String[]{"GAME1", "PLAYER1", "JOIN", "500"}, mock(SocketChannel.class));

        assertEquals("GAME1ERROR: Game is full", response, "Powinna zostać zwrócona informacja o wyjątkowej sytuacji");
    }

    @Test
    @DisplayName("Test handleJoin - sprawdzenie mapy klienta po dołączeniu")
    void testHandleJoinClientMap() {
        Game mockGame = mock(Game.class);
        SocketChannel mockSocketChannel = mock(SocketChannel.class);

        when(mockGame.getGameState()).thenReturn(GameState.WAITING_FOR_PLAYERS);
        when(mockGame.getGameID()).thenReturn("GAME1");

        server.handleJoin(mockGame, "PLAYER1", new String[]{"GAME1", "PLAYER1", "JOIN", "500"}, mockSocketChannel);

        assertEquals(mockSocketChannel, server.getClientGameMap().get("PLAYER1"), "Kanał gracza powinien zostać zapisany w mapie klienta");
    }


    @Test
    @DisplayName("Test handleJoin - błędne formatowanie tokenów")
    void testHandleJoinIncorrectTokens() {
        Game game = mock(Game.class);
        SocketChannel socketChannel = mock(SocketChannel.class);

        String response = server.handleJoin(game, "PLAYER1", new String[]{"GAME1", "PLAYER1"}, socketChannel);

        assertEquals("ERROR: INVALID COMMAND (JOIN + amount)", response, "Powinna zostać zwrócona informacja o błędnym formatowaniu tokenów");
    }

    @Test
    @DisplayName("Test handleReady - gracz ustawia się jako READY")
    void testHandleReadySuccess() {
        Game mockGame = mock(Game.class);
        when(mockGame.getNumberOfReadyPlayers()).thenReturn(1);
        when(mockGame.getPlayers()).thenReturn(List.of(new Player("PLAYER1", 1000), new Player("PLAYER2", 1000)));

        String response = server.handleReady(mockGame, "PLAYER1");

        assertEquals("INFO: PLAYER PLAYER1 SET TO READY. READY PLAYERS: 1/2", response,
                "Powinna zostać zwrócona informacja o ustawieniu gracza jako READY");
        verify(mockGame, times(1)).setPlayerReady("PLAYER1");
    }

    @Test
    @DisplayName("Test handleReady - wszyscy gracze są READY, gra się rozpoczyna")
    void testHandleReadyGameStart() {
        Game mockGame = mock(Game.class);
        when(mockGame.getNumberOfReadyPlayers()).thenReturn(0); // Ostatni gracz ustawia się jako READY
        when(mockGame.getPlayers()).thenReturn(List.of(new Player("PLAYER1", 1000)));

        MainServer spyServer = spy(server);
        doNothing().when(spyServer).notifyGameStartToPlayers(mockGame);

        String response = spyServer.handleReady(mockGame, "PLAYER1");

        assertEquals(" ", response, "Powinna zostać zwrócona pusta odpowiedź po rozpoczęciu gry");
        verify(mockGame, times(1)).setPlayerReady("PLAYER1");
        verify(spyServer, times(1)).notifyGameStartToPlayers(mockGame);
    }

    @Test
    @DisplayName("Test handleReady - wyjątek IllegalStateException")
    void testHandleReadyIllegalStateException() {
        Game mockGame = mock(Game.class);
        doThrow(new IllegalStateException("Player not in game")).when(mockGame).setPlayerReady("PLAYER1");

        String response = server.handleReady(mockGame, "PLAYER1");

        assertEquals("ERROR: Player not in game", response,
                "Powinna zostać zwrócona informacja o błędzie przy ustawieniu gracza jako READY");
        verify(mockGame, times(1)).setPlayerReady("PLAYER1");
    }

    @Test
    @DisplayName("Test handleReady - sprawdzenie liczby graczy READY")
    void testHandleReadyPlayersReadyCount() {
        Game mockGame = mock(Game.class);
        when(mockGame.getNumberOfReadyPlayers()).thenReturn(2);
        when(mockGame.getPlayers()).thenReturn(List.of(new Player("PLAYER1", 1000), new Player("PLAYER2", 1000), new Player("PLAYER3", 1000)));

        String response = server.handleReady(mockGame, "PLAYER3");

        assertEquals("INFO: PLAYER PLAYER3 SET TO READY. READY PLAYERS: 2/3", response,
                "Powinna zostać zwrócona informacja o aktualnej liczbie graczy READY");
        verify(mockGame, times(1)).setPlayerReady("PLAYER3");
    }

    @Test
    @DisplayName("Test handleReady - gracz nie istnieje w grze")
    void testHandleReadyNonExistentPlayer() {
        Game mockGame = mock(Game.class);
        doThrow(new IllegalStateException("Player does not exist")).when(mockGame).setPlayerReady("INVALID_PLAYER");

        String response = server.handleReady(mockGame, "INVALID_PLAYER");

        assertEquals("ERROR: Player does not exist", response,
                "Powinna zostać zwrócona informacja o próbie ustawienia READY dla nieistniejącego gracza");
        verify(mockGame, times(1)).setPlayerReady("INVALID_PLAYER");
    }

    @Test
    @DisplayName("Test handleReady - gra bez graczy")
    void testHandleReadyNoPlayers() {
        Game mockGame = mock(Game.class);
        when(mockGame.getPlayers()).thenReturn(Collections.emptyList());
        when(mockGame.getNumberOfReadyPlayers()).thenReturn(0);

        String response = server.handleReady(mockGame, "PLAYER1");

        assertEquals(" ", response,
                "Powinna zostać zwrócona informacja o READY przy pustej liście graczy");
        verify(mockGame, times(1)).setPlayerReady("PLAYER1");
    }

    @Test
    @DisplayName("Test handleReady - notifyGameStartToPlayers")
    void testHandleReadyNotifyGameStart() {
        Game mockGame = mock(Game.class);
        MainServer spyServer = spy(server);

        when(mockGame.getNumberOfReadyPlayers()).thenReturn(0);
        doNothing().when(spyServer).notifyGameStartToPlayers(mockGame);

        String response = spyServer.handleReady(mockGame, "PLAYER1");

        assertEquals(" ", response, "Powinna zostać zwrócona pusta odpowiedź po powiadomieniu graczy");
        verify(spyServer, times(1)).notifyGameStartToPlayers(mockGame);
        verify(mockGame, times(1)).setPlayerReady("PLAYER1");
    }

    @Test
    @DisplayName("Test handleCards - poprawne pobranie ręki gracza")
    void testHandleCardsSuccess() {
        Game mockGame = mock(Game.class);
        when(mockGame.getPlayerHand("PLAYER1")).thenReturn("2H 3D 5S 9C KD");
        when(mockGame.getGameID()).thenReturn("GAME1");

        String response = server.handleCards(mockGame, "PLAYER1");

        assertEquals("GAME_ID: GAME1 PLAYER_ID: PLAYER1 CARDS: \n2H 3D 5S 9C KD", response,
                "Powinna zostać zwrócona ręka gracza w formacie tekstowym.");
        verify(mockGame, times(1)).getPlayerHand("PLAYER1");
    }

    @Test
    @DisplayName("Test handleCards - wyjątek IllegalStateException")
    void testHandleCardsIllegalStateException() {
        Game mockGame = mock(Game.class);
        doThrow(new IllegalStateException("Player not found")).when(mockGame).getPlayerHand("PLAYER1");

        String response = server.handleCards(mockGame, "PLAYER1");

        assertEquals("ERROR: Player not found", response,
                "Powinna zostać zwrócona informacja o błędzie w przypadku wyjątku.");
        verify(mockGame, times(1)).getPlayerHand("PLAYER1");
    }

    @Test
    @DisplayName("Test handleCards - gracz z pustą ręką")
    void testHandleCardsEmptyHand() {
        Game mockGame = mock(Game.class);
        when(mockGame.getPlayerHand("PLAYER1")).thenReturn("");
        when(mockGame.getGameID()).thenReturn("GAME1");

        String response = server.handleCards(mockGame, "PLAYER1");

        assertEquals("GAME_ID: GAME1 PLAYER_ID: PLAYER1 CARDS: \n", response,
                "Powinna zostać zwrócona pusta ręka gracza.");
        verify(mockGame, times(1)).getPlayerHand("PLAYER1");
    }

    @Test
    @DisplayName("Test handleCards - null game ID")
    void testHandleCardsNullGameID() {
        Game mockGame = mock(Game.class);
        when(mockGame.getPlayerHand("PLAYER1")).thenReturn("2H 3D 5S 9C KD");
        when(mockGame.getGameID()).thenReturn(null);

        String response = server.handleCards(mockGame, "PLAYER1");

        assertEquals("GAME_ID: null PLAYER_ID: PLAYER1 CARDS: \n2H 3D 5S 9C KD", response,
                "Powinna zostać zwrócona ręka gracza z null jako GAME_ID.");
        verify(mockGame, times(1)).getPlayerHand("PLAYER1");
    }



    @Test
    @DisplayName("Test handleStatus - wyjątek IllegalStateException")
    void testHandleStatusIllegalStateException() {
        Game mockGame = mock(Game.class);
        when(mockGame.getPlayers()).thenThrow(new IllegalStateException("Game not initialized"));

        String response = server.handleStatus(mockGame, "PLAYER1");

        assertEquals("ERROR: Game not initialized", response,
                "Powinna zostać zwrócona wiadomość o błędzie, gdy wystąpi wyjątek IllegalStateException.");
        verify(mockGame, times(1)).getPlayers();
    }





    @Test
    @DisplayName("Test handleCall - nieautoryzowany gracz")
    void testHandleCallUnauthorized() {
        Game mockGame = mock(Game.class);
        SocketChannel mockChannel = mock(SocketChannel.class);

        MainServer spyServer = spy(server); // Tworzymy mocka serwera
        doReturn(false).when(spyServer).isPlayerAuthorized("PLAYER1", mockChannel);

        String response = spyServer.handleCall(mockGame, "PLAYER1", mockChannel);

        assertEquals("ERROR: UNAUTHORIZED ACCESS", response,
                "Powinna zostać zwrócona informacja o nieautoryzowanym dostępie.");
        verify(mockGame, never()).playerCall(anyString());
    }

    @Test
    @DisplayName("Test handleCall - poprawne wywołanie")
    void testHandleCallSuccess() {
        Game mockGame = mock(Game.class);
        SocketChannel mockChannel = mock(SocketChannel.class);

        MainServer spyServer = spy(server);
        doReturn(true).when(spyServer).isPlayerAuthorized("PLAYER1", mockChannel);

        String response = spyServer.handleCall(mockGame, "PLAYER1", mockChannel);

        assertEquals(" ", response, "Odpowiedź powinna być pusta po poprawnym wywołaniu.");
        verify(mockGame, times(1)).playerCall("PLAYER1");
        verify(spyServer, times(1)).notifyGameStateToPlayers(mockGame);
    }

    @Test
    @DisplayName("Test handleCall - IllegalStateException")
    void testHandleCallIllegalStateException() {
        Game mockGame = mock(Game.class);
        SocketChannel mockChannel = mock(SocketChannel.class);

        MainServer spyServer = spy(server);
        doReturn(true).when(spyServer).isPlayerAuthorized("PLAYER1", mockChannel);

        doThrow(new IllegalStateException("Test error")).when(mockGame).playerCall("PLAYER1");

        String response = spyServer.handleCall(mockGame, "PLAYER1", mockChannel);

        assertEquals("ERROR: Test error", response, "Powinna zostać zwrócona informacja o błędzie.");
        verify(spyServer, never()).notifyGameStateToPlayers(mockGame);
    }

    @Test
    @DisplayName("Test handleCall - processBettingRound zwraca true")
    void testHandleCallProcessBettingRound() {
        Game mockGame = mock(Game.class);
        SocketChannel mockChannel = mock(SocketChannel.class);

        MainServer spyServer = spy(server);
        doReturn(true).when(spyServer).isPlayerAuthorized("PLAYER1", mockChannel);
        when(mockGame.processBettingRound()).thenReturn(true);

        spyServer.handleCall(mockGame, "PLAYER1", mockChannel);

        verify(mockGame, times(1)).playerCall("PLAYER1");
        verify(spyServer, times(1)).notifyGameStateToPlayers(mockGame);
        verify(spyServer, times(1)).notifyBettingRoundFinished(mockGame);
    }

    @Test
    @DisplayName("Test handleCall - processBettingRound zwraca false")
    void testHandleCallProcessBettingRoundFalse() {
        Game mockGame = mock(Game.class);
        SocketChannel mockChannel = mock(SocketChannel.class);

        MainServer spyServer = spy(server);
        doReturn(true).when(spyServer).isPlayerAuthorized("PLAYER1", mockChannel);
        when(mockGame.processBettingRound()).thenReturn(false);

        spyServer.handleCall(mockGame, "PLAYER1", mockChannel);

        verify(mockGame, times(1)).playerCall("PLAYER1");
        verify(spyServer, times(1)).notifyGameStateToPlayers(mockGame);
        verify(spyServer, never()).notifyBettingRoundFinished(mockGame);
    }


    @Test
    @DisplayName("Test handleRaise - niepoprawna liczba tokenów")
    void testHandleRaiseInvalidCommand() {
        Game mockGame = mock(Game.class);
        SocketChannel mockChannel = mock(SocketChannel.class);
        String[] tokens = {"RAISE"};

        String response = server.handleRaise(mockGame, "PLAYER1", tokens, mockChannel);

        assertEquals("ERROR: INVALID COMMAND (RAISE + amount)", response,
                "Powinno zwrócić błąd dla niepoprawnej liczby tokenów.");
    }

    @Test
    @DisplayName("Test handleRaise - nieautoryzowany gracz")
    void testHandleRaiseUnauthorized() {
        Game mockGame = mock(Game.class);
        SocketChannel mockChannel = mock(SocketChannel.class);
        MainServer spyServer = spy(server);

        // Tablica z przynajmniej 4 elementami, aby przejść pierwszy warunek
        String[] tokens = {"RAISE", "some", "ignored", "100"};

        // Mockowanie braku autoryzacji
        doReturn(false).when(spyServer).isPlayerAuthorized("PLAYER1", mockChannel);

        // Wywołanie testowanej metody
        String response = spyServer.handleRaise(mockGame, "PLAYER1", tokens, mockChannel);

        // Sprawdzenie wyniku
        assertEquals("ERROR: UNAUTHORIZED ACCESS", response,
                "Powinno zwrócić błąd dla nieautoryzowanego gracza.");

        // Upewnienie się, że metoda playerRaise nie została wywołana
        verify(mockGame, never()).playerRaise(anyString(), anyInt());
    }


    @Test
    @DisplayName("Test handleRaise - niepoprawna kwota podbicia (NumberFormatException)")
    void testHandleRaiseInvalidAmount() {
        Game mockGame = mock(Game.class);
        SocketChannel mockChannel = mock(SocketChannel.class);
        MainServer spyServer = spy(server);
        String[] tokens = {"RAISE", "some", "ignored", "INVALID"};

        doReturn(true).when(spyServer).isPlayerAuthorized("PLAYER1", mockChannel);

        String response = spyServer.handleRaise(mockGame, "PLAYER1", tokens, mockChannel);

        assertEquals("ERROR: INVALID RAISE AMOUNT", response,
                "Powinno zwrócić błąd dla niepoprawnej kwoty podbicia.");
        verify(mockGame, never()).playerRaise(anyString(), anyInt());
    }

    @Test
    @DisplayName("Test handleRaise - poprawne wywołanie")
    void testHandleRaiseSuccess() {
        Game mockGame = mock(Game.class);
        SocketChannel mockChannel = mock(SocketChannel.class);
        MainServer spyServer = spy(server);
        String[] tokens = {"RAISE", "ignored", "ignored", "100"};

        doReturn(true).when(spyServer).isPlayerAuthorized("PLAYER1", mockChannel);

        String response = spyServer.handleRaise(mockGame, "PLAYER1", tokens, mockChannel);

        assertEquals(" ", response, "Powinna zostać zwrócona pusta odpowiedź po poprawnym podbiciu.");
        verify(mockGame, times(1)).playerRaise("PLAYER1", 100);
        verify(spyServer, times(1)).notifyGameStateToPlayers(mockGame);
    }

    @Test
    @DisplayName("Test handleRaise - IllegalStateException przy podbijaniu")
    void testHandleRaiseIllegalStateException() {
        Game mockGame = mock(Game.class);
        SocketChannel mockChannel = mock(SocketChannel.class);
        MainServer spyServer = spy(server);
        String[] tokens = {"RAISE", "ignored", "ignored", "100"};

        doReturn(true).when(spyServer).isPlayerAuthorized("PLAYER1", mockChannel);
        doThrow(new IllegalStateException("Raise not allowed")).when(mockGame).playerRaise("PLAYER1", 100);

        String response = spyServer.handleRaise(mockGame, "PLAYER1", tokens, mockChannel);

        assertEquals("ERROR: Raise not allowed", response,
                "Powinna zostać zwrócona informacja o błędzie z IllegalStateException.");
        verify(spyServer, never()).notifyGameStateToPlayers(mockGame);
    }

    @Test
    @DisplayName("Test handleRaise - zakończenie rundy licytacji")
    void testHandleRaiseBettingRoundFinished() {
        Game mockGame = mock(Game.class);
        SocketChannel mockChannel = mock(SocketChannel.class);
        MainServer spyServer = spy(server);
        String[] tokens = {"RAISE", "ignored", "ignored", "150"};

        doReturn(true).when(spyServer).isPlayerAuthorized("PLAYER1", mockChannel);
        when(mockGame.processBettingRound()).thenReturn(true);

        String response = spyServer.handleRaise(mockGame, "PLAYER1", tokens, mockChannel);

        assertEquals(" ", response, "Powinna zostać zwrócona pusta odpowiedź po zakończeniu rundy.");
        verify(mockGame, times(1)).playerRaise("PLAYER1", 150);
        verify(spyServer, times(1)).notifyGameStateToPlayers(mockGame);
        verify(spyServer, times(1)).notifyBettingRoundFinished(mockGame);
    }

    @Test
    @DisplayName("Test handleRaise - kontynuacja rundy licytacji")
    void testHandleRaiseBettingRoundNotFinished() {
        Game mockGame = mock(Game.class);
        SocketChannel mockChannel = mock(SocketChannel.class);
        MainServer spyServer = spy(server);
        String[] tokens = {"RAISE", "ignored", "ignored", "200"};

        doReturn(true).when(spyServer).isPlayerAuthorized("PLAYER1", mockChannel);
        when(mockGame.processBettingRound()).thenReturn(false);

        String response = spyServer.handleRaise(mockGame, "PLAYER1", tokens, mockChannel);

        assertEquals(" ", response, "Powinna zostać zwrócona pusta odpowiedź, jeśli runda nie jest zakończona.");
        verify(mockGame, times(1)).playerRaise("PLAYER1", 200);
        verify(spyServer, times(1)).notifyGameStateToPlayers(mockGame);
        verify(spyServer, never()).notifyBettingRoundFinished(mockGame);
    }

    @Test
    @DisplayName("Test handleCheck - nieautoryzowany gracz")
    void testHandleCheckUnauthorized() {
        Game mockGame = mock(Game.class);
        SocketChannel mockChannel = mock(SocketChannel.class);
        MainServer spyServer = spy(server);

        doReturn(false).when(spyServer).isPlayerAuthorized("PLAYER1", mockChannel);

        String response = spyServer.handleCheck(mockGame, "PLAYER1", mockChannel);

        assertEquals("ERROR: UNAUTHORIZED ACCESS", response,
                "Powinno zwrócić błąd dla nieautoryzowanego gracza.");
        verify(mockGame, never()).playerCheck(anyString());
    }

    @Test
    @DisplayName("Test handleCheck - poprawne wywołanie")
    void testHandleCheckSuccess() {
        Game mockGame = mock(Game.class);
        SocketChannel mockChannel = mock(SocketChannel.class);
        MainServer spyServer = spy(server);

        doReturn(true).when(spyServer).isPlayerAuthorized("PLAYER1", mockChannel);

        String response = spyServer.handleCheck(mockGame, "PLAYER1", mockChannel);

        assertEquals(" ", response, "Powinna zostać zwrócona pusta odpowiedź po poprawnym check.");
        verify(mockGame, times(1)).playerCheck("PLAYER1");
        verify(spyServer, times(1)).notifyGameStateToPlayers(mockGame);
    }

    @Test
    @DisplayName("Test handleCheck - IllegalStateException podczas check")
    void testHandleCheckIllegalStateException() {
        Game mockGame = mock(Game.class);
        SocketChannel mockChannel = mock(SocketChannel.class);
        MainServer spyServer = spy(server);

        doReturn(true).when(spyServer).isPlayerAuthorized("PLAYER1", mockChannel);
        doThrow(new IllegalStateException("Check not allowed")).when(mockGame).playerCheck("PLAYER1");

        String response = spyServer.handleCheck(mockGame, "PLAYER1", mockChannel);

        assertEquals("ERROR: Check not allowed", response,
                "Powinna zostać zwrócona informacja o błędzie z IllegalStateException.");
        verify(spyServer, never()).notifyGameStateToPlayers(mockGame);
    }

    @Test
    @DisplayName("Test handleCheck - zakończenie rundy licytacji")
    void testHandleCheckBettingRoundFinished() {
        Game mockGame = mock(Game.class);
        SocketChannel mockChannel = mock(SocketChannel.class);
        MainServer spyServer = spy(server);

        doReturn(true).when(spyServer).isPlayerAuthorized("PLAYER1", mockChannel);
        when(mockGame.processBettingRound()).thenReturn(true);

        String response = spyServer.handleCheck(mockGame, "PLAYER1", mockChannel);

        assertEquals(" ", response, "Powinna zostać zwrócona pusta odpowiedź po zakończeniu rundy.");
        verify(mockGame, times(1)).playerCheck("PLAYER1");
        verify(spyServer, times(1)).notifyGameStateToPlayers(mockGame);
        verify(spyServer, times(1)).notifyBettingRoundFinished(mockGame);
    }

    @Test
    @DisplayName("Test handleCheck - kontynuacja rundy licytacji")
    void testHandleCheckBettingRoundNotFinished() {
        Game mockGame = mock(Game.class);
        SocketChannel mockChannel = mock(SocketChannel.class);
        MainServer spyServer = spy(server);

        doReturn(true).when(spyServer).isPlayerAuthorized("PLAYER1", mockChannel);
        when(mockGame.processBettingRound()).thenReturn(false);

        String response = spyServer.handleCheck(mockGame, "PLAYER1", mockChannel);

        assertEquals(" ", response, "Powinna zostać zwrócona pusta odpowiedź, jeśli runda nie jest zakończona.");
        verify(mockGame, times(1)).playerCheck("PLAYER1");
        verify(spyServer, times(1)).notifyGameStateToPlayers(mockGame);
        verify(spyServer, never()).notifyBettingRoundFinished(mockGame);
    }

    @Test
    @DisplayName("Test handleExchange - gracz jest folded")
    void testHandleExchangePlayerFolded() {
        Game mockGame = mock(Game.class);
        SocketChannel mockChannel = mock(SocketChannel.class);
        Player mockPlayer = mock(Player.class);
        MainServer spyServer = spy(server);
        List<Integer> indexes = Arrays.asList(0, 1, 2);

        when(mockGame.getPlayerById("PLAYER1")).thenReturn(mockPlayer);
        when(mockPlayer.isFolded()).thenReturn(true);

        String response = spyServer.handleExchange(mockGame, "PLAYER1", indexes, mockChannel);

        assertEquals(" ", response, "Powinna zostać zwrócona pusta odpowiedź, jeśli gracz jest folded.");
        verify(mockGame, times(1)).exchangeCards("PLAYER1", indexes);
        verify(spyServer, times(1)).notifyPlayerExchangeIfFolded(mockGame, "PLAYER1");
    }

    @Test
    @DisplayName("Test handleExchange - nieautoryzowany gracz")
    void testHandleExchangeUnauthorizedPlayer() {
        Game mockGame = mock(Game.class);
        SocketChannel mockChannel = mock(SocketChannel.class);
        Player mockPlayer = mock(Player.class);
        MainServer spyServer = spy(server);
        List<Integer> indexes = Arrays.asList(0, 1, 2);

        when(mockGame.getPlayerById("PLAYER1")).thenReturn(mockPlayer);
        when(mockPlayer.isFolded()).thenReturn(false);
        doReturn(false).when(spyServer).isPlayerAuthorized("PLAYER1", mockChannel);

        String response = spyServer.handleExchange(mockGame, "PLAYER1", indexes, mockChannel);

        assertEquals("ERROR: UNAUTHORIZED ACCESS", response, "Powinno zwrócić błąd dla nieautoryzowanego gracza.");
        verify(mockGame, never()).exchangeCards(anyString(), anyList());
    }

    @Test
    @DisplayName("Test handleExchange - wymiana więcej niż 4 kart")
    void testHandleExchangeMoreThanFourCards() {
        Game mockGame = mock(Game.class);
        SocketChannel mockChannel = mock(SocketChannel.class);
        Player mockPlayer = mock(Player.class);
        MainServer spyServer = spy(server);
        List<Integer> indexes = Arrays.asList(0, 1, 2, 3, 4);

        when(mockGame.getPlayerById("PLAYER1")).thenReturn(mockPlayer);
        when(mockPlayer.isFolded()).thenReturn(false);
        doReturn(true).when(spyServer).isPlayerAuthorized("PLAYER1", mockChannel);

        String response = spyServer.handleExchange(mockGame, "PLAYER1", indexes, mockChannel);

        assertEquals("ERROR: You can exchange max 4 cards", response,
                "Powinno zwrócić błąd przy próbie wymiany więcej niż 4 kart.");
        verify(mockGame, never()).exchangeCards(anyString(), anyList());
    }

    @Test
    @DisplayName("Test handleExchange - niepoprawne indeksy kart")
    void testHandleExchangeInvalidIndexes() {
        Game mockGame = mock(Game.class);
        SocketChannel mockChannel = mock(SocketChannel.class);
        Player mockPlayer = mock(Player.class);
        MainServer spyServer = spy(server);
        List<Integer> indexes = Arrays.asList(0, 1, 6); // indeks 6 jest nieprawidłowy

        when(mockGame.getPlayerById("PLAYER1")).thenReturn(mockPlayer);
        when(mockPlayer.isFolded()).thenReturn(false);
        doReturn(true).when(spyServer).isPlayerAuthorized("PLAYER1", mockChannel);

        String response = spyServer.handleExchange(mockGame, "PLAYER1", indexes, mockChannel);

        assertEquals("ERROR: Invalid indexes of cards", response,
                "Powinno zwrócić błąd dla niepoprawnych indeksów kart.");
        verify(mockGame, never()).exchangeCards(anyString(), anyList());
    }

    @Test
    @DisplayName("Test handleExchange - poprawne wywołanie wymiany")
    void testHandleExchangeSuccess() {
        Game mockGame = mock(Game.class);
        SocketChannel mockChannel = mock(SocketChannel.class);
        Player mockPlayer = mock(Player.class);
        MainServer spyServer = spy(server);
        List<Integer> indexes = Arrays.asList(0, 1, 2);

        when(mockGame.getPlayerById("PLAYER1")).thenReturn(mockPlayer);
        when(mockPlayer.isFolded()).thenReturn(false);
        doReturn(true).when(spyServer).isPlayerAuthorized("PLAYER1", mockChannel);
        when(mockGame.getPlayerHand("PLAYER1")).thenReturn("HAND_AFTER_EXCHANGE");
        when(mockGame.getGameID()).thenReturn("GAME123");

        String response = spyServer.handleExchange(mockGame, "PLAYER1", indexes, mockChannel);

        assertEquals("GAME_ID: GAME123 PLAYER_ID: PLAYER1 EXCHANGED HAND: \nHAND_AFTER_EXCHANGE", response,
                "Powinna zostać zwrócona poprawna informacja o wymianie kart.");
        verify(mockGame, times(1)).exchangeCards("PLAYER1", indexes);
        verify(spyServer, times(1)).notifyPlayerExchange(mockGame, "PLAYER1");
    }

    @Test
    @DisplayName("Test handleExchange - IllegalStateException")
    void testHandleExchangeIllegalStateException() {
        Game mockGame = mock(Game.class);
        SocketChannel mockChannel = mock(SocketChannel.class);
        Player mockPlayer = mock(Player.class);
        MainServer spyServer = spy(server);
        List<Integer> indexes = Arrays.asList(0, 1, 2);

        when(mockGame.getPlayerById("PLAYER1")).thenReturn(mockPlayer);
        when(mockPlayer.isFolded()).thenReturn(false);
        doReturn(true).when(spyServer).isPlayerAuthorized("PLAYER1", mockChannel);
        doThrow(new IllegalStateException("Exchange not allowed")).when(mockGame).exchangeCards("PLAYER1", indexes);

        String response = spyServer.handleExchange(mockGame, "PLAYER1", indexes, mockChannel);

        assertEquals("ERROR: Exchange not allowed", response,
                "Powinna zostać zwrócona informacja o błędzie z IllegalStateException.");
    }

    @Test
    @DisplayName("Test sendMessage - IOException podczas wysyłania wiadomości")
    void testSendMessageIOException() throws IOException {
        // Arrange
        SocketChannel mockChannel = mock(SocketChannel.class);
        String testMessage = "Test message";

        // Symulowanie rzucenia IOException podczas wywołania write()
        doThrow(new IOException("Simulated write error")).when(mockChannel).write(any(ByteBuffer.class));


        server.sendMessage(mockChannel, testMessage);

        verify(mockChannel, times(1)).write(any(ByteBuffer.class));

    }




}


class MainServerTestCase {
    private MainServer server;
    private Game mockGame;
    private SocketChannel mockChannel1;
    private SocketChannel mockChannel2;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        server = spy(new MainServer()); // Tworzenie szpiega dla klasy MainServer
        mockGame = mock(Game.class); // Mockowanie Game
        mockChannel1 = mock(SocketChannel.class); // Mockowanie SocketChannel dla gracza 1
        mockChannel2 = mock(SocketChannel.class); // Mockowanie SocketChannel dla gracza 2

        // Tworzenie graczy
        player1 = new Player("PLAYER1", 1000);
        player2 = new Player("PLAYER2", 1000);

        // Symulowanie mapy klient -> kanał
        server.getClientGameMap().put("PLAYER1", mockChannel1);
        server.getClientGameMap().put("PLAYER2", mockChannel2);
    }

    @Test
    @DisplayName("Test notifyGameStateToPlayers - poprawne powiadomienie graczy")
    void testNotifyGameStateToPlayersSuccess() {
        when(mockGame.getGameID()).thenReturn("GAME1");
        when(mockGame.getGameState()).thenReturn(GameState.FIRST_BETTING_ROUND);
        when(mockGame.getPlayers()).thenReturn(Arrays.asList(player1, player2));
        when(mockGame.getCurrentPlayerIndex()).thenReturn(0);
        when(mockGame.getPot()).thenReturn(500);

        server.notifyGameStateToPlayers(mockGame);

        verify(server, times(1)).sendMessage((mockChannel1), anyString());
        verify(server, times(1)).sendMessage((mockChannel2), anyString());
    }

    @Test
    @DisplayName("Test notifyGameStateToPlayers - brak kanału dla jednego z graczy")
    void testNotifyGameStateToPlayersMissingChannel() {
        server.getClientGameMap().remove("PLAYER1");

        when(mockGame.getGameID()).thenReturn("GAME1");
        when(mockGame.getGameState()).thenReturn(GameState.FIRST_BETTING_ROUND);
        when(mockGame.getPlayers()).thenReturn(Arrays.asList(player1, player2));
        when(mockGame.getCurrentPlayerIndex()).thenReturn(0);
        when(mockGame.getPot()).thenReturn(500);

        server.notifyGameStateToPlayers(mockGame);

        verify(server, never()).sendMessage((mockChannel1), anyString());
        verify(server, times(1)).sendMessage((mockChannel2), anyString());
    }

    @Test
    @DisplayName("Test notifyGameStartToPlayers - poprawne powiadomienie graczy")
    void testNotifyGameStartToPlayersSuccess() {
        when(mockGame.getGameID()).thenReturn("GAME1");
        when(mockGame.getGameState()).thenReturn(GameState.FIRST_BETTING_ROUND);
        when(mockGame.getPlayers()).thenReturn(Arrays.asList(player1, player2));
        when(mockGame.getSmallBlindIndex()).thenReturn(0);
        when(mockGame.getBigBlindIndex()).thenReturn(1);
        when(mockGame.getCurrentPlayerIndex()).thenReturn(0);
        when(mockGame.getPot()).thenReturn(500);
        when(mockGame.getPlayerHand("PLAYER1")).thenReturn("AH, KH");
        when(mockGame.getPlayerHand("PLAYER2")).thenReturn("QH, JH");

        server.notifyGameStartToPlayers(mockGame);

        // Weryfikacja powiadomień dla gracza 1
        verify(server, times(1)).sendMessage((mockChannel1), contains("GAME STARTED!"));
        verify(server, times(1)).sendMessage((mockChannel1), contains("YOUR CARDS: AH, KH"));

        // Weryfikacja powiadomień dla gracza 2
        verify(server, times(1)).sendMessage((mockChannel2), contains("GAME STARTED!"));
        verify(server, times(1)).sendMessage((mockChannel2), contains("YOUR CARDS: QH, JH"));
    }

    @Test
    @DisplayName("Test notifyGameStartToPlayers - brak kanału dla jednego gracza")
    void testNotifyGameStartToPlayersMissingChannel() {
        server.getClientGameMap().remove("PLAYER2"); // Brak kanału dla PLAYER2

        when(mockGame.getGameID()).thenReturn("GAME1");
        when(mockGame.getGameState()).thenReturn(GameState.FIRST_BETTING_ROUND);
        when(mockGame.getPlayers()).thenReturn(Arrays.asList(player1, player2));
        when(mockGame.getSmallBlindIndex()).thenReturn(0);
        when(mockGame.getBigBlindIndex()).thenReturn(1);
        when(mockGame.getCurrentPlayerIndex()).thenReturn(0);
        when(mockGame.getPot()).thenReturn(500);
        when(mockGame.getPlayerHand("PLAYER1")).thenReturn("AH, KH");

        server.notifyGameStartToPlayers(mockGame);

        // PLAYER1 otrzymuje wiadomość
        verify(server, times(1)).sendMessage((mockChannel1), contains("GAME STARTED!"));
        verify(server, times(1)).sendMessage((mockChannel1), contains("YOUR CARDS: AH, KH"));

        // PLAYER2 nie otrzymuje wiadomości
        verify(server, never()).sendMessage((mockChannel2), anyString());
    }

    @Test
    @DisplayName("Test notifyPlayerExchange - poprawne powiadomienie dla graczy")
    void testNotifyPlayerExchangeSuccess() {
        when(mockGame.getGameID()).thenReturn("GAME1");
        when(mockGame.getPlayers()).thenReturn(Arrays.asList(player1, player2));
        when(mockGame.getCurrentPlayerIndex()).thenReturn(0);
        when(mockGame.getGameState()).thenReturn(GameState.EXCHANGE_PHASE);
        when(mockGame.getPot()).thenReturn(500);

        server.notifyPlayerExchange(mockGame, "PLAYER1");

        // PLAYER1 i PLAYER2 otrzymują powiadomienie
        verify(server, times(1)).sendMessage((mockChannel1), contains("PLAYER PLAYER1 FINISHED EXCHANGING CARDS"));
        verify(server, times(1)).sendMessage((mockChannel2), contains("PLAYER PLAYER1 FINISHED EXCHANGING CARDS"));
    }

    @Test
    @DisplayName("Test notifyPlayerExchange - null kanał dla jednego gracza")
    void testNotifyPlayerExchangeNullChannel() {
        when(mockGame.getGameID()).thenReturn("GAME1");
        when(mockGame.getPlayers()).thenReturn(Arrays.asList(player1, player2));
        when(mockGame.getCurrentPlayerIndex()).thenReturn(0);
        when(mockGame.getGameState()).thenReturn(GameState.EXCHANGE_PHASE);
        when(mockGame.getPot()).thenReturn(500);

        // Usuwamy kanał dla PLAYER1
        server.getClientGameMap().remove("PLAYER1");

        server.notifyPlayerExchange(mockGame, "PLAYER1");

        // PLAYER1 nie otrzymuje wiadomości
        verify(server, never()).sendMessage((mockChannel1), anyString());
        // PLAYER2 otrzymuje wiadomość
        verify(server, times(1)).sendMessage((mockChannel2), contains("PLAYER PLAYER1 FINISHED EXCHANGING CARDS"));
    }



    @Test
    @DisplayName("Test notifyGameStateToPlayers - brak kanału dla jednego gracza")
    void testNotifyGameStateToPlayersPartialChannels() {
        // Mockowanie graczy
        player1 = mock(Player.class);
        player2 = mock(Player.class);

        // Arrange
        when(player1.getPlayerId()).thenReturn("PLAYER1");
        when(player1.getCurrentBetInThisRound()).thenReturn(100);
        when(player2.getPlayerId()).thenReturn("PLAYER2");
        when(player2.getCurrentBetInThisRound()).thenReturn(200);

        when(mockGame.getPlayers()).thenReturn(Arrays.asList(player1, player2));
        when(mockGame.getGameID()).thenReturn("GAME2");
        when(mockGame.getGameState()).thenReturn(GameState.SECOND_BETTING_ROUND);
        when(mockGame.getCurrentPlayerIndex()).thenReturn(1);
        when(mockGame.getPot()).thenReturn(800);

        // Usunięcie kanału dla PLAYER2
        server.getClientGameMap().remove("PLAYER2");

        // Act
        server.notifyGameStateToPlayers(mockGame);

        verify(server, never()).sendMessage((mockChannel2), anyString());
    }



    @Test
    @DisplayName("Test notifyGameStateToPlayers - pusta lista graczy")
    void testNotifyGameStateToPlayersEmptyPlayers() {
        // Arrange
        when(mockGame.getPlayers()).thenReturn(Collections.emptyList());

        // Act
        server.notifyGameStateToPlayers(mockGame);

        // Assert
        verify(server, never()).sendMessage(any(SocketChannel.class), anyString());
    }

    @Test
    @DisplayName("Test notifyGameStartToPlayers - poprawne wysyłanie wiadomości do wszystkich graczy")
    void testNotifyGameStartToPlayersAllChannels() {
        // Arrange
        when(mockGame.getPlayers()).thenReturn(Arrays.asList(player1, player2));
        when(mockGame.getGameID()).thenReturn("GAME1");
        when(mockGame.getSmallBlindIndex()).thenReturn(0);
        when(mockGame.getBigBlindIndex()).thenReturn(1);
        when(mockGame.getCurrentPlayerIndex()).thenReturn(0);
        when(mockGame.getGameState()).thenReturn(GameState.FIRST_BETTING_ROUND);
        when(mockGame.getPot()).thenReturn(500);

        // Mockowanie metod graczy
        Player mockPlayer1 = mock(Player.class);
        Player mockPlayer2 = mock(Player.class);
        when(mockPlayer1.getPlayerId()).thenReturn("PLAYER1");
        when(mockPlayer2.getPlayerId()).thenReturn("PLAYER2");

        when(mockGame.getPlayers()).thenReturn(Arrays.asList(mockPlayer1, mockPlayer2));

        when(mockGame.getPlayerHand("PLAYER1")).thenReturn("AS, KD");
        when(mockGame.getPlayerHand("PLAYER2")).thenReturn("QH, JC");

        // Dodanie kanałów do mapy
        server.getClientGameMap().put("PLAYER1", mockChannel1);
        server.getClientGameMap().put("PLAYER2", mockChannel2);

        // Act
        server.notifyGameStartToPlayers(mockGame);

        // Assert
        String expectedMessagePlayer1 = """
        GAME STARTED!
        GAME_ID: GAME1
        SMALL_BLIND: PLAYER1
        BIG_BLIND: PLAYER2
        CURRENT_PLAYER: PLAYER1
        PHASE: FIRST_BETTING_ROUND
        POT: 500
        YOUR CARDS: AS, KD
        """.strip();

        String expectedMessagePlayer2 = """
        GAME STARTED!
        GAME_ID: GAME1
        SMALL_BLIND: PLAYER1
        BIG_BLIND: PLAYER2
        CURRENT_PLAYER: PLAYER1
        PHASE: FIRST_BETTING_ROUND
        POT: 500
        YOUR CARDS: QH, JC
        """.strip();

        verify(server, times(1)).sendMessage((mockChannel1), (expectedMessagePlayer1));
        verify(server, times(1)).sendMessage((mockChannel2), (expectedMessagePlayer2));
    }


    @Test
    @DisplayName("Test notifyGameStartToPlayers - brak kanału dla jednego gracza")
    void testNotifyGameStartToPlayersMissingChannel1() {
        // Arrange
        Player mockPlayer1 = mock(Player.class);
        Player mockPlayer2 = mock(Player.class);

        when(mockGame.getPlayers()).thenReturn(Arrays.asList(mockPlayer1, mockPlayer2));
        when(mockGame.getGameID()).thenReturn("GAME2");
        when(mockGame.getSmallBlindIndex()).thenReturn(0);
        when(mockGame.getBigBlindIndex()).thenReturn(1);
        when(mockGame.getCurrentPlayerIndex()).thenReturn(0);
        when(mockGame.getGameState()).thenReturn(GameState.FIRST_BETTING_ROUND);
        when(mockGame.getPot()).thenReturn(500);

        when(mockPlayer1.getPlayerId()).thenReturn("PLAYER1");
        when(mockPlayer2.getPlayerId()).thenReturn("PLAYER2");

        when(mockGame.getPlayerHand("PLAYER1")).thenReturn("AS, KD");

        server.getClientGameMap().put("PLAYER1", mockChannel1);
        server.getClientGameMap().remove("PLAYER2"); // Usunięcie kanału dla PLAYER2

        // Act
        server.notifyGameStartToPlayers(mockGame);

        // Assert                                 // zle
        String expectedMessagePlayer1 = """  
        GAME STARTED!
        GAME_ID: GAME2
        SMALL_BLIND: PLAYER1
        BIG_BLIND: PLAYER2
        CURRENT_PLAYER: PLAYER1
        PHASE: FIRST_BETTING_ROUND
        POT: 500
        YOUR CARDS: AS, KD
        """;

        verify(server, times(1)).sendMessage((mockChannel1), (expectedMessagePlayer1));
        verify(server, never()).sendMessage((mockChannel2), anyString());
    }


    @Test
    @DisplayName("Test notifyBettingRoundFinished - zakończenie pierwszej rundy licytacji i przejście do wymiany kart")
    void testNotifyBettingRoundFinishedFirstRound() {
        // Arrange
        Player mockPlayer1 = mock(Player.class);
        Player mockPlayer2 = mock(Player.class);

        when(mockGame.getPlayers()).thenReturn(Arrays.asList(mockPlayer1, mockPlayer2));
        when(mockGame.getGameID()).thenReturn("GAME1");
        when(mockGame.getGameState()).thenReturn(GameState.EXCHANGE_PHASE);
        when(mockGame.getCurrentPlayerIndex()).thenReturn(0);
        when(mockPlayer1.getPlayerId()).thenReturn("PLAYER1");
        when(mockPlayer2.getPlayerId()).thenReturn("PLAYER2");

        server.getClientGameMap().put("PLAYER1", mockChannel1);
        server.getClientGameMap().put("PLAYER2", mockChannel2);

        // Act
        server.notifyBettingRoundFinished(mockGame);

        // Assert
        String expectedMessage = """
        FIRST BETTING ROUND FINISHED!
        GAME_ID: GAME1
        STATE: EXCHANGE_PHASE
        PHASE: EXCHANGE CARDS
        CURRENT_PLAYER: PLAYER1
        """;

        verify(server, times(1)).sendMessage((mockChannel1), (expectedMessage));
        verify(server, times(1)).sendMessage((mockChannel2), (expectedMessage));
    }

    @Test
    @DisplayName("Test notifyBettingRoundFinished - zakończenie drugiej rundy licytacji i przejście do Showdown")
    void testNotifyBettingRoundFinishedSecondRound() {
        // Arrange
        Player mockPlayer1 = mock(Player.class);
        Player mockPlayer2 = mock(Player.class);

        when(mockGame.getPlayers()).thenReturn(Arrays.asList(mockPlayer1, mockPlayer2));
        when(mockGame.getGameID()).thenReturn("GAME2");
        when(mockGame.getGameState()).thenReturn(GameState.SHOWDOWN);
        when(mockPlayer1.getPlayerId()).thenReturn("PLAYER1");
        when(mockPlayer2.getPlayerId()).thenReturn("PLAYER2");

        server.getClientGameMap().put("PLAYER1", mockChannel1);
        server.getClientGameMap().put("PLAYER2", mockChannel2);

        // Act
        server.notifyBettingRoundFinished(mockGame);

        // Assert
        String expectedMessage = """
        SECOND BETTING ROUND FINISHED!
        GAME_ID: GAME2
        STATE: SHOWDOWN
        """;

        verify(server, times(1)).sendMessage((mockChannel1), (expectedMessage));
        verify(server, times(1)).sendMessage((mockChannel2), (expectedMessage));
    }




    @Test
    @DisplayName("Test notifyBettingRoundFinished - brak kanału dla jednego gracza")
    void testNotifyBettingRoundFinishedMissingChannel() {
        // Arrange
        Player mockPlayer1 = mock(Player.class);
        Player mockPlayer2 = mock(Player.class);

        when(mockGame.getPlayers()).thenReturn(Arrays.asList(mockPlayer1, mockPlayer2));
        when(mockGame.getGameID()).thenReturn("GAME5");
        when(mockGame.getGameState()).thenReturn(GameState.EXCHANGE_PHASE);
        when(mockGame.getCurrentPlayerIndex()).thenReturn(0);
        when(mockPlayer1.getPlayerId()).thenReturn("PLAYER1");
        when(mockPlayer2.getPlayerId()).thenReturn("PLAYER2");

        server.getClientGameMap().put("PLAYER1", mockChannel1);
        server.getClientGameMap().remove("PLAYER2"); // Usunięcie kanału dla PLAYER2

        // Act
        server.notifyBettingRoundFinished(mockGame);

        // Assert
        String expectedMessage = """
        FIRST BETTING ROUND FINISHED!
        GAME_ID: GAME5
        STATE: EXCHANGE_PHASE
        PHASE: EXCHANGE CARDS
        CURRENT_PLAYER: PLAYER1
        """.strip();

        verify(server, times(1)).sendMessage((mockChannel1), (expectedMessage));
        verify(server, never()).sendMessage((mockChannel2), anyString());
    }

    @Test
    @DisplayName("Test notifyBettingRoundFinished - pierwsza runda licytacji zakończona")
    void testNotifyBettingRoundFinishedFirstBettingRound() {
        // Arrange
        Player mockPlayer = mock(Player.class); // Upewnij się, że player jest mockiem
        when(mockPlayer.getPlayerId()).thenReturn("PLAYER1");

        when(mockGame.getPlayers()).thenReturn(List.of(mockPlayer));
        when(mockGame.getGameID()).thenReturn("GAME1");
        when(mockGame.getGameState()).thenReturn(GameState.EXCHANGE_PHASE);
        when(mockGame.getCurrentPlayerIndex()).thenReturn(0);

        server.getClientGameMap().put("PLAYER1", mockChannel1);

        // Act
        server.notifyBettingRoundFinished(mockGame);

        // Assert
        String expectedMessage = """
        FIRST BETTING ROUND FINISHED!
        GAME_ID: GAME1
        STATE: EXCHANGE_PHASE
        PHASE: EXCHANGE CARDS
        CURRENT_PLAYER: PLAYER1
        """;

        verify(server, times(1)).sendMessage((mockChannel1), (expectedMessage));
    }


    @Test
    @DisplayName("Test notifyBettingRoundFinished - druga runda licytacji zakończona")
    void testNotifyBettingRoundFinishedSecondBettingRound() {
        // Arrange
        when(mockGame.getPlayers()).thenReturn(Arrays.asList(player1));
        when(mockGame.getGameID()).thenReturn("GAME2");
        when(mockGame.getGameState()).thenReturn(GameState.SHOWDOWN);

        server.getClientGameMap().put("PLAYER1", mockChannel1);

        // Act
        server.notifyBettingRoundFinished(mockGame);

        // Assert
        String expectedMessage = """
        SECOND BETTING ROUND FINISHED!
        GAME_ID: GAME2
        STATE: SHOWDOWN
        """;

        verify(server, times(1)).sendMessage((mockChannel1), (expectedMessage));
    }


    @Test
    @DisplayName("Test sendHandPlayers - poprawne wysyłanie wiadomości do wszystkich graczy")
    void testSendHandPlayersAllChannels() {
        // Arrange
        // Zmieniając player1 i player2 na poprawnie zmockowane obiekty
        Player player11 = mock(Player.class);
        Player player21 = mock(Player.class);

        when(player11.getPlayerId()).thenReturn("PLAYER1");
        when(player21.getPlayerId()).thenReturn("PLAYER2");
        when(mockGame.getPlayers()).thenReturn(Arrays.asList(player11, player21));

        // Mockowanie wartości dla rąk i puli
        when(mockGame.getPlayerHand("PLAYER1")).thenReturn("AS, KD");
        when(mockGame.getPlayerHand("PLAYER2")).thenReturn("QH, JC");
        when(mockGame.getPot()).thenReturn(500);

        // Mapowanie kanałów graczy
        server.getClientGameMap().put("PLAYER1", mockChannel1);
        server.getClientGameMap().put("PLAYER2", mockChannel2);

        // Act
        server.sendHandPlayers(mockGame);

        // Assert
        // Oczekiwane wiadomości
        String expectedMessagePlayer1 = """
        PLAYER_ID: PLAYER1
        YOUR CARDS: AS, KD
        CURRENT POT: 500
        """;

        String expectedMessagePlayer2 = """
        PLAYER_ID: PLAYER2
        YOUR CARDS: QH, JC
        CURRENT POT: 500
        """;

        // Weryfikacja poprawnego wysyłania wiadomości
        verify(server, times(1)).sendMessage((mockChannel1), (expectedMessagePlayer1));
        verify(server, times(1)).sendMessage((mockChannel2), (expectedMessagePlayer2));
    }




    @Test
    @DisplayName("Test sendHandPlayers - brak graczy w grze")
    void testSendHandPlayersNoPlayers() {
        // Arrange
        when(mockGame.getPlayers()).thenReturn(Collections.emptyList());

        // Act
        server.sendHandPlayers(mockGame);

        // Assert
        verify(server, never()).sendMessage(any(), anyString());
    }

}

class MainServerIT {

    @Test
    @DisplayName("Test main method - verify MainServer runs successfully")
    void testMainMethod()  {
        ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));

        Thread serverThread = new Thread(() -> {
            MainServer.main(new String[]{});
        });
        serverThread.start();

        assertTrue(outputStreamCaptor.toString().contains("NONBLOCKING SERVER ON PORT: 9999"),
                "Output should confirm that server started successfully");

        serverThread.interrupt();

        System.setOut(System.out);
    }


}




