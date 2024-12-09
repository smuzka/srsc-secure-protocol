package SHP.messages;

import java.util.Arrays;

public class MessageType2 extends Message {
    private byte[] nonce1;
    private byte[] nonce2;
    private byte[] nonce3;

    public MessageType2() {
    }

    public MessageType2(byte[] nonce1, byte[] nonce2, byte[] nonce3) {
        this.nonce1 = nonce1;
        this.nonce2 = nonce2;
        this.nonce3 = nonce3;
    }

    public byte[] getNonce1() {
        return nonce1;
    }

    public byte[] getNonce2() {
        return nonce2;
    }

    public byte[] getNonce3() {
        return nonce3;
    }

    @Override
    public byte[] toByteArray() {
        byte[] payload = new byte[nonce1.length + nonce2.length + nonce3.length];
        System.arraycopy(nonce1, 0, payload, 0, nonce1.length);
        System.arraycopy(nonce2, 0, payload, nonce1.length, nonce2.length);
        System.arraycopy(nonce3, 0, payload, nonce1.length + nonce2.length, nonce3.length);
        return payload;
    }

    @Override
    public void fromByteArray(byte[] data) {
        if (data.length != 48) {
            throw new IllegalArgumentException("Invalid data length");
        }
        this.nonce1 = Arrays.copyOfRange(data, 0, 16);
        this.nonce2 = Arrays.copyOfRange(data, 16, 32);
        this.nonce3 = Arrays.copyOfRange(data, 32, 48);
    }

    @Override
    public MessageType getType() {
        return MessageType.TYPE2;
    }

    @Override
    public String toString() {
        return "MessageType2: nonce1=" + Arrays.toString(nonce1) + ", nonce2=" + Arrays.toString(nonce2) + ", nonce3=" + Arrays.toString(nonce3);
    }
}

