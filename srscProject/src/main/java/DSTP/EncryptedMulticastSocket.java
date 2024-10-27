package DSTP;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

public class EncryptedMulticastSocket {

    private MulticastSocket socket;

    public EncryptedMulticastSocket(int port) {
        try {
            socket = new MulticastSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public EncryptedMulticastSocket() {
        try {
            socket = new MulticastSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void joinGroup(InetAddress group) throws IOException {
        socket.joinGroup(group);
    }

    public void send(EncryptedDatagramPacket packet) throws IOException {
        socket.send(packet.getPacket());
    }

    public void receive(EncryptedDatagramPacket packet) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, ShortBufferException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        socket.receive(packet.getPacket());
        packet.decryptData();
    }

    public void close() {
        socket.close();
    }

}