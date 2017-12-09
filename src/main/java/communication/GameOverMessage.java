package communication;

import com.google.gson.Gson;

public class GameOverMessage extends Message {

    public enum MessageType {
        ASK_GAMEOVER_INFO,
        SEND_GAMEOVER_INFO
    }

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
