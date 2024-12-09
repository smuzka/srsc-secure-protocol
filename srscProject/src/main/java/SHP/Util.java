package SHP;

import java.nio.ByteBuffer;
import java.security.SecureRandom;

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

    public static byte[] serializeString(String value) {
        byte[] stringBytes = value.getBytes();
        int stringBytesLength = stringBytes.length;

        byte[] serializedString = new byte[stringBytesLength + 4];
        System.arraycopy(
                ByteBuffer.allocate(4).putInt(stringBytesLength).array(),
                0,
                serializedString,
                0,
                4
        );
        System.arraycopy(
                stringBytes,
                0,
                serializedString,
                4,
                stringBytesLength
        );

        return serializedString;
    }

    public static byte[] deserializeFirstStringInArray(byte[] data) {
        byte[] stringLengthBytes = new byte[4];
        System.arraycopy(data, 0, stringLengthBytes, 0, 4);
        int stringLength = ByteBuffer.wrap(stringLengthBytes).getInt();

        byte[] stringBytes = new byte[stringLength];
        System.arraycopy(data, 4, stringBytes, 0, stringLength);

        return stringBytes;
    }

    public static byte[] serializeInt(int value) {
        byte[] serializedInt = new byte[4];

        System.arraycopy(
                ByteBuffer.allocate(4).putInt(value).array(),
                0,
                serializedInt,
                0,
                4
        );

        return serializedInt;
    }

    public static byte[] deserializeFirstIntInArray(byte[] data) {
        byte[] intBytes = new byte[4];
        System.arraycopy(data, 0, intBytes, 0, 4);

        return intBytes;
    }

    public static byte[] serializeBytes(byte[] value) {
        byte[] serializedBytes = new byte[value.length + 4];

        System.arraycopy(
                ByteBuffer.allocate(4).putInt(value.length).array(),
                0,
                serializedBytes,
                0,
                4
        );
        System.arraycopy(
                value,
                0,
                serializedBytes,
                4,
                value.length
        );

        return serializedBytes;
    }

    public static byte[] deserializeFirstBytesInArray(byte[] data) {
        byte[] bytesLengthBytes = new byte[4];
        System.arraycopy(data, 0, bytesLengthBytes, 0, 4);
        int bytesLength = ByteBuffer.wrap(bytesLengthBytes).getInt();

        byte[] stringBytes = new byte[bytesLength];
        System.arraycopy(data, 4, stringBytes, 0, bytesLength);

        return stringBytes;
    }
}
