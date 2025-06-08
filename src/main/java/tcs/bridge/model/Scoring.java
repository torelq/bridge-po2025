package tcs.bridge.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Scoring implements Serializable {
    public static class ScoringEntry implements Serializable {
        private final Contract contract;
        private final int wonTricks;
        private final int nsScore; // won points for ns
        private final int ewScore;
        
        public ScoringEntry(Contract contract, int wonTricks) {
            this.contract = contract;
            this.wonTricks = wonTricks;

            int score = calcScore(contract, wonTricks);
            boolean declarerIsNS = isDeclarerNS(contract.declarer);
            if (declarerIsNS) {
                this.nsScore = score;
                this.ewScore = -score;
            } else {
                this.nsScore = -score;
                this.ewScore = score;
            }
        }

        private static int calcScore(Contract contract, int wonTricks) {
            int level = contract.level;
            Suit trump = contract.trump;
            int scoreMultiplier = contract.scoreMultiplier;
            int needed = level + 6;
            if (wonTricks < needed) {
                int penalty = needed - wonTricks;
                return -50 * penalty;
            }

            int over = wonTricks - needed;
            int basePerTrick = (
                trump == null ? 30 : 
                trump == Suit.CLUBS || trump == Suit.DIAMONDS ? 20 :
                30
            );

            int trickScore = 0;
            if (trump == null) {
                trickScore += 40 + (level - 1) * 30;
            } else {
                trickScore += level * basePerTrick;
            }
            trickScore *= scoreMultiplier;

            int overScore = 0;
            if (over > 0) {
                overScore = (
                    scoreMultiplier == 1 ?  over * (trump == null ? 30 : basePerTrick) :
                    scoreMultiplier == 2 ? over * 100 :
                    over * 200
                );
            }

            int insult = (
                scoreMultiplier == 1 ? 0 : 
                scoreMultiplier == 2 ? 50 : 
                100
            );

            int rawScore = (
                trump == null ? 40 * (level - 1) * 30 :
                level * (trump == Suit.CLUBS || trump == Suit.DIAMONDS ? 20 : 30)
            );

            int bonus = (
                rawScore < 100 ? 50 : 300
            );

            return trickScore + overScore + insult + bonus;
        }
        private static boolean isDeclarerNS(Player.Position p) {
            return p == Player.Position.NORTH || p == Player.Position.SOUTH;
        }
        public Contract getContract() { return contract; }
        public int getWonTricks() { return wonTricks; }
        public int getNS() { return nsScore; }
        public int getEW() { return ewScore; }

        @Override
        public String toString() {
            return "ScoringEntry{" +
                    "contract=" + contract +
                    ", wonTricks=" + wonTricks +
                    ", nsScore=" + nsScore +
                    ", ewScore=" + ewScore +
                    '}';
        }
    }
    private final List<ScoringEntry> scoring = new ArrayList<>();

    public void addEntry(ScoringEntry entry) {
        scoring.add(entry);
    }
    ScoringEntry getEntry(int index) {
        return scoring.get(index);
    }
    public List<ScoringEntry> getScoring() {
        return scoring;
    }
    public int getTotalNSPoints() {
        return scoring.stream().mapToInt(entry -> entry.nsScore).sum();
    }
    public int getTotalEWPoints() {
        return scoring.stream().mapToInt(entry -> entry.ewScore).sum();
    }
}
