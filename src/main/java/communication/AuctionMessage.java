package communication;

import com.google.gson.Gson;

public class AuctionMessage extends Message {

    public enum MessageType{
        SHARE_AUCTION,
        BID_ON_SHARE,
        BID_REJECTED,
        SHARE_SOLD
    }

    private MessageType msgType;

    private final String jsonExtra;

    public AuctionMessage(String sender, MessageType msgType, String jsonExtra){
        super(sender);
        this.msgType = msgType;
        this.jsonExtra = jsonExtra;
    }

    public AuctionMessage(String sender, String receiver, MessageType msgType, String jsonExtra){
        super(sender, receiver);
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
