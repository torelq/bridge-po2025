package tcs.bridge.model;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tcs.bridge.model.Bidding.Bid;
import static tcs.bridge.model.Bidding.Bid.DOUBLE_BID;
import static tcs.bridge.model.Bidding.Bid.PASS_BID;
import static tcs.bridge.model.Bidding.Bid.REDOUBLE_BID;
import static tcs.bridge.model.Player.Position.EAST;
import static tcs.bridge.model.Player.Position.NORTH;
import static tcs.bridge.model.Player.Position.SOUTH;
import static tcs.bridge.model.Player.Position.WEST;
import static tcs.bridge.model.Suit.CLUBS;
import static tcs.bridge.model.Suit.DIAMONDS;
import static tcs.bridge.model.Suit.HEARTS;
import static tcs.bridge.model.Suit.NO_TRUMP;
import static tcs.bridge.model.Suit.SPADES;

class BiddingTest {
    private Bidding bidding;

    @BeforeEach
    void setUp() {bidding = new Bidding();}

    boolean testBid(Bidding bidding, Player.Position position, Bid bid) {
        boolean dryRunResult = bidding.canBid(position, bid);
        assertEquals(dryRunResult, bidding.makeBid(position, bid));
        return dryRunResult;
    }
    @Test
    void getAvailableBidsTest() {
        List<Bid> availableBids = bidding.getAvailableBids();
        assertEquals(35, availableBids.size());
        assertTrue(availableBids.contains(new Bid(1, CLUBS)));
        assertTrue(availableBids.contains(new Bid(7, NO_TRUMP)));

        testBid(bidding, NORTH, new Bid(1, HEARTS));
        availableBids = bidding.getAvailableBids();
        assertFalse(availableBids.contains(new Bid(1, CLUBS)));
        assertTrue(availableBids.contains(new Bid(2, HEARTS)));
        assertTrue(availableBids.contains(new Bid(7, NO_TRUMP)));

        testBid(bidding, EAST, new Bid(3, SPADES));
        availableBids = bidding.getAvailableBids();
        assertFalse(availableBids.contains(new Bid(3, HEARTS)));
        assertTrue(availableBids.contains(new Bid(4, SPADES)));
        assertTrue(availableBids.contains(new Bid(7, NO_TRUMP)));

        testBid(bidding, SOUTH, PASS_BID);
        availableBids = bidding.getAvailableBids();
        assertTrue(availableBids.contains(new Bid(4, SPADES)));
        assertTrue(availableBids.contains(new Bid(7, NO_TRUMP)));
    }
    @Test
    void onlyPassesFinish() {
        assertNull(bidding.getTurn());
        assertTrue(testBid(bidding, EAST, PASS_BID));
        assertEquals(SOUTH, bidding.getTurn());
        assertTrue(testBid(bidding, SOUTH, PASS_BID));
        assertEquals(WEST, bidding.getTurn());
        assertTrue(testBid(bidding, WEST, PASS_BID));
        assertEquals(NORTH, bidding.getTurn());
        assertFalse(bidding.decision());
        assertThrows(RuntimeException.class, () -> bidding.getContract());
        assertTrue(testBid(bidding, NORTH, PASS_BID));
        assertTrue(bidding.decision());
        assertNull(bidding.getContract());
        assertThrows(RuntimeException.class, () -> testBid(bidding, EAST, PASS_BID));
    }

    @Test
    void sampleBidding() {
        assertNull(bidding.getTurn());
        assertTrue(testBid(bidding, NORTH, PASS_BID));
        assertEquals(EAST, bidding.getTurn());
        assertTrue(testBid(bidding, EAST, new Bid(1, SPADES)));
        assertEquals(SOUTH, bidding.getTurn());
        assertFalse(testBid(bidding, SOUTH, new Bid(1, SPADES)));
        assertEquals(SOUTH, bidding.getTurn());
        assertTrue(testBid(bidding, SOUTH, new Bid(1, NO_TRUMP)));
        assertEquals(WEST, bidding.getTurn());
        assertFalse(testBid(bidding, WEST, new Bid(1, HEARTS)));
        assertEquals(WEST, bidding.getTurn());
        assertTrue(testBid(bidding, WEST, new Bid(2, SPADES)));
        assertEquals(NORTH, bidding.getTurn());
        assertFalse(testBid(bidding, NORTH, new Bid(2, HEARTS)));
        assertEquals(NORTH, bidding.getTurn());
        assertTrue(testBid(bidding, NORTH, PASS_BID));
        assertEquals(EAST, bidding.getTurn());
        assertTrue(testBid(bidding, EAST, PASS_BID));
        assertEquals(SOUTH, bidding.getTurn());
        assertTrue(testBid(bidding, SOUTH, PASS_BID));
        assertTrue(bidding.decision());
        assertEquals(new Contract(2, SPADES, EAST), bidding.getContract());
    }

