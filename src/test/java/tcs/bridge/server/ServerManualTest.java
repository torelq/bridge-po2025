package tcs.bridge.server;

import tcs.bridge.communication.messages.ClientToServerMessage;
import tcs.bridge.communication.messages.StringMessage;
import tcs.bridge.communication.streams.ClientMessageStream;
import tcs.bridge.communication.streams.TCPMessageStream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class ServerManualTest {

    public static class ClientTest {

        private static void readerThread(ClientMessageStream clientMessageStream) {
            try {
                while (true) {
                    System.out.println(clientMessageStream.readMessage());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public static void main(String[] args) throws IOException, InterruptedException {
            Scanner scanner = new Scanner(System.in);
            MessageScanner messageScanner = new MessageScanner(scanner);

            int port = scanner.nextInt();
            try (ClientMessageStream clientMessageStream = new ClientMessageStream(new TCPMessageStream(new Socket("127.0.0.1", port)))) {
                Thread readerTh = new Thread(() -> readerThread(clientMessageStream));
                readerTh.start();

                while (true) {
                    ClientToServerMessage message = messageScanner.scanMessage();
                    if (message==null) {
                        clientMessageStream.close();
                        break;
                    }

                    clientMessageStream.writeMessage(message);
                }

                readerTh.join();
            }
        }

    }

    public static class ServerTest {

        public static void main(String[] args) throws InterruptedException {
            Scanner scanner = new Scanner(System.in);
            Server server = new Server();
            server.setVerbose(true);
            Thread serverTh = server.runInNewThread();
            System.out.println(server.getPort());

            while (true) {
                String s = scanner.nextLine();
                if (s.equals("H")) {
                    server.shutdown();
                    break;
                }
            }

            serverTh.join();
        }
    }
}
