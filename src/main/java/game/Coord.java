package game;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Coord {
    private final int x;
    private final int y;

    @JsonCreator
    public Coord(@JsonProperty("x") int x, @JsonProperty("y") int y) {
        this.x = x;
        this.y = y;
    }

    public int getX(){
        return this.x;
    }

    public int getY(){
        return this.y;
    }

    @Override
    public boolean equals(Object other){
        if (!(other instanceof Coord)){
            return false;
        }
        else {return this.x == ((Coord) other).getX() && this.y == ((Coord)other).getY();}
    }

    @Override
    public int hashCode(){
        return Integer.hashCode(this.x) + Integer.hashCode(this.y);
    }
}
