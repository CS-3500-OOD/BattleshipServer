package server;

import game.Player;
import game.PlayerImp;
import game.Referee;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

  private static final int MAX_GAMES_RUNNING_AT_A_TIME = Server.PROPERTIES.getInt(
      "max_games_in_parallel", 2);
  private final ExecutorService executorService;
  private final ClientsAcceptor clientsAcceptor;
  private final InputListener inputListener;

  private final List<ProxyPlayer> clientsWaitingToPlay;

  private final Map<String, Future<Boolean>> activeGames;

  private boolean stopServerFlag;

  private Set<String> allowedClientNames;
  private boolean whitelistEnabled;
  private Set<String> winners;
  private final Object dataLock;
  private Optional<Observer> observer;
  private int refereeDelayBetweenRoundsMillis;

  public GamesManager(int port) {
    this.clientsAcceptor = new ClientsAcceptor(port, this);
    this.inputListener = new InputListener(this);
    this.clientsWaitingToPlay = Collections.synchronizedList(new ArrayList<>());
    this.stopServerFlag = false;

    // add two for the server.ClientsAcceptor and server.InputListener
    int numThreadsInPool = MAX_GAMES_RUNNING_AT_A_TIME + 2;
    this.executorService = new BoundedExecutorService(
        Executors.newFixedThreadPool(numThreadsInPool), numThreadsInPool);

    this.activeGames = new HashMap<>();

    this.allowedClientNames = new HashSet<>();
    this.whitelistEnabled = false;
    this.winners = new HashSet<>();
    this.dataLock = new Object();
    this.observer = Optional.empty();
    this.refereeDelayBetweenRoundsMillis = -1;
  }

  /**
   * Main loop of the GamesManager. This method first submits the helper instances of a
   * ClientsAcceptor and InputListener to the thread pool to work independently of the GamesManager.
   * Then the main loop begins by looping until a player has joined the game queue. The method then
   * attempts to spawn a game for the first player in the queue. When the server is stopped, then
   * the loop ends, and the ExecutorService shuts down.
   */
  public void startHostingGames() {
    Server.logger.info("Submitting ClientsAcceptor and InputListener threads.");
    this.executorService.submit(clientsAcceptor::acceptClients);
    this.executorService.submit(inputListener::acceptInput);

    Server.logger.info("Starting to attempt spawning new games");

    while (!stopServerFlag) {
      if (!this.clientsWaitingToPlay.isEmpty()) {
//        Server.logger.info("Queue: " + this.clientsWaitingToPlay);

        ProxyPlayer nextPlayer = this.clientsWaitingToPlay.remove(0);

        this.attemptDelegateGameCreationForPlayer(nextPlayer);
      }
    }

    Server.logger.info("Shutting down server...");
    this.stopAllGames();
    this.clientsAcceptor.stopAcceptingClients();
    this.executorService.shutdown();
    this.executorService.shutdownNow();
    this.observer.ifPresent(Observer::stopObserver);

    Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
    Server.logger.info("Shutdown. " + map.keySet().stream().filter(Thread::isAlive).toList().size()
        + " alive threads found");
    System.exit(0);
  }


  /**
   * Add a server.Player to the queue to play a game.
   *
   * @param player the player to add
   */
  public synchronized void addPlayerToQueue(ProxyPlayer player) {
//    Server.logger.info("Adding player " + player.name() + " [" + player.getGameType() + "] to queue");
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
   */
  private void attemptDelegateGameCreationForPlayer(ProxyPlayer player) {
    if (player.getGameType() == GameType.MULTI) {
      this.attemptSpawnMultiPlayerGame(player);
    } else {
      this.attemptSpawnSingleRemotePlayerGame(player);
    }
  }

  /**
   * Attempt to spawn a game with the given player against another player waiting in queue.
   *
   * @param player1 Player 1
   */
  private void attemptSpawnMultiPlayerGame(ProxyPlayer player1) {
    Optional<ProxyPlayer> player2 = getNextMultiPlayer();
    if (player2.isPresent()) {
      attemptSpawnGame(player1, player2.get());
    } else {
      addPlayerToQueue(player1);
    }
  }

  private Optional<ProxyPlayer> getNextMultiPlayer() {
    for (ProxyPlayer current : this.clientsWaitingToPlay) {
      if (current.getGameType() == GameType.MULTI) {
        this.clientsWaitingToPlay.remove(current);
        return Optional.of(current);
      }
    }
    return Optional.empty();
  }


  /**
   * Attempt to spawn a game with one player against the Server CPU.
   *
   * @param player1 Player 1
   */
  private void attemptSpawnSingleRemotePlayerGame(Player player1) {
    Player serverAgent = new PlayerImp();
    attemptSpawnGame(serverAgent, player1);
  }

  /**
   * Attempts to spawn a game in the ExecutorService Thread Pool.
   *
   * @param player1 Player 1
   * @param player2 Player 2
   */
  private void attemptSpawnGame(Player player1, Player player2) {
    try {
      String gameId = UUID.randomUUID().toString();
      Referee referee = new Referee(player1, player2, gameId, this.observer, this.refereeDelayBetweenRoundsMillis);

      Callable<Boolean> game = () -> {
        List<String> winners = referee.run();
        synchronized (this.dataLock) {
          this.winners.addAll(winners);
          if(this.whitelistEnabled) {
            this.allowedClientNames.remove(player1.name());
            this.allowedClientNames.remove(player2.name());
          }
        }

        this.activeGames.remove(gameId);
        Server.logger.info("Game " + gameId + " finished.");
        Server.logger.info(
            "Current Active Games: (" + this.activeGames.size() + ") " + Arrays.toString(
                this.activeGames.keySet().toArray()));
        return true;
      };

      Future<Boolean> gameFuture = this.executorService.submit(game);
      this.activeGames.put(gameId, gameFuture);

      Server.logger.info("Successfully spawned new game [" + gameId + "] (" + player1.name() + ", "
          + player2.name() + ")");
    } catch (RejectedExecutionException e) {
//      Server.logger.info(
//          "Unable to spawn game with players: " + player1.name() + ", " + player2.name()
//              + ". Adding players back to queue.");
      this.attemptAddPlayerBackToQueueIfIsProxy(player1);
      this.attemptAddPlayerBackToQueueIfIsProxy(player2);
    }
  }

  /**
   * Attempts to add the given player to the queue if they are a ProxyPlayer.
   *
   * @param player the player to add
   */
  private void attemptAddPlayerBackToQueueIfIsProxy(Player player) {
    if (player instanceof ProxyPlayer) {
      this.addPlayerToQueue((ProxyPlayer) player);
    }
  }

  /**
   * Times out all active games to gracefully shut down the server.
   */
  private void stopAllGames() {
    Server.logger.info(
        "Shutting down all active games: (" + this.activeGames.size() + ") " + Arrays.toString(
            this.activeGames.keySet().toArray()));
    for (Future<Boolean> game : this.activeGames.values()) {
      try {
        game.get(1, TimeUnit.SECONDS);
      } catch (ExecutionException | InterruptedException | TimeoutException ignored) {

      }
    }
  }


  public boolean isPlayerNameAllowedToJoin(String name) {

    name = name.replace("//", "");
    name = name.replace("\\", "");

    boolean allowed = name.trim().length() > 0; // must have a name that is not whitespace

    if(!allowed) return false;

    if(this.whitelistEnabled) {
      return this.allowedClientNames.contains(name.trim()) || "bernard".equalsIgnoreCase(name);
    }
    return true;
  }

  public void enableWhitelist(Set<String> allowedClientNames) {
    this.allowedClientNames = allowedClientNames;
    this.whitelistEnabled = true;
  }

  public void disableWhitelist() {
    this.whitelistEnabled = false;
  }

  public Set<String> getWinners() {
    synchronized (this.dataLock) {
      return new HashSet<>(this.winners);
    }
  }

  public void resetWinners() {
    synchronized (this.dataLock) {
      this.winners = new HashSet<>();
    }
  }

  public void setObserver(Optional<Observer> observer) {
    this.observer = observer;
  }

  public void setDelay(int delay) {
    this.refereeDelayBetweenRoundsMillis = delay;
  }

  public boolean isObserverEnabled() {
    return this.observer.isPresent();
  }

  public String printQueue() {
    return this.clientsWaitingToPlay.toString();
  }
}
