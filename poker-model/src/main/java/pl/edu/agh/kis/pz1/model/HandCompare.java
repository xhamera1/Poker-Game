package pl.edu.agh.kis.pz1.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for comparing poker hands and determining the winner.
 * This class provides methods for evaluating the rank of a hand and determining the best hand
 * among a list of players based on poker hand rankings.
 */
public class HandCompare {

    /**
     * Evaluates the hand and returns its rank.
     * The method sorts the cards by rank and checks for the presence of various hand types
     * (Royal Flush, Straight Flush, Four of a Kind, etc.).
     * It then returns the appropriate HandRank object with the hand's type and kickers,
     * if applicable. Kickers are used to break ties between hands of the same type.
     *
     * @param hand The list of cards representing the player's hand.
     * @return A HandRank object representing the evaluated hand's type and kickers.
     */
    public HandRank checkHand(List<Card> hand) {
        hand.sort((a,b) -> Integer.compare(a.getRank().getValue(), b.getRank().getValue()));


        if (isRoyalFlush(hand)) {
            return new HandRank(HandType.RoyalFlush, getRanks(hand));
        } else if (isStraightFlush(hand)) {
            // Straight Flush – karty w kolejności i ten sam kolor
            // Kickers: pełna ręka, ponieważ najwyższa karta StritFlush decyduje
            return new HandRank(HandType.StraightFlush, getRanks(hand));
        } else if (isFourOfAKind(hand)) {
            // Kareta
            // Kickery: [Rank Karety, Kicker]
            return getFourOfAKindHandRank(hand);
        } else if (isFullHouse(hand)) {
            // Full (3+2)
            // Kickery: [Rank trójki, Rank pary]
            return getFullHouseHandRank(hand);
        } else if (isFlush(hand)) {
            // Kolor
            // Kickery: posortowane ranki kart w ręce od najwyższej do najniższej
            return new HandRank(HandType.Flush, getRanksDescending(hand));
        } else if (isStraight(hand)) {
            // Strit =  5 kart pod rzad
            // Kickery: pełna ręka, najwyższa karta decyduje
            return new HandRank(HandType.Straight, getRanks(hand));
        } else if (isThreeOfAKind(hand)) {
            // Trójka
            // Kickery: [Rank trójki, pozostałe karty jako kickery w kolejności malejącej]
            return getThreeOfAKindHandRank(hand);
        } else if (isTwoPair(hand)) {
            // Dwie pary
            // Kickery: [wyższa para, niższa para, kicker]
            return getTwoPairHandRank(hand);
        } else if (isOnePair(hand)) {
            // Para
            // Kickery: [Rank pary, pozostałe karty w kolejności malejącej]
            return getOnePairHandRank(hand);
        } else {
            // High Card
            // Kickery: wszystkie karty w kolejności malejącej
            return new HandRank(HandType.HighCard, getRanksDescending(hand));
        }
    }

    /**
     * Determines the winner among a list of players by comparing their hands.
     * The method checks each player's hand and evaluates its rank using the `checkHand` method.
     * It then compares each hand to find the highest ranked hand and returns the player with that hand.
     *
     * @param players The list of players to evaluate.
     * @return The player with the best hand, based on the hand rankings.
     */
    public Player determineWinner(List<Player> players) {
        Player bestPlayer = null;
        HandRank bestHandRank = null;

        for (Player p : players) {
            HandRank current = checkHand(p.getPlayerHand().getCards());
            if (bestHandRank == null || compareHandRank(current, bestHandRank) > 0) {
                bestHandRank = current;
                bestPlayer = p;
            }
        }

        return bestPlayer;
    }

    // pomocnicze metody do sprawdznaia ukladow:


    /**
     * Checks if the hand is a Royal Flush.
     * A Royal Flush consists of a straight flush with the highest possible cards: Ten to Ace.
     * This method checks if the hand is both a flush and a straight, and if it includes the Ace and Ten.
     *
     * @param hand The list of cards representing the player's hand.
     * @return True if the hand is a Royal Flush, false otherwise.
     */
    public boolean isRoyalFlush(List<Card> hand) {
        return isFlush(hand) && isStraight(hand) && hand.get(4).getRank() == Rank.ACE && hand.get(0).getRank() == Rank.TEN;
    }


