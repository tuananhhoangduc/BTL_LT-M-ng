package common;

import java.io.Serializable;

public final class NetworkMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public final MessageType type;
    public String username;
    public String password;
    public String target;
    public String text;
    public String color;
    public int row = -1;
    public int column = -1;
    public boolean accepted;
    public Object payload;

    private NetworkMessage(MessageType type) {
        this.type = type;
    }

    public static NetworkMessage of(MessageType type) {
        return new NetworkMessage(type);
    }
}