package tcs.bridge.server;
import tcs.bridge.model.Player;

class Client {

    final ClientHandler clientHandler;
    Player.Position position=null;

    Client (ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }
}
