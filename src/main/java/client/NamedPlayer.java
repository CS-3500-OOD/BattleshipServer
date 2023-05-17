package client;

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
  public void endGame(boolean won) {
    System.out.println(name + (won ? " won!" : " lost!"));
  }

}
