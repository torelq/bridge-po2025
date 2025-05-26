package tcs.bridge.communication.sockets;

import tcs.bridge.communication.messages.ServerToClientMessage;

import java.io.IOException;

public interface ClientMessageStream extends MessageStream {
    default ServerToClientMessage readMessageFromServer() throws IOException {
        return (ServerToClientMessage) readMessage();
    }
}
