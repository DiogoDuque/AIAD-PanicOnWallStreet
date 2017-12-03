package communication;

import com.google.gson.Gson;
import com.sun.istack.internal.NotNull;

public class NegotiationMessage extends Message {

    public String getJsonExtra() {
        return jsonExtra;
    }

    public enum NegotiationMessageType{
        MANAGER_SHARES, // every manager starts by sending a message with all its available shares

        NEW_PROPOSAL, // send a new proposal
        RENEGOTIATE_PROPOSAL, // attempt to renegotiate proposal //is this needed?
        PROPOSAL_REJECTED, // proposal rejected
        PROPOSAL_ACCEPTED, // proposal accepted

        CLOSE_DEAL, // attempt to close the deal
        CLOSE_DEAL_ACCEPT, // close the deal
        CLOSE_DEAL_REJECT // reject deal, but the proposal remains
    }

    @NotNull
    private NegotiationMessageType msgType; // message type. this is also important for interpreting the jsonExtra

    private String jsonExtra;  // contains extra information regarding the message type

    public NegotiationMessage(String senderCid, NegotiationMessageType type) {
        super(senderCid);
        msgType=type;
    }

    public NegotiationMessage(String senderCid, NegotiationMessageType type, String json) {
        super(senderCid);
        msgType=type;
        jsonExtra=json;
    }

    public NegotiationMessage(String senderCid, String receiverCid, NegotiationMessageType type) {
        super(senderCid, receiverCid);
        msgType=type;
    }

    public NegotiationMessage(String senderCid, String receiverCid, NegotiationMessageType type, String json) {
        super(senderCid, receiverCid);
        msgType=type;
        jsonExtra=json;
    }

    public NegotiationMessageType getMsgType(){
        return msgType;
    }

    @Override
    public String toJsonStr(){
        return new Gson().toJson(this);
    }

}
