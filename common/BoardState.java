package common;

import java.io.Serializable;

public final class BoardState implements Serializable {
    private static final long serialVersionUID = 1L;

    public final int[][] cells;
    public final int[][] areas;
    public final int turnsRemaining;
    public final int secondsRemaining;
    public final String turnColor;
    public final int redScore;
    public final int blueScore;

    public BoardState(int[][] cells, int turnsRemaining, int secondsRemaining,
            String turnColor, int redScore, int blueScore) {
        this(cells, cells, turnsRemaining, secondsRemaining, turnColor, redScore, blueScore);
    }

    public BoardState(int[][] cells, int[][] areas, int turnsRemaining, int secondsRemaining,
            String turnColor, int redScore, int blueScore) {
        this.cells = copy(cells);
        this.areas = copy(areas);
        this.turnsRemaining = turnsRemaining;
        this.secondsRemaining = secondsRemaining;
        this.turnColor = turnColor;
        this.redScore = redScore;
        this.blueScore = blueScore;
    }

    private static int[][] copy(int[][] source) {
        int[][] result = new int[source.length][];
        for (int row = 0; row < source.length; row++) {
            result[row] = source[row].clone();
        }
        return result;
    }
}