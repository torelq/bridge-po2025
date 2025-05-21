package tcs.bridge.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DeckTest {
    private Deck deck;

    @BeforeEach
    void setUp() {
        deck = new Deck();
    }

    @Test
    void new_deck_52_unique_cards_check() {
        List<Card> cards = deck.getCards();
        assertEquals(52, cards.size(), "Deck with 52 cards");
        Set<Card> uniqueCards = new HashSet<>(cards);
        assertEquals(52, uniqueCards.size(), "Deck with 52 unique cards");
    }

    @Test
    void deck_shuffle() {
        List<Card> before = new ArrayList<>(deck.getCards());
        List<Card> after = new ArrayList<>(deck.shuffle().getCards());
        assertNotEquals(before, after, "Deck shuffled");
    }
}