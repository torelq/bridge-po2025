package tcs.bridge.communication.messages;

import tcs.bridge.model.Scoring;

import java.util.Objects;

public record ScoringRequest() implements ClientToServerMessage {

    public record ScoringResponse(Scoring scoring) implements ServerToClientMessage {
        public ScoringResponse {
            Objects.requireNonNull(scoring);
        }
    }
}
