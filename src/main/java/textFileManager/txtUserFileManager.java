/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package textFileManager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jamesmcdonnell
 * Class contains methods that interfaces with the UserList.txt file
 * 
 */
public class txtUserFileManager {
    String  filePath;
    
     public txtUserFileManager() {
        filePath = "./permData/userList.txt";
    }
    public void addUsername(String userName, String chatId) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(userName + "," + chatId);
            writer.newLine();
        }
    }


    public String getchatIDFromName(String username) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts[0].equals(username)) {
                return parts[1];
            }
        }
        return null;
    }

    public String getNameFromChatID(String username) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts[1].equals(username)) {
                return parts[0];
            }
        }
        return null;
    }
    
    //Checks if a inputed name is already in the userList
     public boolean isRealNameInFile(String realName) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts[0].trim().equals(realName.trim())){ //checks if name is in DOO and CDOO list
                return true;
            }
        }
        return false;
    }

    public boolean isChatIDInFile(String chatID) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts[1].trim().equals(chatID.trim())) {
                return true;
            }
        }
        return false;
    }

    //returns all the usernames in a string
    public String getAllUserNames() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        StringBuilder allNames = new StringBuilder();
        for (String line : lines) {
            String[] parts = line.split(",");
            allNames.append(parts[0]).append("\n");
        }
        return allNames.toString().trim();
    }

    public ArrayList<String> getAllUserNamesArray () throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        ArrayList <String> names = new ArrayList<>();
        for (String line : lines) {
            String[] parts = line.split(",");
            names.add(parts[0]);
        }
        return names;
    }

    /**
     * 
     * @return returns an array that is parallel to the usernames array
     * @throws IOException
     */
    public ArrayList<String> getAllchatIdArray () throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        ArrayList <String> chatIds = new ArrayList<>();
        for (String line : lines) {
            String[] parts = line.split(",");
            chatIds.add(parts[1]);
        }
        return chatIds;
    }

    public void eraseTxtFile() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Writing an empty string to the file to erase its contents
            writer.write("");
        }
    }

    public boolean deleteUser(String username) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        boolean userDeleted = false;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String line : lines) {
                String[] parts = line.split(",");
                if (!parts[0].equals(username)) {
                    writer.write(line);
                    writer.newLine();
                } else {
                    userDeleted = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.print(": Likely an issue with the filePath");
        }
        return userDeleted;
    }
}
