package game;

public class Coord {
    private final int x;
    private final int y;

    public Coord(int x, int y) {
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
