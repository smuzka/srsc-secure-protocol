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
    private byte[] nonce5_previous;
    private byte[] nonce5;
    private String password;

    // for server
    public MessageType5(String userId, byte[] nonce5_previous) {
        this.userId = userId;
        this.nonce5_previous = nonce5_previous;
    }

    // for client
    public MessageType5(byte[] nonce5, String password) {
        this.nonce5 = nonce5;
        this.password = password;
    }

    @Override
    public byte[] toByteArray() {
        System.out.println("MessageType5 current: " + Arrays.toString(nonce5));

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

        Serializer<byte[]> nonce5Deserialized = Serializer
                .deserializeFirstBytesInArray(synchronizationString.getRemainingBytes());

        Util.verifyNonce(nonce5_previous, nonce5Deserialized.getExtractedBytes());

        System.out.println("MessageType5 received: " + Arrays.toString(nonce5Deserialized.getExtractedBytes()));
        System.out.println("MessageType5 previous: " + Arrays.toString(nonce5_previous));
    }

    public byte[] createSerializedPayload() {
        byte[] synchronizationStringSerialized = Serializer.serializeString(SYNCHRONIZATION);
        byte[] nonce5Serialized = Serializer.serializeBytes(nonce5);

        return Util.mergeArrays(synchronizationStringSerialized, nonce5Serialized);
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
        return "MessageType5: " + "nonce5=" + Arrays.toString(nonce5) + "nonce5_previous="
                + Arrays.toString(nonce5_previous);
    }
}
