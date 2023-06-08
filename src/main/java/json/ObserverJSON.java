package json;

import com.fasterxml.jackson.annotation.JsonProperty;
import game.CellStatus;


public record ObserverJSON(
    @JsonProperty("game_id") String id,
    @JsonProperty("game_over") boolean gameOver,
    @JsonProperty("winner") String winner,
    @JsonProperty("player_1") String player1Name,
    @JsonProperty("player_2") String player2Name,
    @JsonProperty("board_1_home") CellStatus[][] board1Home,
    @JsonProperty("board_1_opp") CellStatus[][] board1Opp,
    @JsonProperty("board_2_home") CellStatus[][] board2Home,
    @JsonProperty("board_2_opp") CellStatus[][] board2Opp) {

}
