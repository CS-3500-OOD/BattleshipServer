package client;

import game.GameResult;
import game.Player;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import server.GameType;

public class Client {

  private static final String NAMES_PATH = "/Users/nickselvitelli/Desktop/cs3500_usernames.txt";

  public static void main(String[] args) {
    Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.ALL);

    System.out.println("Client Start...");

    if(args.length == 4) {
      try {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String name = args[2];
        int numClients = Integer.parseInt(args[3]);
        spawnClients(host, port, numClients, name, true);
        System.out.println("Client end.");
        System.exit(0);
      }
      catch (NumberFormatException e) {
        System.out.println("Usage: [host] [port] [name] [num clients]");
      }
    }
    else {
      System.out.println("Usage: [host] [port] [name] [num clients]");
    }
  }

  public static void spawnClients(String host, int port, int numClients, String name, boolean waitForFinish) {
    System.out.println("Connecting " + numClients + " clients to " + host + ":" + port);

    ExecutorService service = Executors.newFixedThreadPool(numClients);
    List<Future<GameResult>> clients = new ArrayList<>();

//    List<String> names = getNames();

    for (int i = 0; i < numClients; i++) {
      try {
        System.out.println("connecting... (" + i + ")");
        Socket server = new Socket(host, port);

        Player player = new NamedPlayer(numClients > 1 ? (name + "_" + i) : name);

        GameType type = GameType.MULTI;

        Future<GameResult> future = service.submit(() -> new ProxyReferee(server, player, type).run());
        clients.add(future);
        System.out.println("Spawned player " + player);
      } catch (IOException e) {
        System.err.println("IO Exception: " + e);
      }
    }

    if (waitForFinish) {
      Map<GameResult, Integer> results = new HashMap<>();

      for (Future<GameResult> client : clients) {
        try {
          GameResult result = client.get();
          results.put(result, results.getOrDefault(result, 0) + 1);
        } catch (InterruptedException | ExecutionException e) {
          System.err.println("Client thread error: " + e);
        }
      }
      System.out.println(results);
    }

  }


  private static List<String> getNames() {
    try {
      return Files.readAllLines(new File(NAMES_PATH).toPath());
    } catch (IOException e) {
      return Collections.emptyList();
    }
  }
}
