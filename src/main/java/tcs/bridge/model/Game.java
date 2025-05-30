package tcs.bridge.model;

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tcs.bridge.model.Bidding.Bid;
import tcs.bridge.model.Player.Position;

/* THIS IS MAIN MODEL OF THIS APPLICATION */
public class Game implements Serializable {

    public enum State {
        PREGAME,
        BIDDING,
        PLAYING,
        FINISHED
    }

    private State state = State.PREGAME;
    private Position dealer;

    /*  DEALING THE DECK */
    private Deck deck;
    private final Map<Position, Player> players = new HashMap<>();
    /* ------------------------ */

    /*  BIDDING */
    private Bidding bidding = new Bidding();
    private Contract contract = null;
    private Position turn = null;
    /* ------------------------ */

    /*  PLAYING */
    private final List<Trick> completeTricks = new ArrayList<>();
    private Trick currentTrick;
    private int playedCards = 0;
    /* ------------------------ */

    public void joinGame(Player player) {
        Position position = player.getPosition();
        if (state != State.PREGAME) {
            throw new IllegalStateException("Game is not in pregame state");
        }
        if (players.containsKey(position)) {
            throw new IllegalArgumentException("Position already occupied");
        }
        if (players.isEmpty()) {
            dealer = position; // first to join is dealer
            turn = dealer; // first to join is dealer
        }
        players.put(position, player);
        if (players.size() == 4) {
            state = State.BIDDING;
            dealCardsToPlayers();
        }
    }

    public void leaveGame(Player player) {
        if (state != State.PREGAME) {
            throw new IllegalStateException("Game is not in pregame state");
        }
        if (!players.containsKey(player.getPosition())) {
            throw new IllegalArgumentException("Position not occupied");
        }
        players.remove(player.getPosition());
    }

    public boolean makeBid(Bid bid) {
        if (state != State.BIDDING) {
            throw new IllegalStateException("Game is not in bidding state");
        }
        boolean ok = bidding.makeBid(players.get(turn).getPosition(), bid);
        if (!ok) return false;
        if (bidding.toRedeal()) {
            dealCardsToPlayers();
            bidding = new Bidding();
            turn = dealer;
            return true;
        }
        if (bidding.decision()) {
            state = State.PLAYING;
            contract = bidding.getContract();
            turn = Position.next(contract.declarer); // left from the declarer
            currentTrick = new Trick(contract.trump);
        } else {
            turn = Position.next(turn);
        }
        return true;
    }

    private void dealCardsToPlayers() {
        if (players.size() != 4) {
            throw new IllegalStateException("Game is not full");
        }
        deck = new Deck().shuffle();
        List<Hand> hands = deck.deal();
        players.get(Position.NORTH).setHand(hands.get(0));
        players.get(Position.EAST).setHand(hands.get(1));
        players.get(Position.SOUTH).setHand(hands.get(2));
        players.get(Position.WEST).setHand(hands.get(3));
    }

    public void setDeck(Deck deck) {
        this.deck.getCards().clear();
        this.deck.getCards().addAll(deck.getCards());
    }

    public boolean canPlayCard(Card card) {
        if (state != State.PLAYING) {
            throw new IllegalArgumentException("Game is not in playing state");
        }
        Player player = players.get(turn);
        return currentTrick.canPlayCard(player, card);
    }

    /**
     * 
     * @param card
     * @return true if the card was successfully played
     */
    public boolean playCard(Card card) {
        if (!canPlayCard(card)) return false;
        Player player = players.get(turn);
        currentTrick.PlayCard(player, card);
        turn = Position.next(turn);
        playedCards++;
        if (currentTrick.isComplete()) {
            completeTricks.add(currentTrick);
            turn = currentTrick.getWinner().getPosition();
            if (completeTricks.size() == Trick.MAX_NUMBER_OF_TRICKS) {
                state = State.FINISHED;
            } else {
                currentTrick = new Trick(contract.trump);
            }
        }
        return true;
    }

    public SimpleEntry<Position, Position> getWinner() {
        if (state != State.FINISHED) {
            throw new IllegalStateException("Game is not finished");
        }
        Player declarer = players.get(contract.declarer);
        Player dummy = players.get(Position.teammate(contract.declarer));
        int goal = contract.level + 6;

        if (getGainedTricks() >= goal) {
            return new SimpleEntry<>(declarer.getPosition(), dummy.getPosition());
        } else {
            return new SimpleEntry<>(Position.next(dummy.getPosition()), Position.next(declarer.getPosition()));
        }
    }

    public State getState() {
        return state;
    }

    public List<Trick> getCompleteTricks() {
        return completeTricks;
    }

    public Deck getDeck() {
        return deck;
    }

    public Trick getCurrentTrick() {
        return currentTrick;
    }

    public Position getCurrentTurn(){
        return turn;
    }

    public int getNumberOfPlayedCards() {
        return playedCards;
    }

    public int getGainedTricks() {
        int wonTricks = 0;
        for (Trick trick : completeTricks) {
            if (trick.getWinner() == players.get(contract.declarer)
                    || trick.getWinner() == players.get(Position.teammate(contract.declarer))) {
                wonTricks++;
            }
        }
        return wonTricks;
    }

    public Contract getContract() {
        return contract;
    }

    public Map<Position, Player> getPlayers(){
        return players;
    }

    public Bidding getBidding() {
        return bidding;
    }
}
