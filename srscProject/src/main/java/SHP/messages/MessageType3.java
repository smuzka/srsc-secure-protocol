package SHP.messages;

import java.util.Arrays;
import java.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import SHP.ECDSADigitalSignature;
import SHP.HMAC;
import SHP.Serializer;
import SHP.Util;

import static SHP.PBEEncryptor.PBEDecrypt;
import static SHP.PBEEncryptor.PBEEncrypt;
import static SHP.User.getUserPrivateKeyFromClientFile;
import static SHP.User.getUserPublicKeyFromClientFile;
import static SHP.User.getUserPasswordFromFile;;

public class MessageType3 extends Message {

    private String password;
    private byte[] salt;
    private byte[] counter;

    private String request;
    private String userId;
    private byte[] nonce3_previous;
    private byte[] nonce3;
    private byte[] nonce4;
    private int udpPort;

    public MessageType3(byte[] salt, byte[] counter, byte[] nonce3_previous, String userId) {
        this.salt = salt;
        this.counter = counter;
        this.nonce3_previous = nonce3_previous;
        this.userId = userId;
    }

    public MessageType3(String password, byte[] salt, byte[] counter, String request, String userId, byte[] nonce3,
            byte[] nonce4, int udpPort) {
        this.password = password;
        this.salt = salt;
        this.counter = counter;
        this.request = request;
        this.userId = userId;
        this.nonce3 = nonce3;
        this.nonce4 = nonce4;
        this.udpPort = udpPort;
    }

    @Override
    public byte[] toByteArray() {

        byte[] serializedPayload = createSerializedPayload();

        byte[] PBEPayload = PBEEncrypt(serializedPayload, password, salt, counter);
        byte[] PBEPayloadSerialized = Serializer.serializeBytes(PBEPayload);

        String userPrivateKey = getUserPrivateKeyFromClientFile(userId);
        byte[] digitalSignature = ECDSADigitalSignature.sign(serializedPayload, userPrivateKey);
        byte[] digitalSignatureSerialized = Serializer.serializeBytes(digitalSignature);

        byte[] X = Util.mergeArrays(PBEPayloadSerialized, digitalSignatureSerialized);
        byte[] hMac = HMAC.generateHMAC(X, Util.hashPassword(password));

        return Util.mergeArrays(PBEPayloadSerialized, digitalSignatureSerialized, hMac);
    }

    private byte[] createSerializedPayload() {
        byte[] requestSerialized = Serializer.serializeString(request);
        byte[] userIdSerialized = Serializer.serializeString(userId);
        byte[] nonce3Serialized = Serializer.serializeBytes(nonce3);
        byte[] nonce4Serialized = Serializer.serializeBytes(nonce4);
        byte[] udpPortSerialized = Serializer.serializeInt(udpPort);

        return Util.mergeArrays(requestSerialized, userIdSerialized, nonce3Serialized, nonce4Serialized,
                udpPortSerialized);
    }

    @Override
    public void fromByteArray(byte[] data) {

        // Extract the HMAC from the end of the byte array
        byte[] hMac = Arrays.copyOfRange(data, data.length - HMAC.getHMACLength(), data.length);
        byte[] remainingBytes = Arrays.copyOfRange(data, 0, data.length - HMAC.getHMACLength());
        HMAC.verifyHMAC(hMac, remainingBytes, getUserPasswordFromFile(userId).getBytes());

        // Deserialize the remaining bytes
        Serializer<byte[]> dataDeserialized = Serializer.deserializeFirstBytesInArray(remainingBytes);
        decryptAndDeserializeData(dataDeserialized.getExtractedBytes());

        // verify nonce3
        Util.verifyNonce(nonce3_previous, nonce3);

        // Deserialize the digital signature
        Serializer<byte[]> digitalSignatureDeserialized = Serializer
                .deserializeFirstBytesInArray(dataDeserialized.getRemainingBytes());
        Util.verifySignature(digitalSignatureDeserialized.getExtractedBytes(),
                createSerializedPayload(),
                getUserPublicKeyFromClientFile(userId));
    }

    private void decryptAndDeserializeData(byte[] data) {
        data = PBEDecrypt(data, userId, salt, counter);

        Serializer<String> requestDeserialized = Serializer.deserializeFirstStringInArray(data);
        this.request = requestDeserialized.getExtractedBytes();

        Serializer<String> userIdDeserialized = Serializer
                .deserializeFirstStringInArray(requestDeserialized.getRemainingBytes());
        this.userId = userIdDeserialized.getExtractedBytes();

        Serializer<byte[]> nonce3Deserialized = Serializer
                .deserializeFirstBytesInArray(userIdDeserialized.getRemainingBytes());
        this.nonce3 = nonce3Deserialized.getExtractedBytes();

        Serializer<byte[]> nonce4Deserialized = Serializer
                .deserializeFirstBytesInArray(nonce3Deserialized.getRemainingBytes());
        this.nonce4 = nonce4Deserialized.getExtractedBytes();

        Serializer<Integer> udpPortDeserialized = Serializer
                .deserializeFirstIntInArray(nonce4Deserialized.getRemainingBytes());
        this.udpPort = udpPortDeserialized.getExtractedBytes();
    }

    public byte[] getNonce4() {
        return nonce4;
    }

    @Override
    public MessageType getType() {
        return MessageType.TYPE3;
    }

    @Override
    public String toString() {
        return "MessageType3: " + "request=" + request + ", userId=" + userId + ", nonce3=" + Arrays.toString(nonce3)
                + ", nonce4=" + Arrays.toString(nonce4) + ", udpPort=" + udpPort;
    }
}
