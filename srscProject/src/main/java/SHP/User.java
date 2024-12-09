package SHP;

import java.io.BufferedReader;
import java.io.FileReader;

public class User {
    private final static String databaseFilePath = "srscProject/src/main/resources/userDatabase.txt";
    private final static String clientKeysFilePath = "srscProject/src/main/resources/clientEccKeyPair.sec";
    private final static String serverKeysFilePath = "srscProject/src/main/resources/ServerEccKeyPair.sec";

    public static String getUserPasswordFromFile(String userId) {
        try (BufferedReader br = new BufferedReader(new FileReader(databaseFilePath))) {
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

    public static String getUserPrivateKeyFromClientFile (String userId) {
        try (BufferedReader br = new BufferedReader(new FileReader(clientKeysFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("UserId,")) {
                    continue;
                }

                String[] columns = line.split(",");

                if (columns[0].equals(userId)) {
                    return columns[2];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getUserPublicKeyFromClientFile (String userId) {
        try (BufferedReader br = new BufferedReader(new FileReader(databaseFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("UserId,")) {
                    continue;
                }

                String[] columns = line.split(",");

                if (columns[0].equals(userId)) {
                    return columns[3];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
