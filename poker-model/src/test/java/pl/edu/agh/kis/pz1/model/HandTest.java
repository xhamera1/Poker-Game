package pl.edu.agh.kis.pz1.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

class HandTest {

    private Hand hand;
    private Deck deck;

    @BeforeEach
    void setUp() {
        hand = new Hand();
        deck = new Deck();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(hand.getCards(), "Lista kart powinna być zainicjalizowana jako niepusta lista");
        assertTrue(hand.getCards().isEmpty(), "Nowa ręka powinna być pusta");
    }

    @Test
    void testConstructorWithInitialCards() {
        Card card1 = new Card(Rank.ACE, Suit.SPADE);
        Card card2 = new Card(Rank.KING, Suit.HEART);
        List<Card> initialCards = Arrays.asList(card1, card2);
        Hand customHand = new Hand(initialCards);

        assertEquals(2, customHand.size(), "Ręka powinna zawierać dwa początkowe karty");
        assertTrue(customHand.getCards().contains(card1), "Ręka powinna zawierać ACE of SPADE");
        assertTrue(customHand.getCards().contains(card2), "Ręka powinna zawierać KING of HEART");

        // Sprawdzenie, czy lista kart jest kopią, a nie referencją
        initialCards.set(0, new Card(Rank.TWO, Suit.CLUB));
        assertEquals(Rank.ACE, customHand.getCards().get(0).getRank(), "Zmiana w oryginalnej liście nie powinna wpływać na rękę");
    }

    @Test
    void testAddCard() {
        Card card = new Card(Rank.QUEEN, Suit.DIAMOND);
        hand.addCard(card);
        assertEquals(1, hand.size(), "Ręka powinna zawierać jedną kartę po dodaniu");
        assertTrue(hand.getCards().contains(card), "Ręka powinna zawierać dodaną kartę");
    }

    @Test
    void testRemoveCardValidIndex() {
        Card card1 = new Card(Rank.JACK, Suit.CLUB);
        Card card2 = new Card(Rank.TEN, Suit.HEART);
        hand.addCard(card1);
        hand.addCard(card2);

        Card removedCard = hand.removeCard(0);
        assertEquals(card1, removedCard, "Usunięta karta powinna być JACK of CLUB");
        assertEquals(1, hand.size(), "Ręka powinna zawierać jedną kartę po usunięciu");
        assertFalse(hand.getCards().contains(card1), "Ręka nie powinna zawierać usuniętej karty");
    }

    @Test
    void testRemoveCardInvalidIndexNegative() {
        hand.addCard(new Card(Rank.NINE, Suit.SPADE));
        Card removedCard = hand.removeCard(-1);
        assertNull(removedCard, "Usunięcie karty z ujemnym indeksem powinno zwrócić null");
        assertEquals(1, hand.size(), "Ręka powinna pozostać niezmieniona");
    }

    @Test
    void testRemoveCardInvalidIndexTooLarge() {
        hand.addCard(new Card(Rank.EIGHT, Suit.DIAMOND));
        Card removedCard = hand.removeCard(5);
        assertNull(removedCard, "Usunięcie karty z indeksem poza zakresem powinno zwrócić null");
        assertEquals(1, hand.size(), "Ręka powinna pozostać niezmieniona");
    }

    @Test
    void testGetCardsReturnsCopy() {
        Card card = new Card(Rank.SEVEN, Suit.CLUB);
        hand.addCard(card);
        List<Card> retrievedCards = hand.getCards();
        assertEquals(1, retrievedCards.size(), "Pobrana lista kart powinna zawierać jedną kartę");
        assertTrue(retrievedCards.contains(card), "Pobrana lista powinna zawierać SEVEN of CLUB");

        // Modyfikacja pobranej listy nie powinna wpływać na oryginalną rękę
        retrievedCards.remove(card);
        assertEquals(1, hand.size(), "Oryginalna ręka powinna pozostać niezmieniona po modyfikacji pobranej listy");
    }

