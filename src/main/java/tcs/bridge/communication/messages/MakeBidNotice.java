package tcs.bridge.communication.messages;

import tcs.bridge.model.Bidding;
import tcs.bridge.model.Player;

import java.util.Objects;

public record MakeBidNotice(Player.Position position, Bidding.Bid bid) implements ServerToClientMessage {

    public MakeBidNotice {
        Objects.requireNonNull(position);
        Objects.requireNonNull(bid);
    }
}
