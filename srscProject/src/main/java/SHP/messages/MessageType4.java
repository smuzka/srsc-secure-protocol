package SHP.messages;

import java.util.Arrays;

public class MessageType4 extends Message {
    private byte[] numbers;

    public MessageType4() {
        this.numbers = new byte[0];
    }

    public MessageType4(byte[] numbers) {
        this.numbers = numbers;
    }

    @Override
    public byte[] toByteArray() {
        return numbers; // Serialize numbers
    }

    @Override
    public void fromByteArray(byte[] data) {
        this.numbers = data; // Deserialize numbers
    }

    @Override
    public MessageType getType() {
        return MessageType.TYPE4;
    }

    @Override
    public String toString() {
        return "MessageType4: " + Arrays.toString(numbers);
    }
}

