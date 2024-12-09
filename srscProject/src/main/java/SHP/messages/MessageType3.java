package SHP.messages;

import java.util.Arrays;

public class MessageType3 extends Message {
    private byte[] data;

    public MessageType3() {
        this.data = new byte[0];
    }

    public MessageType3(byte[] data) {
        this.data = data;
    }

    @Override
    public byte[] toByteArray() {
        return data; // Serialize raw data
    }

    @Override
    public void fromByteArray(byte[] data) {
        this.data = data; // Deserialize data
    }

    @Override
    public MessageType getType() {
        return MessageType.TYPE3;
    }

    @Override
    public String toString() {
        return "MessageType3: " + Arrays.toString(data);
    }
}
