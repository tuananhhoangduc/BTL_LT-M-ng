package common;

import java.io.Serializable;

/** Endpoints are recorded by the authoritative rules, before affected stones disappear. */
public record CaptureRay(
        int startRow, int startColumn, int endRow, int endColumn, int originRow, int originColumn)
        implements Serializable {
    private static final long serialVersionUID = 2L;

    public CaptureRay(int startRow, int startColumn, int endRow, int endColumn) {
        this(
                startRow,
                startColumn,
                endRow,
                endColumn,
                (startRow + endRow) / 2,
                (startColumn + endColumn) / 2);
    }
}
