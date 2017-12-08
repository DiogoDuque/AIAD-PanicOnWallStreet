package communication;

import com.google.gson.Gson;

public class IncomeMessage extends Message {

    /**
     * Message types for every possible (Manager or Investor) Income Message sent.
     */
    public enum MessageType {
        ASK_INVESTOR_INFO,
        INVESTOR_INFO,
        INVESTOR_RESULT,

        ASK_MANAGER_INFO,
        ASK_INVESTOR_FOR_MANAGER_INCOME,
        MANAGER_INCOME_RESULT
    }

    /**
     * Message type. this is also important for interpreting the jsonExtra.
     */
    private MessageType type;

    /**
     * (Not always required) JSON string containing extra information, regarding the specific type of message.
     */
    private String jsonExtra;

    public IncomeMessage(String senderCid, MessageType type){
        super(senderCid);
        this.type = type;
    }

    public IncomeMessage(String senderCid, MessageType type, String jsonExtra){
        super(senderCid);
        this.type = type;
        this.jsonExtra = jsonExtra;
    }

    public IncomeMessage(String sender, String receiver, MessageType type, String jsonExtra) {
        super(sender, receiver);
        this.type = type;
        this.jsonExtra = jsonExtra;
    }

    public MessageType getMsgType() {
        return type;
    }

    public String getJsonExtra() {
        return jsonExtra;
    }

    @Override
    public String toJsonStr() {
        return new Gson().toJson(this);
    }
}
