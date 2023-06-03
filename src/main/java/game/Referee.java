package game;

import java.util.*;
import json.SetupJSON;
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
    private static final boolean GAME_DEBUG = true;


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

    /**
     * Execute Game Of BattleShip.
     */
    @Override
    public boolean run() {
        if(Server.DEBUG && GAME_DEBUG) logger.info(this.gameUniqueMarker, "Starting game...");

        SetupJSON setupParameters = createBoardParameters();

        if(Server.DEBUG && GAME_DEBUG) logger.info(this.gameUniqueMarker, "Setup Stage:");
        List<Ship> c1Ships = this.setupPlayer(setupParameters, client1, p1Board);
        List<Ship> c2Ships = this.setupPlayer(setupParameters, client2, p2Board);
        //check fleets for validity
        boolean validSetups = checkValidSetups(setupParameters, c1Ships, c2Ships);

        if(!validSetups) {
            return false;
        }

        // Place Boats on tracking boards
        p1Board.mirrorClientPlacement(c1Ships);
        p2Board.mirrorClientPlacement(c2Ships);


        if(Server.DEBUG && GAME_DEBUG) logger.info(this.gameUniqueMarker, "Starting game loop.");

        return gameLoop();
    }

    private boolean gameLoop() {
        while(true) {

            List<Coord> p1AttackVolley = client1.takeShots();
            List<Coord> p2AttackVolley = client2.takeShots();

            boolean validVolleys = checkValidVolleys(p1AttackVolley, p2AttackVolley);

            if(!validVolleys) {
                return false;
            }

            p1Board.removePossibleShots(p1AttackVolley);
            p2Board.removePossibleShots(p2AttackVolley);

            if(Server.DEBUG && GAME_DEBUG) {
                logger.info(this.gameUniqueMarker, "Volley sent:");
                logger.info(this.gameUniqueMarker, client1.name() + " " + Arrays.toString(p1AttackVolley.toArray()));
                logger.info(this.gameUniqueMarker, client2.name() + " " + Arrays.toString(p2AttackVolley.toArray()));
            }

            boolean gameOver = isGameOver(p1AttackVolley, p2AttackVolley);

            if(gameOver) {
                return true;
            }

            List<Coord> damageDoneToP1 = client1.reportDamage(p2AttackVolley);
            List<Coord> damageDoneToP2 = client2.reportDamage(p1AttackVolley);

            boolean validReports = checkValidReports(p1AttackVolley, p2AttackVolley, damageDoneToP1, damageDoneToP2);

            if(!validReports) {
                return false;
            }

            if(Server.DEBUG && GAME_DEBUG) {
                logger.info(this.gameUniqueMarker, "Damage Reports:");
                logger.info(this.gameUniqueMarker, client1.name() + " was hit at " + Arrays.toString(damageDoneToP1.toArray()));
                logger.info(this.gameUniqueMarker, client2.name() + " was hit at " + Arrays.toString(damageDoneToP2.toArray()));
            }

            client1.successfulHits(damageDoneToP2);
            client2.successfulHits(damageDoneToP1);
        }
    }

    private boolean checkValidReports(List<Coord> p1AttackVolley, List<Coord> p2AttackVolley,
        List<Coord> damageDoneToP1, List<Coord> damageDoneToP2) {

        List<Coord> expectedDamageP1 = p1Board.reportDamage(p2AttackVolley);
        List<Coord> expectedDamageP2 = p2Board.reportDamage(p1AttackVolley);

        boolean p1Valid = new HashSet<>(expectedDamageP1).equals(new HashSet<>(damageDoneToP1));
        boolean p2Valid = new HashSet<>(expectedDamageP2).equals(new HashSet<>(damageDoneToP2));

        if(!p1Valid || !p2Valid) {
            boolean draw = !p1Valid && !p2Valid;
            GameResult p1Result = draw ? GameResult.DRAW : p1Valid ? GameResult.WIN : GameResult.LOSE;
            GameResult p2Result = draw ? GameResult.DRAW : p2Valid ? GameResult.WIN : GameResult.LOSE;
            String reason = "A player did not return the correct damage report";

            if(Server.DEBUG && GAME_DEBUG) {
                logger.info(this.gameUniqueMarker, reason);
                logger.info(client1.name() + ": " + p1Result + ", " + client2.name() + ": " + p2Result);
            }

            client1.endGame(p1Result, reason);
            client2.endGame(p2Result, reason);
            return false;
        }
        return true;
    }

    private boolean checkValidVolleys(List<Coord> p1AttackVolley, List<Coord> p2AttackVolley) {

        int numShotsExpectedFromP1 = p1Board.numShotsAvailable();
        int numShotsExpectedFromP2 = p2Board.numShotsAvailable();

        boolean p1InvalidVolley = p1AttackVolley.size() != numShotsExpectedFromP1
            || this.invalidSalvo(p1AttackVolley, p1Board);
        boolean p2InvalidVolley = p2AttackVolley.size() != numShotsExpectedFromP2
            || this.invalidSalvo(p2AttackVolley, p2Board);



        if (p1InvalidVolley || p2InvalidVolley) {

            String badVolleyReason = "Your player did not return a valid volley";
            String normalReason = "The opponent did not return a valid volley";

            boolean draw = p1InvalidVolley && p2InvalidVolley;
            GameResult result1 = draw ? GameResult.DRAW : (p1InvalidVolley ? GameResult.LOSE : GameResult.WIN);
            GameResult result2 = draw ? GameResult.DRAW : (p2InvalidVolley ? GameResult.LOSE : GameResult.WIN);

            client1.endGame(result1, p1InvalidVolley ? badVolleyReason : normalReason);
            client2.endGame(result2, p2InvalidVolley ? badVolleyReason : normalReason);

            if(Server.DEBUG && GAME_DEBUG) logger.info(this.gameUniqueMarker, "A player did not return a valid volley");

            String serverOutcomeLog = (p1InvalidVolley && p2InvalidVolley) ? "Both players lost" : (p1InvalidVolley ? client2.name() + " won!" : client1.name() + " won!");

            if(Server.DEBUG && GAME_DEBUG) logger.info(this.gameUniqueMarker, serverOutcomeLog);
            return false;
        }
        return true;
    }

    private boolean isGameOver(List<Coord> p1AttackVolley, List<Coord> p2AttackVolley) {
        boolean p1Won = p2AttackVolley.isEmpty();
        boolean p2Won = p1AttackVolley.isEmpty();

        if(p1Won || p2Won) {
            String winner = p2Won ? (p1Won ? "Draw!" : client2.name() + " won!")
                : (client1.name() + " won!");
            if(Server.DEBUG && GAME_DEBUG) logger.info(this.gameUniqueMarker, "Game Over! " + winner);

            boolean draw = p1Won && p2Won;
            GameResult result1 = draw ? GameResult.DRAW : (p1Won ? GameResult.WIN : GameResult.LOSE);
            GameResult result2 = draw ? GameResult.DRAW : (p2Won ? GameResult.WIN : GameResult.LOSE);

            String winReason = "You won!";
            String loseReason = "You lost!";
            client1.endGame(result1, p1Won ? winReason : loseReason);
            client2.endGame(result2, p2Won ? winReason : loseReason);
            return true; // game over, someone won
        }
        return false;
    }

    // returns true if both setups are valid
    private boolean checkValidSetups(SetupJSON setupParameters, List<Ship> c1Ships, List<Ship> c2Ships) {
        boolean c1Valid = this.isValidFleet(c1Ships, setupParameters.boats());
        boolean c2Valid = this.isValidFleet(c2Ships, setupParameters.boats());

        if (!c1Valid || !c2Valid) {
            if(Server.DEBUG && GAME_DEBUG) logger.info(this.gameUniqueMarker,
                "A player did not provide a valid setup configuration.");
            if(Server.DEBUG && GAME_DEBUG) logger.info(this.gameUniqueMarker, c1Valid ? (this.client1.name() + " won")
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
        return true;
    }

    //Call Place Boats on each client
    //Retrieve Placed fleets
    private List<Ship> setupPlayer(SetupJSON setup, Player player, Board board) {
        int height = setup.height();
        int width = setup.width();
        Map<ShipType, Integer> boats = setup.boats();

        List<Ship> ships = player.setup(height, width, boats);
        board.setup(height, width, boats);

        if(Server.DEBUG && GAME_DEBUG) logger.info(this.gameUniqueMarker,
            player.name() + " - " + Arrays.toString(ships.toArray()));

        return ships;
    }

    private SetupJSON createBoardParameters() {
        //pick random height and width between 6 and 15, and add to game info
        Random r = new Random();
        int height = r.nextInt(10) + 6;
        int width = r.nextInt(10) + 6;

        Map<ShipType, Integer> gameInfo = new HashMap<>(Map.of(
            ShipType.CARRIER, 1,
            ShipType.BATTLESHIP, 1,
            ShipType.DESTROYER, 1,
            ShipType.SUBMARINE, 1
        ));

        int numberAdditionalShips = r.nextInt(0, Math.min(width, height) - 4);

        //Create Random Bounded Ship assignments
        List<ShipType> types = new ArrayList<>(
            Arrays.asList(ShipType.CARRIER, ShipType.BATTLESHIP, ShipType.DESTROYER,
                ShipType.SUBMARINE));

        for(int i = 0; i < numberAdditionalShips; i++) {
            ShipType ship = types.get(r.nextInt(types.size()));
            gameInfo.put(ship, gameInfo.getOrDefault(ship, 0) + 1);
        }

        if(Server.DEBUG && GAME_DEBUG) logger.info(this.gameUniqueMarker,
            "Board [" + width + "x" + height + "] - " + Arrays.toString(
                gameInfo.entrySet().toArray()));

        return new SetupJSON(height, width, gameInfo);
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
