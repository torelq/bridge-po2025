package tcs.bridge.model;

public class Card {
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
        return rank + " of " + suit;
    }
}