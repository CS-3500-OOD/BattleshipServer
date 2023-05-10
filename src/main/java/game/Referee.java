package game;

import java.util.*;

public class Referee implements IReferee{
    private Board p1Board;
    private Board p2Board;
    private Player client1;
    private Player client2;

    private int height;

    private int width;



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
        this.height = r.nextInt(30) + 20;
        this.width = r.nextInt(30) + 20;
        Map<String, Integer> gameInfo = new HashMap<>();
        gameInfo.put("height", this.height);
        gameInfo.put("width", this.width);

        //Create Random Bounded Ship assignments
        List<String> types = new ArrayList<>(Arrays.asList("carrier", "battleShip", "destroyer", "submarine"));
        for (String s : types){
            gameInfo.put(s, r.nextInt(3));
        }

        //Call Place Boats on each client
        //Retrieve Placed fleets
        System.out.println(gameInfo.toString());
        List<Ship> c1Ships = client1.setup(gameInfo);
        List<Ship> c2Ships = client2.setup(gameInfo);
        p1Board.setup(gameInfo);
        p2Board.setup(gameInfo);
        //check fleets for validity
        //TODO: Valid Check

        // Place Boats on tracking boards
        p1Board.mirrorClientPlacement(c1Ships);
        p2Board.mirrorClientPlacement(c2Ships);

        //Send first empty Salvo to players
        //INBOUND C2 = shots coming FROM c1 TOWARDS c2
        List<Coord> inboundc2 = new ArrayList<>();
        List<Coord> inboundc1 = new ArrayList<>();
        boolean flag = true;
        while(flag){
            //Send old salvos, retrieve new ones, and retrieve ref versions
            List<Coord> c1Return = client1.salvo(inboundc1);
            List<Coord> ref1Return = p1Board.salvo(inboundc1);
            System.out.println(ref1Return.size());

            List<Coord> c2Return = client2.salvo(inboundc2);
            List<Coord> ref2Return = p2Board.salvo(inboundc2);

            if(c1Return.size() != ref1Return.size() || ref2Return.size() != c2Return.size() || ref1Return.size() == 0 || ref2Return.size()==0){
                //End Game, add 0 len conditions
                flag = false;
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
}
