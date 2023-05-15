package game;

public record Coord(int x, int y) {

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Coord)) {
            return false;
        } else {
            return this.x == ((Coord) other).x() && this.y == ((Coord) other).y();
        }
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(this.x) + Integer.hashCode(this.y);
    }
}
