package game;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record Coord(int x, int y) {

  @JsonCreator
  public Coord(@JsonProperty("x") int x, @JsonProperty("y") int y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Coord)) {
      return false;
    } else {
      return this.x == ((Coord) other).x() && this.y == ((Coord) other).y();
    }
  }

  @Override
  public int hashCode() {
    return Integer.hashCode(this.x) + Integer.hashCode(this.y);
  }


  @Override
  public String toString() {
    return "(" + this.x + ", " + this.y + ")";
  }
}
