package common;

import java.util.List;

/** Annotated examples validated against server GameRules by RulesChecks. */
public final class RuleExample {
    private RuleExample() {}

    public static BoardState step(int index) {
        if (index < 0 || index > 2) throw new IllegalArgumentException("Unknown example step");
        int[][] cells = new int[9][9], areas = new int[9][9];
        cells[4][2] = 1;
        cells[4][6] = 2;
        if (index >= 1) cells[4][3] = 1;
        if (index == 2)
            for (int col = 0; col < 6; col++) {
                cells[4][col] = 3;
                areas[4][col] = 3;
            }
        return new BoardState(
                cells,
                areas,
                50,
                15,
                "RED",
                index == 2 ? 6 : 0,
                0,
                "tutorial-" + index,
                index,
                4,
                index == 0 ? 2 : index == 1 ? 3 : 4,
                1,
                index == 2 ? List.of(new CaptureRay(4, 0, 4, 5, 4, 3)) : List.of());
    }
}
