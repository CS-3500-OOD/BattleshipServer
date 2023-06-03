import game.Coord;
import game.Dir;
import game.Ship;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShipsTests {

    Ship s1;
    Ship s2;
    Ship s3;
    Ship s4;
    Ship s5;
    Ship s6;
    Ship s7;


    @BeforeEach
    public void initializeSampleShips(){
        s1 = new Ship(5);
        s2 = new Ship(3);
        s3 = new Ship(new Coord(3, 4), 4, Dir.VERTICAL);
        s4 = new Ship(new Coord(8, 9), 5, Dir.HORIZONTAL);
        s5 = new Ship(new Coord(13, 15), 3, Dir.VERTICAL);
        s6 = new Ship(new Coord(13, 16), 3, Dir.HORIZONTAL);
    }

    @Test
    public void testIsHit(){
        assertTrue(s3.isHit(new Coord(3, 4)));
        assertFalse(s3.isHit(new Coord(3, 0)));
        assertTrue(s3.isHit(new Coord(3, 5)));
        assertFalse(s3.isHit(new Coord(4, 4)));
        assertFalse(s3.isHit(new Coord(2, 4)));

        assertTrue(s4.isHit(new Coord(12, 9)));
        assertFalse(s4.isHit(new Coord(13, 9)));
        assertFalse(s4.isHit(new Coord(7, 9)));
        assertFalse(s4.isHit(new Coord(14, 9)));
    }

    @Test
    public void testIsColliding(){
        Ship col1 = new Ship(new Coord(2, 5), 4, Dir.HORIZONTAL);
        assertTrue(s3.isColliding(col1));
        assertTrue(col1.isColliding(s3));
        assertFalse(s4.isColliding(s3));
    }
}