    /**
     * Checks if the hand is a Straight Flush.
     * A Straight Flush consists of five consecutive cards in the same suit.
     * This method checks if the hand is both a flush and a straight.
     *
     * @param hand The list of cards representing the player's hand.
     * @return True if the hand is a Straight Flush, false otherwise.
     */
    public boolean isStraightFlush(List<Card> hand) {
        return isFlush(hand) && isStraight(hand);
    }


    /**
     * Checks if the hand is a Flush.
     * A Flush consists of five cards of the same suit, regardless of their order.
     * This method checks if all cards in the hand share the same suit.
     *
     * @param hand The list of cards representing the player's hand.
     * @return True if the hand is a Flush, false otherwise.
     */
    public boolean isFlush(List<Card> hand) {
        Suit s = hand.get(0).getSuit();
        for (Card c : hand) {
            if (c.getSuit() != s) return false;
        }
        return true;
    }


    /**
     * Checks if the hand is a Straight.
     * A Straight consists of five consecutive cards in any suit.
     * This method verifies if the ranks of the cards form a sequence of consecutive values.
     *
     * @param hand The list of cards representing the player's hand.
     * @return True if the hand is a Straight, false otherwise.
     */
    public boolean isStraight(List<Card> hand) {
        for (int i = 1; i < hand.size(); i++) {
            if (hand.get(i).getRank().getValue() != hand.get(i-1).getRank().getValue()+1) {
                return false;
            }
        }
        return true;
    }


    /**
     * Checks if the hand is a Four of a Kind.
     * A Four of a Kind consists of four cards with the same rank.
     * This method checks if any four consecutive cards have the same rank.
     *
     * @param hand The list of cards representing the player's hand.
     * @return True if the hand is a Four of a Kind, false otherwise.
     */
    public boolean isFourOfAKind(List<Card> hand) {
        // sprawdzamy czy 4 karty pod rząd mają tę samą wartość
        return (hand.get(0).getRank() == hand.get(1).getRank() &&
                hand.get(1).getRank() == hand.get(2).getRank() &&
                hand.get(2).getRank() == hand.get(3).getRank())
                ||
                (hand.get(1).getRank() == hand.get(2).getRank() &&
                        hand.get(2).getRank() == hand.get(3).getRank() &&
                        hand.get(3).getRank() == hand.get(4).getRank());
    }


    /**
     * Checks if the hand is a Full House.
     * A Full House consists of three cards of one rank and two cards of another rank.
     * This method verifies two possible Full House patterns: three cards of one rank followed by two of another,
     * or two cards of one rank followed by three of another.
     *
     * @param hand The list of cards representing the player's hand.
     * @return True if the hand is a Full House, false otherwise.
     */
    public boolean isFullHouse(List<Card> hand) {
        // Full: 3 + 2
        // układ np: x,x,x,y,y albo y,y,x,x,x
        boolean threeFirst = (hand.get(0).getRank()==hand.get(1).getRank() && hand.get(1).getRank()==hand.get(2).getRank());
        boolean pairLast = (hand.get(3).getRank()==hand.get(4).getRank());

        boolean pairFirst = (hand.get(0).getRank()==hand.get(1).getRank());
        boolean threeLast = (hand.get(2).getRank()==hand.get(3).getRank() && hand.get(3).getRank()==hand.get(4).getRank());

        return (threeFirst && pairLast) || (pairFirst && threeLast);
    }


    /**
     * Checks if the hand is a Three of a Kind.
     * A Three of a Kind consists of three cards of the same rank, plus two other different cards.
     * This method checks if any set of three consecutive cards have the same rank.
     *
     * @param hand The list of cards representing the player's hand.
     * @return True if the hand is a Three of a Kind, false otherwise.
     */
    public boolean isThreeOfAKind(List<Card> hand) {
        // Trójka: x,x,x,y,z lub y,x,x,x,z lub y,z,x,x,x
        for (int i = 0; i < 3; i++) {
            if (hand.get(i).getRank() == hand.get(i+1).getRank() &&
                    hand.get(i+1).getRank() == hand.get(i+2).getRank()) {
                return true;
            }
        }
        return false;
    }


