package tcs.bridge.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScoringTest {

    @Test
    void acceptsTwoEntries() {
        Scoring scoring = new Scoring();
        scoring.addEntry(new Scoring.ScoringEntry(new Contract(1, Suit.CLUBS, Player.Position.EAST, 1), 7));
        scoring.addEntry(new Scoring.ScoringEntry(new Contract(2, Suit.HEARTS, Player.Position.WEST, 2), 9));
        assertEquals(2, scoring.getScoring().size());
    }
}
