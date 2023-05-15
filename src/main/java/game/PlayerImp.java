package game;

import java.util.*;

public class PlayerImp implements Player {
    /**
     * Board owned by the user represented by this program
     */
    protected List<Ship> fleet;
    /**
     * Board owned by the opponent of the user represented by this program
     */
    protected CellStatus[][] OpponentBoard;

    /**
     * Recieves a list of opponents shots from the previous round. Updates board, and replies with a list of shots
     * for the new round
     * @param shots The Shots fired by opponent
     * @return  Shots Fired back at opponent, not exceeding the size of the number of boats alive on board
     */
    @Override
    public List<Coord> salvo(List<Coord> shots) {
        for (Ship s : this.fleet){
            for (Coord c : shots){
                s.receiveShot(c);
            }
        }
        int acc = 0;
        for (Ship s: this.fleet){
            if (!s.isSunk()){
                acc ++;
            }
        }
        return this.generateShots(acc);
    }


    private List<Coord> generateShots(int number){
        List<Coord> retList = new ArrayList<>();
        Random r = new Random();
        for (int i = 0; i < number; i++){
            Coord currentCoord;
            do {
                int x = r.nextInt(OpponentBoard.length);
                int y = r.nextInt(OpponentBoard[0].length);
                currentCoord = new Coord(x, y);
            }while(retList.contains(currentCoord) && ! this.validShot(currentCoord));
            retList.add(currentCoord);

        }
        for (Coord shot : retList){
            OpponentBoard[shot.x()][shot.y()] = CellStatus.SPLASH;
        }
        return retList;
    }


    private boolean validShot(Coord c){
        return this.OpponentBoard[c.x()][c.y()] == CellStatus.EMPTY;
    }


    @Override
    public List<Ship> setup(Map<String, Integer> spec) {
        Map<String, Integer> specifications = new HashMap<>(spec);
        this.OpponentBoard = new CellStatus[specifications.get("width")][specifications.get("height")];
        for(int i = 0; i < OpponentBoard.length; i ++){
            for (int j = 0; j < OpponentBoard[0].length; j++){
                OpponentBoard[i][j] = CellStatus.EMPTY;
            }
        }
        this.fleet = new ArrayList<>();
        specifications.remove("width");
        specifications.remove("height");
        this.placeBoats(specifications);
        return this.fleet; //Format Ship list into format expected by server

    }


    //Given a fleet from the server, place boats
    private void placeBoats(Map<String, Integer> boats) {

        //Initialize a hashmap of ship types paired to lengths
        Map<String, Integer> reference = new HashMap<>();
        reference.put("carrier", 6);
        reference.put("battleShip", 5);
        reference.put("destroyer", 4);
        reference.put("submarine", 3);
        List<Ship> temp = new ArrayList<>();

        //Create initial set of Ship objects
        for (String s : boats.keySet()){
            for (int i = 0; i < boats.getOrDefault(s, 0); i ++){
                temp.add(new Ship(reference.get(s)));
            }
        }
        Random r = new Random();
        List<Dir> allDirs = new ArrayList<>(Arrays.asList(Dir.VERTICAL, Dir.VERTICAL));

        //Repeatedly place ships at random valid locations until all ships are placed.
         for (Ship s : temp){
             boolean NicksFlag = true;
               do {
                   System.out.println(this.fleet.size());
                   int x = r.nextInt(this.OpponentBoard.length);
                   int y = r.nextInt(this.OpponentBoard[0].length);
                   Dir dir = allDirs.get(r.nextInt(2));
                   s.place(new Coord(x, y), dir);
                   if (this.validCoords(s)) {
                       for (Ship s2 : this.fleet) {
                           if (s.isColliding(s2)) {
                               NicksFlag = false;
                           }
                       }
                   }
                   fleet.add(s);
               } while(NicksFlag);

        }
    }

    private boolean validCoords(Ship s) {
        return s.getStartPoint().x() >= 0 && s.getEndpoint().x() >= 0 && s.getStartPoint().x() < OpponentBoard.length
                && s.getEndpoint().y() < OpponentBoard[0].length;
    }

    /**
     * Process Hits. These hits represent shots fired by this player in the previous salvo that hit boats.
     * 0 indexed.
     * @param shots
     */
    @Override
    public void hits(List<Coord> shots) {
        for (Coord shot : shots) {
            this.OpponentBoard[shot.x()][shot.y()] = CellStatus.HIT;
        }
    }

    @Override
    public void endGame(boolean win) {
        System.out.print("I won");
    }
}
