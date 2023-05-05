package server;

import java.net.Socket;

public class ClientSignupAttempt implements Runnable {


  private final Socket client;

  public ClientSignupAttempt(Socket client) {
    this.client = client;
  }

  @Override
  public void run() {

  }
}