    /**
     * Checks if the hand is Two Pair.
     * A Two Pair consists of two cards of one rank, two cards of another rank, and one kicker card.
     * This method verifies if there are exactly two pairs in the hand.
     *
     * @param hand The list of cards representing the player's hand.
     * @return True if the hand is Two Pair, false otherwise.
     */
    public boolean isTwoPair(List<Card> hand) {
        int pairs = 0;
        int i = 0;
        while (i < 4) {
            if (hand.get(i).getRank() == hand.get(i + 1).getRank()) {
                pairs++;
                i += 2; // przeskakujemy kolejną kartę
            } else {
                i++;
            }
        }
        return pairs == 2;
    }


    /**
     * Checks if the hand is One Pair.
     * A One Pair consists of two cards of the same rank, and three other cards as kickers.
     * This method verifies if there is exactly one pair in the hand.
     *
     * @param hand The list of cards representing the player's hand.
     * @return True if the hand is One Pair, false otherwise.
     */
    public boolean isOnePair(List<Card> hand) {
        // Jedna para: x,x,y,z,w
        for (int i = 0; i < 4; i++) {
            if (hand.get(i).getRank() == hand.get(i+1).getRank()) {
                return true;
            }
        }
        return false;
    }

    // ---------------- Metody generujące HandRank dla konkretnych układów ---------------- //


    /**
     * Generates the HandRank for a Four of a Kind hand.
     * A Four of a Kind consists of four cards of the same rank and one kicker card.
     * This method identifies the rank of the four-of-a-kind and the kicker card.
     *
     * @param hand The list of cards representing the player's hand.
     * @return A HandRank object representing the Four of a Kind hand and its kicker.
     */
    private HandRank getFourOfAKindHandRank(List<Card> hand) {
        Rank fourOfAKindRank = null;
        Rank kicker = null;

        // Sprawdź, gdzie jest kareta
        if (hand.get(0).getRank() == hand.get(3).getRank()) {
            fourOfAKindRank = hand.get(0).getRank();
            kicker = hand.get(4).getRank();
        } else {
            fourOfAKindRank = hand.get(1).getRank();
            kicker = hand.get(0).getRank();
        }

        return new HandRank(HandType.FourOfAKind, List.of(fourOfAKindRank, kicker));
    }


    /**
     * Generates the HandRank for a Full House hand.
     * A Full House consists of three cards of the same rank and two cards of another rank.
     * This method identifies the rank of the three-of-a-kind and the rank of the pair.
     *
     * @param hand The list of cards representing the player's hand.
     * @return A HandRank object representing the Full House hand with the ranks of the three-of-a-kind and the pair.
     */
    private HandRank getFullHouseHandRank(List<Card> hand) {
        Rank threeRank;
        Rank pairRank;

        // Jeśli układ: x,x,x,y,y
        if (hand.get(0).getRank() == hand.get(2).getRank()) {
            threeRank = hand.get(0).getRank();
            pairRank = hand.get(3).getRank();
        } else {
            // y,y,x,x,x
            threeRank = hand.get(2).getRank();
            pairRank = hand.get(0).getRank();
        }

        return new HandRank(HandType.FullHouse, List.of(threeRank, pairRank));
    }


    /**
     * Generates the HandRank for a Three of a Kind hand.
     * A Three of a Kind consists of three cards of the same rank and two other kicker cards.
     * This method identifies the rank of the three-of-a-kind and sorts the remaining cards as kickers.
     *
     * @param hand The list of cards representing the player's hand.
     * @return A HandRank object representing the Three of a Kind hand with the rank of the three-of-a-kind and the kickers.
     */
    private HandRank getThreeOfAKindHandRank(List<Card> hand) {
        Rank threeRank = null;
        List<Rank> other = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            if (hand.get(i).getRank() == hand.get(i+1).getRank() &&
                    hand.get(i+1).getRank() == hand.get(i+2).getRank()) {
                threeRank = hand.get(i).getRank();
                // Dodajemy pozostałe karty jako kickery
                for (int j = 0; j < 5; j++) {
                    if (j < i || j > i+2) {
                        other.add(hand.get(j).getRank());
                    }
                }
                break;
            }
        }

        // Sortujemy kickery malejąco
        other.sort((a,b) -> Integer.compare(b.getValue(), a.getValue()));
        List<Rank> kickers = new ArrayList<>();
        kickers.add(threeRank);
        kickers.addAll(other);

