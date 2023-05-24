package game;

import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager.Log4jMarker;
import server.Server;

public class Referee implements IReferee{
    private final Board p1Board;
    private final Board p2Board;
    private final Player client1;
    private final Player client2;

    private final Marker gameUniqueMarker;

    private static final Logger logger = LogManager.getLogger(Referee.class);


    public Referee(Player p1, Player p2) {
        this(p1, p2, UUID.randomUUID().toString());
    }

    public Referee(Player p1, Player p2, String gameUUID){
        this.client1 = p1;
        this.client2 = p2;
        this.gameUniqueMarker = new Log4jMarker(gameUUID);
        this.p1Board = new BoardImpl();
        this.p2Board = new BoardImpl();
    }

    //TODO: MAYBE, check if shots are firing upon repeat locations? Depends on what we expect from Students
    //TODO: OPTIONALS: How do we deal with bad JSON, not just bad logic.

    /**
     * Execute Game Of BattleShip.
     */
    @Override
    public boolean run() {
        if(Server.DEBUG) logger.info(this.gameUniqueMarker, "Starting game...");

        //pick random height and width between 10 and 20, and add to game info
        Random r = new Random();
        int height = r.nextInt(11) + 10;
        int width = r.nextInt(11) + 10;
        Map<ShipType, Integer> gameInfo = new HashMap<>();

        // 50x50 == 20 ships
        // 10x10 == 5 ships
        // area bounds [100, 2500]
        // ship bounds [5, 20]
        // output = output_start + ((output_end - output_start) / (input_end - input_start)) * (input - input_start)

//        int area = width * height;
//        int numAdditionalShips = Math.round((15f / 2400f) * (area - 100f));

        //Create Random Bounded Ship assignments
        List<ShipType> types = new ArrayList<>(
            Arrays.asList(ShipType.CARRIER, ShipType.BATTLESHIP, ShipType.DESTROYER,
                ShipType.SUBMARINE));
        for (ShipType s : types) {
            gameInfo.put(s, 1);//TODO: Random Ship Selection
        }
//        for (int i = 0; i < numAdditionalShips; i++) {
//            ShipType type = types.get(r.nextInt(types.size()));
//            gameInfo.put(type, gameInfo.get(type) + 1);
//        }

        if(Server.DEBUG) logger.info(this.gameUniqueMarker,
            "Board [" + width + "x" + height + "] - " + Arrays.toString(
                gameInfo.entrySet().toArray()));

        //Call Place Boats on each client
        //Retrieve Placed fleets
        List<Ship> c1Ships = client1.setup(height, width, gameInfo);
        List<Ship> c2Ships = client2.setup(height, width, gameInfo);
        p1Board.setup(height, width, gameInfo);
        p2Board.setup(height, width, gameInfo);
        if(Server.DEBUG) logger.info(this.gameUniqueMarker, "Setup Stage:");
        if(Server.DEBUG) logger.info(this.gameUniqueMarker,
            this.client1.name() + " - " + Arrays.toString(c1Ships.toArray()));
        if(Server.DEBUG) logger.info(this.gameUniqueMarker,
            this.client2.name() + " - " + Arrays.toString(c2Ships.toArray()));

        //check fleets for validity
        boolean c1Valid = this.isValidFleet(c1Ships, gameInfo);
        boolean c2Valid = this.isValidFleet(c2Ships, gameInfo);
        if (!c1Valid || !c2Valid) {
            if(Server.DEBUG) logger.info(this.gameUniqueMarker,
                "A player did not provide a valid setup configuration.");
            if(Server.DEBUG) logger.info(this.gameUniqueMarker, c1Valid ? (this.client1.name() + " won")
                : (c2Valid ? (this.client2.name() + " won") : "Both players lost"));

            String reasonFailed = "Your player did not provide a valid setup configuration";
            String reasonStopped = "The opponent did not provide a valid setup configuration";
            boolean draw = (!c1Valid && !c2Valid);
            GameResult result1 = draw ? GameResult.DRAW : (c1Valid ? GameResult.WIN : GameResult.LOSE);
            GameResult result2 = draw ? GameResult.DRAW : (c2Valid ? GameResult.WIN : GameResult.LOSE);
            client1.endGame(result1, c1Valid ? reasonStopped : reasonFailed);
            client2.endGame(result2, c2Valid ? reasonStopped : reasonFailed);
            return false;
        }
        // Place Boats on tracking boards
        p1Board.mirrorClientPlacement(c1Ships);
        p2Board.mirrorClientPlacement(c2Ships);

        //Send first empty Salvo to players
        //INBOUND C2 = shots coming FROM c1 TOWARDS c2
        List<Coord> inboundc2 = new ArrayList<>();
        List<Coord> inboundc1 = new ArrayList<>();

        if(Server.DEBUG) logger.info(this.gameUniqueMarker, "Starting game loop.");
        //TODO: Extract to Helper
        while (true) {
            //Send old salvos, retrieve new ones, and retrieve ref versions
            List<Coord> c1Return = client1.salvo(inboundc1);
            List<Coord> ref1Return = p1Board.salvo(inboundc1);

            List<Coord> c2Return = client2.salvo(inboundc2);
            List<Coord> ref2Return = p2Board.salvo(inboundc2);

            if(Server.DEBUG) logger.info(this.gameUniqueMarker, "Volley sent:");
            if(Server.DEBUG) logger.info(this.gameUniqueMarker,
                client1.name() + " " + Arrays.toString(c1Return.toArray()));
            if(Server.DEBUG) logger.info(this.gameUniqueMarker,
                client2.name() + " " + Arrays.toString(c2Return.toArray()));

            boolean p2Won = ref1Return.size() == 0;
            boolean p1Won = ref2Return.size() == 0;
            if (p1Won || p2Won) {
                String winner = p2Won ? (p1Won ? "Draw!" : client2.name() + " won!")
                    : (client1.name() + " won!");
                if(Server.DEBUG) logger.info(this.gameUniqueMarker, "Game Over! " + winner);

                boolean draw = p1Won && p2Won;
                GameResult result1 = draw ? GameResult.DRAW : (p1Won ? GameResult.WIN : GameResult.LOSE);
                GameResult result2 = draw ? GameResult.DRAW : (p2Won ? GameResult.WIN : GameResult.LOSE);

                String winReason = "You won!";
                String loseReason = "You lost!";
                client1.endGame(result1, p1Won ? winReason : loseReason);
                client2.endGame(result2, p2Won ? winReason : loseReason);
                return true;
            } else {

                boolean p1InvalidVolley =
                    c1Return.size() != ref1Return.size() || this.invalidSalvo(c1Return,
                        p1Board);
                boolean p2InvalidVolley =
                    c2Return.size() != ref2Return.size() || this.invalidSalvo(c2Return,
                        p2Board);

                if (p1InvalidVolley || p2InvalidVolley) {

                    String badVolleyReason = "Your player did not return a valid volley";
                    String normalReason = "The opponent did not return a valid volley";

                    boolean draw = p1InvalidVolley && p2InvalidVolley;
                    GameResult result1 = draw ? GameResult.DRAW : (p1InvalidVolley ? GameResult.LOSE : GameResult.WIN);
                    GameResult result2 = draw ? GameResult.DRAW : (p2InvalidVolley ? GameResult.LOSE : GameResult.WIN);

                    client1.endGame(result1, p1InvalidVolley ? badVolleyReason : normalReason);
                    client2.endGame(result2, p2InvalidVolley ? badVolleyReason : normalReason);

                    if(Server.DEBUG) logger.info(this.gameUniqueMarker,
                        "A player did not return a valid volley");
                    if (p1InvalidVolley && p2InvalidVolley) {
                        if(Server.DEBUG) logger.info(this.gameUniqueMarker, "Both players lost");
                    } else if (p1InvalidVolley) {
                        if(Server.DEBUG) logger.info(this.gameUniqueMarker, client2.name() + " won!");
                    } else {
                        if(Server.DEBUG) logger.info(this.gameUniqueMarker, client1.name() + " won!");
                    }
                    return false;
                }
            }

            //Now give players each of their hits, by giving return salvos to opponent boards
            List<Coord> p1Hits = p2Board.reportDamage(c1Return);
            List<Coord> p2Hits = p1Board.reportDamage(c2Return);
            client1.hits(p1Hits);
            client2.hits(p2Hits);
            inboundc1 = c2Return;
            inboundc2 = c1Return;

            if(Server.DEBUG) logger.info(this.gameUniqueMarker, "Reporting successful hits:");
            if(Server.DEBUG) logger.info(this.gameUniqueMarker,
                client1.name() + " succeeded in hitting - " + Arrays.toString(
                    p1Hits.toArray()));
            if(Server.DEBUG) logger.info(this.gameUniqueMarker,
                client2.name() + " succeeded in hitting - " + Arrays.toString(
                    p2Hits.toArray()));
        }
    }


