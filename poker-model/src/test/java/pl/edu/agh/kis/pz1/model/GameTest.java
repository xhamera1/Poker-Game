package pl.edu.agh.kis.pz1.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Klasa testowa dla Game.
 */
@ExtendWith(MockitoExtension.class)
class GameTest {

    @Mock
    private Deck mockDeck;

    @Mock
    private HandCompare mockHandCompare;

    @InjectMocks
    private Game game;

    @BeforeEach
    void setUp() {
        // Inicjalizacja Game z mock Deck i HandCompare
        // (Zakładając, że @InjectMocks już zainicjalizował 'game' z mockDeck i mockHandCompare)

        // Przygotowanie mock Deck.getRandomCard()
        lenient().when(mockDeck.getRandomCard()).thenReturn(
                new Card(Rank.TWO, Suit.HEART),
                new Card(Rank.THREE, Suit.DIAMOND),
                new Card(Rank.FOUR, Suit.CLUB),
                new Card(Rank.FIVE, Suit.SPADE),
                new Card(Rank.SIX, Suit.HEART),
                new Card(Rank.SEVEN, Suit.DIAMOND),
                new Card(Rank.EIGHT, Suit.CLUB),
                new Card(Rank.NINE, Suit.SPADE),
                new Card(Rank.TEN, Suit.HEART),
                new Card(Rank.JACK, Suit.DIAMOND),
                new Card(Rank.QUEEN, Suit.CLUB),
                new Card(Rank.KING, Suit.SPADE),
                new Card(Rank.ACE, Suit.HEART)
        );

        // Przygotowanie pełnego decku do fabryki
        ArrayList<Card> fullDeck = new ArrayList<>();
        for (Rank r : Rank.values()) {
            for (Suit s : Suit.values()) {
                fullDeck.add(new Card(r, s));
            }
        }

        // Inne lenient stubs
        lenient().when(mockDeck.fabryki()).thenReturn(fullDeck);
        lenient().when(mockDeck.shuffle(Mockito.<ArrayList<Card>>any())).thenAnswer(invocation -> {
            ArrayList<Card> deckToShuffle = invocation.getArgument(0);
            Collections.shuffle(deckToShuffle);
            return deckToShuffle;
        });
        lenient().when(mockDeck.shuffle()).thenAnswer(invocation -> {
            ArrayList<Card> currentDeck = new ArrayList<>(fullDeck);
            Collections.shuffle(currentDeck);
            return currentDeck;
        });
    }

    // --- Testy Konstrukcji Gry --- //

    @Nested
    @DisplayName("Testy konstrukcji i inicjalizacji gry")
    class ConstructorAndInitializationTests {

        @Test
        @DisplayName("Test konstrukcji Game")
        void testGameConstructor() {
            Game newGame = new Game("game123");
            assertEquals("game123", newGame.getGameID(), "GameID powinien być poprawnie ustawiony");
            assertTrue(newGame.getPlayers().isEmpty(), "Lista graczy powinna być pusta na starcie");
            assertEquals(GameState.WAITING_FOR_PLAYERS, newGame.getGameState(), "Stan gry powinien być WAITING_FOR_PLAYERS");
            assertEquals(0, newGame.getCurrentPlayerIndex(), "currentPlayerIndex powinien być 0 na starcie");
            assertFalse(newGame.isStarted(), "Gra nie powinna być rozpoczęta na starcie");
            assertEquals(2, newGame.getMinPlayers(), "Minimalna liczba graczy powinna być 2");
            assertEquals(4, newGame.getMaxPlayers(), "Maksymalna liczba graczy powinna być 4");
            assertEquals(0, newGame.getPot(), "Pot powinien być 0 na starcie");
            assertEquals(0, newGame.getCurrentBet(), "currentBet powinien być 0 na starcie");
            assertEquals(0, newGame.getDealerIndex(), "dealerIndex powinien być 0 na starcie");
        }
    }

    // --- Testy Dodawania Graczy --- //

    @Nested
    @DisplayName("Testy dodawania graczy")
    class AddPlayerTests {

        @Test
        @DisplayName("Dodanie gracza do gry")
        void testAddPlayer() {
            Player player = new Player("player1", 1000);
            game.addPlayer(player);
            assertEquals(1, game.getPlayers().size(), "Powinien być dodany jeden gracz");
            assertTrue(game.getPlayers().contains(player), "Lista graczy powinna zawierać dodanego gracza");
        }

