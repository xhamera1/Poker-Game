package pl.edu.agh.kis.pz1.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SuitTest {

    @Test
    void testSuitValues() {
        Suit[] expectedSuits = {Suit.SPADE, Suit.HEART, Suit.DIAMOND, Suit.CLUB};
        assertArrayEquals(expectedSuits, Suit.values());
    }

    @Test
    void testSuitNames() {
        assertEquals("SPADE", Suit.SPADE.name());
        assertEquals("HEART", Suit.HEART.name());
        assertEquals("DIAMOND", Suit.DIAMOND.name());
        assertEquals("CLUB", Suit.CLUB.name());
    }
}
