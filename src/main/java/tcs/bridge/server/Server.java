package tcs.bridge.server;

import tcs.bridge.communication.messages.ClientToServerMessage;
import tcs.bridge.communication.messages.StringMessage;
import tcs.bridge.communication.streams.ClientMessageStream;
import tcs.bridge.communication.streams.PipedMessageStream;
import tcs.bridge.communication.streams.ServerMessageStream;
import tcs.bridge.communication.streams.TCPMessageStream;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

public class Server {
    private Thread acceptorTh, mainLoopTh;
    BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>();
    private int port;
    private final CompletableFuture<Integer> portFuture = new CompletableFuture<>();
    private final ExecutorService clientHandlerExecutor = Executors.newCachedThreadPool();

    private final MainLoop mainLoop = new MainLoop();

    private void acceptorThread(ServerSocket serverSocket) {
        try {
            serverSocket.setSoTimeout(400);
            try {
                while (!Thread.interrupted()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("JO");
                        eventQueue.put(new TCPConnectEvent(clientSocket));
                    } catch (SocketTimeoutException ignored) {}
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException ignored) {
            } finally {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class MainLoop {
        private final Set<Client> clients = new HashSet<>();

        private void mainLoopThread() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Event event = eventQueue.take();

                    if (event instanceof ConnectEvent connectEvent) {
                        handleConnectEvent(connectEvent);
                    } else if (event instanceof DisconnectEvent disconnectEvent) {
                        handleDisconnectEvent(disconnectEvent);
                    } else if (event instanceof MessageEvent messageEvent) {
                        handleMessageEvent(messageEvent);
                    } else if (event instanceof ShutdownEvent) {
                        handleShutdownEvent();
                    } else {
                        throw new RuntimeException();
                    }

                } catch (InterruptedException ignored) {}
            }
        }

        private void handleConnectEvent(ConnectEvent connectEvent) {
            if (connectEvent instanceof LocalConnectEvent localConnectEvent) {
                clients.add(new Client(localConnectEvent.serverMessageStream(), eventQueue, clientHandlerExecutor));
            } else if (connectEvent instanceof TCPConnectEvent tcpConnectEvent) {
                try {
                    clients.add(new Client(new ServerMessageStream(new TCPMessageStream(tcpConnectEvent.socket())), eventQueue, clientHandlerExecutor));
                } catch (IOException e) {
                    try {
                        tcpConnectEvent.socket().close();
                    } catch (IOException e2) {
                        e.printStackTrace();
                        e2.printStackTrace();
                    }
                }

            } else {
                throw new RuntimeException();
            }
        }

        private void handleDisconnectEvent(DisconnectEvent disconnectEvent) {
            if (clients.contains(disconnectEvent.client())) {
                clients.remove(disconnectEvent.client());
                // TODO: Unregister the client
                disconnectEvent.client().clientHandler.interrupt();
            }
        }

        private void handleMessageEvent(MessageEvent messageEvent) {
            Client client = messageEvent.from();
            ClientToServerMessage message = messageEvent.message();
            if (!clients.contains(client)) return;

            if (message instanceof StringMessage stringMessage) {
                handleStringMessage(client, stringMessage);
            } else {
                throw new RuntimeException();
            }
        }

        private void handleShutdownEvent() {
            acceptorTh.interrupt();

            for (Client client : clients) {
                client.clientHandler.interrupt();
            }

            clientHandlerExecutor.shutdownNow();

            while (true) {
                try {
                    acceptorTh.join();
                    break;
                } catch (InterruptedException ignored) {}
            }

            while (!eventQueue.isEmpty()) {
                try {
                    Event event = eventQueue.take();
                    if (event instanceof TCPConnectEvent tcpConnectEvent) {
                        try {
                            tcpConnectEvent.socket().close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (InterruptedException ignored) {}
            }

            Thread.currentThread().interrupt();
        }

        private void handleStringMessage(Client client, StringMessage message) {
            System.out.println("SERVER: StringMessage from "+client.position+": \""+message+"\"");
        }
    }

    // Blocking
    private void run() {
        try {
            ServerSocket socket = new ServerSocket(0);
            port = socket.getLocalPort();
            portFuture.complete(port);
            acceptorTh = new Thread(() -> acceptorThread(socket));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        mainLoopTh = new Thread(mainLoop::mainLoopThread);
        acceptorTh.start();
        mainLoopTh.start();

        while (true) {
            try {
                acceptorTh.join();
                mainLoopTh.join();
                return;
            } catch (InterruptedException ignored) {}
        }
    }

    synchronized public Thread runInNewThread() {
        Thread thread = new Thread(this::run);
        thread.start();
        return thread;
    }

    synchronized public void shutdown() {
        while (true) {
            try {
                eventQueue.put(new ShutdownEvent());
                return;
            } catch (InterruptedException ignored) {}
        }
    }

    synchronized public ClientMessageStream localConnect() throws IOException, InterruptedException {
        AbstractMap.SimpleEntry<PipedMessageStream, PipedMessageStream> entry = PipedMessageStream.makePipe();
        ServerMessageStream serverMessageStream = new ServerMessageStream(entry.getKey());
        ClientMessageStream clientMessageStream = new ClientMessageStream(entry.getValue());
        eventQueue.put(new LocalConnectEvent(serverMessageStream));
        return clientMessageStream;
    }

    public int getPort() throws InterruptedException {
        try {
            return portFuture.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}
