package communication;

import com.google.gson.Gson;

public class PhaseMessage extends Message {

    private final MessageType type;

    public enum MessageType {
        NEGOTIATION
        // TODO add more
    }

    public PhaseMessage(MessageType type){
        super(null);
        this.type = type;
    }

    @Override
    public String toJsonStr() {
        return new Gson().toJson(this);
    }
}
