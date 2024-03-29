package communication.message;

import com.google.gson.Gson;

public class GameOverMessage extends Message {

    /**
     * Message types for every possible GameOver Message sent.
     */
    public enum MessageType {
        ASK_GAMEOVER_INFO,
        SEND_GAMEOVER_INFO
    }

    /**
     * Message type. this is also important for interpreting the jsonExtra.
     */
    private MessageType msgType;

    private String jsonExtra;

    public GameOverMessage(String sender, MessageType msgType){
        super(sender);
        this.msgType = msgType;
    }

    public GameOverMessage(String sender, MessageType msgType, String jsonExtra){
        super(sender);
        this.msgType = msgType;
        this.jsonExtra = jsonExtra;
    }

    public MessageType getMsgType() {
        return msgType;
    }

    public String getJsonExtra() {
        return jsonExtra;
    }

    @Override
    public String toJsonStr() {
        return new Gson().toJson(this);
    }
}
