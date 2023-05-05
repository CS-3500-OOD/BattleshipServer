import java.util.Scanner;

/**
 * This class is responsible for handling input given on the Standard Input asynchronously.
 * <p>
 * For example, if the word 'quit' is given, then the server will shut down.
 */
public class InputListener {

  private static final String QUIT = "quit";
  private final GamesManager manager;
  private boolean takeInput;
  public InputListener(GamesManager manager) {
    this.manager = manager;
    this.takeInput = true;
  }

  /**
   * Accepts input until the keyword 'quit' is given. Then notifies the GamesManager to stop the server.
   */
  public void acceptInput() {
    Scanner scanner = new Scanner(System.in);
    while (this.takeInput && scanner.hasNext()) {
      String line = scanner.next();

      if(QUIT.equalsIgnoreCase(line)) {
        this.manager.stopServer();
        this.takeInput = false;
      }
      else {
        System.out.println("To stop the server, type 'quit'");
      }
    }
    this.manager.stopServer();
  }

}
