package tcs.bridge.model;

public enum Suit {
    HEARTS("Hearts"),
    DIAMONDS("Diamonds"),
    CLUBS("Clubs"),
    SPADES("Spades");

    private final String name;

    Suit(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static String abbreviationOf(Suit suit) {
        if (suit==null) return "NT";
        else return String.valueOf(suit.name.charAt(0));
    }
}