package common;

import java.io.Serializable;

public record PlayerInfo(String username, double points, int wins, boolean busy)
        implements Serializable {
    private static final long serialVersionUID = 1L;
}
