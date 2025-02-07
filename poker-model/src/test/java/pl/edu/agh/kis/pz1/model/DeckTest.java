package pl.edu.agh.kis.pz1.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

class DeckTest {

    private Deck deck;

    @BeforeEach
    void setUp() {
        deck = new Deck();
    }

    @Test
    void testConstructor() {
        assertNotNull(deck.getDeck(), "Deck powinien być zainicjalizowany jako niepusty ArrayList");
        assertTrue(deck.getDeck().isEmpty(), "Nowa talia powinna być pusta");
    }

    @Test
    void testAddCard() {
        Card card = new Card(Rank.ACE, Suit.SPADE);
        deck.addCard(card);
        assertEquals(1, deck.getDeck().size(), "Talia powinna zawierać jedną kartę po dodaniu");
        assertTrue(deck.getDeck().contains(card), "Talia powinna zawierać dodaną kartę");
    }

    @Test
    void testAddCartToDeckWhenCardNotPresent() {
        Card card = new Card(Rank.KING, Suit.HEART);
        boolean result = deck.addCartToDeck(card);
        assertTrue(result, "Metoda powinna zwrócić true, gdy karta nie była w talii");
        assertEquals(1, deck.getDeck().size(), "Talia powinna zawierać jedną kartę po dodaniu");
    }

    @Test
    void testAddCartToDeckWhenCardPresent() {
        Card card = new Card(Rank.QUEEN, Suit.DIAMOND);
        deck.addCard(card);
        boolean result = deck.addCartToDeck(card);
        assertFalse(result, "Metoda powinna zwrócić false, gdy karta już jest w talii");
        assertEquals(1, deck.getDeck().size(), "Talia powinna zawierać tylko jedną kartę");
    }

    @Test
    void testGetRandomCardFromNonEmptyDeck() {
        Card card1 = new Card(Rank.TEN, Suit.CLUB);
        Card card2 = new Card(Rank.JACK, Suit.HEART);
        deck.addCard(card1);
        deck.addCard(card2);

        Card randomCard = deck.getRandomCard();
        assertTrue(randomCard.equals(card1) || randomCard.equals(card2), "Pobrana karta powinna być jedną z dodanych");
        assertEquals(1, deck.getDeck().size(), "Talia powinna zawierać jedną kartę po pobraniu");
        assertFalse(deck.getDeck().contains(randomCard), "Pobrana karta powinna zostać usunięta z talii");
    }

    @Test
    void testGetRandomCardFromEmptyDeck() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            deck.getRandomCard();
        });
        assertEquals("The deck is empty!", exception.getMessage(), "Powinna zostać rzucona IllegalStateException z odpowiednim komunikatem");
    }

    @Test
    void testFabryki() {
        List<Card> populatedDeck = deck.fabryki();
        assertEquals(52, populatedDeck.size(), "Talia powinna zawierać 52 karty po fabryce");

        Set<Card> uniqueCards = new HashSet<>(populatedDeck);
        assertEquals(52, uniqueCards.size(), "Talia powinna zawierać 52 unikalne karty");

        for (Rank rank : Rank.values()) {
            for (Suit suit : Suit.values()) {
                Card card = new Card(rank, suit);
                assertTrue(populatedDeck.contains(card), "Talia powinna zawierać kartę " + card);
            }
        }
    }

    @Test
    void testShuffleDeck() {
        deck.fabryki();
        List<Card> originalDeck = new ArrayList<>(deck.getDeck());
        deck.shuffle();
        List<Card> shuffledDeck = deck.getDeck();

        assertEquals(52, shuffledDeck.size(), "Po przetasowaniu talia powinna zawierać 52 karty");
        assertTrue(shuffledDeck.containsAll(originalDeck), "Talia po przetasowaniu powinna zawierać te same karty");

        // Sprawdzenie, że kolejność została zmieniona (może się zdarzyć, że kolejność się nie zmieni)
        // Aby uniknąć fałszywych negatywów, sprawdzamy, czy kolejność się różni w przynajmniej jednym elemencie
        boolean isShuffled = isDeckShuffled(originalDeck, shuffledDeck);

        // Ponieważ istnieje możliwość, że shuffle nie zmieni kolejności, powtórzymy shuffle jeśli jest taki przypadek
        if (!isShuffled) {
            deck.shuffle();
            shuffledDeck = deck.getDeck();
            isShuffled = isDeckShuffled(originalDeck, shuffledDeck);
        }

        assertTrue(isShuffled, "Talia powinna zostać przetasowana i kolejność kart powinna się zmienić");
    }

    private boolean isDeckShuffled(List<Card> originalDeck, List<Card> shuffledDeck) {
        for (int i = 0; i < originalDeck.size(); i++) {
            if (!originalDeck.get(i).equals(shuffledDeck.get(i))) {
                return true;
            }
        }
        return false;
    }

    @Test
    void testShuffleProvidedDeck() {
        List<Card> providedDeck = new ArrayList<>();
        providedDeck.add(new Card(Rank.ACE, Suit.SPADE));
        providedDeck.add(new Card(Rank.KING, Suit.HEART));
        providedDeck.add(new Card(Rank.QUEEN, Suit.DIAMOND));

        List<Card> originalDeck = new ArrayList<>(providedDeck);
        List<Card> shuffledDeck = deck.shuffle(providedDeck);

        assertEquals(originalDeck.size(), shuffledDeck.size(), "Przetasowana talia powinna mieć ten sam rozmiar");
        assertTrue(shuffledDeck.containsAll(originalDeck), "Przetasowana talia powinna zawierać te same karty");

    }

    @Test
    void testShuffleEmptyDeck() {
        List<Card> shuffledDeck = deck.shuffle();
        assertTrue(shuffledDeck.isEmpty(), "Przetasowana pusta talia powinna pozostać pusta");
    }

    @Test
    void testIsEmptyWhenDeckIsEmpty() {
        assertTrue(deck.isEmpty(), "Metoda isEmpty powinna zwracać true dla pustej talii");
    }

    @Test
    void testIsEmptyWhenDeckIsNotEmpty() {
        deck.addCard(new Card(Rank.FIVE, Suit.CLUB));
        assertFalse(deck.isEmpty(), "Metoda isEmpty powinna zwracać false dla niepustej talii");
    }
}
