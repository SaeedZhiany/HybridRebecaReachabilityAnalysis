package dataStructure;

import javax.annotation.Nullable;

public enum ConnectionType {
    WIRE,
    CAN;

    @Nullable
    public static ConnectionType byValue(String value) {
        for (ConnectionType connectionType : values()) {
            if (connectionType.name().toLowerCase().equals(value.toLowerCase())) {
                return connectionType;
            }
        }
        return null;
    }
}