    @Test
    void doubleFirstOpponent() {
        assertFalse(testBid(bidding, NORTH, DOUBLE_BID));
        assertTrue(testBid(bidding, NORTH, PASS_BID));
        assertFalse(testBid(bidding, EAST, DOUBLE_BID));
        assertTrue(testBid(bidding, EAST, new Bid(1, CLUBS)));
        assertTrue(testBid(bidding, SOUTH, DOUBLE_BID));
        assertFalse(testBid(bidding, WEST, DOUBLE_BID));
        assertTrue(testBid(bidding, WEST, PASS_BID));
        assertFalse(testBid(bidding, NORTH, DOUBLE_BID));
        assertTrue(testBid(bidding, NORTH, PASS_BID));
        assertTrue(testBid(bidding, EAST, PASS_BID));
        assertTrue(bidding.decision());
        assertEquals(new Contract(1, CLUBS, EAST, 2), bidding.getContract());
    }

    @Test
    void partnerCannotDouble() {
        testBid(bidding, NORTH, new Bid(1, HEARTS));
        testBid(bidding, EAST, PASS_BID);
        assertFalse(testBid(bidding, SOUTH, DOUBLE_BID));
    }

    @Test
    void doubleSecondOpponent() {
        testBid(bidding, NORTH, new Bid(7, DIAMONDS));
        testBid(bidding, EAST, PASS_BID);
        testBid(bidding, SOUTH, PASS_BID);
        assertTrue(testBid(bidding, WEST, DOUBLE_BID));
        assertFalse(testBid(bidding, NORTH, DOUBLE_BID));
        assertTrue(testBid(bidding, NORTH, PASS_BID));
        assertFalse(testBid(bidding, EAST, DOUBLE_BID));
        assertTrue(testBid(bidding, EAST, PASS_BID));
        assertFalse(testBid(bidding, SOUTH, DOUBLE_BID));
        assertTrue(testBid(bidding, SOUTH, PASS_BID));
        assertTrue(bidding.decision());
        assertEquals(new Contract(7, DIAMONDS, NORTH, 2), bidding.getContract());
    }

    @Test
    void higherBidResetsDouble() {
        testBid(bidding, NORTH, new Bid(5, HEARTS));
        testBid(bidding, EAST, DOUBLE_BID);
        testBid(bidding, SOUTH, PASS_BID);
        testBid(bidding, WEST, new Bid(5, NO_TRUMP));
        testBid(bidding, NORTH, PASS_BID);
        testBid(bidding, EAST, PASS_BID);
        testBid(bidding, SOUTH, PASS_BID);
        assertEquals(new Contract(5, NO_TRUMP, WEST, 1), bidding.getContract());
    }

    @Test
    void outOfOrderBidding() {
        assertNull(bidding.getTurn());
        testBid(bidding, NORTH, new Bid(1, HEARTS));
        assertEquals(EAST, bidding.getTurn());
        assertThrows(IllegalArgumentException.class, () -> testBid(bidding, SOUTH, PASS_BID));
        assertEquals(EAST, bidding.getTurn());
        testBid(bidding, EAST, PASS_BID);
        assertEquals(SOUTH, bidding.getTurn());
        testBid(bidding, SOUTH, PASS_BID);
        assertEquals(WEST, bidding.getTurn());
        testBid(bidding, WEST, PASS_BID);
        assertEquals(new Contract(1, HEARTS, NORTH), bidding.getContract());
    }

    @Test
    void cannotRedoubleWithoutDouble() {
        testBid(bidding, NORTH, new Bid(1, CLUBS));
        assertFalse(testBid(bidding, EAST, REDOUBLE_BID));
        testBid(bidding, EAST, PASS_BID);
        assertFalse(testBid(bidding, SOUTH, REDOUBLE_BID));
        testBid(bidding, SOUTH, PASS_BID);
        assertFalse(testBid(bidding, WEST, REDOUBLE_BID));
        testBid(bidding, WEST, PASS_BID);
        assertTrue(bidding.decision());
        assertEquals(new Contract(1, CLUBS, NORTH, 1), bidding.getContract());
    }

    @Test
    void redoublePartner() {
        testBid(bidding, NORTH, new Bid(1, CLUBS));
        testBid(bidding, EAST, DOUBLE_BID);
        assertTrue(testBid(bidding, SOUTH, REDOUBLE_BID));

        assertFalse(testBid(bidding, WEST, DOUBLE_BID));
        assertFalse(testBid(bidding, WEST, REDOUBLE_BID));
        testBid(bidding, WEST, PASS_BID);
        assertFalse(testBid(bidding, NORTH, DOUBLE_BID));
        assertFalse(testBid(bidding, NORTH, REDOUBLE_BID));
        testBid(bidding, NORTH, PASS_BID);
        assertFalse(testBid(bidding, EAST, DOUBLE_BID));
        assertFalse(testBid(bidding, EAST, REDOUBLE_BID));
        testBid(bidding, EAST, PASS_BID);
        assertEquals(new Contract(1, CLUBS, NORTH, 4), bidding.getContract());
    }

