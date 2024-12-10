package SHP.messages;

import java.util.Arrays;

import DSTP.DSTP;
import SHP.HMAC;
import SHP.Serializer;
import SHP.Util;

import static SHP.User.getUserPasswordFromFile;

public class MessageType5 extends Message {
    private final static String SYNCHRONIZATION = "GO";

    private String userId;
    private byte[] nonce1;
    private String password;

    public MessageType5() {
    }

    public MessageType5(byte[] nonce1, String password) {
        this.nonce1 = nonce1;
        this.password = password;
    }

    public MessageType5(String userId, byte[] nonce1) {
        this.userId = userId;
        this.nonce1 = nonce1;
    }

    @Override
    public byte[] toByteArray() {

        byte[] serializedPayload = createSerializedPayload();

        byte[] encryptedPayload = encryptData(serializedPayload);
        byte[] encryptedPayloadSerialized = Serializer.serializeBytes(encryptedPayload);

        byte[] X = Util.mergeArrays(encryptedPayloadSerialized);
        byte[] hMac = HMAC.generateHMAC(X, Util.hashPassword(password));

        return Util.mergeArrays(encryptedPayloadSerialized, hMac);
    }

    @Override
    public void fromByteArray(byte[] data) {
        // Extract the HMAC from the end of the byte array
        byte[] hMac = Arrays.copyOfRange(data, data.length - HMAC.getHMACLength(), data.length);
        byte[] remainingBytes = Arrays.copyOfRange(data, 0, data.length - HMAC.getHMACLength());
        HMAC.verifyHMAC(hMac, remainingBytes, getUserPasswordFromFile(userId).getBytes());

        Serializer<byte[]> encryptedPayload = Serializer.deserializeFirstBytesInArray(remainingBytes);
        byte[] decryptedPayload = decryptData(encryptedPayload.getExtractedBytes());

        Serializer<String> synchronizationString = Serializer.deserializeFirstStringInArray(decryptedPayload);
        if (!SYNCHRONIZATION.equals(synchronizationString.getExtractedBytes())) {
            throw new RuntimeException("Wrong Synchronization String");
        }

        Serializer<byte[]> nonce1Deserialized = Serializer.deserializeFirstBytesInArray(synchronizationString.getRemainingBytes());
        if (!Arrays.equals(this.nonce1, nonce1Deserialized.getExtractedBytes())) {
            throw new RuntimeException("Wrong nonce");
        }

        System.out.println("MessageType5 received: " + this);
    }


    public byte[] createSerializedPayload() {
        byte[] synchronizationStringSerialized = Serializer.serializeString(SYNCHRONIZATION);
        byte[] nonce1Serialized = Serializer.serializeBytes(nonce1);

        return Util.mergeArrays(synchronizationStringSerialized, nonce1Serialized);
    }

    public byte[] encryptData(byte[] data) {
        byte[] encryptedData;

        try {
            encryptedData = DSTP.encryptBytes(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return encryptedData;
    }

    public byte[] decryptData(byte[] data) {
        byte[] decryptedData;

        try {
            decryptedData = DSTP.decryptBytes(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return decryptedData;
    }

    @Override
    public MessageType getType() {
        return MessageType.TYPE5;
    }

    @Override
    public String toString() {
        return "MessageType5: " + Arrays.toString(nonce1);
    }
}
