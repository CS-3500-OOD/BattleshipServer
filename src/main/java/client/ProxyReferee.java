package client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import game.Coord;
import game.GameResult;
import game.Player;
import game.Ship;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Optional;
import json.JsonSocketCommunication;
import json.JsonUtils;
import json.MessageJSON;
import json.PlayerJSON;
import json.SetupJSON;
import json.VolleyJSON;
import json.EndGameJSON;
import server.GameType;

public class ProxyReferee {

  private static final boolean DEBUG = false;

  private final JsonSocketCommunication communication;
  private final Player player;
  private final GameType type;

  private boolean isGameOver;
  private GameResult result;

  public ProxyReferee(Socket server, Player player, GameType type) throws IOException {
    this.communication = new JsonSocketCommunication(server);
    this.player = player;
    this.type = type;
    this.isGameOver = false;
    this.result = GameResult.LOSE;
  }


  /**
   *
   * @return true if the given player won.
   */
  public GameResult run() {
    while (!this.isGameOver) {
      Optional<MessageJSON> receivedJson = this.communication.receiveJson();

      if (receivedJson.isPresent()) {

        MessageJSON message = receivedJson.get();

        if (DEBUG) {
          System.out.println(this.player.name() + " received: " + message);
        }

        delegateMessage(message);

      } else {
        this.isGameOver = true; // no message, give up and leave
      }
    }
    this.communication.endCommunication();
    return this.result;
  }


  private void delegateMessage(MessageJSON message) {
    String name = message.messageName();
    JsonNode arguments = message.arguments();

    if ("join".equals(name)) {
      join();
    } else if ("setup".equals(name)) {
      setup(arguments);
    } else if ("take-shots".equals(name)) {
      takeShots();
    } else if ("report-damage".equals(name)) {
      reportDamage(arguments);
    } else if ("successful-hits".equals(name)) {
      successfulHits(arguments);
    } else if ("end-game".equals(name)) {
      win(arguments);
    } else {
      throw new IllegalArgumentException("Unknown request: " + name + " " + arguments.asText());
    }
  }


  private void join() {
    PlayerJSON playerJSON = new PlayerJSON(this.player.name(), this.type);
    JsonNode arguments = JsonUtils.serializeRecordToJson(playerJSON);
    MessageJSON response = new MessageJSON("join", arguments);

    if (DEBUG) {
      System.out.println(this.player.name() + " sent: " + response);
    }
    this.communication.sendJson(response);
  }

  private void setup(JsonNode arguments) {
    SetupJSON setupJSON = JsonUtils.convertNodeToRecord(arguments, SetupJSON.class);

    List<Ship> ships = this.player.setup(setupJSON.height(), setupJSON.width(), setupJSON.boats());

    JsonNode node = serializeFleet(ships);
    MessageJSON response = new MessageJSON("setup", node);

    if (DEBUG) {
      System.out.println(this.player.name() + " sent: " + response);
    }
    this.communication.sendJson(response);
  }

  private void takeShots() {
    List<Coord> shots = this.player.takeShots();

    VolleyJSON volleyJSONResponse = new VolleyJSON(shots);
    JsonNode node = JsonUtils.serializeRecordToJson(volleyJSONResponse);
    MessageJSON response = new MessageJSON("take-shots", node);

    if (DEBUG) {
      System.out.println(this.player.name() + " sent: " + response);
    }
    this.communication.sendJson(response);
  }

  private void reportDamage(JsonNode arguments) {
    VolleyJSON volleyJSON = JsonUtils.convertNodeToRecord(arguments, VolleyJSON.class);

    List<Coord> shots = this.player.reportDamage(volleyJSON.coordinates());

    VolleyJSON volleyJSONResponse = new VolleyJSON(shots);
    JsonNode node = JsonUtils.serializeRecordToJson(volleyJSONResponse);
    MessageJSON response = new MessageJSON("report-damage", node);

    if (DEBUG) {
      System.out.println(this.player.name() + " sent: " + response);
    }
    this.communication.sendJson(response);
  }

  private void successfulHits(JsonNode arguments) {
    VolleyJSON vollyHit = JsonUtils.convertNodeToRecord(arguments, VolleyJSON.class);

    this.player.successfulHits(vollyHit.coordinates());

    JsonNode node = JsonNodeFactory.instance.objectNode();
    MessageJSON response = new MessageJSON("successful-hits", node);

    if (DEBUG) {
      System.out.println(this.player.name() + " sent: " + response);
    }
    this.communication.sendJson(response);
  }

  private void win(JsonNode arguments) {

    EndGameJSON endGameJSON = JsonUtils.convertNodeToRecord(arguments, EndGameJSON.class);

    this.player.endGame(endGameJSON.result(), endGameJSON.reason());
    this.isGameOver = true;
    this.result = endGameJSON.result();

    JsonNode node = JsonNodeFactory.instance.objectNode();
    MessageJSON response = new MessageJSON("end-game", node);

    if (DEBUG) {
      System.out.println(this.player.name() + " sent: " + response);
    }
    this.communication.sendJson(response);
  }


  private static JsonNode serializeFleet(List<Ship> ships) {
    JsonNodeFactory factory = JsonNodeFactory.instance;
    ObjectMapper mapper = new ObjectMapper();

    ArrayNode arrayNode = factory.arrayNode();

    for (Ship ship : ships) {
      ObjectNode shipNode = factory.objectNode();
      shipNode.set("coord", mapper.convertValue(ship.getStartPoint(), JsonNode.class));
      shipNode.set("length", factory.numberNode(ship.getLength()));
      shipNode.set("direction", mapper.convertValue(ship.getDir(), JsonNode.class));
      arrayNode.add(shipNode);
    }

    ObjectNode fleet = factory.objectNode();
    fleet.set("fleet", arrayNode);

    return fleet;
  }
}
