package tcs.bridge.communication.messages;

import tcs.bridge.model.Card;
import tcs.bridge.model.Player;

import java.util.Objects;

public record PlayCardRequest(Player.Position position, Card card) implements ClientToServerMessage {

    public PlayCardRequest {
        Objects.requireNonNull(position);
        Objects.requireNonNull(card);
    }

    public record AcceptResponse() implements AcceptingResponse {}
}