    private boolean invalidSalvo(List<Coord> salvo, Board board){
        for (Coord c: salvo) {
            if (!board.shotIsValid(c)){
                return true;
            }
        }
        return false;
    }


    private boolean isValidFleet(List<Ship> c1Ships, Map<ShipType, Integer> gameInfo) {
        Map<Integer, Integer> counter = new HashMap<>();
        List<Ship> copy = new ArrayList<>(c1Ships);
        boolean flag = true;
        for (Ship s : c1Ships) {
            counter.put(s.getLength(), counter.getOrDefault(s.getLength(), 0) + 1);
            int acc = 0;
            for (Ship s2 : copy){
                if (s.isColliding(s2)){
                    acc++;
                }
            }
            if (acc > 1){
                flag = false;
            }
        }
        return flag &&
            (gameInfo.getOrDefault(ShipType.CARRIER, 0).equals(counter.getOrDefault(6, 0))) &&
            (gameInfo.getOrDefault(ShipType.BATTLESHIP, 0).equals(counter.getOrDefault(5, 0))) &&
            (gameInfo.getOrDefault(ShipType.DESTROYER, 0).equals(counter.getOrDefault(4, 0))) &&
            (gameInfo.getOrDefault(ShipType.SUBMARINE, 0).equals(counter.getOrDefault(3, 0)));
    }
}
