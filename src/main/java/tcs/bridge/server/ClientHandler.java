package tcs.bridge.server;

import tcs.bridge.communication.messages.ClientToServerMessage;
import tcs.bridge.communication.messages.ServerToClientMessage;
import tcs.bridge.communication.streams.MessageStream;
import tcs.bridge.communication.streams.ServerMessageStream;
import tcs.bridge.communication.streams.TCPMessageStream;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.*;

class ClientHandler {
    private boolean defunct=false, disconnectEventPut=false;
    final ServerMessageStream messageStream;
    private final BlockingQueue<ServerToClientMessage> outputQueue = new LinkedBlockingQueue<>(50);
    final BlockingQueue<Event> eventQueue;
    private final Client client;
    private final Future<?> readerFuture, writerFuture;
    private final CountDownLatch countDownLatch = new CountDownLatch(2);

    ClientHandler(ServerMessageStream messageStream, Client client, BlockingQueue<Event> eventQueue, ExecutorService executorService) {
        this.messageStream = messageStream;
        this.eventQueue = eventQueue;
        this.client = client;
        readerFuture = executorService.submit(this::readerThread);
        writerFuture = executorService.submit(this::writerThread);
    }

    private void readerThread() {
        try {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    ClientToServerMessage message = messageStream.readMessage();
                    eventQueue.put(new MessageEvent(client, message));
                }
            } catch (IOException | InterruptedException ignored) {}

            putDisconnectEvent();
        } finally {
            countDownLatch.countDown();
        }
    }

    private void writerThread() {
        try {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    ServerToClientMessage message = outputQueue.take();
                    messageStream.writeMessage(message);
                }
            } catch (IOException | InterruptedException ignored) {}

            putDisconnectEvent();
        } finally {
            countDownLatch.countDown();
        }
    }

    synchronized void putDisconnectEvent() {
        while ((!defunct) && (!disconnectEventPut)) {
            try {
                eventQueue.put(new DisconnectEvent(client));
                disconnectEventPut = true;
                return;
            } catch (InterruptedException ignored) {}
        }
    }

    synchronized void interrupt() {
        if (defunct) return;
        defunct = true;
        readerFuture.cancel(true);
        writerFuture.cancel(true);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException();
        }
    }

    synchronized void writeMessage(ServerToClientMessage message) {
        if (defunct) return;
        try {
            outputQueue.add(message);
        } catch (IllegalStateException e) {
            putDisconnectEvent();
        }
    }
}
