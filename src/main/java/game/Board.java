package game;

import java.util.List;
import java.util.Map;


/**
 * Board can be used to check against players in the referee setting,
 * is helpful as a parent class for an AI player interface.
 */
public interface Board extends Player {



    /**
     * Place ships based on player placement
     * @param playerShips
     */
    public void mirrorClientPlacement(List<Ship> playerShips);


    /**
     * Return List of coords that hit ships placed on board
     * @param shots
     * @return
     */
    List<Coord> reportDamage(List<Coord> shots);


    boolean shotIsValid(Coord c);



}
