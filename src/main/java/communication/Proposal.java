package communication;

import assets.Share;
import com.google.gson.Gson;

public class Proposal {

    private Share share;

    private int value;

    private boolean accepted;

    public Proposal(Share share, int value) {
        this.share = share;
        this.value = value;
    }

    public Share getShare() {
        return share;
    }

    public int getValue() {
        return value;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public String toJsonStr() {
        return new Gson().toJson(this);
    }
}
