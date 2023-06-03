package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;
import json.JsonSocketCommunication;
import json.MessageJSON;
import json.PlayerJSON;

public class ClientSignupAttempt implements Runnable {

  private final GamesManager manager;
  private final ServerSocket serverSocket;


  public ClientSignupAttempt(GamesManager manager, ServerSocket serverSocket) {
    this.manager = manager;
    this.serverSocket = serverSocket;
  }

  @Override
  public void run() {
    try {
      Socket client = serverSocket.accept();
      Server.logger.info("Establishing communication with " + client);

      JsonSocketCommunication communication = new JsonSocketCommunication(client);
      communication.sendJson(new MessageJSON("join", JsonNodeFactory.instance.objectNode()));
      Optional<MessageJSON> messageJSON = communication.receiveJson();

      if (messageJSON.isPresent() && "join".equals(messageJSON.get().messageName())) {
        PlayerJSON playerJSON = new ObjectMapper().convertValue(messageJSON.get().arguments(),
            PlayerJSON.class);
        ProxyPlayer player = new ProxyPlayer(client, playerJSON.name(), playerJSON.gameType());
        manager.addPlayerToQueue(player);
      } else {
        client.close();
      }
    } catch (IOException e) {
      // client connection interrupted or client sent bad input
      // TODO: separate catching network issue versus parsing issues
      Server.logger.error("Issue establishing connection: " + e);
    }
  }
}
