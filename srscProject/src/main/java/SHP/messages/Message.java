package SHP.messages;

import java.io.*;

abstract public class Message {
    public abstract byte[] toByteArray();

    public abstract void fromByteArray(byte[] data);

    public abstract MessageType getType();

    public abstract String toString();

    public void send(DataOutputStream out) throws IOException {
        byte[] data = toByteArray();
        out.writeInt(data.length);
        out.writeInt(getType().getId());
        out.write(data);
        System.out.println("=============sent message=============");
        System.out.println("length: " + data.length);
        System.out.println("data: " + this.toString());
        System.out.println("======================================");
    }

    public void receive(DataInputStream in) throws IOException {
        int length = in.readInt();
        int messageType = in.readInt();
        byte[] data = new byte[length];
        in.readFully(data);
        fromByteArray(data);
        System.out.println("=============receive message=============");
        System.out.println("length: " + length);
        System.out.println("data: " + this.toString());
        System.out.println("messageType: " + messageType);
        System.out.println("=========================================");
    }
}
