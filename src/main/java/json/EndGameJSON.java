package json;

import com.fasterxml.jackson.annotation.JsonProperty;
import game.GameResult;

public record EndGameJSON(
    @JsonProperty("result") GameResult result,
    @JsonProperty("reason") String reason) {

}
