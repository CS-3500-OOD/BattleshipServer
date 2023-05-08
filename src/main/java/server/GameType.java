package server;

/**
 * This enum designates which type of game a client wants to play on the server.
 * CPU - The client wants to be matched with the agent hosted on the server.
 * MULTI - The client wants to be matched with another client.
 */
public enum GameType {
  CPU, MULTI;

  /**
   * Decodes a GameType from a String
   *
   * @param type the String variant of the game type
   * @return the GameType representation of the given String
   * @throws IllegalStateException if the given string does not represent a valid GameType
   */
  public static GameType decodeType(String type) throws IllegalStateException {
    return switch (type.toLowerCase()) {
      case "cpu" -> CPU;
      case "multi" -> MULTI;
      default -> throw new IllegalStateException("Unknown type");
    };
  }
}
