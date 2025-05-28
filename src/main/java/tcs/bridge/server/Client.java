package tcs.bridge.server;
import tcs.bridge.communication.messages.ClientToServerMessage;
import tcs.bridge.communication.messages.ServerToClientMessage;
import tcs.bridge.communication.streams.ServerMessageStream;
import tcs.bridge.model.Player;

import java.io.IOException;
import java.util.concurrent.*;

class Client {

    class ClientHandler {
        private boolean defunct=false, disconnectEventPut=false;
        private final ServerMessageStream messageStream;
        private final BlockingQueue<ServerToClientMessage> outputQueue = new LinkedBlockingQueue<>(50);
        final BlockingQueue<Event> eventQueue;
        private final Future<?> readerFuture, writerFuture;
        private final CountDownLatch countDownLatch = new CountDownLatch(2);
        private final Object putDisconnectEventLock = new Object();

        ClientHandler(ServerMessageStream messageStream, BlockingQueue<Event> eventQueue, ExecutorService executorService) {
            this.messageStream = messageStream;
            this.eventQueue = eventQueue;
            readerFuture = executorService.submit(this::readerThread);
            writerFuture = executorService.submit(this::writerThread);
        }

        private void readerThread() {
            try {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        ClientToServerMessage message = messageStream.readMessage();
                        eventQueue.put(new MessageEvent(Client.this, message));
                    }
                } catch (IOException | InterruptedException ignored) {}

                initiateDisconnection();
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

                initiateDisconnection();
            } finally {
                countDownLatch.countDown();
            }
        }

        private void initiateDisconnection() {
            synchronized (putDisconnectEventLock) {
                try {
                    messageStream.close();
                } catch (IOException ignored) {}

                while ((!defunct) && (!disconnectEventPut)) {
                    try {
                        eventQueue.put(new DisconnectEvent(Client.this));
                        disconnectEventPut = true;
                        return;
                    } catch (InterruptedException ignored) {}
                }
            }
        }

        synchronized void interrupt() {
            if (defunct) return;
            defunct = true;
            try {
                messageStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }
            readerFuture.cancel(true);
            writerFuture.cancel(true);
            while (true) {
                try {
                    countDownLatch.await();
                    return;
                } catch (InterruptedException ignored) {}
            }
        }

        synchronized void writeMessage(ServerToClientMessage message) {
            if (defunct) return;
            try {
                outputQueue.add(message);
            } catch (IllegalStateException e) {
                initiateDisconnection();
            }
        }
    }


    final ClientHandler clientHandler;
    Player.Position position=null;

    Client (ServerMessageStream messageStream, BlockingQueue<Event> eventQueue, ExecutorService executorService) {
        this.clientHandler = new ClientHandler(messageStream, eventQueue, executorService);
    }
}
