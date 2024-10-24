package srscProject.src.main.java.Utils;

public class ToHex {

    private final static String digits = "0123456789abcdef";

    /**
     * Return string hexadecimal from byte array of certain size
     *
     * @param data   : bytes to convert
     * @param length : nr of bytes in data block to be converted
     * @return hex : hexadecimal representation of data
     */

    public static String toHex(byte[] data, int length) {
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i != length; i++) {
            int v = data[i] & 0xff;

            buf.append(digits.charAt(v >> 4));
            buf.append(digits.charAt(v & 0xf));
        }

        return buf.toString();
    }

}
