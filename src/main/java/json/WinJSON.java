package json;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WinJSON(
        @JsonProperty("won") boolean won) {
}
