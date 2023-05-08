package json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import server.GameType;

public class PlayerJSON {

  private final String name;
  private final GameType gameType;

  @JsonCreator
  public PlayerJSON(@JsonProperty("name") String name, @JsonProperty("gameType") String gameType) {
    this.name = name;
    this.gameType = GameType.decodeType(gameType);;
  }

  public String getName() {
    return name;
  }

  public GameType getGameType() {
    return this.gameType;
  }
}
