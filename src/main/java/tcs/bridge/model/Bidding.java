package tcs.bridge.model;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Bidding {

    public static class Bid {
        public enum SpecialBid {
            PASS,
            DOUBLE,
            REDOUBLE
        }

        private final Suit suit; // null if no trump
        private final int level;
        private final SpecialBid special;

        public Bid(Suit suit, int level, SpecialBid special) {
            if ((level < 1 || level > 7) && special==null) {
                throw new IllegalArgumentException("Level must be between 1 and 7");
            }
            this.suit = suit;
            this.level = level;
            this.special = special;
        }

        /**
         * 
         * @param other
         * @return checks if this bid is less that the other bid.
         */
        public boolean isGreaterThan(Bid other) {
            if (other==null) return true;
            if (special!=null || other.special!=null) {
                throw new IllegalArgumentException("Cannot compare special bids");
            }
            if (this.level != other.level) {
                return this.level > other.level;
            }
            List<Suit> order = Arrays.asList(null, Suit.SPADES, Suit.HEARTS, Suit.DIAMONDS, Suit.CLUBS);
            return order.indexOf(this.suit) < order.indexOf(other.suit);
        }

        boolean isPass() {
            return special==SpecialBid.PASS;
        }

        boolean isDouble() {
            return special==SpecialBid.DOUBLE;
        }

        boolean isRedouble() {
            return special==SpecialBid.REDOUBLE;
        }

        boolean isSpecial() {
            return special==null;
        }
    }

    private final List<SimpleEntry<Player.Position, Bid>> bid_history = new ArrayList<>();
    private Player.Position lastNumericalBidPosition=null;
    private Bid lastNumericalBid=null;
    private boolean lastNumericalBidDoubled=false, lastNumericalBidRedoubled=false;
    private boolean isFinished=false;
    private Contract contract=null; // null - four passes

    public boolean makeBid(Player.Position position, Bid bid) {
        if (isFinished) throw new RuntimeException("Cannot makeBid on a finished Bidding object.");
        if (bid_history.isEmpty()) {
            if (bid.isSpecial() && !bid.isPass()) return false;
            bid_history.add(new SimpleEntry<>(position, bid));
            return true;
        }
        if (Player.Position.next(bid_history.get(bid_history.size()-1).getKey())!=position) throw new IllegalArgumentException("Bidding out of order.");
        if (!bid.isSpecial()) {
            if (bid.isGreaterThan(lastNumericalBid)) {
                lastNumericalBid = bid;
                lastNumericalBidPosition = position;
                lastNumericalBidDoubled = false;
                lastNumericalBidRedoubled = false;
                bid_history.add(new SimpleEntry<>(position, bid));
                return true;
            }
            return false;
        } else { // bid is a special one
            if (bid.isPass()) {
                bid_history.add(new SimpleEntry<>(position, bid));
                if (bid_history.size()<4) return true;
                if (bid_history.get(bid_history.size()-1).getValue().isPass() &&
                        bid_history.get(bid_history.size()-2).getValue().isPass() &&
                        bid_history.get(bid_history.size()-3).getValue().isPass()) {
                    makeFinished();
                }
                return true;
            } else {
                if (lastNumericalBid==null) return false;
                if (bid.isDouble()) {
                    if (Player.Position.areTeammates(position, lastNumericalBidPosition)) return false;
                    if (lastNumericalBidDoubled) return false;
                    lastNumericalBidDoubled = true;
                    bid_history.add(new SimpleEntry<>(position, bid));
                } else if (bid.isRedouble()) {
                    if (Player.Position.areOpponents(position, lastNumericalBidPosition)) return false;
                    if (!lastNumericalBidDoubled||lastNumericalBidRedoubled) return false;
                    lastNumericalBidRedoubled = true;
                    bid_history.add(new SimpleEntry<>(position, bid));
                }
                throw new RuntimeException();
            }
        }
    }

    private void makeFinished() {
        isFinished = true;
        if (lastNumericalBid==null) return; // 4 passes

        int scoreMultiplier=1;
        if (lastNumericalBidRedoubled) scoreMultiplier=4;
        else if (lastNumericalBidDoubled) scoreMultiplier=2;

        Player.Position declarer=null;
        for (SimpleEntry<Player.Position, Bid> entry : bid_history) {
            if (Player.Position.areTeammates(entry.getKey(), lastNumericalBidPosition)) {
                if (entry.getValue().suit==lastNumericalBid.suit) {
                    declarer = entry.getKey();
                    break;
                }
            }
        }

        contract = new Contract(lastNumericalBid.level, lastNumericalBid.suit, declarer, scoreMultiplier);
    }

    public boolean decision() {
        return isFinished;
    }

    public Contract getContract() {
        if (!isFinished) throw new RuntimeException("Cannot getContract of an unfinished Bidding object.");
        return contract;
    }

    public List<SimpleEntry<Player.Position, Bid>> getBidHistory() {
        return bid_history;
    }
}
