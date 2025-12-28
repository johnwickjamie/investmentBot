/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package telegramAPI;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import botLogic.UserManager;
import textFileManager.txtUserFileManager;
import textFileManager.txtUsersStock;
import tiger.StockQuote;

/**
 *
 * @author james
 */
public class InvestmentBot extends TelegramLongPollingBot{

    public static final String WRONG_USER_ERROR = "Please login to the bot in order to use it";
    private static final Set<String> CRYPTO_SYMBOLS = new HashSet<>(Set.of(
        "BTC", "ETH", "BNB", "SOL", "DOGE", "ADA", "XRP", "AVAX", "DOT", "MATIC",
        "SHIB", "LTC", "TRX", "LINK", "ATOM", "ETC", "BCH", "XLM", "HBAR", "NEAR",
        "APT", "ARB", "OP", "VET", "ICP", "FIL", "SAND", "MANA", "EOS", "AAVE",
        "GRT", "RPL", "QNT", "ALGO", "FLOW", "CRO", "CHZ", "XTZ", "LDO", "TWT",
        "THETA", "ZIL", "ENJ", "XMR", "DASH", "SNX", "CAKE", "KAVA", "FTM", "ONE"
    ));

    private Map<Long, UserState> userStates = new HashMap<>(); // Track user states by chat ID
    private static long lastAvCallMs = 0;

    enum UserState{
        NEW_USER, //Users that just connected to the bot. Will only allow the user to create a new account
        IDLE,//Default state for logged in users
        ADDING_NEW_STOCK, //for when users are sending their list of stocks they want
    }

    public static void debugging (String message){
        System.out.println(message);
    }
    
