package client;

import common.BoardState;

import java.util.Objects;

/** Timer/spectator snapshots must never replay a move, including a late spectator's first state. */
final class BoardFeedback {
    private BoardState previous;

    record Change(boolean reset, boolean move, boolean turn, boolean tick) {}

    Change accept(BoardState state, String ownColor) {
        boolean reset = previous == null || !Objects.equals(previous.matchId, state.matchId);
        boolean move = !reset && state.moveSequence > previous.moveSequence;
        boolean turn = !reset && !Objects.equals(previous.turnColor, state.turnColor);
        boolean tick =
                !reset
                        && Objects.equals(previous.turnColor, state.turnColor)
                        && state.turnsRemaining == previous.turnsRemaining
                        && state.secondsRemaining < previous.secondsRemaining
                        && state.secondsRemaining > 0
                        && state.secondsRemaining <= 5
                        && Objects.equals(ownColor, state.turnColor);
        previous = state;
        return new Change(reset, move, turn, tick);
    }
}
