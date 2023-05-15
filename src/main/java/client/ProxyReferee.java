package client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.TextNode;
import game.Coord;
import game.Player;
import game.Ship;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Optional;
import json.FleetJSON;
import json.JsonSocketCommunication;
import json.JsonUtils;
import json.MessageJSON;
import json.PlayerJSON;
import json.SetupJSON;
import json.VolleyJSON;
import json.WinJSON;
import server.GameType;

public class ProxyReferee implements Runnable{

  private static final TextNode VOID_JSON = JsonNodeFactory.instance.textNode("void");
  private final JsonSocketCommunication communication;
  private final Player player;
  private final GameType type;

  private boolean isGameOver;

  public ProxyReferee(Socket server, Player player, GameType type) throws IOException {
    this.communication = new JsonSocketCommunication(server);
    this.player = player;
    this.type = type;
    this.isGameOver = false;
  }


  @Override
  public void run() {
    while(!this.isGameOver) {
      Optional<MessageJSON> receivedJson = this.communication.receiveJson();

      if(receivedJson.isPresent()) {

        MessageJSON message = receivedJson.get();
        delegateMessage(message);

      }
      else {
        this.isGameOver = true; // no message, give up and leave
      }
    }
  }


  private void delegateMessage(MessageJSON message) {
    String name = message.messageName();
    JsonNode arguments = message.arguments();

    if("join".equals(name)) {
      join();
    }
    else if("setup".equals(name)) {
      setup(arguments);
    }
    else if("take-turn".equals(name)) {
      takeTurn(arguments);
    }
    else if("hit".equals(name)) {
      hit(arguments);
    }
    else if("win".equals(name)) {
      win(arguments);
    }
    else {
      throw new IllegalArgumentException("Unknown request: " + name + " " + arguments.asText());
    }
  }

  private void join() {
    PlayerJSON playerJSON = new PlayerJSON(this.player.name(), this.type);
    JsonNode arguments = JsonUtils.serializeRecordToJson(playerJSON);
    MessageJSON response = new MessageJSON("join", arguments);
    this.communication.sendJson(response);
  }

  private void setup(JsonNode arguments) {
    SetupJSON setupJSON = JsonUtils.convertNodeToRecord(arguments, SetupJSON.class);

    List<Ship> ships = this.player.setup(setupJSON.height(), setupJSON.width(), setupJSON.boats());

    FleetJSON fleetJSON = new FleetJSON(ships);
    JsonNode node = JsonUtils.serializeRecordToJson(fleetJSON);
    MessageJSON response = new MessageJSON("setup", node);
    this.communication.sendJson(response);
  }

  private void takeTurn(JsonNode arguments) {
    VolleyJSON volleyJSON = JsonUtils.convertNodeToRecord(arguments, VolleyJSON.class);

    List<Coord> shots = this.player.salvo(volleyJSON.coordinates());

    VolleyJSON volleyJSONResponse = new VolleyJSON(shots);
    JsonNode node = JsonUtils.serializeRecordToJson(volleyJSONResponse);
    MessageJSON response = new MessageJSON("take-turn", node);
    this.communication.sendJson(response);
  }

  private void hit(JsonNode arguments) {
    VolleyJSON vollyHit = JsonUtils.convertNodeToRecord(arguments, VolleyJSON.class);

    this.player.hits(vollyHit.coordinates());

    this.communication.sendJson(new MessageJSON("hit", VOID_JSON));
  }

  private void win(JsonNode arguments) {

    WinJSON winJSON = JsonUtils.convertNodeToRecord(arguments, WinJSON.class);

    this.player.endGame(winJSON.won());
    this.isGameOver = true;

    this.communication.sendJson(new MessageJSON("hit", VOID_JSON));

  }
}
