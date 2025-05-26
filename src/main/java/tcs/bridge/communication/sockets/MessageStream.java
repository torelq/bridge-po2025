package tcs.bridge.communication.sockets;

import tcs.bridge.communication.messages.Message;

import java.io.IOException;

public interface MessageStream {
    Message readMessage() throws IOException;
    void writeMessage(Message message) throws IOException;
}
