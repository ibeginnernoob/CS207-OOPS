package oops;

import java.io.*;
import java.util.*;

public class IphoneAvailability {

    private Map<String, List<String>> iPhoneData = new HashMap<>();

    // Parse the file to extract iPhone availability by region
    public void parseFile(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            String currentModel = null;

            while ((line = br.readLine()) != null) {
                // Check for the iPhone model
                if (line.startsWith("Extracted Data from:")) {
                    currentModel = line.substring(line.lastIndexOf('/') + 1).replace('_', ' ');
                }

                // Check for availability by region
                if (line.startsWith("Availability by region:") && currentModel != null) {
                    String[] regions = line.substring(line.indexOf(":") + 1).trim().split("\\s+");
                    iPhoneData.put(currentModel, Arrays.asList(regions));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Search for iPhones available in a specific region
    public List<String> searchByRegion(String region) {
        List<String> availableModels = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : iPhoneData.entrySet()) {
            if (entry.getValue().contains(region)) {
                availableModels.add(entry.getKey());
            }
        }
        return availableModels;
    }

    public static void main(String[] args) {
    	IphoneAvailability parser = new IphoneAvailability();
        String filePath = "Output_DFS.txt"; // Replace with the actual file path

        // Parse the file
        parser.parseFile(filePath);

        // Search for iPhones available in India
        String searchRegion = "India";
        List<String> modelsInIndia = parser.searchByRegion(searchRegion);

        // Output the results
        if (modelsInIndia.isEmpty()) {
            System.out.println("No iPhones available in " + searchRegion);
        } else {
            System.out.println("iPhones available in " + searchRegion + ": " + modelsInIndia);
        }
    }
}

