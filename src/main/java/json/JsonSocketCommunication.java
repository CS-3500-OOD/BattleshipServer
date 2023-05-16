package json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.MarkerManager.Log4jMarker;
import server.Server;

/**
 * Simple Socket communication class to send/receive Json. Json is formatted as a MessageJson structure.
 * See MessageJson.java for more info.
 */
public class JsonSocketCommunication {

    private final Socket connection;
    private final JsonParser input;
    private final PrintWriter output;

    private static final Logger logger = LogManager.getLogger(JsonSocketCommunication.class);
    private final Marker marker;

    /**
     * Construct a new JsonSocketCommunication with the given socket.
     *
     * @param connection the connection to talk with
     * @throws IOException if there is an issue retrieving either the input or output stream from the socket connection
     */
    public JsonSocketCommunication(Socket connection) throws IOException {
        this.connection = connection;
        this.input = new ObjectMapper().createParser(connection.getInputStream());
        this.output = new PrintWriter(connection.getOutputStream());

        this.marker = new Log4jMarker("COMM_" + connection);
    }

    /**
     * Sends a MessageJson to the socket connection.
     *
     * @param messageJson the message to send
     */
    public void sendJson(MessageJSON messageJson) {
        JsonNode message = JsonUtils.serializeRecordToJson(messageJson);
        logger.info(this.marker, "SENDING: " + message);
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
            MessageJSON messageJSON = this.input.readValueAs(MessageJSON.class);
            logger.info(this.marker, "RECEIVED: " + messageJSON);
            return Optional.of(messageJSON);
        } catch (IOException e) {
            logger.info(this.marker, "RECEIVED: deserialization issue");
            return Optional.empty();
        }
    }

    /**
     * Attempts to close the connection with the socket.
     */
    public void endCommunication() {
        try {
            this.connection.close();
        } catch (IOException ignored) {
        }
    }
}
