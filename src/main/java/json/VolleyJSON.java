package json;

import com.fasterxml.jackson.annotation.JsonProperty;
import game.Coord;
import java.util.List;

public record VolleyJSON(
    @JsonProperty("coordinates") List<Coord> coordinates) {

}
