package server;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

/**
 * This is the entrypoint for the server.Server. Running this file will start the server on the given port
 * and run until shutdown. If a port is not given, the default port will be used.
 */
public class Server {

  private static final int DEFAULT_PORT = 35001;
  static final Logger logger = LogManager.getLogger(Server.class);

  public static final boolean DEBUG = false;

  /**
   * Runs the server on the given port until the server is shutdown.
   * @param port the port to host the server on
   */
  public static void runServer(int port) {
    Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.ALL);

    logger.info("Starting server on port " + port);
    GamesManager manager = new GamesManager(port);
    manager.startHostingGames();
  }

  /**
   * Method used to shut down the server in the event of an error.
   * @param errorMessage the error to display
   */
  public static void shutdownServerWithError(String errorMessage) {
    logger.error("ERROR: " + errorMessage);
    System.exit(1);
  }


  /**
   * The main entrypoint into this program. Parses command line arguments given to configure the
   * server and then runs the server.
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    CommandLine cmd = parseCommandLine(args);
    int port = getPort(cmd);
    runServer(port);
  }

  /**
   * Retrieves the port number from the options in the command line. If no port number was given,
   * use the default port number.
   * @param cmd the parsed command line arguments
   * @return the port to host the server on
   */
  private static int getPort(CommandLine cmd) {
    try {
      if(cmd.hasOption('p')) {
        int port = Integer.parseInt(cmd.getOptionValue('p'));
        return (port >= 0 && port <= 65535) ? port : DEFAULT_PORT;
      }
    }
    catch (NumberFormatException ignored) {
      // ignored
    }
    return DEFAULT_PORT;
  }

  /**
   * Parses the given command line arguments. If there is an issue parsing the options, then the
   * server will print a usage block and shutdown with an error message.
   * @param args the command line arguments to parse
   * @return the parsed arguments
   */
  private static CommandLine parseCommandLine(String[] args) {
    Options options = createCommandLineOptions();
    try {
      CommandLineParser parser = new DefaultParser();
      return parser.parse(options, args);

    } catch (ParseException e) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("server.Server", options);

      shutdownServerWithError("Unable to parse. Reason: " + e.getMessage());
    }
    return null; // catch block will terminate program with 'shutdownServerWithError'
  }

  /**
   * Static method used to create the command line options.
   * @return the options for the command line
   */
  private static Options createCommandLineOptions() {
    Options options = new Options();
    options.addOption("p", "port", true, "The port to host on");

    return options;
  }
}
