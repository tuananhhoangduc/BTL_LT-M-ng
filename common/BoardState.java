package common;

import java.io.Serializable;

public final class BoardState implements Serializable {
    private static final long serialVersionUID = 3L;

    public final String matchId;
    public final long moveSequence;
    public final int moveRow, moveColumn, moveColor;
    public final java.util.List<CaptureRay> captureRays;
    public final int[][] cells;
    public final int[][] areas;
    public final int turnsRemaining;
    public final int secondsRemaining;
    public final String turnColor;
    public final int redScore;
    public final int blueScore;

    public BoardState(
            int[][] cells,
            int turnsRemaining,
            int secondsRemaining,
            String turnColor,
            int redScore,
            int blueScore) {
        this(cells, cells, turnsRemaining, secondsRemaining, turnColor, redScore, blueScore);
    }

    public BoardState(
            int[][] cells,
            int[][] areas,
            int turnsRemaining,
            int secondsRemaining,
            String turnColor,
            int redScore,
            int blueScore) {
        this(
                cells,
                areas,
                turnsRemaining,
                secondsRemaining,
                turnColor,
                redScore,
                blueScore,
                "",
                0,
                -1,
                -1,
                0,
                java.util.List.of());
    }

    public BoardState(
            int[][] cells,
            int[][] areas,
            int turnsRemaining,
            int secondsRemaining,
            String turnColor,
            int redScore,
            int blueScore,
            String matchId,
            long moveSequence,
            int moveRow,
            int moveColumn,
            int moveColor,
            java.util.List<CaptureRay> rays) {
        this.matchId = matchId;
        this.moveSequence = moveSequence;
        this.moveRow = moveRow;
        this.moveColumn = moveColumn;
        this.moveColor = moveColor;
        this.captureRays = java.util.List.copyOf(rays);
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
