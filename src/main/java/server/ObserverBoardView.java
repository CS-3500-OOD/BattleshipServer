package server;

import game.CellStatus;
import game.Coord;
import java.util.List;

public class ObserverBoardView {

  private CellStatus[][] board;

  public ObserverBoardView(int height, int width) {
    this.board = new CellStatus[height][width];
    for(int row = 0; row < height; row++) {
      for(int col = 0; col < width; col++) {
        this.board[row][col] = CellStatus.EMPTY;
      }
    }
  }

  public CellStatus[][] getBoard() {
    return board;
  }

  public void addCoordsWithStatus(List<Coord> coords, CellStatus status) {
    for(Coord coord : coords) {
      if(inBounds(coord)) {
        this.board[coord.y()][coord.x()] = status;
      }
    }
  }

  private boolean inBounds(Coord coord) {
    return coord.x() >= 0 && coord.y() >= 0
        && coord.x() < this.board[0].length
        && coord.y() < this.board.length;
  }

}
