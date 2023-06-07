package game;

/**
 * Cell Status is an enum representing the state of a single cell on a BattleShip board. - Empty: A
 * Cell that has neither been hit nor had a ship placed upon it - Ship: A Cell with a Ship placed on
 * it - Hit: Formerly ship, Struck - Splash: A cell that was empty, Struck
 */
public enum CellStatus {
  EMPTY, HIT, SPLASH, SHIP
}
