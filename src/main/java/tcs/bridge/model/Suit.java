package tcs.bridge.model;

import java.io.Serializable;

public enum Suit implements Serializable {
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
        else return String.valueOf(suit.name.charAt(0));
    }
}
