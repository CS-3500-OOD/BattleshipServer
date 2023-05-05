package json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Jackson JSON utility class to serialize/deserialize Messages to/from the client.
 * <p>
 * The expected JSON given is:
 * {"name": "NAME", "arguments": {ARG_OBJECT} }
 */
public class MessageJSON {

  private final String messageName;
  private final JsonNode arguments;

  @JsonCreator
  public MessageJSON(@JsonProperty("name") String messageName, @JsonProperty("arguments") JsonNode arguments) {
    this.messageName = messageName;
    this.arguments = arguments;
  }

  public String getMessageName() {
    return this.messageName;
  }

  public JsonNode getArguments() {
    return arguments;
  }
}
