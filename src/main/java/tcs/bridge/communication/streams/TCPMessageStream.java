package tcs.bridge.communication.streams;

import tcs.bridge.communication.messages.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class TCPMessageStream implements MessageStream {
    private final Socket socket;
    private final ObjectInputStream objectInputStream;
    private final ObjectOutputStream objectOutputStream;

    public TCPMessageStream(Socket socket) throws IOException {
        this.socket = socket;
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectInputStream = new ObjectInputStream(socket.getInputStream());
    }

    public Message readMessage() throws IOException {
        try {
            return (Message) objectInputStream.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException();
        }
    }

    public void writeMessage(Message message) throws IOException {
        objectOutputStream.writeObject(message);
    }

    public void close() throws IOException {
        socket.close();
    }
}
