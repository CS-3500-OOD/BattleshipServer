package json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;

/**
 * Simple Socket communication class to send/receive Json. Json is formatted as a MessageJson structure.
 * See MessageJson.java for more info.
 */
public class JsonSocketCommunication {

    private final JsonParser input;
    private final PrintWriter output;

    /**
     * Construct a new JsonSocketCommunication with the given socket.
     *
     * @param connection the connection to talk with
     * @throws IOException if there is an issue retrieving either the input or output stream from the socket connection
     */
    public JsonSocketCommunication(Socket connection) throws IOException {
        this.input = new ObjectMapper().createParser(connection.getInputStream());
        this.output = new PrintWriter(connection.getOutputStream());
    }

    /**
     * Sends a MessageJson to the socket connection.
     *
     * @param messageJson the message to send
     */
    public void sendJson(MessageJSON messageJson) {
        JsonNode message = JsonUtils.serializeRecordToJson(messageJson);
        this.output.println(message);
        this.output.flush();
    }

    /**
     * Parses the next message given by the socket.
     *
     * @return An optional of the parsed message, empty if the message was not parsed correctly
     */
    public Optional<MessageJSON> receiveJson() {
        try {
            return Optional.of(this.input.readValueAs(MessageJSON.class));
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
