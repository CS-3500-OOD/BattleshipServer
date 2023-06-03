package server;

/**
 * This enum designates which type of game a client wants to play on the server. CPU - The client
 * wants to be matched with the agent hosted on the server. MULTI - The client wants to be matched
 * with another client.
 */
public enum GameType {
  SINGLE, MULTI
}
