package SHP.messages;

import java.io.*;

abstract public class Message {
    private final byte PROTOCOL_VERSION_RELEASE = 0b00000011;

    public abstract byte[] toByteArray();

    public abstract void fromByteArray(byte[] data);

    public abstract MessageType getType();

    public abstract String toString();

    public void send(DataOutputStream out) throws IOException {
        out.writeByte(PROTOCOL_VERSION_RELEASE);
        out.writeByte(getType().getId());

        byte[] data = toByteArray();
        out.writeInt(data.length);
        out.write(data);
        System.out.println("============= sent message =============");
        System.out.println("length: " + data.length);
        System.out.println("data: " + this);
        System.out.println("========================================");
    }

    public void receive(DataInputStream in) throws IOException {
        byte protocolVersionRelease = in.readByte();
        if (protocolVersionRelease != PROTOCOL_VERSION_RELEASE) {
            throw new IOException("Unsupported protocol version");
        }

        byte messageType = in.readByte();
        if (messageType != getType().getId()) {
            throw new IOException("Unexpected message type");
        }

        int length = in.readInt();
        byte[] data = new byte[length];
        in.readFully(data);
        fromByteArray(data);
        System.out.println("============= receive message =============");
        System.out.println("length: " + length);
        System.out.println("data: " + this);
        System.out.println("messageType: " + messageType);
        System.out.println("===========================================");
    }
}
