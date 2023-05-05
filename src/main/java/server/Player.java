package server;

/**
 * The server.Player interface. All players must contain these methods.
 */
public interface Player {

  /**
   * Retrieves the server.Player's game type preference.
   * @return the server.Player's game type preference
   */
  GameType getGameType();

}
