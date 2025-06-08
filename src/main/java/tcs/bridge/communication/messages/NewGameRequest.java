package tcs.bridge.communication.messages;

public record NewGameRequest() implements ClientToServerMessage {

    public record AcceptResponse() implements AcceptingResponse {}
}
