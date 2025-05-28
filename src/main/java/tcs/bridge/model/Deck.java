package tcs.bridge.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck implements Serializable {
    private final List<Card> cards;
    
    public Deck() {
        cards = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
    }

    public List<Card> getCards() {
        return cards;
    }

    public Deck shuffle() {
        Collections.shuffle(cards, new java.util.Random());
        return this;
    }

    public List<Hand> deal() {
        List<Hand> hands = new ArrayList<>();
        int cardsPerPlayer = cards.size() / 4;
        for (int i = 0; i < 4; i++) {
            List<Card> handCards = new ArrayList<>(cards.subList(i * cardsPerPlayer, (i + 1) * cardsPerPlayer));
            hands.add(new Hand(handCards).sort());
        }
        return hands;
    }

    
}