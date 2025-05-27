package tcs.bridge.communication.messages;
import java.io.Serializable;

public interface Message extends Serializable {
    /*
        Each message shall be a record with maximal null protection via Objects.requireNonNull.
        All implementing classes shall be documented here:

        Client to Server:
        - MakeBidRequest (+ AcceptResponse)
        - JoinGameRequest (+ AcceptResponse)
        - StateRequest (+ StateResponse)

        Server to Client:
        - RejectResponse

        Others:
        - StringMessage (for test/debug purposes)

        As of now not all are documented. Not my problem.
    */
}
