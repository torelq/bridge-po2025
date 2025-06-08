package tcs.bridge.server;

import tcs.bridge.communication.messages.*;
import tcs.bridge.communication.streams.ClientMessageStream;
import tcs.bridge.communication.streams.PipedMessageStream;
import tcs.bridge.communication.streams.ServerMessageStream;
import tcs.bridge.communication.streams.TCPMessageStream;
import tcs.bridge.model.Game;
import tcs.bridge.model.Player;
import tcs.bridge.model.Scoring;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private Thread acceptorTh, mainLoopTh;
    BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>();
    private int port;
    private boolean verbose=false;
    private final CompletableFuture<Integer> portFuture = new CompletableFuture<>();
    private final ExecutorService clientHandlerExecutor = Executors.newCachedThreadPool();
    private final GameWrapper gameWrapper = new GameWrapper();

    private final MainLoop mainLoop = new MainLoop();

    private void acceptorThread(ServerSocket serverSocket) {
        try {
            serverSocket.setSoTimeout(400);
            try {
                while (!Thread.interrupted()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
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

    class GameWrapper {
        private Game game;
        private final Scoring scoring = new Scoring();
        private final Map<Player.Position, PlayerRecord> playerMap = new HashMap<>();

        static class PlayerRecord {
            Client client = null;
            Player player;
            String name;

            PlayerRecord (Player player) {
                this.player = player;
            }
        }

        GameWrapper() {
            for (Player.Position position : Player.Position.values()) {
                playerMap.put(position, new PlayerRecord(null));
            }
            initNewGame();
        }

        private void initNewGame() {
            synchronized (playerMap) {
                game = new Game();
                for (Player.Position position : Player.Position.values()) {
                    Player player = new Player(position);
                    game.joinGame(player);
                    playerMap.get(position).player = player;
                }
            }
        }

        private void gameFinished() {
            Scoring.ScoringEntry scoringEntry = game.getScoringEntry();
            scoring.addEntry(scoringEntry);
            sendAll(new GameFinishedNotice(scoringEntry));
        }

        Player.Position register(Client client, Player.Position position) {
            if (client.position!=null) return null;
            if (position==null) {
                for (Player.Position position1 : Player.Position.values()) {
                    if (register(client, position1)!=null) return position1;
                }
                return null;
            } else {
                synchronized (playerMap) {
                    PlayerRecord playerRecord = playerMap.get(position);
                    if (playerRecord.client!=null) return null;
                    playerRecord.client = client;
                    client.position = position;
                    return position;
                }
            }
        }

        Player.Position unregister(Client client) {
            if (client.position==null) return null;
            synchronized (playerMap) {
                Player.Position position = client.position;
                playerMap.get(position).client = null;
                client.position = null;
                return position;
            }
        }

        void sendAll(ServerToClientMessage message) {
            synchronized (playerMap) {
                for (Map.Entry<Player.Position, PlayerRecord> entry : playerMap.entrySet()) {
                    if (entry.getValue().client!=null) {
                        entry.getValue().client.clientHandler.writeMessage(message);
                    }
                }
            }
        }

        void handleUnregister(Client client) {
            Player.Position position = unregister(client);
            if (position!=null) sendAll(new LeaveGameNotice(position));
        }

        void handleJoinGame(Client client, JoinGameRequest joinGameRequest) {
            Player.Position positionActual = register(client, joinGameRequest.position());
            if (positionActual==null) {
                client.clientHandler.writeMessage(new RejectResponse());
                return;
            }
            synchronized (playerMap) {
                playerMap.get(positionActual).name = joinGameRequest.name();
            }
            client.clientHandler.writeMessage(new JoinGameRequest.AcceptResponse());
            sendAll(new JoinGameNotice(joinGameRequest.name(), positionActual));
        }

        void handleStateRequest(Client client) {
            client.clientHandler.writeMessage(new StateRequest.StateResponse(client.position, game));
        }

        void handleMakeBidRequest(Client client, MakeBidRequest makeBidRequest) {
            if (game.getState()==Game.State.BIDDING) {
                if (client.position==game.getCurrentTurn() && makeBidRequest.position()==client.position) {
                    if (game.makeBid(makeBidRequest.bid())) {
                        client.clientHandler.writeMessage(new MakeBidRequest.AcceptResponse());
                        sendAll(new MakeBidNotice(makeBidRequest.position(), makeBidRequest.bid()));
                        return;
                    }
                }
            }
            client.clientHandler.writeMessage(new RejectResponse());
        }

        void handlePlayCardRequest(Client client, PlayCardRequest playCardRequest) {
            if (game.getState()==Game.State.PLAYING) {
                // TODO: make sure players cannot play other's cards.
                if (game.playCard(playCardRequest.card())) {
                    client.clientHandler.writeMessage(new PlayCardRequest.AcceptResponse());
                    sendAll(new PlayCardNotice(playCardRequest.position(), playCardRequest.card()));

                    if (game.getState() == Game.State.FINISHED) gameFinished();
                    return;
                }
            }
            client.clientHandler.writeMessage(new RejectResponse());
        }

        void handleNewGameRequest(Client client, NewGameRequest newGameRequest) {
            initNewGame();
            client.clientHandler.writeMessage(new NewGameRequest.AcceptResponse());
            sendAll(new NewGameNotice());
        }

        void handleNicksRequest(Client client, NicksRequest nicksRequest) {
            Map<Player.Position, String> nicks = new HashMap<>();
            synchronized (playerMap) {
                for (Player.Position position : Player.Position.values()) {
                    if (playerMap.get(position).client==null) nicks.put(position, null);
                    else nicks.put(position, playerMap.get(position).name);
                }
            }
            client.clientHandler.writeMessage(new NicksRequest.NicksResponse(nicks));
        }

        void handleScoringRequest(Client client, ScoringRequest scoringRequest) {
            client.clientHandler.writeMessage(new ScoringRequest.ScoringResponse(scoring));
        }
    }

    private class MainLoop {
        private final Set<Client> clients = new HashSet<>();

        private void mainLoopThread() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Event event = eventQueue.take();
                    if (verbose) System.out.println(event);

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
                gameWrapper.handleUnregister(disconnectEvent.client());
                disconnectEvent.client().clientHandler.interrupt();
            }
        }

        private void handleMessageEvent(MessageEvent messageEvent) {
            Client client = messageEvent.from();
            ClientToServerMessage message = messageEvent.message();
            if (!clients.contains(client)) return;

            if (message instanceof StringMessage stringMessage) {
                handleStringMessage(client, stringMessage);
            } else if (message instanceof JoinGameRequest joinGameRequest) {
                gameWrapper.handleJoinGame(client, joinGameRequest);
            } else if (message instanceof StateRequest) {
                gameWrapper.handleStateRequest(client);
            } else if (message instanceof MakeBidRequest makeBidRequest) {
                gameWrapper.handleMakeBidRequest(client, makeBidRequest);
            } else if (message instanceof PlayCardRequest playCardRequest) {
                gameWrapper.handlePlayCardRequest(client, playCardRequest);
            } else if (message instanceof NewGameRequest newGameRequest) {
                gameWrapper.handleNewGameRequest(client, newGameRequest);
            } else if (message instanceof NicksRequest nicksRequest) {
                gameWrapper.handleNicksRequest(client, nicksRequest);
            } else if (message instanceof ScoringRequest scoringRequest) {
                gameWrapper.handleScoringRequest(client, scoringRequest);
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

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
