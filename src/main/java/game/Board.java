package game;

import java.util.List;


/**
 * Board can be used to check against players in the referee setting, is helpful as a parent class
 * for an AI player interface.
 */
public interface Board extends Player {


  /**
   * Place ships based on player placement
   *
   * @param playerShips
   */
  void mirrorClientPlacement(List<Ship> playerShips);


  boolean shotIsValid(Coord c);

  int numShotsAvailable();

  void removePossibleShots(List<Coord> shots);

  boolean entireFleetSunk();

  List<Ship> shipsAlive();


}
