package client;

import game.Player;
import game.PlayerImp;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.GameType;

public class Client {

  static final Logger logger = LogManager.getLogger(Client.class);

  public static void main(String[] args) throws IOException {
    logger.info("Client start");
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
    logger.info("Client end");
  }

  private static void spawnClients(String host, int port, int numClients) {
    logger.info("Connecting " + numClients + " clients to " + host + ":" + port);
    ExecutorService service = Executors.newFixedThreadPool(numClients);
    for(int i = 0; i < numClients; i++) {
      try {
        logger.info("connecting... (" + i + ")");
        Socket server = new Socket(host, port);
        Player player = new NamedPlayer("Player_" + i);
        GameType type = GameType.MULTI;
        service.submit(new ProxyReferee(server, player, type));
        logger.info("Spawned player " + player);
      }
      catch (IOException e) {
        logger.error("IO Exception: " + e);
      }
    }
  }
}
