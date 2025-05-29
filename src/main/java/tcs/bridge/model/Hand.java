package tcs.bridge.model;

import java.util.ArrayList;
import java.util.List;

public class Hand {
    private final List<Card> cards;

    public Hand() {
        this.cards = new ArrayList<>();
    }
    public Hand(List<Card> cards) {
        this.cards = cards;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards.clear();
        this.cards.addAll(cards);
    }

    public Hand sort() {
        cards.sort(Card::compareTo);
        return this;
    }
    
    public boolean canPlay(Card card) {
        return cards.contains(card);
    }

    /**
     * 
     * @param card
     * @return true if the card was successfully played (without check for trump)
     */

    public boolean play(Card card) {
        return cards.remove(card);
    }

    /**
     * 
     * @param suit
     * @return checks of the hand has a card of the given suit
     */
    public boolean hasSuit(Suit suit) {
        return cards.stream().anyMatch(
            card -> { return card.getSuit() == suit; }
        );
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Card card : cards) {
            sb.append(card.toString()).append(" ");
        }
        return sb.toString().trim();
    }
}
