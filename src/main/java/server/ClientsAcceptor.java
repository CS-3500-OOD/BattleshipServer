package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This class accepts clients on the specified port number until the server is shutdown.
 * <p>
 * Clients are accepted if they send a server.Player JSON object after connecting to the server
 * within a set timespan. If the object is well-formed and valid, then the client is accepted and
 * added to server.GamesManager's queue of acceptable players
 */
public class ClientsAcceptor {

  private static final int MAX_CLIENTS_TO_SIGNUP_AT_ONE_TIME = Server.PROPERTIES.getInt(
      "max_player_signup_in_parallel", 4);
  private static final int MAX_SIGNUP_TIME_SECS = Server.PROPERTIES.getInt(
      "max_signup_response_seconds", 2);
  private final GamesManager manager;
  private final int port;
  private boolean stopAcceptingClients;

  /**
   * A server.ClientsAcceptor needs the port number on which to accept connections and the
   * server.GamesManager reference to add players upon successful sign up.
   *
   * @param port    the port to accept clients on
   * @param manager the server.GamesManager reference to use to add Players after sign up
   */
  public ClientsAcceptor(int port, GamesManager manager) {
    this.manager = manager;
    this.port = port;
    this.stopAcceptingClients = false;
  }

  /**
   * Accept client connections synchronously. Once a TCP connection is established with a client,
   * spawn a thread to finish the signup process. If the signup process is successful, add the
   * player to the server.GamesManager queue. Continue accepting clients asynchronously during
   * signup process of other clients. If the maximum number of clients signed up at one time is
   * reached, sever connection from new clients that join.
   */
  public void acceptClients() {

    ExecutorService signupExecutorService = new BoundedExecutorService(
        Executors.newFixedThreadPool(MAX_CLIENTS_TO_SIGNUP_AT_ONE_TIME),
        MAX_CLIENTS_TO_SIGNUP_AT_ONE_TIME);

    try {
      ServerSocket serverSocket = new ServerSocket(this.port);
      Server.logger.info("Accepting clients on" + serverSocket);
      while (!this.stopAcceptingClients) {
        this.attemptToSignupClient(signupExecutorService, serverSocket);
      }
      serverSocket.close();
    } catch (IOException e) {
      Server.logger.error("Unable to start server on port " + this.port);
      this.manager.stopServer();
    } finally {
      signupExecutorService.shutdown();
      signupExecutorService.shutdownNow();
    }
  }

  /**
   * Spawns a new thread to sign up a client that connects to the given server socket. If the signup
   * is unsuccessful or there is no available room to queue the signup, then the client connection
   * will be closed.
   *
   * @param executor     the ExecutorService to spawn signup threads with
   * @param serverSocket the server socket to accept clients
   */
  private void attemptToSignupClient(ExecutorService executor, ServerSocket serverSocket) {
    try {
      Future<?> future = executor.submit(new ClientSignupAttempt(this.manager, serverSocket));
      future.get(MAX_SIGNUP_TIME_SECS, TimeUnit.SECONDS);
    } catch (RejectedExecutionException | ExecutionException | InterruptedException |
             TimeoutException ignored) {
//      Server.logger.info("Connection not accepted. " + e);
    }
  }

  public void stopAcceptingClients() {
    this.stopAcceptingClients = true;
  }
}
