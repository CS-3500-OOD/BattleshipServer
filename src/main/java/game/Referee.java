package game;

import java.util.*;

public class Referee implements IReferee{
    private final Board p1Board;
    private final Board p2Board;
    private final Player client1;
    private final Player client2;


    public Referee(Player p1, Player p2){
        this.client1 = p1;
        this.client2 = p2;
        this.p1Board = new BoardImpl();
        this.p2Board = new BoardImpl();
    }

    //TODO: MAYBE, check if shots are firing upon repeat locations? Depends on what we expect from Students
    //TODO: OPTIONALS: How do we deal with bad JSON, not just bad logic.

    /**
     * Execute Game Of BattleShip.
     */
    @Override
    public void run() {
        //pick random height and width between 20 and 50, and add to game info
        Random r = new Random();
        int height = r.nextInt(30) + 20;
        int width = r.nextInt(30) + 20;
        Map<ShipType, Integer> gameInfo = new HashMap<>();

        //Create Random Bounded Ship assignments
        List<ShipType> types = new ArrayList<>(Arrays.asList(ShipType.CARRIER, ShipType.BATTLESHIP, ShipType.DESTROYER, ShipType.SUBMARINE));
        for (ShipType s : types){
            gameInfo.put(s, r.nextInt(3));
        }

        //Call Place Boats on each client
        //Retrieve Placed fleets
        List<Ship> c1Ships = client1.setup(height, width, gameInfo);
        List<Ship> c2Ships = client2.setup(height, width, gameInfo);
        p1Board.setup(height, width, gameInfo);
        p2Board.setup(height, width, gameInfo);
        //check fleets for validity
        if (!this.isValidFleet(c1Ships, gameInfo) || !this.isValidFleet(c2Ships, gameInfo)){
            client1.endGame(this.isValidFleet(c1Ships, gameInfo));
            client2.endGame(this.isValidFleet(c2Ships, gameInfo));
        }
        // Place Boats on tracking boards
        p1Board.mirrorClientPlacement(c1Ships);
        p2Board.mirrorClientPlacement(c2Ships);

        //Send first empty Salvo to players
        //INBOUND C2 = shots coming FROM c1 TOWARDS c2
        List<Coord> inboundc2 = new ArrayList<>();
        List<Coord> inboundc1 = new ArrayList<>();

        //TODO: Extract to Helper
        while(true){
            //Send old salvos, retrieve new ones, and retrieve ref versions
            List<Coord> c1Return = client1.salvo(inboundc1);
            List<Coord> ref1Return = p1Board.salvo(inboundc1);
            System.out.println( "Surviving Ships" + ref1Return.size());

            List<Coord> c2Return = client2.salvo(inboundc2);
            List<Coord> ref2Return = p2Board.salvo(inboundc2);

            if(c1Return.size() != ref1Return.size() || ref2Return.size() != c2Return.size() || ref1Return.size() == 0 || ref2Return.size()==0){
                Pair<Boolean, Boolean> endStates = this.endgameCond(c1Return, c2Return, ref1Return, ref2Return);
                client1.endGame(endStates.getKey());
                client2.endGame(endStates.getVal());
                return;
            }
            //Now give players each of their hits, by giving return salvos to opponent boards
            List<Coord> p1Hits = p2Board.reportDamage(c1Return);
            List<Coord> p2Hits = p1Board.reportDamage(c2Return);
            client1.hits(p1Hits);
            client2.hits(p2Hits);
            inboundc1 = c2Return;
            inboundc2 = c1Return;
        }
    }


    private Pair<Boolean, Boolean> endgameCond(List<Coord> c1Return, List<Coord> c2Return, List<Coord> ref1Return, List<Coord> ref2Return) {
        boolean c1Win;
        boolean c2Win;
        if(c1Return.size() == ref1Return.size() && c2Return.size() != ref2Return.size()){
            c1Win = true;
            c2Win = false;
        } else if (c2Return.size() == ref2Return.size() && c1Return.size() != ref1Return.size()) {
            c1Win = false;
            c2Win = true;
        }
        else if (ref1Return.size() == 0 && ref2Return.size() != 0){
            c1Win = false;
            c2Win = true;
        }
        else if (ref2Return.size() == 0 && ref1Return.size() != 0){
            c1Win = true;
            c2Win = false;
        }
        else if (ref1Return.size() == 0 && ref2Return.size() != 0){
            c1Win = false;
            c2Win = true;
        } else if (ref1Return.size() == 0 && ref2Return.size() == 0) {
            c1Win = true;
            c2Win = true;
        }
        else {
            c1Win = false;
            c2Win = false;
        }
        return new Pair<Boolean, Boolean>(c1Win, c2Win);
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
                //TODO: Convert Ship Types to Enums
                Objects.equals(gameInfo.get(ShipType.CARRIER), counter.get(6)) &&
                Objects.equals(gameInfo.get(ShipType.BATTLESHIP), counter.get(5)) &&
                Objects.equals(gameInfo.get(ShipType.DESTROYER), counter.get(4)) &&
                Objects.equals(gameInfo.get(ShipType.SUBMARINE), counter.get(3));
    }
}
