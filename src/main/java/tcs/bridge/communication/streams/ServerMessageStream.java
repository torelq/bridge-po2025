package tcs.bridge.communication.streams;

import tcs.bridge.communication.messages.ClientToServerMessage;
import tcs.bridge.communication.messages.ServerToClientMessage;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;

public class ServerMessageStream implements Closeable {
    private final MessageStream messageStream;

    public ServerMessageStream(MessageStream messageStream) {
        Objects.requireNonNull(messageStream);
        this.messageStream = messageStream;
    }

    @Override
    public void close() throws IOException {
        messageStream.close();
    }

    public ClientToServerMessage readMessage() throws IOException {
        while (true) if (messageStream.readMessage() instanceof ClientToServerMessage message) return message;
    }

    public void writeMessage(ServerToClientMessage message) throws IOException {
        Objects.requireNonNull(message);
        messageStream.writeMessage(message);
    }
}
