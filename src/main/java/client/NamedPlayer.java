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

}