        return new HandRank(HandType.ThreeOfAKind, kickers);
    }


    /**
     * Generates the HandRank for a Two Pair hand.
     * A Two Pair consists of two pairs of cards and one kicker card.
     * This method identifies the two pairs and the kicker card, and sorts the pairs in descending order.
     *
     * @param hand The list of cards representing the player's hand.
     * @return A HandRank object representing the Two Pair hand with the two pairs and the kicker.
     */
    private HandRank getTwoPairHandRank(List<Card> hand) {
        List<Rank> pairs = new ArrayList<>();
        Rank kicker = null;

        int i = 0;
        while (i < 4) {
            if (hand.get(i).getRank() == hand.get(i + 1).getRank()) {
                pairs.add(hand.get(i).getRank());
                i += 2;
            } else {
                i++;
            }
        }

        for (Card c : hand) {
            if (!pairs.contains(c.getRank()) ||
                    (pairs.indexOf(c.getRank()) != pairs.lastIndexOf(c.getRank())
                            && !pairs.contains(c.getRank()))) {
                kicker = c.getRank();
            }
        }

        pairs.sort((a,b) -> Integer.compare(b.getValue(), a.getValue()));

        List<Rank> kickers = new ArrayList<>(pairs);
        kickers.add(kicker);

        return new HandRank(HandType.TwoPair, kickers);
    }


    /**
     * Generates the HandRank for a One Pair hand.
     * A One Pair consists of two cards of the same rank and three other kicker cards.
     * This method identifies the rank of the pair and sorts the remaining cards as kickers.
     *
     * @param hand The list of cards representing the player's hand.
     * @return A HandRank object representing the One Pair hand with the rank of the pair and the kickers.
     */
    private HandRank getOnePairHandRank(List<Card> hand) {
        Rank pairRank = null;
        List<Rank> others = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            if (hand.get(i).getRank() == hand.get(i+1).getRank()) {
                pairRank = hand.get(i).getRank();
                for (int j = 0; j < 5; j++) {
                    if (j < i || j > i+1) {
                        others.add(hand.get(j).getRank());
                    }
                }
                break;
            }
        }

        others.sort((a,b) -> Integer.compare(b.getValue(), a.getValue()));

        List<Rank> kickers = new ArrayList<>();
        kickers.add(pairRank);
        kickers.addAll(others);

        return new HandRank(HandType.OnePair, kickers);
    }

    // ---------------- Pomocnicze metody do kickers ---------------- //


    /**
     * Generates a list of ranks from the player's hand.
     * This method extracts the rank of each card in the hand.
     *
     * @param hand The list of cards representing the player's hand.
     * @return A list of ranks corresponding to the cards in the hand.
     */
    private List<Rank> getRanks(List<Card> hand) {
        List<Rank> rankList = new ArrayList<>();
        for (Card card : hand) {
            rankList.add(card.getRank());
        }
        return rankList;
    }

    /**
     * Generates a list of ranks from the player's hand sorted in descending order.
     * This method extracts the ranks of the cards and sorts them in descending order.
     *
     * @param hand The list of cards representing the player's hand.
     * @return A list of ranks corresponding to the cards in the hand, sorted in descending order.
     */
    private List<Rank> getRanksDescending(List<Card> hand) {
        List<Rank> rankList = getRanks(hand);
        rankList.sort((a,b) -> Integer.compare(b.getValue(), a.getValue()));
        return rankList;
    }

    /**
     * Compares two HandRank objects.
     * This method compares the types of the hands first. If the types are the same, it compares the kickers in descending order.
     *
     * @param h1 The first HandRank to compare.
     * @param h2 The second HandRank to compare.
     * @return A positive number if h1 is better, a negative number if h2 is better, or 0 if they are equal.
     */
    public int compareHandRank(HandRank h1, HandRank h2) {
        if (h1.getType().getValue() != h2.getType().getValue()) {
            return Integer.compare(h1.getType().getValue(), h2.getType().getValue());
        }

        // Jeśli typ taki sam, porównujemy kickery po kolei
        List<Rank> k1 = h1.getKickers();
        List<Rank> k2 = h2.getKickers();

        int size = Math.min(k1.size(), k2.size());
        for (int i = 0; i < size; i++) {
            int comp = Integer.compare(k1.get(i).getValue(), k2.get(i).getValue());
            if (comp != 0) return comp;
        }

        return 0;
    }
}

