package bittorrent;

public enum MessageType {
    READY,
    STANDBY,
    GET_CHUNK,
    GET_AVAILABLE_CHUNKS,
    BIT_FIELD_LENGTH,
    BIT_FIELD
}
