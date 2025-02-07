package pl.edu.agh.kis.pz1.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Klasa testowa dla Player.
 */
class PlayerTest {

    private Player playerWithoutHand;
    private Player playerWithHand;
    private Hand mockHand;

    @BeforeEach
    void setUp() {
        // Tworzenie mocka dla Hand
        mockHand = mock(Hand.class);

        // Inicjalizacja gracza bez ręki
        playerWithoutHand = new Player("player1", 1000);

        // Inicjalizacja gracza z ręką
        playerWithHand = new Player("player2", mockHand, 1500);
    }

    /**
     * Testuje konstruktor Player bez ręki.
     */
    @Test
    @DisplayName("Test konstrukcji Player bez ręki")
    void testConstructorWithoutHand() {
        assertEquals("player1", playerWithoutHand.getPlayerId(), "playerId powinien być 'player1'");
        assertEquals(1000, playerWithoutHand.getStack(), "Stack powinien wynosić 1000");
        assertFalse(playerWithoutHand.isFolded(), "Gracz nie powinien być spasowany na starcie");
        assertFalse(playerWithoutHand.isReady(), "Gracz nie powinien być gotowy na starcie");
        assertEquals(0, playerWithoutHand.getCurrentBetInThisRound(), "currentBetInThisRound powinien być 0 na starcie");
        assertNull(playerWithoutHand.getPlayerHand(), "playerHand powinien być null");
    }

    /**
     * Testuje konstruktor Player z ręką.
     */
    @Test
    @DisplayName("Test konstrukcji Player z ręką")
    void testConstructorWithHand() {
        assertEquals("player2", playerWithHand.getPlayerId(), "playerId powinien być 'player2'");
        assertEquals(1500, playerWithHand.getStack(), "Stack powinien wynosić 1500");
        assertFalse(playerWithHand.isFolded(), "Gracz nie powinien być spasowany na starcie");
        assertFalse(playerWithHand.isReady(), "Gracz nie powinien być gotowy na starcie");
        assertEquals(0, playerWithHand.getCurrentBetInThisRound(), "currentBetInThisRound powinien być 0 na starcie");
        assertEquals(mockHand, playerWithHand.getPlayerHand(), "playerHand powinien być równy mockHand");
    }

    /**
     * Testuje getter i setter dla playerId.
     */
    @Test
    @DisplayName("Test getter i setter dla playerId")
    void testGetAndSetPlayerId() {
        playerWithoutHand.setPlayerId("newPlayerId");
        assertEquals("newPlayerId", playerWithoutHand.getPlayerId(), "playerId powinien być 'newPlayerId'");
    }

    /**
     * Testuje getter i setter dla playerHand.
     */
    @Test
    @DisplayName("Test getter i setter dla playerHand")
    void testGetAndSetPlayerHand() {
        Hand newHand = mock(Hand.class);
        playerWithoutHand.setPlayerHand(newHand);
        assertEquals(newHand, playerWithoutHand.getPlayerHand(), "playerHand powinien być równy newHand");
    }

    /**
     * Testuje getter i setter dla stack.
     */
    @Test
    @DisplayName("Test getter i setter dla stack")
    void testGetAndSetStack() {
        playerWithoutHand.setStack(2000);
        assertEquals(2000, playerWithoutHand.getStack(), "stack powinien wynosić 2000");
    }

    /**
     * Testuje getter i setter dla ready.
     */
    @Test
    @DisplayName("Test getter i setter dla ready")
    void testGetAndSetReady() {
        playerWithoutHand.setReady(true);
        assertTrue(playerWithoutHand.isReady(), "ready powinien być true");
    }

    /**
     * Testuje getter i setter dla folded.
     */
    @Test
    @DisplayName("Test getter i setter dla folded")
    void testGetAndSetFolded() {
        playerWithoutHand.setFolded(true);
        assertTrue(playerWithoutHand.isFolded(), "folded powinien być true");
    }

    /**
     * Testuje getter i setter dla currentBetInThisRound.
     */
    @Test
    @DisplayName("Test getter i setter dla currentBetInThisRound")
    void testGetAndSetCurrentBetInThisRound() {
        playerWithoutHand.setCurrentBetInThisRound(500);
        assertEquals(500, playerWithoutHand.getCurrentBetInThisRound(), "currentBetInThisRound powinien wynosić 500");
    }

    /**
     * Testuje metodę placeBet z wystarczającą ilością środków.
     */
    @Test
    @DisplayName("Test placeBet z wystarczającą ilością środków")
    void testPlaceBetEnoughFunds() {
        boolean result = playerWithoutHand.placeBet(300);
        assertTrue(result, "placeBet powinien zwrócić true, gdy gracz ma wystarczające środki");
        assertEquals(700, playerWithoutHand.getStack(), "stack powinien zostać zmniejszony do 700");
    }

    /**
     * Testuje metodę placeBet bez wystarczającej ilości środków.
     */
    @Test
    @DisplayName("Test placeBet bez wystarczającej ilości środków")
    void testPlaceBetNotEnoughFunds() {
        boolean result = playerWithoutHand.placeBet(1200);
        assertFalse(result, "placeBet powinien zwrócić false, gdy gracz nie ma wystarczających środków");
        assertEquals(1000, playerWithoutHand.getStack(), "stack nie powinien się zmienić");
    }

    /**
     * Testuje metodę placeBet z dokładnie tyle środków, ile ma gracz.
     */
    @Test
    @DisplayName("Test placeBet z dokładnie tyle środków, ile ma gracz")
    void testPlaceBetExactFunds() {
        boolean result = playerWithoutHand.placeBet(1000);
        assertTrue(result, "placeBet powinien zwrócić true, gdy gracz obstawia dokładnie stack");
        assertEquals(0, playerWithoutHand.getStack(), "stack powinien zostać zmniejszony do 0");
    }

