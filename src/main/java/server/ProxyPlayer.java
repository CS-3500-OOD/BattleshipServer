package server;

import game.Coord;
import game.Player;
import game.Ship;
import game.ShipType;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Class used to facilitate socket communication with a client using the JSON communication design.
 */
public class ProxyPlayer implements Player {

    private final JsonSocketCommunication communication;

    private final String name;
    private final GameType type;


    public ProxyPlayer(Socket client, String name, GameType type) throws IOException {
        this.communication = new JsonSocketCommunication(client);
        this.name = name;
        this.type = type;
    }

    /**
     * Get the preferred GameType of the ProxyPlayer
     * @return the proxyPlayer's GameType
     */
    public GameType getGameType() {
        return this.type;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public List<Coord> salvo(List<Coord> shots) {
        VolleyJSON volley = new VolleyJSON(shots);
        JsonNode messageArgs = JsonUtils.serializeRecordToJson(volley);
        MessageJSON messageJson = new MessageJSON("take-turn", messageArgs);
        this.communication.sendJson(messageJson);

        Optional<MessageJSON> response = this.communication.receiveJson();

        if(response.isPresent() && "take-turn".equals(response.get().messageName())) {
            return this.parseVolleyResponse(response.get().arguments());
        }
        return List.of(new Coord(-1, -1));
    }

    @Override
    public List<Ship> setup(Map<String, Integer> specifications) {
        JsonNode setupArgs = new ObjectMapper().convertValue(specifications, JsonNode.class);
        MessageJSON messageJSON = new MessageJSON("setup", setupArgs);
        this.communication.sendJson(messageJSON);

        Optional<MessageJSON> response = this.communication.receiveJson();

        if(response.isPresent() && "setup".equals(response.get().messageName())) {
            return this.parseFleetResponse(response.get().arguments());
        }
        return List.of(new Ship(new Coord(-1, -1), -1, Dir.DOWN));
    }

    @Override
    public void hits(List<Coord> shots) {
        VolleyJSON volley = new VolleyJSON(shots);
        JsonNode messageArgs = JsonUtils.serializeRecordToJson(volley);
        MessageJSON messageJson = new MessageJSON("hit", messageArgs);
        this.communication.sendJson(messageJson);

        Optional<MessageJSON> response = this.communication.receiveJson();
    }

    @Override
    public void endGame(boolean win) {
        WinJSON winJSON = new WinJSON(win);
        JsonNode messageArgs = JsonUtils.serializeRecordToJson(winJSON);
        MessageJSON messageJSON = new MessageJSON("win", messageArgs);
        this.communication.sendJson(messageJSON);

        Optional<MessageJSON> response = this.communication.receiveJson();
        this.communication.endCommunication();
    }


    /**
     * Parses a JsonNode as a Volley and returns the list of Coords in that volley.
     *
     * @param node the node to deserialize
     * @return the list of coordinates
     */
    private List<Coord> parseVolleyResponse(JsonNode node) {
        ObjectMapper mapper = new ObjectMapper();
        VolleyJSON volley = mapper.convertValue(node, VolleyJSON.class);
        return volley.coordinates();
    }

    /**
     * Parses a JsonNode as a Fleet and returns the list of Ships in the Fleet.
     *
     * @param node the node to deserialize
     * @return the list of Ships
     */
    private List<Ship> parseFleetResponse(JsonNode node) {
        ObjectMapper mapper = new ObjectMapper();
        FleetJSON volley = mapper.convertValue(node, FleetJSON.class);
        return volley.fleet();
    }
}
