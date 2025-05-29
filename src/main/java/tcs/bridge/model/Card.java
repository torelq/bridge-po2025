package tcs.bridge.model;

import java.io.Serializable;

public class Card implements Comparable<Card>, Serializable {
    private final Suit suit;
    private final Rank rank;

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Card)) return false;
        Card card = (Card) obj;
        return suit == card.suit && rank == card.rank;
    }
    @Override
    public int hashCode() {
        return 31 * suit.hashCode() + rank.hashCode();
    }

    @Override
    public String toString() {
        return rank.toString() + suit.toString();
    }

    @Override
    public int compareTo(Card other) {
        if (this.suit != other.suit) {
            return this.suit.compareTo(other.suit);
        } else {
            return this.rank.compareTo(other.rank);
        }
    }
}