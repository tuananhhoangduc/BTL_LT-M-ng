package server;

import common.CaptureRay;

import java.util.ArrayList;
import java.util.List;

final class GameRules {
    private GameRules() {}

    static boolean applyMove(int[][] board, int[][] areas, int color, int row, int column) {
        return applyMove(board, areas, color, row, column, new ArrayList<>());
    }

    static boolean applyMove(
            int[][] board, int[][] areas, int color, int row, int column, List<CaptureRay> rays) {
        int area = color == 1 ? 3 : 4;
        int stone = color;
        int opponentStone = color == 1 ? 2 : 1;
        int opponentArea = color == 1 ? 4 : 3;
        if (!inside(row, column, board)
                || (board[row][column] != 0 && board[row][column] != area)) {
            return false;
        }

        board[row][column] = stone;
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
        for (int[] direction : directions) {
            List<int[]> line = findLine(board, row, column, direction[0], direction[1], stone);
            if (line.size() >= 3) {
                int[] center = line.get(line.size() / 2);
                int[] first =
                        rayEnd(
                                board,
                                center[0],
                                center[1],
                                -direction[0],
                                -direction[1],
                                opponentStone);
                int[] last =
                        rayEnd(
                                board,
                                center[0],
                                center[1],
                                direction[0],
                                direction[1],
                                opponentStone);
                rays.add(
                        new CaptureRay(first[0], first[1], last[0], last[1], center[0], center[1]));
                shootRay(
                        board,
                        areas,
                        center[0] + direction[0],
                        center[1] + direction[1],
                        direction[0],
                        direction[1],
                        area,
                        opponentStone,
                        opponentArea);
                shootRay(
                        board,
                        areas,
                        center[0] - direction[0],
                        center[1] - direction[1],
                        -direction[0],
                        -direction[1],
                        area,
                        opponentStone,
                        opponentArea);
                for (int[] position : line) {
                    board[position[0]][position[1]] = area;
                    areas[position[0]][position[1]] = area;
                }
            }
        }
        return true;
    }

    private static int[] rayEnd(int[][] board, int row, int col, int dr, int dc, int opponent) {
        while (inside(row + dr, col + dc, board) && board[row + dr][col + dc] != opponent) {
            row += dr;
            col += dc;
        }
        return new int[] {row, col};
    }

    private static List<int[]> findLine(
            int[][] board, int row, int column, int rowStep, int columnStep, int stone) {
        List<int[]> line = new ArrayList<>();
        int startRow = row;
        int startColumn = column;
        while (inside(startRow - rowStep, startColumn - columnStep, board)
                && board[startRow - rowStep][startColumn - columnStep] == stone) {
            startRow -= rowStep;
            startColumn -= columnStep;
        }
        int currentRow = startRow;
        int currentColumn = startColumn;
        while (inside(currentRow, currentColumn, board)
                && board[currentRow][currentColumn] == stone) {
            line.add(new int[] {currentRow, currentColumn});
            currentRow += rowStep;
            currentColumn += columnStep;
        }
        return line;
    }

    private static void shootRay(
            int[][] board,
            int[][] areas,
            int row,
            int column,
            int rowStep,
            int columnStep,
            int area,
            int opponentStone,
            int opponentArea) {
        while (inside(row, column, board)) {
            if (board[row][column] == opponentStone) {
                return;
            }
            if (board[row][column] == 0 || board[row][column] == opponentArea) {
                board[row][column] = area;
                areas[row][column] = area;
            }
            row += rowStep;
            column += columnStep;
        }
    }

    private static boolean inside(int row, int column, int[][] board) {
        return row >= 0 && row < board.length && column >= 0 && column < board[row].length;
    }

    static int count(int[][] areas, int area) {
        int total = 0;
        for (int[] row : areas) {
            for (int cell : row) {
                if (cell == area) {
                    total++;
                }
            }
        }
        return total;
    }
}
