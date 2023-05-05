package server;

import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class connects clients to opponents and manages all active games.
 * <p>
 * Each game is spawned in their own thread. When the game is over, the thread is recycled to be
 * used for another game.
 * <p>
 * Clients that have joined the server wait in a queue to be connected with an opponent when space
 * is available to host a new game.
 */
public class GamesManager {

  private static final int MAX_GAMES_RUNNING_AT_A_TIME = 10;
  private final ExecutorService executorService;
  private final ClientsAcceptor clientsAcceptor;
  private final InputListener inputListener;

  private final Queue<Player> clientsWaitingToPlay;

  private boolean stopServerFlag;

  public GamesManager(int port) {
    this.clientsAcceptor = new ClientsAcceptor(port, this);
    this.inputListener = new InputListener(this);
    this.clientsWaitingToPlay = new LinkedBlockingQueue<>();
    this.stopServerFlag = false;

    // add two for the server.ClientsAcceptor and server.InputListener
    int numThreadsInPool = MAX_GAMES_RUNNING_AT_A_TIME + 2;
    this.executorService = Executors.newFixedThreadPool(numThreadsInPool);
  }

  public void startHostingGames() {
    this.executorService.submit(clientsAcceptor::acceptClients);
    this.executorService.submit(inputListener::acceptInput);

    while(!stopServerFlag) {
      if(!this.clientsWaitingToPlay.isEmpty()) {
        //TODO: build logic for taking clients in queue and placing them in games.
        /*
          Notes for spawning games:
          - There needs to be an available thread to run the game
          - CPU requests at start of queue can be added immediately
          - MULTI requests need a second client with MULTI to start a game
          - should MULTI queued players timeout if they wait for a match for too long?
        */
      }
    }
    System.out.println("Shutting down server...");
    this.executorService.shutdown();
    this.executorService.shutdownNow();
  }

  /**
   * Add a server.Player to the queue to play a game.
   * @param player the player to add
   */
  public synchronized void addPlayerToQueue(Player player) {
    this.clientsWaitingToPlay.add(player);
  }

  /**
   * Tell the server.GamesManager to stop the server.
   */
  public synchronized void stopServer() {
    this.stopServerFlag = true;
  }
}
