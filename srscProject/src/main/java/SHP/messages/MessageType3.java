package SHP.messages;

import java.io.BufferedReader;
import java.io.FileReader;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import SHP.Serializer;
import SHP.Util;

public class MessageType3 extends Message {
    private final String ALGORITHM = "PBEWITHSHA256AND192BITAES-CBC-BC";
    private final String PROVIDER = "BC";

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
        byte[] PBEPayload = createPBEPayload();

        byte[] payload = Util.mergeArrays(PBEPayload);

        return payload;
    }

    private byte[] createPBEPayload() {
        byte[] requestSerialized = Serializer.serializeString(request);
        byte[] userIdSerialized = Serializer.serializeString(userId);
        byte[] nonce1Serialized = Serializer.serializeBytes(nonce1);
        byte[] nonce2Serialized = Serializer.serializeBytes(nonce2);
        byte[] udpPortSerialized = Serializer.serializeInt(udpPort);

        byte[] payload = Util.mergeArrays(requestSerialized, userIdSerialized, nonce1Serialized, nonce2Serialized, udpPortSerialized);

        return PBEEncrypt(payload);
    }

    @Override
    public void fromByteArray(byte[] data) {
        data = PBEDecrypt(data);

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

    @Override
    public MessageType getType() {
        return MessageType.TYPE3;
    }

    @Override
    public String toString() {
        return "MessageType3: " + "request=" + request + ", userId=" + userId + ", nonce1=" + Arrays.toString(nonce1) + ", nonce2=" + Arrays.toString(nonce2) + ", udpPort=" + udpPort;
    }

    private byte[] PBEEncrypt(byte[] data) {
        char[] password;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] passwordSHA256 = digest.digest(this.password.getBytes());
            password = Base64.getEncoder().encodeToString(passwordSHA256).toCharArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        byte[] salt = this.salt;
        int counter = Util.bytesToInt(this.counter);

        try {
            PBEKeySpec pbeSpec = new PBEKeySpec(password);
            SecretKeyFactory keyFact = SecretKeyFactory.getInstance(ALGORITHM, PROVIDER);
            Key sKey = keyFact.generateSecret(pbeSpec);

            Cipher cEnc = Cipher.getInstance(ALGORITHM, PROVIDER);
            cEnc.init(Cipher.ENCRYPT_MODE, sKey, new PBEParameterSpec(salt, counter));

            return cEnc.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] PBEDecrypt(byte[] data) {
        String passwordFromFile = getUserPasswordFromFile(this.userId);
        if (passwordFromFile == null) {
            throw new RuntimeException("User not found");
        }
        char[] password = passwordFromFile.toCharArray();
        byte[] salt = this.salt;
        int counter = Util.bytesToInt(this.counter);

        try {
            PBEKeySpec pbeSpec = new PBEKeySpec(password);
            SecretKeyFactory keyFact = SecretKeyFactory.getInstance(ALGORITHM, PROVIDER);
            Key sKey = keyFact.generateSecret(pbeSpec);

            Cipher cDec = Cipher.getInstance(ALGORITHM, PROVIDER);
            cDec.init(Cipher.DECRYPT_MODE, sKey, new PBEParameterSpec(salt, counter));

            return cDec.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getUserPasswordFromFile(String userId) {

        String filePath = "srscProject/src/main/resources/userDatabase.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("UserId,")) {
                    continue;
                }

                String[] columns = line.split(",");

                if (columns[0].equals(userId)) {
                    return columns[1];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
