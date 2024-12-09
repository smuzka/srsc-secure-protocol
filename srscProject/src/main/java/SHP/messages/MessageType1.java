package SHP.messages;

import java.util.Arrays;

public class MessageType1 extends Message {
    private byte[] userId;

    public MessageType1() {
        this.userId = new byte[0];
    }

    public MessageType1(byte[] payload) {
        this.userId = payload;
    }

    @Override
    public byte[] toByteArray() {
        return userId;
    }

    @Override
    public void fromByteArray(byte[] data) {
        this.userId = data;
    }

    @Override
    public MessageType getType() {
        return MessageType.TYPE1;
    }

    @Override
    public String toString() {
        return "MessageType1: " + Arrays.toString(userId);
    }
}