    @Override
    public void onUpdateReceived(Update update) {    

        if(update.hasMessage()){
            Message message = update.getMessage();
            Long chatId = message.getChatId();
            String text = message.getText();

            if (text.equals("/start")){
                try{
                    if (isChatIDInFile(Long.toString(chatId))){
                        sendTextMessage(chatId, "You have already logged in, type '/' to see the full list of commands");
                    }else{
                        userStates.put(chatId, UserState.NEW_USER);
                        sendTextMessage(chatId, "Hello, welcome to the investment bot. To get started please send me your FIRST name in all caps.\nFor example:JAMES");
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }else if(text.equals("/price")){
                if (!isChatIDInFile(Long.toString(chatId)) || UserManager.hasTickers(Long.toString(chatId))) {
                    sendTextMessage(chatId, WRONG_USER_ERROR);
                }else{
                    try{
                    SendMessage sendmessage = new SendMessage();
                    sendmessage.setChatId(Long.toString(chatId));
                    sendmessage.setParseMode(ParseMode.HTML);

                    String[] stockTickers = getStockTickers(Long.toString(chatId));
                    List<String> stockPrices = new ArrayList<>();
                    boolean tooManyRequests = false;
                    for (String stockTicker : stockTickers) {
                        
                            try {
                                stockPrices.add(getStockPrice(stockTicker));                               
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }                                       
                    }
                
                    if (tooManyRequests == false){
                        StringBuilder stockPriceMessage = new StringBuilder();
                        for (int i = 0; i < stockTickers.length; i++){
                            stockPriceMessage.append(stockTickers[i])
                                .append(": $")
                                .append(stockPrices.get(i))
                                .append("\n");
                        }     
                        sendmessage.setText(stockPriceMessage.toString());          
                        try {
                            execute(sendmessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

            }else if(text.equals("/edit")){
                if (!isChatIDInFile(Long.toString(chatId))) {
                    sendTextMessage(chatId, WRONG_USER_ERROR);
                    return;
                }
                SendMessage sendmessage = new SendMessage();
                sendmessage.setChatId(Long.toString(chatId));
                sendmessage.setParseMode(ParseMode.HTML);
                sendmessage.setText("Please send the list of stocks you want to see\nSeperate each stock by a comma with no spaces. E.g: BTC,AAPL,DOGE");

                try {
                    execute(sendmessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

                userStates.put(chatId, UserState.ADDING_NEW_STOCK);
                
            }else{

                UserState currentState = userStates.getOrDefault(chatId, UserState.IDLE); //stores users current state, if the user does not have a state then the user will be placed in deafult "idle" state
                // Determine what to do based on the user's current state
                switch(currentState){
                    case NEW_USER:
                        try {                           
                            if (UserManager.doesUserExist(text) == true){ //CHECKS: if user already exists
                                sendTextMessage(chatId, "User already exists, please make sure you have sent the correct name");
                                break;
                            }else{ //CHECKS: The method will check if the user is in the duty list before creating the new user, if not it will return false which is why it can be in the if statement
                                addUser(text, Long.toString(chatId));
                                sendTextMessage(chatId, "Hello " + text + ", user profile sucessfully created. To get started, type /edit to send me a list of stocks");
                                userStates.put(chatId, UserState.IDLE); // set to a normal user in idle state
                                break;
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            sendTextMessage(chatId, "Error creating user profile, please try again (IO EXCEPTION)");
                            break;
                        }

                    case IDLE:
                        // If no specific state, handle as general input or command
                        sendTextMessage(chatId, "I'm not sure what you're asking. Try typing / to see the full list of commands");
                        break;
                    
                    case ADDING_NEW_STOCK:
                        try {
                            storeTickers(parseTickers(text), Long.toString(chatId));
                        } catch (IOException e) {
                            System.out.println("Error storing user stock tickets");
                            e.printStackTrace();
                            sendTextMessage(chatId, "Sorry there was an issue storing your stock profile, please try again later");
                            userStates.put(chatId, UserState.IDLE);
                            break;
                        }
                        sendTextMessage(chatId, "Stock profile stored successfully, type /price to see the current price of your stocks");
                        userStates.put(chatId, UserState.IDLE);
                        break;
                }

            }

        } else if (update.hasCallbackQuery()){
            
        }

    }
    
    //<---------Getting stock prices------->
    //TODO: ADD methods for parsing crypto
    private static String getStockPrice(String stockTicker) throws InterruptedException {
        String symbol = sanitizeTicker(stockTicker);
        if (symbol.isEmpty()) {
            System.out.println("Invalid ticker from user");
            return "Invalid ticker";
        }

        try {
            StockQuote sq = new StockQuote();
            Double price = sq.delayedPriceCached(symbol);
            return Double.toString(price);
        } catch (Exception e) {
            System.err.println("Error fetching quote: " + e.getMessage());
            return "Error getting stock price";
        }
    }

    private static boolean isCrypto(String symbol) {
        return CRYPTO_SYMBOLS.contains(symbol.toUpperCase());
    }

    private static String sanitizeTicker(String raw) {
    if (raw == null) return "";

    String s = raw.trim().toUpperCase();

    // If separators slip through, only take the first token.
    s = s.split("[,;\\s]+", 2)[0];

    s = s.replaceAll("[^A-Z0-9._-]", "");
    return s;
    }

    private static boolean isNumeric(String s) {
        if (s == null) {
            return false;
        }
        try {
            new BigDecimal(s.trim());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String roundToDecimalPlace(String price) {
        double num = Double.parseDouble(price);

        if (num == 0.0) {
            return BigDecimal.ZERO.toPlainString();
        }

        BigDecimal num2 = BigDecimal.valueOf(num);
        BigDecimal bd = num2.setScale(2, RoundingMode.HALF_UP);

        return bd.toPlainString();
    }

    /**
     * 
     * @param tickers
     * @return An String array with the users's stock tickers
     */
    private static String[] parseTickers(String tickers) {
        if (tickers == null || tickers.isBlank()) {
            return new String[0];
        }

        return Arrays.stream(tickers.split("[,;]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(InvestmentBot::sanitizeTicker)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

    private static void storeTickers (String[] tickers, String chatId) throws IOException{
        txtUsersStock txtuserstock = new txtUsersStock();
        txtuserstock.saveUserTickers(chatId, tickers);
    }

    private static String[] getStockTickers (String chatId){
        txtUsersStock txtuserstock = new txtUsersStock();
        return txtuserstock.loadUserTickers(chatId);
    }

    private static void addUser (String userName, String chatId) throws IOException{
        txtUserFileManager userfilemanager = new txtUserFileManager();
        userfilemanager.addUsername(userName, chatId);
    }

    private void sendTextMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setParseMode("HTML"); //allows for HTML tags to be used in messages, for formatting the text
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.out.println(e + ", Error sending message");
        }
    }

    private boolean isChatIDInFile (String chatId){
        txtUserFileManager txtuserfilemanager = new txtUserFileManager();
        try {
            return txtuserfilemanager.isChatIDInFile(chatId);
        } catch (IOException e) {
            System.out.println(e + ", Error checking if chatId exists");
            return false;
        } catch (IndexOutOfBoundsException e){
            return false;
        }
    }

    public static String getCurrentTime() {
        LocalTime now = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return now.format(formatter);
    }
    
    


  //<------------------------------------KEY METHODS FOR TELEGRAM API-------------------------------------------->
    @Override
    public String getBotUsername() {
        return "mcdonnell_investment_bot";
    }

    @Override
    public String getBotToken() {
        return "8241373705:AAFMW9scHCKgz6L0jhWQdHWHjDv2KYvg9ug";
    }
}
