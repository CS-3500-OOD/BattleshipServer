package game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import server.Server;

public class PlayerImp implements Player {

  /**
   * Board owned by the user represented by this program
   */
  protected List<Ship> fleet;
  /**
   * Board owned by the opponent of the user represented by this program
   */
  protected CellStatus[][] OpponentBoard;

  private static final String SERVER_NAME = Server.PROPERTIES.get("server_name", "SERVER_AGENT");

  protected List<Coord> possibleShots;

  @Override
  public String name() {
    return SERVER_NAME;
  }

  @Override
  public List<Ship> setup(int height, int width, Map<ShipType, Integer> spec) {
    Map<ShipType, Integer> specifications = new HashMap<>(spec);
    this.OpponentBoard = new CellStatus[height][width];
    this.possibleShots = new ArrayList<>();
    for (int row = 0; row < OpponentBoard.length; row++) {
      for (int col = 0; col < OpponentBoard[0].length; col++) {
        OpponentBoard[row][col] = CellStatus.EMPTY;
        this.possibleShots.add(new Coord(col, row));
      }
    }
    this.fleet = new ArrayList<>();
    this.placeBoats(specifications);
    return this.fleet; //Format Ship list into format expected by server
  }

  @Override
  public List<Coord> takeShots() {
    int acc = 0;
    for (Ship s : this.fleet) {
      if (!s.isSunk()) {
        acc++;
      }
    }
    return this.generateShots(Math.min(acc, this.possibleShots.size()));
  }

  @Override
  public List<Coord> reportDamage(List<Coord> opponentShotsOnBoard) {
    List<Coord> successfulShots = new ArrayList<>();
    for (Ship s : this.fleet) {
      for (Coord c : opponentShotsOnBoard) {
        boolean hit = s.receiveShot(c);
        if (hit) {
          successfulShots.add(c);
        }
      }
    }
    return successfulShots;
  }

  @Override
  public void successfulHits(List<Coord> shotsThatHitOpponentShips) {
    for (Coord shot : shotsThatHitOpponentShips) {
      this.OpponentBoard[shot.y()][shot.x()] = CellStatus.HIT;
    }
  }

  @Override
  public void endGame(GameResult result, String reason) {
//        System.out.println(result + ": " + reason);
  }


  //Given a fleet from the server, place boats
  private void placeBoats(Map<ShipType, Integer> boats) {

    List<Coord> possiblePlacements = new ArrayList<>(this.possibleShots);
    Collections.shuffle(possiblePlacements);

    //Initialize a hashmap of ship types paired to lengths
    Map<ShipType, Integer> reference = Map.of(
        ShipType.CARRIER, 6,
        ShipType.BATTLESHIP, 5,
        ShipType.DESTROYER, 4,
        ShipType.SUBMARINE, 3
    );

    List<Ship> temp = new ArrayList<>();

    //Create initial set of Ship objects
    for (ShipType s : boats.keySet()) {
      int numShips = boats.get(s);
      for (int i = 0; i < numShips; i++) {
        temp.add(new Ship(reference.get(s)));
      }
    }

    Random r = new Random();
    List<Dir> allDirs = new ArrayList<>(Arrays.asList(Dir.VERTICAL, Dir.HORIZONTAL));

    for (Ship s : temp) {
      boolean flag = false;
      while (!flag) {
        Coord place = possiblePlacements.remove(0);
        Collections.shuffle(allDirs);

        boolean placed = false;
        for (int i = 0; i < allDirs.size() && !placed && !flag; i++) {
          s.place(place, allDirs.get(i));

          if (this.validCoords(s)) {
            flag = true;
            for (Ship s2 : this.fleet) {
              if (s.isColliding(s2)) {
                flag = false;
                break;
              }
            }
              if (flag) {
                  fleet.add(s);
              }
          }
        }
      }
    }
  }

  private List<Coord> generateShots(int number) {

    if (this.possibleShots.isEmpty()) {
      return new ArrayList<>();
    }

    List<Coord> retList = new ArrayList<>();
    Random random = new Random();

    for (int i = 0; i < number; i++) {
      Coord currentCoord = this.possibleShots.remove(random.nextInt(this.possibleShots.size()));
      OpponentBoard[currentCoord.y()][currentCoord.x()] = CellStatus.SPLASH;
      retList.add(currentCoord);
    }
    return retList;
  }


  private boolean validCoords(Ship s) {

    Coord start = s.getStartPoint();
    Coord end = s.getEndpoint();

    Predicate<Coord> inBounds = (a) -> a.x() >= 0 && a.y() >= 0 && a.y() < OpponentBoard.length
        && a.x() < OpponentBoard[0].length;

    return inBounds.test(start) && inBounds.test(end);
  }
}
