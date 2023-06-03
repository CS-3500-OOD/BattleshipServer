package json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Jackson JSON record to serialize/deserialize Messages to/from the client.
 * <p>
 * The expected JSON given is: {"method-name": String, "arguments": {} }
 */
public record MessageJSON(
    @JsonProperty("method-name") String messageName,
    @JsonProperty("arguments") JsonNode arguments) {


  @Override
  public String toString() {
    return JsonUtils.serializeRecordToJson(this).toString();
  }
}