        @Test
        @DisplayName("Dodanie gracza z istniejącym ID powinno rzucić wyjątek")
        void testAddPlayerDuplicateID() {
            Player player1 = new Player("player1", 1000);
            Player player2 = new Player("player1", 1500);
            game.addPlayer(player1);
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                game.addPlayer(player2);
            });
            assertEquals("ERROR: Player with that ID is already in this game", exception.getMessage());
            assertEquals(1, game.getPlayers().size(), "Powinien być tylko jeden gracz w grze");
        }

        @Test
        @DisplayName("Dodanie gracza po rozpoczęciu gry powinno rzucić wyjątek")
        void testAddPlayerAfterGameStarted() {
            // Dodanie dwóch graczy
            Player player1 = new Player("player1", 1000);
            Player player2 = new Player("player2", 1000);
            game.addPlayer(player1);
            game.addPlayer(player2);

            // Zaznaczenie graczy jako gotowych
            game.setPlayerReady("player1");
            game.setPlayerReady("player2");

            // Dodanie trzeciego gracza po rozpoczęciu gry
            Player player3 = new Player("player3", 1000);
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                game.addPlayer(player3);
            });
            assertEquals("Cannot join: Game is not in a joinable state", exception.getMessage());
            assertEquals(2, game.getPlayers().size(), "Nie powinno być dodanego trzeciego gracza");
        }

        @Test
        @DisplayName("Dodanie graczy do osiągnięcia maksymalnej liczby graczy")
        void testAddPlayerMaxPlayers() {
            Player player1 = new Player("player1", 1000);
            Player player2 = new Player("player2", 1000);
            Player player3 = new Player("player3", 1000);
            Player player4 = new Player("player4", 1000);
            Player player5 = new Player("player5", 1000);

            game.addPlayer(player1);
            game.addPlayer(player2);
            game.addPlayer(player3);
            game.addPlayer(player4);

            // Próba dodania piątego gracza powinna rzucić wyjątek
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                game.addPlayer(player5);
            });
            assertEquals("Max players reached", exception.getMessage());
            assertEquals(4, game.getPlayers().size(), "Maksymalna liczba graczy to 4");
        }
    }

    // --- Testy Usuwania Graczy --- //

    @Nested
    @DisplayName("Testy usuwania graczy")
    class RemovePlayerTests {

        @Test
        @DisplayName("Usunięcie istniejącego gracza")
        void testRemoveExistingPlayer() {
            Player player1 = new Player("player1", 1000);
            Player player2 = new Player("player2", 1000);
            game.addPlayer(player1);
            game.addPlayer(player2);

            game.removePlayer("player1");
            assertEquals(1, game.getPlayers().size(), "Powinien zostać usunięty jeden gracz");
            assertFalse(game.getPlayers().contains(player1), "Gracz1 powinien być usunięty");
        }

        @Test
        @DisplayName("Usunięcie nieistniejącego gracza powinno nic nie zrobić")
        void testRemoveNonExistingPlayer() {
            Player player1 = new Player("player1", 1000);
            game.addPlayer(player1);

            game.removePlayer("player2"); // Gracz2 nie istnieje
            assertEquals(1, game.getPlayers().size(), "Nie powinno być zmiany w liczbie graczy");
            assertTrue(game.getPlayers().contains(player1), "Gracz1 powinien nadal być obecny");
        }

        @Test
        @DisplayName("Usunięcie ostatniego gracza powinno zresetować grę")
        void testRemoveLastPlayerResetsGame() {
            Player player1 = new Player("player1", 1000);
            game.addPlayer(player1);
            game.removePlayer("player1");

            assertTrue(game.getPlayers().isEmpty(), "Lista graczy powinna być pusta");
            assertEquals(GameState.WAITING_FOR_PLAYERS, game.getGameState(), "Stan gry powinien być resetowany do WAITING_FOR_PLAYERS");
            assertFalse(game.isStarted(), "Gra nie powinna być rozpoczęta po usunięciu ostatniego gracza");
        }
    }

    // --- Testy Zaznaczania Graczy jako Gotowych --- //

    @Nested
    @DisplayName("Testy zaznaczania graczy jako gotowych")
    class SetPlayerReadyTests {


        @Test
        @DisplayName("Zaznaczenie gotowego nieistniejącego gracza powinno rzucić wyjątek")
        void testSetPlayerReadyNonExistingPlayer() {
            Player player1 = new Player("player1", 1000);
            game.addPlayer(player1);

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                game.setPlayerReady("player2");
            });
            assertEquals("ERROR: NO PLAYER WITH ID : player2 IN THIS GAME", exception.getMessage());
        }

        @Test
        @DisplayName("Rozpoczęcie gry tylko po osiągnięciu minimalnej liczby graczy")
        void testStartGameWithMinPlayers() {
            Player player1 = new Player("player1", 1000);
            game.addPlayer(player1);
            game.setPlayerReady("player1");

            assertFalse(game.isStarted(), "Gra nie powinna się rozpocząć, ponieważ minimalna liczba graczy to 2");

            Player player2 = new Player("player2", 1000);
            game.addPlayer(player2);
            game.setPlayerReady("player2");

            assertTrue(game.isStarted(), "Gra powinna się rozpocząć, ponieważ osiągnięto minimalną liczbę graczy i wszyscy są gotowi");
        }
    }

    // --- Testy Rozpoczęcia Gry --- //

    @Nested
    @DisplayName("Testy rozpoczęcia gry")
    class StartGameCycleTests {


        @Test
        @DisplayName("Rozpoczęcie gry z graczami mającymi niedostateczną liczbę graczy")
        void testStartGameCycleInsufficientPlayers() {
            Player player1 = new Player("player1", 1000);
            game.addPlayer(player1);
            game.setPlayerReady("player1");

            assertFalse(game.isStarted(), "Gra nie powinna się rozpocząć, ponieważ minimalna liczba graczy to 2");
            assertEquals(GameState.WAITING_FOR_PLAYERS, game.getGameState(), "Stan gry powinien pozostać WAITING_FOR_PLAYERS");
        }
    }

    // --- Testy Akcji Graczy --- //

    @Nested
    @DisplayName("Testy akcji graczy")
    class PlayerActionTests {

        private void setTurnToPlayer(String playerId) {
            Player player = game.getPlayerById(playerId);
            int index = game.getPlayers().indexOf(player);
            if (index != -1) {
                game.setCurrentPlayerIndex(index);
            } else {
                throw new IllegalArgumentException("Player not found in the game.");
            }
        }

        @BeforeEach
        void setUpPlayers() {
            // Dodanie i przygotowanie graczy
            Player player1 = new Player("player1", 1000);
            Player player2 = new Player("player2", 1000);
            game.addPlayer(player1);
            game.addPlayer(player2);
            game.setPlayerReady("player1");
            game.setPlayerReady("player2");

            // Rozpoczęcie gry
            game.startGameCycle();
        }

        @Test
        @DisplayName("Gracz wykonuje akcję Fold")
        void testPlayerFold() {
            // Gracz1 folduje
            game.playerFold("player1");

            Player player1 = game.getPlayerById("player1");
            Player player2 = game.getPlayerById("player2");

            assertTrue(player1.isFolded(), "Gracz1 powinien być oznaczony jako spasowany");
            assertEquals(GameState.GAME_OVER, game.getGameState(), "Gra powinna się zakończyć, ponieważ został tylko jeden gracz");
            assertEquals(60, game.getPot(), "Pot powinien być sumą blindów: 20 + 40 = 60");
            assertEquals(960, player2.getStack(), "Gracz2 powinien otrzymać pot (900 + 60 = 960)");
        }

        @Test
        @DisplayName("Gracz wykonuje akcję Call")
        void testPlayerCall() {
            Player player2 = game.getPlayerById("player2");

            // Ustawienie currentPlayerIndex na gracza2 (indeks 1)
            game.setCurrentPlayerIndex(1);

            // Gracz2 wykonuje akcję Call, dodając 20 do już postawionych 40 (big blind)
            game.playerCall("player2");

            // Sprawdzenie, że gracz2 zwiększył swoją stawkę w rundzie
            assertEquals(40, player2.getCurrentBetInThisRound(), "Gracz2 powinien mieć currentBetInThisRound = 60");

            // Sprawdzenie, że pot został zaktualizowany prawidłowo
            assertEquals(80, game.getPot(), "Pot powinien wynosić 60 (40 + 40)");

            // Sprawdzenie, czy stack gracza2 został zmniejszony o 20
            assertEquals(940, player2.getStack(), "Gracz2 powinien mieć zmniejszony stack o 20");
        }


        @Test
        @DisplayName("Gracz wykonuje akcję Check")
        void testPlayerCheck() {
            Player player2 = game.getPlayerById("player2");

            // Ustawienie tury na gracza2
            setTurnToPlayer("player2");

            // Gracz2 ma już postawioną dużą stawkę (big blind)
            player2.setCurrentBetInThisRound(40);
            game.setCurrentBet(40);

            // Gracz2 wykonuje akcję Check
            game.playerCheck("player2");

            // Sprawdzenie, że currentPlayerIndex przeszedł do gracza1
            assertEquals(0, game.getCurrentPlayerIndex(), "currentPlayerIndex powinien być ustawiony na gracza1");
        }

        @Test
        @DisplayName("Gracz wykonuje akcję Raise bez wystarczających chipów")
        void testPlayerRaiseInsufficientChips() {
            // Gracz2 ma tylko 30 chipów
            Player player2 = game.getPlayerById("player2");
            player2.setStack(30);
            game.setCurrentBet(20);
            player2.setCurrentBetInThisRound(10); // Gracz2 ma już postawione 10, więc do call: 10

            // Ustawienie tury na gracza2
            setTurnToPlayer("player2");

            // Stack powinien pozostać bez zmian
            assertEquals(30, player2.getStack(), "Gracz2 powinien mieć stack bez zmian");
        }

        @Test
        @DisplayName("Gracz wykonuje akcję Check, gdy nie może")
        void testPlayerCheckCannot() {
            Player player2 = game.getPlayerById("player2");

            // Ustawienie tury na gracza2
            setTurnToPlayer("player2");

            // Gracz2 ma aktualną stawkę mniejszą niż currentBet
            player2.setCurrentBetInThisRound(20);
            game.setCurrentBet(40);

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                game.playerCheck("player2");
            });
            assertEquals("Cannot check, must call or fold.", exception.getMessage());
        }

        @Test
        @DisplayName("Gracz wykonuje akcję Call bez wystarczających chipów")
        void testPlayerCallInsufficientChips() {
            // Gracz2 ma tylko 30 chipów
            Player player2 = game.getPlayerById("player2");
            setTurnToPlayer("player2");
            player2.setStack(30);
            game.setCurrentBet(40);
            player2.setCurrentBetInThisRound(10); // Gracz2 ma już postawione 10, więc do call: 30

            // Gracz2 wykonuje akcję Call
            game.playerCall("player2");

            assertEquals(40, player2.getCurrentBetInThisRound(), "Gracz2 powinien mieć currentBetInThisRound = 40");
            assertEquals(90, game.getPot(), "");
            assertEquals(0, player2.getStack(), "Gracz2 powinien mieć stack zmniejszony do 0");
        }


        @Test
        @DisplayName("Gracz wykonuje akcję Raise z wystarczającymi chipami")
        void testPlayerRaiseSufficientChips() {
            Player player2 = game.getPlayerById("player2");

            // Ustawienie tury na gracza2
            setTurnToPlayer("player2");

            // Gracz2 ma 1000 chipów
            player2.setStack(1000);
            game.setCurrentBet(40);
            player2.setCurrentBetInThisRound(20); // Gracz2 ma już postawione 20

            // Gracz2 wykonuje Raise o 60 (total call 20 + raise 60 = 80)
            game.playerRaise("player2", 60);

            // Sprawdzenie, że gracz2 zwiększył swoją stawkę w rundzie
            assertEquals(100, player2.getCurrentBetInThisRound(), "Gracz2 powinien mieć currentBetInThisRound = 80");

            // Sprawdzenie, że pot został zaktualizowany prawidłowo
            assertEquals(140, game.getPot(), "Pot powinien wynosić 140 (20 + 40 + 80)");

            // Sprawdzenie, czy stack gracza2 został zmniejszony o 60
            assertEquals(920, player2.getStack(), "Gracz2 powinien mieć zmniejszony stack o 60");

            // Sprawdzenie, że currentBet został zaktualizowany
            assertEquals(100, game.getCurrentBet(), "currentBet powinien być ustawiony na 80");
        }


        @Test
        @DisplayName("Każdy gracz otrzymuje dokładnie 5 kart podczas rozdania")
        void testDealCards() {
            Player player100 = new Player("player101", 1000);
            Player player201 = new Player("player201", 1000);
            game.setGameState(GameState.WAITING_FOR_PLAYERS);
            game.addPlayer(player100);
            game.addPlayer(player201);

            // Zaznaczenie graczy jako gotowych
            game.setPlayerReady("player1");
            game.setPlayerReady("player2");

            // Rozpoczęcie gry
            game.startGameCycle();

            // Sprawdzenie, że każdy gracz ma 5 kart
            for (Player player : game.getPlayers()) {
                assertNotNull(player.getPlayerHand(), "Gracz powinien mieć rozdane karty");
                assertEquals(5, player.getPlayerHand().getCards().size(), "Gracz powinien mieć dokładnie 5 kart");
            }
        }
    }

    @Nested
    @DisplayName("Testy metod pomocniczych i getterów")
    class HelperAndGetterTests {

        @Test
        @DisplayName("Pobranie liczby gotowych graczy gdy żaden nie jest gotowy")
        void testGetNumberOfReadyPlayersNoneReady() {
            Player player1 = new Player("player1", 1000);
            Player player2 = new Player("player2", 1000);
            game.addPlayer(player1);
            game.addPlayer(player2);

            // Żaden gracz nie jest gotowy
            assertEquals(0, game.getNumberOfReadyPlayers(), "Powinno zwrócić 0 gotowych graczy");
        }

        @Test
        @DisplayName("Pobranie liczby gotowych graczy gdy niektórzy są gotowi")
        void testGetNumberOfReadyPlayersSomeReady() {
            Player player1 = new Player("player1", 1000);
            Player player2 = new Player("player2", 1000);
            Player player3 = new Player("player3", 1000);
            game.addPlayer(player1);
            game.addPlayer(player2);
            game.addPlayer(player3);

            game.setPlayerReady("player1");
            game.setPlayerReady("player3");

            assertEquals(2, game.getNumberOfReadyPlayers(), "Powinno zwrócić 2 gotowych graczy");
        }
    }


    @Nested
    @DisplayName("Testy metod informacyjnych")
    class InformationalMethodTests {

        @Test
        @DisplayName("Pobranie informacji o blindach po ich zamieszczeniu")
        void testGetInfoBlinds() {
            Player player1 = new Player("player1", 1000);
            Player player2 = new Player("player2", 1000);
            Player player3 = new Player("player3", 1000);
            game.addPlayer(player1);
            game.addPlayer(player2);
            game.addPlayer(player3);

            // Zaznaczenie graczy jako gotowych i rozpoczęcie gry
            game.setPlayerReady("player1");
            game.setPlayerReady("player2");
            game.setPlayerReady("player3");

            game.startGameCycle();

            String expectedInfo = "Small blind: player2, Big blind: player3. 20$ and 40$ have been placed";
            assertEquals(expectedInfo, game.getInfoBlinds(), "Informacja o blindach powinna być poprawna");
        }
    }


    @Nested
    @DisplayName("Testy metod zarządzania kartami")
    class CardManagementTests {

        @Test
        @DisplayName("Pobranie ręki gracza po rozdaniu kart")
        void testGetPlayerHandAfterDealing() {
            Player player1 = new Player("player1", 1000);
            Player player2 = new Player("player2", 1000);
            game.addPlayer(player1);
            game.addPlayer(player2);

            // Zaznaczenie graczy jako gotowych i rozpoczęcie gry
            game.setPlayerReady("player1");
            game.setPlayerReady("player2");

            game.startGameCycle();

            String handInfo1 = game.getPlayerHand("player1");
            String handInfo2 = game.getPlayerHand("player2");

            assertTrue(handInfo1.startsWith("CARDS: "), "Informacja o ręce gracza1 powinna zaczynać się od 'CARDS: '");
            assertTrue(handInfo2.startsWith("CARDS: "), "Informacja o ręce gracza2 powinna zaczynać się od 'CARDS: '");

            // Sprawdzenie, że każda ręka zawiera dokładnie 5 kart
            long cardCount1 = handInfo1.substring("CARDS: ".length()).split(", ").length;
            long cardCount2 = handInfo2.substring("CARDS: ".length()).split(", ").length;

            assertEquals(5, cardCount1, "Gracz1 powinien mieć dokładnie 5 kart w ręce");
            assertEquals(5, cardCount2, "Gracz2 powinien mieć dokładnie 5 kart w ręce");
        }


        @Test
        @DisplayName("Pobranie ręki gracza przed rozdaniem kart")
        void testGetPlayerHandBeforeDealing() {
            Player player1 = new Player("player1", 1000);
            game.addPlayer(player1);

            String handInfo = game.getPlayerHand("player1");
            assertEquals("No cards dealt yet.", handInfo, "Powinno zwrócić informację, że karty jeszcze nie zostały rozdane");
        }

        @Test
        @DisplayName("Pobranie ręki nieistniejącego gracza powinno rzucić wyjątek")
        void testGetPlayerHandNonExistingPlayer() {
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                game.getPlayerHand("playerX");
            });
            assertEquals("ERROR: Player with ID playerX not found.", exception.getMessage());
        }
    }


    @Nested
    @DisplayName("Testy wymiany kart")
    class ExchangeCardsTests {

        @BeforeEach
        void setUpExchangePhase() {
            // Dodanie i przygotowanie graczy
            Player player1 = new Player("player1", 1000);
            Player player2 = new Player("player2", 1000);
            game.addPlayer(player1);
            game.addPlayer(player2);
            game.setPlayerReady("player1");
            game.setPlayerReady("player2");

            // Rozpoczęcie gry
            game.startGameCycle();

            // Przejście do fazy wymiany
            game.setGameState(GameState.EXCHANGE_PHASE);
            game.setCurrentPlayerIndex(0);

            // Ustawienie ręki gracza1
            player1.setPlayerHand(new Hand(Arrays.asList(
                    new Card(Rank.TWO, Suit.HEART),
                    new Card(Rank.THREE, Suit.DIAMOND),
                    new Card(Rank.FOUR, Suit.CLUB),
                    new Card(Rank.FIVE, Suit.SPADE),
                    new Card(Rank.SIX, Suit.HEART)
            )));
        }


        @Test
        @DisplayName("Wymiana więcej niż 4 karty powinna rzucić wyjątek")
        void testExchangeCardsExceedsLimit() {
            List<Integer> cardIndices = Arrays.asList(0, 1, 2, 3, 4);
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                game.exchangeCards("player1", cardIndices);
            });
            assertEquals("Cannot exchange more than 4 cards", exception.getMessage());
        }

        @Test
        @DisplayName("Wymiana kart poza fazą wymiany powinna rzucić wyjątek")
        void testExchangeCardsOutOfPhase() {
            // Przejście do innej fazy
            game.setGameState(GameState.FIRST_BETTING_ROUND);

            List<Integer> cardIndices = Arrays.asList(1, 3);
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                game.exchangeCards("player1", cardIndices);
            });
            assertEquals("Not exchange phase", exception.getMessage());
        }

        @Test
        @DisplayName("Wymiana kart z nieprawidłowymi indeksami powinna rzucić wyjątek")
        void testExchangeCardsInvalidIndices() {
            // Indeks poza zakresem
            List<Integer> cardIndices = Arrays.asList(-1, 5);
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                game.exchangeCards("player1", cardIndices);
            });
            assertEquals("Invalid card index: -1", exception.getMessage());
        }

        @Test
        @DisplayName("Wymiana kart przez nieaktualnego gracza powinna rzucić wyjątek")
        void testExchangeCardsNotCurrentPlayer() {
            // Dodanie trzeciego gracza
            Player player3 = new Player("player3", 1000);
            game.setGameState(GameState.WAITING_FOR_PLAYERS);
            game.addPlayer(player3);
            game.setPlayerReady("player3");

            // Przejście do gracza3
            game.setCurrentPlayerIndex(2);
            game.setGameState(GameState.EXCHANGE_PHASE);

            List<Integer> cardIndices = Arrays.asList(0, 1);
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                game.exchangeCards("player1", cardIndices);
            });
            assertEquals("Not this player's turn", exception.getMessage());
        }
    }


    @Nested
    @DisplayName("Testy fazy wymiany")
    class ExchangePhaseTests {

        @BeforeEach
        void setUpExchangePhase() {
            // Dodanie i przygotowanie graczy
            Player player1 = new Player("player1", 1000);
            Player player2 = new Player("player2", 1000);
            game.addPlayer(player1);
            game.addPlayer(player2);
            game.setPlayerReady("player1");
            game.setPlayerReady("player2");

            // Rozpoczęcie gry
            game.startGameCycle();

            // Przejście do fazy wymiany
            game.setGameState(GameState.EXCHANGE_PHASE);
            game.setCurrentPlayerIndex(0);
        }

        @Test
        @DisplayName("Zakończenie fazy wymiany przechodzi do drugiej rundy betowania")
        void testFinishExchangePhase() {
            game.finishExchangePhase();

            assertEquals(GameState.SECOND_BETTING_ROUND, game.getGameState(), "Gra powinna przejść do drugiej rundy betowania");
            assertEquals(0, game.getCurrentBet(), "currentBet powinien zostać zresetowany do 0");

            // Sprawdzenie, że gracze mają zresetowane currentBetInThisRound
            for (Player p : game.getPlayers()) {
                assertEquals(0, p.getCurrentBetInThisRound(), "currentBetInThisRound powinien być zresetowany do 0");
            }
        }
    }



    @Nested
    @DisplayName("Testy resetowania gry")
    class ResetGameTests {

        @Test
        @DisplayName("Resetowanie gry poprzez resetGame()")
        void testResetGame() {
            Player player1 = new Player("player1", 1000);
            Player player2 = new Player("player2", 1000);
            game.addPlayer(player1);
            game.addPlayer(player2);

            // Zaznaczenie graczy jako gotowych i rozpoczęcie gry
            game.setPlayerReady("player1");
            game.setPlayerReady("player2");
            game.startGameCycle();

            // Symulacja zmiany stanu gry
            player1.setFolded(true);
            player2.setCurrentBetInThisRound(40);
            game.setPot(100);
            game.setGameState(GameState.FIRST_BETTING_ROUND);

            // Resetowanie gry
            game.resetGame();

            // Sprawdzenie, że stan gry został zresetowany
            assertEquals(GameState.WAITING_FOR_PLAYERS, game.getGameState(), "Stan gry powinien zostać zresetowany do WAITING_FOR_PLAYERS");
            assertEquals(0, game.getPot(), "Pot powinien zostać zresetowany do 0");
            assertEquals(0, game.getCurrentBet(), "currentBet powinien zostać zresetowany do 0");
            assertFalse(game.isStarted(), "Gra nie powinna być oznaczona jako rozpoczęta");

            // Sprawdzenie, że gracze zostali zresetowani
            for (Player p : game.getPlayers()) {
                assertFalse(p.isReady(), "Gracz nie powinien być oznaczony jako gotowy");
                assertFalse(p.isFolded(), "Gracz nie powinien być oznaczony jako spasowany");
                assertEquals(0, p.getCurrentBetInThisRound(), "currentBetInThisRound powinien być zresetowany do 0");
                assertNull(p.getPlayerHand(), "PlayerHand powinien być zresetowany do null");
            }
        }

        @Test
        @DisplayName("Resetowanie gry poprzez resetAfterGame()")
        void testResetAfterGame() {
            Player player1 = new Player("player1", 1000);
            Player player2 = new Player("player2", 1000);
            game.addPlayer(player1);
            game.addPlayer(player2);

            // Zaznaczenie graczy jako gotowych i rozpoczęcie gry
            game.setPlayerReady("player1");
            game.setPlayerReady("player2");
            game.startGameCycle();

            // Symulacja zakończenia gry
            game.setGameState(GameState.GAME_OVER);
            game.setPot(100);

            // Resetowanie gry
            game.resetAfterGame();

            // Sprawdzenie, że stan gry został zresetowany
            assertEquals(GameState.WAITING_FOR_PLAYERS, game.getGameState(), "Stan gry powinien zostać zresetowany do WAITING_FOR_PLAYERS");
            assertEquals(0, game.getPot(), "Pot powinien zostać zresetowany do 0");
            assertEquals(0, game.getCurrentBet(), "currentBet powinien zostać zresetowany do 0");
            assertFalse(game.isStarted(), "Gra nie powinna być oznaczona jako rozpoczęta");

            // Sprawdzenie, że gracze zostali zresetowani
            for (Player p : game.getPlayers()) {
                assertFalse(p.isReady(), "Gracz nie powinien być oznaczony jako gotowy");
                assertFalse(p.isFolded(), "Gracz nie powinien być oznaczony jako spasowany");
                assertEquals(0, p.getCurrentBetInThisRound(), "currentBetInThisRound powinien być zresetowany do 0");
                assertNull(p.getPlayerHand(), "PlayerHand powinien być zresetowany do null");
            }
        }
    }



    @Nested
    @DisplayName("Testy metod pobierania graczy")
    class PlayerRetrievalTests {

        @Test
        @DisplayName("Pobranie istniejącego gracza przez ID")
        void testGetPlayerByIdExisting() {
            Player player1 = new Player("player1", 1000);
            game.addPlayer(player1);

            Player retrieved = game.getPlayerById("player1");
            assertNotNull(retrieved, "Gracz powinien zostać znaleziony");
            assertEquals("player1", retrieved.getPlayerId(), "ID gracza powinno być zgodne");
        }

        @Test
        @DisplayName("Pobranie nieistniejącego gracza przez ID powinno rzucić wyjątek")
        void testGetPlayerByIdNonExisting() {
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                game.getPlayerById("playerX");
            });
            assertEquals("No such player", exception.getMessage());
        }

        @Test
        @DisplayName("Pobranie ostatniego gracza stojącego")
        void testGetLastPlayerStanding() {
            Player player1 = new Player("player1", 1000);
            Player player2 = new Player("player2", 1000);
            game.addPlayer(player1);
            game.addPlayer(player2);

            // Tylko player1 jest aktywny
            game.playerFold("player2");

            Player lastStanding = game.getLastPlayerStanding();
            assertNotNull(lastStanding, "Powinien być wybrany ostatni gracz stojący");
            assertEquals("player1", lastStanding.getPlayerId(), "Ostatnim graczem powinien być player1");
        }

        @Test
        @DisplayName("Pobranie ostatniego gracza stojącego gdy wszyscy foldują")
        void testGetLastPlayerStandingAllFolded() {
            Player player1 = new Player("player1", 1000);
            Player player2 = new Player("player2", 1000);
            game.addPlayer(player1);
            game.addPlayer(player2);

            // Obaj gracze foldują
            game.playerFold("player1");
            game.playerFold("player2");

            Player lastStanding = game.getLastPlayerStanding();
            assertNull(lastStanding, "Nie powinien być wybrany ostatni gracz, ponieważ wszyscy foldowali");
        }
    }



    @Nested
    @DisplayName("Testy metod liczenia aktywnych graczy")
    class ActivePlayersCountTests {

        @Test
        @DisplayName("Liczba aktywnych graczy gdy wszyscy są aktywni")
        void testGetActivePlayersCountAllActive() {
            Player player1 = new Player("player1", 1000);
            Player player2 = new Player("player2", 1000);
            Player player3 = new Player("player3", 1000);
            game.addPlayer(player1);
            game.addPlayer(player2);
            game.addPlayer(player3);

            assertEquals(3, game.getActivePlayersCount(), "Powinno zwrócić 3 aktywnych graczy");
        }

        @Test
        @DisplayName("Liczba aktywnych graczy gdy niektórzy foldują")
        void testGetActivePlayersCountSomeFolded() {
            Player player1 = new Player("player1", 1000);
            Player player2 = new Player("player2", 1000);
            Player player3 = new Player("player3", 1000);
            game.addPlayer(player1);
            game.addPlayer(player2);
            game.addPlayer(player3);

            // Gracz2 folduje
            game.playerFold("player2");

            assertEquals(2, game.getActivePlayersCount(), "Powinno zwrócić 2 aktywnych graczy");
        }

        @Test
        @DisplayName("Liczba aktywnych graczy gdy wszyscy foldują")
        void testGetActivePlayersCountAllFolded() {
            Player player1 = new Player("player1", 1000);
            Player player2 = new Player("player2", 1000);
            game.addPlayer(player1);
            game.addPlayer(player2);

            // Obaj gracze foldują
            game.playerFold("player1");
            game.playerFold("player2");

            assertEquals(0, game.getActivePlayersCount(), "Powinno zwrócić 0 aktywnych graczy");
        }
    }


    @Nested
    @DisplayName("Testy metody canStartGame()")
    class CanStartGameTests {

        @Test
        @DisplayName("CanStartGame() zwraca false gdy liczba graczy jest mniejsza niż minimalna")
        void testCanStartGameTooFewPlayers() {
            Player player1 = new Player("player1", 1000);
            game.addPlayer(player1);
            game.setPlayerReady("player1");

            assertFalse(game.canStartGame(), "CanStartGame() powinno zwrócić false przy mniej niż minimalnej liczbie graczy");
        }

        @Test
        @DisplayName("CanStartGame() zwraca false gdy niektórzy gracze nie są gotowi")
        void testCanStartGameSomeNotReady() {
            Player player1 = new Player("player1", 1000);
            Player player2 = new Player("player2", 1000);
            game.addPlayer(player1);
            game.addPlayer(player2);
            game.setPlayerReady("player1");

            assertFalse(game.canStartGame(), "CanStartGame() powinno zwrócić false gdy niektórzy gracze nie są gotowi");
        }

    }

    @Test
    @DisplayName("Test getDeck() i setDeck()")
    void testGetSetDeck() {
        Deck newDeck = new Deck();
        game.setDeck(newDeck);
        assertEquals(newDeck, game.getDeck(), "getDeck() powinno zwrócić ustawiony deck");
    }

    @Test
    @DisplayName("Test getGameState() i setGameState()")
    void testGetSetGameState() {
        game.setGameState(GameState.DEALING);
        assertEquals(GameState.DEALING, game.getGameState(), "getGameState() powinno zwrócić ustawiony stan gry");
    }

    @Test
    @DisplayName("Test getCurrentPlayerIndex() i setCurrentPlayerIndex()")
    void testGetSetCurrentPlayerIndex() {
        int newIndex = 2;
        game.setCurrentPlayerIndex(newIndex);
        assertEquals(newIndex, game.getCurrentPlayerIndex(), "getCurrentPlayerIndex() powinno zwrócić ustawiony indeks gracza");
    }

    @Test
    @DisplayName("Test getHandCompare() i setHandCompare()")
    void testGetSetHandCompare() {
        HandCompare newHandCompare = new HandCompare();
        game.setHandCompare(newHandCompare);
        assertEquals(newHandCompare, game.getHandCompare(), "getHandCompare() powinno zwrócić ustawiony HandCompare");
    }

    @Test
    @DisplayName("Test getMinPlayers() i setMinPlayers()")
    void testGetSetMinPlayers() {
        int newMinPlayers = 3;
        game.setMinPlayers(newMinPlayers);
        assertEquals(newMinPlayers, game.getMinPlayers(), "getMinPlayers() powinno zwrócić ustawioną minimalną liczbę graczy");
    }

    @Test
    @DisplayName("Test getMaxPlayers() i setMaxPlayers()")
    void testGetSetMaxPlayers() {
        int newMaxPlayers = 5;
        game.setMaxPlayers(newMaxPlayers);
        assertEquals(newMaxPlayers, game.getMaxPlayers(), "getMaxPlayers() powinno zwrócić ustawioną maksymalną liczbę graczy");
    }

    @Test
    @DisplayName("Test isStarted() i setStarted()")
    void testIsSetStarted() {
        game.setStarted(true);
        assertTrue(game.isStarted(), "isStarted() powinno zwrócić true po ustawieniu na true");
        game.setStarted(false);
        assertFalse(game.isStarted(), "isStarted() powinno zwrócić false po ustawieniu na false");
    }

    @Test
    @DisplayName("Test getDealerIndex() i setDealerIndex()")
    void testGetSetDealerIndex() {
        int newDealerIndex = 1;
        game.setDealerIndex(newDealerIndex);
        assertEquals(newDealerIndex, game.getDealerIndex(), "getDealerIndex() powinno zwrócić ustawiony indeks dealer'a");
    }

    @Test
    @DisplayName("Test getSmallBlindIndex() i setSmallBlindIndex()")
    void testGetSetSmallBlindIndex() {
        int newSmallBlindIndex = 3;
        game.setSmallBlindIndex(newSmallBlindIndex);
        assertEquals(newSmallBlindIndex, game.getSmallBlindIndex(), "getSmallBlindIndex() powinno zwrócić ustawiony indeks small blind");
    }

    @Test
    @DisplayName("Test getBigBlindIndex() i setBigBlindIndex()")
    void testGetSetBigBlindIndex() {
        int newBigBlindIndex = 4;
        game.setBigBlindIndex(newBigBlindIndex);
        assertEquals(newBigBlindIndex, game.getBigBlindIndex(), "getBigBlindIndex() powinno zwrócić ustawiony indeks big blind");
    }

    @Test
    @DisplayName("Test getSmallBlindAmount() i setSmallBlindAmount()")
    void testGetSetSmallBlindAmount() {
        int newSmallBlindAmount = 25;
        game.setSmallBlindAmount(newSmallBlindAmount);
        assertEquals(newSmallBlindAmount, game.getSmallBlindAmount(), "getSmallBlindAmount() powinno zwrócić ustawioną kwotę small blind");
    }

    @Test
    @DisplayName("Test getBigBlindAmount() i setBigBlindAmount()")
    void testGetSetBigBlindAmount() {
        int newBigBlindAmount = 50;
        game.setBigBlindAmount(newBigBlindAmount);
        assertEquals(newBigBlindAmount, game.getBigBlindAmount(), "getBigBlindAmount() powinno zwrócić ustawioną kwotę big blind");
    }

    @Test
    @DisplayName("Test getPot() i setPot()")
    void testGetSetPot() {
        int newPot = 200;
        game.setPot(newPot);
        assertEquals(newPot, game.getPot(), "getPot() powinno zwrócić ustawiony pot");
    }

    @Test
    @DisplayName("Test getCurrentBet() i setCurrentBet()")
    void testGetSetCurrentBet() {
        int newCurrentBet = 100;
        game.setCurrentBet(newCurrentBet);
        assertEquals(newCurrentBet, game.getCurrentBet(), "getCurrentBet() powinno zwrócić ustawioną aktualną stawkę");
    }

    @Test
    @DisplayName("Test getPlayersRemaining() i setPlayersRemaining()")
    void testGetSetPlayersRemaining() {
        int newPlayersRemaining = 3;
        game.setPlayersRemaining(newPlayersRemaining);
        assertEquals(newPlayersRemaining, game.getPlayersRemaining(), "getPlayersRemaining() powinno zwrócić ustawioną liczbę graczy pozostających");
    }





}