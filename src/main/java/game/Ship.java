package game;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a ship in a game of BattleShip
 */
public class Ship {

    //False for alive, true for sunk
    private boolean status = false;

    private Coord startPoint;

    private final int length;

    private Dir orientation;

    private List<Coord> hits;


    @JsonCreator
    public Ship(@JsonProperty("coord") Coord startPoint,
                @JsonProperty("length") int length,
                @JsonProperty("direction") Dir orientation) {
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
        if (this.isHit(c) && !hits.contains(c)){
            hits.add(c);
        }
        status =  hits.size() >= length;
    }

    /**
     * Determines if another ship is colliding with this
     * @param other   another ship
     * @return
     */
    public boolean isColliding(Ship other){
        List<Coord> points = new ArrayList<>();
        if(other.getDir() == Dir.HORIZONTAL){
            for (int i = 0; i < other.length; i++){
                points.add(new Coord(other.getStartPoint().x() + i, other.getStartPoint().y()));
            }
        }
        else {
            for (int i = 0; i < other.getLength(); i ++){
                points.add(new Coord(other.getStartPoint().x(), other.getStartPoint().y() + i));
            }
        }
        boolean flag = false;
        for (Coord c :points) {
            if (this.isHit(c)){
                flag = true;
            }
        }
        return flag;
    }


    /**
     *Calculates endpoint of ship
     *
     */
    public Coord getEndpoint(){
        Coord temp;
        if (this.orientation == Dir.VERTICAL){
            return new Coord(this.startPoint.x(), this.startPoint.y() + length);
        }
        else {return new Coord(this.startPoint.x() + length, this.startPoint.y());}
    }

    public boolean isHit(Coord c){
        if (this.orientation == Dir.HORIZONTAL) {
            return (this.startPoint.y() == c.y() &&
                    c.x() >= this.startPoint.x() &&
                    c.x() <= this.startPoint.x() + length);
        }
        else {
            return (this.startPoint.x() == c.x() &&
                    this.startPoint.y() <= c.y() &&
                    this.getEndpoint().y() >= c.y())
                    && !hits.contains(c);
        }
    }
}
