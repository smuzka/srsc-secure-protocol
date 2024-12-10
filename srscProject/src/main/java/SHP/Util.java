package SHP;

import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class Util {

    public static byte[] createNonce() {
        byte[] nonce = new byte[16];
        try {
            SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
            prng.nextBytes(nonce);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nonce;
    }

    public static PublicKey getPublicKeyFromString(String key) {
        try {
            byte[] byteKey = Base64.getDecoder().decode(key);
            KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(byteKey);

            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static byte[] hashPassword(String password) {
        try {
            byte[] passwordHash = MessageDigest.getInstance("SHA-256").digest(password.getBytes());
            return Base64.getEncoder().encodeToString(passwordHash).getBytes();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Sha-256 algorithm not found", e);
        }
    }

    public static byte[] mergeArrays(byte[]... arrays) {
        int totalLength = Arrays.stream(arrays).mapToInt(arr -> arr.length).sum();
        byte[] result = new byte[totalLength];

        int currentPosition = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, currentPosition, array.length);
            currentPosition += array.length;
        }

        return result;
    }

    public static String bytesToString(byte[] value) {
        return new String(value);
    }

    public static byte[] stringToBytes(String value) {
        return value.getBytes();
    }

    public static int bytesToInt(byte[] value) {
        return ByteBuffer.wrap(value).getInt();
    }

    public static byte[] intToBytes(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }
}