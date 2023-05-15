package json;

import com.fasterxml.jackson.annotation.JsonProperty;
import game.ShipType;

import java.util.Map;

public record SetupJSON(@JsonProperty("height") int height,
                        @JsonProperty("width") int width,
                        @JsonProperty("setup") Map<ShipType, Integer> boats) {
}
