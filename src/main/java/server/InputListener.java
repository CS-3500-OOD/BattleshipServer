package server;

import java.util.Scanner;

/**
 * This class is responsible for handling input given on the Standard Input asynchronously.
 * <p>
 * For example, if the word 'quit' is given, then the server will shut down.
 */
public class InputListener {

  private static final String QUIT = "quit";
  private final GamesManager manager;
  public InputListener(GamesManager manager) {
    this.manager = manager;
  }

  /**
   * Accepts input until the keyword 'quit' is given. Then notifies the server.GamesManager to stop the server.
   */
  public void acceptInput() {
    Scanner scanner = new Scanner(System.in);
    while (scanner.hasNext()) {
      String line = scanner.next();

      if(QUIT.equalsIgnoreCase(line)) {
        Server.logger.info("[INPUT] Quit received, requesting server graceful shutdown...");
        this.manager.stopServer();
        return;
      }
      else {
        System.out.println("[INPUT] To stop the server, type 'quit'");
      }
    }
  }

}
