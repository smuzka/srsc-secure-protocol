package DSTP;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

import DSTP.utils.ToHex;

public class EncryptedDatagramPacket {
    private DatagramPacket packet;

    public EncryptedDatagramPacket(byte[] buffer, int length)
            throws InvalidAlgorithmParameterException, NoSuchPaddingException, ShortBufferException,
            IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String encryptedData = DSTP.encryptString(buffer.toString());
        this.packet = new DatagramPacket(encryptedData.getBytes(), encryptedData.getBytes().length);
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

    public void setSequenceNumber(short sequenceNumber)
            throws InvalidAlgorithmParameterException, NoSuchPaddingException, ShortBufferException,
            IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        byte[] sequenceNumberBytes = { (byte) ((sequenceNumber >> 8) & 0xFF), (byte) (sequenceNumber & 0xFF) };
        String decryptedData = DSTP.decryptString(new String(getData()));
        String encryptedData = DSTP.encryptString(ToHex.toHex(sequenceNumberBytes, 2) + decryptedData);
        this.packet = new DatagramPacket(encryptedData.getBytes(), encryptedData.getBytes().length, getAddress(), getPort());
    }

    public void encryptData() throws InvalidAlgorithmParameterException, NoSuchPaddingException, ShortBufferException,
            IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String decryptedData = new String(packet.getData(), 0, packet.getLength());
        this.packet.setData(DSTP.encryptString(decryptedData).getBytes());
    }

    public short decryptData() throws InvalidAlgorithmParameterException, NoSuchPaddingException, ShortBufferException,
            IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String encryptedData = new String(packet.getData(), 0, packet.getLength());
        String decryptedData = DSTP.decryptString(encryptedData);
        this.packet.setData(decryptedData.substring(4).getBytes());
        byte[] sequenceBytes = ToHex.fromHex(decryptedData.substring(0, 4));
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

    public void setData(byte[] data, int offset, int length) throws InvalidAlgorithmParameterException, NoSuchPaddingException, ShortBufferException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        byte[] encryptedData = DSTP.encryptString(new String(data, offset, length)).getBytes();
        this.packet.setData(encryptedData, offset, encryptedData.length);
    }

    public void setSocketAddress(InetSocketAddress address) {
        this.packet.setSocketAddress(address);
    }

}