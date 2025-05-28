package tcs.bridge.communication.streams;

import org.junit.jupiter.api.Test;
import tcs.bridge.communication.messages.ClientToServerMessage;
import tcs.bridge.communication.messages.Message;
import tcs.bridge.communication.messages.StringMessage;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class PipedMessageStreamTest {

    void doSimpleTest(ClientMessageStream clientMessageStream, ServerMessageStream serverMessageStream) throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        AtomicInteger exceptionThrown= new AtomicInteger();
        List<Message> listIn = new ArrayList<>(), listOut = new ArrayList<>();
        for (int i=0; i!=1000; ++i) listIn.add(new StringMessage(Integer.toString(i)));

        Future<?> f1 = executorService.submit(() -> {
            int i=1;
            try {
                for (Message message : listIn) {
                    clientMessageStream.writeMessage((ClientToServerMessage) message);
                    // System.out.println("Sent: " + i);
                    ++i;
                }
            } catch (IOException e) {
                exceptionThrown.set(1);
                // System.out.println("E1: "+e.getMessage());
            }
            try {
                clientMessageStream.close();
            } catch (IOException e) {
                exceptionThrown.set(2);
            }
        });

        Future<?> f2 = executorService.submit(() -> {
            try {
                // int i=1;
                // System.out.println("T2");
                while (true) {
                    listOut.add(serverMessageStream.readMessage());
                    //  System.out.println("Received: "+i);
                    // ++i;
                }
            } catch (IOException e) {
                // System.out.println(e.getMessage());
            }
        });

        // System.out.println("test1");
        f1.get();
        // System.out.println("test2");
        f2.get();

        assertEquals(listIn, listOut);
        assertEquals(0, exceptionThrown.get());

        executorService.shutdownNow();
    }

    @Test
    void simpleTest() throws IOException, ExecutionException, InterruptedException {

        AbstractMap.SimpleEntry<PipedMessageStream, PipedMessageStream> entry = PipedMessageStream.makePipe();

        doSimpleTest(new ClientMessageStream(entry.getKey()), new ServerMessageStream(entry.getValue()));

        AbstractMap.SimpleEntry<PipedMessageStream, PipedMessageStream> entry2 = PipedMessageStream.makePipe();
        doSimpleTest(new ClientMessageStream(entry2.getValue()), new ServerMessageStream(entry2.getKey()));
    }
}
