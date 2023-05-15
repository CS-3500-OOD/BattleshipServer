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


    /**
     * Execute Game Of BattleShip.
     */
    @Override
    public void run() {
        //pick random height and width between 20 and 50, and add to game info
        Random r = new Random();
        int height = r.nextInt(30) + 20;
        int width = r.nextInt(30) + 20;
        Map<String, Integer> gameInfo = new HashMap<>();
        gameInfo.put("height", height);
        gameInfo.put("width", width);

        //Create Random Bounded Ship assignments
        List<String> types = new ArrayList<>(Arrays.asList("carrier", "battleShip", "destroyer", "submarine"));
        for (String s : types){
            gameInfo.put(s, r.nextInt(3));
        }

        //Call Place Boats on each client
        //Retrieve Placed fleets
        List<Ship> c1Ships = client1.setup(gameInfo);
        List<Ship> c2Ships = client2.setup(gameInfo);
        p1Board.setup(gameInfo);
        p2Board.setup(gameInfo);
        //check fleets for validity
        if (!this.validFleet(c1Ships, gameInfo) || !this.validFleet(c2Ships, gameInfo)){
            client1.endGame(this.validFleet(c1Ships, gameInfo));
            client2.endGame(this.validFleet(c2Ships, gameInfo));
        }
        // Place Boats on tracking boards
        p1Board.mirrorClientPlacement(c1Ships);
        p2Board.mirrorClientPlacement(c2Ships);

        //Send first empty Salvo to players
        //INBOUND C2 = shots coming FROM c1 TOWARDS c2
        List<Coord> inboundc2 = new ArrayList<>();
        List<Coord> inboundc1 = new ArrayList<>();
        while(true){
            //Send old salvos, retrieve new ones, and retrieve ref versions
            List<Coord> c1Return = client1.salvo(inboundc1);
            List<Coord> ref1Return = p1Board.salvo(inboundc1);
            System.out.println( "Surviving Ships" + ref1Return.size());

            List<Coord> c2Return = client2.salvo(inboundc2);
            List<Coord> ref2Return = p2Board.salvo(inboundc2);


            //TODO: Extract to helper
            if(c1Return.size() != ref1Return.size() || ref2Return.size() != c2Return.size() || ref1Return.size() == 0 || ref2Return.size()==0){
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
                client1.endGame(c1Win);
                client2.endGame(c2Win);
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

    private boolean validFleet(List<Ship> c1Ships, Map<String, Integer> gameInfo) {
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
                gameInfo.get("Carrier") == counter.get(6) &&
                gameInfo.get("BattleShip") == counter.get(5) &&
                gameInfo.get("Destroyer") == counter.get(4) &&
                gameInfo.get("Submarine") == counter.get(3);
    }
}
