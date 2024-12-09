package SHP;

public class Serializer<T> {
    private final T extractedBytes;
    private final byte[] remainingBytes;

    public Serializer(T extractedBytes, byte[] remainingBytes) {
        this.extractedBytes = extractedBytes;
        this.remainingBytes = remainingBytes;
    }

    public T getExtractedBytes() {
        return extractedBytes;
    }

    public byte[] getRemainingBytes() {
        return remainingBytes;
    }

    public static byte[] serializeString(String value) {
        byte[] stringBytes = value.getBytes();
        int stringBytesLength = stringBytes.length;

        byte[] serializedString = new byte[stringBytesLength + 4];
        System.arraycopy(
                Util.intToBytes(stringBytesLength),
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

    public static Serializer<String> deserializeFirstStringInArray(byte[] data) {
        byte[] stringLengthBytes = new byte[4];
        System.arraycopy(data, 0, stringLengthBytes, 0, 4);
        int stringLength = Util.bytesToInt(stringLengthBytes);

        byte[] stringBytes = new byte[stringLength];
        System.arraycopy(data, 4, stringBytes, 0, stringLength);

        byte[] rest = new byte[data.length - 4 - stringLength];
        System.arraycopy(data, 4 + stringLength, rest, 0, rest.length);

        return new Serializer<String>(new String(stringBytes), rest);
    }

    public static byte[] serializeInt(int value) {
        byte[] serializedInt = new byte[4];

        System.arraycopy(
                Util.intToBytes(value),
                0,
                serializedInt,
                0,
                4
        );

        return serializedInt;
    }

    public static Serializer<Integer> deserializeFirstIntInArray(byte[] data) {
        byte[] intBytes = new byte[4];
        System.arraycopy(data, 0, intBytes, 0, 4);

        byte[] rest = new byte[data.length - 4];
        System.arraycopy(data, 4, rest, 0, rest.length);

        return new Serializer<Integer>(Util.bytesToInt(intBytes), rest);
    }

    public static byte[] serializeBytes(byte[] value) {
        byte[] serializedBytes = new byte[value.length + 4];

        System.arraycopy(
                Util.intToBytes(value.length),
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

    public static Serializer<byte[]> deserializeFirstBytesInArray(byte[] data) {
        byte[] bytesLengthBytes = new byte[4];
        System.arraycopy(data, 0, bytesLengthBytes, 0, 4);
        int bytesLength = Util.bytesToInt(bytesLengthBytes);

        byte[] stringBytes = new byte[bytesLength];
        System.arraycopy(data, 4, stringBytes, 0, bytesLength);

        byte[] rest = new byte[data.length - 4 - bytesLength];
        System.arraycopy(data, 4 + bytesLength, rest, 0, rest.length);

        return new Serializer<byte[]>(stringBytes, rest);
    }

}