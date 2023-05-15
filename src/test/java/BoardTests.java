import game.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Test
    public void testSalvo(){
        //Initialize Board Object
        Board b1 = new BoardImpl();
        //Create Specs List
        Map<String, Integer> specs = new HashMap<>();
        //Initialize h/w
        specs.put("height", 40);
        specs.put("width", 40);
        b1.setup(specs);
        //Set fleet to s list
        b1.mirrorClientPlacement(new ArrayList<>(Arrays.asList(s3, s4, s5, s6)));
        List<Coord> volley = b1.salvo(new ArrayList<>());
        assertEquals(volley.size(), 4);
        List<Coord> volley2 = b1.salvo(new ArrayList<>(Arrays.asList(new Coord(13, 16))));
        assertEquals(4, volley2.size());
        volley2 = b1.salvo(Arrays.asList(new Coord(14, 16)));
        assertEquals(volley2.size(), 4);
        volley2 = b1.salvo(Arrays.asList(new Coord(15, 16)));
        assertEquals(volley2.size(), 3);
    }
}
