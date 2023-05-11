package game;

import java.util.List;
import java.util.Map;

public interface Player {


    /**
     * Get the player's name.
     *
     * @return the player's name
     */
    String name();

    /**
     * Receives the shots fired by opposition in previous round, and returns the shots fired
     */
    List<Coord> salvo(List<Coord> shots);


    /**
     * Sets up a game of battleship with given specifications
     * @param specifications  has height, width, and each ship
     * @return  Fleet of placed ships in valid places
     */
    List<Ship> setup(Map<String, Integer> specifications);


    /**
     * Process hits sent by previous salvo.
     * @param shots   list of coordinates representing player shots from previous salvo that connected with a target
     */
    void hits(List<Coord> shots);


    void endGame(boolean win);



}
