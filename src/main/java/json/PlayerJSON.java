package json;

import com.fasterxml.jackson.annotation.JsonProperty;
import server.GameType;

/**
 * Jackson JSON record to serialize/deserialize Player JSON objects to/from the client.
 * <p>
 * The expected JSON given is: {"name": String, "gameType": GameType }
 */
public record PlayerJSON(
    @JsonProperty("name") String name,
    @JsonProperty("gameType") GameType gameType) {

}
