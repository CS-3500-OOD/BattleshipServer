import java.io.IOException;
import java.net.ServerSocket;

/**
 * This class accepts clients on the specified port number until the server is shutdown.
 * <p>
 * Clients are accepted if they send a Player JSON object after connecting to the server within a
 * set timespan. If the object is well-formed and valid, then the client is accepted and added to
 * GamesManager's queue of acceptable players
 */
public class ClientsAcceptor {

  private final GamesManager manager;
  private final int port;

  public ClientsAcceptor(int port, GamesManager manager) {
    this.manager = manager;
    this.port = port;
  }

  public void acceptClients() {
    try {
      ServerSocket serverSocket = new ServerSocket(this.port);

    } catch (IOException e) {
      Server.shutdownServerWithError("Unable to start Server on port " + this.port);
    }
  }
}
