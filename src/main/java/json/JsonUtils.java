package json;

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
}
