package tcs.bridge.communication;

import java.util.Objects;

public record StringMessage(String text) implements ClientToServerMessage, ServerToClientMessage {

    public StringMessage {
        Objects.requireNonNull(text);
    }
}
