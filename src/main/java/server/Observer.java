package server;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import json.JsonUtils;
import json.ObserverJSON;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Observer {

  private static final Logger logger = LogManager.getLogger(Observer.class);

  private Socket socket;
  private PrintStream outputStream;


  private boolean continueConnection = true;

  public Observer(String host, int port) {
    try {
      this.socket = new Socket(host, port);
      this.outputStream = new PrintStream(this.socket.getOutputStream());
    } catch (IOException e) {
      logger.error("Unable to connect to observer...");
      this.continueConnection = false;
    }
  }

  public synchronized void updateObserver(ObserverJSON observerJSON) {
    if(this.continueConnection) {
      JsonNode node = JsonUtils.serializeRecordToJson(observerJSON);
      this.outputStream.println(node);
      this.outputStream.flush();
    }
  }

  public boolean isConnected() {
    return this.continueConnection;
  }

  public void stopObserver() {
    try {
      this.socket.close();
    } catch (IOException ignored) {
    }
    this.continueConnection = false;
  }

}