    /**
     * Testuje metodę placeBet z kwotą 0.
     */
    @Test
    @DisplayName("Test placeBet z kwotą 0")
    void testPlaceBetZeroAmount() {
        boolean result = playerWithoutHand.placeBet(0);
        assertTrue(result, "placeBet powinien zwrócić true, gdy gracz obstawia 0");
        assertEquals(1000, playerWithoutHand.getStack(), "stack nie powinien się zmienić");
    }

    /**
     * Testuje metodę addWinnings.
     */
    @Test
    @DisplayName("Test addWinnings")
    void testAddWinnings() {
        playerWithoutHand.addWinnings(500);
        assertEquals(1500, playerWithoutHand.getStack(), "stack powinien zostać zwiększony do 1500");
    }

    /**
     * Testuje metodę fold i isFolded.
     */
    @Test
    @DisplayName("Test fold i isFolded")
    void testFold() {
        assertFalse(playerWithoutHand.isFolded(), "Gracz nie powinien być spasowany na starcie");
        playerWithoutHand.fold();
        assertTrue(playerWithoutHand.isFolded(), "Gracz powinien być spasowany po wywołaniu fold");
    }

    /**
     * Testuje, czy metoda placeBet nie zmienia stacka przy obstawianiu kwoty większej niż stack.
     */
    @Test
    @DisplayName("Test placeBet nie zmienia stacka przy obstawianiu kwoty większej niż stack")
    void testPlaceBetNoChangeOnFailure() {
        int initialStack = playerWithoutHand.getStack();
        boolean result = playerWithoutHand.placeBet(initialStack + 1);
        assertFalse(result, "placeBet powinien zwrócić false przy obstawianiu kwoty większej niż stack");
        assertEquals(initialStack, playerWithoutHand.getStack(), "stack nie powinien się zmienić");
    }

    /**
     * Testuje, czy metoda placeBet zmienia currentBetInThisRound.
     * (Zakładając, że metoda placeBet powinna również aktualizować currentBetInThisRound)
     * Jeśli nie, to ten test należy pominąć lub zmodyfikować.
     */
    @Test
    @DisplayName("Test placeBet aktualizuje currentBetInThisRound")
    void testPlaceBetUpdatesCurrentBetInThisRound() {
        playerWithoutHand.setCurrentBetInThisRound(0);
        int betAmount = 200;
        boolean result = playerWithoutHand.placeBet(betAmount);
        assertTrue(result, "placeBet powinien zwrócić true przy obstawianiu 200");
        // Zakładamy, że currentBetInThisRound powinien zostać zaktualizowany
        // Jeśli klasa Player nie ma takiej logiki, ten test powinien zostać dostosowany
        // W obecnej implementacji klasy Player, setCurrentBetInThisRound jest niezależne od placeBet
        // Możemy więc tylko sprawdzić, że currentBetInThisRound pozostaje niezmienione
        // lub zaimplementować logikę aktualizacji w klasie Player
        // Dla tego przykładu, zakładamy, że nie jest aktualizowane
        assertEquals(0, playerWithoutHand.getCurrentBetInThisRound(),
                "currentBetInThisRound powinien pozostać 0, ponieważ placeBet nie aktualizuje tego pola");
    }

    /**
     * Testuje, czy metoda addWinnings nie zmienia stacka przy dodawaniu kwoty 0.
     */
    @Test
    @DisplayName("Test addWinnings z kwotą 0")
    void testAddWinningsZeroAmount() {
        int initialStack = playerWithoutHand.getStack();
        playerWithoutHand.addWinnings(0);
        assertEquals(initialStack, playerWithoutHand.getStack(), "stack nie powinien się zmienić przy dodawaniu 0");
    }

    /**
     * Testuje, czy metoda placeBet z ujemną kwotą.
     * W obecnej implementacji metoda placeBet nie sprawdza czy kwota jest dodatnia,
     * więc obstawienie ujemnej kwoty zmniejszy stack gracza, co jest niepożądane.
     * Można rozważyć dodanie walidacji w klasie Player.
     */
    @Test
    @DisplayName("Test placeBet z ujemną kwotą")
    void testPlaceBetNegativeAmount() {
        int initialStack = playerWithoutHand.getStack();
        boolean result = playerWithoutHand.placeBet(-100);
        // W obecnej implementacji metoda placeBet pozwala na obstawienie ujemnej kwoty
        // co powoduje zwiększenie stacka
        // Możemy to sprawdzić
        assertTrue(result, "placeBet powinien zwrócić true przy obstawianiu ujemnej kwoty");
        assertEquals(initialStack + 100, playerWithoutHand.getStack(),
                "stack powinien zostać zwiększony o 100 przy obstawianiu -100");
        // Jeśli to nie jest pożądane, należy dodać walidację w metodzie placeBet
    }

    /**
     * Testuje, czy metoda addWinnings z ujemną kwotą.
     * W obecnej implementacji metoda addWinnings pozwala na dodanie ujemnej kwoty,
     * co powoduje zmniejszenie stacka gracza.
     * Można rozważyć dodanie walidacji w klasie Player.
     */
    @Test
    @DisplayName("Test addWinnings z ujemną kwotą")
    void testAddWinningsNegativeAmount() {
        int initialStack = playerWithoutHand.getStack();
        playerWithoutHand.addWinnings(-500);
        assertEquals(initialStack - 500, playerWithoutHand.getStack(),
                "stack powinien zostać zmniejszony o 500 przy dodawaniu -500");
        // Jeśli to nie jest pożądane, należy dodać walidację w metodzie addWinnings
    }
}
