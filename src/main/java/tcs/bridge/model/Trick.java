package tcs.bridge.model;

import java.util.HashMap;
import java.util.Map;

public class Trick {
    private final Suit trump; // null of no trump
    private Suit leadingSuit = null; // not played yet
    private final Map<Player, Card> plays = new HashMap<>();

    public Trick(Suit trump) {
        this.trump = trump;
    }

    public Suit getTrump() {
        return trump;
    }

    public Suit getLeadingSuit() {
        return leadingSuit;
    }

    public boolean PlayCard(Player player, Card card) {
        if (leadingSuit == null) {
            leadingSuit = card.getSuit();
            boolean ok = player.play(card);
            if (ok) plays.put(player, card);
            return ok;
        } else {
            if (card.getSuit() == leadingSuit || !player.hasSuit(leadingSuit)) {
                boolean ok = player.play(card);
                if (ok) plays.put(player, card);
                return ok;
            } else {
                return false;
            }
        }
    }

    public boolean isComplete() {
        return plays.size() == 4;
    }
    
    public Player getWinner() {
        if (!isComplete()) {
            throw new IllegalStateException("Trick is not complete");
        }
        Player winner = null;
        Card bestCard = null;
        for (var entry : plays.entrySet()) {
            Player player = entry.getKey();
            Card card = entry.getValue();
            if (bestCard == null) {
                bestCard = card;
                winner = player;
            } else if (card.getSuit() == trump) {
                if (bestCard.getSuit() != trump || card.getRank().compareTo(bestCard.getRank()) > 0) {
                    bestCard = card;
                    winner = player;
                }
            } else if (card.getSuit() == leadingSuit) {
                if (bestCard.getSuit() != trump && card.getRank().compareTo(bestCard.getRank()) > 0) {
                    bestCard = card;
                    winner = player;
                }
            }
        }
        return winner;
    }
}
