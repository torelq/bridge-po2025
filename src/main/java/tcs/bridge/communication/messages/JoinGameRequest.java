package tcs.bridge.communication.messages;

import tcs.bridge.model.Player;

import java.util.Objects;

public record JoinGameRequest(String name, Player.Position position) implements ClientToServerMessage {

    // Position is nullable. It means you do not care where you sit.

    public JoinGameRequest {
        Objects.requireNonNull(name);
        if (name.isEmpty()) throw new IllegalArgumentException("Name cannot be empty.");
    }

    public record AcceptResponse() implements AcceptingResponse {}
}
