package server;

import game.Coord;
import game.Player;
import game.Ship;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class ProxyPlayer implements Player {

    private final Socket client;
    private final InputStream input;
    private final PrintWriter output;

    private final GameType type;

    public ProxyPlayer(Socket client, GameType type) throws IOException {
        this.client = client;
        this.input = client.getInputStream();
        this.output = new PrintWriter(client.getOutputStream());
        this.type = type;
    }

    public GameType getGameType() {
        return this.type;
    }

    @Override
    public List<Coord> salvo(List<Coord> shots) {
        return null;
    }

    @Override
    public List<Ship> setup(Map<String, Integer> specifications) {
        return null;
    }

    @Override
    public void hits(List<Coord> shots) {

    }

    @Override
    public void endGame(boolean win) {

    }
}
