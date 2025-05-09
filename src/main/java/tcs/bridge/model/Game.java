package tcs.bridge.model;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tcs.bridge.model.Bidding.Bid;
import tcs.bridge.model.Player.Position;

public class Game {

    public enum State {
        BIDDING,
        PLAYING,
        FINISHED
    }

    private State state = State.BIDDING;

    /*  DEALING THE DECK */
    private final Deck deck = new Deck().shuffle();
    private final List<Hand> hands = deck.deal();
    private final Map<Position, Player> players = Map.of(
            Position.NORTH, new Player(Position.NORTH, hands.get(0)),
            Position.EAST, new Player(Position.EAST, hands.get(1)),
            Position.SOUTH, new Player(Position.SOUTH, hands.get(2)),
            Position.WEST, new Player(Position.WEST, hands.get(3))
    );
    /* ------------------------ */

    /*  BIDDING */
    private final Bidding bidding = new Bidding();
    private Position turn = Position.NORTH;
    /* ------------------------ */

    /*  PLAYING */
    private final List<Trick> completeTricks = new ArrayList<>();
    private Trick currentTrick;
    /* ------------------------ */

    public boolean makeBid(Bid bid) {
        if (state != State.BIDDING) {
            throw new IllegalStateException("Game is not in bidding state");
        }
        boolean ok = bidding.makeBid(players.get(turn), bid);
        if (bidding.decision()) {
            state = State.PLAYING;
            turn = bidding.getDeclarer().getPosition();
        } else {
            turn = turn.next(turn);
        }
        return ok;
    }

    public void playCard(Card card) {
        if (state != State.PLAYING) {
            throw new IllegalStateException("Game is not in playing state");
        }
        Player player = players.get(turn);
        currentTrick.PlayCard(player, card);
        if (currentTrick.isComplete()) {
            completeTricks.add(currentTrick);
            turn = currentTrick.getWinner().getPosition();
            if (completeTricks.size() == 13) {
                state = State.FINISHED;
            } else {                
                currentTrick = new Trick(bidding.getTrump());
            }
        }
    }

    public SimpleEntry<Position, Position> getWinner() {
        if (state != State.FINISHED) {
            throw new IllegalStateException("Game is not finished");
        }
        Player declarer = bidding.getDeclarer();
        Player dummy = bidding.getDummy();
        int goal = bidding.getLevel() + 6;
        int wonTricks = 0;
        for (Trick trick : completeTricks) {
            if (trick.getWinner() == declarer || trick.getWinner() == dummy) {
                wonTricks++;
            }
        }
        if (wonTricks >= goal) {
            return new SimpleEntry<>(declarer.getPosition(), dummy.getPosition());
        } else {
            return new SimpleEntry<>(dummy.getPosition().next(dummy.getPosition()), declarer.getPosition().next(declarer.getPosition()));
        }
    }
}
