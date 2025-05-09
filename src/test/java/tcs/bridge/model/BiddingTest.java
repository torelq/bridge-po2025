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

    @Test
    void onlyPassesFinish() {
        assertTrue(bidding.makeBid(EAST, PASS_BID));
        assertTrue(bidding.makeBid(SOUTH, PASS_BID));
        assertTrue(bidding.makeBid(WEST, PASS_BID));
        assertFalse(bidding.decision());
        assertThrows(RuntimeException.class, () -> bidding.getContract());
        assertTrue(bidding.makeBid(NORTH, PASS_BID));
        assertTrue(bidding.decision());
        assertNull(bidding.getContract());
        assertThrows(RuntimeException.class, () -> bidding.makeBid(EAST, PASS_BID));
    }

    @Test
    void sampleBidding() {
        assertTrue(bidding.makeBid(NORTH, PASS_BID));
        assertTrue(bidding.makeBid(EAST, new Bid(1, SPADES)));
        assertTrue(bidding.makeBid(SOUTH, new Bid(1, NO_TRUMP)));
        assertFalse(bidding.makeBid(WEST, new Bid(1, HEARTS)));
        assertTrue(bidding.makeBid(WEST, new Bid(2, SPADES)));
        assertTrue(bidding.makeBid(NORTH, PASS_BID));
        assertTrue(bidding.makeBid(EAST, PASS_BID));
        assertTrue(bidding.makeBid(SOUTH, PASS_BID));
        assertTrue(bidding.decision());
        assertEquals(new Contract(2, SPADES, EAST), bidding.getContract());
    }

    @Test
    void doubleFirstOpponent() {
        assertFalse(bidding.makeBid(NORTH, DOUBLE_BID));
        assertTrue(bidding.makeBid(NORTH, PASS_BID));
        assertFalse(bidding.makeBid(EAST, DOUBLE_BID));
        assertTrue(bidding.makeBid(EAST, new Bid(1, CLUBS)));
        assertTrue(bidding.makeBid(SOUTH, DOUBLE_BID));
        assertFalse(bidding.makeBid(WEST, DOUBLE_BID));
        assertTrue(bidding.makeBid(WEST, PASS_BID));
        assertFalse(bidding.makeBid(NORTH, DOUBLE_BID));
        assertTrue(bidding.makeBid(NORTH, PASS_BID));
        assertTrue(bidding.makeBid(EAST, PASS_BID));
        assertTrue(bidding.decision());
        assertEquals(new Contract(1, CLUBS, EAST, 2), bidding.getContract());
    }

    @Test
    void partnerCannotDouble() {
        bidding.makeBid(NORTH, new Bid(1, HEARTS));
        bidding.makeBid(EAST, PASS_BID);
        assertFalse(bidding.makeBid(SOUTH, DOUBLE_BID));
    }

    @Test
    void doubleSecondOpponent() {
        bidding.makeBid(NORTH, new Bid(7, DIAMONDS));
        bidding.makeBid(EAST, PASS_BID);
        bidding.makeBid(SOUTH, PASS_BID);
        assertTrue(bidding.makeBid(WEST, DOUBLE_BID));
        assertFalse(bidding.makeBid(NORTH, DOUBLE_BID));
        assertTrue(bidding.makeBid(NORTH, PASS_BID));
        assertFalse(bidding.makeBid(EAST, DOUBLE_BID));
        assertTrue(bidding.makeBid(EAST, PASS_BID));
        assertFalse(bidding.makeBid(SOUTH, DOUBLE_BID));
        assertTrue(bidding.makeBid(SOUTH, PASS_BID));
        assertTrue(bidding.decision());
        assertEquals(new Contract(7, DIAMONDS, NORTH, 2), bidding.getContract());
    }

    @Test
    void higherBidResetsDouble() {
        bidding.makeBid(NORTH, new Bid(5, HEARTS));
        bidding.makeBid(EAST, DOUBLE_BID);
        bidding.makeBid(SOUTH, PASS_BID);
        bidding.makeBid(WEST, new Bid(5, NO_TRUMP));
        bidding.makeBid(NORTH, PASS_BID);
        bidding.makeBid(EAST, PASS_BID);
        bidding.makeBid(SOUTH, PASS_BID);
        assertEquals(new Contract(5, NO_TRUMP, WEST, 1), bidding.getContract());
    }

    @Test
    void outOfOrderBidding() {
        bidding.makeBid(NORTH, new Bid(1, HEARTS));
        assertThrows(IllegalArgumentException.class, () -> bidding.makeBid(SOUTH, PASS_BID));
        bidding.makeBid(EAST, PASS_BID);
        bidding.makeBid(SOUTH, PASS_BID);
        bidding.makeBid(WEST, PASS_BID);
        assertEquals(new Contract(1, HEARTS, NORTH), bidding.getContract());
    }

    @Test
    void cannotRedoubleWithoutDouble() {
        bidding.makeBid(NORTH, new Bid(1, CLUBS));
        assertFalse(bidding.makeBid(EAST, REDOUBLE_BID));
        bidding.makeBid(EAST, PASS_BID);
        assertFalse(bidding.makeBid(SOUTH, REDOUBLE_BID));
        bidding.makeBid(SOUTH, PASS_BID);
        assertFalse(bidding.makeBid(WEST, REDOUBLE_BID));
        bidding.makeBid(WEST, PASS_BID);
        assertTrue(bidding.decision());
        assertEquals(new Contract(1, CLUBS, NORTH, 1), bidding.getContract());
    }

    @Test
    void redoublePartner() {
        bidding.makeBid(NORTH, new Bid(1, CLUBS));
        bidding.makeBid(EAST, DOUBLE_BID);
        assertTrue(bidding.makeBid(SOUTH, REDOUBLE_BID));

        assertFalse(bidding.makeBid(WEST, REDOUBLE_BID));
        bidding.makeBid(WEST, PASS_BID);
        assertFalse(bidding.makeBid(NORTH, REDOUBLE_BID));
        bidding.makeBid(NORTH, PASS_BID);
        assertFalse(bidding.makeBid(EAST, REDOUBLE_BID));
        bidding.makeBid(EAST, PASS_BID);
        assertEquals(new Contract(1, CLUBS, NORTH, 4), bidding.getContract());
    }


}
