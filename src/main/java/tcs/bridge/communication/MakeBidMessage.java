package tcs.bridge.communication;

import tcs.bridge.model.Player.Position;
import tcs.bridge.model.Bidding.Bid;

import java.util.Objects;

public record MakeBidMessage(Position position, Bid bid) implements ClientToServerMessage {

    public MakeBidMessage {
        Objects.requireNonNull(position);
        Objects.requireNonNull(bid);
    }
}
