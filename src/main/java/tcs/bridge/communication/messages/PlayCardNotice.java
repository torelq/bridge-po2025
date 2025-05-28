package tcs.bridge.communication.messages;

import tcs.bridge.model.Card;
import tcs.bridge.model.Player;

import java.util.Objects;

public record PlayCardNotice(Player.Position position, Card card) implements ServerToClientMessage {

    public PlayCardNotice {
        Objects.requireNonNull(position);
        Objects.requireNonNull(card);
    }
}
