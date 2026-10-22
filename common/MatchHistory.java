package common;

import java.io.Serializable;

public record MatchHistory(String time, String opponent, String color,
        int capturedCells, int opponentCells, String result, double points) implements Serializable {
    private static final long serialVersionUID = 1L;

    public MatchHistory(String time, String opponent, String color,
            int capturedCells, String result, double points) {
        this(time, opponent, color, capturedCells, 0, result, points);
    }
}