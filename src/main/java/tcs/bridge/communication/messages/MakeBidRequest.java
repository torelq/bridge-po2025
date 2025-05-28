package tcs.bridge.communication.messages;

import tcs.bridge.model.Game;
import tcs.bridge.model.Player.Position;
import tcs.bridge.model.Bidding.Bid;

import java.util.Objects;

public record MakeBidRequest(Position position, Bid bid) implements ClientToServerMessage {

    public MakeBidRequest {
        Objects.requireNonNull(position);
        Objects.requireNonNull(bid);
    }

    public record AcceptResponse() implements AcceptingResponse {}
}
