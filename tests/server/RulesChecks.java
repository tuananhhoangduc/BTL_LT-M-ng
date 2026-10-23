package server;

import common.CaptureRay;

import java.util.*;

public final class RulesChecks {
    private static void check(boolean value, String text) {
        if (!value) throw new AssertionError(text);
    }

    public static void main(String[] args) {
        common.BoardState tutorial = common.RuleExample.step(1);
        List<CaptureRay> tutorialRays = new ArrayList<>();
        check(
                GameRules.applyMove(tutorial.cells, tutorial.areas, 1, 4, 4, tutorialRays),
                "Tutorial move accepted");
        common.BoardState expected = common.RuleExample.step(2);
        check(
                Arrays.deepEquals(tutorial.cells, expected.cells)
                        && Arrays.deepEquals(tutorial.areas, expected.areas),
                "Tutorial exactly matches authoritative rules");
        check(
                tutorialRays.equals(expected.captureRays),
                "Tutorial ray uses correct blocker endpoint");
        for (int[] d : new int[][] {{0, 1}, {1, 0}, {1, 1}, {1, -1}}) {
            int[][] board = new int[9][9], areas = new int[9][9];
            List<CaptureRay> rays = new ArrayList<>();
            board[4 - d[0]][4 - d[1]] = 1;
            board[4 + d[0]][4 + d[1]] = 1;
            check(GameRules.applyMove(board, areas, 1, 4, 4, rays), "Valid move");
            check(rays.size() == 1, "Exactly one capture ray");
            check(GameRules.count(areas, 3) == 9, "Entire diagonal/row/column captured");
            check(board[4][4] == 3, "Source stones become territory");
        }
        int[][] board = new int[9][9], areas = new int[9][9];
        List<CaptureRay> rays = new ArrayList<>();
        board[4][2] = 1;
        board[4][3] = 1;
        board[4][6] = 2;
        check(GameRules.applyMove(board, areas, 1, 4, 4, rays), "Capture before blocker");
        check(rays.get(0).endColumn() == 5, "Ray ends before opposing stone");
        check(
                rays.get(0).originRow() == 4 && rays.get(0).originColumn() == 3,
                "Asymmetric ray preserves actual center, not endpoint midpoint");
        check(board[4][6] == 2 && board[4][7] == 0, "Opponent and beyond unchanged");
        check(GameRules.count(areas, 3) == 6, "Capture six cells before blocker");
        check(
                !GameRules.applyMove(board, areas, 1, 4, 6, new ArrayList<>()),
                "Reject opponent cell");
        check(!GameRules.applyMove(board, areas, 1, -1, 0, new ArrayList<>()), "Reject off-board");
        board[0][0] = 3;
        areas[0][0] = 3;
        check(
                GameRules.applyMove(board, areas, 1, 0, 0, new ArrayList<>()),
                "Place on own territory");
        check(board[0][0] == 1 && areas[0][0] == 3, "Preserve territory under crystal");
        board = new int[9][9];
        areas = new int[9][9];
        rays = new ArrayList<>();
        board[4][3] = 1;
        board[4][5] = 1;
        board[3][4] = 1;
        board[5][4] = 1;
        GameRules.applyMove(board, areas, 1, 4, 4, rays);
        check(
                rays.size() == 1,
                "Preserve sequential rule semantics: horizontal consumes center first");
        System.out.println(
                "PASS: four directions, blocker ray metadata, invalid moves, own territory,"
                        + " sequential rules");
    }
}
