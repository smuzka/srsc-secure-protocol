package SHP.messages;

import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;

import java.security.KeyPair;
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

    public MessageType4(byte[] nonce4, byte[] nonce5, String userId, String requestConfirmation, String cryptoConfig) {
        this.nonce4 = nonce4;
        this.nonce5 = nonce5;
        this.userId = userId;
        this.cryptoConfig = cryptoConfig;
        this.requestConfirmation = requestConfirmation;
    }

    // public MessageType4(String password, byte[] salt, byte[] counter, String
    // request, String userId, byte[] nonce1,
    // byte[] nonce2, int udpPort) {
    // this.password = password;
    // this.salt = salt;
    // this.counter = counter;
    // this.request = request;
    // this.userId = userId;
    // this.nonce1 = nonce1;
    // this.nonce2 = nonce2;
    // this.udpPort = udpPort;
    // }

    @Override
    public byte[] toByteArray() {
        byte[] serializedPayload = createSerializedPayload();

        // Encrypt with users public key

        // cipher.init(Cipher.DECRYPT_MODE, ecKeyPair.getPrivate());
        // byte[] plaintext = cipher.doFinal(cipherText);

        // System.out.println("plain : " + new String(plaintext));
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

    private byte[] createSerializedPayload() {
        byte[] nonce4Serialized = Serializer.serializeBytes(nonce4);
        byte[] nonce5Serialized = Serializer.serializeBytes(nonce5);
        byte[] requestConfirmationSerialized = Serializer.serializeString(requestConfirmation);
        byte[] cryptoConfigSerialized = Serializer.serializeString(cryptoConfig);
        byte[] userIdSerialized = Serializer.serializeString(userId);

        return Util.mergeArrays(nonce4Serialized, nonce5Serialized, requestConfirmationSerialized,
                cryptoConfigSerialized, userIdSerialized);
    }

    @Override
    public void fromByteArray(byte[] data) {
        // this.numbers = data; // Deserialize numbers
    }

    @Override
    public MessageType getType() {
        return MessageType.TYPE4;
    }

    @Override
    public String toString() {
        return "MessageType4: ";
        // return "MessageType4: " + Arrays.toString(numbers);
        // make it return the actual values
    }
}
