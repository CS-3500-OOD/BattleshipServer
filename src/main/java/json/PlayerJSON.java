package json;

import com.fasterxml.jackson.annotation.JsonProperty;
import server.GameType;

/**
 * Jackson JSON record to serialize/deserialize Player JSON objects to/from the client.
 * <p>
 * The expected JSON given is: {"name": String, "game-type": GameType }
 */
public record PlayerJSON(
    @JsonProperty("name") String name,
    @JsonProperty("game-type") GameType gameType) {

}
