package game;

public class Main {

    public static void main(String[] args) {
        Referee r = new Referee(new PlayerImp(), new PlayerImp());
        r.run();
    }
}

