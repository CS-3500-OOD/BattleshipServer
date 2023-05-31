import game.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BoardTests {
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



//    @Test
    public void testSetup() {
        Player p = new PlayerImp();
        Map<ShipType, Integer> sampleSet = new HashMap<>();
        sampleSet.put(ShipType.BATTLESHIP, 2);
        sampleSet.put(ShipType.CARRIER, 2);
        sampleSet.put(ShipType.DESTROYER, 2);
        sampleSet.put(ShipType.SUBMARINE, 2);
        List<Ship> setuped = p.setup(10, 10, sampleSet);
        for (Ship s : setuped) {
            Coord start = s.getStartPoint();
            Coord end = s.getEndpoint();
            assertTrue(start.x() >= 0, start.x() + " >= 0");
            assertTrue(start.y() >= 0, start.y() + " >= 0");
            assertTrue(end.x() <= 9, end.x() + " <= 9");
            assertTrue(end.y() <= 9, end.y() + " <= 9");
        }
        List<Ship> copy = new ArrayList<>(setuped);
        for (Ship s : copy) {
            int acc = 0;
            for (Ship s2 : copy) {
                if (s.equals(s2)) {
                    assertTrue(acc <= 1);
                    acc++;
                } else {
                    assertFalse(s.isColliding(s2), s.toString() + s2.toString());
                }
            }
        }
    }
}
