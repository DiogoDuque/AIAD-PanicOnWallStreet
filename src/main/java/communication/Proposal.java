package communication;

import assets.Share;
import com.google.gson.Gson;

public class Proposal {

    private Share share;

    private int value;

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

    public String toJsonStr() {
        return new Gson().toJson(this);
    }
}
