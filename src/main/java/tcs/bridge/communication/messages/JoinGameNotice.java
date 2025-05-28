package tcs.bridge.communication.messages;

import tcs.bridge.model.Player;

import java.util.Objects;

public record JoinGameNotice(String name, Player.Position position) implements ServerToClientMessage {

    public JoinGameNotice {
        Objects.requireNonNull(name);
        Objects.requireNonNull(position);
        if (name.isEmpty()) throw new IllegalArgumentException("Name cannot be empty.");
    }
}
