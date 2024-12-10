package SHP.messages;

import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;

import SHP.ECDSADigitalSignature;
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
    private byte[] nonce1;
    private byte[] nonce2;
    private int udpPort;
    private static String hmacAlgorithm = "HMacSHA3-512";

    public MessageType3(byte[] salt, byte[] counter, String userId) {
        this.salt = salt;
        this.counter = counter;
        this.userId = userId;
    }

    public MessageType3(String password, byte[] salt, byte[] counter, String request, String userId, byte[] nonce1,
            byte[] nonce2, int udpPort) {
        this.password = password;
        this.salt = salt;
        this.counter = counter;
        this.request = request;
        this.userId = userId;
        this.nonce1 = nonce1;
        this.nonce2 = nonce2;
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
        byte[] hMac = generateHMAC(X, hashPassword(password));

        return Util.mergeArrays(PBEPayloadSerialized, digitalSignatureSerialized, hMac);
    }

    private byte[] createSerializedPayload() {
        byte[] requestSerialized = Serializer.serializeString(request);
        byte[] userIdSerialized = Serializer.serializeString(userId);
        byte[] nonce1Serialized = Serializer.serializeBytes(nonce1);
        byte[] nonce2Serialized = Serializer.serializeBytes(nonce2);
        byte[] udpPortSerialized = Serializer.serializeInt(udpPort);

        return Util.mergeArrays(requestSerialized, userIdSerialized, nonce1Serialized, nonce2Serialized,
                udpPortSerialized);
    }

    @Override
    public void fromByteArray(byte[] data) {
        int hMacLength;
        try {
            hMacLength = Mac.getInstance(hmacAlgorithm).getMacLength();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Selected hmac algorithm not found", e);
        }
        // Extract the HMAC from the end of the byte array
        byte[] hMac = Arrays.copyOfRange(data, data.length - hMacLength, data.length);
        byte[] remainingBytes = Arrays.copyOfRange(data, 0, data.length - hMacLength);
        verifyHMAC(hMac, remainingBytes);

        // Deserialize the remaining bytes
        Serializer<byte[]> dataDeserialized = Serializer.deserializeFirstBytesInArray(remainingBytes);
        decryptAndDeserializeData(dataDeserialized.getExtractedBytes());

        Serializer<byte[]> digitalSignatureDeserialized = Serializer
                .deserializeFirstBytesInArray(dataDeserialized.getRemainingBytes());
        verifySignature(digitalSignatureDeserialized.getExtractedBytes());
    }

    private void decryptAndDeserializeData(byte[] data) {
        data = PBEDecrypt(data, userId, salt, counter);

        Serializer<String> requestDeserialized = Serializer.deserializeFirstStringInArray(data);
        this.request = requestDeserialized.getExtractedBytes();

        Serializer<String> userIdDeserialized = Serializer
                .deserializeFirstStringInArray(requestDeserialized.getRemainingBytes());
        this.userId = userIdDeserialized.getExtractedBytes();

        Serializer<byte[]> nonce1Deserialized = Serializer
                .deserializeFirstBytesInArray(userIdDeserialized.getRemainingBytes());
        this.nonce1 = nonce1Deserialized.getExtractedBytes();

        Serializer<byte[]> nonce2Deserialized = Serializer
                .deserializeFirstBytesInArray(nonce1Deserialized.getRemainingBytes());
        this.nonce2 = nonce2Deserialized.getExtractedBytes();

        Serializer<Integer> udpPortDeserialized = Serializer
                .deserializeFirstIntInArray(nonce2Deserialized.getRemainingBytes());
        this.udpPort = udpPortDeserialized.getExtractedBytes();
    }

    private byte[] hashPassword(String password) {
        try {
            byte[] passwordHash = MessageDigest.getInstance("SHA-256").digest(password.getBytes());
            return Base64.getEncoder().encodeToString(passwordHash).getBytes();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Sha-256 algorithm not found", e);
        }
    }

    private void verifyHMAC(byte[] hMac, byte[] data) {
        byte[] generatedHMAC = generateHMAC(data, getUserPasswordFromFile(userId).getBytes());
        if (!Arrays.equals(hMac, generatedHMAC)) {
            throw new RuntimeException("HMAC verification failed");
        }
    }

    private void verifySignature(byte[] digitalSignature) {
        boolean signatureVerificatied = ECDSADigitalSignature.verifySignature(digitalSignature,
                createSerializedPayload(), getUserPublicKeyFromClientFile(userId));
        if (!signatureVerificatied) {
            throw new RuntimeException("Signature verification failed");
        }
    }

    private byte[] generateHMAC(byte[] data, byte[] passwordHash) {
        Mac hMac;
        try {
            hMac = Mac.getInstance(hmacAlgorithm);
            Key hMacKey = new SecretKeySpec(passwordHash, hmacAlgorithm);
            hMac.init(hMacKey);
            hMac.update(data);
            return hMac.doFinal();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Selected hmac algorithm not found", e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Invalid key for hmac algorithm", e);
        }
    }

    @Override
    public MessageType getType() {
        return MessageType.TYPE3;
    }

    @Override
    public String toString() {
        return "MessageType3: " + "request=" + request + ", userId=" + userId + ", nonce1=" + Arrays.toString(nonce1)
                + ", nonce2=" + Arrays.toString(nonce2) + ", udpPort=" + udpPort;
    }
}
