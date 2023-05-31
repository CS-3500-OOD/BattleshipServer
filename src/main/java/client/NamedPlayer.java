package client;

import game.GameResult;
import game.PlayerImp;

public class NamedPlayer extends PlayerImp {

  private final String name;

  public NamedPlayer(String name) {
    super();
    this.name = name;
  }

  @Override
  public String name() {
    return this.name;
  }

  @Override
  public void endGame(GameResult result, String reason) {
    System.out.println("\n" + name + " [" + result + "] - " + reason + "\n");
  }

}
