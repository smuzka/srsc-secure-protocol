package SHP;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;

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

    public static String bytesToString (byte[] value) {
        return new String(value);
    }

    public static byte[] stringToBytes (String value) {
        return value.getBytes();
    }

    public static int bytesToInt (byte[] value) {
        return ByteBuffer.wrap(value).getInt();
    }

    public static byte[] intToBytes (int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }
}