    @Test
    void redoubleBidder() {
        testBid(bidding, NORTH, new Bid(4, NO_TRUMP));
        testBid(bidding, EAST, DOUBLE_BID);
        testBid(bidding, SOUTH, PASS_BID);
        testBid(bidding, WEST, PASS_BID);
        assertTrue(testBid(bidding, NORTH, REDOUBLE_BID));

        assertFalse(testBid(bidding, EAST, DOUBLE_BID));
        assertFalse(testBid(bidding, EAST, REDOUBLE_BID));
        testBid(bidding, EAST, PASS_BID);
        assertFalse(testBid(bidding, SOUTH, DOUBLE_BID));
        assertFalse(testBid(bidding, SOUTH, REDOUBLE_BID));
        testBid(bidding, SOUTH, PASS_BID);
        assertFalse(testBid(bidding, WEST, DOUBLE_BID));
        assertFalse(testBid(bidding, WEST, REDOUBLE_BID));
        testBid(bidding, WEST, PASS_BID);
        assertEquals(new Contract(4, NO_TRUMP, NORTH, 4), bidding.getContract());
    }

    @Test
    void secondOpponentCannotRedouble() {
        testBid(bidding, NORTH, new Bid(4, NO_TRUMP));
        testBid(bidding, EAST, DOUBLE_BID);
        testBid(bidding, SOUTH, PASS_BID);
        assertFalse(testBid(bidding, WEST, REDOUBLE_BID));
    }

    @Test
    void redoubleAfterDoubleBySecondOpponent() {
        testBid(bidding, NORTH, new Bid(4, NO_TRUMP));
        testBid(bidding, EAST, PASS_BID);
        testBid(bidding, SOUTH, PASS_BID);
        testBid(bidding, WEST, DOUBLE_BID);

        assertTrue(testBid(bidding, NORTH, REDOUBLE_BID));
    }

    @Test
    void firstOpponentCannotRedouble() {
        testBid(bidding, NORTH, new Bid(4, NO_TRUMP));
        testBid(bidding, EAST, PASS_BID);
        testBid(bidding, SOUTH, PASS_BID);
        testBid(bidding, WEST, DOUBLE_BID);
        testBid(bidding, NORTH, PASS_BID);
        assertFalse(testBid(bidding, EAST, REDOUBLE_BID));
    }

    @Test
    void higherBidResetsRedouble() {
        testBid(bidding, NORTH, new Bid(4, NO_TRUMP));
        testBid(bidding, EAST, DOUBLE_BID);
        testBid(bidding, SOUTH, REDOUBLE_BID);
        assertTrue(testBid(bidding, WEST, new Bid(5, NO_TRUMP)));
        testBid(bidding, NORTH, PASS_BID);
        testBid(bidding, EAST, PASS_BID);
        testBid(bidding, SOUTH, PASS_BID);
        assertEquals(new Contract(5, NO_TRUMP, WEST, 1), bidding.getContract());
    }

    @Test
    void cannotDoubleOnPass() {
        assertFalse(testBid(bidding, NORTH, DOUBLE_BID));
        assertTrue(testBid(bidding, NORTH, PASS_BID));
        assertFalse(testBid(bidding, EAST, DOUBLE_BID));
        assertTrue(testBid(bidding, EAST, PASS_BID));
        assertFalse(testBid(bidding, SOUTH, DOUBLE_BID));
        assertTrue(testBid(bidding, SOUTH, PASS_BID));
        assertFalse(testBid(bidding, WEST, DOUBLE_BID));
        assertTrue(testBid(bidding, WEST, PASS_BID));
        assertNull(bidding.getContract());
    }

    @Test
    void getBidHistoryTest() {
        List<SimpleEntry<Player.Position, Bid>> history = bidding.getBidHistory();
        assertEquals(0, history.size());
        assertThrows(RuntimeException.class, () -> history.add(new SimpleEntry<>(NORTH, new Bid(1, HEARTS))));
        bidding.makeBid(NORTH, PASS_BID);
        assertEquals(1, history.size());
        assertEquals(new SimpleEntry<>(NORTH, PASS_BID), history.get(0));
    }

    public static void main(String[] args) {
        Bidding bidding = new Bidding();
        bidding.makeBid(NORTH, new Bid(1, HEARTS));
        System.out.println(bidding);

        bidding.makeBid(EAST, PASS_BID);
        bidding.makeBid(SOUTH, PASS_BID);
        bidding.makeBid(WEST, PASS_BID);
        System.out.println(bidding);

        bidding = new Bidding();
        bidding.makeBid(NORTH, new Bid(1, CLUBS));
        bidding.makeBid(EAST, DOUBLE_BID);
        bidding.makeBid(SOUTH, REDOUBLE_BID);
        bidding.makeBid(WEST, PASS_BID);
        bidding.makeBid(NORTH, PASS_BID);
        bidding.makeBid(EAST, PASS_BID);
        System.out.println(bidding);

        bidding = new Bidding();
        bidding.makeBid(EAST, PASS_BID);
        bidding.makeBid(SOUTH, PASS_BID);
        bidding.makeBid(WEST, PASS_BID);
        bidding.makeBid(NORTH, PASS_BID);
        System.out.println(bidding);
    }
}
