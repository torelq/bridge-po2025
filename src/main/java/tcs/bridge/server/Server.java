package tcs.bridge.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private Thread acceptorTh, mainLoopTh;
    BlockingQueue<Event> eventQueue;
    private int port;
    private final ExecutorService clientHandlerExecutor = Executors.newCachedThreadPool();

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

    private void mainLoopThread() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Event event = eventQueue.take();
            } catch (InterruptedException ignored) {}
        }
    }

    // Blocking
    private void run() {
        try {
            ServerSocket socket = new ServerSocket(0);
            port = socket.getLocalPort();
            acceptorTh = new Thread(() -> acceptorThread(socket));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        mainLoopTh = new Thread(this::mainLoopThread);
        acceptorTh.start();
        mainLoopTh.start();

        while (true) {
            try {
                acceptorTh.join();
                mainLoopTh.join();
            } catch (InterruptedException ignored) {}
        }
    }

    synchronized public Thread runInNewThread() {
        Thread thread = new Thread(this::run);
        thread.start();
        return thread;
    }
}
