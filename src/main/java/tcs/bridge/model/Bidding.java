package tcs.bridge.model;

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Bidding implements Serializable {

    public static class Bid implements Serializable {
        public enum SpecialBid {
            PASS,
            DOUBLE,
            REDOUBLE
        }

        private final Suit suit; // null if no trump
        private final int level;
        private final SpecialBid special;
        public static final Bid PASS_BID = new Bid(0, null, SpecialBid.PASS);
        public static final Bid DOUBLE_BID = new Bid(0, null, SpecialBid.DOUBLE);
        public static final Bid REDOUBLE_BID = new Bid(0, null, SpecialBid.REDOUBLE);

        public Bid(int level, Suit suit) {
            this(level, suit, null);
        }

        public Bid(int level, Suit suit, SpecialBid special) {
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
            List<Suit> order = Arrays.asList(Suit.NO_TRUMP, Suit.SPADES, Suit.HEARTS, Suit.DIAMONDS, Suit.CLUBS);
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

        public boolean isSpecial() {
            return special!=null;
        }

        @Override
        public String toString() {
            if (special != null) {
                return special.name();
            }
            if (suit == Suit.NO_TRUMP) {
                return level + " NT";
            }
            String[] suits = {"♦", "♣", "♥", "♠"};
            return level + " " + suits[suit.ordinal()];
        }
    }

    private final List<SimpleEntry<Player.Position, Bid>> bid_history = new ArrayList<>();
    private Player.Position lastNumericalBidPosition=null;
    private Bid lastNumericalBid=null;
    private boolean lastNumericalBidDoubled=false, lastNumericalBidRedoubled=false;
    private boolean isFinished=false;
    private Contract contract=null; // null - four passes

    private boolean makeOrAttemptBid(Player.Position position, Bid bid, boolean placeTheBid) {
        if (isFinished) throw new IllegalStateException("Cannot make bids on a finished Bidding object.");

        if (bid_history.isEmpty()) {
            if (bid.isSpecial() && !bid.isPass()) return false;
            if (placeTheBid) {
                bid_history.add(new SimpleEntry<>(position, bid));
                if (!bid.isSpecial()) {
                    lastNumericalBid = bid;
                    lastNumericalBidPosition = position;
                }
            }
            return true;
        }

        if (Player.Position.next(bid_history.get(bid_history.size()-1).getKey())!=position) throw new IllegalArgumentException("Bidding out of order.");

        if (!bid.isSpecial()) {
            if (bid.isGreaterThan(lastNumericalBid)) {
                if (placeTheBid) {
                    lastNumericalBid = bid;
                    lastNumericalBidPosition = position;
                    lastNumericalBidDoubled = false;
                    lastNumericalBidRedoubled = false;
                    bid_history.add(new SimpleEntry<>(position, bid));
                }
                return true;
            }
            return false;
        } else { // bid is a special one
            if (bid.isPass()) {
                if (placeTheBid) {
                    bid_history.add(new SimpleEntry<>(position, bid));
                    if (bid_history.size()<4) return true;
                    if (bid_history.get(bid_history.size()-1).getValue().isPass() &&
                            bid_history.get(bid_history.size()-2).getValue().isPass() &&
                            bid_history.get(bid_history.size()-3).getValue().isPass()) {
                        makeFinished();
                    }
                }
            } else {
                if (lastNumericalBid==null) return false;
                if (bid.isDouble()) {
                    if (Player.Position.areTeammates(position, lastNumericalBidPosition)) return false;
                    if (lastNumericalBidDoubled) return false;
                    if (placeTheBid) {
                        lastNumericalBidDoubled = true;
                        bid_history.add(new SimpleEntry<>(position, bid));
                    }
                } else if (bid.isRedouble()) {
                    if (Player.Position.areOpponents(position, lastNumericalBidPosition)) return false;
                    if (!lastNumericalBidDoubled||lastNumericalBidRedoubled) return false;
                    if (placeTheBid) {
                        lastNumericalBidRedoubled = true;
                        bid_history.add(new SimpleEntry<>(position, bid));
                    }
                }
            }
            return true;
        }
    }

    public boolean canBid(Player.Position position, Bid bid) {
        return makeOrAttemptBid(position, bid, false);
    }

    public boolean makeBid(Player.Position position, Bid bid) {
        return makeOrAttemptBid(position, bid, true);
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
                if (!entry.getValue().isSpecial() && entry.getValue().suit==lastNumericalBid.suit) {
                    declarer = entry.getKey();
                    break;
                }
            }
        }

        contract = new Contract(lastNumericalBid.level, lastNumericalBid.suit, declarer, scoreMultiplier);
    }

    // can return null if no bids have been made
    public Player.Position getTurn() {
        if (bid_history.isEmpty()) return null;
        return Player.Position.next(bid_history.get(bid_history.size()-1).getKey());
    }

    public boolean decision() {
        return isFinished;
    }

    public Contract getContract() {
        if (!isFinished) throw new RuntimeException("Cannot getContract of an unfinished Bidding object.");
        return contract;
    }

    public boolean toRedeal() {
        return bid_history.size() == 4 && bid_history.get(0).getValue().isPass() &&
                bid_history.get(1).getValue().isPass() &&
                bid_history.get(2).getValue().isPass() &&
                bid_history.get(3).getValue().isPass();
    }

    public List<SimpleEntry<Player.Position, Bid>> getBidHistory() {
        return Collections.unmodifiableList(bid_history);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Bidding {\n");
        for (SimpleEntry<Player.Position, Bid> entry : bid_history) {
            sb.append("  ").append(entry.getKey().toString()).append(" bids ").append(entry.getValue().toString()).append("\n");
        }
        if (toRedeal()) {
            sb.append("  redeal!!!\n");
        } else if (isFinished) {
            sb.append("  Contract: ").append(contract.toString()).append("\n");
        } else {
            sb.append("  Bidding is not finished.\n");
        }
        sb.append("}");
        return sb.toString();
    }
}
