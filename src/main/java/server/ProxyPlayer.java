package server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import game.Coord;
import game.Dir;
import game.GameResult;
import game.Player;
import game.Ship;
import game.ShipType;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import json.FleetJSON;
import json.JsonSocketCommunication;
import json.JsonUtils;
import json.MessageJSON;
import json.SetupJSON;
import json.VolleyJSON;
import json.EndGameJSON;

/**
 * Class used to facilitate socket communication with a client using the JSON communication design.
 */
public class ProxyPlayer implements Player {

  private static final int RESPONSE_TIMEOUT_SECS = Server.PROPERTIES.getInt(
      "client_game_max_response_seconds", 2);
  private final JsonSocketCommunication communication;

  private final String name;
  private final GameType type;
  private final ExecutorService executorService;


  public ProxyPlayer(Socket client, String name, GameType type) throws IOException {
    this.communication = new JsonSocketCommunication(client);
    this.executorService = Executors.newSingleThreadExecutor();
    this.name = name;
    this.type = type;
  }

  /**
   * Get the preferred GameType of the ProxyPlayer
   *
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
  public List<Ship> setup(int height, int width, Map<ShipType, Integer> specifications) {
    SetupJSON set = new SetupJSON(height, width, specifications);
    JsonNode setupArgs = JsonUtils.serializeRecordToJson(set);
    MessageJSON messageJSON = new MessageJSON("setup", setupArgs);
    this.communication.sendJson(messageJSON);

    Optional<MessageJSON> response = this.getResponse();

    if (response.isPresent() && "setup".equals(response.get().messageName())) {
      return this.parseFleetResponse(response.get().arguments());
    }

    return List.of(new Ship(new Coord(-1, -1), -1, Dir.VERTICAL));
  }

  @Override
  public List<Coord> takeShots() {
    JsonNode node = JsonNodeFactory.instance.objectNode();
    MessageJSON messageJson = new MessageJSON("take-shots", node);
    this.communication.sendJson(messageJson);

    Optional<MessageJSON> response = this.getResponse();

    if (response.isPresent() && "take-shots".equals(response.get().messageName())) {
      return this.parseVolleyResponse(response.get().arguments());
    }
    return List.of(new Coord(-1, -1));
  }

  @Override
  public List<Coord> reportDamage(List<Coord> opponentShotsOnBoard) {
    VolleyJSON volley = new VolleyJSON(opponentShotsOnBoard);
    JsonNode messageArgs = JsonUtils.serializeRecordToJson(volley);
    MessageJSON messageJson = new MessageJSON("report-damage", messageArgs);
    this.communication.sendJson(messageJson);

    Optional<MessageJSON> response = this.getResponse();

    if (response.isPresent() && "report-damage".equals(response.get().messageName())) {
      return this.parseVolleyResponse(response.get().arguments());
    }
    return List.of(new Coord(-1, -1));
  }

  @Override
  public void successfulHits(List<Coord> shotsThatHitOpponentShips) {
    VolleyJSON volley = new VolleyJSON(shotsThatHitOpponentShips);
    JsonNode messageArgs = JsonUtils.serializeRecordToJson(volley);
    MessageJSON messageJson = new MessageJSON("successful-hits", messageArgs);
    this.communication.sendJson(messageJson);

    Optional<MessageJSON> response = this.getResponse();
  }

  @Override
  public void endGame(GameResult result, String reason) {
    EndGameJSON endGameJSON = new EndGameJSON(result, reason);
    JsonNode messageArgs = JsonUtils.serializeRecordToJson(endGameJSON);
    MessageJSON messageJSON = new MessageJSON("end-game", messageArgs);
    this.communication.sendJson(messageJSON);

    Optional<MessageJSON> response = this.getResponse();
    this.communication.endCommunication();
  }

  /**
   * Waits for response from client for a max timeout. If there is no response or an invalid
   * response, return an empty optional.
   *
   * @return an optional message JSON representation
   */
  private Optional<MessageJSON> getResponse() {
    Callable<Optional<MessageJSON>> callable = this.communication::receiveJson;
    Future<Optional<MessageJSON>> future = this.executorService.submit(callable);
    try {
      return future.get(RESPONSE_TIMEOUT_SECS, TimeUnit.SECONDS);
    } catch (CancellationException | InterruptedException | ExecutionException |
             TimeoutException e) {
      Server.logger.error(this.name + " COULD NOT RETRIEVE RESPONSE: " + e);
      return Optional.empty();
    }
  }

  /**
   * Parses a JsonNode as a Volley and returns the list of Coords in that volley.
   *
   * @param node the node to deserialize
   * @return the list of coordinates
   */
  private List<Coord> parseVolleyResponse(JsonNode node) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      VolleyJSON volley = mapper.convertValue(node, VolleyJSON.class);
      return volley.coordinates();
    } catch (IllegalArgumentException e) {
      return List.of(new Coord(-1, -1));
    }
  }

  /**
   * Parses a JsonNode as a Fleet and returns the list of Ships in the Fleet.
   *
   * @param node the node to deserialize
   * @return the list of Ships
   */
  private List<Ship> parseFleetResponse(JsonNode node) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      FleetJSON volley = mapper.convertValue(node, FleetJSON.class);
      return volley.fleet();
    } catch (IllegalArgumentException e) {
      return List.of(new Ship(new Coord(-1, -1), -1, Dir.HORIZONTAL));
    }
  }

  @Override
  public String toString() {
    return this.name;
  }
}
