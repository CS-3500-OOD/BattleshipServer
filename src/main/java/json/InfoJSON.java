package json;


import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Jackson JSON record to serialize Info messages to/from the client.
 * <p>
 * The expected JSON given is: {"info": String }
 */
public record InfoJSON(
    @JsonProperty("info") String message) {

}
