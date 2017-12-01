package communication;

import com.google.gson.Gson;
import com.sun.istack.internal.NotNull;

public abstract class Message {

    @NotNull
    private String senderCid;

    private String receiverCid;

    public Message(String senderCid, String receiverCid) {
        this.senderCid = senderCid;
        this.receiverCid = receiverCid;
    }

    public Message(String senderCid) {
        this.senderCid = senderCid;
    }

    public String getSenderCid() {
        return senderCid;
    }

    public String getReceiverCid() {
        return receiverCid;
    }

    public String toJsonStr(){
        return new Gson().toJson(this);
    }

    public boolean sentByInvestor() {
        return senderCid.startsWith("Investor");
    }

    public boolean sentByManager() {
        return senderCid.startsWith("Manager");
    }
}
