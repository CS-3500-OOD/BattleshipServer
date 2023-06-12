package server;

import java.util.prefs.Preferences;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This is the entrypoint for the server.Server. Running this file will start the server on the
 * given port and run until shutdown. If a port is not given, the default port will be used.
 */
public class Server {

  static final Logger logger = LogManager.getLogger(Server.class);

  static {
    System.setProperty("log4j.configurationFile", "resources/log4j2.xml");
  }

  public static final Preferences PROPERTIES = ServerProperties.getPreferences();
  public static final boolean DEBUG = PROPERTIES.getBoolean("server_debug", true);


  /**
   * Runs the server on the given port until the server is shutdown.
   *
   * @param port the port to host the server on
   */
  public static void runServer(int port) {
    logger.info("Starting server on port " + port);
    GamesManager manager = new GamesManager(port);
    manager.startHostingGames();
  }

  /**
   * The main entrypoint into this program. Parses command line arguments given to configure the
   * server and then runs the server.
   *
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    Options options = createCommandLineOptions();

    try {
      CommandLine commandLine = new DefaultParser().parse(options, args);
      runWithCommandLine(commandLine, options);

    } catch (ParseException e) {
      printHelpMessage(options);
    }
  }

  private static void runWithCommandLine(CommandLine commandLine, Options options) {
    if (commandLine.hasOption('h')) {
      printHelpMessage(options);
    } else {
      handleMiscOptions(commandLine);

      int port = PROPERTIES.getInt("default_port", 35001);

      ServerProperties.printPreferences(PROPERTIES);

      runServer(port);
    }
  }

  private static void handleMiscOptions(CommandLine commandLine) {
    if (commandLine.hasOption('r')) {
      ServerProperties.setDefaults(PROPERTIES);
    }

    if (commandLine.hasOption('p')) {
      try {
        int port = Integer.parseInt(commandLine.getOptionValue('p'));
        PROPERTIES.putInt("default_port", port);
      } catch (NumberFormatException e) {
        logger.error("Invalid port number, using default");
      }
    }

    if (commandLine.hasOption('d')) {
      String level = commandLine.getOptionValue('d');

      boolean changeDebug = true;
      boolean server = true;
      boolean game = false;
      boolean socket = false;

      switch (level.toLowerCase()) {
        case "all" -> {
          game = true;
          socket = true;
        }
        case "game" -> game = true;
        case "socket" -> socket = true;
        case "none" -> server = false;
        default -> {
          logger.error("Invalid debug level, using default");
          changeDebug = false;
        }
      }

      if (changeDebug) {
        PROPERTIES.putBoolean("server_debug", server);
        PROPERTIES.putBoolean("game_specific_debug", game);
        PROPERTIES.putBoolean("socket_communication_debug", socket);
      }
    }

    ServerProperties.syncPreferences(PROPERTIES);
  }

  private static void printHelpMessage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("Server", options);
  }

  /**
   * Static method used to create the command line options.
   *
   * @return the options for the command line
   */
  private static Options createCommandLineOptions() {
    Options options = new Options();

    options.addOption(Option.builder("p")
        .longOpt("port")
        .desc(
            "Set the port number to run on. This number will be saved locally and persist between each run")
        .hasArg(true)
        .argName("port number")
        .required(false)
        .build());

    options.addOption(Option.builder("d")
        .longOpt("debug")
        .desc(
            "Set the debug information category for the server. Available levels: [none, game, socket, all]")
        .hasArg(true)
        .argName("level")
        .required(false)
        .build());

    options.addOption(Option.builder("r")
        .longOpt("reset")
        .desc("Reset server preferences to default")
        .hasArg(false)
        .required(false)
        .build());

    options.addOption(Option.builder("h")
        .longOpt("help")
        .desc("Print this help message")
        .hasArg(false)
        .required(false)
        .build());

    return options;
  }
}
