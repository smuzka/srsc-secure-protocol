package SHP;

import java.io.BufferedReader;
import java.io.FileReader;

public class User {
    private static String databaseFilePath;
    private static String clientKeysFilePath;
    private static String serverKeysFilePath;

    public static void setFilePath (String filePath) {
        databaseFilePath = filePath + "userDatabase.txt";
        clientKeysFilePath = filePath + "clientEccKeyPair.sec";
        serverKeysFilePath = filePath + "ServerEccKeyPair.sec";
    }

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
