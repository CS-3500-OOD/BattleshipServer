package game;

//TODO: Replace String Vals with Enum. NO MAGIC CONSTANTS
public enum ShipType {
  CARRIER(6), BATTLESHIP(5), DESTROYER(4), SUBMARINE(3);

  private final int length;

  ShipType(int i) {
    length = i;
  }
}
