package pl.edu.agh.kis.pz1.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;

/**
 * Klasa testowa dla enumeracji HandType.
 */
@TestInstance(Lifecycle.PER_CLASS)
class HandTypeTest {

    /**
     * Dostarcza pary HandType i oczekiwanych wartości.
     *
     * @return Stream of Arguments containing HandType and expectedValue.
     */
    static Stream<Arguments> provideHandTypesWithExpectedValues() {
        return Stream.of(
                Arguments.of(HandType.RoyalFlush, 10),
                Arguments.of(HandType.StraightFlush, 9),
                Arguments.of(HandType.FourOfAKind, 8),
                Arguments.of(HandType.FullHouse, 7),
                Arguments.of(HandType.Flush, 6),
                Arguments.of(HandType.Straight, 5),
                Arguments.of(HandType.ThreeOfAKind, 4),
                Arguments.of(HandType.TwoPair, 3),
                Arguments.of(HandType.OnePair, 2),
                Arguments.of(HandType.HighCard, 1)
        );
    }

    /**
     * Testuje, czy każda stała HandType ma przypisaną poprawną wartość.
     *
     * @param handType      Typ ręki pokerowej.
     * @param expectedValue Oczekiwana wartość przypisana do typu ręki.
     */
    @ParameterizedTest(name = "{0} powinien mieć wartość {1}")
    @MethodSource("provideHandTypesWithExpectedValues")
    @DisplayName("Testowanie wartości przypisanych do HandType")
    void testHandTypeValues(HandType handType, int expectedValue) {
        assertEquals(expectedValue, handType.getValue(),
                handType.name() + " powinien mieć wartość " + expectedValue);
    }

    /**
     * Testuje, czy każda stała HandType ma unikalną wartość.
     */
    @ParameterizedTest(name = "{0} i {1} mają różne wartości")
    @MethodSource("provideUniqueHandTypePairs")
    @DisplayName("Testowanie unikalności wartości HandType")
    void testUniqueHandTypeValues(HandType handType1, HandType handType2) {
        assertNotEquals(handType1.getValue(), handType2.getValue(),
                handType1.name() + " i " + handType2.name() + " powinny mieć różne wartości");
    }

    /**
     * Dostarcza wszystkie unikalne pary HandType dla testowania unikalności wartości.
     *
     * @return Stream of Arguments containing pairs of HandType.
     */
    static Stream<Arguments> provideUniqueHandTypePairs() {
        HandType[] types = HandType.values();
        Stream.Builder<Arguments> builder = Stream.builder();
        for (int i = 0; i < types.length; i++) {
            for (int j = i + 1; j < types.length; j++) {
                builder.add(Arguments.of(types[i], types[j]));
            }
        }
        return builder.build();
    }

    /**
     * Testuje, czy enumeracja HandType zawiera wszystkie oczekiwane stałe.
     */
    @org.junit.jupiter.api.Test
    @DisplayName("Testowanie kompletności enumeracji HandType")
    void testHandTypeContainsAllExpectedConstants() {
        HandType[] expectedTypes = {
                HandType.RoyalFlush, HandType.StraightFlush, HandType.FourOfAKind,
                HandType.FullHouse, HandType.Flush, HandType.Straight,
                HandType.ThreeOfAKind, HandType.TwoPair, HandType.OnePair, HandType.HighCard
        };

        HandType[] actualTypes = HandType.values();
        assertArrayEquals(expectedTypes, actualTypes, "Enumeracja HandType powinna zawierać wszystkie oczekiwane stałe");
    }

    /**
     * Testuje, czy metoda getValue() zwraca poprawne wartości dla każdej stałej HandType.
     */
    @org.junit.jupiter.api.Test
    @DisplayName("Testowanie getValue() dla każdej stałej HandType")
    void testGetValue() {
        assertEquals(10, HandType.RoyalFlush.getValue(), "RoyalFlush powinien mieć wartość 10");
        assertEquals(9, HandType.StraightFlush.getValue(), "StraightFlush powinien mieć wartość 9");
        assertEquals(8, HandType.FourOfAKind.getValue(), "FourOfAKind powinien mieć wartość 8");
        assertEquals(7, HandType.FullHouse.getValue(), "FullHouse powinien mieć wartość 7");
        assertEquals(6, HandType.Flush.getValue(), "Flush powinien mieć wartość 6");
        assertEquals(5, HandType.Straight.getValue(), "Straight powinien mieć wartość 5");
        assertEquals(4, HandType.ThreeOfAKind.getValue(), "ThreeOfAKind powinien mieć wartość 4");
        assertEquals(3, HandType.TwoPair.getValue(), "TwoPair powinien mieć wartość 3");
        assertEquals(2, HandType.OnePair.getValue(), "OnePair powinien mieć wartość 2");
        assertEquals(1, HandType.HighCard.getValue(), "HighCard powinien mieć wartość 1");
    }

    /**
     * Testuje, czy metoda getValue() nie zwraca nieoczekiwanych wartości.
     */
    @org.junit.jupiter.api.Test
    @DisplayName("Testowanie, czy getValue() zwraca wartości w oczekiwanym zakresie")
    void testGetValueDoesNotReturnUnexpectedValues() {
        for (HandType handType : HandType.values()) {
            int value = handType.getValue();
            assertTrue(value >= 1 && value <= 10,
                    handType.name() + " powinien mieć wartość między 1 a 10, a zwrócona wartość to " + value);
        }
    }
}
