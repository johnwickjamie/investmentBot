/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package botLogic;

import java.io.IOException;


import textFileManager.txtUserFileManager;
import textFileManager.txtUsersStock;

/**
 *
 * @author james
 * This class is mainly used to interface with the txtUserFileManager class which manages the UserList.txt
 * Reason why the bot can't just use the txtUserFileManager class is that there still is some logic that needs to be handled after-
 * calling the methods with the txtUserFileManager class
 * 
 */
public class UserManager {
   
   
    public static boolean doesUserExist (String username) throws IOException{
        txtUserFileManager txtuserfilemanager = new txtUserFileManager();

        return txtuserfilemanager.isRealNameInFile(username);
    }

    public static String findNameFromChatID (String chatId){
        txtUserFileManager txtuserfilemanager = new txtUserFileManager();
        try {
            return txtuserfilemanager.getNameFromChatID(chatId);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.print(": Likely an issue with the filePath");
            return "Error finding name for " + chatId;
        }
    }

    public static String findchatIdFromName (String username){
        txtUserFileManager txtuserfilemanager = new txtUserFileManager();
        try {
            return txtuserfilemanager.getchatIDFromName(username);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.print(": Likely an issue with the filePath");
            return "Error finding chatID for " + username;
        }
    }

    public static String getAllUsers (){
        txtUserFileManager txtuserfilemanager = new txtUserFileManager();
        try {
            return txtuserfilemanager.getAllUserNames();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.print(": Likely an issue with the filePath");
            return "Error finding all users";
        }

    }

    public static String removeUser(String username) throws IOException{
        txtUserFileManager txtuserfilemanager = new txtUserFileManager();
        try {
            if (txtuserfilemanager.deleteUser(username)){
                return "User " + username + " has been removed";
            }else{
                return "User " + username + " does not exist";
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.print(": Likely an issue with the filePath");
            return "Error finding all users";
        }
    }
    
    public static boolean hasTickers (String chatId){
        txtUsersStock userstock = new txtUsersStock();
        return userstock.userHasSavedTickers(chatId);       
    }


    
}
