package SHP.messages;

import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.BadPaddingException;

import SHP.ECDSADigitalSignature;
import SHP.HMAC;
import SHP.Serializer;
import SHP.Util;

import static SHP.PBEEncryptor.PBEDecrypt;
import static SHP.PBEEncryptor.PBEEncrypt;
import static SHP.User.getUserPrivateKeyFromClientFile;
import static SHP.User.getUserPublicKeyFromClientFile;
import static SHP.User.getUserPasswordFromFile;
import java.security.KeyPair;

public class MessageType4 extends Message {
    private byte[] nonce4;
    private byte[] nonce5;
    private String userId;
    private String requestConfirmation;
    private String cryptoConfig;
    private String password;

    public MessageType4(String password, String userId) {
        this.password = password;
        this.userId = userId;
    }

    public MessageType4(byte[] nonce4, byte[] nonce5, String userId, String requestConfirmation, String cryptoConfig) {
        this.nonce4 = nonce4;
        this.nonce5 = nonce5;
        this.userId = userId;
        this.cryptoConfig = cryptoConfig;
        this.requestConfirmation = requestConfirmation;
    }

    @Override
    public byte[] toByteArray() {
        byte[] serializedPayload = createSerializedPayload();

        // Encrypt with users public key
        byte[] EPayload = EEncrypt(serializedPayload, getUserPublicKeyFromClientFile(userId));
        byte[] EPayloadSerialized = Serializer.serializeBytes(EPayload);

        String userPrivateKey = getUserPrivateKeyFromClientFile(userId);
        byte[] digitalSignature = ECDSADigitalSignature.sign(serializedPayload, userPrivateKey);
        byte[] digitalSignatureSerialized = Serializer.serializeBytes(digitalSignature);

        byte[] X = Util.mergeArrays(EPayloadSerialized, digitalSignatureSerialized);
        byte[] hMac = HMAC.generateHMAC(X, getUserPasswordFromFile(userId).getBytes());

        return Util.mergeArrays(EPayloadSerialized, digitalSignatureSerialized, hMac);
    }

    private byte[] EEncrypt(byte[] data, String publicKeyString) {
        try {
            Cipher cipher = Cipher.getInstance("ECIES", "BC");
            PublicKey publicKey = Util.getPublicKeyFromString(publicKeyString);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private byte[] EDecrypt(byte[] data, String privateKeyString) {
        try {
            Cipher cipher = Cipher.getInstance("ECIES", "BC");
            PrivateKey privateKey = Util.getPrivateKeyFromString(privateKeyString);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private byte[] createSerializedPayload() {
        byte[] requestConfirmationSerialized = Serializer.serializeString(requestConfirmation);
        byte[] userIdSerialized = Serializer.serializeString(userId);
        byte[] nonce4Serialized = Serializer.serializeBytes(nonce4);
        byte[] nonce5Serialized = Serializer.serializeBytes(nonce5);
        byte[] cryptoConfigSerialized = Serializer.serializeString(cryptoConfig);

        return Util.mergeArrays(requestConfirmationSerialized, userIdSerialized, nonce4Serialized, nonce5Serialized,
                cryptoConfigSerialized);
    }

    @Override
    public void fromByteArray(byte[] data) {
        // Extract the HMAC from the end of the byte array
        byte[] hMac = Arrays.copyOfRange(data, data.length - HMAC.getHMACLength(), data.length);
        byte[] remainingBytes = Arrays.copyOfRange(data, 0, data.length - HMAC.getHMACLength());
        HMAC.verifyHMAC(hMac, remainingBytes, Util.hashPassword(password));

        // Deserialize the remaining bytes
        Serializer<byte[]> dataDeserialized = Serializer.deserializeFirstBytesInArray(remainingBytes);
        decryptAndDeserializeData(dataDeserialized.getExtractedBytes());

        Serializer<byte[]> digitalSignatureDeserialized = Serializer
                .deserializeFirstBytesInArray(dataDeserialized.getRemainingBytes());
        Util.verifySignature(digitalSignatureDeserialized.getExtractedBytes(),
                createSerializedPayload(),
                getUserPublicKeyFromClientFile(userId));
    }

    private void decryptAndDeserializeData(byte[] data) {
        data = EDecrypt(data, getUserPrivateKeyFromClientFile(userId));

        Serializer<String> requestConfirmationDeserialized = Serializer.deserializeFirstStringInArray(data);
        this.requestConfirmation = requestConfirmationDeserialized.getExtractedBytes();

        Serializer<String> userIdDeserialized = Serializer
                .deserializeFirstStringInArray(requestConfirmationDeserialized.getRemainingBytes());
        this.userId = userIdDeserialized.getExtractedBytes();

        Serializer<byte[]> nonce4Deserialized = Serializer
                .deserializeFirstBytesInArray(userIdDeserialized.getRemainingBytes());
        this.nonce4 = nonce4Deserialized.getExtractedBytes();

        Serializer<byte[]> nonce5Deserialized = Serializer
                .deserializeFirstBytesInArray(nonce4Deserialized.getRemainingBytes());
        this.nonce5 = nonce5Deserialized.getExtractedBytes();

        Serializer<String> cryptoConfigDeserialized = Serializer
                .deserializeFirstStringInArray(nonce5Deserialized.getRemainingBytes());
        this.cryptoConfig = cryptoConfigDeserialized.getExtractedBytes();
    }

    @Override
    public MessageType getType() {
        return MessageType.TYPE4;
    }

    @Override
    public String toString() {
        return "MessageType4: " + "request-confirmation=" + requestConfirmation + ", userId=" + userId + ", nonce4="
                + Arrays.toString(nonce4) + ", nonce5=" + Arrays.toString(nonce5) + ", cryptoConfig=" + cryptoConfig;
    }
}
