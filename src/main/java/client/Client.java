package client;

import game.Player;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import server.GameType;

public class Client {

  static final Logger logger = LogManager.getLogger(Client.class);

  public static void main(String[] args) {
    Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.ALL);
    System.out.println("Client start");
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
    System.out.println("Client end");
  }

  private static void spawnClients(String host, int port, int numClients) {
    System.out.println("Connecting " + numClients + " clients to " + host + ":" + port);

    ExecutorService service = Executors.newFixedThreadPool(numClients);
    List<Future<Boolean>> clients = new ArrayList<>();


    for(int i = 0; i < numClients; i++) {
      try {
        System.out.println("connecting... (" + i + ")");
        Socket server = new Socket(host, port);
        Player player = new NamedPlayer("Player_" + i);
        GameType type = GameType.SINGLE;
        Future<Boolean> future = service.submit(() -> {new ProxyReferee(server, player, type).run(); return true;});
        clients.add(future);
        System.out.println("Spawned player " + player);
      }
      catch (IOException e) {
        System.err.println("IO Exception: " + e);
      }
    }


    for(Future<Boolean> client : clients) {
      try {
        client.get();
      } catch (InterruptedException | ExecutionException e) {
        System.err.println("Client thread error: " + e);
      }
    }
  }
}
