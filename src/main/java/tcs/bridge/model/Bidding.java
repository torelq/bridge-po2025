package tcs.bridge.model;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Bidding {

    public static class Bid {
        private final Suit suit; // null if no trump
        private final int level;
        private final boolean pass;

        public Bid(Suit suit, int level, boolean pass) {
            if ((level < 1 || level > 7) && !pass) {
                throw new IllegalArgumentException("Level must be between 1 and 7");
            }
            this.suit = suit;
            this.level = level;
            this.pass = pass;
        }

        /**
         * 
         * @param other
         * @return checks if this bid is less that the other bid.
         */
        public boolean isGreaterThan(Bid other) {
            if (this.pass || other.pass) {
                throw new IllegalArgumentException("Cannot compare pass bids");
            }
            if (this.level != other.level) {
                return this.level > other.level;
            }
            List<Suit> order = Arrays.asList(Suit.SPADES, Suit.HEARTS, Suit.DIAMONDS, Suit.CLUBS, null);
            return order.indexOf(this.suit) > order.indexOf(other.suit);
        }
    }

    private final List<SimpleEntry<Player, Bid>> bid_history = new ArrayList<>();

    /**
     * 
     * @param player
     * @param bid
     * @return true if the bid was made
     */
    public boolean makeBid(Player player, Bid bid) {
        if (bid_history.isEmpty()) {
            bid_history.add(new SimpleEntry<>(player, bid));
            return true;
        } else {
            Bid lastBid = bid_history.get(bid_history.size() - 1).getValue();
            if (!bid.isGreaterThan(lastBid)) {
                return false;
            }
            bid_history.add(new SimpleEntry<>(player, bid));
            return true;
        }
    }

    /**
     * 
     * @return true if three skips in a row
     */
    public boolean decision() {
        if (bid_history.size() < 4) {
            return false;
        }
        int sz = bid_history.size();
        return !bid_history.get(sz - 4).getValue().pass && bid_history.get(sz - 1).getValue().pass 
                && bid_history.get(sz - 2).getValue().pass && bid_history.get(sz - 3).getValue().pass;
    }

    public List<SimpleEntry<Player, Bid>> getBidHistory() {
        return bid_history;
    }

    public Player getWinner() {
        if (!decision()) {
            throw new IllegalStateException("Bidding is not complete");
        }
        return bid_history.get(bid_history.size() - 4).getKey();
    }
    public Player getDeclarer() {
        if (!decision()) {
            throw new IllegalStateException("Bidding is not complete");
        }
        return bid_history.get(bid_history.size() - 3).getKey();
    }
    public Player getDummy() {
        if (!decision()) {
            throw new IllegalStateException("Bidding is not complete");
        }
        return bid_history.get(bid_history.size() - 2).getKey();
    }
    public Suit getTrump() {
        if (!decision()) {
            throw new IllegalStateException("Bidding is not complete");
        }
        return bid_history.get(bid_history.size() - 4).getValue().suit;
    }
    public int getLevel() {
        if (!decision()) {
            throw new IllegalStateException("Bidding is not complete");
        }
        return bid_history.get(bid_history.size() - 4).getValue().level;
    }
}
