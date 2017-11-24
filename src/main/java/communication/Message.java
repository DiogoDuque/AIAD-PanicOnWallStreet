package communication;

import com.google.gson.Gson;
import com.sun.istack.internal.NotNull;

import java.io.Serializable;

public class Message {

    @NotNull
    private String senderCid;

    private String receiverCid;

    @NotNull
    private String msg;

    public Message(String senderCid, String receiverCid, String msg) {
        this.senderCid = senderCid;
        this.receiverCid = receiverCid;
        this.msg = msg;
    }

    public Message(String senderCid, String msg) {
        this.senderCid = senderCid;
        this.msg = msg;
    }

    public String getSenderCid() {
        return senderCid;
    }

    public String getReceiverCid() {
        return receiverCid;
    }

    public String getMsg() {
        return msg;
    }

    public String toJsonStr(){
        return new Gson().toJson(this);
    }
}
