package common;

import java.io.Serializable;

/** Server-owned readiness; a client must not infer peer acceptance from a local timer. */
public record RematchStatus(
        String matchId, boolean redAccepted, boolean blueAccepted, boolean closed, String reason)
        implements Serializable {
    private static final long serialVersionUID = 1L;
}
