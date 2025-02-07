package pl.edu.agh.kis.pz1.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RankTest {

    @Test
    void testGetValue() {
        assertEquals(2, Rank.TWO.getValue());
        assertEquals(3, Rank.THREE.getValue());
        assertEquals(4, Rank.FOUR.getValue());
        assertEquals(5, Rank.FIVE.getValue());
        assertEquals(6, Rank.SIX.getValue());
        assertEquals(7, Rank.SEVEN.getValue());
        assertEquals(8, Rank.EIGHT.getValue());
        assertEquals(9, Rank.NINE.getValue());
        assertEquals(10, Rank.TEN.getValue());
        assertEquals(11, Rank.JACK.getValue());
        assertEquals(12, Rank.QUEEN.getValue());
        assertEquals(13, Rank.KING.getValue());
        assertEquals(14, Rank.ACE.getValue());
    }
}
