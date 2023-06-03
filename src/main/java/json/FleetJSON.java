package json;

import com.fasterxml.jackson.annotation.JsonProperty;
import game.Ship;
import java.util.List;

public record FleetJSON(
    @JsonProperty("fleet") List<Ship> fleet) {

}
