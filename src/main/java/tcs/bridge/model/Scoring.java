package tcs.bridge.model;

import java.util.ArrayList;
import java.util.List;

public class Scoring {
    public static class ScoringEntry {
        private final Contract contract;
        private final int wonTricks;
        private final int declarerAndDummy; // won points for this team
        private final int leftAndRightDefenders;
        ScoringEntry(Contract contract, int wonTricks) {
            this.contract = contract;
            this.wonTricks = wonTricks;
            int score = calculateScore(contract, wonTricks);
            this.declarerAndDummy = score;
            this.leftAndRightDefenders = -score;
        }
        private int calculateScore(Contract c, int wonTricks) {
            // TODO change to ok scoring
            // https://www.funbridge.com/pl/counting-bridge
            return wonTricks - c.level - (c.trump == null ? 4 : c.trump.ordinal() + 1) * 40 * c.scoreMultiplier;
        }
    }
    private final List<ScoringEntry> scoring = new ArrayList<>();

    void addScoringEntry(ScoringEntry entry) {
        scoring.add(entry);
    }
    ScoringEntry getScoringEntry(int index) {
        return scoring.get(index);
    }
    public List<ScoringEntry> getScoring() {
        return scoring;
    }
    public int getTotalDeclarerAndDummyPoints() {
        return scoring.stream().mapToInt(entry -> entry.declarerAndDummy).sum();
    }
    public int getTotalLeftAndRightDefendersPoints() {
        return scoring.stream().mapToInt(entry -> entry.leftAndRightDefenders).sum();
    }
}
