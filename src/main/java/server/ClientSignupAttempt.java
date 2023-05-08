package server;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.Socket;
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
      ObjectMapper mapper = new ObjectMapper();
      JsonParser parser = mapper.createParser(this.client.getInputStream());
      PlayerJSON playerJSON = mapper.readValue(parser, PlayerJSON.class);
      // TODO: build the proxy player
      manager.addPlayerToQueue(...);
    }
    catch (IOException ignored) {
      // client connection interrupted or client sent bad input
      // TODO: separate catching network issue versus parsing issues
    }
  }
}
