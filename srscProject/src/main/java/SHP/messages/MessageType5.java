package SHP.messages;

import java.util.Arrays;

public class MessageType5 extends Message {
    private byte[] metadata;

    public MessageType5() {
        this.metadata = new byte[0];
    }

    public MessageType5(byte[] metadata) {
        this.metadata = metadata;
    }

    @Override
    public byte[] toByteArray() {
        return metadata; // Serialize metadata
    }

    @Override
    public void fromByteArray(byte[] data) {
        this.metadata = data; // Deserialize metadata
    }

    @Override
    public MessageType getType() {
        return MessageType.TYPE5;
    }

    @Override
    public String toString() {
        return "MessageType5: " + Arrays.toString(metadata);
    }
}
