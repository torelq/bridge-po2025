package tcs.bridge.server;

import tcs.bridge.communication.messages.StringMessage;
import tcs.bridge.communication.streams.ClientMessageStream;
import tcs.bridge.communication.streams.TCPMessageStream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class ServerManualTest {

    public static class ClientTest {

        public static void main(String[] args) throws IOException {
            Scanner scanner = new Scanner(System.in);

            int port = scanner.nextInt();
            try (ClientMessageStream clientMessageStream = new ClientMessageStream(new TCPMessageStream(new Socket("127.0.0.1", port)))) {
                while (true) {
                    String s = scanner.nextLine();
                    if (s.equals("D")) break;
                    clientMessageStream.writeMessage(new StringMessage(s));
                }
            }
        }

    }

    public static class ServerTest {

        public static void main(String[] args) throws InterruptedException {
            Scanner scanner = new Scanner(System.in);
            Server server = new Server();
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
