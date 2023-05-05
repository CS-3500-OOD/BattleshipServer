package server;

import java.net.Socket;

public class ClientSignupAttempt implements Runnable {


  private final GamesManager manager;
  private final Socket client;


  public ClientSignupAttempt(GamesManager manager, Socket client) {
    this.manager = manager;
    this.client = client;
  }

  @Override
  public void run() {

  }
}
