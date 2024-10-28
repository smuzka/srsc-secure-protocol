package DSTP;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

import DSTP.utils.ToHex;

import static DSTP.utils.ToHex.concatArrays;

public class EncryptedDatagramPacket {
    private DatagramPacket packet;

    public EncryptedDatagramPacket(byte[] buffer, int length)
            throws InvalidAlgorithmParameterException, NoSuchPaddingException, ShortBufferException,
            IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        // String encryptedData = DSTP.encryptBytes(buffer.toString());
        // this.packet = new DatagramPacket(encryptedData.getBytes(),
        // encryptedData.getBytes().length);
        this.packet = new DatagramPacket(buffer, length);
    }

    public EncryptedDatagramPacket(byte[] buffer, int length, SocketAddress address) {
        this.packet = new DatagramPacket(buffer, length, address);
    }

    public EncryptedDatagramPacket(byte[] buffer, int length, InetSocketAddress address) {
        this.packet = new DatagramPacket(buffer, length, address);
    }

    public EncryptedDatagramPacket(String data, InetAddress address, int port)
            throws InvalidAlgorithmParameterException, NoSuchPaddingException, ShortBufferException,
            IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        this.packet = new DatagramPacket(data.getBytes(StandardCharsets.UTF_8),
                data.getBytes(StandardCharsets.UTF_8).length, address, port);
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

    public void setSequenceNumber(short sequenceNumber)
            throws InvalidAlgorithmParameterException, NoSuchPaddingException, ShortBufferException,
            IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        byte[] sequenceNumberBytes = { (byte) ((sequenceNumber >> 8) & 0xFF), (byte) (sequenceNumber & 0xFF) };
        byte[] dataFromPacket = getData();

        byte[] encryptedData = DSTP.encryptBytes(concatArrays(sequenceNumberBytes, dataFromPacket));

        this.packet = new DatagramPacket(encryptedData, encryptedData.length, getAddress(),
                getPort());
    }

    public short decryptData() throws InvalidAlgorithmParameterException, NoSuchPaddingException, ShortBufferException,
            IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        byte[] encryptedData = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
        byte[] decryptedData = DSTP.decryptBytes(encryptedData);
        this.packet.setData(Arrays.copyOfRange(decryptedData, 4, decryptedData.length));
        byte[] sequenceBytes = Arrays.copyOfRange(decryptedData, 0, 4);
        short sequenceNumber = (short) (((sequenceBytes[0] & 0xFF) << 8) | (sequenceBytes[1] & 0xFF));
        return sequenceNumber;
    }

    public int getLength() {
        return this.packet.getLength();
    }

    public InetAddress getAddress() {
        return this.packet.getAddress();
    }

    public int getPort() {
        return this.packet.getPort();
    }

    public void setData(byte[] data, int offset, int length)
            throws InvalidAlgorithmParameterException, NoSuchPaddingException, ShortBufferException,
            IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        this.packet.setData(data, offset, length);
    }

    public void setSocketAddress(InetSocketAddress address) {
        this.packet.setSocketAddress(address);
    }

}