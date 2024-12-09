package SHP.messages;

import java.util.Arrays;

import SHP.ECDSADigitalSignature;
import SHP.Serializer;
import SHP.Util;

import static SHP.PBEEncryptor.PBEDecrypt;
import static SHP.PBEEncryptor.PBEEncrypt;
import static SHP.User.getUserPrivateKeyFromClientFile;
import static SHP.User.getUserPublicKeyFromClientFile;

public class MessageType3 extends Message {

    private String password;
    private byte[] salt;
    private byte[] counter;

    private String request;
    private String userId;
    private byte[] nonce1;
    private byte[] nonce2;
    private int udpPort;


    public MessageType3(byte[] salt, byte[] counter, String userId) {
        this.salt = salt;
        this.counter = counter;
        this.userId = userId;
    }

    public MessageType3(String password, byte[] salt, byte[] counter, String request, String userId, byte[] nonce1, byte[] nonce2, int udpPort) {
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

        return Util.mergeArrays(PBEPayloadSerialized, digitalSignatureSerialized);
    }

    private byte[] createSerializedPayload() {
        byte[] requestSerialized = Serializer.serializeString(request);
        byte[] userIdSerialized = Serializer.serializeString(userId);
        byte[] nonce1Serialized = Serializer.serializeBytes(nonce1);
        byte[] nonce2Serialized = Serializer.serializeBytes(nonce2);
        byte[] udpPortSerialized = Serializer.serializeInt(udpPort);

        return Util.mergeArrays(requestSerialized, userIdSerialized, nonce1Serialized, nonce2Serialized, udpPortSerialized);
    }

    @Override
    public void fromByteArray(byte[] data) {
        Serializer<byte[]> dataDeserialized = Serializer.deserializeFirstBytesInArray(data);
        decryptAndDeserializeData(dataDeserialized.getExtractedBytes());

        Serializer<byte[]> digitalSignatureDeserialized = Serializer.deserializeFirstBytesInArray(dataDeserialized.getRemainingBytes());
        verifySignature(digitalSignatureDeserialized.getExtractedBytes());
    }

    private void decryptAndDeserializeData (byte[] data) {
        data = PBEDecrypt(data, userId, salt, counter);

        Serializer<String> requestDeserialized = Serializer.deserializeFirstStringInArray(data);
        this.request = requestDeserialized.getExtractedBytes();

        Serializer<String> userIdDeserialized = Serializer.deserializeFirstStringInArray(requestDeserialized.getRemainingBytes());
        this.userId = userIdDeserialized.getExtractedBytes();

        Serializer<byte[]> nonce1Deserialized = Serializer.deserializeFirstBytesInArray(userIdDeserialized.getRemainingBytes());
        this.nonce1 = nonce1Deserialized.getExtractedBytes();

        Serializer<byte[]> nonce2Deserialized = Serializer.deserializeFirstBytesInArray(nonce1Deserialized.getRemainingBytes());
        this.nonce2 = nonce2Deserialized.getExtractedBytes();

        Serializer<Integer> udpPortDeserialized = Serializer.deserializeFirstIntInArray(nonce2Deserialized.getRemainingBytes());
        this.udpPort = udpPortDeserialized.getExtractedBytes();
    }

    private void verifySignature(byte[] digitalSignature) {
        boolean signatureVerificatied = ECDSADigitalSignature.verifySignature(digitalSignature, createSerializedPayload(), getUserPublicKeyFromClientFile(userId));
        if (!signatureVerificatied) {
            throw new RuntimeException("Signature verification failed");
        }
    }

    @Override
    public MessageType getType() {
        return MessageType.TYPE3;
    }

    @Override
    public String toString() {
        return "MessageType3: " + "request=" + request + ", userId=" + userId + ", nonce1=" + Arrays.toString(nonce1) + ", nonce2=" + Arrays.toString(nonce2) + ", udpPort=" + udpPort;
    }
}
