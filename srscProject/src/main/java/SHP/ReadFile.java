package SHP;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ReadFile {
    public static String readFileContent(String filePath) {
        StringBuilder content = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content.toString();
    }

    public static String mapToString(Map<String, String> map) {

        return map.entrySet()
                .stream()
                .map(entry -> entry.getKey() + ":" + entry.getValue())
                .collect(Collectors.joining("\n"));
    }

    public static Map<String, String> getVariables(String[] args) {
        // Path to the file, change for correct path while saving files
        // String filePath = "../../../srscProject/src/main/resources/cryptoconfig.txt";
        String filePath = "srscProject/src/main/resources/ciphersuite.conf";

        // HashMap to store the variables and their values
        Map<String, String> variables = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;

            // Read each line of the file
            while ((line = br.readLine()) != null) {
                // Split the line into two parts: variable name and value
                String[] parts = line.split(":");

                // Ensure that there are exactly two parts after splitting
                if (parts.length == 2) {
                    String variableName = parts[0].trim(); // Trim any extra spaces
                    String value = parts[1].trim(); // Trim any extra spaces

                    // Add to the map
                    variables.put(variableName, value);
                } else {
                    System.out.println("Skipping invalid line: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Configuration:");
        // Output the variables and values
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }

        return variables;
    }

    public static Map<String, String> getVariables(String configData) {
        Map<String, String> variables = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new StringReader(configData))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");

                if (parts.length == 2) {
                    String variableName = parts[0].trim();
                    String value = parts[1].trim();

                    variables.put(variableName, value);
                } else {
                    System.out.println("Skipping invalid line: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Configuration:");
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }

        return variables;
    }
}