package json;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Jackson JSON utility class to serialize Info messages to/from the client.
 * <p>
 * The expected JSON given is:
 * {"info": "MESSAGE"}
 */
public class InfoJSON {

  private final String message;

  @JsonCreator
  public InfoJSON(@JsonProperty("info") String message) {
    this.message = message;
  }

  public String getMessage() {
    return this.message;
  }

}
