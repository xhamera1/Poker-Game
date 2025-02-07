package pl.edu.agh.kis.pz1.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CardTest {

    @Test
    void testConstructorAndGetters() {
        Card card = new Card(Rank.ACE, Suit.SPADE);
        assertEquals(Rank.ACE, card.getRank());
        assertEquals(Suit.SPADE, card.getSuit());
    }

    @Test
    void testEqualsSameObject() {
        Card card = new Card(Rank.KING, Suit.HEART);
        assertEquals(card, card);
    }

    @Test
    void testEqualsEqualObjects() {
        Card card1 = new Card(Rank.KING, Suit.HEART);
        Card card2 = new Card(Rank.KING, Suit.HEART);
        assertEquals(card1, card2);
    }

    @Test
    void testEqualsDifferentRank() {
        Card card1 = new Card(Rank.KING, Suit.HEART);
        Card card2 = new Card(Rank.QUEEN, Suit.HEART);
        assertNotEquals(card1, card2);
    }

    @Test
    void testEqualsDifferentSuit() {
        Card card1 = new Card(Rank.KING, Suit.HEART);
        Card card2 = new Card(Rank.KING, Suit.DIAMOND);
        assertNotEquals(card1, card2);
    }

    @Test
    void testEqualsNull() {
        Card card = new Card(Rank.KING, Suit.HEART);
        assertNotEquals(
                null, card);
    }

    @Test
    void testEqualsDifferentClass() {
        Card card = new Card(Rank.KING, Suit.HEART);
        String other = "Not a Card";
        assertNotEquals(card, other);
    }

    @Test
    void testHashCodeEqualObjects() {
        Card card1 = new Card(Rank.QUEEN, Suit.DIAMOND);
        Card card2 = new Card(Rank.QUEEN, Suit.DIAMOND);
        assertEquals(card1.hashCode(), card2.hashCode());
    }

    @Test
    void testHashCodeDifferentObjects() {
        Card card1 = new Card(Rank.QUEEN, Suit.DIAMOND);
        Card card2 = new Card(Rank.JACK, Suit.DIAMOND);
        assertNotEquals(card1.hashCode(), card2.hashCode());
    }

    @Test
    void testToString() {
        Card card = new Card(Rank.TEN, Suit.CLUB);
        assertEquals("TEN of CLUB", card.toString());
    }
}
