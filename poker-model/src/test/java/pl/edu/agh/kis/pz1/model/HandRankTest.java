package pl.edu.agh.kis.pz1.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Klasa testowa dla HandRank.
 */
class HandRankTest {

    @Test
    void testConstructorAndGetters() {
        HandType type = HandType.RoyalFlush;
        Rank kicker1 = Rank.TEN;
        Rank kicker2 = Rank.JACK;
        Rank kicker3 = Rank.QUEEN;
        Rank kicker4 = Rank.KING;
        Rank kicker5 = Rank.ACE;
        HandRank handRank = new HandRank(type, Arrays.asList(kicker1, kicker2, kicker3, kicker4, kicker5));

        assertEquals(type, handRank.getType(), "Typ powinien być RoyalFlush");
        assertEquals(Arrays.asList(kicker1, kicker2, kicker3, kicker4, kicker5), handRank.getKickers(), "Kickerzy powinni być [TEN, JACK, QUEEN, KING, ACE]");
    }

    @Test
    void testEquals_SameObject() {
        HandRank handRank = new HandRank(HandType.Flush, Arrays.asList(Rank.KING, Rank.QUEEN, Rank.JACK, Rank.NINE, Rank.FIVE));
        assertEquals(handRank, handRank, "Obiekt powinien być równy samemu sobie");
    }

    @Test
    void testEquals_EqualObjects() {
        HandRank handRank1 = new HandRank(HandType.Straight, Arrays.asList(Rank.FIVE, Rank.SIX, Rank.SEVEN, Rank.EIGHT, Rank.NINE));
        HandRank handRank2 = new HandRank(HandType.Straight, Arrays.asList(Rank.FIVE, Rank.SIX, Rank.SEVEN, Rank.EIGHT, Rank.NINE));

        assertEquals(handRank1, handRank2, "Obiekty z tymi samymi wartościami powinny być równe");
        assertEquals(handRank1.hashCode(), handRank2.hashCode(), "Równe obiekty powinny mieć taki sam hashCode");
    }

    @Test
    void testEquals_DifferentType() {
        HandRank handRank1 = new HandRank(HandType.Flush, Arrays.asList(Rank.KING, Rank.JACK, Rank.NINE, Rank.FIVE, Rank.TWO));
        HandRank handRank2 = new HandRank(HandType.Straight, Arrays.asList(Rank.KING, Rank.JACK, Rank.NINE, Rank.FIVE, Rank.TWO));

        assertNotEquals(handRank1, handRank2, "Obiekty z różnymi typami nie powinny być równe");
    }

    @Test
    void testEquals_DifferentKickers() {
        HandRank handRank1 = new HandRank(HandType.Flush, Arrays.asList(Rank.KING, Rank.JACK, Rank.NINE, Rank.FIVE, Rank.TWO));
        HandRank handRank2 = new HandRank(HandType.Flush, Arrays.asList(Rank.KING, Rank.JACK, Rank.NINE, Rank.FIVE, Rank.THREE));

        assertNotEquals(handRank1, handRank2, "Obiekty z różnymi kickerami nie powinny być równe");
    }

    @Test
    void testEquals_Null() {
        HandRank handRank = new HandRank(HandType.Flush, Arrays.asList(Rank.KING, Rank.JACK, Rank.NINE, Rank.FIVE, Rank.TWO));
        assertNotEquals(
                null, handRank, "Obiekt nie powinien być równy null");
    }

    @Test
    void testEquals_DifferentClass() {
        HandRank handRank = new HandRank(HandType.Flush, Arrays.asList(Rank.KING, Rank.JACK, Rank.NINE, Rank.FIVE, Rank.TWO));
        String otherObject = "Some String";
        assertNotEquals(handRank, otherObject, "Obiekt nie powinien być równy obiektowi innej klasy");
    }

    @Test
    void testHashCode_EqualObjects() {
        HandRank handRank1 = new HandRank(HandType.FullHouse, Arrays.asList(Rank.TEN, Rank.ACE));
        HandRank handRank2 = new HandRank(HandType.FullHouse, Arrays.asList(Rank.TEN, Rank.ACE));

        assertEquals(handRank1.hashCode(), handRank2.hashCode(), "Równe obiekty powinny mieć taki sam hashCode");
    }

    @Test
    void testHashCode_DifferentObjects() {
        HandRank handRank1 = new HandRank(HandType.FullHouse, Arrays.asList(Rank.TEN, Rank.ACE));
        HandRank handRank2 = new HandRank(HandType.FullHouse, Arrays.asList(Rank.NINE, Rank.ACE));

        assertNotEquals(handRank1.hashCode(), handRank2.hashCode(), "Różne obiekty powinny mieć różne hashCode");
    }

    @Test
    void testImmutableKickers() {
        List<Rank> kickers = Arrays.asList(Rank.KING, Rank.QUEEN, Rank.JACK, Rank.NINE, Rank.FIVE);
        HandRank handRank = new HandRank(HandType.Flush, kickers);
        List<Rank> immutableKickers = handRank.getKickers();

        assertThrows(UnsupportedOperationException.class, () -> immutableKickers.add(Rank.TWO), "Lista kickerów powinna być niezmienna");
    }
}
