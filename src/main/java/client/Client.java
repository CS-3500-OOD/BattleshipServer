package client;

import game.Player;
import game.PlayerImp;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import server.GameType;

public class Client {

  public static void main(String[] args) throws IOException {

    if(args.length >= 2) {

      String host = args[0];
      int port = Integer.parseInt(args[1]);

      if(args.length == 3) {
        spawnClients(host, port, Integer.parseInt(args[2]));
      }
      else {
        spawnClients(host, port, 1);
      }
    }
  }

  private static void spawnClients(String host, int port, int numClients) throws IOException {
    ExecutorService service = Executors.newFixedThreadPool(numClients);
    for(int i = 0; i < numClients; i++) {

      Socket server = new Socket(host, port);
      Player player = new NamedPlayer("Player_" + i);
      GameType type = GameType.MULTI;

      service.submit(new ProxyReferee(server, player, type));
    }
  }
}
