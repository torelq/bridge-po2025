package tcs.bridge.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static tcs.bridge.model.Player.Position.*;
import static tcs.bridge.model.Bidding.Bid.*;
import static tcs.bridge.model.Suit.*;
import tcs.bridge.model.Bidding.Bid;

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
    void onlyPassesFinish() {
        assertThrows(RuntimeException.class, () -> testBid(bidding, NORTH, new Bid(1, HEARTS)));
        bidding.setStartingPosition(EAST);
        assertThrows(RuntimeException.class, () -> bidding.setStartingPosition(EAST));
        assertEquals(EAST, bidding.getTurn());
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
        bidding.setStartingPosition(NORTH);
        assertEquals(NORTH, bidding.getTurn());
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
        bidding.setStartingPosition(NORTH);
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
        bidding.setStartingPosition(NORTH);
        testBid(bidding, NORTH, new Bid(1, HEARTS));
        testBid(bidding, EAST, PASS_BID);
        assertFalse(testBid(bidding, SOUTH, DOUBLE_BID));
    }

    @Test
    void doubleSecondOpponent() {
        bidding.setStartingPosition(NORTH);
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
        bidding.setStartingPosition(NORTH);
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
        bidding.setStartingPosition(NORTH);
        assertEquals(NORTH, bidding.getTurn());
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
        bidding.setStartingPosition(NORTH);
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
        bidding.setStartingPosition(NORTH);
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
        bidding.setStartingPosition(NORTH);
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
        bidding.setStartingPosition(NORTH);
        testBid(bidding, NORTH, new Bid(4, NO_TRUMP));
        testBid(bidding, EAST, DOUBLE_BID);
        testBid(bidding, SOUTH, PASS_BID);
        assertFalse(testBid(bidding, WEST, REDOUBLE_BID));
    }

    @Test
    void redoubleAfterDoubleBySecondOpponent() {
        bidding.setStartingPosition(NORTH);
        testBid(bidding, NORTH, new Bid(4, NO_TRUMP));
        testBid(bidding, EAST, PASS_BID);
        testBid(bidding, SOUTH, PASS_BID);
        testBid(bidding, WEST, DOUBLE_BID);

        assertTrue(testBid(bidding, NORTH, REDOUBLE_BID));
    }

    @Test
    void firstOpponentCannotRedouble() {
        bidding.setStartingPosition(NORTH);
        testBid(bidding, NORTH, new Bid(4, NO_TRUMP));
        testBid(bidding, EAST, PASS_BID);
        testBid(bidding, SOUTH, PASS_BID);
        testBid(bidding, WEST, DOUBLE_BID);
        testBid(bidding, NORTH, PASS_BID);
        assertFalse(testBid(bidding, EAST, REDOUBLE_BID));
    }

    @Test
    void higherBidResetsRedouble() {
        bidding.setStartingPosition(NORTH);
        testBid(bidding, NORTH, new Bid(4, NO_TRUMP));
        testBid(bidding, EAST, DOUBLE_BID);
        testBid(bidding, SOUTH, REDOUBLE_BID);
        assertTrue(testBid(bidding, WEST, new Bid(5, NO_TRUMP)));
        testBid(bidding, NORTH, PASS_BID);
        testBid(bidding, EAST, PASS_BID);
        testBid(bidding, SOUTH, PASS_BID);
        assertEquals(new Contract(5, NO_TRUMP, WEST, 1), bidding.getContract());
    }
}
