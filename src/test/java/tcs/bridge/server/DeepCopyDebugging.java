package tcs.bridge.server;

import tcs.bridge.communication.messages.StateRequest;
import tcs.bridge.communication.streams.PipedMessageStream;
import tcs.bridge.model.*;

import java.io.IOException;
import java.util.AbstractMap;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DeepCopyDebugging {

    public static class Test1 {

        private static Game copyGame(Game game) throws IOException {
            AbstractMap.SimpleEntry<PipedMessageStream, PipedMessageStream> entry = PipedMessageStream.makePipe();

            new Thread(() -> {
                try {
                    entry.getKey().writeMessage(new StateRequest.StateResponse(null, game));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();

            return  ((StateRequest.StateResponse) entry.getValue().readMessage()).game();
        }

        public static void main(String[] args) throws IOException {

            try {
                test(false);
            } catch (AssertionError e) {
                e.printStackTrace();
            }

            try {
                test(true);
            } catch (AssertionError e) {
                e.printStackTrace();
            }

        }

        private static void test(boolean rev) throws IOException {
            System.out.println("rev: "+rev);
            Game game = new Game();
            for (Player.Position position : Player.Position.values()) {
                Player player = new Player(position);
                game.joinGame(player);
            }

            assertTrue(game.makeBid(new Bidding.Bid(1, Suit.CLUBS)));
            assertTrue(game.makeBid(Bidding.Bid.PASS_BID));
            assertTrue(game.makeBid(Bidding.Bid.PASS_BID));
            assertTrue(game.makeBid(Bidding.Bid.PASS_BID));

            Game game2 = copyGame(game);

            System.out.println("game:\n"+game+"\n\n");
            System.out.println("game2:\n"+game2+"\n\n");

            Card card = game.getPlayers().get(Player.Position.EAST).getCards().get(0);
            System.out.println("card: "+card);
            if (rev) assertTrue(game2.playCard(card));
            assertTrue(game.playCard(card));
            if (!rev) assertTrue(game2.playCard(card));
        }

    }

}
