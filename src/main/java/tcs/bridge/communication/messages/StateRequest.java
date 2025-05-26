package tcs.bridge.communication.messages;

import tcs.bridge.model.Game;
import tcs.bridge.model.Player;

import java.util.Objects;

public record StateRequest() implements ClientToServerMessage {

    public record StateResponse(Player.Position myPosition, Game game) implements AcceptingResponse {
        public StateResponse {
            Objects.requireNonNull(myPosition);
            Objects.requireNonNull(game);
        }
    }
}
