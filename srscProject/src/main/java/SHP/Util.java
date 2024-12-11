package SHP;

import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.math.BigInteger;

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

    public static PrivateKey getPrivateKeyFromString(String key) {
        try {
            byte[] byteKey = Base64.getDecoder().decode(key);
            KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(byteKey);

            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
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

    public static PublicKey getPublicKeyFromBytes(byte[] byteKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("DH", "BC");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(byteKey);

            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void verifySignature(byte[] digitalSignature, byte[] data, String publicKeyString) {
        boolean signatureVerificatied = ECDSADigitalSignature.verifySignature(digitalSignature,
                data, publicKeyString);
        if (!signatureVerificatied) {
            throw new RuntimeException("Signature verification failed");
        }
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

    public static byte[] incrementNonce(byte[] nonce) {
        if (nonce.length != 16) {
            throw new IllegalArgumentException("Nonce must be 16 bytes long");
        }

        BigInteger nonceBigInt = new BigInteger(1, nonce);
        BigInteger incrementedNonce = nonceBigInt.add(BigInteger.ONE);
        byte[] incrementedNonceBytes = incrementedNonce.toByteArray();

        // Ensure the byte array is 16 bytes long
        if (incrementedNonceBytes.length > 16) {
            incrementedNonceBytes = Arrays.copyOfRange(incrementedNonceBytes, incrementedNonceBytes.length - 16,
                    incrementedNonceBytes.length);
        } else if (incrementedNonceBytes.length < 16) {
            byte[] temp = new byte[16];
            System.arraycopy(incrementedNonceBytes, 0, temp, 16 - incrementedNonceBytes.length,
                    incrementedNonceBytes.length);
            incrementedNonceBytes = temp;
        }

        return incrementedNonceBytes;
    }

    public static void verifyNonce(byte[] nonce_previous, byte[] nonce) {
        try {
            if (!Arrays.equals(Util.incrementNonce(nonce_previous), nonce)) {
                throw new RuntimeException("Nonce verification failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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