    @Test
    void testSize() {
        assertEquals(0, hand.size(), "Nowa ręka powinna mieć rozmiar 0");
        hand.addCard(new Card(Rank.FIVE, Suit.HEART));
        assertEquals(1, hand.size(), "Ręka powinna mieć rozmiar 1 po dodaniu karty");
        hand.addCard(new Card(Rank.SIX, Suit.DIAMOND));
        assertEquals(2, hand.size(), "Ręka powinna mieć rozmiar 2 po dodaniu kolejnej karty");
        hand.removeCard(0);
        assertEquals(1, hand.size(), "Ręka powinna mieć rozmiar 1 po usunięciu karty");
    }

    @Test
    void testExchangeCardValid() {
        // Przygotowanie ręki i talii
        Card originalCard = new Card(Rank.THREE, Suit.CLUB);
        hand.addCard(originalCard);
        deck.fabryki(); // Uzupełnienie talii pełną talią
        deck.getDeck().remove(originalCard); // Usunięcie karty z talii, aby uniknąć duplikatów

        boolean result = hand.exchangeCard(0, deck);
        assertTrue(result, "Wymiana karty powinna zwrócić true dla poprawnego indeksu i dostępnej talii");
        assertEquals(1, hand.size(), "Ręka powinna nadal zawierać jedną kartę po wymianie");
        assertFalse(hand.getCards().contains(originalCard), "Oryginalna karta powinna zostać wymieniona");
    }

    @Test
    void testExchangeCardInvalidIndexNegative() {
        hand.addCard(new Card(Rank.FOUR, Suit.DIAMOND));
        boolean result = hand.exchangeCard(-2, deck);
        assertFalse(result, "Wymiana karty powinna zwrócić false dla ujemnego indeksu");
        assertEquals(1, hand.size(), "Ręka powinna pozostać niezmieniona");
    }

    @Test
    void testExchangeCardInvalidIndexTooLarge() {
        hand.addCard(new Card(Rank.FOUR, Suit.DIAMOND));
        boolean result = hand.exchangeCard(3, deck);
        assertFalse(result, "Wymiana karty powinna zwrócić false dla indeksu poza zakresem");
        assertEquals(1, hand.size(), "Ręka powinna pozostać niezmieniona");
    }

    @Test
    void testExchangeCardEmptyDeck() {
        hand.addCard(new Card(Rank.FOUR, Suit.DIAMOND));
        // Talia jest pusta, ponieważ została zainicjalizowana jako pusta i nie uzupełniliśmy jej
        // W związku z tym getRandomCard() powinien rzucić IllegalStateException
        // Jednak zgodnie z kodem Hand.exchangeCard, jeśli getRandomCard() zwraca null, to metoda zwraca false
        // W praktyce, getRandomCard() rzuci wyjątek, więc musimy obsłużyć to w teście

        // Możemy użyć try-catch, aby sprawdzić, czy metoda exchangeCard rzuca wyjątek
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            hand.exchangeCard(0, deck);
        });
        assertEquals("The deck is empty!", exception.getMessage(), "Powinien zostać rzucony IllegalStateException z odpowiednim komunikatem");
        assertEquals(1, hand.size(), "Ręka powinna pozostać niezmieniona po nieudanej wymianie");
    }

    @Test
    void testExchangeCardDeckReturnsNull() {
        // Zakładamy, że getRandomCard() może zwrócić null (chociaż w aktualnej implementacji Deck rzuca wyjątek)
        // Możemy to osiągnąć, tworząc klasę DeckMock, która nadpisuje metodę getRandomCard()
        DeckMock deckMock = new DeckMock();
        hand.addCard(new Card(Rank.FIVE, Suit.SPADE));

        boolean result = hand.exchangeCard(0, deckMock);
        assertFalse(result, "Wymiana karty powinna zwrócić false, gdy talia zwraca null");
        assertEquals(1, hand.size(), "Ręka powinna pozostać niezmieniona po nieudanej wymianie");
    }

    /**
     * Klasa mock dla Deck, która symuluje zachowanie getRandomCard() zwracające null.
     */
    private class DeckMock extends Deck {
        @Override
        public Card getRandomCard() {
            return null;
        }
    }
}
