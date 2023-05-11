package server;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import game.Coord;
import game.Dir;
import game.Player;
import game.Ship;
import json.JsonSocketCommunication;
import json.JsonUtils;
import json.MessageJSON;
import json.VolleyJSON;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ProxyPlayer implements Player {

    private final JsonSocketCommunication communication;

    private final GameType type;

    public ProxyPlayer(Socket client, GameType type) throws IOException {
        this.communication = new JsonSocketCommunication(client);
        this.type = type;
    }

    public GameType getGameType() {
        return this.type;
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

        if(response.isPresent() && "take-turn".equals(response.get().messageName())) {
            return this.parseFleetResponse(response.get().arguments());
        }
        return List.of(new Ship(new Coord(-1, -1), -1, Dir.DOWN));
    }

    @Override
    public void hits(List<Coord> shots) {

    }

    @Override
    public void endGame(boolean win) {

    }


    private List<Coord> parseVolleyResponse(JsonNode node) {
        ObjectMapper mapper = new ObjectMapper();
        VolleyJSON volley = mapper.convertValue(node, VolleyJSON.class);
        return volley.coordinates();
    }

    private List<Ship> parseFleetResponse(JsonNode arguments) {
    }
}
