package communication;

import com.google.gson.Gson;

public abstract class Message {

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

    public abstract String toJsonStr();

    public boolean sentByInvestor() {
        return senderCid.startsWith("Investor");
    }

    public boolean sentByManager() {
        return senderCid.startsWith("Manager");
    }
}
