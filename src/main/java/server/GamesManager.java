package server;

import game.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

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

  private final List<ProxyPlayer> clientsWaitingToPlay;

  private boolean stopServerFlag;

  public GamesManager(int port) {
    this.clientsAcceptor = new ClientsAcceptor(port, this);
    this.inputListener = new InputListener(this);
    this.clientsWaitingToPlay = Collections.synchronizedList(new ArrayList<>());
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
        ProxyPlayer nextPlayer = this.clientsWaitingToPlay.get(0);
        if(nextPlayer.getGameType() == GameType.MULTI) {

        }
        else {

        }
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

  public boolean attemptSpawnMultiPlayerGame(Player player1) {
    //TODO: find next player in queue that wants to do multi
    // if there exists a player with multi, get reference to it
    Player player2 = ...;
    return attemptSpawnGame(player1, player2);
    //TODO: if no player exists, move player to end of queue?
    return false;
  }

  public boolean attemptSpawnCPUPlayerGame(Player player1) {
    //TODO: make instance of CPU player
    Player cpu = ...;
    return attemptSpawnGame(player1, cpu);
  }

  public boolean attemptSpawnGame(Player player1, Player player2) {
    // TODO: make instance of referee with both players
    try {
      //TODO: attempt to spawn thread with GameReferee:startGame
      this.executorService.submit(...);
      return true;
    }
    catch(RejectedExecutionException e) {
      // unable to start game, queue is full...
      return false;
    }
  }

  /**
   * Add a server.Player to the queue to play a game.
   * @param player the player to add
   */
  public synchronized void addPlayerToQueue(ProxyPlayer player) {
    this.clientsWaitingToPlay.add(player);
  }

  /**
   * Tell the server.GamesManager to stop the server.
   */
  public synchronized void stopServer() {
    this.stopServerFlag = true;
  }
}
