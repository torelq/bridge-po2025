package tcs.bridge.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TrickTest {
    Deck deck = new Deck();

    Player player1, player2, player3, player4;

    @BeforeEach
    void setUp() {
        deck.shuffle();
        player1 = new Player(Player.Position.NORTH, deck.deal().get(0));
        player2 = new Player(Player.Position.EAST, deck.deal().get(1));
        player3 = new Player(Player.Position.SOUTH, deck.deal().get(2));
        player4 = new Player(Player.Position.WEST, deck.deal().get(3));
    }

    @Test
    void trick_winner_test() {
        Trick trick = new Trick(Suit.HEARTS);
        assertThrows(IllegalStateException.class, trick::getWinner, "Trick not complete");

        dummyPlayCard(trick, Suit.HEARTS, player1);
        dummyPlayCard(trick, Suit.HEARTS, player2);
        dummyPlayCard(trick, Suit.HEARTS, player3);
        dummyPlayCard(trick, Suit.HEARTS, player4);
        assertTrue(trick.isComplete(), "Trick complete");
        Player winner = trick.getWinner();
        assertNotNull(winner, "Winner not null");
    }

    private void dummyPlayCard(Trick trick, Suit suit, Player player) {
        for (Card card : player.getCards()) {
            if (card.getSuit() == suit) {
                trick.PlayCard(player, card);
                return;

            }
        }
        trick.PlayCard(player, player.getCards().get(0));
    }
}
