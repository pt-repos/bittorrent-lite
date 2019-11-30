package bittorrent;

public enum MessageType {
    READY,
    STANDBY,
    SHUTDOWN,
    GET_CHUNK,
    GET_AVAILABLE_CHUNKS,
    BIT_FIELD_LENGTH,
    BIT_FIELD
}
