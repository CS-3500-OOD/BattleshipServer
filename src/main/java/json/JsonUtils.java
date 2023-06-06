package json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {


  /**
   * Serializes a given Record to a JsonNode.
   *
   * @param record the instance of a Record to convert to Json
   * @return the JsonNode representation of the record instance
   * @throws IllegalArgumentException if the record cannot be converted into json
   */
  public static JsonNode serializeRecordToJson(Record record) throws IllegalArgumentException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.convertValue(record, JsonNode.class);
  }

  /**
   * Converts the given JsonNode value to the specified class type. This method is used for Json
   * deserialization.
   *
   * @param fromValue   the value to convert
   * @param toValueType the class to convert to
   * @param <T>         The class type
   * @return a new instance of the class type provided that contains the data represented in the
   * JsonNode
   */
  public static <T> T convertNodeToRecord(JsonNode fromValue, Class<T> toValueType) {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.convertValue(fromValue, toValueType);
  }


  public static MessageJSON buildMessageJSON(String name, Record argument) {
    return new MessageJSON(name, serializeRecordToJson(argument));
  }

  /**
   * Attempts to print a record as an indented Json String.
   *
   * @param record the record to format
   * @return a formatted string of the json format of the given record
   */
  public static String prettifyJSON(Record record) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(record);
    } catch (JsonProcessingException e) {
      return record.toString();
    }
  }
}
