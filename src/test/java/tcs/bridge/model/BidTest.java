package tcs.bridge.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static tcs.bridge.model.Bidding.*;
import static tcs.bridge.model.Suit.*;

public class BidTest {

    @Test
    void bidComparison() {
        List<Bid> numericBids = List.of(new Bid(1, CLUBS), new Bid(1, HEARTS), new Bid(1, NO_TRUMP),
                new Bid(2, CLUBS), new Bid(2, DIAMONDS), new Bid (3, DIAMONDS), new Bid(3, SPADES),
                new Bid(6, NO_TRUMP), new Bid(7, CLUBS));

        for (int i=0; i<numericBids.size(); ++i) {
            for (int j=i+1; j<numericBids.size(); ++j) {
                assertTrue(numericBids.get(i).isGreaterThan(null));
                assertTrue(numericBids.get(j).isGreaterThan(numericBids.get(i)));
                assertFalse(numericBids.get(i).isGreaterThan(numericBids.get(j)));
            }
        }
    }
}
