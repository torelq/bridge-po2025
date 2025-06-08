package tcs.bridge.communication.messages;

import tcs.bridge.model.Player;

import java.util.Map;
import java.util.Objects;

public record NicksRequest() implements ClientToServerMessage {

    public record NicksResponse(Map<Player.Position, String> nicks) implements ServerToClientMessage {
        public NicksResponse {
            Objects.requireNonNull(nicks);
        }
    }
}
