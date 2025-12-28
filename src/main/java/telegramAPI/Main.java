/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package telegramAPI;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 *
 * @author james
 */
public class Main {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    public static void main(String[] args) {
        globalConfig.APITracker[0] = "0";
        if (globalConfig.APITracker == null) {
            globalConfig.APITracker = new String[] { "0", LocalTime.now().format(FORMATTER) };
        }
        
        try {
           TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
           botsApi.registerBot(new InvestmentBot());



       } catch (TelegramApiException e) {
           System.out.println("Error with main method " + e);
       }  

    }
    
}
