package server;

import java.util.Optional;
import java.util.Scanner;

/**
 * This class is responsible for handling input given on the Standard Input asynchronously.
 * <p>
 * For example, if the word 'quit' is given, then the server will shut down.
 */
public class InputListener {

  private static final String QUIT = "quit"; // no args
  private static final String WHITELIST = "whitelist"; // takes filepath as single arg

  private static final String PRINT_WINNERS = "print_winners"; // takes filepath as single arg

  private static final String OBSERVER = "observer"; // takes host and port as args

  private final GamesManager manager;

  public InputListener(GamesManager manager) {
    this.manager = manager;
  }

  /**
   * Accepts input until the keyword 'quit' is given. Then notifies the server.GamesManager to stop
   * the server.
   */
  public void acceptInput() {
    Scanner scanner = new Scanner(System.in);
    Server.logger.info("[INPUT] To stop the server, type 'quit' on a single line");

    while (scanner.hasNextLine()) {
      String line = scanner.nextLine().trim();
      String[] tokens = line.split(" ");

      if (QUIT.equalsIgnoreCase(line)) {
        quit();
      }
      else if (tokens.length > 0) {

        switch (tokens[0].toLowerCase()) {
          case WHITELIST -> whitelist(tokens);
          case PRINT_WINNERS -> printWinners(tokens);
          case OBSERVER -> addObserver(tokens);
        }

      }
      else if(!line.isBlank()) {
        Server.logger.info("[INPUT] To stop the server, type 'quit'");
      }
    }
  }

  private void addObserver(String[] tokens) {
    if(tokens.length == 3) {
      try {
        Observer observer = new Observer(tokens[1], Integer.parseInt(tokens[2]));
        if(observer.isConnected()) {
          this.manager.setObserver(Optional.of(observer));
          Server.logger.info("Added observer.");
        }
        else {
          Server.logger.info("Unable to connect to the specified observer.");
        }
      }
      catch (NumberFormatException e) {
        Server.logger.info("Invalid port number.");
      }
    }
    else {
      Server.logger.info("Removing observer. use '" + OBSERVER + " [host] [port]' to add a new observer.");
      this.manager.setObserver(Optional.empty());
    }
  }

  private void printWinners(String[] tokens) {
    boolean saved = WinnerFileHandler.saveWinners(this.manager.getWinners(), tokens[1]);
    if(saved) {
      Server.logger.info("Saved winners to file: [" + tokens[1] + "].");
    }
    else {
      Server.logger.info("Invalid save file given.");
    }
  }

  private void whitelist(String[] tokens) {
    if(tokens.length == 2) {
      boolean enabled = WhitelistFileHandler.loadNewWhitelist(this.manager, tokens[1]);
      if(enabled) {
        this.manager.resetWinners();
        Server.logger.info("Reset winners for new whitelist.");
        Server.logger.info("Whitelist enabled. File: [" + tokens[1] + "]");
      }
      else {
        Server.logger.info("Invalid whitelist file given.");
      }
    }
    else {
      this.manager.disableWhitelist();
      this.manager.resetWinners();
      Server.logger.info("Reset winners.");
      Server.logger.info("Whitelist disabled.");
    }
  }

  private void quit() {
    Server.logger.info("[INPUT] Quit received, requesting server graceful shutdown...");
    this.manager.stopServer();
  }

}
