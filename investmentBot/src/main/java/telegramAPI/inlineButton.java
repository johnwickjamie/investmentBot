package telegramAPI;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;


public class inlineButton {
    private static final int MAX_INLINEBUTTON_COLLUMNS = 8;
    int messageId;
    int buttonCollumns;
    int maxCollumns;

    InlineKeyboardMarkup inlinekeyBoardMarkup = new InlineKeyboardMarkup();
    List<List<InlineKeyboardButton>> inlinebuttons = new ArrayList<>();
    List<InlineKeyboardButton> inlineKeyboardButtonList = new ArrayList<>();
    
    /**
     * Note the constructor will not create an exit button if the value of the formType == 999
     * @param messageId ensure this message id is the id of the bot's message (so + 1)
     * @param maxCollumns Specified how many collumns should be in a row. MAX 8 COLLUMNS
     * @param formType If =999 exit button will not be created
     */
    public inlineButton(int messageId, int maxCollumns, int formType){
        this.messageId = messageId;
        this.maxCollumns = maxCollumns;
        buttonCollumns = 0;
        //creating exit button if formType value indicates as such
        if (formType != 999){
            inlineKeyboardButtonList.add(exitButton((messageId), formType)); //adding exit button
            inlinebuttons.add(new ArrayList<>(inlineKeyboardButtonList)); // Add the current row of buttons
            inlineKeyboardButtonList.clear(); // Clear the list for the next row
        }
    }

    /**
     * This method handles the creation of a individual button and handles the creation of new rows when reached the maximum number of collumns
     * 
     * @param data text the button displays
     * @param callBackData the callback data that will be stored in the button
     */
    public void addButton (String data, String callBackData){
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText(data); // Set up the inline buttons and store them in the list objects
        inlineKeyboardButton1.setCallbackData(callBackData); // Set callback data. Callback data format: Command, data, messageId
        inlineKeyboardButtonList.add(inlineKeyboardButton1); // Store a button to the list

        if (buttonCollumns % maxCollumns == 0) { 
            inlinebuttons.add(new ArrayList<>(inlineKeyboardButtonList)); // Add the current row of buttons
            inlineKeyboardButtonList.clear(); // Clear the list for the next row
        }
        buttonCollumns++;

    }

    /**
     *  Cleans up the button lists and returns the buttons in an object that can be stored in the sendMessage object
     * @return the inlinekeyboardmarkup object that can be stored in the sendMessage object
     */
    public InlineKeyboardMarkup getInlineButton (){
        // Add any remaining buttons
        if (!inlineKeyboardButtonList.isEmpty()) {
            inlinebuttons.add(inlineKeyboardButtonList);
        }  
        
        inlinekeyBoardMarkup.setKeyboard(inlinebuttons);                         
        return inlinekeyBoardMarkup;

    }

    /**
     * This method returns the exit button for the inline button forms. formType is used to determin which hashmaps to clear data from. 
     * 
     * formType = 1: unavailable dates form
     * @param messageId
     * @param formType
     * @return exitButton (inlineKeyboardButton)
     */
    private InlineKeyboardButton exitButton(int messageId, int formType){
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();        
        inlineKeyboardButton1.setText("Exit"); //lines sets up the inline buttons and stores them in the list objects 
        inlineKeyboardButton1.setCallbackData("Exit," + formType + "," + messageId); //CALLBACK DATA: This is the data that will be sent to the bot when the button is pressed. The message ID is added by one as the next message sent should be from the bot, source: https://handbook.tmat.me/en/messages/id#:~:text=Messages%20in%20personal%20dialogs%20and,ID%202%2C%20and%20so%20on.&text=A%20bot%20can%20retrieve%20a%20message%20by%20its%20ID%20through%20Telegram%20API.
        return inlineKeyboardButton1;
    }
    
}
