package tcs.bridge.communication.streams;


import tcs.bridge.communication.messages.Message;

import java.io.*;
import java.util.AbstractMap;

public class PipedMessageStream implements MessageStream {

    private final ObjectInputStream inputStream;
    private final ObjectOutputStream outputStream;

    private PipedMessageStream(PipedInputStream inputStream, PipedOutputStream outputStream) throws IOException {
        this.inputStream = new ObjectInputStream(inputStream);
        this.outputStream = new ObjectOutputStream(outputStream);
    }

    @Override
    public Message readMessage() throws IOException {
        try {
            return (Message) inputStream.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public void writeMessage(Message message) throws IOException {
        outputStream.writeObject(message);
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
        outputStream.close();
    }

    public static AbstractMap.SimpleEntry<PipedMessageStream, PipedMessageStream> makePipe() throws IOException {
        PipedOutputStream output1 = new PipedOutputStream();
        PipedInputStream input1 = new PipedInputStream(output1);
        PipedOutputStream output2 = new PipedOutputStream();
        PipedInputStream input2 = new PipedInputStream(output2);
        return new AbstractMap.SimpleEntry<>(new PipedMessageStream(input1, output2), new PipedMessageStream(input2, output1));
    }
}
