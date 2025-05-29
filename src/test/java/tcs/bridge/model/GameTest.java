package tcs.bridge.model;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import tcs.bridge.model.Bidding.Bid;



class GameTest {

    Player playerNorth = new Player(Player.Position.NORTH);
    Player playerEast = new Player(Player.Position.EAST);
    Player playerSouth = new Player(Player.Position.SOUTH);
    Player playerWest = new Player(Player.Position.WEST);

    @Test
    void testJoinGameSuccessfully() {
        Game game = new Game();

        game.joinGame(playerNorth);

        assertThrows(IllegalArgumentException.class, () -> game.joinGame(playerNorth));
    }

    @Test
    void testJoinGameNotInPregameState() {
        Game game = new Game();

        game.joinGame(playerNorth);
        game.joinGame(playerEast);
        game.joinGame(playerSouth);
        game.joinGame(playerWest);

        assertThrows(IllegalStateException.class, () -> game.joinGame(playerNorth));
    }

    @Test
    void testLeaveGameSuccessfully() {
        Game game = new Game();

        game.joinGame(playerNorth);
        game.leaveGame(playerNorth);

        assertThrows(IllegalArgumentException.class, () -> game.leaveGame(playerNorth));
    }
    
    @Test
    void testLeaveGameNotInPregameState() {
        Game game = new Game();
    
        game.joinGame(playerNorth);
        game.joinGame(playerEast);
        game.joinGame(playerSouth);
        game.joinGame(playerWest);

        assertThrows(IllegalStateException.class, () -> game.leaveGame(playerNorth));
    }
    
    @Test
    void testMakeBidSuccessfully() {
        Game game = new Game();

        game.joinGame(playerNorth);
        game.joinGame(playerEast);
        game.joinGame(playerSouth);
        game.joinGame(playerWest);

        Bid bid = new Bid(1, Suit.HEARTS);
        assertTrue(game.makeBid(bid));
    }
    
    @Test
    void check_sorted_Hand() {
        Game game = new Game();
        game.joinGame(playerNorth);
        game.joinGame(playerEast);
        game.joinGame(playerSouth);
        game.joinGame(playerWest);

        Bid bid = new Bid(1, Suit.HEARTS);
        Bid passBid = Bid.PASS_BID;
        game.makeBid(bid);
        game.makeBid(passBid);
        game.makeBid(passBid);
        game.makeBid(passBid);

        assertEquals(Game.State.PLAYING, game.getState());
        
        List<Card> hand = playerNorth.getCards();
        for (int i = 0; i < hand.size() - 1; i++) {
            assertTrue(hand.get(i).compareTo(hand.get(i + 1)) <= 0, "Hand is not sorted");
        }
        hand = playerEast.getCards();
        for (int i = 0; i < hand.size() - 1; i++) {
            assertTrue(hand.get(i).compareTo(hand.get(i + 1)) <= 0, "Hand is not sorted");
        }
        hand = playerSouth.getCards();
        for (int i = 0; i < hand.size() - 1; i++) {
            assertTrue(hand.get(i).compareTo(hand.get(i + 1)) <= 0, "Hand is not sorted");
        }
        hand = playerWest.getCards();
        for (int i = 0; i < hand.size() - 1; i++) {
            assertTrue(hand.get(i).compareTo(hand.get(i + 1)) <= 0, "Hand is not sorted");
        }
    }

    @Test
    void testMakeBidNotInBiddingState() {
        Game game = new Game();
        
        assertThrows(IllegalStateException.class, () -> game.makeBid(new Bid(1, Suit.HEARTS)));
    }
    
    @Test
    void testPlayCardSuccessfully() {
        Game game = new Game();

        game.joinGame(playerNorth);
        game.joinGame(playerEast);
        game.joinGame(playerSouth);
        game.joinGame(playerWest);

        Bid bid = new Bid(1, Suit.HEARTS);
        Bid passBid = Bid.PASS_BID;
        game.makeBid(bid);
        game.makeBid(passBid);
        game.makeBid(passBid);
        game.makeBid(passBid);

        assertEquals(Game.State.PLAYING, game.getState());

        Card card = playerEast.getCards().get(0); // left from north
        assertDoesNotThrow(() -> game.playCard(card));
    }
    
    @Test
    void testGetWinnerSuccessfully() {
        Game game = new Game();
        game.joinGame(playerNorth);
        game.joinGame(playerEast);
        game.joinGame(playerSouth);
        game.joinGame(playerWest);

        Bid bid = new Bid(1, Suit.HEARTS);
        Bid passBid = Bid.PASS_BID;
        game.makeBid(bid);
        game.makeBid(passBid);
        game.makeBid(passBid);
        game.makeBid(passBid);

        assertEquals(Game.State.PLAYING, game.getState());

        for (int i = 0; i < 13; i++) {
            int count = 0;
            for (int j = 0; j < 4; j++) {
                Deck sampleDeck = new Deck();
                for (Card card : sampleDeck.getCards()) {
                    if (game.playCard(card)) {
                        count++;
                        break;
                    }
                }
            }
            assertEquals(i + 1, game.getCompleteTricks().size());
            assertEquals("4 " + i, String.valueOf(count) + " " +  String.valueOf(i));
        }

        List<Trick> tricks = game.getCompleteTricks();
        assertEquals(13, tricks.size());
        assertEquals(Game.State.FINISHED, game.getState());
        SimpleEntry<Player.Position, Player.Position> winner = game.getWinner();
        assertNotNull(winner);
    }

    public static void main(String[] args) {
        Game game = new Game();
        Player playerNorth = new Player(Player.Position.NORTH);
        Player playerEast = new Player(Player.Position.EAST);
        Player playerSouth = new Player(Player.Position.SOUTH);
        Player playerWest = new Player(Player.Position.WEST);
        game.joinGame(playerNorth);
        game.joinGame(playerEast);
        game.joinGame(playerSouth);
        game.joinGame(playerWest);

        System.out.println(game.toString());

        Bid bid = new Bid(1, Suit.HEARTS);
        Bid passBid = Bid.PASS_BID;
        game.makeBid(bid);
        game.makeBid(passBid);
        game.makeBid(passBid);
        game.makeBid(passBid);

        System.out.println(game.toString());

        for (int i = 0; i < 13; i++) {
            int count = 0;
            for (int j = 0; j < 4; j++) {
                Deck sampleDeck = new Deck();
                for (Card card : sampleDeck.getCards()) {
                    if (game.playCard(card)) {
                        count++;
                        break;
                    }
                }
            }
            // System.out.println(game.getCompleteTricks().size());
        }
        
        System.out.println(game.toString());

        List<Trick> tricks = game.getCompleteTricks();
        SimpleEntry<Player.Position, Player.Position> winner = game.getWinner();
        System.out.println(game.toString());
    }
}