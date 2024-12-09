package SHP.messages;

public enum MessageType {
    TYPE1((byte) 0b00000001),
    TYPE2((byte) 0b00000010),
    TYPE3((byte) 0b00000011),
    TYPE4((byte) 0b00000100),
    TYPE5((byte) 0b00000101);

    private final byte id;

    MessageType(byte id) {
        this.id = id;
    }

    public byte getId() {
        return id;
    }

    public static MessageType fromId(byte id) {
        for (MessageType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid MessageType ID: " + id);
    }
}