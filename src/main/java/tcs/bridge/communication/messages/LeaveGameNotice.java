package tcs.bridge.communication.messages;

import tcs.bridge.model.Player;

import java.util.Objects;

public record LeaveGameNotice(Player.Position position) implements ServerToClientMessage {

    public LeaveGameNotice {
        Objects.requireNonNull(position);
    }
}
