package pl.edu.agh.kis.pz1.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {

    @Test
    void testGameStateValues() {
        GameState[] expectedStates = {
                GameState.WAITING_FOR_PLAYERS,
                GameState.POST_BLINDS,
                GameState.DEALING,
                GameState.FIRST_BETTING_ROUND,
                GameState.EXCHANGE_PHASE,
                GameState.SECOND_BETTING_ROUND,
                GameState.SHOWDOWN,
                GameState.FINISHED,
                GameState.WAITING_FOR_NEXT_ROUND,
                GameState.GAME_OVER
        };

        assertArrayEquals(expectedStates, GameState.values(), "Lista wartości GameState powinna być zgodna z oczekiwaną");
    }

    @Test
    void testGameStateNames() {
        assertEquals("WAITING_FOR_PLAYERS", GameState.WAITING_FOR_PLAYERS.name(), "Nazwa stanu powinna być 'WAITING_FOR_PLAYERS'");
        assertEquals("POST_BLINDS", GameState.POST_BLINDS.name(), "Nazwa stanu powinna być 'POST_BLINDS'");
        assertEquals("DEALING", GameState.DEALING.name(), "Nazwa stanu powinna być 'DEALING'");
        assertEquals("FIRST_BETTING_ROUND", GameState.FIRST_BETTING_ROUND.name(), "Nazwa stanu powinna być 'FIRST_BETTING_ROUND'");
        assertEquals("EXCHANGE_PHASE", GameState.EXCHANGE_PHASE.name(), "Nazwa stanu powinna być 'EXCHANGE_PHASE'");
        assertEquals("SECOND_BETTING_ROUND", GameState.SECOND_BETTING_ROUND.name(), "Nazwa stanu powinna być 'SECOND_BETTING_ROUND'");
        assertEquals("SHOWDOWN", GameState.SHOWDOWN.name(), "Nazwa stanu powinna być 'SHOWDOWN'");
        assertEquals("FINISHED", GameState.FINISHED.name(), "Nazwa stanu powinna być 'FINISHED'");
        assertEquals("WAITING_FOR_NEXT_ROUND", GameState.WAITING_FOR_NEXT_ROUND.name(), "Nazwa stanu powinna być 'WAITING_FOR_NEXT_ROUND'");
        assertEquals("GAME_OVER", GameState.GAME_OVER.name(), "Nazwa stanu powinna być 'GAME_OVER'");
    }

    @Test
    void testGameStateOrdinals() {
        assertEquals(0, GameState.WAITING_FOR_PLAYERS.ordinal(), "Ordinal dla WAITING_FOR_PLAYERS powinien być 0");
        assertEquals(1, GameState.POST_BLINDS.ordinal(), "Ordinal dla POST_BLINDS powinien być 1");
        assertEquals(2, GameState.DEALING.ordinal(), "Ordinal dla DEALING powinien być 2");
        assertEquals(3, GameState.FIRST_BETTING_ROUND.ordinal(), "Ordinal dla FIRST_BETTING_ROUND powinien być 3");
        assertEquals(4, GameState.EXCHANGE_PHASE.ordinal(), "Ordinal dla EXCHANGE_PHASE powinien być 4");
        assertEquals(5, GameState.SECOND_BETTING_ROUND.ordinal(), "Ordinal dla SECOND_BETTING_ROUND powinien być 5");
        assertEquals(6, GameState.SHOWDOWN.ordinal(), "Ordinal dla SHOWDOWN powinien być 6");
        assertEquals(7, GameState.FINISHED.ordinal(), "Ordinal dla FINISHED powinien być 7");
        assertEquals(8, GameState.WAITING_FOR_NEXT_ROUND.ordinal(), "Ordinal dla WAITING_FOR_NEXT_ROUND powinien być 8");
        assertEquals(9, GameState.GAME_OVER.ordinal(), "Ordinal dla GAME_OVER powinien być 9");
    }

    @Test
    void testValueOf() {
        for (GameState state : GameState.values()) {
            assertEquals(state, GameState.valueOf(state.name()), "Metoda valueOf powinna zwrócić ten sam stan dla nazwy " + state.name());
        }
    }
}
