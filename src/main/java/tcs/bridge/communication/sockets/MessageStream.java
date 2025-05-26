package tcs.bridge.communication.sockets;

import tcs.bridge.communication.messages.Message;

import java.io.Closeable;
import java.io.IOException;

public interface MessageStream extends Closeable {
    /*
        Instances of MessageStream should always be wrapped inside ClientMessageStream or ServerMessageStream.
        The MessageStream or the ClientMessageStream/ServerMessageStream have to be closed.
    */
    Message readMessage() throws IOException;
    void writeMessage(Message message) throws IOException;
}
