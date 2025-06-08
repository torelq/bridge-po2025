package tcs.bridge.communication.messages;

import tcs.bridge.model.Scoring;

import java.util.Objects;

public record GameFinishedNotice(Scoring.ScoringEntry scoringEntry) implements ServerToClientMessage {

    public GameFinishedNotice {
        Objects.requireNonNull(scoringEntry);
    }
}
