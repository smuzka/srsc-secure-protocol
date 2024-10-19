package srscProject.src.main.java;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import srscProject.src.main.java.Utils.HexToByteArray;
import srscProject.src.main.java.Utils.RandomBytesGenerator;


public class main {
    public static void printMessage() throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, InvalidKeyException {

        Map<String, String> variables = ReadFile.getVariables(new String[0]);

//        Generate Key
        byte[] keyBytes = RandomBytesGenerator.getRandomBytes(variables.get("SYMMETRIC_KEY_SIZE"));
        SecretKeySpec key = new SecretKeySpec(keyBytes, "DES");
        String configSymmetricKey = variables.get("SYMMETRIC_KEY");
        if (configSymmetricKey != null) {
            key = new SecretKeySpec(HexToByteArray.hexStringToByteArray(configSymmetricKey), "DES");
        }

//        Generate IV
        String configIVSize = variables.get("IV Size");
        IvParameterSpec ivSpec = new IvParameterSpec(new byte[Integer.parseInt(configIVSize)]);
        String configIV = variables.get("IV");
        if (configIV != null) {
            ivSpec = new IvParameterSpec(HexToByteArray.hexStringToByteArray(configIV));
        }

        Cipher cipher = Cipher.getInstance(variables.get("CONFIDENTIALITY"));

        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

        String configIntegrity = variables.get("INTEGRITY");
        if (configIntegrity.equals("H")) {
            String configH = variables.get("H");
            MessageDigest hash = MessageDigest.getInstance(configH);

        } else if (configIntegrity.equals("HMAC")) {
            String configMAC = variables.get("MAC");
            Mac hMac = Mac.getInstance(configMAC);

        }


    }
}

