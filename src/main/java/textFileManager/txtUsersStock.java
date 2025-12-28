/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package textFileManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author james
 */
public class txtUsersStock {
    private final String filePath;

    public txtUsersStock (){
        filePath = "./permData/userStockTickers.txt";
    }

    /**
     * Reads a user's saved stock/crypto tickers from a file based on their chat ID.
     *
     * @param chatId   the user's chat ID
     * @param filePath the file path containing chat ID and tickers
     * @return a String array of tickers, or an empty array if no match is found
     */
    public String[] loadUserTickers(String chatId) {
    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line == null || line.isBlank()) continue;

            // Expected: chatId,<semicolon-separated tickers>
            // Tolerate legacy comma-separated storage too.
            String[] parts = line.split(",", 2);
            String id = parts[0].trim();
            if (!id.equals(chatId)) continue;

            String stored = parts.length > 1 ? parts[1].trim() : "";
            if (stored.isBlank()) return new String[0];

            return Arrays.stream(stored.split("[;,]+"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toArray(String[]::new);
        }
    } catch (IOException e) {
        System.err.println("Error reading file: " + e.getMessage());
    }
    return new String[0];
}

        /**
     * Stores a user's tickers to a text file. Each line is:
     * chatId,<semicolon-separated tickers>
     *
     * - If the chatId already exists, its tickers are overwritten.
     * - If not, a new entry is added.
     *
     * @param chatId   the user's chat ID
     * @param tickers  array of tickers (e.g., {"AAPL","BTC","DOGE"})
     * @param filePath path to the text file (e.g., "tickers.txt")
     * @throws IOException if reading/writing fails
     */
    public void saveUserTickers(String chatId, String[] tickers) throws IOException {
        Path path = Paths.get(filePath);
        Map<String, String> map = new LinkedHashMap<>();

        // Read existing entries (if any)
        if (Files.exists(path)) {
            for (String line : Files.readAllLines(path)) {
                if (line == null || line.isBlank()) continue;
                String[] parts = line.split(",", 2); // chatId, tickersJoined
                String id = parts[0].trim();
                String stored = parts.length > 1 ? parts[1].trim() : "";
                if (!id.isEmpty()) map.put(id, stored);
            }
        }

        // Normalize + dedupe incoming tickers, then join with semicolons
        String joined = Arrays.stream(tickers == null ? new String[0] : tickers)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .distinct()
                .collect(Collectors.joining(";"));

        // Overwrite or add this user's entry
        map.put(chatId, joined);

        // Write back atomically
        List<String> out = new ArrayList<>(map.size());
        for (Map.Entry<String, String> e : map.entrySet()) {
            out.add(e.getKey() + "," + e.getValue());
        }
        Files.write(path, out, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
    
        /**
     * Checks whether a user has any saved stock/crypto tickers.
     *
     * @param chatId the user's chat ID
     * @return true if the user exists in the file and has >= 1 ticker saved; false otherwise
     */
    public boolean userHasSavedTickers(String chatId) {
        if (chatId == null || chatId.isBlank()) return false;

        String[] tickers = loadUserTickers(chatId.trim());
        return tickers != null && tickers.length > 0;
    }

}
