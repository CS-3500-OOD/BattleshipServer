package server;

import game.Player;

import game.Player;
import game.PlayerImp;
import game.Referee;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

  /**
   * Main loop of the GamesManager. This method first submits the helper instances of a ClientsAcceptor and
   * InputListener to the thread pool to work independently of the GamesManager. Then the main loop begins by looping
   * until a player has joined the game queue. The method then attempts to spawn a game for the first player in the
   * queue. When the server is stopped, then the loop ends, and the ExecutorService shuts down.
   */
  public void startHostingGames() {
    Server.logger.info("Submitting ClientsAcceptor and InputListener threads.");
    this.executorService.submit(clientsAcceptor::acceptClients);
    this.executorService.submit(inputListener::acceptInput);

    Server.logger.info("Starting to attempt spawning new games");
    while(!stopServerFlag) {
      if(!this.clientsWaitingToPlay.isEmpty()) {
        ProxyPlayer nextPlayer = this.clientsWaitingToPlay.remove(0);

        boolean success = this.attemptDelegateGameCreationForPlayer(nextPlayer);

        //unable to find a game, add the player to the end of the queue
        if(!success) {
          this.clientsWaitingToPlay.add(nextPlayer);
        }
      }
    }

    Server.logger.info("Shutting down server...");
    this.executorService.shutdown();
    this.executorService.shutdownNow();
  }


  /**
   * Add a server.Player to the queue to play a game.
   * @param player the player to add
   */
  public synchronized void addPlayerToQueue(ProxyPlayer player) {
    Server.logger.info("Adding player " + player.name() + " [" + player.getGameType() + "] to queue");
    this.clientsWaitingToPlay.add(player);
  }


  /**
   * Tell the server.GamesManager to stop the server.
   */
  public synchronized void stopServer() {
    this.stopServerFlag = true;
  }


  /**
   * Depending on the GameType of the given ProxyPlayer, attempt to spawn a game with that player.
   *
   * @param player the ProxyPlayer
   * @return if spawning the game was successful
   */
  private boolean attemptDelegateGameCreationForPlayer(ProxyPlayer player) {
    if(player.getGameType() == GameType.MULTI) {
      return this.attemptSpawnMultiPlayerGame(player);
    }
    else {
      return this.attemptSpawnCPUPlayerGame(player);
    }
  }

  /**
   * Attempt to spawn a game with the given player against another player waiting in queue.
   *
   * @param player1 Player 1
   * @return true if spawning the game was successful, false if not
   */
  private boolean attemptSpawnMultiPlayerGame(Player player1) {
    Optional<ProxyPlayer> player2 = getNextMultiPlayer();
    if(player2.isPresent()) {
      return attemptSpawnGame(player1, player2.get());
    }
    return false;
  }

  private Optional<ProxyPlayer> getNextMultiPlayer() {
    for (ProxyPlayer current : this.clientsWaitingToPlay) {
      if (current.getGameType() == GameType.MULTI) {
        return Optional.of(current);
      }
    }
    return Optional.empty();
  }


  /**
   * Attempt to spawn a game with one player against the Server CPU.
   *
   * @param player1 Player 1
   * @return true if spawning the game was successful, false if not
   */
  private boolean attemptSpawnCPUPlayerGame(Player player1) {
    Player cpu = new PlayerImp();
    return attemptSpawnGame(player1, cpu);
  }

  /**
   * Attempts to spawn a game in the ExecutorService Thread Pool.
   *
   * @param player1 Player 1
   * @param player2 Player 2
   * @return true if spawning the game was successful, false if not
   */
  private boolean attemptSpawnGame(Player player1, Player player2) {
    try {
      String gameId = UUID.randomUUID().toString();
      Referee referee = new Referee(player1, player2);
      this.executorService.submit(referee::run);
      Server.logger.info("Successfully spawned new game [" + gameId + "] (" + player1.name() + ", " + player2.name() + ")");
      return true;
    }
    catch(RejectedExecutionException e) {
      return false; // unable to start game, queue is full...
    }
  }
}
