package game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BoardImpl extends PlayerImp implements Board {

  private final Set<Coord> previousShotTracker = new HashSet<>();

  @Override
  public void mirrorClientPlacement(List<Ship> playerShips) {
    super.fleet = playerShips;
  }

  @Override
  public List<Ship> setup(int height, int width, Map<ShipType, Integer> spec) {
    this.OpponentBoard = new CellStatus[height][width];
    this.possibleShots = new ArrayList<>();
    for (int row = 0; row < OpponentBoard.length; row++) {
      for (int col = 0; col < OpponentBoard[0].length; col++) {
        OpponentBoard[row][col] = CellStatus.EMPTY;
        this.possibleShots.add(new Coord(col, row));
      }
    }
    this.fleet = new ArrayList<>();
    return this.fleet; //Format Ship list into format expected by server
  }

  @Override
  public boolean shotIsValid(Coord c) {
    boolean flag = !(previousShotTracker.contains(c)) &&
        ((c.x() >= 0 && c.x() < super.OpponentBoard[1].length) &&
            (c.y() >= 0 && c.y() < super.OpponentBoard.length));
    previousShotTracker.add(c);
    return flag;
  }

  @Override
  public int numShotsAvailable() {
    int acc = 0;
    for (Ship s : this.fleet) {
      if (!s.isSunk()) {
        acc++;
      }
    }
    return Math.min(acc,
    this.possibleShots.size()); // NOTE: IF THERE ARE NO SHOTS LEFT, they can only take that many.
  }

  @Override
  public void removePossibleShots(List<Coord> shots) {
    for (Coord shot : shots) {
      this.possibleShots.remove(shot);
    }
  }

  @Override
  public boolean entireFleetSunk() {
    return shipsAlive().isEmpty();
  }

  @Override
  public List<Ship> shipsAlive() {
    List<Ship> alive = new ArrayList<>();
    for(Ship ship : this.fleet) {
      if(!ship.isSunk()) {
        alive.add(new Ship(ship.getStartPoint(), ship.getLength(), ship.getDir()));
      }
    }
    return alive;
  }
}
