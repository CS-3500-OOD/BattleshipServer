package game;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a ship in a game of BattleShip
 */
public class Ship {

    //False for alive, true for sunk
    private boolean status;

    private Coord startPoint;

    private final int length;

    private Dir orientation;

    private List<Coord> hits;


    public Ship(Coord startPoint, int length, Dir orientation) {
        this.status = false;
        this.startPoint = startPoint;
        this.length = length;
        this.orientation = orientation;
        this.hits = new ArrayList<>();
    }

    public Ship(int length){
        this.length = length;
        this.hits = new ArrayList<>();
    }

    public void place(Coord start, Dir dir){
        this.orientation = dir;
        this.startPoint = start;
    }


    public Dir getDir(){
        return this.orientation;
    }

    public int getLength(){
        return this.length;
    }

    public Coord getStartPoint(){
        return this.startPoint;
    }

    /**
     * Tells whether a ship has been sunk or not
     * @return true for sunk, False for floating
     */
    public boolean isSunk(){
        return status;
    }

    /**
     * Processes a shot from opponent on a single ship level. Will change status if necessary
     *
     * @return  true if hit, and false if not a NEW hit.
     */
    public void receiveShot(Coord c){
        if (this.orientation == Dir.LEFT || this.orientation == Dir.RIGHT){
            if((this.startPoint.getY() == c.getY() && this.startPoint.getX() <= c.getX() && this.getEndpoint().getX() >= c.getX())
            && !hits.contains(c));
            hits.add(c);
        }
        else if (this.orientation == Dir.UP || this.orientation == Dir.DOWN){
            if((this.startPoint.getX() == c.getX() && this.startPoint.getY() <= c.getY() && this.getEndpoint().getY() >= c.getY())
                    && !hits.contains(c));
            hits.add(c);
        }
        if (hits.size() >= this.length)
        {
            this.status = true;
        }
    }

    /**
     * Determines if another ship is colliding with this
     * @param other   another ship
     * @return
     */
    public boolean isColliding(Ship other){
        Coord thisEnd = this.getEndpoint();
        Coord thatEnd = other.getEndpoint();
        boolean xIntersect = (this.startPoint.getX() <= other.getStartPoint().getX() && thisEnd.getX() >= other.getStartPoint().getX()) ||
                (this.startPoint.getX() <= thatEnd.getX() && thisEnd.getX() >= thatEnd.getX());
        boolean yIntersect = (this.startPoint.getY() <= other.getStartPoint().getY() && thisEnd.getY() >= other.getStartPoint().getY()) ||
                (this.startPoint.getY() <= thatEnd.getY() && thisEnd.getY() >= thatEnd.getY());
        return xIntersect && yIntersect;
    }


    /**
     *Calculates endpoint of ship
     *
     */
    public Coord getEndpoint(){
        Coord temp;
        if (this.orientation == Dir.RIGHT){
            temp = new Coord(this.startPoint.getX() + length - 1, this.startPoint.getY());
        }
        else if (this.orientation == Dir.LEFT){
            temp = new Coord(this.startPoint.getX() - length + 1, this.startPoint.getY());
        }
        else if (this.orientation == Dir.UP){
            temp = new Coord(this.startPoint.getX(), this.startPoint.getY()  - length + 1);
        }
        else {
            temp = new Coord(this.startPoint.getX(), this.startPoint.getY() + length - 1);
        }
        return temp;
    }

    public boolean isHit(Coord c){
        if (this.orientation == Dir.LEFT || this.orientation == Dir.RIGHT){
            if((this.startPoint.getY() == c.getY() && this.startPoint.getX() <= c.getX() && this.getEndpoint().getX() >= c.getX())
                    && !hits.contains(c));
            return true;
        }
        else if (this.orientation == Dir.UP || this.orientation == Dir.DOWN){
            if((this.startPoint.getX() == c.getX() && this.startPoint.getY() <= c.getY() && this.getEndpoint().getY() >= c.getY())
                    && !hits.contains(c));
            return true;
        }
        else{
            return false;
        }
    }
}
