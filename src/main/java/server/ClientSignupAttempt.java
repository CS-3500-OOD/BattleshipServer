package server;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.net.Socket;

import java.util.Optional;
import json.JsonSocketCommunication;
import json.MessageJSON;
import json.PlayerJSON;

public class ClientSignupAttempt implements Runnable {

  private final GamesManager manager;
  private final Socket client;


  public ClientSignupAttempt(GamesManager manager, Socket client) {
    this.manager = manager;
    this.client = client;
  }

  @Override
  public void run() {
    try {
      JsonSocketCommunication communication = new JsonSocketCommunication(this.client);
      communication.sendJson(new MessageJSON("join", JsonNodeFactory.instance.objectNode()));

      Optional<MessageJSON> messageJSON = communication.receiveJson();

      if(messageJSON.isPresent() && "join".equals(messageJSON.get().messageName())) {
        PlayerJSON playerJSON = new ObjectMapper().convertValue(messageJSON.get().arguments(), PlayerJSON.class);
        ProxyPlayer player = new ProxyPlayer(this.client, playerJSON.name(), playerJSON.gameType());
        manager.addPlayerToQueue(player);
      }
      else {
        this.client.close();
      }
    }
    catch (IOException ignored) {
      // client connection interrupted or client sent bad input
      // TODO: separate catching network issue versus parsing issues
    }
  }
}
