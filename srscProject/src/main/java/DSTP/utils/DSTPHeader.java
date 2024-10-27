package DSTP.utils;

public class DSTPHeader {
    private short version;
    private byte release;
    private short payloadLength;

    public DSTPHeader(short version, byte release, short payloadLength) {
        this.version = version;
        this.release = release;
        this.payloadLength = payloadLength;
    }

    public static DSTPHeader fromEncodedHeader(String header) {
        byte[] headerBytes = ToHex.fromHex(header);

        if (headerBytes.length != 5) {
            throw new IllegalArgumentException("Header is invalid.");
        }

        // Decode the header
        short version = (short) (((headerBytes[0] & 0xFF) << 8) | (headerBytes[1] & 0xFF));
        byte release = headerBytes[2]; // Assigning the release byte from header
        short payloadLength = (short) (((headerBytes[3] & 0xFF) << 8) | (headerBytes[4] & 0xFF));

        // Return a HeaderInfo object with the decoded results
        return new DSTPHeader(version, release, payloadLength);
    }

    public String encode() {
        byte[] header = new byte[5];
        byte[] versionBytes = { (byte) ((version >> 8) & 0xFF), (byte) (version & 0xFF) };
        byte[] releaseBytes = { (byte) release };
        byte[] payloadLengthBytes = { (byte) ((payloadLength >> 8) & 0xFF), (byte) (payloadLength & 0xFF) };
        System.arraycopy(versionBytes, 0, header, 0, versionBytes.length);
        System.arraycopy(releaseBytes, 0, header, 2, releaseBytes.length);
        System.arraycopy(payloadLengthBytes, 0, header, 3, payloadLengthBytes.length);
        return ToHex.toHex(header, 5).toString();
    }

    public short getVersion() {
        return version;
    }

    public byte getRelease() {
        return release;
    }

    public short getPayloadLength() {
        return payloadLength;
    }

    @Override
    public String toString() {
        return "HeaderInfo{" +
                "version=" + version +
                ", release=" + release +
                ", payloadLength=" + payloadLength +
                '}';
    }
}