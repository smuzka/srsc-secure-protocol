package DSTP;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

public class EncryptedMulticastSocket implements EncryptedSocket {

    private MulticastSocket socket;
    private short sequenceNumberSend = 0;
    private short sequenceNumberReceive = 0;

    public EncryptedMulticastSocket(int port) {
        try {
            socket = new MulticastSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public EncryptedMulticastSocket(InetSocketAddress address) {
        try {
            socket = new MulticastSocket(address);
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

    public void send(EncryptedDatagramPacket packet)
            throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, ShortBufferException,
            IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        // Add sequence number to the packet
        packet.setSequenceNumber(sequenceNumberSend);
        socket.send(packet.getPacket());
        sequenceNumberSend++;
    }

    public void receive(EncryptedDatagramPacket packet)
            throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, ShortBufferException,
            IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        socket.receive(packet.getPacket());
        short packetSequenceNumber = packet.decryptData();
        if (packetSequenceNumber != sequenceNumberReceive) {
            System.out.println("\033[1;31m Sequence number does not match, packet(s) lost! my seq num: "
                    + sequenceNumberReceive + " vs packet seq num: " + packetSequenceNumber + "\033[0m");
            sequenceNumberReceive = packetSequenceNumber;
        }
        System.out.println("Received packet with sequence number: " + packetSequenceNumber + ", my sequence number: "
                + sequenceNumberReceive);
        sequenceNumberReceive++;
    }

    public void close() {
        socket.close();
    }

}