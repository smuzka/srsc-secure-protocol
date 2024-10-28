package DSTP;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

public class EncryptedDatagramSocket implements EncryptedSocket {

    DatagramSocket socket;

    public EncryptedDatagramSocket(int port) throws SocketException, InvalidAlgorithmParameterException, NoSuchPaddingException, ShortBufferException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        DSTP.init();
        this.socket = new DatagramSocket(port);
    }

    public EncryptedDatagramSocket(SocketAddress address) throws SocketException, InvalidAlgorithmParameterException, NoSuchPaddingException, ShortBufferException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        DSTP.init();
        this.socket = new DatagramSocket(address);
    }

    public EncryptedDatagramSocket() throws SocketException, InvalidAlgorithmParameterException, NoSuchPaddingException, ShortBufferException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        DSTP.init();
        this.socket = new DatagramSocket();
    }

    public int getLocalPort() {
        return socket.getLocalPort();
    }

    public void receive(EncryptedDatagramPacket packet) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, ShortBufferException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        socket.receive(packet.getPacket());
        packet.decryptData();
    }

    public void send(EncryptedDatagramPacket packet) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, ShortBufferException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        socket.send(packet.getPacket());
    }

    public void close() {
        socket.close();
    }

    public void setSoTimeout(int timeout) {
        try {
            socket.setSoTimeout(timeout);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
