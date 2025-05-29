package tcs.bridge.model;

public enum Suit {
    DIAMONDS("Diamonds"),
    CLUBS("Clubs"),
    HEARTS("Hearts"),
    SPADES("Spades");

    private final String name;
    public static final Suit NO_TRUMP=null;

    Suit(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static String abbreviationOf(Suit suit) {
        if (suit==NO_TRUMP) return "NT";
        final String[] suits = {"♦", "♣", "♥", "♠"};
        return suits[suit.ordinal()];
    }

    @Override
    public String toString() {
        return abbreviationOf(this);
    }
}
