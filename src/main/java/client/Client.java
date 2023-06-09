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
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import server.GameType;

public class Client {

  static final Logger logger = LogManager.getLogger(Client.class);

  private static final String NAMES_PATH = "/Users/nickselvitelli/Desktop/cs3500_usernames.txt";


  public static void main(String[] args) {
    Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.ALL);

    System.out.println("Client Start...");

    if(args.length == 3) {
      try {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String name = args[2];
        spawnClients(host, port, 1, name);
        System.out.println("Client end.");
        System.exit(0);
      }
      catch (NumberFormatException e) {
        System.out.println("Usage: [host] [port] [name]");
      }
    }
    else {
      System.out.println("Usage: [host] [port] [name]");
    }
  }

//  public static void main(String[] args) {
//    Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.ALL);
//
//    System.out.println("Client start");
//    if (args.length >= 2) {
//
//      String host = args[0];
//      int port = Integer.parseInt(args[1]);
//
//      if (args.length == 3) {
//        spawnClients(host, port, Integer.parseInt(args[2]));
//      } else {
//        spawnClients(host, port, 1);
//      }
//    }
//    System.out.println("Client end");
//    System.exit(0);
//  }

  private static void spawnClients(String host, int port, int numClients, String name) {
    System.out.println("Connecting " + numClients + " clients to " + host + ":" + port);

    ExecutorService service = Executors.newFixedThreadPool(numClients);
    List<Future<GameResult>> clients = new ArrayList<>();

//    List<String> names = getNames();

    for (int i = 0; i < numClients; i++) {
      try {
        System.out.println("connecting... (" + i + ")");
        Socket server = new Socket(host, port);

//        String name = names.isEmpty() ? ("NickPlayer_" + i) : names.remove(0);
        Player player = new NamedPlayer(name); //zoelmg

        GameType type = GameType.MULTI;

        Future<GameResult> future = service.submit(() -> new ProxyReferee(server, player, type).run());
        clients.add(future);
        System.out.println("Spawned player " + player);
      } catch (IOException e) {
        System.err.println("IO Exception: " + e);
      }
    }

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


  private static List<String> getNames() {
    try {
      return Files.readAllLines(new File(NAMES_PATH).toPath());
    } catch (IOException e) {
      return Collections.emptyList();
    }
  }
}
