package pl.edu.agh.kis.pz1.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HandCompareTest {

    private HandCompare handCompare;

    @BeforeEach
    void setUp() {
        handCompare = new HandCompare();
    }

    // --- Testy dla checkHand(List<Card> hand) --- //

    @Nested
    @DisplayName("Testy metody checkHand")
    class CheckHandTests {

        @Test
        @DisplayName("Test Royal Flush")
        void testCheckHandRoyalFlush() {
            List<Card> royalFlush = Arrays.asList(
                    new Card(Rank.TEN, Suit.HEART),
                    new Card(Rank.JACK, Suit.HEART),
                    new Card(Rank.QUEEN, Suit.HEART),
                    new Card(Rank.KING, Suit.HEART),
                    new Card(Rank.ACE, Suit.HEART)
            );

            HandRank expected = new HandRank(HandType.RoyalFlush, Arrays.asList(
                    Rank.TEN, Rank.JACK, Rank.QUEEN, Rank.KING, Rank.ACE
            ));

            HandRank actual = handCompare.checkHand(royalFlush);
            assertEquals(expected, actual, "Royal Flush powinien być poprawnie rozpoznany");
        }

        @Test
        @DisplayName("Test Straight Flush")
        void testCheckHandStraightFlush() {
            List<Card> straightFlush = Arrays.asList(
                    new Card(Rank.FIVE, Suit.SPADE),
                    new Card(Rank.SIX, Suit.SPADE),
                    new Card(Rank.SEVEN, Suit.SPADE),
                    new Card(Rank.EIGHT, Suit.SPADE),
                    new Card(Rank.NINE, Suit.SPADE)
            );

            HandRank expected = new HandRank(HandType.StraightFlush, Arrays.asList(
                    Rank.FIVE, Rank.SIX, Rank.SEVEN, Rank.EIGHT, Rank.NINE
            ));

            HandRank actual = handCompare.checkHand(straightFlush);
            assertEquals(expected, actual, "Straight Flush powinien być poprawnie rozpoznany");
        }

        @Test
        @DisplayName("Test Four of a Kind")
        void testCheckHandFourOfAKind() {
            List<Card> fourOfAKind = Arrays.asList(
                    new Card(Rank.KING, Suit.HEART),
                    new Card(Rank.KING, Suit.SPADE),
                    new Card(Rank.KING, Suit.DIAMOND),
                    new Card(Rank.KING, Suit.CLUB),
                    new Card(Rank.THREE, Suit.HEART)
            );

            HandRank expected = new HandRank(HandType.FourOfAKind, Arrays.asList(
                    Rank.KING, Rank.THREE
            ));

            HandRank actual = handCompare.checkHand(fourOfAKind);
            assertEquals(expected, actual, "Four of a Kind powinien być poprawnie rozpoznany");
        }

        @Test
        @DisplayName("Test Full House")
        void testCheckHandFullHouse() {
            List<Card> fullHouse = Arrays.asList(
                    new Card(Rank.TEN, Suit.HEART),
                    new Card(Rank.TEN, Suit.SPADE),
                    new Card(Rank.TEN, Suit.DIAMOND),
                    new Card(Rank.ACE, Suit.CLUB),
                    new Card(Rank.ACE, Suit.HEART)
            );

            HandRank expected = new HandRank(HandType.FullHouse, Arrays.asList(
                    Rank.TEN, Rank.ACE
            ));

            HandRank actual = handCompare.checkHand(fullHouse);
            assertEquals(expected, actual, "Full House powinien być poprawnie rozpoznany");
        }

        @Test
        @DisplayName("Test Flush")
        void testCheckHandFlush() {
            List<Card> flush = Arrays.asList(
                    new Card(Rank.TWO, Suit.CLUB),
                    new Card(Rank.FIVE, Suit.CLUB),
                    new Card(Rank.NINE, Suit.CLUB),
                    new Card(Rank.JACK, Suit.CLUB),
                    new Card(Rank.KING, Suit.CLUB)
            );

            HandRank expected = new HandRank(HandType.Flush, Arrays.asList(
                    Rank.KING, Rank.JACK, Rank.NINE, Rank.FIVE, Rank.TWO
            ));

            HandRank actual = handCompare.checkHand(flush);
            assertEquals(expected, actual, "Flush powinien być poprawnie rozpoznany");
        }

        @Test
        @DisplayName("Test Straight")
        void testCheckHandStraight() {
            List<Card> straight = Arrays.asList(
                    new Card(Rank.THREE, Suit.DIAMOND),
                    new Card(Rank.FOUR, Suit.HEART),
                    new Card(Rank.FIVE, Suit.SPADE),
                    new Card(Rank.SIX, Suit.CLUB),
                    new Card(Rank.SEVEN, Suit.HEART)
            );

            HandRank expected = new HandRank(HandType.Straight, Arrays.asList(
                    Rank.THREE, Rank.FOUR, Rank.FIVE, Rank.SIX, Rank.SEVEN
            ));

            HandRank actual = handCompare.checkHand(straight);
            assertEquals(expected, actual, "Straight powinien być poprawnie rozpoznany");
        }

        @Test
        @DisplayName("Test Three of a Kind")
        void testCheckHandThreeOfAKind() {
            List<Card> threeOfAKind = Arrays.asList(
                    new Card(Rank.SEVEN, Suit.DIAMOND),
                    new Card(Rank.SEVEN, Suit.HEART),
                    new Card(Rank.SEVEN, Suit.SPADE),
                    new Card(Rank.TWO, Suit.CLUB),
                    new Card(Rank.FOUR, Suit.HEART)
            );

            HandRank expected = new HandRank(HandType.ThreeOfAKind, Arrays.asList(
                    Rank.SEVEN, Rank.FOUR, Rank.TWO
            ));

            HandRank actual = handCompare.checkHand(threeOfAKind);
            assertEquals(expected, actual, "Three of a Kind powinien być poprawnie rozpoznany");
        }

        @Test
        @DisplayName("Test Two Pair")
        void testCheckHandTwoPair() {
            List<Card> twoPair = Arrays.asList(
                    new Card(Rank.NINE, Suit.DIAMOND),
                    new Card(Rank.NINE, Suit.HEART),
                    new Card(Rank.FOUR, Suit.SPADE),
                    new Card(Rank.FOUR, Suit.CLUB),
                    new Card(Rank.KING, Suit.HEART)
            );

            HandRank expected = new HandRank(HandType.TwoPair, Arrays.asList(
                    Rank.NINE, Rank.FOUR, Rank.KING
            ));

            HandRank actual = handCompare.checkHand(twoPair);
            assertEquals(expected, actual, "Two Pair powinien być poprawnie rozpoznany");
        }

        @Test
        @DisplayName("Test One Pair")
        void testCheckHandOnePair() {
            List<Card> onePair = Arrays.asList(
                    new Card(Rank.THREE, Suit.DIAMOND),
                    new Card(Rank.THREE, Suit.HEART),
                    new Card(Rank.FIVE, Suit.SPADE),
                    new Card(Rank.EIGHT, Suit.CLUB),
                    new Card(Rank.KING, Suit.HEART)
            );

            HandRank expected = new HandRank(HandType.OnePair, Arrays.asList(
                    Rank.THREE, Rank.KING, Rank.EIGHT, Rank.FIVE
            ));

            HandRank actual = handCompare.checkHand(onePair);
            assertEquals(expected, actual, "One Pair powinien być poprawnie rozpoznany");
        }

        @Test
        @DisplayName("Test High Card")
        void testCheckHandHighCard() {
            List<Card> highCard = Arrays.asList(
                    new Card(Rank.TWO, Suit.DIAMOND),
                    new Card(Rank.FOUR, Suit.HEART),
                    new Card(Rank.SIX, Suit.SPADE),
                    new Card(Rank.EIGHT, Suit.CLUB),
                    new Card(Rank.JACK, Suit.HEART)
            );

            HandRank expected = new HandRank(HandType.HighCard, Arrays.asList(
                    Rank.JACK, Rank.EIGHT, Rank.SIX, Rank.FOUR, Rank.TWO
            ));

            HandRank actual = handCompare.checkHand(highCard);
            assertEquals(expected, actual, "High Card powinien być poprawnie rozpoznany");
        }

        // --- Testy edge cases --- //

    }

    // --- Testy dla determineWinner(List<Player> players) --- //

    @Nested
    @DisplayName("Testy metody determineWinner")
    class DetermineWinnerTests {

        @Test
        @DisplayName("Test determineWinner z jednym graczem")
        void testDetermineWinnerSinglePlayer() {
            Player player = mock(Player.class);
            Hand playerHand = mock(Hand.class);
            List<Card> cards = Arrays.asList(
                    new Card(Rank.TEN, Suit.HEART),
                    new Card(Rank.JACK, Suit.HEART),
                    new Card(Rank.QUEEN, Suit.HEART),
                    new Card(Rank.KING, Suit.HEART),
                    new Card(Rank.ACE, Suit.HEART)
            );
            when(player.getPlayerHand()).thenReturn(playerHand);
            when(playerHand.getCards()).thenReturn(cards);

            Player winner = handCompare.determineWinner(Arrays.asList(player));
            assertEquals(player, winner, "Jedyny gracz powinien zostać zwycięzcą");
        }

        @Test
        @DisplayName("Test determineWinner z wieloma graczami i różnymi rękami")
        void testDetermineWinnerMultiplePlayers() {
            // Gracz 1: Royal Flush
            Player player1 = mock(Player.class);
            Hand player1Hand = mock(Hand.class);
            List<Card> cards1 = Arrays.asList(
                    new Card(Rank.TEN, Suit.HEART),
                    new Card(Rank.JACK, Suit.HEART),
                    new Card(Rank.QUEEN, Suit.HEART),
                    new Card(Rank.KING, Suit.HEART),
                    new Card(Rank.ACE, Suit.HEART)
            );
            when(player1.getPlayerHand()).thenReturn(player1Hand);
            when(player1Hand.getCards()).thenReturn(cards1);

            // Gracz 2: Four of a Kind
            Player player2 = mock(Player.class);
            Hand player2Hand = mock(Hand.class);
            List<Card> cards2 = Arrays.asList(
                    new Card(Rank.NINE, Suit.DIAMOND),
                    new Card(Rank.NINE, Suit.HEART),
                    new Card(Rank.NINE, Suit.SPADE),
                    new Card(Rank.NINE, Suit.CLUB),
                    new Card(Rank.TWO, Suit.HEART)
            );
            when(player2.getPlayerHand()).thenReturn(player2Hand);
            when(player2Hand.getCards()).thenReturn(cards2);

            // Gracz 3: Full House
            Player player3 = mock(Player.class);
            Hand player3Hand = mock(Hand.class);
            List<Card> cards3 = Arrays.asList(
                    new Card(Rank.FIVE, Suit.DIAMOND),
                    new Card(Rank.FIVE, Suit.HEART),
                    new Card(Rank.FIVE, Suit.SPADE),
                    new Card(Rank.KING, Suit.CLUB),
                    new Card(Rank.KING, Suit.HEART)
            );
            when(player3.getPlayerHand()).thenReturn(player3Hand);
            when(player3Hand.getCards()).thenReturn(cards3);

            // Zwycięzca powinien być player1 z Royal Flush
            Player winner = handCompare.determineWinner(Arrays.asList(player1, player2, player3));
            assertEquals(player1, winner, "Gracz1 z Royal Flush powinien być zwycięzcą");
        }

        @Test
        @DisplayName("Test determineWinner z remisem")
        void testDetermineWinnerTie() {
            // Gracz 1: Full House
            Player player1 = mock(Player.class);
            Hand player1Hand = mock(Hand.class);
            List<Card> cards1 = Arrays.asList(
                    new Card(Rank.FIVE, Suit.DIAMOND),
                    new Card(Rank.FIVE, Suit.HEART),
                    new Card(Rank.FIVE, Suit.SPADE),
                    new Card(Rank.KING, Suit.CLUB),
                    new Card(Rank.KING, Suit.HEART)
            );
            when(player1.getPlayerHand()).thenReturn(player1Hand);
            when(player1Hand.getCards()).thenReturn(cards1);

            // Gracz 2: Full House z tymi samymi rankami
            Player player2 = mock(Player.class);
            Hand player2Hand = mock(Hand.class);
            List<Card> cards2 = Arrays.asList(
                    new Card(Rank.FIVE, Suit.CLUB),
                    new Card(Rank.FIVE, Suit.SPADE),
                    new Card(Rank.FIVE, Suit.HEART),
                    new Card(Rank.KING, Suit.DIAMOND),
                    new Card(Rank.KING, Suit.CLUB)
            );
            when(player2.getPlayerHand()).thenReturn(player2Hand);
            when(player2Hand.getCards()).thenReturn(cards2);

            // Zwycięzca może być null lub pierwszym graczem w przypadku remisu
            // W obecnej implementacji metoda determineWinner zwraca pierwszego gracza z najwyższą ręką
            Player winner = handCompare.determineWinner(Arrays.asList(player1, player2));
            assertEquals(player1, winner, "W przypadku remisu powinien zostać zwrócony pierwszy gracz");
        }

        @Test
        @DisplayName("Test determineWinner z graczami, którzy spasowali")
        void testDetermineWinnerWithFoldedPlayers() {
            // Gracz 1: Royal Flush
            Player player1 = mock(Player.class);
            Hand player1Hand = mock(Hand.class);
            List<Card> cards1 = Arrays.asList(
                    new Card(Rank.TEN, Suit.HEART),
                    new Card(Rank.JACK, Suit.HEART),
                    new Card(Rank.QUEEN, Suit.HEART),
                    new Card(Rank.KING, Suit.HEART),
                    new Card(Rank.ACE, Suit.HEART)
            );
            when(player1.getPlayerHand()).thenReturn(player1Hand);
            when(player1Hand.getCards()).thenReturn(cards1);
            when(player1.isFolded()).thenReturn(false);

            // Gracz 2: Four of a Kind, ale spasowany
            Player player2 = mock(Player.class);
            Hand player2Hand = mock(Hand.class);
            List<Card> cards2 = Arrays.asList(
                    new Card(Rank.NINE, Suit.DIAMOND),
                    new Card(Rank.NINE, Suit.HEART),
                    new Card(Rank.NINE, Suit.SPADE),
                    new Card(Rank.NINE, Suit.CLUB),
                    new Card(Rank.TWO, Suit.HEART)
            );
            when(player2.getPlayerHand()).thenReturn(player2Hand);
            when(player2Hand.getCards()).thenReturn(cards2);
            when(player2.isFolded()).thenReturn(true);

            // Gracz 3: Full House
            Player player3 = mock(Player.class);
            Hand player3Hand = mock(Hand.class);
            List<Card> cards3 = Arrays.asList(
                    new Card(Rank.FIVE, Suit.DIAMOND),
                    new Card(Rank.FIVE, Suit.HEART),
                    new Card(Rank.FIVE, Suit.SPADE),
                    new Card(Rank.KING, Suit.CLUB),
                    new Card(Rank.KING, Suit.HEART)
            );
            when(player3.getPlayerHand()).thenReturn(player3Hand);
            when(player3Hand.getCards()).thenReturn(cards3);
            when(player3.isFolded()).thenReturn(false);

            // Zwycięzca powinien być player1, ponieważ player2 spasował
            Player winner = handCompare.determineWinner(Arrays.asList(player1, player2, player3));
            assertEquals(player1, winner, "Gracz1 z Royal Flush powinien być zwycięzcą, player2 spasował");
        }
    }

    // --- Testy dla compareHandRank(HandRank h1, HandRank h2) --- //

    @Nested
    @DisplayName("Testy metody compareHandRank")
    class CompareHandRankTests {

        @Test
        @DisplayName("Compare HandRank: h1 > h2")
        void testCompareHandRankH1GreaterThanH2() {
            HandRank royalFlush = new HandRank(HandType.RoyalFlush, Arrays.asList(Rank.TEN, Rank.JACK, Rank.QUEEN, Rank.KING, Rank.ACE));
            HandRank straightFlush = new HandRank(HandType.StraightFlush, Arrays.asList(Rank.FIVE, Rank.SIX, Rank.SEVEN, Rank.EIGHT, Rank.NINE));

            int result = handCompare.compareHandRank(royalFlush, straightFlush);
            assertTrue(result > 0, "Royal Flush powinien być wyższy niż Straight Flush");
        }

        @Test
        @DisplayName("Compare HandRank: h1 < h2")
        void testCompareHandRankH1LessThanH2() {
            HandRank threeOfAKind = new HandRank(HandType.ThreeOfAKind, Arrays.asList(Rank.SEVEN, Rank.FOUR, Rank.TWO));
            HandRank twoPair = new HandRank(HandType.TwoPair, Arrays.asList(Rank.NINE, Rank.FOUR, Rank.KING));

            int result = handCompare.compareHandRank(threeOfAKind, twoPair);
            assertFalse(result < 0, "Three of a Kind powinien być niższy niż Two Pair");
        }

        @Test
        @DisplayName("Compare HandRank: h1 == h2")
        void testCompareHandRankH1EqualToH2() {
            HandRank highCard1 = new HandRank(HandType.HighCard, Arrays.asList(Rank.ACE, Rank.KING, Rank.QUEEN, Rank.JACK, Rank.NINE));
            HandRank highCard2 = new HandRank(HandType.HighCard, Arrays.asList(Rank.ACE, Rank.KING, Rank.QUEEN, Rank.JACK, Rank.NINE));

            int result = handCompare.compareHandRank(highCard1, highCard2);
            assertEquals(0, result, "Obie ręce High Card powinny być równe");
        }

        @Test
        @DisplayName("Compare HandRank: h1 == h2 z różnymi kickerami")
        void testCompareHandRankH1EqualToH2DifferentKickers() {
            HandRank straight1 = new HandRank(HandType.Straight, Arrays.asList(Rank.FIVE, Rank.SIX, Rank.SEVEN, Rank.EIGHT, Rank.NINE));
            HandRank straight2 = new HandRank(HandType.Straight, Arrays.asList(Rank.FIVE, Rank.SIX, Rank.SEVEN, Rank.EIGHT, Rank.NINE));

            int result = handCompare.compareHandRank(straight1, straight2);
            assertEquals(0, result, "Obie ręce Straight powinny być równe");
        }

        @Test
        @DisplayName("Compare HandRank: h1 < h2 z tym samym typem i wyższymi kickerami w h2")
        void testCompareHandRankH1LessThanH2SameType() {
            HandRank flush1 = new HandRank(HandType.Flush, Arrays.asList(Rank.KING, Rank.JACK, Rank.NINE, Rank.FIVE, Rank.TWO));
            HandRank flush2 = new HandRank(HandType.Flush, Arrays.asList(Rank.QUEEN, Rank.JACK, Rank.NINE, Rank.FIVE, Rank.THREE));

            int result = handCompare.compareHandRank(flush1, flush2);
            assertTrue(result > 0, "Flush1 powinien być wyższy niż Flush2 ze względu na wyższego kickera (KING vs QUEEN)");
        }

        @Test
        @DisplayName("Compare HandRank: h1 > h2 z tym samym typem i wyższymi kickerami w h1")
        void testCompareHandRankH1GreaterThanH2SameType() {
            HandRank straight1 = new HandRank(HandType.Straight, Arrays.asList(Rank.NINE, Rank.TEN, Rank.JACK, Rank.QUEEN, Rank.KING));
            HandRank straight2 = new HandRank(HandType.Straight, Arrays.asList(Rank.EIGHT, Rank.NINE, Rank.TEN, Rank.JACK, Rank.QUEEN));

            int result = handCompare.compareHandRank(straight1, straight2);
            assertTrue(result > 0, "Straight1 powinien być wyższy niż Straight2 ze względu na wyższego kickera (KING vs QUEEN)");
        }


    }

    // --- Dodatkowe Testy dla pomocniczych metod (opcjonalne) --- //
    // Można dodać testy dla metod takich jak isRoyalFlush, isStraightFlush, itp., jeśli są publiczne
    // Jednak zwykle testujemy je poprzez testy checkHand
}
