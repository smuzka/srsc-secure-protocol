package DSTP;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

public class EncryptedDatagramPacket {
    private DatagramPacket packet;

    public EncryptedDatagramPacket(byte[] buffer, int length) {
        this.packet = new DatagramPacket(buffer, length);
    }

    public EncryptedDatagramPacket(String data, InetAddress address, int port) throws InvalidAlgorithmParameterException, NoSuchPaddingException, ShortBufferException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String encryptedData = DSTP.encryptString(data);
        this.packet = new DatagramPacket(encryptedData.getBytes(), encryptedData.getBytes().length, address, port);
    }

    public void setLength(int length) {
        packet.setData(new byte[65536]);
        packet.setLength(length);
    }

    public DatagramPacket getPacket() {
        return packet;
    }

    public byte[] getData() {
        return packet.getData();
    }

    public void encryptData() throws InvalidAlgorithmParameterException, NoSuchPaddingException, ShortBufferException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String decryptedData = new String(packet.getData(), 0, packet.getLength());
        this.packet.setData(DSTP.encryptString(decryptedData).getBytes());
    }

    public void decryptData() throws InvalidAlgorithmParameterException, NoSuchPaddingException, ShortBufferException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String encryptedData = new String(packet.getData(), 0, packet.getLength());
        this.packet.setData(DSTP.decryptString(encryptedData).getBytes());
    }

    public int getLength() {
        return this.packet.getLength();
    }
}