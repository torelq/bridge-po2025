package tcs.bridge.communication.sockets;

import tcs.bridge.communication.messages.ClientToServerMessage;
import tcs.bridge.communication.messages.ServerToClientMessage;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;

public class ClientMessageStream implements Closeable {
    private final MessageStream messageStream;

    public ClientMessageStream(MessageStream messageStream) {
        Objects.requireNonNull(messageStream);
        this.messageStream = messageStream;
    }

    @Override
    public void close() throws IOException {
        messageStream.close();
    }

    public ServerToClientMessage readMessage() throws IOException {
        while (true) if (messageStream.readMessage() instanceof ServerToClientMessage message) return message;
    }

    public void writeMessage(ClientToServerMessage message) throws IOException {
        Objects.requireNonNull(message);
        messageStream.writeMessage(message);
    }
}
