package tcs.bridge.communication.streams;


import tcs.bridge.communication.messages.Message;

import java.io.*;
import java.util.AbstractMap;

public class PipedMessageStream implements MessageStream {

    private final ObjectInputStream inputStream;
    private final ObjectOutputStream outputStream;

    private PipedMessageStream(ObjectInputStream inputStream, ObjectOutputStream outputStream) throws IOException {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
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

        ObjectOutputStream outputStream1 = new ObjectOutputStream(output1), outputStream2 = new ObjectOutputStream(output2);
        ObjectInputStream inputStream1 = new ObjectInputStream(input1), inputStream2 = new ObjectInputStream(input2);

        return new AbstractMap.SimpleEntry<>(new PipedMessageStream(inputStream1, outputStream2), new PipedMessageStream(inputStream2, outputStream1));
    }
}
