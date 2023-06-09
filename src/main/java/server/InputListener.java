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

  private static final String DELAY = "delay"; // takes num milliseconds as an arg

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
          case DELAY -> setDelay(tokens);
        }

      }
      else if(!line.isBlank()) {
        Server.logger.info("[INPUT] To stop the server, type 'quit'");
      }
    }
  }

  private void setDelay(String[] tokens) {
    if(tokens.length == 2) {
      try {
        int delay = Integer.parseInt(tokens[1]);
        this.manager.setDelay(delay);
        Server.logger.info("Set round delay to " + delay + " milliseconds.");
      }
      catch (NumberFormatException e) {
        Server.logger.info("Invalid delay.");
      }
    }
    else {
      Server.logger.info("Removing delay. Use '" + DELAY + " [number of milliseconds]' to add a new delay");
      this.manager.setDelay(-1);
    }
  }

  private void addObserver(String[] tokens) {
    if(tokens.length == 1 && !this.manager.isObserverEnabled()) {
      Observer observer = new Observer();
      if(observer.isConnected()) {
        this.manager.setObserver(Optional.of(observer));
        Server.logger.info("Added observer.");
      }
      else {
        Server.logger.info("Unable to connect to the specified observer.");
      }
    }
    else {
      Server.logger.info("Removing observer.");
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
