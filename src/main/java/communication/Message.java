package communication;

public abstract class Message {

    /**
     * Sender's cid.
     */
    private String senderCid;

    /**
     * Receiver's cid.
     */
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

    /**
     * Converts this object to a JSON string.
     * @return JSON string of this object.
     */
    public abstract String toJsonStr();
}
