package game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BoardImpl extends PlayerImp implements Board{

    private Set<Coord> previousShotTracker = new HashSet<>();

    @Override
    public void mirrorClientPlacement(List<Ship> playerShips) {
        super.fleet = playerShips;
    }

    @Override
    public List<Coord> reportDamage(List<Coord> shots) {
        Set<Coord> hitSet = new HashSet<>();
        for (Coord shot : shots){
            for (Ship boat: this.fleet){
                if (boat.isHit(shot)){
                    hitSet.add(shot);
                }
            }
        }
        return new ArrayList<>(hitSet);
    }

    @Override
    public boolean shotIsValid(Coord c) {
        boolean flag = !(previousShotTracker.contains(c)) ||
                !((c.x() >=0 && c.x() <= super.OpponentBoard[1].length) &&
                        (c.y() >= 0 && c.y() <= super.OpponentBoard.length));
        previousShotTracker.add(c);
        return flag;
    }


}
